package com.deutschlingodeck.game.repository;

import com.deutschlingodeck.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
