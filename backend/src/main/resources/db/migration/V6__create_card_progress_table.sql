CREATE TABLE card_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    card_id BIGINT NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    ease_factor NUMERIC(4, 2) NOT NULL DEFAULT 2.5,
    interval_days INTEGER NOT NULL DEFAULT 0,
    repetitions INTEGER NOT NULL DEFAULT 0,
    due_date DATE NOT NULL DEFAULT CURRENT_DATE,
    last_reviewed_at TIMESTAMPTZ,
    review_count INTEGER NOT NULL DEFAULT 0,
    correct_count INTEGER NOT NULL DEFAULT 0,
    incorrect_count INTEGER NOT NULL DEFAULT 0,
    UNIQUE (user_id, card_id)
);

CREATE INDEX idx_card_progress_user_due ON card_progress (user_id, due_date);
CREATE INDEX idx_card_progress_card_id ON card_progress (card_id);
