TRUNCATE TABLE workout_set, workout_exercise, workout CASCADE;

INSERT INTO workout (id, user_name, date, description, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000111', 'athlete@example.test', '2026-11-05', 'Workout 1', '2026-11-05T10:00:00Z'),
    ('00000000-0000-0000-0000-000000000112', 'athlete@example.test', '2026-11-06', 'Workout 2', '2026-11-06T10:00:00Z'),
    ('00000000-0000-0000-0000-000000000113', 'athlete@example.test', '2026-11-07', 'Workout 3', '2026-11-07T10:00:00Z'),
    ('00000000-0000-0000-0000-000000000114', 'athlete@example.test', '2026-11-08', 'Workout 4', '2026-11-08T10:00:00Z'),
    ('00000000-0000-0000-0000-000000000115', 'athlete@example.test', '2026-11-09', 'Latest workout', '2026-11-09T10:00:00Z'),
    ('00000000-0000-0000-0000-000000000191', 'other@example.test', '2026-11-10', 'Other user 1', '2026-11-10T10:00:00Z'),
    ('00000000-0000-0000-0000-000000000192', 'other@example.test', '2026-11-11', 'Other user 2', '2026-11-11T10:00:00Z');

-- Intentionally insert rows out of their list-index order.
INSERT INTO workout_exercise (id, workout_id, position, name, instructions)
VALUES
    ('00000000-0000-0000-0000-000000000215', '00000000-0000-0000-0000-000000000115', 1, 'Overhead press', NULL),
    ('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000115', 0, 'Back squat', 'Brace before each rep');

-- Set rows are also inserted in reverse number order to exercise @OrderColumn mapping.
INSERT INTO workout_set (
    id, workout_exercise_id, number, target_reps, target_load_kg, target_description,
    actual_reps, actual_load_kg, effort, feedback
)
VALUES
    ('00000000-0000-0000-0000-000000000325', '00000000-0000-0000-0000-000000000205', 1, 5, 100.0, NULL, 5, 100.0, 8, 'Second set'),
    ('00000000-0000-0000-0000-000000000315', '00000000-0000-0000-0000-000000000205', 0, 5, 90.0, NULL, 5, 90.0, 7, 'First set'),
    ('00000000-0000-0000-0000-000000000335', '00000000-0000-0000-0000-000000000215', 0, 8, 40.0, NULL, NULL, NULL, NULL, NULL);
