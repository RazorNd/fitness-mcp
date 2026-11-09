TRUNCATE TABLE workout_set, workout_exercise, workout CASCADE;

INSERT INTO workout (id, user_name, date, description, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000101',
    'fitness-test-user',
    '2026-11-08',
    'Seeded workout',
    '2026-11-08T10:15:30Z'
);

INSERT INTO workout_exercise (id, workout_id, position, name, instructions)
VALUES (
    '00000000-0000-0000-0000-000000000201',
    '00000000-0000-0000-0000-000000000101',
    0,
    'Seeded exercise',
    'Seeded instructions'
);

INSERT INTO workout_set (
    id, workout_exercise_id, number, target_reps, target_load_kg, target_description,
    actual_reps, actual_load_kg, effort, feedback
)
VALUES (
    '00000000-0000-0000-0000-000000000301',
    '00000000-0000-0000-0000-000000000201',
    0,
    8,
    20.0,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL
);
