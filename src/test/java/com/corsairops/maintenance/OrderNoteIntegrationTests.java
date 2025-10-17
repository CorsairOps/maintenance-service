package com.corsairops.maintenance;

import com.corsairops.maintenance.dto.OrderNoteRequest;
import com.corsairops.maintenance.dto.OrderNoteResponse;
import com.corsairops.maintenance.model.Order;
import com.corsairops.maintenance.model.OrderStatus;
import com.corsairops.maintenance.repository.OrderRepository;
import com.corsairops.shared.client.UserServiceClient;
import com.corsairops.shared.dto.User;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static java.time.LocalDateTime.*;
import static java.util.UUID.*;
import static org.hamcrest.Matchers.*;

import static com.corsairops.maintenance.RestAssuredUtil.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public class OrderNoteIntegrationTests {

    private static final User MOCK_USER = new User("tech1",
            "Tech One",
            "tech1@email.com",
            "Tech",
            "1",
            true,
            null,
            List.of("TECHNICIAN"));

    @LocalServerPort
    int port;

    @Autowired
    private OrderRepository orderRepository;

    private Order mockOrder;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/maintenance/orders";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        // Add mock order
        var sampleOrder = Order.builder()
                .assetId(randomUUID().toString())
                .description("Engine check")
                .status(OrderStatus.PENDING)
                .priority(2)
                .placedBy("planner 1")
                .completedBy(null)
                .createdAt(now())
                .updatedAt(now())
                .build();
        mockOrder = orderRepository.save(sampleOrder);
        Mockito.when(userServiceClient.getUserById(MOCK_USER.id()))
                .thenReturn(MOCK_USER);
        Mockito.when(userServiceClient.getUsersByIds(Mockito.anyString(), Mockito.anyBoolean()))
                .thenReturn(List.of(MOCK_USER));
    }

    @Test
    void givenInvalidRequest_whenAddNote_thenBadRequest() {
        var invalidNoteRequest = new OrderNoteRequest("");
        jsonRequest(invalidNoteRequest)
                .header("X-User-Id", MOCK_USER.id())
                .when()
                .post("/{orderId}/notes", mockOrder.getId())
                .then()
                .statusCode(400);
    }

    @Test
    void givenValidRequest_whenAddNote_thenNoteAdded() {
        addNoteToOrder(mockOrder.getId(), "Replaced oil filter", "tech1");
    }

    @Test
    void givenInvalidOrderId_whenAddNote_thenNotFound() {
        var noteRequest = new OrderNoteRequest("Checked brakes");
        jsonRequest(noteRequest)
                .header("X-User-Id", MOCK_USER.id())
                .when()
                .post("/{orderId}/notes", 9999L)
                .then()
                .statusCode(404);
    }

    @Test
    void givenNoExistingOrders_whenGetNotes_thenEmptyList() {
        jsonRequest()
                .when()
                .get("/{orderId}/notes", mockOrder.getId())
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    void givenExistingOrders_whenGetNotes_thenReturnNotes() {
        addNoteToOrder(mockOrder.getId(), "Checked landing gear", "tech1");
        addNoteToOrder(mockOrder.getId(), "Replaced hydraulic fluid", "tech1");

        jsonRequest()
                .when()
                .get("/{orderId}/notes", mockOrder.getId())
                .then()
                .statusCode(200)
                .body("", hasSize(2))
                .body("[0].note", equalTo("Replaced hydraulic fluid"))
                .body("[1].note", equalTo("Checked landing gear"));
    }

    @Test
    void givenInvalidOrderId_whenDeleteNote_thenNotFound() {
        var invalidOrderId = 9999L;
        var noteId = 1L;

        jsonRequest()
                .when()
                .delete("/{orderId}/notes/{noteId}", invalidOrderId, noteId)
                .then()
                .statusCode(404);
    }

    @Test
    void givenInvalidNoteId_whenDeleteNote_thenNotFound() {
        var validOrderId = mockOrder.getId();
        var invalidNoteId = 9999L;

        jsonRequest()
                .when()
                .delete("/{orderId}/notes/{noteId}", validOrderId, invalidNoteId)
                .then()
                .statusCode(404);
    }

    @Test
    void givenValidIds_whenDeleteNote_thenDeleted() {
        var noteResponse = addNoteToOrder(mockOrder.getId(), "Checked avionics", "tech1");
        var noteId = noteResponse.id();

        jsonRequest()
                .when()
                .delete("/{orderId}/notes/{noteId}", mockOrder.getId(), noteId)
                .then()
                .statusCode(204);

        // Verify note is deleted
        jsonRequest()
                .when()
                .get("/{orderId}/notes", mockOrder.getId())
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    private OrderNoteResponse addNoteToOrder(Long orderId, String noteContent, String addedBy) {
        var noteRequest = new OrderNoteRequest(noteContent);
        return jsonRequest(noteRequest)
                .header("X-User-Id", addedBy)
                .when()
                .post("/{orderId}/notes", mockOrder.getId())
                .then()
                .statusCode(201)
                .body("note", equalTo(noteContent))
                .body("orderId", equalTo(orderId.intValue()))
                .body("createdBy.id", equalTo(addedBy))
                .extract()
                .as(OrderNoteResponse.class);
    }


}