package com.deutschlingodeck.gamification.entity;

import com.deutschlingodeck.authentication.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "user_gamification_stats")
public class UserGamificationStats {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "total_xp", nullable = false)
	private Long totalXp;

	@Column(name = "current_streak", nullable = false)
	private Integer currentStreak;

	@Column(name = "longest_streak", nullable = false)
	private Integer longestStreak;

	@Column(name = "last_activity_date")
	private LocalDate lastActivityDate;

	@Column(name = "daily_goal_target", nullable = false)
	private Integer dailyGoalTarget;

	protected UserGamificationStats() {
	}

	public UserGamificationStats(User user) {
		// Leave `userId` (the @MapsId field) null - Hibernate derives it from `user` at
		// persist time. Pre-setting it here would make Spring Data's isNew() check treat
		// this transient instance as existing and call merge() instead of persist().
		this.user = user;
		this.totalXp = 0L;
		this.currentStreak = 0;
		this.longestStreak = 0;
		this.dailyGoalTarget = 20;
	}

	public Long getUserId() {
		return userId;
	}

	public Long getTotalXp() {
		return totalXp;
	}

	public void setTotalXp(Long totalXp) {
		this.totalXp = totalXp;
	}

	public Integer getCurrentStreak() {
		return currentStreak;
	}

	public void setCurrentStreak(Integer currentStreak) {
		this.currentStreak = currentStreak;
	}

	public Integer getLongestStreak() {
		return longestStreak;
	}

	public void setLongestStreak(Integer longestStreak) {
		this.longestStreak = longestStreak;
	}

	public LocalDate getLastActivityDate() {
		return lastActivityDate;
	}

	public void setLastActivityDate(LocalDate lastActivityDate) {
		this.lastActivityDate = lastActivityDate;
	}

	public Integer getDailyGoalTarget() {
		return dailyGoalTarget;
	}

	public void setDailyGoalTarget(Integer dailyGoalTarget) {
		this.dailyGoalTarget = dailyGoalTarget;
	}
}
