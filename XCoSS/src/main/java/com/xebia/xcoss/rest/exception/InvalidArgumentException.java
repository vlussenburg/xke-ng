package com.xebia.xcoss.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED, reason = "Invalid argument")
public class InvalidArgumentException extends RuntimeException {

}
