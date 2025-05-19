package main.java.stats.exception;

import lombok.Value;

@Value
public class ErrorResponse {
    String error;
    String description;
}