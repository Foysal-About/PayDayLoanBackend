# PayDayLoan Backend - Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Step 1: Start Oracle Database

```bash
# Make sure Docker is running, then:
cd /Users/foysalislam/IdeaProjects/PayDayLoan
docker-compose up -d

# Wait ~30 seconds for Oracle to initialize
docker-compose logs oracle
```

### Step 2: Build the Application

```bash
mvn clean install -DskipTests
```

### Step 3: Run the Application

```bash
mvn spring-boot:run
```

The application will start at: **http://localhost:8080/api**

---

## 📋 Database Access

**Oracle SQL Developer / SQL*Plus Connection:**
```
Host: localhost
Port: 1521
SID: XE
Username: system
Password: oracle
```

**Test SQL Query:**
```sql
SELECT * FROM PDL_CORPORATE;
SELECT * FROM PDL_EMPLOYEE;
SELECT * FROM PDL_LOAN_REQUEST;
```

---

## 🔌 API Quick Reference

### Health Check
```bash
curl -X GET http://localhost:8080/api/loans/health
```

### 1. Create Loan Request

```bash
curl -X POST http://localhost:8080/api/loans/request \
  -H "Content-Type: application/json" \
  -H "X-User-Id: EMP-001" \
  -d '{
    "employeeId": 1,
    "customerId": 123,
    "corporateId": 1,
    "productConfigId": 1,
    "requestedAmount": 50000,
    "repaymentDate": "2026-05-24",
    "purpose": "Home renovation",
    "requestChannel": "MOBILE_APP"
  }'
```

**Success Response:**
```json
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

### 2. Get Loan Request Status

```bash
curl -X GET http://localhost:8080/api/loans/request/1
```

### 3. Approve Loan (Corporate)

```bash
curl -X PUT "http://localhost:8080/api/loans/request/1/approve?remarks=Approved%20by%20HR" \
  -H "X-User-Id: CORP-MGR-001" \
  -H "X-User-Name: John Manager"
```

### 4. Disburse Loan (Bank)

```bash
curl -X POST http://localhost:8080/api/loans/1/disburse \
  -H "X-User-Id: BANK-OFFICER-001"
```

### 5. Get Pending Loans for Corporate

```bash
curl -X GET http://localhost:8080/api/loans/corporate/1/pending
```

**Response:**
```json
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
    }
  ]
}
```

### 6. Get Active Loans for Employee

```bash
curl -X GET http://localhost:8080/api/loans/employee/1/active
```

---

## 📊 Sample Data Setup

To test the API, you need to insert sample data:

```sql
-- Insert Corporate
INSERT INTO PDL_CORPORATE (CORPORATE_CODE, CORPORATE_NAME, STATUS, CREATED_BY)
VALUES ('CORP001', 'Acme Corporation', 'ACTIVE', 'ADMIN');
COMMIT;

-- Insert Employee
INSERT INTO PDL_EMPLOYEE (CORPORATE_ID, EMPLOYEE_CODE, EMPLOYEE_NAME, CUSTOMER_ID, MOBILE_NO, SALARY_ACCOUNT_NO, ELIGIBILITY_YN, STATUS, CREATED_BY)
VALUES (1, 'EMP001', 'John Doe', 123, '8801712345678', 'BANKACC001', 1, 'ACTIVE', 'ADMIN');
COMMIT;

-- Insert Salary
INSERT INTO PDL_EMPLOYEE_SALARY (EMPLOYEE_ID, CORPORATE_ID, MONTHLY_SALARY, ELIGIBLE_PERCENT, MAX_ELIGIBLE_AMOUNT, EFFECTIVE_FROM, IS_CURRENT_YN, APPROVAL_STATUS, CREATED_BY)
VALUES (1, 1, 100000, 80, 80000, SYSDATE, 1, 'APPROVED', 'ADMIN');
COMMIT;

