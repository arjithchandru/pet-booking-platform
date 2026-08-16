# pet-booking-platform
Production-grade multi-tenant service &amp; staff scheduling platform with dynamic slot calculation and race-condition-safe booking in Spring Boot 3 &amp; React.

# Multi-Tenant Service, Staff Availability & Booking Platform

A production-grade SaaS administrative platform for pet-care businesses to manage services, staff rosters, recurring working schedules, and real-time appointment bookings with strict double-booking prevention.

#### Start application using 
```bash
pet-booking-platform\start-app.bat
```

#### Stop application using
```bash
pet-booking-platform\stop-app.bat
```

---

## 1. System Architecture & Technical Notes

### A. Server-Side Multi-Tenant Isolation
* **Zero IDOR Guarantee:** Tenant context is established strictly on the server side via `TenantFilter` from the authenticated user token (`TenantContext` `ThreadLocal`).
* **Query Scoping:** Every database read, mutation, and qualification lookup enforces `tenant_id` filtering. Browser-supplied `tenantId` inputs are rejected, preventing cross-tenant data leaks.
* **Resource Boundary:** Attempting to query, update, or book resources belonging to another tenant returns `404 Not Found` without revealing resource existence.

### B. Concurrency Control & Double-Booking Prevention
* **PostgreSQL Exclusion Constraints:** Double-booking prevention is enforced at the database engine level using the PostgreSQL `btree_gist` extension with a `tstzrange` exclusion constraint (`exclude_overlapping_bookings`).
* **Race Condition Resilience:** If two concurrent requests attempt to book the same staff member for overlapping time windows, the database permits exactly one transaction and raises a constraint violation for the second.
* **Transactional Rollback:** The backend translates constraint violations into structured `409 Conflict` responses, ensuring zero orphan states.

### C. Timezone & Temporal Strategy
* **Backend Storage:** All booking boundaries (`startAt`, `endAt`) are stored as UTC timestamps (`TIMESTAMP WITH TIME ZONE` / Java `Instant`).
* **Availability Calculations:** Daily working windows and break periods (`LocalTime`) are calculated against the specific tenant's configured timezone (e.g., `America/New_York`).
* **Client Synchronization:** The frontend transfers standard ISO-8601 UTC strings, rendering slots in the administrative user's operational timezone.

---

## 2. Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot 3.2.3, Spring Data JPA, Spring Security |
| **Frontend** | React 18, TypeScript / JavaScript (ES6+), Tailwind CSS, Lucide Icons, Date-fns |
| **Database** | PostgreSQL 16 with `btree_gist` extension |
| **Migrations** | Flyway DB |
| **Auth** | Okta OIDC / OAuth 2.0 (with Local Mock Token Provider) |
| **API Docs** | SpringDoc OpenAPI 3 / Swagger UI |
| **Testing** | JUnit 5, AssertJ, Spring Security Test, Testcontainers |

---

## 3. Getting Started & Local Execution

### Prerequisites
* Docker & Docker Compose** (Engine 24+)
* Java 17+ JDK
* Node.js 18+ & npm

### Step 1: Start PostgreSQL Infrastructure
```bash
docker compose up -d
```

### Step 2: Run Backend Service
```bash
cd backend
./mvnw clean spring-boot:run
```
Backend API boots on http://localhost:8080

### Step 3: Frontend Portal
```bash
cd frontend
npm install
npm run dev
```
Access the Admin Portal at http://localhost:5173.  

### Step 4: . Core API Endpoints
| Endpoints                                                      | Description                                 |
|:---------------------------------------------------------------|:--------------------------------------------|
| GET /api/services                                              | List all tenant services                    |
| POST /api/services                                             | Create new service catalog item             
| PUT /api/services/{id}                                         | Update service duration, price, and status  
| DELETE /api/services/{id}                                      | Deactivate service                          
| GET /api/staff                                                 | List staff members and qualified services   
| POST /api/staff                                                | Create staff member with service assignments
| PUT /api/staff/{id}                                            | Update staff details and qualifications
| GET /api/staff/{id}/availability                               | Retrieve recurring weekly schedule and breaks
| POST /api/staff/{id}/availability                              | Define working window or break period
| POST /api/bookings                                             | Create booking with atomic concurrency verification
| POST /api/bookings/{id}/cancel                                 | Cancel booking and release slot

### Step 5: Automated Testing

```bash 
cd backend
./mvnw test
```
**TenantIsolationTest:** Asserts cross-tenant queries return 404 and data cannot be leaked across organizations.

**ConcurrentBookingTest:** Executes parallel race conditions against the same staff slot; asserts exactly 1 succeeds and second receives 409 Conflict.

### Step 6: Demo data and tenant testing
| Tenant Name | Tenant ID                            | Demo Admin | 
|-------------|--------------------------------------|------------|
|Happy Paws| 11111111-1111-1111-1111-111111111111 |okta_happy_paws_admin
|Paws & Play| 22222222-2222-2222-2222-222222222222 | okta_paws_play_admin

Use the top-right tenant dropdown in the Admin Portal to switch tenant contexts instantly.