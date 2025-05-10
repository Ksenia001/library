package com.example.myspringproject.service;

import com.example.myspringproject.model.LogTaskInfo;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public interface LogGenerationService {
    String initiateLogFileGeneration(LocalDate date);

    CompletableFuture<Void> processLogGeneration(String taskId, LocalDate date);

    LogTaskInfo getTaskStatus(String taskId);

    Path getGeneratedLogFile(String taskId);
}