package com.fleetbite.vehicle.application.port.out;

import java.util.UUID;

/** Required capability for checking whether a vehicle is owned by an active driver link. */
public interface VehicleAssignmentLookupPort {

	boolean isAssigned(UUID vehicleId);
}
