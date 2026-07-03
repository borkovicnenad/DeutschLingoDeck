# DeutschLingoDeck

An AI-powered language learning platform for building and mastering German vocabulary through personalized flashcards, interactive game modes, and intelligent learning assistance.

DeutschLingoDeck is being developed as a production-quality full-stack application that combines modern web technologies, clean software architecture, and artificial intelligence to create an engaging language learning experience. Besides being a practical learning tool, the project also serves as a portfolio application demonstrating enterprise-level software engineering practices.

---

# Project Status

**Current Status:** In Development

The project is currently in its initial development phase. The repository contains the project structure and the technical foundation upon which all future features will be built.

---

# Vision

Learning vocabulary should be interactive, personalized, and enjoyable.

DeutschLingoDeck allows users to build their own vocabulary collections, import words from external sources, and practice them through multiple learning modes powered by AI-assisted features.

The long-term goal is to provide a flexible learning platform that adapts to each user's learning style while maintaining a clean, scalable, and maintainable software architecture.

---

# Planned Features

## User Management

* Secure user authentication
* Personal user profiles
* Individual learning statistics
* User preferences

## Vocabulary Management

* Create custom vocabulary decks
* Import vocabulary from Excel files
* Organize words into categories
* Edit and manage vocabulary collections

## Learning Modes

* Flashcards
* Quiz mode
* Writing exercises
* Multiple-choice challenges
* Timed game modes

## AI Integration

* AI-assisted vocabulary import
* Automatic translation suggestions
* Example sentence generation
* Difficulty estimation
* Pronunciation assistance
* Smart learning recommendations

## Statistics

* Learning progress
* Daily activity
* Accuracy tracking
* Vocabulary mastery
* Personal achievements

---

# Technology Stack

## Frontend

* Angular 22
* TypeScript
* Angular Signals
* Angular Material
* RxJS

## Backend

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* Flyway

## Database

* PostgreSQL

## Infrastructure

* Docker
* Docker Compose
* GitHub Actions

---

# High-Level Architecture

```text
                 +----------------------+
                 |     Angular SPA      |
                 +----------+-----------+
                            |
                       REST API
                            |
                 +----------v-----------+
                 |    Spring Boot API   |
                 +----------+-----------+
                            |
                     Spring Data JPA
                            |
                 +----------v-----------+
                 |      PostgreSQL      |
                 +----------------------+
```

---

# Repository Structure

```text
backend/        Spring Boot backend
frontend/       Angular frontend
database/       Database scripts and seed data
docker/         Docker configuration
docs/           Project documentation
scripts/        Utility scripts
.github/        GitHub workflows and templates
```

---

# Development Principles

The project follows several core engineering principles:

* Clean Architecture
* Separation of Concerns
* SOLID principles
* RESTful API design
* Layered Architecture
* Secure-by-default development
* Testability
* Scalability
* Maintainability

---

# Roadmap

* Project setup
* Authentication & authorization
* Vocabulary management
* Learning engine
* Game modes
* AI-assisted features
* Statistics dashboard
* Docker deployment
* Continuous Integration
* Cloud deployment

---

# Running the Project

## Backend

```bash
cd backend
./gradlew bootRun
```

## Frontend

```bash
cd frontend
npm install
npm start
```

---

# Documentation

Additional technical documentation is available in the `docs` directory.

---

# Author

Developed by Nenad Borković as a full-stack portfolio project focused on modern software engineering, enterprise architecture, and AI integration.
