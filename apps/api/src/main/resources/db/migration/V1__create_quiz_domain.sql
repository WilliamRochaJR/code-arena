CREATE TABLE app_users (
    id UUID NOT NULL,
    identity_provider_subject VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(120),
    created_at TIMESTAMPTZ NOT NULL,
    last_login_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_app_users PRIMARY KEY (id),
    CONSTRAINT uk_app_users_identity_provider_subject
        UNIQUE (identity_provider_subject),
    CONSTRAINT ck_app_users_email_not_blank
        CHECK (btrim(email) <> ''),
    CONSTRAINT ck_app_users_login_after_creation
        CHECK (created_at <= last_login_at)
);

CREATE TABLE categories (
    id UUID NOT NULL,
    slug VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uk_categories_slug UNIQUE (slug),
    CONSTRAINT ck_categories_slug_not_blank CHECK (btrim(slug) <> ''),
    CONSTRAINT ck_categories_name_not_blank CHECK (btrim(name) <> '')
);

CREATE TABLE questions (
    id UUID NOT NULL,
    statement TEXT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    explanation TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_questions PRIMARY KEY (id),
    CONSTRAINT ck_questions_statement_not_blank
        CHECK (btrim(statement) <> ''),
    CONSTRAINT ck_questions_difficulty
        CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT ck_questions_explanation_not_blank
        CHECK (btrim(explanation) <> ''),
    CONSTRAINT ck_questions_update_after_creation
        CHECK (created_at <= updated_at)
);

CREATE INDEX idx_questions_active_difficulty
    ON questions (active, difficulty);

CREATE TABLE question_categories (
    question_id UUID NOT NULL,
    category_id UUID NOT NULL,
    CONSTRAINT pk_question_categories
        PRIMARY KEY (question_id, category_id),
    CONSTRAINT fk_question_categories_question
        FOREIGN KEY (question_id)
        REFERENCES questions (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_question_categories_category
        FOREIGN KEY (category_id)
        REFERENCES categories (id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_question_categories_category_question
    ON question_categories (category_id, question_id);

CREATE TABLE alternatives (
    id UUID NOT NULL,
    question_id UUID NOT NULL,
    text TEXT NOT NULL,
    correct BOOLEAN NOT NULL,
    display_order SMALLINT NOT NULL,
    CONSTRAINT pk_alternatives PRIMARY KEY (id),
    CONSTRAINT fk_alternatives_question
        FOREIGN KEY (question_id)
        REFERENCES questions (id)
        ON DELETE RESTRICT,
    CONSTRAINT uk_alternatives_question_order
        UNIQUE (question_id, display_order),
    CONSTRAINT uk_alternatives_question_id
        UNIQUE (question_id, id),
    CONSTRAINT ck_alternatives_text_not_blank CHECK (btrim(text) <> ''),
    CONSTRAINT ck_alternatives_display_order_positive CHECK (display_order > 0)
);

CREATE UNIQUE INDEX uk_alternatives_one_correct_per_question
    ON alternatives (question_id)
    WHERE correct;

CREATE TABLE quiz_attempts (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_questions SMALLINT NOT NULL,
    correct_answers SMALLINT,
    score NUMERIC(5, 2),
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT pk_quiz_attempts PRIMARY KEY (id),
    CONSTRAINT fk_quiz_attempts_user
        FOREIGN KEY (user_id)
        REFERENCES app_users (id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_quiz_attempts_difficulty
        CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT ck_quiz_attempts_status
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT ck_quiz_attempts_total_questions
        CHECK (total_questions = 10),
    CONSTRAINT ck_quiz_attempts_correct_answers
        CHECK (
            correct_answers IS NULL
            OR correct_answers BETWEEN 0 AND total_questions
        ),
    CONSTRAINT ck_quiz_attempts_score
        CHECK (score IS NULL OR score BETWEEN 0.00 AND 100.00),
    CONSTRAINT ck_quiz_attempts_state
        CHECK (
            (
                status = 'IN_PROGRESS'
                AND correct_answers IS NULL
                AND score IS NULL
                AND completed_at IS NULL
            )
            OR
            (
                status = 'COMPLETED'
                AND correct_answers IS NOT NULL
                AND score IS NOT NULL
                AND completed_at IS NOT NULL
            )
        ),
    CONSTRAINT ck_quiz_attempts_completion_after_start
        CHECK (completed_at IS NULL OR started_at <= completed_at)
);

CREATE INDEX idx_quiz_attempts_user_started
    ON quiz_attempts (user_id, started_at DESC);

CREATE INDEX idx_quiz_attempts_user_status_started
    ON quiz_attempts (user_id, status, started_at DESC);

CREATE TABLE quiz_attempt_categories (
    attempt_id UUID NOT NULL,
    category_id UUID NOT NULL,
    CONSTRAINT pk_quiz_attempt_categories
        PRIMARY KEY (attempt_id, category_id),
    CONSTRAINT fk_quiz_attempt_categories_attempt
        FOREIGN KEY (attempt_id)
        REFERENCES quiz_attempts (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_quiz_attempt_categories_category
        FOREIGN KEY (category_id)
        REFERENCES categories (id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_quiz_attempt_categories_category_attempt
    ON quiz_attempt_categories (category_id, attempt_id);

CREATE TABLE attempt_questions (
    id UUID NOT NULL,
    attempt_id UUID NOT NULL,
    question_id UUID NOT NULL,
    position SMALLINT NOT NULL,
    selected_alternative_id UUID,
    correct BOOLEAN,
    answered_at TIMESTAMPTZ,
    CONSTRAINT pk_attempt_questions PRIMARY KEY (id),
    CONSTRAINT fk_attempt_questions_attempt
        FOREIGN KEY (attempt_id)
        REFERENCES quiz_attempts (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_attempt_questions_question
        FOREIGN KEY (question_id)
        REFERENCES questions (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_attempt_questions_selected_alternative
        FOREIGN KEY (question_id, selected_alternative_id)
        REFERENCES alternatives (question_id, id)
        ON DELETE RESTRICT,
    CONSTRAINT uk_attempt_questions_attempt_position
        UNIQUE (attempt_id, position),
    CONSTRAINT uk_attempt_questions_attempt_question
        UNIQUE (attempt_id, question_id),
    CONSTRAINT ck_attempt_questions_position
        CHECK (position BETWEEN 1 AND 10),
    CONSTRAINT ck_attempt_questions_answer_timestamp
        CHECK (
            (selected_alternative_id IS NULL AND answered_at IS NULL)
            OR
            (selected_alternative_id IS NOT NULL AND answered_at IS NOT NULL)
        )
);
