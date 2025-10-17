package com.corsairops.maintenance;

import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.*;

public class RestAssuredUtil {

    public static RequestSpecification jsonRequest() {
        return given()
                .contentType("application/json");
    }

    public static RequestSpecification jsonRequest(Object body) {
        return jsonRequest()
                .body(body);
    }
}