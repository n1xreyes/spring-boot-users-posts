package com.angelo.demo.exception;

import com.angelo.demo.model.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler  {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /**
     * General exception handling throughout the application
     *
     * @param ex The target exception
     * @param request The current request
     * @return {@code ResponseEntity} instance
     */
    @ExceptionHandler({
            UserNotFoundException.class,
            UserAlreadyExistsException.class
    })
    @Nullable
    public final ResponseEntity<ApiError> handleException(Exception ex, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();

        LOGGER.error("Handling {} due to {}", ex.getClass().getSimpleName(), ex.getMessage());

        if (ex instanceof UserNotFoundException userNotFoundException) {
            return handleUserNotFoundException(userNotFoundException, headers, request);
        } else if (ex instanceof UserAlreadyExistsException userAlreadyExistsException) {
            return handleUserAlreadyExistsException(userAlreadyExistsException, headers, request);
        } else {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Unknown exception type: {}", ex.getClass().getName());
            }

            HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            return handleExceptionInternal(ex, null, headers, httpStatus, request);
        }
    }

    /**
     * Custom response for UserNotFoundException
     *
     * @param ex      The exception
     * @param headers Header response
     * @param request
     * @return {@code ResponseEntity} instance
     */
    protected ResponseEntity<ApiError> handleUserNotFoundException(UserNotFoundException ex,
                                                                   HttpHeaders headers,
                                                                   WebRequest request) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        return handleExceptionInternal(ex, new ApiError(errors), headers, HttpStatus.NOT_FOUND, request);
    }

    protected ResponseEntity<ApiError> handleUserAlreadyExistsException(UserAlreadyExistsException ex,
                                                                        HttpHeaders headers,
                                                                        WebRequest request) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        return handleExceptionInternal(ex, new ApiError(errors), headers, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * The single location to customize the response body of all exception types
     *
     * <p>The default implementation sets the {@link WebUtils#ERROR_EXCEPTION_ATTRIBUTE}
     * request attribute and creates a {@link ResponseEntity} from the given body, headers, and status</p>
     *
     * @param ex The exception
     * @param body Response body
     * @param headers Response headers
     * @param httpStatus Response status
     * @param request current request
     * @return {@code ResponseEntity} instance
     */
    protected ResponseEntity<ApiError> handleExceptionInternal(Exception ex, @Nullable ApiError body,
                                                               HttpHeaders headers, HttpStatus httpStatus,
                                                               WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(httpStatus)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, RequestAttributes.SCOPE_REQUEST);
        }

        return new ResponseEntity<>(body, headers, httpStatus);
    }

}
