package com.example.userservice.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.userservice.vo.ResponseOrder;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

	@GetMapping("/{userId}/orders")
	List<ResponseOrder> getOrders(@PathVariable("userId") String userId);
}
