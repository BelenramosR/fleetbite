package com.fleetbite.infrastructure.config;

import com.fleetbite.shared.domain.model.Location;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RestaurantLocationProperties.class)
public class RestaurantLocationConfig {

	@Bean
	@Qualifier("restaurantLocation")
	Location restaurantLocation(RestaurantLocationProperties properties) {
		return new Location(properties.latitude(), properties.longitude());
	}
}
