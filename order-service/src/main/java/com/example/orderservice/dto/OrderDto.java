package com.example.orderservice.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class OrderDto implements Serializable {
    private String productId;
    private Integer quantity;
    private Integer unitPrice;
    private Integer totalPrice;

    private String userId;
    private String orderId;
}
