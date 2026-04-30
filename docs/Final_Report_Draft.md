# Sri Lanka Institute of Information Technology

## Programming Applications and Frameworks (IT3030)

# Final Assignment Report

**Group ID:** Group 58  
**Project:** Smart Campus Operations Hub  
**Repository:** https://github.com/AK29-Shay/it3030-paf-2026-smart-campus-group58  
**Date:** 30 April 2026

| Student Name | Contribution Area |
| --- | --- |
| Nethmini | Facilities and assets catalogue |
| Abinayan | Booking workflow and QR check-in |
| Sneha | Maintenance and incident ticketing |
| Akshayan | Authentication, authorization, notifications, and innovation integration |

## Table of Contents

1. Introduction  
2. Functional Requirements and Non-Functional Requirements  
3. Overall Architecture Diagram  
4. REST API Architecture Diagram  
5. Frontend Architecture Diagram  
6. System Functions  
7. GitHub  
8. AI Disclosure Statement  
9. References  

## 1. Introduction

Smart Campus Operations Hub is a production-inspired web application for managing day-to-day university operations. The system supports bookable facilities and assets, booking request workflows, maintenance incident handling, web notifications, role-based access, and Google OAuth 2.0 authentication.

The project follows the IT3030 PAF 2026 assignment requirement to build a Spring Boot REST API and a React client application backed by a persistent database. MongoDB Atlas is used for persistence, GitHub is used for version control, GitHub Actions is used for quality checks, and Vercel is the prepared deployment target for the React frontend. The Spring Boot backend is demonstrated locally because Vercel's official function runtimes do not include Java/Spring Boot.

The innovation feature is the **Campus Command Center**, an admin-only dashboard that aggregates bookings, resources, users, and tickets into live operational metrics, risk alerts, resource demand, seven-day booking trends, and ticket SLA watchlist data.

## 2. Functional Requirements and Non-Functional Requirements

### Functional Requirements

| Module | Requirement | Implementation |
| --- | --- | --- |
| Facilities and Assets | Maintain resources with type, category, capacity, location, availability, description, and status. | Resource CRUD API and React resource catalogue/admin pages. |
| Search and Filtering | Search/filter resources by type, location, capacity, and status. | Resource list and admin resource filters. |
| Bookings | Users request bookings with resource, purpose, attendees, start time, and end time. | Booking API and user booking UI. |
| Booking Workflow | PENDING to APPROVED/REJECTED; approved bookings can be cancelled and checked in. | Booking service workflow, admin review UI, QR check-in. |
| Conflict Prevention | Prevent overlapping PENDING/APPROVED bookings for the same resource. | Booking service overlap validation. |
| Tickets | Users create incident tickets with category, priority, description, contact/location context, and up to 3 images. | Ticket API, upload handling, user/admin/technician ticket UI. |
| Ticket Workflow | OPEN to IN_PROGRESS to RESOLVED to CLOSED; admin may reject. | Ticket status API and technician/admin UI. |
| Assignment and Comments | Assign technicians and allow ticket comments/replies with ownership rules. | Ticket assignment history and comments API/UI. |
| Notifications | Notify users for booking decisions, ticket status changes, assignments, comments, and role changes. | Notification service, bell, dropdown, and full notification page. |
| Authentication | Google OAuth 2.0 and local JWT demo login. | Spring Security OAuth2, JWT filter, protected frontend routes. |
| Authorization | USER, ADMIN, and TECHNICIAN roles. | Role-based backend method security and frontend protected routes. |
| Innovation | Live operational intelligence view. | Campus Command Center API and admin UI. |

### Non-Functional Requirements

