# IT3030 PAF Assignment Execution: Smart Campus Operations Hub

**Goal:** Act as an autonomous Senior Full-Stack Developer agent. You have permission to read the codebase, execute terminal commands, run servers, use browser automation to take screenshots, and modify files to complete this university project.

## 1. Project Stack & Credentials
* **Backend:** Java Spring Boot (REST API, layered architecture)
* **Frontend:** React
* **Database:** MongoDB (snehadhaya55@gmail.com / Snehadhaya@2002)
* **Deployment:** Vercel (using GitHub Auth)

## 2. Git Branch Resolution & Cleanup
Execute the necessary terminal commands to clean up and merge these overlapping branches into a stable `main` or `dev` branch. Handle any merge conflicts autonomously:
* Member 1: `Nethmini-member-1` & `origin/Nethmini-member-1` & `nethmini-member-1`
* Member 2: `Abinayan-member-2` & `member-2`
* Member 3: Sneha (Ensure her commits are integrated)
* Member 4: `member-4` & `Akshayan-member-4`

## 3. Code Review & Implementation Fixes
Analyze the current codebase. If any of the following assignment requirements are broken or missing, write and commit the fixes:
* **Module A (Facilities):** Resource catalogue, metadata, search/filtering.
* **Module B (Bookings):** PENDING/APPROVED/REJECTED workflow, conflict prevention (no overlaps), Admin review.
* **Module C (Tickets):** OPEN/IN_PROGRESS/RESOLVED/CLOSED workflow, up to 3 image attachments, technician assignment, comments.
* **Module D (Notifications):** Web UI notifications for status changes.
* **Module E (Auth):** OAuth 2.0 (Google), USER/ADMIN roles, protected routes.
* **API Constraints:** Verify that the 4 members each have at least 4 endpoints using different HTTP methods (GET, POST, PUT/PATCH, DELETE) with strict RESTful naming and standard HTTP status codes.

## 4. Innovation Feature (10 Marks)
Implement a completely unique, highly impressive "out of the box" feature that fits a Smart Campus Hub to secure the 10 Creativity marks. Write the code, integrate it into the UI/API, and document how it works.

## 5. Deployment via Vercel CLI
Execute the deployment for the React frontend and Spring Boot backend. 
1. Create necessary configuration files (e.g., `vercel.json`).
2. Run the deployment commands in the terminal using the provided GitHub/Vercel credentials.
3. Verify the live URLs are functioning.

## 6. Automated Screenshot Capture
Spin up the local development servers (or use the deployed URLs). Use your browser automation capabilities to navigate to the following and capture screenshots (save them to a `/docs/screenshots` folder):
1. **Frontend UI:** All main pages (Dashboard, Booking, Tickets, Notifications, and the new Innovation feature).
2. **GitHub:** Navigate to the repository's commit history and branching tree and screenshot it.
3. **Database:** Since you cannot open desktop GUIs, write a script to query the MongoDB database, print the collections/tables in the terminal in a clean tabular format, and screenshot the terminal output.
4. **API Testing:** Run curl commands for the main endpoints in the terminal and screenshot the successful JSON responses to substitute for Postman evidence.

## 7. Final Report Compilation
Create a markdown file (`Final_Report_Draft.md`) containing the text for the final PDF submission. It must include:
1. **Introduction**
2. **Functional & Non-Functional Requirements**
3. **Architecture Diagrams:** Use Mermaid.js syntax for the Overall, REST API, and Frontend architectures.
4. **System Functions:** Detail the specific endpoint and UI contributions of Nethmini, Abinayan, Sneha, and Akshayan.
5. **AI Disclosure Statement:** Draft a statement acknowledging the use of AI tools for code generation and testing per the SLIIT academic integrity rubric.
6. **Image Placeholders:** Clearly mark where the screenshots you saved in step 6 should be inserted.