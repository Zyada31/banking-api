-- Insert Initial Customer Data
INSERT INTO customer (id, name) VALUES (1, 'Arisha Barron') ON CONFLICT (id) DO NOTHING;
INSERT INTO customer (id, name) VALUES (2, 'Branden Gibson') ON CONFLICT (id) DO NOTHING;
INSERT INTO customer (id, name) VALUES (3, 'Rhonda Church') ON CONFLICT (id) DO NOTHING;
INSERT INTO customer (id, name) VALUES (4, 'Georgina Hazel') ON CONFLICT (id) DO NOTHING;
