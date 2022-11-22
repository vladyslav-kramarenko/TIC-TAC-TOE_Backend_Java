package com.kramarenko;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.kramarenko.utils.CacheUtils;
import com.kramarenko.utils.RequestHelper;
import com.kramarenko.utils.WebSocketResponseUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import redis.clients.jedis.Jedis;

public class ConnectionFirstFunction implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

    private static final CacheUtils cache = new CacheUtils(System.getenv("HOST"),
            Integer.valueOf(System.getenv("PORT")));


    @Override
    public APIGatewayV2WebSocketResponse handleRequest(APIGatewayV2WebSocketEvent apiGatewayV2WebSocketEvent, Context context) {
        String id;
        String connectionId = apiGatewayV2WebSocketEvent.getRequestContext().getConnectionId();
        Jedis jedis = cache.getClient();

        LambdaLogger logger = context.getLogger();
        logger.log(String.format("WebSocket connection ID %s", connectionId));
        try {
            RequestHelper helper = new RequestHelper(apiGatewayV2WebSocketEvent.getBody(),
                    apiGatewayV2WebSocketEvent.getQueryStringParameters());
            id = helper.getParam("id");
        } catch (JsonProcessingException e) {
            logger.log(e.getMessage());
            return WebSocketResponseUtils.badResponse();
        }
        jedis.sadd(CacheUtils.PREFIX_CONNECTION_PLAYER + id, connectionId);
        jedis.close();
        return WebSocketResponseUtils.successfulResponse(String.format("{\"message\": \"Successful connection user with id: %s.\"}", id));
    }
}
