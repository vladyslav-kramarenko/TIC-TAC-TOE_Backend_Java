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
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.kramarenko.utils.WebSocketResponseUtils.badResponseServer;

public class MoveFunction implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {
    public static final CacheUtils cache = new CacheUtils(System.getenv("Host"),
            Integer.valueOf(System.getenv("PORT")));
    private final ApiGatewayManagementApiAsyncClient client = ApiGatewayManagementApiAsyncClient.builder()
            .endpointOverride(new URI(System.getenv("ENDPOINT")))
            .region(Region.of(System.getenv("REGION")))
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .httpClient(AwsCrtAsyncHttpClient.create())
            .build();

    public MoveFunction() throws URISyntaxException {

    }

    @Override
    public APIGatewayV2WebSocketResponse handleRequest(APIGatewayV2WebSocketEvent apiGatewayV2WebSocketEvent, Context context) {
        PostToConnectionRequest postToConnectionRequest;
        LambdaLogger logger = context.getLogger();


        try (Jedis jedis = cache.getClient()){
            RequestHelper helper = new RequestHelper(apiGatewayV2WebSocketEvent.getBody(),
                    apiGatewayV2WebSocketEvent.getQueryStringParameters());
            String sendTo = helper.getPathParam("player");
            String moveToConnection = jedis.get(CacheUtils.PREFIX_CONNECTION_PLAYER + sendTo);
            if ("nil".equals(moveToConnection)) {
                return WebSocketResponseUtils.badResponse("User unavailable");
            }
//            Map(id, move)
            Map<String, String> moves = RequestHelper.parseMoves(helper.getParam("moves"));
            jedis.hset(CacheUtils.PREFIX_MOVES + moveToConnection, moves);
            postToConnectionRequest = PostToConnectionRequest.builder()
                    .connectionId(moveToConnection)
                    .data(SdkBytes.fromByteArray(RequestHelper.toJson("New State" + moves)
                            .getBytes()))
                    .build();
        } catch (JsonProcessingException e) {
            logger.log(e.getMessage());
            return badResponseServer();
        }

        CompletableFuture<PostToConnectionResponse> response = client.postToConnection(postToConnectionRequest);
        try {
            response.join();
        } catch (CancellationException |
                 CompletionException e) {
            logger.log(e.getMessage());
            try {
                return WebSocketResponseUtils.badResponse("Move failed!");
            } catch (JsonProcessingException e1) {
                logger.log(e1.getMessage());
                return badResponseServer();
            }
        }
        return WebSocketResponseUtils.successfulResponse("{\"message\": \"Move sent and save.\"}");
    }
}
