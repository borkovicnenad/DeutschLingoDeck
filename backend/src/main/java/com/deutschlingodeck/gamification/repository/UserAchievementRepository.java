package com.deutschlingodeck.gamification.repository;

import com.deutschlingodeck.gamification.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

	List<UserAchievement> findByUserId(Long userId);

	boolean existsByUserIdAndAchievementCode(Long userId, String achievementCode);
}
