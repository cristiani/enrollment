package com.test.enrollment.exception;


import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@Setter
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CourseNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1232452345L;
    private String message;
    public CourseNotFoundException( String message) {
        this.message = message;
    }
}