| Category | Requirement | Implementation |
| --- | --- | --- |
| Security | Protect sensitive API routes and enforce roles. | Spring Security, JWT, OAuth2, ADMIN/TECHNICIAN restrictions. |
| Reliability | Use persistent storage and avoid in-memory-only data. | MongoDB repositories for users, resources, bookings, tickets, comments, and notifications. |
| Validation | Reject invalid input and unsafe workflow transitions. | Jakarta validation, service checks, conflict exceptions, image limits. |
| Maintainability | Use layered architecture. | Controller, service, repository, DTO, entity, and enum layers. |
| Usability | Provide clear workflows by role. | Student, technician, and admin dashboards with task-focused navigation. |
| Quality | Provide tests and API evidence. | Backend tests, frontend lint/build, Postman collections, curl script, screenshots. |

## 3. Overall Architecture Diagram

```mermaid
flowchart LR
    User[Campus User] --> React[React Vite Frontend]
    Admin[Admin User] --> React
    Tech[Technician] --> React
    React -->|JWT / OAuth2 redirect token| API[Spring Boot REST API]
    API --> Security[Spring Security and JWT Filter]
    API --> Services[Service Layer]
    Services --> Repositories[MongoDB Repositories]
    Repositories --> Mongo[(MongoDB Atlas)]
    API --> Uploads[Ticket Image Upload Storage]
    API --> QR[QR Code Generation]
    React --> Vercel[Vercel Frontend Deployment]
    API --> LocalDemo[Local Spring Boot Demo Backend]
```

**Screenshot placeholder:** Insert generated overall architecture diagram image here.

## 4. REST API Architecture Diagram

```mermaid
flowchart TB
    Client[React Client] --> Controllers[REST Controllers]
    Controllers --> Auth[Auth and User Services]
    Controllers --> Resource[Resource Service]
    Controllers --> Booking[Booking Service]
    Controllers --> Ticket[Ticket Service]
    Controllers --> Notify[Notification Service]
    Controllers --> Command[Command Center Service]
    Booking --> QR[QRCode Service]
    Auth --> UserRepo[User Repository]
    Resource --> ResourceRepo[Resource Repository]
    Booking --> BookingRepo[Booking Repository]
    Ticket --> TicketRepo[Ticket and Comment Repositories]
    Notify --> NotificationRepo[Notification Repository]
    Command --> BookingRepo
    Command --> ResourceRepo
    Command --> TicketRepo
    Command --> UserRepo
```

**Screenshot placeholder:** Insert generated REST API architecture diagram image here.

## 5. Frontend Architecture Diagram

```mermaid
flowchart TB
    App[App.jsx Router] --> Protected[ProtectedRoute]
    App --> AuthPages[Login, Signup, OAuth Redirect]
    App --> ResourcePages[Resource Catalogue and Admin Resource Page]
    App --> BookingPages[Bookings, Calendar, Admin Review, QR Verify]
    App --> TicketPages[Ticket Create, My Tickets, Admin Tickets, Technician Tickets]
    App --> NotificationPages[Notification Bell, Dropdown, List]
    App --> AdminPages[Admin Dashboard, Users, Command Center]
    Protected --> AuthContext[AuthContext]
    AuthContext --> AuthService[authService]
    ResourcePages --> API[Axios API Layer]
    BookingPages --> API
    TicketPages --> API
    NotificationPages --> API
    AdminPages --> API
```

**Screenshot placeholder:** Insert generated frontend architecture diagram image here.

## 6. System Functions

### Nethmini - Facilities and Assets Catalogue

Implemented resource catalogue and admin resource management.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/resources` | List all resources. |
| POST | `/api/resources` | Create a resource. |
| PUT | `/api/resources/{id}` | Update a resource. |
| DELETE | `/api/resources/{id}` | Delete a resource. |

UI evidence:

![Screenshot: Resources catalogue](screenshots/07_resources.png)
![Screenshot: Admin resources](screenshots/18_admin_resources.png)

### Abinayan - Booking Management

Implemented booking requests, conflict prevention, admin review, QR regeneration, and check-in.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/bookings/my-bookings` | View authenticated user's bookings. |
| POST | `/api/bookings` | Create a booking request. |
| PUT | `/api/bookings/{id}/approve` | Approve a pending booking. |
| DELETE | `/api/bookings/{id}` | Delete a pending booking. |

