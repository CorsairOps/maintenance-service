package com.corsairops.maintenance;

import com.corsairops.maintenance.dto.MaintenanceOrderRequest;
import com.corsairops.maintenance.dto.MaintenanceOrderResponse;
import com.corsairops.maintenance.model.OrderStatus;
import com.corsairops.maintenance.repository.MaintenanceOrderRepository;
import com.corsairops.shared.client.AssetServiceClient;
import com.corsairops.shared.client.UserServiceClient;
import com.corsairops.shared.dto.User;
import com.corsairops.shared.dto.asset.AssetResponse;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static com.corsairops.shared.dto.asset.AssetStatus.*;
import static com.corsairops.shared.dto.asset.AssetType.*;
import static java.time.LocalDateTime.*;
import static java.util.UUID.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public class MaintenanceOrderIntegrationTests {
    private static final String VALID_ASSET_ID = "123e4567-e89b-12d3-a456-426614174000";
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
    private MaintenanceOrderRepository maintenanceOrderRepository;

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
    }

    @AfterEach
    void cleanup() {
        Mockito.reset(assetServiceClient);
        Mockito.reset(userServiceClient);
        maintenanceOrderRepository.deleteAll();
    }

    @Test
    void givenInvalidRequest_whenCreateOrder_thenBadRequest() {
        var request = new MaintenanceOrderRequest(null, "", OrderStatus.COMPLETED, -10);

        jsonRequest(request)
                .header("X-User-Id", VALID_USER_ID)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    void givenExistingAssetOrder_whenCreateOrder_thenConflict() {
        var request = new MaintenanceOrderRequest(VALID_ASSET_ID, "Routine check", OrderStatus.PENDING, 5);
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
        var request = new MaintenanceOrderRequest(VALID_ASSET_ID, "Routine check", OrderStatus.PENDING, 5);
        createOrder(request);
    }

    private MaintenanceOrderResponse createOrder(MaintenanceOrderRequest request) {
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
                .as(MaintenanceOrderResponse.class);
    }

    private RequestSpecification jsonRequest() {
        return given().contentType("application/json");
    }

    private RequestSpecification jsonRequest(Object body) {
        return jsonRequest().body(body);
    }

}