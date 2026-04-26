# Smart Campus Operations Hub - Group 58

IT3030 Programming Applications and Frameworks Assignment 2026.

Smart Campus Operations Hub is a Spring Boot REST API and React web application for university resource bookings, maintenance tickets, notifications, and role-based user management.

## Technology Stack

- Backend: Java 17, Spring Boot 4, Spring Web, Spring Security, OAuth2 Client, JWT, Spring Data MongoDB
- Frontend: React, Vite, Axios, Bootstrap, HTML5 QR scanner
- Database: MongoDB Atlas
- API testing: Postman collections in `postman/`
- CI: GitHub Actions for backend tests, frontend lint/build, and Postman artifact validation

## Main Features

- Facilities and assets catalogue with resource CRUD, active/out-of-service status, capacity, location, and availability windows.
- Booking workflow: `PENDING -> APPROVED/REJECTED -> CANCELLED/CHECKED_IN`, overlap conflict checks, QR generation, and check-in.
- Maintenance ticket workflow: ticket creation, image upload support, technician assignment, assignment history, status updates, comments, and replies.
- Notifications for booking decisions, ticket updates, comments, assignments, and role changes.
- Authentication with Google OAuth2 and local JWT login for demo/testing.
- Roles: `USER`, `ADMIN`, and `TECHNICIAN`.

## Team Contribution Summary

| Module | Member |
| --- | --- |
| Facilities catalogue and resource management | Dakshika M G N - IT23813984 |
| Booking workflow and QR check-in | Chamya N D - IT23848184 |
| Incident ticketing and attachments | Chamoda M S - IT23832480 |
| Authentication, user management, OAuth, and notifications | Christopher K K - IT23827530 |

## Backend Setup

1. Install Java 17 and Maven, or use the included Maven wrapper.
2. Create/update `backend/smartcampus/.env` with local secrets:

```env
SERVER_PORT=8080
SPRING_DATA_MONGODB_URI=mongodb+srv://<username>:<password>@<cluster>/<database>?retryWrites=true&w=majority
AUTH_JWT_SECRET=smart-campus-group58-local-demo-secret-2026
AUTH_JWT_EXPIRATION_SECONDS=86400
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID=<google-client-id>
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET=<google-client-secret>
APP_FRONTEND_URL=http://localhost:5173
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
APP_SEED_ENABLED=true
```

3. Run the backend:

```powershell
cd backend/smartcampus
.\mvnw.cmd spring-boot:run
```

The first successful startup seeds demo users and resources. MongoDB Atlas creates collections lazily when these documents are inserted.

## Frontend Setup

```powershell
cd frontend/smartcampus
npm install
npm run dev
```

Open `http://localhost:5173`.

## Google OAuth Setup

In Google Cloud OAuth client settings:

- Authorized JavaScript origin: `http://localhost:3000` and/or `http://localhost:5173`
- Authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`

The backend redirects successful OAuth logins to `http://localhost:5173/oauth2/redirect?token=...`.

## Demo Accounts

| Role | Email | Password |
| --- | --- | --- |
| USER | `student@example.com` | `ChangeMe123!` |
| ADMIN | `admin@example.com` | `ChangeMe123!` |
| TECHNICIAN | `technician@example.com` | `ChangeMe123!` |

## Postman Verification

Import the collections and environment from `postman/` into the VS Code Postman extension.

Run in order:

1. `00 Health and Smoke`
2. `01 Resources`
3. `02 Bookings`
4. `03 Tickets`
5. `04 Notifications and Auth`

See `postman/README.md` for details.

## Tests And CI

Backend:

```powershell
cd backend/smartcampus
.\mvnw.cmd test
```

Frontend:

```powershell
cd frontend/smartcampus
npm run lint
npm run build
```

GitHub Actions runs backend tests with MongoDB, frontend lint/build, and validates Postman JSON artifacts.

## Documentation

- Final report: `docs/IT3030_PAF_Assignment_2026_Group58_Final_Report.docx`
- Viva guide: `docs/viva-guidance.md`
- Postman guide: `postman/README.md`

## AI Usage Disclosure

AI assistance from Codex/ChatGPT was used for code, configuration, documentation, Postman artifact, and report preparation support. Outputs were reviewed, tested, adapted, and accepted by the project team before inclusion.

## Security Notes

Secrets must stay in ignored `.env` files and must not be committed. If a MongoDB password or Google OAuth secret is exposed in chat, screenshots, commits, or documentation, rotate it before production use.
