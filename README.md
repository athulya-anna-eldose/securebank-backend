# SecureBank – Intelligent Cyber Threat Detection & API Security Platform

## Project Overview

SecureBank is a simulated online banking application developed to explore cybersecurity concepts and secure API development.

The project combines basic banking operations with security mechanisms to identify suspicious activities, monitor potential threats, and protect backend APIs from unauthorized access.

This project was developed for educational purposes as a cybersecurity portfolio project.

## Technologies Used

### Backend
- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT Authentication
- PostgreSQL
- Maven

### Frontend
- React
- TypeScript
- Vite
- Axios
- Recharts

## Key Features

### Banking Functionality
- User registration and login
- JWT-based authentication
- Account dashboard
- View transaction history
- Money transfers
- Demo deposits
- Profile management

### Cybersecurity Features
- **JWT Authentication:** Protects APIs using signed JSON Web Tokens.
- **API Rate Limiting:** Limits excessive API requests.
- **Brute-Force Detection:** Detects repeated failed login attempts and temporarily blocks suspicious IP addresses.
- **SQL Injection Detection:** Identifies suspicious SQL injection patterns in incoming requests.
- **XSS Detection:** Identifies potentially malicious cross-site scripting payloads.
- **Security Event Monitoring:** Records and displays suspicious security events.
- **Security Headers:** Implements HTTP security headers to improve API security.
- **Input Validation:** Validates user input to reduce malicious or invalid data.

## Security Testing

The following security scenarios were tested:

| Test | Expected Result |
|---|---|
| Login with valid credentials | Successful authentication |
| Access protected API without token | 401 Unauthorized |
| Access protected API with invalid JWT | 401 Unauthorized |
| Logout | JWT removed from browser Local Storage |
| Excessive API requests | 429 Too Many Requests |
| Repeated failed login attempts | Brute-force detection |
| SQL injection test payload | Suspicious activity detected |
| XSS test payload | Suspicious activity detected |
| Invalid profile input | Validation error |

**Note:** SQL injection and XSS detection mechanisms in this project are designed for monitoring and detection. Detection does not necessarily mean every malicious request is blocked.

## Running the Project Locally

### Backend

1. Open the `securebank-backend` folder.
2. Configure the required environment variables:
    - `DB_URL`
    - `DB_USERNAME`
    - `DB_PASSWORD`
    - `JWT_SECRET`
3. Run the Spring Boot application.

Backend URL: `http://localhost:8080`

### Frontend

1. Open the `securebank-frontend` folder.
2. Install dependencies:

```bash
npm install
```

3. Start the development server:

```bash
npm run dev
```

Frontend URL: `http://localhost:5173`

## Project Structure

### Backend
- `controller` – REST API endpoints
- `service` – Business logic
- `repository` – Database operations
- `entity` – Database entities
- `dto` – Request and response objects
- `security` – JWT authentication and security filters
- `config` – Application and security configuration

### Frontend
- `components` – Reusable UI components
- `pages` – Application pages
- `services` – API communication
- `assets` – Static assets

## Learning Outcomes

- Implementing JWT-based authentication and authorization
- Developing secure REST APIs using Spring Boot
- Understanding API rate limiting and brute-force detection
- Identifying suspicious SQL injection and XSS patterns
- Implementing input validation and HTTP security headers
- Monitoring and analysing security events

## Disclaimer

SecureBank is an educational simulation. It does not process real money or connect to actual banking systems. Its security mechanisms are intended for learning and demonstration, not production banking use.