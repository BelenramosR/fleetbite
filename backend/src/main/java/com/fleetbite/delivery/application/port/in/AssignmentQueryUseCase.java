package com.fleetbite.delivery.application.port.in;

import com.fleetbite.delivery.application.dto.AssignmentResult;

import java.util.List;
import java.util.UUID;

public interface AssignmentQueryUseCase {

	AssignmentResult getById(UUID assignmentId);

	List<AssignmentResult> findAll();
}
