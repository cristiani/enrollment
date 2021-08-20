package com.test.enrollment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ResponseStatus(value= HttpStatus.FORBIDDEN, reason="Invalid value")
    @ExceptionHandler(ConstraintViolationException.class)
    public void handleValidationException() { }

    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Course Capacity Exceeded")
    @ExceptionHandler(CourseCapacityExceeded.class)
    public void handleCourseCapacityExceeded() { }

    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Student Enrolment Capacity Exceeded")
    @ExceptionHandler(StudentEnrolmentCapacityExceeded.class)
    public void handleStudentEnrolmentCapacityExceeded() { }

    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Course not found")
    @ExceptionHandler(CourseNotFoundException.class)
    public void handleCourseNotFoundException() { }

    @ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Student not found")
    @ExceptionHandler(StudentNotFoundException.class)
    public void handleStudentNotFoundException() { }

    @ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Invalid number")
    @ExceptionHandler(InvalidNumberException.class)
    public void handleInvalidNumberException() { }
}
