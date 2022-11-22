package com.kramarenko.utils;

import com.kramarenko.model.User;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DDBUtils {
    private final DynamoDbEnhancedClient enhancedClient;
    private final String tableName;

    public DDBUtils(String tableName, String region) {
        this.tableName = tableName;
        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(Region.of(region))
                .build();
        enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();
    }

    public String addRankRecord(User user) {
        DynamoDbTable<User> table = enhancedClient.table(tableName, TableSchema.fromBean(User.class));

        return table.updateItem(user).getNickName();
    }

    public User getRankRecord(String userId) {
        DynamoDbTable<User> mappedTable = enhancedClient
                .table(tableName, TableSchema.fromBean(User.class));

        Key key = Key.builder()
                .partitionValue(userId)
                .build();
        return mappedTable.getItem(key);
    }
}
