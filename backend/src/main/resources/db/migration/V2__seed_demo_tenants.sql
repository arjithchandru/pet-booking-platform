-- ----------------------------------------------------
-- Tenant 1: Happy Paws
-- ----------------------------------------------------
INSERT INTO tenants (id, name, timezone)
VALUES ('11111111-1111-1111-1111-111111111111', 'Happy Paws', 'UTC');

INSERT INTO users (id, tenant_id, okta_subject, email, role)
VALUES ('11111111-0000-0000-0000-111111111111', '11111111-1111-1111-1111-111111111111', 'okta_happy_paws_admin', 'admin@happypaws.test', 'TENANT_ADMIN');

INSERT INTO services (id, tenant_id, name, description, category, duration_minutes, price, status) VALUES
                                                                                                       ('11111111-2222-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Full Grooming', 'Complete bath, haircut & nail trim', 'Grooming', 90, 85.00, 'ACTIVE'),
                                                                                                       ('11111111-2222-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'Bath & Dry', 'Organic shampoo wash and blow dry', 'Bathing', 45, 40.00, 'ACTIVE');

INSERT INTO staff (id, tenant_id, name, email, status) VALUES
                                                           ('11111111-3333-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'John Doe', 'john@happypaws.test', 'ACTIVE'),
                                                           ('11111111-3333-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'Sarah Connor', 'sarah@happypaws.test', 'ACTIVE'),
                                                           ('11111111-3333-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', 'Mike Ross', 'mike@happypaws.test', 'ACTIVE');

INSERT INTO staff_services (tenant_id, staff_id, service_id) VALUES
                                                                 ('11111111-1111-1111-1111-111111111111', '11111111-3333-0000-0000-000000000001', '11111111-2222-0000-0000-000000000001'),
                                                                 ('11111111-1111-1111-1111-111111111111', '11111111-3333-0000-0000-000000000001', '11111111-2222-0000-0000-000000000002'),
                                                                 ('11111111-1111-1111-1111-111111111111', '11111111-3333-0000-0000-000000000002', '11111111-2222-0000-0000-000000000001'),
                                                                 ('11111111-1111-1111-1111-111111111111', '11111111-3333-0000-0000-000000000003', '11111111-2222-0000-0000-000000000002');

-- Seed Schedule: Mon-Fri 09:00 - 18:00 (John has Break 13:00 - 14:00)
DO $$
DECLARE
d INT;
BEGIN
FOR d IN 1..5 LOOP
        INSERT INTO staff_availability (tenant_id, staff_id, day_of_week, start_time, end_time, type)
        VALUES ('11111111-1111-1111-1111-111111111111', '11111111-3333-0000-0000-000000000001', d, '09:00', '18:00', 'WORKING_HOURS');
INSERT INTO staff_availability (tenant_id, staff_id, day_of_week, start_time, end_time, type)
VALUES ('11111111-1111-1111-1111-111111111111', '11111111-3333-0000-0000-000000000001', d, '13:00', '14:00', 'BREAK');

INSERT INTO staff_availability (tenant_id, staff_id, day_of_week, start_time, end_time, type)
VALUES ('11111111-1111-1111-1111-111111111111', '11111111-3333-0000-0000-000000000002', d, '09:00', '17:00', 'WORKING_HOURS');
END LOOP;
END $$;

-- ----------------------------------------------------
-- Tenant 2: Paws & Play
-- ----------------------------------------------------
INSERT INTO tenants (id, name, timezone)
VALUES ('22222222-2222-2222-2222-222222222222', 'Paws & Play', 'UTC');

INSERT INTO users (id, tenant_id, okta_subject, email, role)
VALUES ('22222222-0000-0000-0000-222222222222', '22222222-2222-2222-2222-222222222222', 'okta_paws_play_admin', 'admin@pawsplay.test', 'TENANT_ADMIN');

INSERT INTO services (id, tenant_id, name, description, category, duration_minutes, price, status) VALUES
                                                                                                       ('22222222-2222-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', 'Dog Training', '1-on-1 behavioral training', 'Training', 60, 100.00, 'ACTIVE'),
                                                                                                       ('22222222-2222-0000-0000-000000000002', '22222222-2222-2222-2222-222222222222', 'Nail Trim', 'Quick nail trimming & grind', 'Grooming', 20, 15.00, 'ACTIVE');

INSERT INTO staff (id, tenant_id, name, email, status) VALUES
                                                           ('22222222-3333-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', 'Alex Green', 'alex@pawsplay.test', 'ACTIVE'),
                                                           ('22222222-3333-0000-0000-000000000002', '22222222-2222-2222-2222-222222222222', 'Emma Watson', 'emma@pawsplay.test', 'ACTIVE');

INSERT INTO staff_services (tenant_id, staff_id, service_id) VALUES
                                                                 ('22222222-2222-2222-2222-222222222222', '22222222-3333-0000-0000-000000000001', '22222222-2222-0000-0000-000000000001'),
                                                                 ('22222222-2222-2222-2222-222222222222', '22222222-3333-0000-0000-000000000002', '22222222-2222-0000-0000-000000000002');