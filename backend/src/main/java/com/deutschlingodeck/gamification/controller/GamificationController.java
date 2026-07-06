package com.deutschlingodeck.gamification.controller;

import com.deutschlingodeck.common.constants.ApiConstants;
import com.deutschlingodeck.common.security.CurrentUserProvider;
import com.deutschlingodeck.gamification.dto.AchievementResponse;
import com.deutschlingodeck.gamification.dto.DailyGoalResponse;
import com.deutschlingodeck.gamification.dto.LevelProgressResponse;
import com.deutschlingodeck.gamification.dto.WeeklyActivityPointResponse;
import com.deutschlingodeck.gamification.service.GamificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Real (persisted) backing for what was previously a frontend-only demo layer -
 * see {@code features/gamification/services/gamification.service.ts}. One endpoint per
 * frontend call so that service can swap its mocked bodies for HTTP calls with no shape change.
 */
@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + "/gamification")
public class GamificationController {

	private final GamificationService gamificationService;
	private final CurrentUserProvider currentUserProvider;

	public GamificationController(GamificationService gamificationService, CurrentUserProvider currentUserProvider) {
		this.gamificationService = gamificationService;
		this.currentUserProvider = currentUserProvider;
	}

	@GetMapping("/level-progress")
	public ResponseEntity<LevelProgressResponse> getLevelProgress() {
		return ResponseEntity.ok(gamificationService.getLevelProgress(currentUserProvider.getUserId()));
	}

	@GetMapping("/daily-goal")
	public ResponseEntity<DailyGoalResponse> getDailyGoal() {
		return ResponseEntity.ok(gamificationService.getDailyGoal(currentUserProvider.getUserId()));
	}

	@GetMapping("/achievements")
	public ResponseEntity<List<AchievementResponse>> getAchievements() {
		return ResponseEntity.ok(gamificationService.getAchievements(currentUserProvider.getUserId()));
	}

	@GetMapping("/weekly-activity")
	public ResponseEntity<List<WeeklyActivityPointResponse>> getWeeklyActivity() {
		return ResponseEntity.ok(gamificationService.getWeeklyActivity(currentUserProvider.getUserId()));
	}
}
