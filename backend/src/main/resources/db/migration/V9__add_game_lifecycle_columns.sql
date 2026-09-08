ALTER TABLE games ADD COLUMN last_activity_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE games ADD COLUMN game_mode VARCHAR(20) NOT NULL DEFAULT 'STANDARD';

-- Existing data may already have more than one IN_PROGRESS game per user - that's the exact bug
-- this migration guards against going forward. Keep only the most recently started IN_PROGRESS
-- game per user and mark the rest ABANDONED so the unique index below can be created.
WITH ranked AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY started_at DESC) AS rn
    FROM games
    WHERE status = 'IN_PROGRESS'
)
UPDATE games
SET status = 'ABANDONED', finished_at = now()
WHERE id IN (SELECT id FROM ranked WHERE rn > 1);

-- Guarantees at most one IN_PROGRESS game per user even under a race
-- (e.g. a double-submitted "start game" click); the primary defence is the
-- application-level check in GameServiceImpl.createGame, this is the backstop.
CREATE UNIQUE INDEX ux_games_one_in_progress_per_user ON games (user_id) WHERE status = 'IN_PROGRESS';
