TRUNCATE TABLE workout_set, workout_exercise, workout CASCADE;

INSERT INTO workout (id, user_name, date, description, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000101',
    'athlete@example.test',
    '2026-11-08',
    'Planned workout',
    '2026-11-08T10:15:30Z'
);

INSERT INTO workout_exercise (id, workout_id, position, name, instructions)
VALUES (
    '00000000-0000-0000-0000-000000000201',
    '00000000-0000-0000-0000-000000000101',
    0,
    'Squat',
    NULL
);

INSERT INTO workout_set (
    id, workout_exercise_id, number, target_reps, target_load_kg, target_description,
    actual_reps, actual_load_kg, effort, feedback
)
VALUES
    ('00000000-0000-0000-0000-000000000311', '00000000-0000-0000-0000-000000000201', 0, 5, 60.0, NULL, NULL, NULL, NULL, NULL),
    ('00000000-0000-0000-0000-000000000312', '00000000-0000-0000-0000-000000000201', 1, 8, 50.0, NULL, NULL, NULL, NULL, NULL),
    ('00000000-0000-0000-0000-000000000313', '00000000-0000-0000-0000-000000000201', 2, 10, 40.0, NULL, NULL, NULL, NULL, NULL);
