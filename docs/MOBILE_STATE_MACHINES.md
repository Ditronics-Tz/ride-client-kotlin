# Mobile screen state machines and regression ledger

Audit date: 2026-11-07. Scope: current passenger and driver Android source, all screen composables, their navigation, repositories, authentication and background location. This is the single state-machine specification and grouped issue ledger for this task. Tests below exercise production repository methods with controlled Retrofit responses; no live accounts, device, emulator or deployed-server results are implied.

## Reading the tables

A state is the combination of visible presentation, pending work and authoritative data, even when represented by independent booleans. `Ready(data)` can retain data while refreshing or showing an error. `Submitting(command)` includes a frozen payload and an idempotency key. A retry of an uncertain command must reuse that key; a changed payload is a different command. A successful HTTP mutation and a failed subsequent refresh are separate outcomes.

Global invariants:

1. Only a current login may publish private data or navigate to an authenticated destination. Logout invalidates requests, cached lists, route/quote state and account-specific pending keys. Token rotation within a login is not an account switch.
2. A terminal ride cannot become active because a delayed response arrived. A passenger cannot clear an active ride merely because cancellation was attempted.
3. An action applies to the identity and phase that the user saw. Duplicate taps and polling cannot accidentally advance a second phase.
4. A visible success requires the corresponding server acknowledgement. Unsupported capabilities must stay unavailable; local validation does not send a reset email, verify an identity, create a booking or charge a payment.
5. Coroutine cancellation propagates. Work leaving a screen or account must not publish an error, reset a newer loading flag or continue a retry loop.
6. Data freshness belongs to the query/request that produced it. Old quotes, address searches and preference requests cannot overwrite newer input.

## Machine A — login, startup and navigation (both apps)

Owners: each `MainActivity.kt`, `data/local/SessionManager.kt`, `data/remote/RidaApiClient.kt`, auth screens and navigation graphs. Independent dimensions: splash/bootstrap, logged out/logged in, incomplete/complete profile, authentication request idle/pending/error, token valid/refreshing/invalidated.

| From | Event / guard | To / side effect |
| --- | --- | --- |
| Bootstrap | no saved login | passenger onboarding / driver sign-in |
| Bootstrap | saved login; best-effort profile refresh | Home; retain session on temporary network failure |
| Auth editing | invalid fields | editing with field errors; no request |
| Auth editing | valid submission | pending; disable repeat submission |
| Pending | server credentials accepted | persist a new login identity, navigate Home and remove auth stack |
| Pending | HTTP/network failure | editing with error; retain input |
| Any pending screen | dispose/cancel | cancel work; no success navigation |
| Logged in | 401 with access token rotated for same subject | replay once using current token |
| Logged in | 401 requiring refresh | serialize refresh; compare expected refresh token before saving |
| Refreshing | stale response after logout/new login | discard; must not resurrect or replace session |
| Logged in | logout / rejected refresh | clear session and private state; navigate sign-in; stop driver location |
| Logged out | late `/me` or profile success | discard via expected login identity inside DataStore edit |

Auth pending subphases are backend challenge → platform credential picker → backend exchange → session persistence. A provider credential alone is not a Rida login. A replacement login clears previous optional fields, credentials and account caches before its new identity becomes observable. Successful sign-in/signup clears the root auth stack; Home must not have an earlier auth screen beneath it.

Invariant: startup destination is distinct from authentication truth; protected routes react to current session invalidation. `TokenSessionBoundary` prevents replaying an earlier actor's request with a later actor's token. Google credentials are exchanged with the backend before persistence. Password reset currently has no public backend route; its valid-email state must be **unavailable**, never **email sent**. Driver enrollment verification is distinct from login/profile completeness. Driver typed verification states are APPROVED/PENDING/REJECTED; typed document states are APPROVED/PENDING/REJECTED/EXPIRED/MISSING. The canonical enrollment screen displays the server's document/review status, including uploaded-but-not-yet-submitted documents.

## Machine B — passenger conventional ride, quote and receipt

Owners: `RideRepository.kt`, `RideModels.kt`, `RideMappers.kt`, Home map, Ride details, Ride history, schedule/package surfaces. Presentation phases: no ride, creating, SEARCHING, DRIVER_FOUND, ARRIVING, IN_TRIP, cancelling, terminal receipt/history. `IDLE` is a mapping value, not an active completed trip. Route selection and quoting are a parallel machine.

| From | Event / guard | To / side effect |
| --- | --- | --- |
| No route | select destination + pickup | route ready; issue quote tied to query revision |
| Quoting | newer route/clear/logout | invalidate old revision; late quote discarded |
| History loading | newer refresh completes first | keep newer request's records/loading/error; discard older completion |
| Notification link | requested terminal ride ID loads | select that historical ride; preserve unrelated current active trip |
| Route ready / no ride | request | reserve pending action before launching; send frozen request |
| Creating | repeat tap | no second command |
| Creating | uncertain network failure | route ready + error; retain matching command key for retry |
| Creating | successful active response | SEARCHING/DRIVER_FOUND/ARRIVING/IN_TRIP; begin polling |
| Any active phase | nonterminal server snapshot | render mapped server phase |
| Active, before IN_TRIP | cancel | pending cancellation; retain ride and tracking |
| IN_TRIP | local cancel | reject with support guidance; preserve ride |
| Cancelling | HTTP/network failure | active + error; same ride/key available for retry |
| Cancelling | terminal acknowledgement | clear active ride/pin, refresh history, finish cancellation navigation |
| Polling / restore | completed or finished | completed receipt/history; no active ride |
| Polling / restore | cancelled, cancelled_by_*, failed, expired, no_driver_found | history/no active ride; no fabricated completion receipt |
| Active | concurrent poll and command | serialize authoritative reads and writes |
| History/receipt | book again | resolve historical endpoints into a new route, then create; no stale map destination |
| Any | logout | cancel owned work and clear private ride/route/history state |

Backend status evidence: `RideBackend-Rewrite-/store/ride_dispatch.go` defines requested, searching, offered, assigned, driver_arrived, in_progress and the terminal values; its cancellation path returns `cancelled_by_passenger`/driver. `driver_arrived` maps to ARRIVING. Terminal values are excluded from restoration and included in history.

