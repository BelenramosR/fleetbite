package com.fleetbite.shared.infrastructure.config;

import com.fleetbite.shared.domain.time.BusinessTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfig {

	@Bean
	Clock clock() {
		return Clock.system(BusinessTime.ZONE_OFFSET);
	}
}
