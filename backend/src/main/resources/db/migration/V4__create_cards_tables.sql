CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    dictionary_id BIGINT NOT NULL REFERENCES dictionaries (id) ON DELETE CASCADE,
    card_type VARCHAR(20) NOT NULL,
    article VARCHAR(20),
    source_text VARCHAR(255) NOT NULL,
    primary_translation VARCHAR(255),
    example VARCHAR(1000),
    notes VARCHAR(1000),
    difficulty_level INTEGER,
    position INTEGER NOT NULL
);

CREATE INDEX idx_cards_dictionary_id ON cards (dictionary_id);

CREATE TABLE card_accepted_answers (
    card_id BIGINT NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    answer VARCHAR(255) NOT NULL
);

CREATE INDEX idx_card_accepted_answers_card_id ON card_accepted_answers (card_id);

CREATE TABLE card_tags (
    card_id BIGINT NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    tag VARCHAR(50) NOT NULL
);

CREATE INDEX idx_card_tags_card_id ON card_tags (card_id);
CREATE INDEX idx_card_tags_tag ON card_tags (tag);
