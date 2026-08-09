package com.fleetbite.identity.application.port.out;

import com.fleetbite.identity.domain.model.UserId;

/**
 * Cross-module hook: when a User with role DRIVER is created, provision the Driver profile.
 */
public interface DriverProfileProvisionerPort {

	void provisionForDriverUser(UserId userId);
}
