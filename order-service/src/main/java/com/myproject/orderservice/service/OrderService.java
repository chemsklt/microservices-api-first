package com.myproject.orderservice.service;

import com.myproject.orderservice.client.InventoryClient;
import com.myproject.orderservice.domain.Order;
import com.myproject.orderservice.dto.InventoryResponse;
import com.myproject.orderservice.event.OrderPlacedEvent;
import com.myproject.orderservice.exception.InventoryServiceUnavailableException;
import com.myproject.orderservice.exception.OrderProcessingException;
import com.myproject.orderservice.exception.ProductOutOfStockException;
import com.myproject.orderservice.generated.model.OrderRequest;
import com.myproject.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final String ORDER_PLACED_TOPIC = "order-placed";

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Transactional
    public String placeOrder(OrderRequest orderRequest) {
        log.info("Starting order placement for skuCode={}, quantity={}",
                orderRequest.getSkuCode(), orderRequest.getQuantity());

        InventoryResponse inventoryResponse = checkInventory(orderRequest);

        if (!Boolean.TRUE.equals(inventoryResponse.inStock())) {
            throw new ProductOutOfStockException(orderRequest.getSkuCode());
        }

        Order order = buildOrder(orderRequest);
        orderRepository.save(order);

        publishOrderPlacedEvent(order, orderRequest);

        log.info("Order placed successfully with orderNumber={}", order.getOrderNumber());
        return order.getOrderNumber();
    }

    private InventoryResponse checkInventory(OrderRequest orderRequest) {
        try {
            InventoryResponse response =
                    inventoryClient.isInStock(orderRequest.getSkuCode(), orderRequest.getQuantity());

            if (response == null) {
                throw new InventoryServiceUnavailableException(
                        "Inventory service returned no response");
            }

            return response;
        } catch (InventoryServiceUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InventoryServiceUnavailableException(
                    "Failed to check inventory availability", ex);
        }
    }

    private Order buildOrder(OrderRequest orderRequest) {
        BigDecimal totalPrice = BigDecimal.valueOf(orderRequest.getPrice())
                .multiply(BigDecimal.valueOf(orderRequest.getQuantity()));

        return Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .skuCode(orderRequest.getSkuCode())
                .price(totalPrice)
                .quantity(orderRequest.getQuantity())
                .build();
    }

    private void publishOrderPlacedEvent(Order order, OrderRequest orderRequest) {
        try {
            OrderPlacedEvent event = new OrderPlacedEvent();
            event.setOrderNumber(order.getOrderNumber());
            event.setEmail(orderRequest.getUserDetails().getEmail());
            event.setFirstName(orderRequest.getUserDetails().getFirstName());
            event.setLastName(orderRequest.getUserDetails().getLastName());

            log.info("Publishing OrderPlacedEvent for orderNumber={} to topic={}",
                    order.getOrderNumber(), ORDER_PLACED_TOPIC);

            kafkaTemplate.send(ORDER_PLACED_TOPIC, event).join();

        } catch (Exception ex) {
            throw new OrderProcessingException(
                    "Order was saved but event publication failed", ex);
        }
    }

//    public void placeOrder(OrderRequest orderRequest) {
//
//        var inventoryResponse = inventoryClient.isInStock(orderRequest.getSkuCode(), orderRequest.getQuantity());
//        boolean isProductInStock = inventoryResponse != null && Boolean.TRUE.equals(inventoryResponse.inStock());
//        if (isProductInStock) {
//            Order order = new Order();
//            order.setOrderNumber(UUID.randomUUID().toString());
//            order.setPrice(BigDecimal.valueOf(orderRequest.getPrice()).multiply(BigDecimal.valueOf(orderRequest.getQuantity())));
//            order.setSkuCode(orderRequest.getSkuCode());
//            order.setQuantity(orderRequest.getQuantity());
//            orderRepository.save(order);
//
//            // Send the message to Kafka Topic
//            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
//            orderPlacedEvent.setOrderNumber(order.getOrderNumber());
//            orderPlacedEvent.setEmail(orderRequest.getUserDetails().getEmail());
//            orderPlacedEvent.setFirstName(orderRequest.getUserDetails().getFirstName());
//            orderPlacedEvent.setLastName(orderRequest.getUserDetails().getLastName());
//            log.info("Start - Sending OrderPlacedEvent {} to Kafka topic order-placed", orderPlacedEvent);
//            kafkaTemplate.send("order-placed", orderPlacedEvent);
//            log.info("End - Sending OrderPlacedEvent {} to Kafka topic order-placed", orderPlacedEvent);
//        } else {
//            throw new RuntimeException("Product with SkuCode " + orderRequest.getSkuCode() + " is not in stock");
//        }
//    }
}
