package com.airchive.dto;

import java.util.Map;

public record ValidationRequest(
    String field,
    String value,
    Map<String, String> extra
) {}
