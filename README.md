# TaskFlow - Distributed Task Management System

TaskFlow is a robust, scalable microservices-based task management platform. It leverages a modern tech stack to provide secure authentication, task creation, assignment logic, and submission handling, all orchestrated through a centralized API Gateway and Service Discovery mechanism.

## 🌐 Live Application

Frontend (Vercel): https://task-flow-pied-five.vercel.app

This is the single entry point to access the full system.

☁️ Cloud Free-Tier Note: The backend services are deployed on Render's Free Tier. If the system has been idle, the first request may take 5–10 minutes to wake up (Cold Start). Subsequent requests will behave normally. This is standard behavior for free cloud hosting and not an application issue.

## 🚀 Key Features

### Microservices Architecture: Decomposed into 5 independent, loosely coupled services for scalability and fault tolerance.

### Centralized Routing: Uses Spring Cloud Gateway as a single entry point for all client requests.

### Service Discovery: Integrated with Netflix Eureka for dynamic service registration and load balancing.

### Secure Authentication: Implements JWT (JSON Web Tokens) with BCrypt password hashing for stateless, secure user sessions.

### Role-Based Access Control (RBAC):

### ADMIN: Can create, update, delete, and assign tasks.

### USER: Can view assigned tasks and submit GitHub links for review.

### Inter-Service Communication: Uses OpenFeign for seamless, declarative REST client communication between services.

### Modern Frontend: A responsive, user-friendly interface built with React.js and Vite, deployed on Vercel.

## 🏗️ System Architecture

The backend follows a distributed microservices pattern, where each service has a specific responsibility.

graph TD
    Client[React Frontend (Vercel)] --> Gateway[API Gateway (Render)]
    Gateway --> Auth[User Service]
    Gateway --> Task[Task Service]
    Gateway --> Sub[Submission Service]
    
    Auth -.-> Eureka[Eureka Server]
    Task -.-> Eureka
    Sub -.-> Eureka
    Gateway -.-> Eureka
    
    Task -->|Feign Client| Auth
    Sub -->|Feign Client| Task
    Sub -->|Feign Client| Auth

    Auth --> DB[(MongoDB Atlas)]
    Task --> DB
    Sub --> DB


Eureka Server (8085): The service registry. All microservices register here to discover each other dynamically.

API Gateway (8090): The front door. It handles routing, CORS configurations, and load balancing.

User Service (8081): Manages registration, login, and JWT generation.

Task Service (8082): Handles task CRUD operations. It communicates with the User Service to validate roles.

Submission Service (8083): Manages task submissions. It validates task existence via the Task Service before accepting submissions.

## ⚡ Load Testing & Performance

This system has been rigorously tested using Apache JMeter to ensure stability under load.

Scenario: Simulated 100+ concurrent users performing registration and login flows.

Tests Conducted:

Authentication under heavy load.

Task creation and retrieval latency.

Role validation throughput.

Gateway routing performance.

Inter-service Feign communication reliability.

### Results:

✅ All services remained stable.

✅ Token validation passed under load.

✅ 80% Success Rate during peak concurrency stress tests.

✅ Architecture scalability verified.

## 🛠️ Tech Stack

### Backend

Language: Java 17

Framework: Spring Boot 2.7.18

Cloud Components: Spring Cloud Gateway, Netflix Eureka, OpenFeign

Database: MongoDB Atlas (Cloud Database)

Security: Spring Security, JWT, BCrypt

Build Tool: Maven

### Frontend

Library: React.js 18

Build Tool: Vite

Styling: CSS3 (Custom responsive design)

Deployment: Vercel

## 💻 Local Development Setup

If you want to run the project locally instead of using the live link:

Prerequisites

Java JDK 17

Node.js (v16+) & npm

MongoDB (Localhost:27017)

### 1. Clone the Repository

git clone [https://github.com/your-username/TaskFlow.git](https://github.com/your-username/TaskFlow.git)
cd TaskFlow


### 2. Start Backend Services

Run the services in this specific order to avoid connection errors:

Eureka Server (mvn spring-boot:run) → Port 8085

User Service (mvn spring-boot:run) → Port 8081

Task Service (mvn spring-boot:run) → Port 8082

Submission Service (mvn spring-boot:run) → Port 8083

API Gateway (mvn spring-boot:run) → Port 8090

### 3. Start Frontend

cd taskflow-frontend
npm install
npm run dev


Accessible at: http://localhost:5173

## 🔌 API Endpoints

All requests should be routed through the API Gateway (Port 8090).

POST /auth/signup - Register a new user.

POST /auth/signin - Login and receive JWT.

POST /api/tasks - Create a new task (Admin).

POST /api/submissions - Submit work (User).

## 🤝 Contribution

Contributions are welcome! Please fork the repository and create a pull request with your features or fixes.

## 📄 License

This project is licensed under the MIT License.


### **How to Use:**
1.  Create a file named `README.md` in the root of your GitHub repository.
2.  Paste the code above into it.
3.  Commit and push to GitHub.

This README is now **portfolio-ready**. It highlights your cloud deployment, architecture design, and load-testing efforts, which are huge plus points for recruiters! 🚀

## 👨‍💻Author

Aman Manwatkar
