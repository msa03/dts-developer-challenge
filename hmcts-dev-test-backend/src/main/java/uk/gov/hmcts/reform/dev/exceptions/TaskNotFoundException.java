package uk.gov.hmcts.reform.dev.exceptions;

import java.util.UUID;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(UUID id) {
        super("Task with id '" + id + "' not found");
    }
}
