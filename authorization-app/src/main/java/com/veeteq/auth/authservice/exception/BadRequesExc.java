package com.veeteq.auth.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Value Not Found")
public class BadRequesExc extends RuntimeException {

    public BadRequesExc() {
        super();
    }

    public BadRequesExc(String message) {
        super(message);
    }

    public BadRequesExc(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequesExc(Throwable cause) {
        super(cause);
    }

    protected BadRequesExc(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
