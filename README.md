# PayDayLoan Backend System

Enterprise-grade PayDayLoan application built with Spring Boot 3.2 and Oracle Database.

## Architecture Overview

### Database Schema (15 Tables)

**Master Tables:**
- `PDL_CORPORATE` - Corporate master data
- `PDL_CORPORATE_USER` - Corporate admin/users
- `PDL_EMPLOYEE` - Employee master records
- `PDL_EMPLOYEE_SALARY` - Employee salary definitions
- `PDL_PRODUCT_CONFIG` - Loan product configuration

**Transaction Tables:**
- `PDL_LOAN_REQUEST` - Loan request submissions
- `PDL_REQUEST_APPROVAL` - Multi-level approval workflow
- `PDL_LOAN_ACCOUNT` - Active loan accounts
- `PDL_DISBURSEMENT_TXN` - Disbursement transactions
- `PDL_LOAN_CHARGE` - Charges ledger
- `PDL_REPAYMENT_SCHEDULE` - Repayment installment schedule
- `PDL_REPAYMENT_TXN` - Repayment transactions

**Support Tables:**
- `PDL_EMPLOYEE_LIMIT` - Employee limit snapshots
- `PDL_NOTIFICATION_LOG` - Notification audit log
- `PDL_AUDIT_LOG` - Complete audit trail

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker (for Oracle Database)
- Git

## Setup Instructions

### 1. Clone/Extract Project
```bash
cd /Users/foysalislam/IdeaProjects/PayDayLoan
```

### 2. Setup Oracle Database with Docker

#### Option A: Using Docker Compose (Recommended)
```bash
# Start Oracle database
docker-compose up -d

# Wait for Oracle to be ready (check logs)
docker-compose logs -f oracle

# Once ready, the database will be available at:
# Host: localhost
# Port: 1521
# SID: XE
# Username: system
# Password: oracle
```

#### Option B: Manual Oracle Installation
If you have Oracle installed locally, just ensure it's running on `localhost:1521`

### 3. Configure Database Connection

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XE
spring.datasource.username=system
spring.datasource.password=oracle
```

### 4. Build the Project

```bash
# Clean and build
mvn clean install

# Or just build without tests
mvn clean install -DskipTests
```

### 5. Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Or run as JAR
java -jar target/paydayloan-backend-1.0.0.jar
```

The application will start on `http://localhost:8080/api`

## API Endpoints

### Loan Request Management

#### Create Loan Request
```
POST /api/loans/request
Content-Type: application/json

{
  "employeeId": 1,
  "customerId": 123,
  "corporateId": 1,
  "productConfigId": 1,
  "requestedAmount": 50000.00,
  "repaymentDate": "2026-05-24",
  "purpose": "Personal expense",
  "requestChannel": "MOBILE_APP"
}

Response: 201 Created
{
  "success": true,
  "message": "Loan request created successfully",
  "data": {
    "requestId": 1,
    "requestRefNo": "REQ-2026-ABC123XY",
    "requestStatus": "PENDING_CORP_APPROVAL",
    "eligibleAmount": 80000.00,
    "serviceChargeAmount": 1000.00,
    "netDisburseAmount": 49000.00
  }
}
```

#### Get Loan Request Details
```
GET /api/loans/request/{requestId}

Response: 200 OK
{
  "success": true,
  "data": {
    "requestId": 1,
    "requestRefNo": "REQ-2026-ABC123XY",
    "employeeId": 1,
    "customerId": 123,
    "corporateId": 1,
    "requestedAmount": 50000.00,
    "requestStatus": "PENDING_CORP_APPROVAL",
    ...
  }
}
```

#### Approve Loan Request (Corporate)
```
PUT /api/loans/request/{requestId}/approve?remarks=Approved
Headers:
  X-User-Id: CORP-USER-123
  X-User-Name: John Manager

Response: 200 OK
{
  "success": true,
  "message": "Loan request approved successfully"
}
```

#### Disburse Loan
```
POST /api/loans/{requestId}/disburse
Headers:
  X-User-Id: BANK-USER-456

Response: 201 Created
{
  "success": true,
  "message": "Loan disbursed successfully"
}
```

#### Get Pending Loans (Corporate)
```
GET /api/loans/corporate/{corporateId}/pending

Response: 200 OK
{
  "success": true,
  "count": 3,
  "data": [
    {
      "requestId": 1,
      "requestRefNo": "REQ-2026-ABC123XY",
      "employeeId": 1,
      "requestedAmount": 50000.00,
      "requestStatus": "PENDING_CORP_APPROVAL"
    },
    ...
  ]
}
```

