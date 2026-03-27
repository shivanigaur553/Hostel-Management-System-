# GEHU Hostel Management System

**Graphic Era Hill University, Dehradun — PBL Phase 2**

A full-stack hostel management web application with a Java backend and HTML/CSS/JS frontend.

---

## Team

| Name         | Role               | Owns                                          |
| ------------ | ------------------ | --------------------------------------------- |
| Shivani Gaur | Team Leader        | Server, Admin UI, README, GitHub coordination |
| Aryan Negi   | Frontend Developer | Student UI, Shared CSS Design System          |
| Aryan Panwar | Backend Developer  | Data Models, Business Logic                   |

---

## Tech Stack

- **Backend:** Java (`com.sun.net.httpserver`) — REST API on port 8080
- **Frontend:** HTML5, CSS3, JavaScript
- **API Style:** REST / JSON (no external libraries)

---

## Project Structure

```
gehu-hostel-management/
├── BE/
│   ├── Main.java           ← Entry point (Shivani Gaur)
│   ├── Server.java         ← HTTP server & REST API (Shivani Gaur)
│   ├── HostelSystem.java   ← Business logic (Aryan Panwar)
│   ├── Room.java           ← Room model (Aryan Panwar)
│   ├── Student.java        ← Student model (Aryan Panwar)
│   └── User.java           ← User/auth model (Aryan Panwar)
├── frontend/
│   ├── style.css           ← Shared CSS design system (Aryan Negi)
│   ├── index.html          ← Portal home (Aryan Negi)
│   ├── student/
│   │   ├── login.html      ← Student login (Aryan Negi)
│   │   └── dashboard.html  ← Student dashboard (Aryan Negi)
│   └── admin/
│       ├── login.html      ← Admin login (Shivani Gaur)
│       └── dashboard.html  ← Admin dashboard (Shivani Gaur)
└── assets/
    └── Background.webp     ← GEHU campus image (Aryan Negi)
```

---

## How to Run

### 1. Compile

```bash
javac -d out BE/*.java
```

### 2. Start the server

```bash
java -cp out BE.Main
```

You should see:

```
GEHU Hostel Management System
GEHU Hostel — Server started
http://localhost:8080
```

### 3. Open the frontend

Open `frontend/index.html` in your browser.

---

## Login Credentials

| Role    | Username | Password |
| ------- | -------- | -------- |
| Admin   | admin    | admin123 |
| Student | CS101    | pass123  |

---

## API Endpoints (port 8080)

| Method | Endpoint                    | Description              |
| ------ | --------------------------- | ------------------------ |
| GET    | /api/rooms                  | List all rooms           |
| POST   | /api/rooms                  | Add a room               |
| PUT    | /api/rooms/{roomNo}         | Update room              |
| DELETE | /api/rooms/{roomNo}         | Delete room              |
| GET    | /api/students               | List all students        |
| POST   | /api/students               | Register student         |
| DELETE | /api/students/{rollNo}      | Remove student           |
| PUT    | /api/students/{rollNo}/room | Allocate room            |
| DELETE | /api/students/{rollNo}/room | Deallocate room          |
| PUT    | /api/students/{rollNo}/fee  | Update fee status        |
| POST   | /api/login                  | Login (admin or student) |
| GET    | /api/stats                  | Dashboard statistics     |

---

=======

# Modern-Hostel-Management-System

A full-stack Hostel Management System built using Java (backend) and HTML, CSS, JavaScript (frontend) with REST APIs for room allocation, student management, and admin control.

> > > > > > > e05c9b70bf60ed2e37bba36b0315cc9c259dccb0
