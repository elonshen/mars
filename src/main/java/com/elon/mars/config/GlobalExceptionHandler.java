package com.elon.mars.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorResponse.setMessage(error.getDefaultMessage());
        });
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        Throwable rootCause = getRootCause(ex);
        if (rootCause instanceof SQLIntegrityConstraintViolationException sqlEx) {
            String message = sqlEx.getMessage();

            if (message.contains("foreign key constraint fails")) {
                return new ResponseEntity<>(new ErrorResponse("该数据已被其他数据关联，请先取消关联后再删除"), HttpStatus.CONFLICT);
            } else if (message.contains("Duplicate entry")) {
                return new ResponseEntity<>(new ErrorResponse("无法创建或更新资源，因为某些数据重复了。"), HttpStatus.CONFLICT);
            }
        }
        return new ResponseEntity<>(new ErrorResponse("违反数据完整性"), HttpStatus.BAD_REQUEST);
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause;
        Throwable result = throwable;
        while ((cause = result.getCause()) != null && result != cause) {
            result = cause;
        }
        return result;
    }
}
