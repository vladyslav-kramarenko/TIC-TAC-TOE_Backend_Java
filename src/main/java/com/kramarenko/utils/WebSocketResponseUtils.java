package com.kramarenko.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public class WebSocketResponseUtils {

    public static APIGatewayV2WebSocketResponse badResponse() {
        APIGatewayV2WebSocketResponse response = new APIGatewayV2WebSocketResponse();
        response.setStatusCode(400);
        response.setBody("{\"message\": \"Body incorrect.\"}");
        return response;
    }

    public static APIGatewayV2WebSocketResponse badResponseServer() {
        APIGatewayV2WebSocketResponse response = new APIGatewayV2WebSocketResponse();
        response.setStatusCode(500);
        response.setBody("{\"message\": \"Server bad feel.\"}");
        return response;
    }

    public static APIGatewayV2WebSocketResponse badResponse(String message) throws JsonProcessingException {
        APIGatewayV2WebSocketResponse response = new APIGatewayV2WebSocketResponse();
        response.setStatusCode(400);
        response.setBody(RequestHelper.toJson(message));
        return response;
    }

    public static APIGatewayV2WebSocketResponse successfulResponse(String message) {
        APIGatewayV2WebSocketResponse response = new APIGatewayV2WebSocketResponse();
        response.setStatusCode(200);
        response.setBody(message);
        return response;
    }
}
