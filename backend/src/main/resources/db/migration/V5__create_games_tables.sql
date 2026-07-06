CREATE TABLE games (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    dictionary_id BIGINT NOT NULL REFERENCES dictionaries (id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    total_cards INTEGER NOT NULL,
    answered_cards INTEGER,
    correct_answers INTEGER,
    incorrect_answers INTEGER,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at TIMESTAMPTZ,
    deck_card_ids TEXT
);

CREATE INDEX idx_games_user_id ON games (user_id);
CREATE INDEX idx_games_dictionary_id ON games (dictionary_id);
CREATE INDEX idx_games_user_id_status ON games (user_id, status);

CREATE TABLE game_answers (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES games (id) ON DELETE CASCADE,
    card_id BIGINT NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    given_answer VARCHAR(500) NOT NULL,
    result VARCHAR(30) NOT NULL,
    correct BOOLEAN NOT NULL,
    response_time_ms BIGINT NOT NULL,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_game_answers_game_id ON game_answers (game_id);
CREATE INDEX idx_game_answers_card_id ON game_answers (card_id);
