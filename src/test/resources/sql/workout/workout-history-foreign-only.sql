TRUNCATE TABLE workout_set, workout_exercise, workout CASCADE;

INSERT INTO workout (id, user_name, date, description, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000191', 'other@example.test', '2026-11-10', 'Other user 1', '2026-11-10T10:00:00Z'),
    ('00000000-0000-0000-0000-000000000192', 'other@example.test', '2026-11-11', 'Other user 2', '2026-11-11T10:00:00Z');
