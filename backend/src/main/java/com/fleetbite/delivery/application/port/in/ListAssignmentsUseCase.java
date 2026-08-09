package com.fleetbite.delivery.application.port.in;

import com.fleetbite.delivery.application.dto.AssignmentResult;

import java.util.List;

public interface ListAssignmentsUseCase {

	List<AssignmentResult> execute();
}