Scheduled booking states: editable → resolving addresses → submitting → acknowledged/upcoming or error. Empty geocoder results and invalid/past timestamps are errors, not silent exits. Cancellation requires server success before removing an upcoming row. Courier form is a separate product capability: recipient/package fields have no supported conventional ride contract, so it cannot claim a courier was requested.

## Machine C — driver conventional trip, availability, offers and earnings

Owners: `RemoteDriverRepository.kt`, `DriverRules.kt`, `DriverModels.kt`, Driver home, Trips, Earnings and completion/rating dialogs.

| From | Event / guard | To / side effect |
| --- | --- | --- |
| Offline | request online; server accepts | online; start permitted foreground location |
| Availability pending | failure | previous authoritative availability + error; pending clears |
| Online/no trip | offer poll | offer displayed with remaining deadline |
| Offer | local countdown expires | expired display; backend still validates acceptance |
| Offer | decline acknowledged | remove that offer only |
| Offer/no trip | accept acknowledged | TO_PICKUP; offer cleared; availability reflects accepted commitment |
| TO_PICKUP | arrived acknowledged | ARRIVED |
| ARRIVED | start acknowledged | IN_TRIP |
| IN_TRIP | complete acknowledged | no active trip; deduplicated completed record and rating dialog |
| TO_PICKUP or ARRIVED | cancellation acknowledged | cancelled history; no active trip |
| IN_TRIP or terminal | local cancellation/advance beyond terminal | reject |
| Lifecycle command pending | duplicate phase action | reject if requested identity/phase changed; never auto-advance the next phase |
| Poll pending + command | either arrives first | serialize; old poll cannot undo accepted/advanced trip |
| Closing trip | subsequent offer request fails | retain authoritative completion/history; report offer connection error |
| Completed | rating 1..5 acknowledged | update rating locally |
| Rating | non-2xx | error; no local successful rating |
| Earnings | change TODAY/WEEK/MONTH | clear old summary, fetch new period; cancel old effect |
| Any | logout/new login | replace repository instance; invalidate navigation and stop service |

Availability acknowledgement must not be undone by a redundant location request failing. History is keyed by trip ID so retried completion cannot duplicate a record. A ride response's server phase is authoritative; local `nextPhase` only selects the endpoint. Earnings totals are server results, not locally simulated income. Legacy vehicle/document editor composables are not the canonical navigation targets; `DriverEnrollment` is.

## Machine D — shared rides (both apps)

Owners: `TransportApi.kt`, `SharedRideScreen.kt`, `SharedCommon.kt`; driver also `SharedRoutePreview.kt` and enrollment. State is the product of trip aggregate, passenger seat, pending command, connectivity, pagination/filter and screen mode.

Trip states: waiting → in_progress → completed; waiting → cancelled or expired. Seat states: held → confirmed → completed; held → released/expired; relevant trip termination cancels/expires seats. Server enforces capacity, manifest, authorization, PIN attempts, deadlines and one active commitment across hail/shared modes.

| From | Event / guard | To / side effect |
| --- | --- | --- |
| Passenger browse | choose area/filter/page | nearby loading → list/map or connection error |
| Nearby waiting trip | reserve acknowledged | held seat; show server PIN only while lease remains valid |
| Held | elapsed monotonic time reaches server expiry | expired display; hide PIN even without network |
| Held | release acknowledged | released; return to browse |
| Held | driver's matching PIN verification acknowledged | confirmed seat; membership still server controlled |
| Driver empty | enter endpoints, capacity, seats, price, wait | editable draft |
| Valid draft | review | frozen publish payload + fee/route summary |
| Reviewing | confirm | durable idempotent publish → waiting or error |
| Waiting with confirmed manifest | start acknowledged | in_progress |
| Waiting | cancel with required reason acknowledged | cancelled |
| In progress | complete acknowledged | completed receipt/history; ratings allowed for completed membership |
| Any active trip | SOS/support | independent pending request; show acknowledged result or error |
| Any | reload/long poll | merge aggregate versions; lower version cannot undo later state |
| Any command | network failure/retry | retained actor/signature key; no overlapping screen commands |
| Any screen | account identity changes | dispose prior composition and pending effects; clear identity-bound state |
| History | page next/previous | show that page; active commitment is resolved independently |
| Entry banner | current passenger held/confirmed seat on active trip | Resume; released/expired seat is not an active commitment |

Invariant: free + held + confirmed never exceeds published seats; driver cannot start an empty/unconfirmed manifest; only the server can confirm a seat, start or complete. Passenger PIN is neither a payment nor identity OTP. Lease display uses `server_time` plus elapsed monotonic time, not the device wall clock. `rideCommand` retains uncertain keys in account/signature-scoped preferences. A successful command followed by refresh failure must still retain its returned aggregate. HTTP errors remain visible. Foreground lifecycle controls polling; cancellation must end retry loops.

## Machine E — profile, enrollment and settings

| Submachine | States | Transition / invariant |
| --- | --- | --- |
| Passenger name | viewing, editing, saving, error | validate → save acknowledgement → persist/exit; failure retains draft |
| Passenger photo | idle, picked, uploading, error, saved | authenticated image upload → returned backend URL; never persist a local URI as remote success |
| Passenger phone | editing, requesting OTP, OTP sent, verifying, verified/error | server request binds normalized phone; verification result updates profile; editing target invalidates sent challenge |
| Driver vehicle | loading, editable, saving, acknowledged/error | canonical verification API → identifiers PUT; seats exclude driver; no local success before acknowledgement |
| Driver documents | loading, selected type, picked file, uploading, confirming, submitted/rejected/approved | validate file/expiry → upload/confirm → server review submission; uploading alone is not approval |
| Communication preferences | loading, ready, saving, error | ordered updates; response cannot undo newer choice; errors visible and retryable |
| Language | selected preview value / unavailable | Interface switching is not implemented; do not report a locale saved or changed |
| Driver toggles | unavailable | auto-accept/sound/maps preference switches have no operational consumer/persistence; disabled with explanations |
| Saved places | loading, browsing, editing draft, resolving, saving, error | unresolved address cannot silently become city-center coordinates; save acknowledgement precedes cache update/closing editor |
| Promotions | loading, selected, validating, eligible-preview/invalid/error | a valid `/promos/validate` response previews eligibility; it does not redeem or persist a booking discount; changing code/fare invalidates the preview |
| Privacy/legal/help | viewing, expanded/filtered, external intent | navigation/local display only; no server security change implied |

