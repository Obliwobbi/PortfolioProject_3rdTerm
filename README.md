# MemberSystem API

## Vision

This project is a backend API for managing companies, users, and memberships.  
The system allows organizations to manage members, roles, and membership states in a structured and scalable way.

---

## Links

Portfolio website:  
https://portfolio.obli.dk/

Project overview video:  
https://portfolio.obli.dk/posts/video/

Deployed application:  
https://membersystem.obli.dk/api/v1/

Source code repository:  
https://github.com/Obliwobbi/PortfolioProject_3rdTerm

---

# Architecture

## System Overview

This project is built as a layered backend architecture using:

- Controller layer (REST endpoints using Javalin)
- Service layer (business logic and validation)
- DAO layer (database access using JPA/Hibernate)

Technologies used:

- Java
- Javalin
- JPA / Hibernate
- PostgreSQL
- Maven
- JWT authentication
- Docker & Docker Compose
- GitHub Actions (CI/CD)
- Watchtower (auto deployment)

The application is deployed on a Digital Ocean droplet and exposed through a domain using Caddy as a reverse proxy.

---

## Architecture Diagram

Client > REST API (Javalin) > Service Layer > DAO Layer > PostgreSQL Database

---

## Key Design Decisions

### Layered Architecture

The system is structured into clear layers:
- Controllers handle HTTP requests
- Services contain business logic
- DAOs handle persistence

This separation makes the system easier to maintain and extend.

---

### Authentication

Authentication is implemented using JWT tokens.

Users log in via an endpoint and receive a token, which must be included in protected requests.

Passwords are securely hashed using a password service before being stored in the database.

---

### DTO Usage

DTOs are used to separate internal entities from API responses.

This prevents:
- exposing sensitive data
- tight coupling between database and API
- issues with lazy loading

---

### Environment-based configuration

The system supports different environments:
- local development
- test
- production (Docker)

Sensitive values such as database credentials and secrets are handled using environment variables.

---

### CI/CD Pipeline

A CI/CD pipeline is implemented using GitHub Actions:

1. Build and test the application
2. Create a Docker image
3. Push the image to Docker Hub

Watchtower is used on the server to automatically pull and deploy new versions.

---

# Data Model

## ERD

The system is based on a relational data model with the following core entities:

- Company
- User
- Membership
- MembershipType
- Location
- CheckIn

(Will insert ERD here later, but have to figure out again how to put into md)

---

## Important Entities

### User

Represents a user in the system.

Fields:
- id
- email
- firstname
- lastname
- passwordHash
- role
- company

---

### Company

Represents an organization.

Fields:
- id
- name

A company can have multiple users and locations.

---

# API Documentation

## Base URL

```

/api/v1

````

---

## Example Endpoints

### Login

POST /api/v1/login

Request body:

```json
{
  "email": "admin@obli.dk",
  "password": "password"
}
````

Response:

```json
{
  "token": "jwt-token"
}
```

---

### Get All Companies

GET /api/v1/companies

Response:

```json
[
  {
    "id": 1,
    "name": "Example Company"
  }
]
```

---

### Create User

POST /api/v1/users

Request body:

```json
{
  "companyId": 44,
  "email": "user@test.dk",
  "firstname": "John",
  "lastname": "Doe",
  "dob": "yyyy-mm-dd",
  "role": "<ROLE ENUM>",
  "password": "password"
}
```

Response:

201 Created

---

# User Stories

* As a user, I want to register an account.
* As a user, I want to log in and receive a token.
* As an admin, I want to manage users within a company.
* As a system, I want to track membership history.
* As a user, I want to belong to a company.
* As an admin, I want to control user roles.

(not all implemented as of this moment)

---

# Development Notes

This project involved several important learning experiences:

* Designing a domain-driven data model using JPA
* Understanding REST principles and HTTP structure
* Implementing authentication with JWT
* Handling password hashing securely
* Structuring a layered backend architecture
* Debugging deployment issues in Docker environments
* Setting up CI/CD pipelines with GitHub Actions
* Managing environment variables across local and production setups

One of the biggest challenges was moving from a local development environment to a fully deployed system, where configuration, networking, and containerization all play a role.

This project represents a complete backend system from design to deployment.
