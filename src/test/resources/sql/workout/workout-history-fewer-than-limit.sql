TRUNCATE TABLE workout_set, workout_exercise, workout CASCADE;

INSERT INTO workout (id, user_name, date, description, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000121', 'athlete@example.test', '2026-11-05', 'Workout 1', '2026-11-05T10:00:00Z'),
    ('00000000-0000-0000-0000-000000000122', 'athlete@example.test', '2026-11-06', 'Workout 2', '2026-11-06T10:00:00Z'),
    ('00000000-0000-0000-0000-000000000123', 'athlete@example.test', '2026-11-07', 'Workout 3', '2026-11-07T10:00:00Z'),
    ('00000000-0000-0000-0000-000000000191', 'other@example.test', '2026-11-10', 'Other user 1', '2026-11-10T10:00:00Z');
