TRUNCATE TABLE workout_set, workout_exercise, workout CASCADE;

INSERT INTO workout (id, user_name, date, description, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000101',
    'athlete@example.test',
    '2026-11-08',
    'Upper body strength',
    '2026-11-08T10:15:30Z'
), (
    '00000000-0000-0000-0000-000000000199',
    'other@example.test',
    '2026-11-07',
    'Another user workout',
    '2026-11-07T10:15:30Z'
);

INSERT INTO workout_exercise (id, workout_id, position, name, instructions)
VALUES
    ('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000101', 0, 'Dumbbell bench press', 'Control the lowering phase'),
    ('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000101', 1, 'Plank', NULL);

INSERT INTO workout_set (
    id, workout_exercise_id, number, target_reps, target_load_kg, target_description,
    actual_reps, actual_load_kg, effort, feedback
)
VALUES
    ('00000000-0000-0000-0000-000000000301', '00000000-0000-0000-0000-000000000201', 0, 10, 20.0, NULL, NULL, NULL, NULL, NULL),
    ('00000000-0000-0000-0000-000000000302', '00000000-0000-0000-0000-000000000201', 1, 8, 22.5, NULL, 8, 22.5, 7, 'Good set'),
    ('00000000-0000-0000-0000-000000000303', '00000000-0000-0000-0000-000000000202', 0, NULL, NULL, 'Hold for 30 seconds', NULL, NULL, NULL, NULL);
