package com.example.myspringproject.model;

public record LogTaskInfo(
        String taskId,
        LogTaskStatus status,
        String filePath,
        String errorMessage
) {
    public LogTaskInfo withStatus(LogTaskStatus newStatus) {
        return new LogTaskInfo(this.taskId, newStatus, this.filePath, this.errorMessage);
    }

    public LogTaskInfo withFilePath(String newFilePath) {
        return new LogTaskInfo(this.taskId, this.status, newFilePath, this.errorMessage);
    }

    public LogTaskInfo withErrorMessage(String newErrorMessage) {
        return new LogTaskInfo(this.taskId, this.status, this.filePath, newErrorMessage);
    }
}
