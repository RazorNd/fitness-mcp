CREATE TABLE workout (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_name VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX workout_user_date_created_idx
    ON workout (user_name, date DESC, created_at DESC);

CREATE TABLE workout_exercise (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workout_id UUID NOT NULL REFERENCES workout (id) ON DELETE CASCADE,
    position INTEGER,
    name VARCHAR(255) NOT NULL,
    instructions TEXT
);

CREATE INDEX workout_exercise_workout_position_idx
    ON workout_exercise (workout_id, position);

CREATE TABLE workout_set (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workout_exercise_id UUID NOT NULL REFERENCES workout_exercise (id) ON DELETE CASCADE,
    number INTEGER,
    target_reps INTEGER,
    target_load_kg DOUBLE PRECISION,
    target_description TEXT,
    actual_reps INTEGER,
    actual_load_kg DOUBLE PRECISION,
    effort INTEGER,
    feedback TEXT,
    CONSTRAINT workout_set_exercise_number_unique
        UNIQUE (workout_exercise_id, number)
        DEFERRABLE INITIALLY DEFERRED
);
