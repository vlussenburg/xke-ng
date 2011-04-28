package com.xebia.xcoss.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NO_CONTENT, reason = "Item not found")
public class ItemNotFoundException extends RuntimeException {

}
