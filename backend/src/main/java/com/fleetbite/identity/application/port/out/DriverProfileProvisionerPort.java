package com.fleetbite.identity.application.port.out;

import java.util.UUID;


/**
 * Cross-module hook: when a User with role DRIVER is created, provision the Driver profile.
 */
public interface DriverProfileProvisionerPort {

	void provisionForDriverUser(UUID userId);
}