UI evidence:

![Screenshot: Booking list](screenshots/08_booking_list.png)
![Screenshot: Booking calendar](screenshots/09_booking_calendar.png)
![Screenshot: Admin bookings](screenshots/19_admin_bookings.png)
![Screenshot: QR public lookup](screenshots/05_qr_public_lookup.png)

### Sneha - Maintenance and Incident Ticketing

Implemented ticket creation, image uploads, assignment, status updates, comments, and SLA-related ticket response data.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/tickets/{id}` | View ticket details. |
| POST | `/api/tickets` | Create a ticket with image attachments. |
| PUT | `/api/tickets/{ticketId}/status` | Update ticket status. |
| DELETE | `/api/tickets/{ticketId}/comments/{commentId}` | Delete a comment with ownership rules. |

UI evidence:

![Screenshot: Create ticket](screenshots/11_create_ticket.png)
![Screenshot: My tickets](screenshots/12_my_tickets.png)
![Screenshot: Technician tickets](screenshots/15_technician_tickets.png)
![Screenshot: Admin tickets](screenshots/20_admin_tickets.png)

### Akshayan - Auth, Notifications, Roles, and Innovation

Implemented OAuth/JWT authentication, protected routes, notifications, user role management, and the Campus Command Center innovation feature.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/auth/me` | Get authenticated user profile. |
| POST | `/api/auth/login` | Local JWT demo login. |
| PATCH | `/api/notifications/{id}/read` | Mark notification as read. |
| DELETE | `/api/notifications/{id}` | Delete notification. |
| GET | `/api/admin/command-center` | Admin-only innovation analytics snapshot. |

UI evidence:

![Screenshot: Login](screenshots/02_login.png)
![Screenshot: Notifications](screenshots/13_notifications.png)
![Screenshot: User management](screenshots/21_user_management.png)
![Screenshot: Command Center](screenshots/17_command_center.png)

## 7. GitHub

**Repository Link:** https://github.com/AK29-Shay/it3030-paf-2026-smart-campus-group58  
**OneDrive Link:** Insert final shared submission folder link here.

GitHub evidence:

![Screenshot: GitHub commits](screenshots/23_github_commits.png)
![Screenshot: GitHub branches](screenshots/24_github_branches.png)
![Screenshot: GitHub pull requests](screenshots/25_github_pull_requests.png)

Testing and database evidence:

![Screenshot: MongoDB collections](screenshots/26_mongodb_collections.png)
![Screenshot: API curl evidence](screenshots/27_api_curl_evidence.png)
- Evidence scripts: `scripts/query_mongodb.py`, `scripts/api_tests.sh`, and Windows-compatible `scripts/api_tests.ps1`.

Deployment evidence:

- Frontend deployment target: Vercel React/Vite project.
- Backend demonstration target: Local Spring Boot REST API.
- Reason: Vercel official function runtimes include Node.js, Bun, Python, Rust, Go, Ruby, Wasm, and Edge, but not Java/Spring Boot. Java backend deployment on Vercel would require a nonstandard custom/community runtime and was not claimed as the final backend hosting path.
- Insert Vercel deployment URL and screenshot after running the Vercel CLI with authenticated credentials.

## 8. AI Disclosure Statement

AI tools, including Codex/ChatGPT, were used to support code generation, debugging, test creation, report drafting, script preparation, and verification planning. The generated outputs were reviewed, adapted, and tested by the project team before inclusion. The team remains responsible for understanding and explaining the implemented endpoints, database design, UI components, and project decisions during the viva.

## 9. References

- SLIIT IT3030 PAF Assignment 2026 specification.
- SLIIT IT3030 PAF Assignment 2026 marking rubric.
- Spring Boot documentation: https://spring.io/projects/spring-boot
- React documentation: https://react.dev
- MongoDB documentation: https://www.mongodb.com/docs
- Vercel Functions runtimes documentation: https://vercel.com/docs/functions/runtimes
- Mermaid diagram syntax: https://mermaid.js.org
