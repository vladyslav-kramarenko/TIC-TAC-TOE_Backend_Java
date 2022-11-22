package com.kramarenko;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.kramarenko.model.User;
import com.kramarenko.utils.DDBUtils;
import com.kramarenko.utils.RequestHelper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class RankedUpdateFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final DDBUtils ddbUtils = new DDBUtils(System.getenv("TABLE"), System.getenv("REGION"));


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        String userId;
        LambdaLogger logger = context.getLogger();
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            RequestHelper helper = new RequestHelper(apiGatewayProxyRequestEvent.getBody(),
                    apiGatewayProxyRequestEvent.getPathParameters());
            String id = helper.getParam("id");
            User rankRecord = ddbUtils.getRankRecord(id);

            int winStatus = getWinStatus(helper.getParam("status"));
            rankRecord.setRank(rankRecord.getRank() + getRankDelta(helper.getParam("status")));
            rankRecord.setWin(rankRecord.getWin() + winStatus==1?1:0);
            rankRecord.setLoss(rankRecord.getLoss() + winStatus==-1?1:0);
            rankRecord.setDraw(rankRecord.getDraw() + winStatus==0?1:0);

            userId = ddbUtils.addRankRecord(rankRecord);
        } catch (JsonProcessingException e) {
            logger.log(e.getMessage());
            return response.withStatusCode(400).withBody("{\"message\": \"Body incorrect.\"}");
        }
        return response.withStatusCode(200)
                .withBody(String.format("{\"message\": \"Successfully update rank for user %s\"}", userId));
    }

    private Integer getRankDelta(String status) {
        if ("win".equals(status)) {
            return 2;
        } else {
            if ("draw".equals(status)) {
                return 0;
            }
            return -1;
        }
    }

    private Integer getWinStatus(String status) {
        if ("win".equals(status)) {
            return 1;
        } else {
            if ("draw".equals(status)) {
                return 0;
            }
            return -1;
        }
    }
}
