package com.deutschlingodeck.gamification.repository;

import com.deutschlingodeck.gamification.entity.UserGamificationStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGamificationStatsRepository extends JpaRepository<UserGamificationStats, Long> {
}
