package com.corsairops.maintenance;

import com.corsairops.maintenance.dto.OrderRequest;
import com.corsairops.maintenance.exception.OpenOrderExistsException;
import com.corsairops.maintenance.exception.OrderNotFoundException;
import com.corsairops.maintenance.model.Order;
import com.corsairops.maintenance.model.OrderStatus;
import com.corsairops.maintenance.repository.OrderRepository;
import com.corsairops.maintenance.service.OrderService;
import com.corsairops.shared.client.AssetServiceClient;
import com.corsairops.shared.dto.asset.AssetResponse;
import com.corsairops.shared.dto.asset.AssetStatus;
import com.corsairops.shared.dto.asset.AssetType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public class OrderServiceTests {

    private static final String INVALID_ASSET_ID = "96f7c53a-2f1d-47fe-b3f5-eaa6b46372da";

    private static final String VALID_ASSET_ID = "123e4567-e89b-12d3-a456-426614174000";

    private static final AssetResponse VALID_ASSET = new AssetResponse(
            UUID.fromString(VALID_ASSET_ID),
            "Tank A",
            AssetType.GROUND_VEHICLE,
            AssetStatus.ACTIVE,
            90.0,
            90.0,
            LocalDateTime.now().minusDays(10),
            LocalDateTime.now().minusDays(1)
    );

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private AssetServiceClient assetServiceClient;

    @BeforeEach
    void setup() {
        when(assetServiceClient.getAssetById(UUID.fromString(VALID_ASSET_ID)))
                .thenReturn(VALID_ASSET);

        doThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404), "Not Found"))
                .when(assetServiceClient)
                .getAssetById(UUID.fromString(INVALID_ASSET_ID));
    }

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
    }

    @Test
    void givenInvalidAssetId_whenCreateOrder_thenThrowException() {
        var invalidOrderRequest = new OrderRequest(
                INVALID_ASSET_ID,
                "Fix the leaking pipe",
                OrderStatus.PENDING,
                1
        );

        assertThrows(HttpClientErrorException.class, () -> {
            orderService.createOrder(invalidOrderRequest, null);
        });
    }

    @Test
    void givenExistingOpenOrder_whenCreateOrder_thenThrowException() {
        createExistingOrder();

        OrderRequest request = new OrderRequest(
                VALID_ASSET_ID,
                "New order attempt",
                OrderStatus.PENDING,
                2
        );

        assertThrows(OpenOrderExistsException.class, () -> {
            orderService.createOrder(request, null);
        });
    }

    @Test
    void givenValidOrderRequest_whenCreateOrder_thenReturnOrder() {
        OrderRequest request = new OrderRequest(
                VALID_ASSET_ID,
                "Routine Maintenance",
                OrderStatus.PENDING,
                2
        );

        Order order = orderService.createOrder(request, "test-user");

        assertThat(order.getId(), notNullValue());
        assertThat(order.getAssetId(), equalTo(VALID_ASSET_ID));
        assertThat(order.getDescription(), equalTo("Routine Maintenance"));
        assertThat(order.getStatus(), equalTo(OrderStatus.PENDING));
        assertThat(order.getPriority(), equalTo(2));
        assertThat(order.getPlacedBy(), equalTo("test-user"));
    }

    @Test
    void whenGetAllOrders_thenReturnOrderList() {
        createExistingOrder();
        List<Order> orders = orderService.getAllOrders();

        assertThat(orders, Matchers.hasSize(1));
        assertThat(orders.getFirst().getAssetId(), equalTo(VALID_ASSET_ID));
    }

    @Test
    void whenGetAllOrdersByAssetId_thenReturnOrderList() {
        createExistingOrder();
        List<Order> orders = orderService.getAllOrders(VALID_ASSET_ID);

        assertThat(orders, Matchers.hasSize(1));
        assertThat(orders.getFirst().getAssetId(), equalTo(VALID_ASSET_ID));
    }

    @Test
    void whenGetAllOrdersByNonExistingAssetId_thenReturnEmptyList() {
        createExistingOrder();
        List<Order> orders = orderService.getAllOrders(INVALID_ASSET_ID);

        assertThat(orders, Matchers.hasSize(0));
    }

    @Test
    void givenInvalidOrderId_whenGetOrderById_thenThrowException() {
        assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrderById(999L);
        });
    }

    @Test
    void givenValidOrderId_whenGetOrderById_thenReturnOrder() {
        var existingOrder = createExistingOrder();

        var fetchedOrder = orderService.getOrderById(existingOrder.getId());

        assertThat(fetchedOrder.getId(), equalTo(existingOrder.getId()));
        assertThat(fetchedOrder.getAssetId(), equalTo(VALID_ASSET_ID));
        assertThat(fetchedOrder.getDescription(), equalTo("Existing order"));
        assertThat(fetchedOrder.getStatus(), equalTo(OrderStatus.PENDING));
        assertThat(fetchedOrder.getPriority(), equalTo(3));
    }

    @Test
    void givenInvalidRequest_whenUpdateOrder_thenThrowOrderNotFoundException() {
        assertThrows(OrderNotFoundException.class, () -> {
            orderService.updateOrder(333L, new OrderRequest(
                    "some-asset-id",
                    "Some description",
                    OrderStatus.PENDING,
                    2
            ));
        });
    }

    @Test
    void givenValidRequest_whenUpdateOrder_thenReturnUpdatedOrder() {
        var existingOrder = createExistingOrder();

        var updateRequest = new OrderRequest(
                VALID_ASSET_ID,
                "Updated description",
                OrderStatus.IN_PROGRESS,
                1
        );

        var updatedOrder = orderService.updateOrder(existingOrder.getId(), updateRequest);
        assertThat(updatedOrder.getId(), equalTo(existingOrder.getId()));
        assertThat(updatedOrder.getAssetId(), equalTo(VALID_ASSET_ID));
        assertThat(updatedOrder.getDescription(), equalTo("Updated description"));
        assertThat(updatedOrder.getStatus(), equalTo(OrderStatus.IN_PROGRESS));
        assertThat(updatedOrder.getPriority(), equalTo(1));
    }

    @Test
    void givenInvalidId_whenDeleteOrder_thenThrowOrderNotFoundException() {
        assertThrows(OrderNotFoundException.class, () -> {
            orderService.deleteOrder(444L);
        });
    }

    @Test
    void givenValidId_whenDeleteOrder_thenOrderIsDeleted() {
        var existingOrder = createExistingOrder();
        long orderId = existingOrder.getId();
        orderService.deleteOrder(existingOrder.getId());
        assertFalse(orderRepository.existsById(orderId));
    }

    private Order createExistingOrder() {
        // Create existing order
        Order order = Order.builder()
                .assetId(VALID_ASSET_ID)
                .description("Existing order")
                .status(OrderStatus.PENDING)
                .priority(3)
                .build();
        return orderRepository.save(order);
    }
}