package com.fleetbite.order.application.port.in;

import com.fleetbite.order.application.dto.OrderResult;

import java.util.List;

public interface ListOrdersUseCase {

	List<OrderResult> execute();
}
