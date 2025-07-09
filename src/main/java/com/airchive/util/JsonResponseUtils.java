package com.airchive.util;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonResponseUtils {
  public static void sendJsonSuccess(HttpServletResponse response, String message) throws IOException {
    JsonObject jsonResponse = new JsonObject();
    jsonResponse.addProperty("success", true);
    jsonResponse.addProperty("message", message);
    sendJsonResponse(response, jsonResponse);
  }

  public static void sendJsonError(HttpServletResponse response, String message) throws IOException {
    JsonObject jsonResponse = new JsonObject();
    jsonResponse.addProperty("success", false);
    jsonResponse.addProperty("message", message);
    sendJsonResponse(response, jsonResponse);
  }

  private static void sendJsonResponse(HttpServletResponse response, JsonObject jsonResponse) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    PrintWriter out = response.getWriter();
    out.print(jsonResponse.toString());
    out.flush();
  }
}
