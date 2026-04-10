# Payment Orchestration Service

A robust Spring Boot-based payment orchestration service that provides intelligent routing, retry mechanisms, and idempotency handling for payment processing across multiple providers.

## 🚀 Features

- **Smart Payment Routing**: Automatic routing based on payment method (CARD → Provider A, UPI → Provider B)
- **Retry & Failover**: Resilience4j-powered retry mechanism with configurable attempts
- **Idempotency**: In-memory TTL-based idempotency cache to prevent duplicate processing
- **Payment Status Tracking**: Complete lifecycle tracking with database persistence
- **RESTful APIs**: Clean REST endpoints for payment creation and retrieval
- **H2 Database**: Embedded database for development and testing

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Controller    │────│   Service Layer  │────│   Repository    │
│                 │    │                  │    │                 │
│ • REST APIs     │    │ • Orchestration  │    │ • JPA Entities  │
│ • Validation    │    │ • Idempotency    │    │ • H2 Database   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────────┐
                    │   Routing Engine    │
                    │                     │
                    │ • Provider Selection│
                    │ • Load Balancing    │
                    └─────────────────────┘
```

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

## 🎯 Routing Logic

The service automatically routes payments based on the payment method:

| Payment Method | Provider | Success Rate | Notes |
|---------------|----------|--------------|-------|
| **CARD** | Provider A | 70% | Credit/Debit cards |
| **UPI** | Provider B | 80% | Unified Payments Interface |

You can override the automatic routing by specifying `preferredProvider` in the request.

## 🔄 Retry & Failover

- **Retry Attempts**: 3 attempts per payment
- **Retry Delay**: 2 seconds between attempts
- **Failure Handling**: Automatic failover to retry logic
- **Idempotency**: Prevents duplicate processing during retries

## 🔐 Idempotency

- **TTL**: 60 minutes (configurable via `app.idempotency.ttl-minutes`)
- **Storage**: In-memory cache (ConcurrentHashMap)
- **Behavior**: Same idempotency key within TTL window returns existing result

## 🗄️ Database

### H2 Console Access
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:paymentdb`
- **Username**: `sa`
- **Password**: `password`

### Schema
```sql
CREATE TABLE payments (
    id VARCHAR(255) PRIMARY KEY,
    amount DECIMAL(38,2),
    currency VARCHAR(255),
    payment_method VARCHAR(255),
    provider VARCHAR(255),
    idempotency_key VARCHAR(255) UNIQUE,
    status VARCHAR(255),
    error_message VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### API Testing Examples

#### Test CARD Payment (routes to Provider A)
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "USD",
    "paymentMethod": "CARD",
    "idempotencyKey": "test-card-1"
  }'
```

#### Test UPI Payment (routes to Provider B)
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50.00,
    "currency": "INR",
    "paymentMethod": "UPI",
    "idempotencyKey": "test-upi-1"
  }'
```

#### Test Payment Retrieval
```bash
curl http://localhost:8080/api/payments/{payment-id}
```

## ⚙️ Configuration

### Application Properties
```yaml
# Server Configuration
server:
  port: 8080

# Database Configuration
spring:
  datasource:
    url: jdbc:h2:mem:paymentdb
    driverClassName: org.h2.Driver
    username: sa
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  h2:
    console:
      enabled: true

# Idempotency Configuration
app:
  idempotency:
    ttl-minutes: 60

# Resilience4j Configuration
resilience4j:
  retry:
    instances:
      paymentRetry:
        maxAttempts: 3
        waitDuration: 2s
        retryExceptions:
          - java.lang.RuntimeException
          - java.io.IOException
```

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Application Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

## 🐛 Troubleshooting

### Common Issues

1. **Port 8080 already in use**
   ```bash
   # Find process using port 8080
   lsof -i :8080
   # Kill the process
   kill -9 <PID>
   ```

2. **Java version issues**
   ```bash
   # Check Java version
   java -version
   # Set JAVA_HOME if needed
   export JAVA_HOME=/path/to/java17
   ```

3. **Maven build failures**
   ```bash
   # Clean and rebuild
   mvn clean install
   ```

### Logs
Application logs are available in the console output. For detailed logging, check:
- `logs/application.log` (if configured)
- Console output when running with `mvn spring-boot:run`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- Create an issue in the GitHub repository
- Check the troubleshooting section above
- Review the API documentation

---

**Happy coding! 🚀**