## Machine F — inbox, support and presentation-only flows

| Submachine | States | Transition / invariant |
| --- | --- | --- |
| Inbox | initial/loading, content/empty, paginating, filtered, polling/backoff | first page replaces; next page deduplicates IDs; acknowledgement marks read; badge is global server count adjusted by changed items, not loaded-page count |
| Notification navigation | unread/read + destination kind | mark read independently; shared link opens shared machine; hail details must resolve notification's ride ID |
| Support ticket | editable, invalid, submitting, confirmed/error | server ticket ID required for confirmation; busy guard suppresses duplicates; unresolved submit must not erase draft |
| Support thread | loading, ready, sending, sent/error | authenticated ticket ID; send acknowledgement before clearing reply; cancellation/account switch drops callbacks |
| Ticket lifecycle | OPEN, IN_REVIEW, WAITING_FOR_YOU, RESOLVED, CLOSED | display server status; local filter/rating cannot resolve ticket |
| Help pages | query/filter/category/accordion/feedback | local-only state and back navigation; no synthetic server case |
| Prototype payment | ENTER, CONFIRM, WAITING, SUCCESS, DECLINED, TIMED_OUT, FAILED | reducer callbacks are illustrative; no live provider approval/charge is implemented |
| Prototype phone/email | REAUTH → ENTER_NEW → OTP/SENT → SUCCESS/EXPIRED | illustrative flow callbacks are not server verification; no normal navigation entry |
| Prototype deletion/security | warning/confirm; password/session drafts | no server deletion/password change implemented; do not expose as working account mutations |

Registered passenger `pay_flow`, `profile_flow`, `support_flow`, `pay_history`, `flow_gallery` have no incoming normal-app navigation calls. Their screens are inventoried below as prototypes, not reported as remotely exploitable verification/deletion paths. Preview functions are fixtures, not independent product machines. The original prototype OTP reducer accepted empty-code matching and the payment reducer accepted out-of-phase callbacks. Four failing regressions now guard these transitions: phone verification requires the OTP phase and a six-digit expected value; payment outcomes require WAITING and terminal success ignores delayed failure. The profile prototype's verification callback explicitly reports unavailable instead of comparing user input to itself. These guards are presentation logic, not backend authentication.

## Confirmed issue groups, ordered by impact

These are local reviewable issue specifications, not automatically published GitHub issues. P1 = active-trip/account integrity; P2 = incorrect success/data persistence; P3 = misleading secondary UI. Test names provide executable evidence, and source links below map screen owners. “Fixed” is used only after the recorded verification succeeds.

| Issue | Priority | Reachable pre-fix trace / impact | Fix and regression evidence | Status |
| --- | --- | --- | --- | --- |
| SM-01 Passenger ride command integrity | P1 | cancel HTTP fails → unconditional null active ride; two create taps while request suspended → two new keys; lost response → retry new key | serialize ride reads/mutations; pending reservation; retain cancellation on error; reuse matching create/cancel keys. `RideStateMachineTest`: failedCancellationKeepsActiveRideAndShowsRetryError, doubleTapWhileCreateSuspendedOnlySendsOneCommand, retryAfterLostCreateResponseReusesIdempotencyKey | Fixed; JVM regressions verified |
| SM-02 Server lifecycle decoding | P1 | `cancelled_by_*`/no_driver_found restored as SEARCHING; driver_arrived also decoded SEARCHING | canonical terminal statuses and arrival mapping. `backendTerminalStatusesNeverRestoreAsSearchingRides` | Fixed; terminal/arrival regression verified |
| SM-03 Driver concurrent lifecycle | P1 | current-ride GET starts empty → accept succeeds → old GET clears trip; repeated phase taps can target different next phases | serialize snapshots/actions; bind requested trip/phase; terminal/cancel guards and ID-deduplicated history. `RemoteDriverStateMachineTest.delayedPollCannotUndoAcceptedOffer` | Fixed; poll, duplicate-phase, completion/offer and expiry regressions verified |
| SM-04 Account-bound async work | P1 | singleton passenger caches survive logout; delayed `/me` calls `save` setting loggedIn=true after clear; auth success leaves earlier sign-in routes on the stack, and subsequent logins retain old optional identity fields/caches | clear/reset private repositories, cancel owned jobs and fence inbox responses; serialize publication with reset so old tasks cannot attach fresh polling children after cancellation; expected login ID checked inside DataStore for profile/photo writes; every new login replaces preferences and resets account caches; auth success clears the root stack, and protected routes redirect on invalidation. `logoutDiscardsLateRestoreAndAllPrivateState`, `logoutDiscardsDelayedInboxAndUnreadCount`, `RideLogoutConcurrencyTest`, both `SessionStateMachineTest` suites | Fixed; delayed-response, real-thread logout and replacement-login regressions verified; navigation tests compiled |
| SM-14 Supported-Android bootstrap compatibility | P1 | API 24/25 restores/formats rides or schedules using API-26-only `java.time` without desugaring; lint reproduced 22 NewApi errors | enable core library desugaring and package `desugar_jdk_libs` 2.1.5; keep minSdk 24; lint is the compatibility regression check | Fixed; lint API errors eliminated; desugared time classes packaged; API-24 device execution unverified |
| SM-05 Query/history freshness | P2 | two quote/history requests complete newest then oldest → wrong fare or newly completed ride disappears | per-query revision fence; invalidate on clear/logout; old completions cannot clear newer loading/error state. Both Home route effects rethrow cancellation rather than publishing an obsolete fallback/error. `staleQuoteCannotReplaceCurrentRouteQuote` and `olderHistoryCannotEraseNewerCompletedRide` | Fixed; quote/history JVM regressions verified; route cancellation source/build checked |
| SM-06 Driver false acknowledgements | P2 | rating receives 409 `Response` but returns success; availability PUT succeeds then redundant location 503 leaves app offline; cancellation swallowed by Result wrapper | validate rating HTTP status; acknowledge availability independently; propagate cancellation. Three `RemoteDriverStateMachineTest` cases | Fixed; rating, availability and cancellation regressions verified |
| SM-07 Inbox badge, read concurrency and destination | P2 | server unread=50; reading an item recomputes badge from only the loaded page; late refresh can undo read; ride notification ignores its ID and opens unrelated current ride | global count delta and synchronized list/page/badge, serialized read/refresh, account fence; load the requested ride and select history independently of current ride. `NotificationStateMachineTest`, `notificationOpensRequestedHistoricalRideInsteadOfCurrentRide` | Fixed; badge, account fence and requested-ride regressions verified |
| SM-08 Unacknowledged secondary operations | P2 | schedule/courier/reset claim success before/no server operation; rebook clears route immediately before requiring it; support failure says queued and destroys draft; signup marks unsupported profile edits complete | schedule success callback follows server acknowledgement; duplicate and ambiguous retries keep pending/key state; rebook resolves endpoints and preserves active route when busy; driver support controller retains failed drafts; registration persists server facts. Explicit unavailable states replace unsupported operations. Promo checks display eligibility previews without invoking an applied/booking-success callback; ignored Remember-me choices are replaced by an accurate persistence notice. `RideStateMachineTest` schedule/rebook cases, `SupportTicketFormTest`, device `ScreenStateRegressionTest` | Fixed; JVM flow regressions verified; UI regressions compiled and owner-run |
| SM-10 Saved places and communication preferences | P2 | missing geocoder result saves city-center coordinates; failed save updates local shortcut and closes draft; concurrent settings requests can overwrite choices and failures leave unsaved toggles selected | `SavedPlaceWriter` validates resolution and acknowledges before caching; editor retains failures; `CommunicationPreferences` loads before editing, sends one partial acknowledged change at a time and exposes errors/retry. `SecondaryScreenStateTest` | Fixed; JVM preference/saved-place regressions verified |
| SM-11 Auth submission and validation | P2 | loading set inside launched coroutine leaves a duplicate-tap window; signup accepts passwords below backend's minimum; registration locally claims name/phone/vehicle persistence through unsupported endpoints | reserve loading before launch; rethrow cancellation; 10 code-point/72-byte registration bounds; driver registration collects only supported email/password and leaves canonical vehicle/documents onboarding separate. `SessionStateMachineTest` | Fixed; session/registration rules verified; auth/navigation UI regressions compiled |
| SM-13 Location and safety presentation | P2 | service send gate written on main callback and IO completion without synchronization; static share/dialer actions described as live tracking or an already connected call | atomic compare-and-set send gate; accurate snapshot/dialer labels; no assertion of delivered call/GPS updates | Source fixed; build/lint verified; physical GPS/intent execution unverified |
| SM-09 Shared commitment | P3 | released/expired passenger seat on a still-waiting trip causes Resume banner | same membership predicate in banner and active screen; `onlyHeldOrConfirmedMembershipShowsResume` | Fixed; commitment-membership regression verified |
| SM-12 Explicit prototype transitions | P3 | approval in ENTER produces SUCCESS; delayed decline undoes SUCCESS; empty OTP and correct OTP before REAUTH/entry produce phone SUCCESS | guarded phase reducers and code length; unavailable prototype phone action. Four `FlowMachineInvariantTest` cases | Fixed; 4 explicit-phase JVM regressions verified |

