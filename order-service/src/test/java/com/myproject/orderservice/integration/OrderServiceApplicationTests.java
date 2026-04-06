package com.myproject.orderservice.integration;


import com.myproject.orderservice.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.testcontainers.containers.MySQLContainer;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceApplicationTests {

    @MockBean(name = "kafkaTemplate")
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.3.0");

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        reset(kafkaTemplate);

        CompletableFuture<SendResult<String, OrderPlacedEvent>> future = new CompletableFuture<>();
        future.complete(null);

        when(kafkaTemplate.send(anyString(), any(OrderPlacedEvent.class))).thenReturn(future);
    }

    static {
        mySQLContainer.start();
    }

    @Test
    void shouldSubmitOrder() {
        String submitOrderJson = """
                {
                     "skuCode": "iphone_15",
                     "price": 1000,
                     "quantity": 1,
                     "userDetails": {
                                      "email": "test@example.com",
                                      "firstName": "John",
                                      "lastName": "Doe"
                                  }
                }
                """;
        InventoryClientStub.stubInventoryCall("iphone_15", 1);

        RestAssured.given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(201)
                .body(Matchers.anyOf(
                        equalTo("Order Placed Successfully"),
                        containsString("Order placed successfully")
                ));
    }

    @Test
    void shouldReturnConflictWhenProductIsNotInStock() {
        String submitOrderJson = """
                {
                     "skuCode": "iphone_15",
                     "price": 1000,
                     "quantity": 1000,
                     "userDetails": {
                                      "email": "test@example.com",
                                      "firstName": "John",
                                      "lastName": "Doe"
                                  }
                }
                """;
        InventoryClientStub.stubInventoryCall("iphone_15", 1000);

        RestAssured.given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(409)
                .body("status", equalTo(409))
                .body("message", containsString("iphone_15"))
                .body("path", equalTo("/api/order"));
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsInvalid() {
        String invalidOrderJson = """
                {
                  "skuCode": "",
                  "price": -10,
                  "quantity": 0,
                  "userDetails": {
                    "email": "not-an-email",
                    "firstName": "",
                    "lastName": ""
                  }
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(invalidOrderJson)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(400);
    }

    @Test
    void shouldReturnServiceUnavailableWhenInventoryServiceIsDown() {
        String submitOrderJson = """
                {
                  "skuCode": "iphone_15",
                  "price": 1000,
                  "quantity": 1,
                  "userDetails": {
                    "email": "test@example.com",
                    "firstName": "John",
                    "lastName": "Doe"
                  }
                }
                """;

        InventoryClientStub.stubInventoryServiceUnavailable();

        RestAssured.given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(503)
                .body("status", equalTo(503))
                .body("path", equalTo("/api/order"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenKafkaPublicationFails() {
        String submitOrderJson = """
                {
                  "skuCode": "iphone_15",
                  "price": 1000,
                  "quantity": 1,
                  "userDetails": {
                    "email": "test@example.com",
                    "firstName": "John",
                    "lastName": "Doe"
                  }
                }
                """;

        InventoryClientStub.stubInventoryCall("iphone_15", 1);

        CompletableFuture<SendResult<String, OrderPlacedEvent>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka send failed"));

        when(kafkaTemplate.send(anyString(), any(OrderPlacedEvent.class))).thenReturn(failedFuture);

        RestAssured.given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(500)
                .body("status", equalTo(500))
                .body("path", equalTo("/api/order"));
    }
}
