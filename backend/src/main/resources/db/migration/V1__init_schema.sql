CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- 1. Tenants Table
CREATE TABLE tenants (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         name VARCHAR(100) NOT NULL,
                         timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Users Table (Okta Identity Mapping)
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                       okta_subject VARCHAR(255) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL, -- 'TENANT_ADMIN', 'STAFF'
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_users_tenant ON users(tenant_id);

-- 3. Services Catalog Table
CREATE TABLE services (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                          name VARCHAR(150) NOT NULL,
                          description TEXT,
                          category VARCHAR(100) NOT NULL,
                          duration_minutes INT NOT NULL CHECK (duration_minutes > 0),
                          price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
                          status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE'
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_services_tenant ON services(tenant_id);

-- 4. Staff Table
CREATE TABLE staff (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                       user_id UUID REFERENCES users(id) ON DELETE SET NULL,
                       name VARCHAR(150) NOT NULL,
                       email VARCHAR(255),
                       status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE'
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_staff_tenant ON staff(tenant_id);

-- 5. Staff Service Qualifications (Many-to-Many)
CREATE TABLE staff_services (
                                tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                                staff_id UUID NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
                                service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
                                PRIMARY KEY (staff_id, service_id)
);
CREATE INDEX idx_staff_services_tenant ON staff_services(tenant_id);

-- 6. Recurring Weekly Working Hours & Breaks
CREATE TABLE staff_availability (
                                    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                                    staff_id UUID NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
                                    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7), -- 1=Monday, 7=Sunday
                                    start_time TIME NOT NULL,
                                    end_time TIME NOT NULL,
                                    type VARCHAR(30) NOT NULL DEFAULT 'WORKING_HOURS', -- 'WORKING_HOURS', 'BREAK'
                                    CONSTRAINT chk_time_order CHECK (start_time < end_time)
);
CREATE INDEX idx_staff_avail_lookup ON staff_availability(tenant_id, staff_id, day_of_week);

-- 7. Bookings Table
CREATE TABLE bookings (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                          service_id UUID NOT NULL REFERENCES services(id),
                          staff_id UUID NOT NULL REFERENCES staff(id),
                          customer_name VARCHAR(150) NOT NULL,
                          pet_name VARCHAR(100),
                          start_at TIMESTAMPTZ NOT NULL,
                          end_at TIMESTAMPTZ NOT NULL,
                          status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED', -- 'CONFIRMED', 'CANCELLED'
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          CONSTRAINT chk_booking_dates CHECK (start_at < end_at)
);
CREATE INDEX idx_bookings_tenant_calendar ON bookings(tenant_id, staff_id, start_at, end_at);

-- Database Engine-level Exclusion Constraint: Guarantees zero double-booking
ALTER TABLE bookings ADD CONSTRAINT exclude_overlapping_bookings
    EXCLUDE USING gist (
    tenant_id WITH =,
    staff_id WITH =,
    tstzrange(start_at, end_at) WITH &&
) WHERE (status = 'CONFIRMED');