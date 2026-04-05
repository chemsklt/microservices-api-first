package com.myproject.orderservice.stubs;


import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class InventoryClientStub {

    public static void stubInventoryCall(String skuCode, Integer quantity) {
        boolean inStock = quantity <= 100;
            stubFor(get(urlPathEqualTo("/api/inventory"))
                    .withQueryParam("skuCode", equalTo(skuCode))
                    .withQueryParam("quantity", equalTo(String.valueOf(quantity)))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                  "skuCode": "%s",
                                  "quantity": %d,
                                  "inStock": %s
                                }
                                """.formatted(skuCode, quantity, inStock))));
    }

    public static void stubInventoryServiceUnavailable() {
        stubFor(get(urlPathEqualTo("/api/inventory"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "message": "Inventory service unavailable"
                                }
                                """)));
    }
}