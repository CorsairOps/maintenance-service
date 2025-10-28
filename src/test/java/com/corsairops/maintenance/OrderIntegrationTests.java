package com.corsairops.maintenance;

import com.corsairops.maintenance.dto.OrderRequest;
import com.corsairops.maintenance.dto.OrderResponse;
import com.corsairops.maintenance.model.OrderStatus;
import com.corsairops.maintenance.repository.OrderRepository;
import com.corsairops.shared.client.AssetServiceClient;
import com.corsairops.shared.client.UserServiceClient;
import com.corsairops.shared.dto.User;
import com.corsairops.shared.dto.asset.AssetResponse;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static com.corsairops.shared.dto.asset.AssetStatus.*;
import static com.corsairops.shared.dto.asset.AssetType.*;
import static java.time.LocalDateTime.*;
import static java.util.UUID.*;
import static org.hamcrest.Matchers.*;
import static com.corsairops.maintenance.RestAssuredUtil.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public class OrderIntegrationTests {
    private static final String VALID_ASSET_ID = "123e4567-e89b-12d3-a456-426614174000";
    private static final String INVALID_ASSET_ID = "00000000-0000-0000-0000-000000000000";

    private static final AssetResponse MOCK_ASSET = new AssetResponse(
            fromString(VALID_ASSET_ID),
            "Tank A",
            GROUND_VEHICLE,
            ACTIVE,
            90.0,
            90.0,
            now().minusDays(10),
            now().minusDays(1)
    );

    private static final String VALID_USER_ID = "96f7c53a-2f1d-47fe-b3f5-eaa6b46372da";
    private static final User MOCK_USER = new User(
            VALID_USER_ID,
            "jdoe",
            "jdoe@email.com",
            "John",
            "Doe",
            true,
            now().minusDays(30).toEpochSecond(ZoneOffset.UTC),
            List.of("PLANNER")
    );

    @LocalServerPort
    private int port;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private AssetServiceClient assetServiceClient;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/maintenance/orders";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        Mockito.when(assetServiceClient.getAssetById(UUID.fromString(VALID_ASSET_ID)))
                .thenReturn(MOCK_ASSET);
        Mockito.when(userServiceClient.getUsersByIds(VALID_USER_ID, true))
                .thenReturn(List.of(MOCK_USER));
        Mockito.when(assetServiceClient.getAssetById(UUID.fromString(INVALID_ASSET_ID)))
                .thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404)));
    }

    @AfterEach
    void cleanup() {
        Mockito.reset(assetServiceClient);
        Mockito.reset(userServiceClient);
        orderRepository.deleteAll();
    }

    @Test
    void givenInvalidRequest_whenCreateOrder_thenBadRequest() {
        var request = new OrderRequest(null, "", OrderStatus.COMPLETED, -10);

        jsonRequest(request)
                .header("X-User-Id", VALID_USER_ID)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    void givenExistingAssetOrder_whenCreateOrder_thenConflict() {
        var request = new OrderRequest(VALID_ASSET_ID, "Routine check", OrderStatus.PENDING, 5);
        createOrder(request);

        jsonRequest(request)
                .header("X-User-Id", VALID_USER_ID)
                .when()
                .post()
                .then()
                .statusCode(409);
    }

    @Test
    void givenValidRequest_whenCreateOrder_thenCreated() {
        var request = new OrderRequest(VALID_ASSET_ID, "Routine check", OrderStatus.PENDING, 5);
        createOrder(request);
    }

    @Test
    void givenNonExistingAssetId_whenCreateOrder_thenNotFound() {
        var request = new OrderRequest(INVALID_ASSET_ID, "Routine check", OrderStatus.PENDING, 5);

        jsonRequest(request)
                .header("X-User-Id", VALID_USER_ID)
                .when()
                .post()
                .then()
                .statusCode(404);
    }

    @Test
    void givenNoOrders_whenGetOrders_thenEmptyList() {
        jsonRequest()
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    void givenOrders_whenGetOrders_thenListOfOrders() {
        var request1 = new OrderRequest(VALID_ASSET_ID, "Routine check", OrderStatus.PENDING, 5);
        var request2 = new OrderRequest("223e4567-e89b-12d3-a456-426614174000", "Engine repair", OrderStatus.IN_PROGRESS, 4);
        createOrder(request1);
        Mockito.when(assetServiceClient.getAssetById(UUID.fromString("223e4567-e89b-12d3-a456-426614174000")))
                .thenReturn(new AssetResponse(
                        fromString("223e4567-e89b-12d3-a456-426614174000"),
                        "Truck B",
                        GROUND_VEHICLE,
                        ACTIVE,
                        80.0,
                        80.0,
                        now().minusDays(20),
                        now().minusDays(2)
                ));
        createOrder(request2);

        jsonRequest()
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("", hasSize(2))
                .body("asset.id", hasItems(VALID_ASSET_ID, "223e4567-e89b-12d3-a456-426614174000"));
    }

    @Test
    void givenOrders_whenGetOrdersByAssetId_thenListOfOrders() {
        var request1 = new OrderRequest(VALID_ASSET_ID, "Routine check", OrderStatus.PENDING, 5);
        var request2 = new OrderRequest("223e4567-e89b-12d3-a456-426614174000", "Engine repair", OrderStatus.IN_PROGRESS, 4);
        createOrder(request1);
        Mockito.when(assetServiceClient.getAssetById(UUID.fromString("223e4567-e89b-12d3-a456-426614174000")))
                .thenReturn(new AssetResponse(
                        fromString("223e4567-e89b-12d3-a456-426614174000"),
                        "Truck B",
                        GROUND_VEHICLE,
                        ACTIVE,
                        80.0,
                        80.0,
                        now().minusDays(20),
                        now().minusDays(2)
                ));
        createOrder(request2);

        jsonRequest()
                .queryParam("assetId", VALID_ASSET_ID)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("asset.id", hasItem(VALID_ASSET_ID));
    }

    @Test
    void givenInvalidId_whenGetOrderById_thenNotFound() {
        var invalidId = 999L;
        jsonRequest()
                .when()
                .get("/{id}", invalidId)
                .then()
                .statusCode(404);
    }

    @Test
    void givenValidId_whenGetOrderById_thenOrder() {
        var request = new OrderRequest(VALID_ASSET_ID, "Routine Check", OrderStatus.PENDING, 3);
        var createdOrder = createOrder(request);
        Long id = createdOrder.id();

        jsonRequest()
                .when()
                .get("/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(Integer.parseInt(id.toString())))
                .body("asset.id", equalTo(VALID_ASSET_ID))
                .body("description", equalTo("Routine Check"))
                .body("status", equalTo("PENDING"))
                .body("priority", equalTo(3));
    }

    @Test
    void givenInvalidId_whenUpdateOrder_thenNotFound() {
        var invalidId = 4499999L;

        var request = new OrderRequest(VALID_ASSET_ID, "Routine check", OrderStatus.PENDING, 5);

        jsonRequest(request)
                .header("X-User-Id", VALID_USER_ID)
                .when()
                .put("/{id}", invalidId)
                .then()
                .statusCode(404);
    }

    @Test
    void givenInvalidRequest_whenUpdateOrder_thenBadRequest() {
        var request = new OrderRequest(VALID_ASSET_ID, "Routine check", OrderStatus.PENDING, 5);
        var createdOrder = createOrder(request);
        Long id = createdOrder.id();

        var invalidRequest = new OrderRequest(null, "", OrderStatus.COMPLETED, -10);

        jsonRequest(invalidRequest)
                .header("X-User-Id", VALID_USER_ID)
                .when()
                .put("/{id}", id)
                .then()
                .statusCode(400);
    }

    @Test
    void givenValidRequest_whenUpdateOrder_thenUpdated() {
        var request = new OrderRequest(VALID_ASSET_ID, "Routine check", OrderStatus.PENDING, 5);
        var createdOrder = createOrder(request);
        Long id = createdOrder.id();

        var updateRequest = new OrderRequest(VALID_ASSET_ID, "Engine repair", OrderStatus.IN_PROGRESS, 4);

        jsonRequest(updateRequest)
                .header("X-User-Id", VALID_USER_ID)
                .when()
                .put("/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(Integer.parseInt(id.toString())))
                .body("asset.id", equalTo(VALID_ASSET_ID))
                .body("description", equalTo("Engine repair"))
                .body("status", equalTo("IN_PROGRESS"))
                .body("priority", equalTo(4));
    }


    private OrderResponse createOrder(OrderRequest request) {
        return jsonRequest(request)
                .header("X-User-Id", VALID_USER_ID)
                .when()
                .post()
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("asset.id", equalTo(request.assetId()))
                .body("description", equalTo(request.description()))
                .body("status", equalTo(request.status().name()))
                .body("priority", equalTo(request.priority()))
                .extract()
                .as(OrderResponse.class);
    }

    @Test
    void givenInvalidId_whenDeleteOrder_thenNotFound() {
        var invalidId = 999L;
        jsonRequest()
                .header("X-User-Id", VALID_USER_ID)
                .when()
                .delete("/{id}", invalidId)
                .then()
                .statusCode(404);
    }

    @Test
    void givenValidId_whenDeleteOrder_thenNoContent() {
        var request = new OrderRequest(VALID_ASSET_ID, "Routine Check", OrderStatus.PENDING, 3);
        var createdOrder = createOrder(request);
        Long id = createdOrder.id();

        jsonRequest()
                .header("X-User-Id", VALID_USER_ID)
                .when()
                .delete("/{id}", id)
                .then()
                .statusCode(204);

        // Verify deletion
        jsonRequest()
                .when()
                .get("/{id}", id)
                .then()
                .statusCode(404);
    }

}