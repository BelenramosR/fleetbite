package com.fleetbite.delivery.application.policy;

import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.order.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface DriverSelectionPolicy {

	Optional<DriverCandidate> select(Order order, List<Driver> availableDrivers);
}
