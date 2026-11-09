TRUNCATE TABLE workout_set, workout_exercise, workout CASCADE;

INSERT INTO workout (id, user_name, date, description, created_at)
SELECT
    ('00000000-0000-0000-0000-' || lpad(sequence_number::text, 12, '0'))::uuid,
    'athlete@example.test',
    DATE '2026-01-01' + sequence_number,
    'Workout ' || sequence_number,
    TIMESTAMPTZ '2026-01-01T10:00:00Z' + sequence_number * INTERVAL '1 day'
FROM generate_series(1, 105) AS series(sequence_number);

INSERT INTO workout (id, user_name, date, description, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000191', 'other@example.test', '2026-11-10', 'Other user 1', '2026-11-10T10:00:00Z'),
    ('00000000-0000-0000-0000-000000000192', 'other@example.test', '2026-11-11', 'Other user 2', '2026-11-11T10:00:00Z');
