# Backend needs (checked: no `docs/api/openapi.yaml`, no backend routes in this repo → all unknown/missing)

Legend: exists = in openapi; missing = not in openapi but needed; unknown = no spec found (this repo).

| Screen | Method + path (proposed) | Request / response | Errors | Status |
|---|---|---|---|---|
| Payment methods list/create/default/delete | GET/POST/PATCH/DELETE `/v1/payment-methods` | `{provider, msisdn}` → `{id, masked, isDefault}` | 400 invalid_msisdn, 409 in_use, 404 | unknown |
| Mobile-money verify + poll | POST `/v1/payment-methods/verify` + GET `/v1/verifications/{id}` | `{methodId}` → `{status: pending/approved/declined/expired}` | 408 timeout, 422 declined, 429 | unknown |
| Payment history/detail | GET `/v1/payments?filter=&cursor=` + GET `/v1/payments/{id}` | `{route, amountTzs, status, methodMasked, time(Africa/Dar_es_Salaam), ref, discount}` | 404, 401 | unknown |
| Pay trip | POST `/v1/trips/{id}/pay` | `{methodId}` → `{status}` | 402 failed, 409 already_paid | unknown |
| Change phone (OTP) | POST `/v1/profile/phone/otp` + POST `/v1/profile/phone/confirm` | `{msisdn}` → `{sent}` / `{code, attemptsLeft}` → `{ok}` | 400 invalid, 409 in_use, 401 reauth, 429 rate_limited, 410 expired, 423 locked | unknown |
| Change email link | POST `/v1/profile/email` + GET `/v1/profile/email/verify?token=` | `{email}` → `{pending}` | 400 invalid, 409 in_use, 410 expired | unknown |
| Profile photo presigned upload | POST `/v1/uploads/presign` + PUT object URL | `{contentType,size}` → `{url, key}` | 413 too_large | unknown |
| Sessions / logout | GET `/v1/sessions`, DELETE `/v1/sessions/{id|all}` | `{device,lastUsed}` | 401 | unknown |
| Tickets create/list/detail/reply | POST/GET `/v1/tickets`, GET `/v1/tickets/{id}`, POST `/v1/tickets/{id}/replies`, POST `/v1/tickets/{id}/resolve|reopen|rating` | `{topic,subject,description,attachments[],contact,tripId?}` → `{ticketId, status}` | 400 validation, 413 attachments, 404 | unknown |
| Attachment upload (presigned) | POST `/v1/uploads/presign` | ≤3 images, ≤5MB each | 413 | unknown |
| Admin support queue | Admin UI where each ticket appears (queue, assignment, SLA ~24h per `SupportConfig.REVIEW_HOURS`) | — | — | unknown |

Notes: SOS/112 is dial-only (`tel:112`), no monitoring. Payout screens skipped (TARGET=passenger; driver payouts marked coming-soon if needed later). Times stored/displayed Africa/Dar_es_Salaam; money whole TZS.
