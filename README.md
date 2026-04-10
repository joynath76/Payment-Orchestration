# Payment Orchestration Service

A robust Spring Boot-based payment orchestration service that provides intelligent routing, retry mechanisms, and idempotency handling for payment processing across multiple providers.

## 🚀 Features

- **Smart Payment Routing**: Automatic routing based on payment method (CARD → Provider A, UPI → Provider B)
- **Retry & Failover**: Resilience4j-powered retry mechanism with configurable attempts
- **Idempotency**: In-memory TTL-based idempotency cache to prevent duplicate processing
- **Payment Status Tracking**: Complete lifecycle tracking with database persistence
- **RESTful APIs**: Clean REST endpoints for payment creation and retrieval
- **H2 Database**: Embedded database for development and testing

## 📋 Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6+ (or use Maven Wrapper)
- **Git**: For version control

## 🔧 Installation

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/payment-orchestration.git
cd payment-orchestration
```

### 2. Install Java 17 (if not already installed)
```bash
# On Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# On macOS (using Homebrew)
brew install openjdk@17

# On Windows
# Download from: https://adoptium.net/temurin/releases/
```

### 3. Verify Java Installation
```bash
java -version
# Should show: Java 17.x.x
```

## 🚀 Execution Guide

### Method 1: Using Maven Wrapper (Recommended)

```bash
# Build and run the application
./mvnw spring-boot:run
```

### Method 2: Using System Maven

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

### Method 3: Using IDE

1. Open the project in your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Navigate to `PaymentOrchestrationApplication.java`
3. Run the main method

## 📡 API Endpoints

### Base URL
```
http://localhost:8080
```

### Create Payment
**POST** `/api/payments`

Creates a new payment request with intelligent routing.

**Request Body:**
```json
{
  "amount": 100.00,
  "currency": "USD",
  "paymentMethod": "CARD",
  "idempotencyKey": "unique-payment-123",
  "preferredProvider": "PROVIDER_A"  // optional
}
```

**Parameters:**
- `amount` (required): Payment amount (minimum: 0.01)
- `currency` (required): Currency code (e.g., USD, EUR, INR)
- `paymentMethod` (required): "CARD" or "UPI"
- `idempotencyKey` (required): Unique identifier for the payment
- `preferredProvider` (optional): Override automatic routing

**Response (Success - 200):**
```json
{
  "paymentId": "uuid-generated",
  "amount": 100.00,
  "currency": "USD",
  "paymentMethod": "CARD",
  "provider": "PROVIDER_A",
  "status": "SUCCESS",
  "timestamp": "2026-04-10T10:30:00"
}
```

**Response (Failure - 422):**
```json
{
  "paymentId": "uuid-generated",
  "amount": 100.00,
  "currency": "USD",
  "paymentMethod": "CARD",
  "provider": "PROVIDER_A",
  "status": "FAILED",
  "errorMessage": "ProviderA Service Unavailable",
  "timestamp": "2026-04-10T10:30:00"
}
```

### Fetch Payment
**GET** `/api/payments/{paymentId}`

Retrieves payment details by payment ID.

**Response (Success - 200):**
```json
{
  "paymentId": "uuid-generated",
  "amount": 100.00,
  "currency": "USD",
  "paymentMethod": "CARD",
  "provider": "PROVIDER_A",
  "status": "SUCCESS",
  "timestamp": "2026-04-10T10:30:00"
}
```

**Response (Not Found - 404):**
```json
{
  "timestamp": "2026-04-10T10:30:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/payments/non-existent-id"
}
```

**Happy coding! 🚀**
