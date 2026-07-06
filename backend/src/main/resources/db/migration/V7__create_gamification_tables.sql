CREATE TABLE user_gamification_stats (
    user_id BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    total_xp BIGINT NOT NULL DEFAULT 0,
    current_streak INTEGER NOT NULL DEFAULT 0,
    longest_streak INTEGER NOT NULL DEFAULT 0,
    last_activity_date DATE,
    daily_goal_target INTEGER NOT NULL DEFAULT 20
);

CREATE TABLE user_achievements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    achievement_code VARCHAR(50) NOT NULL,
    unlocked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, achievement_code)
);

CREATE INDEX idx_user_achievements_user_id ON user_achievements (user_id);
