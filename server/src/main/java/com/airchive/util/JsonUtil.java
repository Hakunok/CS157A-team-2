package com.airchive.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonUtil {

  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
      .setPrettyPrinting()
      .create();

  private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
      out.value(value != null ? value.format(formatter) : null);
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
      return LocalDateTime.parse(in.nextString(), formatter);
    }
  }

  public static <T> T read(HttpServletRequest request, Class<T> clazz) throws IOException {
    try (BufferedReader reader = request.getReader()) {
      return gson.fromJson(reader, clazz);
    } catch (JsonSyntaxException e) {
      return null;
    }
  }

  public static void sendJson(HttpServletResponse response, int status, Object body) throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    try (PrintWriter out = response.getWriter()) {
      out.print(gson.toJson(body));
      out.flush();
    }
  }

  public static void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
    Map<String, String> errorResponse = Map.of("error", message);
    sendJson(response, status, errorResponse);
  }
}
