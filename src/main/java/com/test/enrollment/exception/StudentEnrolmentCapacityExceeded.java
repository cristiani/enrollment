package com.test.enrollment.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@Setter
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class StudentEnrolmentCapacityExceeded extends RuntimeException {
    private static final long serialVersionUID = 12346767890L;
    private String message;
    public StudentEnrolmentCapacityExceeded( String message) {
        this.message = message;
    }
}
