CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Ensure staff_services is defined as a pure join table
CREATE TABLE IF NOT EXISTS staff_services (staff_id UUID NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    PRIMARY KEY (staff_id, service_id));