-- Insert Product Config
INSERT INTO PDL_PRODUCT_CONFIG (PRODUCT_CODE, PRODUCT_NAME, STATUS, EFFECTIVE_FROM, CREATED_BY)
VALUES ('PDLPRO001', 'Standard PayDay Loan', 'ACTIVE', SYSDATE, 'ADMIN');
COMMIT;
```

---

## 🔍 Key Endpoints Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/loans/request` | Create new loan request |
| GET | `/loans/request/{id}` | Get loan request details |
| PUT | `/loans/request/{id}/approve` | Corporate approval |
| POST | `/loans/{id}/disburse` | Disburse approved loan |
| GET | `/loans/corporate/{id}/pending` | Get pending loans |
| GET | `/loans/employee/{id}/active` | Get employee's active loans |

---

## 🛠️ Troubleshooting

### Oracle not connecting?
```bash
# Check if container is running
docker ps | grep oracle

# View logs
docker-compose logs oracle

# Restart
docker-compose restart oracle
```

### Application won't start?
```bash
# Check if port 8080 is in use
lsof -i :8080

# Clean and rebuild
mvn clean install -DskipTests
```

### Database table not found?
```bash
# Verify tables were created in Oracle
SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE 'PDL%';
```

---

## 📱 Testing with Postman

1. **Import Collection:** Download from `/postman_collection.json` (to be created)
2. **Set Variables:**
   - `base_url`: http://localhost:8080/api
   - `user_id`: EMP-001
   - `corporate_id`: 1

---

## 🎯 Loan Status Workflow

```
┌─────────────┐
│   DRAFT     │
└──────┬──────┘
       │
       ▼
┌──────────────────────┐
│ PENDING_CORP_APPROVAL│ ◄── Corporate Review
└──────┬───────────┬──┘
       │           │ (Reject)
       │ (Approve) │
       │           ▼
       │    ┌──────────────┐
       │    │REJECTED_BY_  │
       │    │ CORP         │
       │    └──────────────┘
       │
       ▼
┌──────────────────┐
│ PENDING_BANK_    │ ◄── Bank Review
│ REVIEW           │
└────┬────────┬───┘
     │        │ (Reject)
     │(App)  │
     │        ▼
     │    ┌──────────────┐
     │    │REJECTED_BY_  │
     │    │ BANK         │
     │    └──────────────┘
     │
     ▼
┌──────────────────┐
│APPROVED_BY_BANK  │
└────────┬─────────┘
         │ (Disburse)
         ▼
     ┌────────┐
     │DISBURSED│
     └────────┘
         │
         ▼
     ┌────────┐
     │REPAID  │ (After repayment)
     └────────┘
```

---

## 📈 Performance Tips

1. **Database Indexing:** All primary searches are indexed
2. **Connection Pooling:** Configured with HikariCP (10 max, 5 min)
3. **Batch Processing:** SQL batch size set to 10
4. **Read-Only Transactions:** Used for safe query methods

---

## 🔐 Security Considerations

- [ ] Enable JWT authentication
- [ ] Implement rate limiting
- [ ] Configure HTTPS/TLS
- [ ] Add input validation decorators
- [ ] Enable request signing
- [ ] Implement OTP verification for disbursement

---

## 📚 Additional Resources

- **Database Schema:** See `/oracle_schema.sql`
- **Full Documentation:** See `/README.md`
- **Entity Relationships:** JPA annotations document all ForeignKeys
- **API Contracts:** DTOs in `/src/main/java/com/paydayloan/dto/`

---

## ✅ Verification Checklist

- [ ] Docker is running
- [ ] Oracle container started (`docker ps`)
- [ ] Application started on port 8080
- [ ] Can connect to Oracle with SQL Developer
- [ ] Can make API calls with curl/Postman
- [ ] Sample data inserted

---

**Next Steps:**
1. Insert sample data (see above)
2. Test loan creation endpoint
3. Review audit logs in database
4. Setup corporate approval workflow
5. Configure notification gateways
