# Backend agent prompt: passenger profile photos

Verify and complete the profile-photo backend used by the Rida Kotlin passenger app. Inspect the existing backend implementation first and preserve its authentication and storage conventions.

The Android app now has an image picker in Profile, upload/loading/error/retry states, and a shared avatar displayed in Profile and Home. It uses the existing Retrofit endpoint declaration below. Implement or reconcile this exact contract:

- `POST /api/v1/passenger/profile/me/image`, authenticated as the current passenger.
- Accept `multipart/form-data` with one binary file part named `profile_picture`.
- Accept valid JPEG, PNG and WebP images, up to 5 MiB (5,242,880 bytes). Validate actual image data, dimensions and file size on the server. Return useful 413/415/422 errors for invalid inputs.
- Store the photo against the authenticated passenger; prevent changing another passenger's image through request parameters or filenames. Generate storage filenames and replace the previous image safely.
- Return a successful JSON object containing `profile_picture_url`, a directly readable HTTPS URL. Return a new/versioned URL after replacement so clients do not retain the previous image. If storage uses signed URLs, refresh expired URLs when the profile is read.
- Include the same `profile_picture_url` in `GET /api/v1/passenger/profile/me`, so photos survive app restart and sign-in on another device. Return null when no photo is set. The avatar image loader uses the returned URL without an Authorization header, so use a readable HTTPS URL or signed HTTPS URL rather than an authenticated image endpoint.
- Keep the existing user's photo when an upload fails. Use appropriate authentication failures and avoid exposing storage internals in error responses.

Example successful response:

```json
{
  "profile_picture_url": "https://media.example.com/passengers/photo-version-2.jpg"
}
```

Add backend tests for successful upload/profile readback, replacement, unauthorized requests, account isolation, empty/invalid images, oversized uploads and unsupported formats. Run the backend's required checks and report any changes needed in the Android contract. Do not claim production verification without exercising the deployed endpoint using an authorized test account.

Home's new “Look for rides” button currently uses the existing destination search and route-planning flow. Existing/shared-ride discovery is a separate feature and is not part of this photo endpoint.
