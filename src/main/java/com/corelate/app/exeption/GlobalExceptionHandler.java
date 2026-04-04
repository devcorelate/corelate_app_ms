package com.corelate.app.exeption;

import com.corelate.app.dto.ErrorResponseDto;
import com.corelate.app.dto.SessionUpdateConflictResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        List<ObjectError> validationErrorList = ex.getAllErrors();

        validationErrorList.forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String validationMsg = error.getDefaultMessage();
            validationErrors.put(fieldName,validationMsg);
        });

        return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception exception,
                                                                            WebRequest webRequest){
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException exception,
                                                                         WebRequest webRequest){
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SessionIdMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleSessionIdMismatchException(SessionIdMismatchException exception,
                                                                             WebRequest webRequest) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SessionUpdateConflictException.class)
    public ResponseEntity<SessionUpdateConflictResponseDto> handleSessionUpdateConflict(
            SessionUpdateConflictException exception) {
        logger.warn("Optimistic locking conflict for sessionId={}", exception.getSessionId());
        SessionUpdateConflictResponseDto body = new SessionUpdateConflictResponseDto(
                "SESSION_UPDATE_CONFLICT",
                "Session was modified by another transaction.",
                exception.getSessionId(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

//    @ExceptionHandler(TemplateAlreadyExistsException.class)
//    public ResponseEntity<ErrorResponseDto> handleTemplateAlreadyExistsExeption(TemplateAlreadyExistsException exception,
//                                                                         WebRequest webRequest){
//        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
//                webRequest.getDescription(false),
//                HttpStatus.BAD_REQUEST,
//                exception.getMessage(),
//                LocalDateTime.now()
//        );
//
//        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
//    }

}
