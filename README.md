# 🏦 Banking System — Microservices

## Architecture Overview
```
Frontend (React/Angular)
        │
        ▼
[API Gateway :8080]  ←→  [Eureka Server :8761]
        │
   ┌────┼────────────┬──────────────┐
   ▼    ▼            ▼              ▼
[auth  :8081] [transaction :8082] [payment :8083] [admin :8084]
   │              │                  │               │
[auth-db]   [transaction-db]   [payment-db]    [admin-db]
(PostgreSQL)   (PostgreSQL)     (PostgreSQL)   (PostgreSQL)
```

## Prerequisites (install on your laptop/lab)

| Tool       | Version  | Download |
|------------|----------|----------|
| Docker     | 20+      | https://docs.docker.com/get-docker/ |
| Docker Compose | 2+   | included with Docker Desktop |
| Java       | 17       | https://adoptium.net/ (only if running without Docker) |
| Maven      | 3.9+     | https://maven.apache.org/ (only if running without Docker) |

---

## 🚀 Run with Docker (RECOMMENDED — works on any lab PC)

### Step 1 — Extract the project
```bash
unzip banking-system.zip
cd banking-system
```

### Step 2 — Build and start everything
```bash
docker compose up --build
```
> ⏳ First run downloads images and builds JARs — takes ~5 minutes.  
> After that, subsequent starts are much faster.

### Step 3 — Verify everything is running
Open your browser and go to:
- **Eureka Dashboard** → http://localhost:8761  
  You should see all 4 services registered: `AUTH-SERVICE`, `TRANSACTION-SERVICE`, `PAYMENT-SERVICE`, `ADMIN-SERVICE`

### Step 4 — Stop everything
```bash
docker compose down
```
To also delete the databases:
```bash
docker compose down -v
```

---

## 📡 API Endpoints (all go through port 8080)

### Auth Service
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | ❌ | Register new user |
| POST | `/api/auth/login` | ❌ | Login, returns JWT |

**Register body:**
```json
{
  "name": "Ahmed Ali",
  "email": "ahmed@example.com",
  "phoneNumber": "+201012345678",
  "password": "password123",
  "role": "CUSTOMER"
}
```
**Login body:**
```json
{
  "email": "ahmed@example.com",
  "password": "password123"
}
```

### Transaction Service (requires JWT)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/wallet/balance` | View wallet balance |
| POST | `/api/transactions/transfer` | Send money to another user |
| GET | `/api/transactions/history` | View my transaction history |
| POST | `/api/transactions/request-money` | Request money from another user |
| POST | `/api/transactions/request-money/{id}/approve` | Approve a money request |
| POST | `/api/transactions/request-money/{id}/reject` | Reject a money request |
| GET | `/api/transactions/request-money/incoming` | View incoming requests |
| GET | `/api/transactions/request-money/outgoing` | View outgoing requests |

**Transfer body:**
```json
{
  "receiverIdentifier": "user-id-or-phone",
  "amount": 100.00,
  "description": "Rent payment"
}
```

### Payment Service (requires JWT)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bank-accounts/link` | Link a bank account |
| GET | `/api/bank-accounts` | List my linked accounts |
| DELETE | `/api/bank-accounts/{id}` | Unlink a bank account |
| POST | `/api/payments/deposit` | Deposit from bank to wallet |
| POST | `/api/payments/withdraw` | Withdraw from wallet to bank |
| POST | `/api/payments/send-external` | Send to external (unregistered) user |
| GET | `/api/payments/history` | My external transaction history |

**Link bank account body:**
```json
{
  "cardNumber": "1234567890123456",
  "bankName": "CIB",
  "accountHolderName": "Ahmed Ali"
}
```

### Admin Service (requires JWT + ADMIN role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | View all users |
| PUT | `/api/admin/users/{id}/freeze?freeze=true` | Freeze/unfreeze account |
| GET | `/api/admin/transactions/internal` | View all internal transfers |
| GET | `/api/admin/transactions/external` | View all external transactions |
| GET | `/api/admin/transactions/search?userId=&from=&to=` | Search transactions |
| GET | `/api/admin/logs` | View audit logs |

---

## 🧪 Quick Test with curl

### 1. Register a customer
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ahmed Ali",
    "email": "ahmed@example.com",
    "phoneNumber": "+201012345678",
    "password": "password123",
    "role": "CUSTOMER"
  }'
```

### 2. Login and save token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ahmed@example.com","password":"password123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

echo "Token: $TOKEN"
```

### 3. Check wallet balance
```bash
curl http://localhost:8080/api/wallet/balance \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Register an admin
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@example.com",
    "phoneNumber": "+201099999999",
    "password": "admin1234",
    "role": "ADMIN"
  }'
```

---

## 🔧 Run Locally Without Docker (for development)

> Requires: Java 17, Maven, PostgreSQL installed and running locally.

### 1. Create 4 databases in PostgreSQL:
```sql
CREATE DATABASE authdb;
CREATE DATABASE transactiondb;
CREATE DATABASE paymentdb;
CREATE DATABASE admindb;
```

### 2. Update each service's `application.yml`:
Change `jdbc:postgresql://auth-db:5432/...` to `jdbc:postgresql://localhost:5432/...`

### 3. Start services in this order:
```bash
# Terminal 1 — Eureka (start first, wait 15s)
cd eureka-server && mvn spring-boot:run

# Terminal 2 — API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 3 — Auth Service
cd auth-service && mvn spring-boot:run

# Terminal 4 — Transaction Service
cd transaction-service && mvn spring-boot:run

# Terminal 5 — Payment Service
cd payment-service && mvn spring-boot:run

# Terminal 6 — Admin Service
cd admin-service && mvn spring-boot:run
```

---

## 🗂 Project Structure
```
banking-system/
├── docker-compose.yml
├── pom.xml                     ← parent POM
├── eureka-server/              ← Service Discovery (port 8761)
├── api-gateway/                ← API Gateway + JWT filter (port 8080)
├── auth-service/               ← Register, Login, JWT, RBAC (port 8081)
├── transaction-service/        ← Wallet, transfers, money requests (port 8082)
├── payment-service/            ← Bank linking, deposit, withdraw (port 8083)
└── admin-service/              ← Admin dashboard, freeze, audit logs (port 8084)
```

## 🛡 Security
- All endpoints except `/api/auth/**` require a valid JWT in the header: `Authorization: Bearer <token>`
- The **API Gateway** validates the JWT and passes `X-User-Id`, `X-User-Role`, `X-User-Email` headers to downstream services
- Passwords are stored **hashed** using BCrypt
- Role-based access via `@PreAuthorize` in each service

## 🔍 AOP Summary
| Service | AOP Applied To |
|---------|----------------|
| auth-service | Login attempts (success/failure logging) |
| transaction-service | Transfer execution time measurement |
| payment-service | Deposit/withdraw execution time + retry (max 3x) |
| admin-service | All admin actions logged (who, what, when) |
