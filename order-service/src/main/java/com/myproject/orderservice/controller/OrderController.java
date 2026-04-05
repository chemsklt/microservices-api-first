package com.myproject.orderservice.controller;


import com.myproject.orderservice.generated.api.OrdersApi;
import com.myproject.orderservice.generated.model.OrderCreatedResponse;
import com.myproject.orderservice.generated.model.OrderRequest;
import com.myproject.orderservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderController implements OrdersApi {

    private final OrderService orderService;

    @Override
    public ResponseEntity<OrderCreatedResponse> placeOrder(OrderRequest orderRequest) {
        String orderNumber = orderService.placeOrder(orderRequest);

        OrderCreatedResponse response = new OrderCreatedResponse()
                .orderNumber(orderNumber)
                .message("Order placed successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
