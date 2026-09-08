package com.deutschlingodeck.game.engine;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class GameEngineConfig {

	@Bean
	public Random gameEngineRandom() {
		return new Random();
	}
}
