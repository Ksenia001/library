package com.example.myspringproject.service.impl;

import com.example.myspringproject.exception.EntityNotFoundException;
import com.example.myspringproject.model.LogTaskInfo;
import com.example.myspringproject.model.LogTaskStatus;
import com.example.myspringproject.service.LogGenerationService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Добавлен импорт
import org.springframework.context.annotation.Lazy; // Добавлен импорт
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogGenerationServiceImpl implements LogGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(LogGenerationServiceImpl.class);
    private static final String SOURCE_LOG_FILE_PATTERN = "logs/library-%s.log";
    private static final String GENERATED_LOGS_DIR = "logs/generated_reports/";

    private final Map<String, LogTaskInfo> tasks = new ConcurrentHashMap<>();
    private final LogGenerationService self;

    @Autowired // Внедряем самого себя через @Lazy для корректной работы @Async
    public LogGenerationServiceImpl(@Lazy LogGenerationService self) {
        this.self = self;
    }

    @Override
    public String initiateLogFileGeneration(LocalDate date) {
        String taskId = UUID.randomUUID().toString();
        try {
            Path targetDir = Paths.get(GENERATED_LOGS_DIR);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
        } catch (IOException e) {
            logger.error(
                    "Could not create directory for generated logs: {}", GENERATED_LOGS_DIR, e);
        }
        tasks.put(taskId, new LogTaskInfo(taskId, LogTaskStatus.PENDING, null, null));
        // Вызываем асинхронный метод через self (прокси)
        self.processLogGeneration(taskId, date);
        return taskId;
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> processLogGeneration(String taskId, LocalDate date) {
        logger.info("Starting log generation for task ID: {} and date: {}", taskId, date);
        try {
            tasks.computeIfPresent(taskId, (k, v) -> v.withStatus(LogTaskStatus.IN_PROGRESS));

            Path sourceLogPath = Paths.get(String.format(SOURCE_LOG_FILE_PATTERN, date.toString()));
            if (!Files.exists(sourceLogPath)) {
                String errorMessage = "Source log file not found for date: "
                        + date + " at path " + sourceLogPath.toAbsolutePath();
                logger.warn(errorMessage);
                tasks.computeIfPresent(taskId, (k, v)
                        -> v.withStatus(LogTaskStatus.FAILED).withErrorMessage(errorMessage));
                return CompletableFuture.completedFuture(null);
            }

            Path targetDir = Paths.get(GENERATED_LOGS_DIR);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Path targetFilePath = targetDir.resolve(taskId + "_" + date.toString() + ".log");
            Files.copy(sourceLogPath,
                    targetFilePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Log file generated successfully for task ID: {}. Path: {}",
                    taskId, targetFilePath);
            tasks.computeIfPresent(taskId, (k, v)
                    -> v.withStatus(LogTaskStatus.COMPLETED)
                    .withFilePath(targetFilePath.toString()));

        } catch (IOException e) {
            String errorMessage = "Error generating log file for task "
                    + taskId + ": " + e.getMessage();
            logger.error(errorMessage, e);
            tasks.computeIfPresent(taskId, (k, v)
                    -> v.withStatus(LogTaskStatus.FAILED).withErrorMessage(errorMessage));
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public LogTaskInfo getTaskStatus(String taskId) {
        LogTaskInfo taskInfo = tasks.get(taskId);
        if (taskInfo == null) {
            throw new EntityNotFoundException("Log generation task not found with ID: " + taskId);
        }
        return taskInfo;
    }

    @Override
    public Path getGeneratedLogFile(String taskId) {
        LogTaskInfo taskInfo = getTaskStatus(taskId); // This will throw if not found
        if (taskInfo.status() == LogTaskStatus.COMPLETED && taskInfo.filePath() != null) {
            return Paths.get(taskInfo.filePath());
        }
        return null;
    }
}