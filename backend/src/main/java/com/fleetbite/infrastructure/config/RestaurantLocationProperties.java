package com.fleetbite.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fleetbite.restaurant.location")
public record RestaurantLocationProperties(double latitude, double longitude) {
}
