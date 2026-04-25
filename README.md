# it3030-paf-2026-smart-campus-groupXX

Smart Campus Operations Hub is a full-stack university assignment scaffold for managing campus operations such as resources, bookings, maintenance requests, notifications, and user access.

## Tech Stack

- Backend: Java Spring Boot
- Frontend: React
- Database: H2 for development, ready to swap to MySQL
- Build tools: Maven and npm
- CI: GitHub Actions

## Folder Structure

```text
it3030-paf-2026-smart-campus-groupXX/
├── backend/              # Spring Boot API source and Maven config
├── frontend/             # React application source and npm config
├── docs/                 # API, testing, contribution, diagrams, and AI usage docs
├── .github/workflows/    # Backend and frontend CI workflows
├── README.md
└── .gitignore
```

## Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend local URL:

```text
http://localhost:8080
```

Sample endpoint:

```text
GET http://localhost:8080/api/resources
```

## Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend local URL:

```text
http://localhost:5173
```

## Team Contribution

| Member | Student ID | Contribution |
| --- | --- | --- |
| Member 1 | IT00000000 | Backend |
| Member 2 | IT00000000 | Frontend |
| Member 3 | IT00000000 | Testing |
| Member 4 | IT00000000 | Documentation |

## Notes

This scaffold is intentionally simple and ready for expansion into the full Smart Campus Operations Hub system.
