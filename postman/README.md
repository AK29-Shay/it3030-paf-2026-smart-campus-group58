# Smart Campus Postman Pack

This folder contains the API verification pack for the IT3030 PAF 2026 Smart Campus Operations Hub.

## Import In VS Code Postman Extension

1. Open the Postman extension in VS Code.
2. Import every file in `postman/collections`.
3. Import `postman/environments/local.environment.json`.
4. Select the `Local` environment.
5. Start the Spring Boot backend on `http://localhost:8080`.
6. Run the collections in this order:
   - `00 Health and Smoke`
   - `01 Resources`
   - `02 Bookings`
   - `03 Tickets`
   - `04 Notifications and Auth`

The first collection logs in the seeded `USER`, `ADMIN`, and `TECHNICIAN` accounts and stores bearer tokens plus IDs into the selected environment.

## Seeded Accounts

| Role | Email | Password |
| --- | --- | --- |
| USER | `student@example.com` | `ChangeMe123!` |
| ADMIN | `admin@example.com` | `ChangeMe123!` |
| TECHNICIAN | `technician@example.com` | `ChangeMe123!` |

## What The Collections Prove

- MongoDB-backed resource CRUD with validation and admin protection.
- Booking creation, conflict detection, approval, rejection, QR regeneration, and check-in.
- Ticket creation, assignment audit history, technician status update, comments, replies, and deletion.
- JWT login, profile lookup, role management, and notification panel endpoints.

If MongoDB Atlas has no collections, run the backend once with `APP_SEED_ENABLED=true`, then rerun `00 Health and Smoke` and `01 Resources`. MongoDB creates collections lazily when documents are inserted.
