package com.myproject.orderservice.service;

import com.myproject.orderservice.client.InventoryClient;
import com.myproject.orderservice.domain.Order;
import com.myproject.orderservice.dto.InventoryResponse;
import com.myproject.orderservice.event.OrderPlacedEvent;
import com.myproject.orderservice.exception.InventoryServiceUnavailableException;
import com.myproject.orderservice.exception.OrderProcessingException;
import com.myproject.orderservice.exception.ProductOutOfStockException;
import com.myproject.orderservice.generated.model.OrderRequest;
import com.myproject.orderservice.generated.model.UserDetails;
import com.myproject.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String ORDER_PLACED_TOPIC = "order-placed";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        UserDetails userDetails = new UserDetails()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe");

        orderRequest = new OrderRequest()
                .skuCode("iphone_15")
                .price(1000.0)
                .quantity(2)
                .userDetails(userDetails);
    }

    @Test
    void shouldPlaceOrderSuccessfully() {
        // given
        InventoryResponse inventoryResponse = new InventoryResponse("iphone_15", 2, true);
        when(inventoryClient.isInStock("iphone_15", 2)).thenReturn(inventoryResponse);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<SendResult<String, OrderPlacedEvent>> sendFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(ORDER_PLACED_TOPIC), any(OrderPlacedEvent.class))).thenReturn(sendFuture);

        // when
        String orderNumber = orderService.placeOrder(orderRequest);

        // then
        assertNotNull(orderNumber);
        assertFalse(orderNumber.isBlank());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertEquals("iphone_15", savedOrder.getSkuCode());
        assertEquals(2, savedOrder.getQuantity());
        assertEquals(BigDecimal.valueOf(2000.0), savedOrder.getPrice());
        assertNotNull(savedOrder.getOrderNumber());

        ArgumentCaptor<OrderPlacedEvent> eventCaptor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(kafkaTemplate).send(eq(ORDER_PLACED_TOPIC), eventCaptor.capture());

        OrderPlacedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(savedOrder.getOrderNumber(), publishedEvent.getOrderNumber().toString());
        assertEquals("test@example.com", publishedEvent.getEmail().toString());
        assertEquals("John", publishedEvent.getFirstName().toString());
        assertEquals("Doe", publishedEvent.getLastName().toString());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(inventoryClient, times(1)).isInStock("iphone_15", 2);
        verify(kafkaTemplate, times(1)).send(eq(ORDER_PLACED_TOPIC), any(OrderPlacedEvent.class));
    }

    @Test
    void shouldThrowProductOutOfStockExceptionWhenProductIsNotInStock() {
        // given
        InventoryResponse inventoryResponse = new InventoryResponse("iphone_15", 2, false);
        when(inventoryClient.isInStock("iphone_15", 2)).thenReturn(inventoryResponse);

        // when / then
        ProductOutOfStockException exception = assertThrows(
                ProductOutOfStockException.class,
                () -> orderService.placeOrder(orderRequest)
        );

        assertTrue(exception.getMessage().contains("iphone_15"));

        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(anyString(), any(OrderPlacedEvent.class));
    }

    @Test
    void shouldThrowInventoryServiceUnavailableExceptionWhenInventoryResponseIsNull() {
        // given
        when(inventoryClient.isInStock("iphone_15", 2)).thenReturn(null);

        // when / then
        InventoryServiceUnavailableException exception = assertThrows(
                InventoryServiceUnavailableException.class,
                () -> orderService.placeOrder(orderRequest)
        );

        assertEquals("Inventory service returned no response", exception.getMessage());
        assertNull(exception.getCause());

        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(anyString(), any(OrderPlacedEvent.class));
    }

    @Test
    void shouldThrowInventoryServiceUnavailableExceptionWhenInventoryClientFails() {
        // given
        when(inventoryClient.isInStock("iphone_15", 2))
                .thenThrow(new RuntimeException("Inventory service timeout"));

        // when / then
        InventoryServiceUnavailableException exception = assertThrows(
                InventoryServiceUnavailableException.class,
                () -> orderService.placeOrder(orderRequest)
        );

        assertEquals("Failed to check inventory availability", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Inventory service timeout", exception.getCause().getMessage());

        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(anyString(), any(OrderPlacedEvent.class));
    }

    @Test
    void shouldThrowOrderProcessingExceptionWhenKafkaPublicationFails() {
        // given
        InventoryResponse inventoryResponse = new InventoryResponse("iphone_15", 2, true);
        when(inventoryClient.isInStock("iphone_15", 2)).thenReturn(inventoryResponse);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<SendResult<String, OrderPlacedEvent>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka publish failed"));

        when(kafkaTemplate.send(eq(ORDER_PLACED_TOPIC), any(OrderPlacedEvent.class))).thenReturn(failedFuture);

        // when / then
        OrderProcessingException exception = assertThrows(
                OrderProcessingException.class,
                () -> orderService.placeOrder(orderRequest)
        );

        assertEquals("Order was saved but event publication failed", exception.getMessage());
        assertNotNull(exception.getCause());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaTemplate, times(1)).send(eq(ORDER_PLACED_TOPIC), any(OrderPlacedEvent.class));
    }
}