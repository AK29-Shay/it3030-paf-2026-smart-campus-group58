# Smart Campus Operations Hub - Viva Guidance

## 1. Opening Summary

Smart Campus Operations Hub is a role-based web platform for campus facilities, resource bookings, maintenance incidents, and notifications. The backend is a Spring Boot REST API secured with JWT and Google OAuth2. The frontend is a React/Vite application that consumes the API. Data is persisted in MongoDB Atlas.

## 2. Demo Flow

1. Start MongoDB-backed backend from `backend/smartcampus` with `.\mvnw.cmd spring-boot:run`.
2. Start frontend from `frontend/smartcampus` with `npm run dev`.
3. Open `http://localhost:5173`.
4. Log in as admin with `admin@example.com` and `ChangeMe123!`.
5. Show resource catalogue and create/update one resource.
6. Log in as user with `student@example.com` and create a booking.
7. Switch to admin, approve or reject the booking, and show notification/QR behavior.
8. Create a ticket as user, assign it to technician as admin, update status as technician.
9. Add a comment/reply and show notifications.
10. Open Postman collections and show successful requests/responses.

## 3. Member-Wise Explanation Points

| Member | Area | What To Explain |
| --- | --- | --- |
| IN Chinthana - IT23699526 | Facilities catalogue | Resource entity, MongoDB `resources` collection, CRUD endpoints, filtering, validation, active/out-of-service status. |
| Abinayan K - IT23764552 | Bookings | Booking workflow, conflict query, QR generation, check-in endpoint, admin approval/rejection. |
| Dhayabari B - IT23741478 | Tickets | Ticket entity, file upload safety, technician assignment, assignment history, status flow, comments/replies. |
| Akshayan I - IT23587106 | Auth and notifications | JWT token creation, Google OAuth flow, role checks, notification storage and read/delete endpoints. |

## 4. Database Design

MongoDB database: `smartcampus`

Collections:

- `users`: local/OAuth users, roles, password hash, notification preference, technician specialty.
- `resources`: bookable facilities and equipment.
- `bookings`: booking requests, workflow status, QR path, check-in time.
- `tickets`: incident reports, priority, status, image paths, assignment.
- `ticket_comments`: ticket comments and admin replies.
- `ticket_assignment_history`: audit trail for technician assignment changes.
- `notifications`: user notification panel records.

MongoDB creates collections when the application first inserts data. If Atlas shows no collections, start the backend with `APP_SEED_ENABLED=true` or run the Postman create requests.

## 5. API And Security Talking Points

- Authentication returns JWTs from `/api/auth/login`.
- JWT contains user id, email, role, and name.
- `JwtAuthenticationFilter` reads `Authorization: Bearer <token>` and sets Spring Security authentication.
- Admin-only actions use `@PreAuthorize("hasRole('ADMIN')")`.
- Technician ticket status updates use `hasAnyRole('ADMIN','TECHNICIAN')`.
- Public catalogue reads are allowed, but write operations require admin.
- Validation errors return `400`, unauthorized requests return `401`, forbidden role access returns `403`, missing data returns `404`, and booking conflicts return `409`.

## 6. OAuth Flow

1. User clicks Google sign-in in React.
2. Browser goes to `/oauth2/authorization/google`.
3. Google redirects to `http://localhost:8080/login/oauth2/code/google`.
4. `CustomOAuth2UserService` creates or updates the user.
5. `OAuth2LoginSuccessHandler` generates a JWT.
6. Backend redirects to `http://localhost:5173/oauth2/redirect?token=<jwt>`.
7. Frontend stores the token and loads the user profile.

## 7. Postman Evidence

Run order:

1. `00 Health and Smoke`: checks backend and stores user/admin/technician tokens.
2. `01 Resources`: creates, updates, validates, and deletes resources.
3. `02 Bookings`: creates booking, proves conflict detection, approval, rejection, QR, and check-in.
4. `03 Tickets`: creates ticket, assigns technician, updates status, comments, replies, deletes comment.
5. `04 Notifications and Auth`: dynamic signup, notifications, role update, unread count, cleanup.

Important environment variables:

- `accessToken`, `adminToken`, `technicianToken`
- `userId`, `adminId`, `technicianId`
- `resourceId`, `resourceName`, `bookingId`, `ticketId`, `commentId`

## 8. Likely Viva Questions

**Why MongoDB?**  
MongoDB is flexible for this project because tickets, notifications, user profiles, and booking metadata can evolve without migrations. It also works well with Spring Data repositories.

**How do you prevent booking conflicts?**  
The booking service checks existing `PENDING` and `APPROVED` bookings for the same resource where the existing start is before the requested end and the existing end is after the requested start.

**How is authorization enforced?**  
Spring Security authenticates JWTs and method-level `@PreAuthorize` rules restrict admin and technician actions.

**What happens when a user signs in with Google for the first time?**  
The OAuth service reads Google profile attributes, creates a `USER` record in MongoDB, then the success handler issues a JWT.

**How are notifications created?**  
Service methods call `NotificationService` after events such as booking approval/rejection, ticket assignment, ticket status changes, role changes, and ticket comments.

**How are files handled safely?**  
Ticket upload accepts up to three files, checks content type, limits size to 5 MB per image, generates unique filenames, and stores paths under `uploads/tickets`.

**What are the unique features?**  
QR booking check-in, technician assignment history, SLA timing fields for tickets, notification preferences, role-specific dashboards, and Postman automation.

## 9. AI Usage Disclosure For Viva

Say clearly: AI assistance from Codex/ChatGPT was used to support code review, configuration cleanup, Postman collection preparation, documentation, and final report drafting. The team reviewed, tested, and adapted the generated output before submission.

## 10. Quick Troubleshooting

- No MongoDB collections: start backend with `APP_SEED_ENABLED=true` or run Postman create requests.
- Google login fails: verify Google redirect URI is exactly `http://localhost:8080/login/oauth2/code/google`.
- Frontend cannot call API: confirm backend is on port `8080` and frontend proxy is running from `frontend/smartcampus`.
- Postman `401`: rerun `00 Health and Smoke` to refresh tokens.
- Postman `403`: use `adminToken` for admin-only requests.
