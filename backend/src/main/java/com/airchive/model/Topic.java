package com.airchive.model;

public class Topic {

  private Integer topicId;
  private String code;
  private String name;
  private String colorHex;

  public Topic(Integer topicId, String code, String name, String colorHex) {
    this.topicId = topicId;
    this.code = code;
    this.name = name;
    this.colorHex = colorHex;
  }

  public Topic(String code, String name, String colorHex) {
    this(null, code, name, colorHex);
  }

  public Integer getTopicId() { return topicId; }
  public void setTopicId(Integer topicId) { this.topicId = topicId; }

  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getColorHex() { return colorHex; }
  public void setColorHex(String colorHex) { this.colorHex = colorHex; }
}