package com.matheus.api_abito_arcano.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AreaNotFoundException.class)
    public ResponseEntity<ApiError> handleAreaNotFoundException(AreaNotFoundException ex) {
        ApiError apiError = new ApiError();
        apiError.setCode("AREA-001");
        apiError.setStatus(404);
        apiError.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(SubareaNotFoundException.class)
    public ResponseEntity<ApiError> handleSubareaNotFoundException(SubareaNotFoundException ex) {
        ApiError apiError = new ApiError();
        apiError.setCode("SUBAREA-001");
        apiError.setStatus(404);
        apiError.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(TarefaNotFoundException.class)
    public ResponseEntity<ApiError> handleTarefaNotFoundException(TarefaNotFoundException ex) {
        ApiError apiError = new ApiError();
        apiError.setCode("TAREFA-001");
        apiError.setStatus(404);
        apiError.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(InvalidAreaException.class)
    public ResponseEntity<ApiError> handleInvalidAreaException(InvalidAreaException ex) {
        ApiError apiError = new ApiError();
        apiError.setCode("AREA-002");
        apiError.setStatus(400);
        apiError.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        ApiError apiError = new ApiError();
        apiError.setCode("GENERAL-001");
        apiError.setStatus(500);
        apiError.setMessage("Ocorreu um erro inesperado: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
