package com.test.enrollment.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@Setter
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class CourseCapacityExceeded extends RuntimeException {
    private static final long serialVersionUID = 363456345643L;
    private String message;
    public CourseCapacityExceeded( String message) {
        this.message = message;
    }
}