## Screen coverage index

Every top-level screen surface is assigned a machine below. `live` means a normal navigation entry or an embedded normal screen; `registered` means a route exists but no normal incoming action was found; `component` means reusable screen/overlay; `prototype` means fixture-driven or unconnected. A machine assignment describes behavior, not a claim that every UI path has a device test.


### ride-client-kotlin

| Surface | Source | Machine / entry |
| --- | --- | --- |
| `PayMethodsFlowScreen` | [PaymentFlowScreens.kt:51](../app/src/main/java/com/example/ridepassenger2/ui/payment/PaymentFlowScreens.kt#L51) | F (prototype); prototype; no normal entry |
| `AddMethodScreen` | [PaymentFlowScreens.kt:103](../app/src/main/java/com/example/ridepassenger2/ui/payment/PaymentFlowScreens.kt#L103) | F (prototype); prototype; no normal entry |
| `PayHistoryScreen` | [PaymentFlowScreens.kt:161](../app/src/main/java/com/example/ridepassenger2/ui/payment/PaymentFlowScreens.kt#L161) | F (prototype); prototype; no normal entry |
| `PayDetailScreen` | [PaymentFlowScreens.kt:196](../app/src/main/java/com/example/ridepassenger2/ui/payment/PaymentFlowScreens.kt#L196) | F (prototype); prototype; no normal entry |
| `PayTripScreen` | [PaymentFlowScreens.kt:222](../app/src/main/java/com/example/ridepassenger2/ui/payment/PaymentFlowScreens.kt#L222) | F (prototype); prototype; no normal entry |
| `TransactionsScreen` | [TransactionsScreen.kt:53](../app/src/main/java/com/example/ridepassenger2/ui/payment/TransactionsScreen.kt#L53) | F; component; no normal route |
| `EditNameScreen` | [ProfileFlowScreens.kt:54](../app/src/main/java/com/example/ridepassenger2/ui/profile/ProfileFlowScreens.kt#L54) | F (prototype); prototype; no normal entry |
| `ChangePhoneScreen` | [ProfileFlowScreens.kt:73](../app/src/main/java/com/example/ridepassenger2/ui/profile/ProfileFlowScreens.kt#L73) | F (prototype); prototype; no normal entry |
| `ChangeEmailScreen` | [ProfileFlowScreens.kt:113](../app/src/main/java/com/example/ridepassenger2/ui/profile/ProfileFlowScreens.kt#L113) | F (prototype); prototype; no normal entry |
| `ProfileVerifyRows` | [ProfileFlowScreens.kt:142](../app/src/main/java/com/example/ridepassenger2/ui/profile/ProfileFlowScreens.kt#L142) | F (prototype); prototype; no normal entry |
| `PhotoChangeScreen` | [ProfileFlowScreens.kt:152](../app/src/main/java/com/example/ridepassenger2/ui/profile/ProfileFlowScreens.kt#L152) | F (prototype); prototype; no normal entry |
| `SecurityScreen` | [ProfileFlowScreens.kt:169](../app/src/main/java/com/example/ridepassenger2/ui/profile/ProfileFlowScreens.kt#L169) | F (prototype); prototype; no normal entry |
| `DeleteAccountScreen` | [ProfileFlowScreens.kt:201](../app/src/main/java/com/example/ridepassenger2/ui/profile/ProfileFlowScreens.kt#L201) | F (prototype); prototype; no normal entry |
| `AddAddressScreen` | [AddAddressScreen.kt:60](../app/src/main/java/com/example/ridepassenger2/ui/screens/AddAddressScreen.kt#L60) | E; live/component |
| `PaymentMethodsScreen` | [AliveScreens.kt:150](../app/src/main/java/com/example/ridepassenger2/ui/screens/AliveScreens.kt#L150) | E; live/component |
| `ScheduleRideScreen` | [AliveScreens.kt:352](../app/src/main/java/com/example/ridepassenger2/ui/screens/AliveScreens.kt#L352) | B; live/component |
| `PackageRideScreen` | [AliveScreens.kt:662](../app/src/main/java/com/example/ridepassenger2/ui/screens/AliveScreens.kt#L662) | B; live/component |
| `SafetyScreen` | [AliveScreens.kt:754](../app/src/main/java/com/example/ridepassenger2/ui/screens/AliveScreens.kt#L754) | F; live/component |
| `PersonalInfoScreen` | [AliveScreens.kt:940](../app/src/main/java/com/example/ridepassenger2/ui/screens/AliveScreens.kt#L940) | E; live/component |
| `HelpFaqScreen` | [AliveScreens.kt:1305](../app/src/main/java/com/example/ridepassenger2/ui/screens/AliveScreens.kt#L1305) | F; live/component |
| `ContactUsScreen` | [AliveScreens.kt:1340](../app/src/main/java/com/example/ridepassenger2/ui/screens/AliveScreens.kt#L1340) | F; live/component |
| `TermsDialog` | [AliveScreens.kt:1363](../app/src/main/java/com/example/ridepassenger2/ui/screens/AliveScreens.kt#L1363) | F; live/component |
| `PrivacyDialog` | [AliveScreens.kt:1381](../app/src/main/java/com/example/ridepassenger2/ui/screens/AliveScreens.kt#L1381) | F; live/component |
| `CommunicationSettingsScreen` | [CommunicationSettingsScreen.kt:37](../app/src/main/java/com/example/ridepassenger2/ui/screens/CommunicationSettingsScreen.kt#L37) | E; live/component |
| `CreateAccountScreen` | [CreateAccountScreen.kt:99](../app/src/main/java/com/example/ridepassenger2/ui/screens/CreateAccountScreen.kt#L99) | A; live/component |
| `ForgotPasswordScreen` | [ForgotPasswordScreen.kt:81](../app/src/main/java/com/example/ridepassenger2/ui/screens/ForgotPasswordScreen.kt#L81) | A; live/component |
| `HelpArticleScreen` | [HelpArticleScreen.kt:53](../app/src/main/java/com/example/ridepassenger2/ui/screens/HelpArticleScreen.kt#L53) | F; component; no normal route |
| `HelpArticlesScreen` | [HelpArticlesScreen.kt:31](../app/src/main/java/com/example/ridepassenger2/ui/screens/HelpArticlesScreen.kt#L31) | F; live/component |
| `HelpCenterScreen` | [HelpCenterScreen.kt:25](../app/src/main/java/com/example/ridepassenger2/ui/screens/HelpCenterScreen.kt#L25) | F; live/component |
| `HelpHubScreen` | [HelpHubScreen.kt:27](../app/src/main/java/com/example/ridepassenger2/ui/screens/HelpHubScreen.kt#L27) | F; live/component |
| `HomeMapScreen` | [HomeMapScreen.kt:111](../app/src/main/java/com/example/ridepassenger2/ui/screens/HomeMapScreen.kt#L111) | B; live/component |
| `LanguageSettingsScreen` | [LanguageSettingsScreen.kt:54](../app/src/main/java/com/example/ridepassenger2/ui/screens/LanguageSettingsScreen.kt#L54) | E; live/component |
| `LegalPoliciesScreen` | [LegalPoliciesScreen.kt:46](../app/src/main/java/com/example/ridepassenger2/ui/screens/LegalPoliciesScreen.kt#L46) | E; component; no normal route |
| `NotificationsScreen` | [NotificationsScreen.kt:32](../app/src/main/java/com/example/ridepassenger2/ui/screens/NotificationsScreen.kt#L32) | F; live/component |
| `OnboardingScreen` | [OnboardingScreen.kt:64](../app/src/main/java/com/example/ridepassenger2/ui/screens/OnboardingScreen.kt#L64) | A; live/component |
| `PreferencesScreen` | [PreferencesScreen.kt:26](../app/src/main/java/com/example/ridepassenger2/ui/screens/PreferencesScreen.kt#L26) | E; live/component |
| `SettingsScreen` | [PreferencesScreen.kt:186](../app/src/main/java/com/example/ridepassenger2/ui/screens/PreferencesScreen.kt#L186) | E; live/component |
| `PrivacySecurityScreen` | [PrivacySecurityScreen.kt:49](../app/src/main/java/com/example/ridepassenger2/ui/screens/PrivacySecurityScreen.kt#L49) | E; component; no normal route |
| `ProfileScreen` | [ProfileScreen.kt:56](../app/src/main/java/com/example/ridepassenger2/ui/screens/ProfileScreen.kt#L56) | E; live/component |
| `PromotionDetailsScreen` | [PromotionDetailsScreen.kt:51](../app/src/main/java/com/example/ridepassenger2/ui/screens/PromotionDetailsScreen.kt#L51) | E; live/component |
| `RidaSplashScreen` | [RidaSplashScreen.kt:26](../app/src/main/java/com/example/ridepassenger2/ui/screens/RidaSplashScreen.kt#L26) | A; live/component |
| `RideDetailsScreen` | [RideDetailsScreen.kt:43](../app/src/main/java/com/example/ridepassenger2/ui/screens/RideDetailsScreen.kt#L43) | B; live/component |
| `RideHistoryScreen` | [RideHistoryScreen.kt:41](../app/src/main/java/com/example/ridepassenger2/ui/screens/RideHistoryScreen.kt#L41) | B; live/component |
| `SavedPlacesScreen` | [SavedPlacesScreen.kt:81](../app/src/main/java/com/example/ridepassenger2/ui/screens/SavedPlacesScreen.kt#L81) | E; live/component |
| `SelectIssueCategoryScreen` | [SelectIssueCategoryScreen.kt:31](../app/src/main/java/com/example/ridepassenger2/ui/screens/SelectIssueCategoryScreen.kt#L31) | F; live/component |
| `SelectRideIssueScreen` | [SelectRideIssueScreen.kt:24](../app/src/main/java/com/example/ridepassenger2/ui/screens/SelectRideIssueScreen.kt#L24) | F; live/component |
| `SharedTripHelp` | [SharedCommon.kt:184](../app/src/main/java/com/example/ridepassenger2/ui/screens/SharedCommon.kt#L184) | D; live/component |
| `SharedTripEntry` | [SharedCommon.kt:210](../app/src/main/java/com/example/ridepassenger2/ui/screens/SharedCommon.kt#L210) | D; live/component |
| `SharedTripRating` | [SharedCommon.kt:226](../app/src/main/java/com/example/ridepassenger2/ui/screens/SharedCommon.kt#L226) | D; live/component |
| `SharedRideScreen` | [SharedRideScreen.kt:28](../app/src/main/java/com/example/ridepassenger2/ui/screens/SharedRideScreen.kt#L28) | D; live/component |
| `SignInScreen` | [SignInScreen.kt:99](../app/src/main/java/com/example/ridepassenger2/ui/screens/SignInScreen.kt#L99) | A; live/component |
| `TripHelpScreen` | [TripHelpScreen.kt:32](../app/src/main/java/com/example/ridepassenger2/ui/screens/TripHelpScreen.kt#L32) | F; live/component |
| `SafetyCenterScreen` | [SupportFlowScreens.kt:88](../app/src/main/java/com/example/ridepassenger2/ui/support/SupportFlowScreens.kt#L88) | F (prototype); prototype; no normal entry |
| `MySupportCasesScreen` | [SupportFlowScreens.kt:332](../app/src/main/java/com/example/ridepassenger2/ui/support/SupportFlowScreens.kt#L332) | F (prototype); prototype; no normal entry |
| `ReportIssueScreen` | [SupportFlowScreens.kt:554](../app/src/main/java/com/example/ridepassenger2/ui/support/SupportFlowScreens.kt#L554) | F (prototype); prototype; no normal entry |
| `CaseDetailsScreen` | [SupportFlowScreens.kt:816](../app/src/main/java/com/example/ridepassenger2/ui/support/SupportFlowScreens.kt#L816) | F (prototype); prototype; no normal entry |
| `SupportHomeScreen` | [SupportFlowScreens.kt:1067](../app/src/main/java/com/example/ridepassenger2/ui/support/SupportFlowScreens.kt#L1067) | F (prototype); prototype; no normal entry |
| `MyRequestsScreen` | [SupportFlowScreens.kt:1087](../app/src/main/java/com/example/ridepassenger2/ui/support/SupportFlowScreens.kt#L1087) | F (prototype); prototype; no normal entry |
| `RequestFormScreen` | [SupportFlowScreens.kt:1103](../app/src/main/java/com/example/ridepassenger2/ui/support/SupportFlowScreens.kt#L1103) | F (prototype); prototype; no normal entry |
| `RequestDetailScreen` | [SupportFlowScreens.kt:1120](../app/src/main/java/com/example/ridepassenger2/ui/support/SupportFlowScreens.kt#L1120) | F (prototype); prototype; no normal entry |
| `TopicScreen` | [SupportFlowScreens.kt:1141](../app/src/main/java/com/example/ridepassenger2/ui/support/SupportFlowScreens.kt#L1141) | F (prototype); prototype; no normal entry |
| `SelfHelpScreen` | [SupportFlowScreens.kt:1151](../app/src/main/java/com/example/ridepassenger2/ui/support/SupportFlowScreens.kt#L1151) | F (prototype); prototype; no normal entry |
| `ConfirmScreen` | [SupportFlowScreens.kt:1161](../app/src/main/java/com/example/ridepassenger2/ui/support/SupportFlowScreens.kt#L1161) | F (prototype); prototype; no normal entry |

### rida-driver

| Surface | Source | Machine / entry |
| --- | --- | --- |
| `DriverEnrollment` | [DriverEnrollment.kt:28](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/DriverEnrollment.kt#L28) | E; live/component |
| `DriverHomeScreen` | [DriverHomeScreen.kt:94](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/DriverHomeScreen.kt#L94) | C; live/component |
| `TripCompletionDialog` | [DriverHomeScreen.kt:823](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/DriverHomeScreen.kt#L823) | C; live/component |
| `EarningsScreen` | [EarningsScreen.kt:54](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/EarningsScreen.kt#L54) | C; live/component |
| `ForgotPasswordScreen` | [ForgotPasswordScreen.kt:58](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/ForgotPasswordScreen.kt#L58) | A; live/component |
| `DriverProfileScreen` | [ProfileScreens.kt:63](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/ProfileScreens.kt#L63) | E; live/component |
| `VehicleEditScreen` | [ProfileScreens.kt:267](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/ProfileScreens.kt#L267) | E; legacy component; canonical route uses DriverEnrollment |
| `DocumentsScreen` | [ProfileScreens.kt:457](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/ProfileScreens.kt#L457) | E; legacy component; canonical route uses DriverEnrollment |
| `RidaSplashScreen` | [RidaSplashScreen.kt:26](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/RidaSplashScreen.kt#L26) | A; live/component |
| `SettingsScreen` | [SettingsAndSupportScreens.kt:66](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/SettingsAndSupportScreens.kt#L66) | E; live/component |
| `SupportScreen` | [SettingsAndSupportScreens.kt:241](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/SettingsAndSupportScreens.kt#L241) | F; live/component |
| `SharedTripHelp` | [SharedCommon.kt:215](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/SharedCommon.kt#L215) | D; live/component |
| `SharedTripEntry` | [SharedCommon.kt:241](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/SharedCommon.kt#L241) | D; live/component |
| `SharedTripRating` | [SharedCommon.kt:257](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/SharedCommon.kt#L257) | D; live/component |
| `SharedRideScreen` | [SharedRideScreen.kt:24](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/SharedRideScreen.kt#L24) | D; live/component |
| `SharedRoutePreview` | [SharedRoutePreview.kt:19](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/SharedRoutePreview.kt#L19) | D; live/component |
| `SignInScreen` | [SignInScreen.kt:82](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/SignInScreen.kt#L82) | A; live/component |
| `SignUpScreen` | [SignUpScreen.kt:69](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/SignUpScreen.kt#L69) | A; live/component |
| `TripsHistoryScreen` | [TripsScreen.kt:62](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/TripsScreen.kt#L62) | C; live/component |
| `TripDetailScreen` | [TripsScreen.kt:225](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/ui/screens/TripsScreen.kt#L225) | C; live/component |

## Executable evidence index

The pre-fix traces above were observed against the original behavior. The following files retain the regression assertions against the fixed production implementations; fake APIs control timing and failures rather than replacing repository logic.

| Coverage | Implementation | Regression source |
| --- | --- | --- |
| Passenger creation/cancellation, retries, restoration, quote/history order, schedules, rebooking and notification IDs | [RideRepository.kt](../app/src/main/java/com/example/ridepassenger2/data/repository/RideRepository.kt) / [RideMappers.kt](../app/src/main/java/com/example/ridepassenger2/data/repository/RideMappers.kt) | [RideStateMachineTest.kt](../app/src/test/java/com/example/ridepassenger2/data/repository/RideStateMachineTest.kt) |
| Driver poll/action ordering, phase duplication, rating/status acknowledgement, cancellation, completion and offer lifetime | [RemoteDriverRepository.kt](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/data/driver/RemoteDriverRepository.kt) | [RemoteDriverStateMachineTest.kt](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/test/java/com/example/ridedriver/data/driver/RemoteDriverStateMachineTest.kt) |
| Logout interleaved with publication (real threads and state subscriber) | Passenger repository publication/reset monitor | [RideLogoutConcurrencyTest.kt](../app/src/test/java/com/example/ridepassenger2/data/repository/RideLogoutConcurrencyTest.kt) |
| Inbox count across pages and delayed response after logout | [NotificationRepository.kt](../app/src/main/java/com/example/ridepassenger2/data/repository/NotificationRepository.kt) | [NotificationStateMachineTest.kt](../app/src/test/java/com/example/ridepassenger2/data/repository/NotificationStateMachineTest.kt) |
| Preferences, saved-place acknowledgement and session write guard | [CommunicationPreferences.kt](../app/src/main/java/com/example/ridepassenger2/data/repository/CommunicationPreferences.kt) / [SavedPlaceWriter.kt](../app/src/main/java/com/example/ridepassenger2/data/repository/SavedPlaceWriter.kt) | [SecondaryScreenStateTest.kt](../app/src/test/java/com/example/ridepassenger2/data/repository/SecondaryScreenStateTest.kt) |
| Driver support draft failure and duplicate tap | [SupportTicketForm.kt](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/main/java/com/example/ridedriver/data/repository/SupportTicketForm.kt) | [SupportTicketFormTest.kt](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/test/java/com/example/ridedriver/data/repository/SupportTicketFormTest.kt) |
| Login identity and registration bounds | Passenger/driver `data/local/SessionManager.kt` and `SessionWriteGuard.kt` | [Passenger SessionStateMachineTest](../app/src/test/java/com/example/ridepassenger2/data/local/SessionStateMachineTest.kt), [driver SessionStateMachineTest](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/test/java/com/example/ridedriver/data/local/SessionStateMachineTest.kt) |
| Shared membership, page replacement and existing version/countdown/token recovery | [TransportApi.kt](../app/src/main/java/com/example/ridepassenger2/data/remote/TransportApi.kt) | [TransportRecoveryTest.kt](../app/src/test/java/com/example/ridepassenger2/data/remote/TransportRecoveryTest.kt) |
| Explicit prototype phase/OTP guards | `ui/payment/PaymentModels.kt`, `ui/profile/ProfileEditModels.kt` | [FlowMachineInvariantTest.kt](../app/src/test/java/com/example/ridepassenger2/ui/flows/FlowMachineInvariantTest.kt) |
| Reset and locale UI claims (device execution pending) | Passenger/driver reset screens and passenger language screen | [Passenger ScreenStateRegressionTest](../app/src/androidTest/java/com/example/ridepassenger2/ui/screens/ScreenStateRegressionTest.kt), [driver ScreenStateRegressionTest](https://github.com/Ditronics-Tz/rida-driver/blob/2880edbc7ea91d695cd4ddc69bcd1eea623ef0e9/app/src/androidTest/java/com/example/ridedriver/ui/screens/ScreenStateRegressionTest.kt) |

## Rejected hypotheses and remaining capability boundaries

- Shared-history pagination was suspected of accumulating all earlier pages. Source inspection disproved this: `mergeTransportSnapshots` maps only incoming rows; `historyMergeDoesNotAccumulateEarlierPages` locks down page replacement. It is not a confirmed bug.
- The driver Home argument named `todayGrossTzs` was checked for an asserted “today” total. Its visible pill has no such period label; it displays a recent-history quick amount. The Earnings screen uses the server's selected-period aggregate. This audit does not turn the internal variable name into a confirmed period-calculation defect.
- Bootstrap was suspected of signing out on every network error. Both `UserRepository.refreshSessionFromApi` implementations already tolerate temporary network failures; the confirmed defect was the late persistence after account invalidation.
- Legacy vehicle/document editors and the standalone payment/profile/support galleries are not canonical normal-app paths. Their fixture data is not proof of a live backend defect.
- Password reset, courier-specific booking, non-cash payments, interface locale switching, promotion redemption in the mobile booking flow and driver personal-profile editing lack complete current mobile/backend support. The fix is an honest unavailable state, not a claim those capabilities were implemented. Driver signup now creates the supported email login; vehicle/document registration remains available in canonical enrollment.
- Driver alert/maps/auto-accept preference toggles have no implemented consumer and are disabled. External support hours/numbers/provider delivery are not verified by this audit.
- A local “Remember me” toggle previously had no effect on persistence. It has been replaced by an accurate notice that login persists until logout. Promo validation now remains an eligibility preview: backend `PromoController.Validate` calls `EvaluatePromo` and returns validity/discount estimates, without the mobile flow storing a redeemed booking discount.
- The normal passenger Home now exposes real pending-request/active-trip controls and requires an actual GPS pickup before dispatch, rather than leaving those existing ride helpers unconnected or substituting a city/map center for the pickup.

## Verification log and practical limits

The results in the first table describe the original workspace, including existing local previews and toolchain edits. The publication verification below checks the exact source selected for GitHub without those unrelated edits.

2026-11-07, initial regression run:

- Passenger `bash ./gradlew :app:testDebugUnitTest --tests '*RideStateMachineTest' --max-workers=2 --console=plain`: 5 tests, 5 failures, each matching the issue trace above.
- Driver `bash ./gradlew :app:testDebugUnitTest --tests '*RemoteDriverStateMachineTest' --max-workers=2 --console=plain`: 4 tests, 4 failures.
- First fixes: same focused commands succeeded (5 passenger and 4 driver tests).
- Further minimized failures: historical rebooking (1), prototype phase/OTP reducers (4), completion followed by offer failure (1), and old history overwriting newer history (1) were reproduced before their corresponding fixes.
- A full intermediate passenger run passed 73 of 74 tests, with only the newly introduced stale-history regression failing; that failure drove its revision fence. Final results are recorded below.
- Account replacement was reproduced with a failing DataStore preference-mutation regression: the old phone/photo/refresh token remained when a new login supplied no replacement values. Both managers now clear the preference map in the login transaction and reset per-account caches before publishing the new login. Both auth graphs use a single root-clearing success helper; two new device navigation regressions compile against those actual helpers.
- A deterministic publication/logout interleaving also failed against the compiled pre-fix production repository: a blocking state subscriber let logout cancel the current children, after which the old restoration task attached a fresh polling job. `RideLogoutConcurrencyTest` asserts no such child survives; publication and reset now share a monitor with cancellation checked inside it.
- Passenger final lint then exposed 22 API-compatibility errors at minSdk 24. Core library desugaring was enabled; its recheck and APK packaging verification are recorded with the final results. This follows [Android library-desugaring guidance](https://developer.android.com/studio/write/java8-support#library-desugaring) and the [library changelog](https://github.com/google/desugar_jdk_libs/blob/master/CHANGELOG.md).
- Seven new device UI regressions (five passenger, two driver) cover unavailable reset/locale claims, promo eligibility versus redemption, and the persistence notice. They are intended for owner execution; compilation is checked as part of the test-APK build.

Repository tests use synthetic public-shaped DTOs and suspended continuations/deferred values to deterministically choose response order. They do not prove server authorization, delivery of push/SMS/email, OEM background service behavior, GPS permission behavior or process-death recovery. Device execution is intentionally left to the user under the earlier instruction. Remaining product capabilities and deployment gaps are distinguished from confirmed mobile transition defects above.

### Final verification and artifacts

All four Gradle targets succeeded in both repositories on the final source:

```bash
bash ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug \
  --no-daemon '-Dorg.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8' \
  -Pkotlin.compiler.execution.strategy=in-process --max-workers=1 --console=plain
```

| Repository | JVM tests | Lint | App / test APK |
| --- | --- | --- | --- |
| Passenger | 77 passed; 0 failed/errors/skipped | 0 errors; 102 warnings, 4 hints | Both built |
| Driver | 40 passed; 0 failed/errors/skipped | 0 errors; 70 warnings, 1 hint | Both built |

Total: **117 passing JVM tests**, including **43 added regressions**, with seven new device UI/navigation regressions compiled. Existing lint warnings remain; no baseline or error suppression was added. Driver verification includes the owner's concurrent AGP 9.5.1 edit, which this task preserved. Passenger wrapper/IDE and existing driver preview edits were preserved. Verification preceded publication. No device execution or deployment was performed.

The passenger APK contains desugared `j$/time/Instant` references/classes in its DEX files. Source fingerprints in the verification record were compared before and after the final checks; both repositories' inputs matched.

- Passenger app APK: `build-artifacts/2026-11-07-state-machines/passenger-debug.apk` (local workspace artifact)
- Driver app APK: `build-artifacts/2026-11-07-state-machines/driver-debug.apk` (local workspace artifact)
- Passenger test APK: `build-artifacts/2026-11-07-state-machines/passenger-debug-androidTest.apk` (local workspace artifact)
- Driver test APK: `build-artifacts/2026-11-07-state-machines/driver-debug-androidTest.apk` (local workspace artifact)
- Verification record, source fingerprints and artifact hashes: `build-artifacts/2026-11-07-state-machines/verification.json` (local workspace artifact)
- APK checksums: `build-artifacts/2026-11-07-state-machines/SHA256SUMS` (local workspace artifact)

The artifact's `verification/` folder retains sanitized Gradle/lint/JUnit results and the available initial failing-test logs. The complete state-machine specification and issue ledger remain this one file. This file publishes the shared workspace audit. APKs and raw logs listed above remain local artifacts and are not included in the source push.

Owner device execution can select `com.example.ridepassenger2.ui.screens.ScreenStateRegressionTest` and `com.example.ridedriver.ui.screens.ScreenStateRegressionTest` with AndroidJUnitRunner after installing each app/test APK. Those tests cover reset/locale/promo/persistence claims and root-clearing auth navigation without creating real accounts. The existing live journey tests remain separate backend/device acceptance work.

## GitHub publication verification

The exact publication trees are checked with the four Gradle targets above using AGP 9.4.1 and the repository Gradle 9.8.0 wrappers. User IDE settings, local preview additions, the passenger wrapper upgrade and the separate driver AGP 9.5.1 edit remain in the original workspace. All four targets passed in both publication trees. No device execution was performed.

| Publication tree | JVM tests | Lint errors | Lint warnings / hints | APK builds |
| --- | --- | --- | --- | --- |
| `ride-client-kotlin` | 77 passed | 0 | 102 / 4 | App and instrumentation passed |
| `rida-driver` | 40 passed | 0 | 70 / 1 | App and instrumentation passed |

These checks validate the exact implementation being committed, with the unrelated local previews/toolchain edits excluded. The original workspace verification and its local APK hashes remain separate evidence.
