package com.fleetbite.infrastructure.config;

import com.fleetbite.shared.domain.model.Location;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantLocationConfigTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(RestaurantLocationConfig.class)
			.withPropertyValues(
					"fleetbite.restaurant.location.latitude=-12.0919738",
					"fleetbite.restaurant.location.longitude=-76.9737017");

	@Test
	void shouldExposeConfiguredRestaurantLocation() {
		contextRunner.run(context -> {
			Location location = context.getBean("restaurantLocation", Location.class);
			assertThat(location.latitude()).isEqualTo(-12.0919738);
			assertThat(location.longitude()).isEqualTo(-76.9737017);
		});
	}
}
