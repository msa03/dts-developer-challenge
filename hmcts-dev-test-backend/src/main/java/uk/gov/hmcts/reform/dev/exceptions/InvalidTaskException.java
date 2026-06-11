package uk.gov.hmcts.reform.dev.exceptions;

public class InvalidTaskException extends RuntimeException {

    public InvalidTaskException(String message) {
        super(message);
    }
}
