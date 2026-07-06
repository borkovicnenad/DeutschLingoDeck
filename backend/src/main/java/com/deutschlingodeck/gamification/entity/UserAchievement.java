package com.deutschlingodeck.gamification.entity;

import com.deutschlingodeck.authentication.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_achievements")
public class UserAchievement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "achievement_code", nullable = false)
	private String achievementCode;

	@Column(name = "unlocked_at", nullable = false)
	private OffsetDateTime unlockedAt;

	protected UserAchievement() {
	}

	public UserAchievement(User user, String achievementCode, OffsetDateTime unlockedAt) {
		this.user = user;
		this.achievementCode = achievementCode;
		this.unlockedAt = unlockedAt;
	}

	public Long getId() {
		return id;
	}

	public String getAchievementCode() {
		return achievementCode;
	}

	public OffsetDateTime getUnlockedAt() {
		return unlockedAt;
	}
}
