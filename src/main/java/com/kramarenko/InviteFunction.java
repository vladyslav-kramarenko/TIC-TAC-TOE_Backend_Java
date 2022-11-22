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
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class InviteFunction implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {
    private static final CacheUtils cache = new CacheUtils(System.getenv("HOST"),
            Integer.valueOf(System.getenv("PORT")));

    private final ApiGatewayManagementApiAsyncClient client = ApiGatewayManagementApiAsyncClient.builder()
            .endpointOverride(new URI(System.getenv("ENDPOINT")))
            .region(Region.of(System.getenv("REGION")))
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .httpClient(AwsCrtAsyncHttpClient.create())
            .build();

    public InviteFunction() throws URISyntaxException {
    }

    @Override
    public APIGatewayV2WebSocketResponse handleRequest(APIGatewayV2WebSocketEvent apiGatewayV2WebSocketEvent, Context context) {
        PostToConnectionRequest postToConnectionRequest;
        LambdaLogger logger = context.getLogger();

        try (Jedis jedis = cache.getClient()) {
            RequestHelper helper = new RequestHelper(apiGatewayV2WebSocketEvent.getBody(),
                    apiGatewayV2WebSocketEvent.getQueryStringParameters());
            String idInvited = helper.getPathParam("player");
            String invitedConnection = jedis.get(CacheUtils.PREFIX_CONNECTION_PLAYER + idInvited);
            String id = helper.getParam("id");
            if ("nil".equals(invitedConnection)) {
                return WebSocketResponseUtils.badResponse("User unavailable");
            }
            postToConnectionRequest = PostToConnectionRequest.builder()
                    .connectionId(invitedConnection)
                    .data(SdkBytes.fromByteArray(RequestHelper.toJson(String.format("User with is %s invite you", id))
                            .getBytes()))
                    .build();
        } catch (JsonProcessingException e) {
            logger.log(e.getMessage());
            return WebSocketResponseUtils.badResponseServer();
        }
        CompletableFuture<PostToConnectionResponse> response = client.postToConnection(postToConnectionRequest);
        try {
            response.join();
        } catch (CancellationException | CompletionException e) {
            logger.log(e.getMessage());
            try {
                return WebSocketResponseUtils.badResponse("Invite failed");
            } catch (JsonProcessingException ex) {
                logger.log(ex.getMessage());
                return WebSocketResponseUtils.badResponseServer();
            }
        }
        return WebSocketResponseUtils.successfulResponse("{\"message\": \"Invited.\"}");
    }
}
