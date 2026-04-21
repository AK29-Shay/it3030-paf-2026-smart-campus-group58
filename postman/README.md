# Postman Workspace Setup

## Workspace
Use the Postman browser app to create a shared workspace named `IT3030 PAF 2026 - Smart Campus Group58`.

Use the VS Code Postman extension only after the shared workspace exists and the exported files in this folder have been imported.

## Team Ownership

| Collection | Owner | Scope |
| --- | --- | --- |
| `00 Health and Smoke` | Shared | Common smoke checks, quick auth sanity checks, and draft bootstrap requests |
| `01 Resources` | You | Facilities and assets catalogue |
| `02 Bookings` | `snehadhaya55@gmail.com` | Booking workflow and conflict handling |
| `03 Tickets` | `Imbnethminichinth@gmail.com` | Incident tickets, comments, and technician updates |
| `04 Notifications and Auth` | `Kesavanabinayan12@gmail.com` | Login, notification flows, role and auth scaffolding |

## Import Order
1. Create the workspace `IT3030 PAF 2026 - Smart Campus Group58`.
2. Create an API entry named `Smart Campus REST API`.
3. Import these collections into the workspace:
   1. `postman/collections/00-health-and-smoke.collection.json`
   2. `postman/collections/01-resources.collection.json`
   3. `postman/collections/02-bookings.collection.json`
   4. `postman/collections/03-tickets.collection.json`
   5. `postman/collections/04-notifications-and-auth.collection.json`
4. Import these environments:
   1. `postman/environments/local.environment.json`
   2. `postman/environments/demo.environment.json`
5. Set `baseUrl`, `accessToken`, and `adminToken` in the selected environment.
6. Invite these members to the workspace:
   1. `snehadhaya55@gmail.com`
   2. `Imbnethminichinth@gmail.com`
   3. `Kesavanabinayan12@gmail.com`

## Environment Variables

| Variable | Purpose |
| --- | --- |
| `baseUrl` | Backend API root such as `http://localhost:8080` or `http://localhost:8080/api` |
| `accessToken` | Regular user bearer token |
| `adminToken` | Admin bearer token |
| `resourceId` | Sample or captured resource identifier |
| `bookingId` | Sample or captured booking identifier |
| `ticketId` | Sample or captured ticket identifier |
| `commentId` | Sample or captured ticket comment identifier |
| `notificationId` | Sample or captured notification identifier |
| `userEmail` | Standard user account email |
| `adminEmail` | Admin account email |
| `technicianEmail` | Technician account email |

## Working Agreement
- Each member should mainly update the collection they own.
- Keep the folder pattern consistent in every owner collection:
  - `01 Happy Path`
  - `02 Validation and Errors`
  - `03 Role and Access`
  - `99 Draft or Placeholder`
- Keep request names in the format `METHOD Action Resource`, for example `GET List Resources` or `PATCH Update Ticket Status`.
- Every owner collection should show at least four scaffolded requests across `GET`, `POST`, `PUT/PATCH`, and `DELETE`.
- Replace placeholder paths and bodies only when the backend route and payload are agreed by the team.
- After updating a collection in Postman, export it back into this repo and commit the updated JSON to `dev`.

## GitHub Integration
Primary integration is Git-based versioning of exported Postman JSON files in this repository.

Recommended flow:
1. Collaborate in the shared Postman workspace.
2. Export the updated collection or environment back into this folder.
3. Commit the JSON changes to the `dev` branch with a meaningful message.

Optional Postman Git sync can be enabled later, but only after revoking the GitHub PAT that was pasted in chat and creating a fresh fine-grained token restricted to this repo.

Fine-grained PAT guidance:
- Repository access: only `AK29-Shay/it3030-paf-2026-smart-campus-group58`
- Minimum permissions: repository contents and metadata needed for Postman sync
- Never store the token in the repo, Postman environments, or collection variables

## Viva and Rubric Evidence
- Module ownership is visible by collection owner mapping and commit history.
- Mixed HTTP methods are scaffolded per owner collection.
- Validation and role-based access scenarios are separated into dedicated folders.
- Exported JSON files in Git provide concrete testing evidence even before the backend is fully complete.
- The GitHub workflow in `.github/workflows/validate-postman-artifacts.yml` validates these Postman artifacts on push and pull request activity.

## Manual Postman UI Checklist
These actions still require your logged-in Postman account and cannot be completed from this repo alone:
1. Create the shared workspace.
2. Invite the three team members.
3. Create the API entry and link the imported collections to it if desired.
4. Optionally enable Postman Git sync after rotating the exposed PAT.