#### Get Active Loans (Employee)
```
GET /api/loans/employee/{employeeId}/active

Response: 200 OK
{
  "success": true,
  "count": 1,
  "data": [
    {
      "loanId": 1,
      "loanRefNo": "LOAN-2026-XYZ987AB",
      "sanctionedAmount": 50000.00,
      "outstandingAmount": 51000.00,
      "loanStatus": "ACTIVE",
      "maturityDate": "2026-05-24"
    }
  ]
}
```

## Database Schema

### PDL_LOAN_REQUEST Status Flow
- `DRAFT` - Initial state
- `PENDING_CORP_APPROVAL` - Awaiting corporate approval
- `APPROVED_BY_CORP` - Approved by corporate
- `REJECTED_BY_CORP` - Rejected by corporate
- `PENDING_BANK_REVIEW` - Bank review
- `APPROVED_BY_BANK` - Bank approved
- `REJECTED_BY_BANK` - Bank rejected
- `DISBURSED` - Funds disbursed
- `CANCELLED` - Request cancelled
- `REPAID` - Fully repaid
- `OVERDUE` - Payment overdue

### PDL_LOAN_ACCOUNT Status
- `ACTIVE` - Active loan
- `REPAID` - Fully repaid
- `OVERDUE` - Payment overdue
- `CLOSED` - Closed/Settled
- `WRITTEN_OFF` - Written off

## Business Logic

### Service Charge Calculation
```
Service Charge = GREATEST(Requested Amount * Service Charge %, Minimum Service Charge)
Example: 50000 * 2% = 1000 (if >= 200)
```

### Eligible Amount Calculation
```
Eligible Amount = Monthly Salary * Max Eligible %
Example: 100000 * 80% = 80000
```

### Constraint Validations
1. Requested Amount <= Eligible Amount
2. Only one active loan per employee (configurable)
3. Corporate agreement must be ACTIVE
4. Employee must be ACTIVE and ELIGIBLE
5. Product must be ACTIVE and within date range

## Key Services

### LoanService
Core business logic for:
- Creating loan requests
- Validating eligibility
- Calculating service charges
- Managing approvals
- Creating loan accounts
- Tracking repayments

## Security

- CORS configured for all API endpoints
- Can integrate JWT authentication
- Audit logging for all transactions
- Request/Response logging enabled
- SQL injection prevention via JPA parameterized queries

## Architecture Patterns

- **Repository Pattern** - Data access layer
- **Service Layer** - Business logic encapsulation
- **DTO Pattern** - Request/Response mapping
- **Transaction Management** - ACID compliance
- **Audit Trail** - Complete activity logging
- **Error Handling** - Comprehensive exception handling

## Database Triggers

Auto-timestamp triggers on:
- PDL_CORPORATE
- PDL_CORPORATE_USER
- PDL_EMPLOYEE
- PDL_EMPLOYEE_SALARY
- PDL_PRODUCT_CONFIG
- PDL_LOAN_REQUEST
- PDL_LOAN_ACCOUNT

## Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Deployment

### Production Configuration
```properties
spring.jpa.hibernate.ddl-auto=validate  # Never auto-create in production
spring.jpa.show-sql=false  # Disable SQL logging in production
logging.level.root=WARN  # Reduce log verbosity
```

### Docker Build
```bash
mvn clean package -DskipTests
docker build -t paydayloan-backend:1.0 .
```

## Troubleshooting

### Oracle Connection Issues
```bash
# Check if Oracle is running
docker ps | grep oracle

# View Oracle logs
docker-compose logs oracle

# Restart Oracle
docker-compose restart oracle
```

### Application Won't Start
```bash
# Check if port 8080 is in use
lsof -i :8080

# Clear Maven cache
mvn clean

# Rebuild
mvn clean install -DskipTests
```

## Future Enhancements

1. **Repayment Module**
   - Auto-debit processing
   - Penalty calculation
   - Collection reporting

2. **Approval Workflow**
   - Multi-level approvals
   - Rule-based routing
   - Notification integration

3. **Reporting**
   - MIS reports
   - Business analytics
   - Dashboard views

4. **Integration**
   - CBS integration
   - Third-party lending platforms
   - Notification gateways (SMS, Email, Push)

5. **Security**
   - JWT/OAuth2 authentication
   - Role-based access control (RBAC)
   - API rate limiting

## Support

For issues or questions, please contact the development team.

## License

Copyright © 2026 PayDayLoan. All rights reserved.
