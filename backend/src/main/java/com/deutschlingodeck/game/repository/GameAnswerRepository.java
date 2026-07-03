package com.deutschlingodeck.game.repository;

import com.deutschlingodeck.game.entity.GameAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameAnswerRepository extends JpaRepository<GameAnswer, Long> {
}
