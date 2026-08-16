---

### `AI_USAGE.md`

```markdown
# AI Collaboration & Engineering Disclosure (`AI_USAGE.md`)

This document discloses the AI development tools used, tasks delegated, engineering decisions made, and defects corrected during the implementation of the **Multi-Tenant Service, Staff Availability & Booking Platform**.

---

## 1. Tools & Workflows Used

* **Claude Code & Anthropic Models:** Used for initial structural brainstorming, boilerplate DTO drafting, complex interval query design, and drafting test mock datasets.
* **GitHub Copilot:** Used in-IDE for autocompleting repetitive repository methods, UI layout scaffolding, and standard CRUD boilerplate.

---

## 2. Major Tasks Delegated to AI

1. **Scaffolding Repetitive Boilerplate:**
    * Generated initial request/response DTO records (`CreateServiceRequest`, `StaffResponse`, `BookingResponse`).
    * Scaffolding standard Spring Data JPA query interfaces and CRUD controller mappings.
2. **Frontend UI Components:**
    * Drafting weekly grid calculations and date arithmetic using `date-fns` for `ServicesCalendar.jsx` and `StaffAvailabilityCalendar.jsx`.
    * Building modal forms with dynamic multi-select tags for service qualification assignments.
3. **Database Migration Baseline:**
    * Writing initial Flyway SQL scripts for table schemas and demo tenant seed inserts.

---

## 3. Human Architectural Decisions & AI Overrides

While AI accelerated code generation, critical architectural decisions were human-directed and verified:

### A. Rejection of Application-Level Locking for Concurrency
* **AI Suggestion:** Initial AI code proposals suggested checking slot availability with a `SELECT` query followed by a `save()` inside a `@Transactional` method.
* **Human Engineering Correction:** Rejected as vulnerable to race conditions under high concurrency. Implemented PostgreSQL `btree_gist` exclusion constraints with `tstzrange` (`exclude_overlapping_bookings`) directly at the database engine level to guarantee mathematical overlap prevention.

### B. Strict Server-Side Multi-Tenancy (Anti-IDOR)
* **AI Suggestion:** Client components initially passed `tenantId` in request bodies.
* **Human Engineering Correction:** Stripped all browser-supplied `tenantId` parameters. Enforced server-side resolution via `TenantFilter` into a thread-safe `TenantContext` to eliminate Insecure Direct Object References (IDOR).

### C. Timezone Precision
* **Human Engineering Decision:** Mandated UTC `Instant` storage across all database timestamp columns, resolving availability windows locally against the tenant's configured IANA timezone string to eliminate daylight saving shift errors.

---

## 4. Defects Caught & Corrected During Review

1. **Lombok Annotation Processing Halts:**
    * *Issue:* A duplicate class declaration in `GlobalExceptionHandler.java` and duplicate variable definition in `Staff.java` caused Maven `javac` to fail annotation processing, breaking symbols across 90+ files.
    * *Fix:* Cleaned entity models, unified DTO imports, and configured explicit Lombok annotation processor paths in `pom.xml`.
2. **Join Table Constraint Rollbacks:**
    * *Issue:* Flyway schema originally had a `NOT NULL` constraint on `tenant_id` inside the `staff_services` join table, which caused JPA `@ManyToMany` persistence to fail during staff creation.
    * *Fix:* Refactored `staff_services` to a pure composite foreign key join table (`staff_id`, `service_id`), letting entity root tenants manage isolation.
3. **Exception Handler Misclassification:**
    * *Issue:* The global exception handler originally returned generic booking conflict messages for any `DataIntegrityViolationException`.
    * *Fix:* Implemented root cause inspection to distinguish between PostgreSQL exclusion overlaps (409 Conflict), unique email violations (400 Bad Request), and foreign key failures.

---

## 5. Responsible AI Compliance

* **No Secrets Committed:** Zero API keys, environment credentials, or production tokens were shared with AI tools or committed to source control.
* **Full Code Ownership:** All AI-assisted components, business logic, and test suites were reviewed, compiled, and verified through automated test suites.