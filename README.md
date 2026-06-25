# PlayScout - Sports Facility Booking Platform

A full-stack web application that connects sports enthusiasts with venues, enabling them to book facilities, create games, find players, and manage bookings with real-time communication.

**Built with Spring Boot + React**

---

## Live Demo

🌐 **https://playscout.dev**

> **Note:** The backend uses Supabase's free tier. If the application has been inactive for some time, the database may take a few seconds to resume on the first request.

---

## Project Background

## Project Background

PlayScout was originally developed as a collaborative university project that I contributed to as part of a development team. This repository is my fork of the original project, where I extended it with production-ready deployment, cloud infrastructure, and hosting improvements while preserving the core application features.

---

## Home Page Preview

![Home Page Screenshot](previews/homepage.png)

The screenshots of the remaining pages are available in the `previews` folder.

---

## Features

* **Venue Management** – Browse sports venues with filters, view detailed information including facilities and images, and manage venues through an admin panel.
* **Booking System** – Check availability, reserve time slots, manage bookings, cancellations, and refunds.
* **Game Management** – Create games, set player limits, join existing games, and manage join requests.
* **Real-Time Chat** – WebSocket-based messaging between game participants with instant updates.
* **Player Discovery** – Explore player profiles, interests, and connect with nearby players.
* **Sports News Feed** – Stay updated with the latest sports news using the GNews API.
* **Payment Integration** – Secure booking payments with payment history and refund tracking.
* **Authentication & Security** – JWT authentication, OAuth2 login, and Spring Security.

---

## Production Deployment

This fork extends the original project with production-ready deployment and infrastructure improvements:

* Dockerized frontend and backend
* Docker Compose multi-container deployment
* nginx reverse proxy
* Azure Virtual Machine deployment
* Custom domain (**https://playscout.dev**)
* HTTPS using Let's Encrypt
* Production environment configuration
* Deployment documentation

---

## Tech Stack

### Backend

* **Framework:** Spring Boot 4.0.5
* **Language:** Java 25
* **Database:** Supabase (PostgreSQL)
* **Authentication:** Spring Security with JWT
* **Real-time:** WebSocket with STOMP

### Frontend

* **Framework:** React 19
* **Build Tool:** Vite
* **Styling:** CSS3
* **HTTP Client:** Axios
* **Real-time:** SockJS + STOMP Client

### Storage

* Supabase (PostgreSQL + File Storage)

### Deployment

* Docker
* Docker Compose
* nginx
* Azure Virtual Machine
* Let's Encrypt
* Name.com

---

## Deployment Architecture

```text
                Internet
                    │
        https://playscout.dev
                    │
              Azure Ubuntu VM
                    │
            Docker Compose
          ┌──────────┴──────────┐
          │                     │
Frontend (nginx + React)   Backend (Spring Boot)
          │                     │
          └──────────┬──────────┘
                     │
          Supabase PostgreSQL
```

---

## Installation & Setup

### Prerequisites

* Java 25 or higher
* Node.js 18+
* Maven 3.8+
* Git
* Supabase Account

---

### Clone the Repository

```bash
git clone https://github.com/omkarh20/Playscout_V2.git
cd Playscout_V2
```

---

## Local Development

### 1. Database Setup (Supabase)

1. Create a Supabase project.
2. Open the SQL Editor.
3. Execute `database/schema.sql`.
4. Populate sample data from the `data/` directory if required.

---

### 2. Backend Setup

```bash
cd backend
```

Copy `.env.example` to `.env` and configure:

* Database credentials
* JWT secret
* OAuth credentials
* Stripe keys (optional)
* Other required environment variables

Run the backend:

Windows:

```bash
.\mvn.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Backend runs on:

```
http://localhost:8080
```

---

### 3. Frontend Setup

```bash
cd frontend
```

Copy `.env.example` to `.env` and configure:

* Backend URL
* Supabase Storage URL
* OAuth configuration

Run:

```bash
npm install
npm run dev
```

Frontend runs on:

```
http://localhost:5173
```

---

## Docker Deployment

Build the backend:

```bash
cd backend
mvn clean package
cd ..
```

Start the application:

```bash
docker compose up --build -d
```

The application will be available at:

```
http://localhost
```

or configure a custom domain for production deployment as demonstrated in this repository.
