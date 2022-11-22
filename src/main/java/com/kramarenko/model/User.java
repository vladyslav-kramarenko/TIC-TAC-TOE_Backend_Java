package com.kramarenko.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.io.Serializable;
import java.util.Date;

@DynamoDbBean

public class User implements Serializable {
  @JsonProperty("nick")
  private String nickName = null;
  @JsonProperty("email")
  private String email = null;
  @JsonProperty("logo")
  private String logoOriginFileName = null;
  @JsonProperty("logokey")
  private String logoFileKey = null;
  @JsonProperty("accesstime")
  private Date accessTime = null;
  @JsonProperty("rank")
  private Integer rank = null;
  @JsonProperty("draw")
  private Integer draw = null;
  @JsonProperty("win")
  private Integer win = null;
  @JsonProperty("loss")
  private Integer loss = null;

  public User() {
  }

  public User(String nickName, String email, String logoOriginFileName, String logoFileKey, Date accessTime, Integer rank, Integer draw, Integer win, Integer loss) {
    this.nickName = nickName;
    this.email = email;
    this.logoOriginFileName = logoOriginFileName;
    this.logoFileKey = logoFileKey;
    this.accessTime = accessTime;
    this.rank = rank;
    this.draw = draw;
    this.win = win;
    this.loss = loss;
  }

  public Integer getDraw() {
    return draw;
  }

  public void setDraw(Integer draw) {
    this.draw = draw;
  }

  public Integer getWin() {
    return win;
  }

  public void setWin(Integer win) {
    this.win = win;
  }

  public Integer getLoss() {
    return loss;
  }

  public void setLoss(Integer loss) {
    this.loss = loss;
  }

  public String getNickName() {
    return nickName;
  }

  public void setNickName(String nickName) {
    this.nickName = nickName;
  }

  @DynamoDbPartitionKey
  public String getEmail() {
    return email;
  }

  @DynamoDbPartitionKey
  public void setEmail(String email) {
    this.email = email;
  }

  public String getLogoOriginFileName() {
    return logoOriginFileName;
  }

  public void setLogoOriginFileName(String logoOriginFileName) {
    this.logoOriginFileName = logoOriginFileName;
  }

  public String getLogoFileKey() {
    return logoFileKey;
  }

  public void setLogoFileKey(String logoFileKey) {
    this.logoFileKey = logoFileKey;
  }

  public Date getAccessTime() {
    return accessTime;
  }

  public void setAccessTime(Date accessTime) {
    this.accessTime = accessTime;
  }

  public Integer getRank() {
    return rank;
  }

  public void setRank(Integer rank) {
    this.rank = rank;
  }
}
