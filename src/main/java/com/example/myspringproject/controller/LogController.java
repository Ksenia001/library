package com.example.myspringproject.controller;

import com.example.myspringproject.exception.EntityNotFoundException;
import com.example.myspringproject.model.LogTaskInfo;
import com.example.myspringproject.service.LogGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@Tag(name = "Logs", description = "API for managing logs and log reports")
public class LogController {
    private static final String LOG_FILE_PATH_BASE = "logs/library";
    private final LogGenerationService logGenerationService;

    public LogController(LogGenerationService logGenerationService) {
        this.logGenerationService = logGenerationService;
    }

    @Operation(summary = "Get logs by date",
            description = "Retrieves logs by date directly from the file system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logs retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Logs not found")
    })
    @GetMapping
    public ResponseEntity<Resource> getLogByDate(
            @Parameter(description = "Log's date", example = "2025-04-13")
            @RequestParam("date") LocalDate date) throws IOException {

        String dateString = date.toString();
        Path logPath = Paths.get(LOG_FILE_PATH_BASE + "-" + dateString + ".log");
        if (!Files.exists(logPath)) {
            throw new EntityNotFoundException("Log file was not found with date: "
                    + dateString + " at path " + logPath.toAbsolutePath());
        }

        Resource resource = new UrlResource(logPath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + "library-" + dateString + ".log" + "\"")
                .body(resource);
    }

    @Operation(summary = "Initiate log report generation",
            description = "Asynchronously generates a log report for a specific date.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Log report generation accepted"),
        @ApiResponse(responseCode = "400", description = "Invalid date provided")
    })
    @PostMapping("/reports")
    public ResponseEntity<Map<String, String>> generateLogReport(
            @Parameter(description = "Date for the log report", example = "2025-04-13")
            @RequestParam("date") LocalDate date) {
        String taskId = logGenerationService.initiateLogFileGeneration(date);
        return ResponseEntity.accepted().body(Map.of("taskId", taskId));
    }

    @Operation(summary = "Get log report generation status",
            description = "Retrieves the status of an asynchronous log report generation task.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/reports/{taskId}/status")
    public ResponseEntity<LogTaskInfo> getLogReportStatus(
            @Parameter(description = "ID of the log generation task")
            @PathVariable String taskId) {
        LogTaskInfo taskInfo = logGenerationService.getTaskStatus(taskId);
        return ResponseEntity.ok(taskInfo);
    }

    @Operation(summary = "Download generated log report",
            description = "Downloads the generated log report file if completed.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                    description = "Log report downloaded successfully"),
        @ApiResponse(responseCode = "202",
                    description = "Log report generation still in progress or pending"),
        @ApiResponse(responseCode = "404",
                    description = "Task not found or file not available"),
        @ApiResponse(responseCode = "500",
                    description = "Log report generation failed or error accessing file")
    })
    @GetMapping("/reports/{taskId}/download")
    public ResponseEntity<Object> downloadGeneratedLogReport(
            @Parameter(description = "ID of the log generation task")
            @PathVariable String taskId) {
        try {
            LogTaskInfo taskInfo = logGenerationService.getTaskStatus(taskId);

            switch (taskInfo.status()) {
                case COMPLETED:
                    Path filePath = logGenerationService.getGeneratedLogFile(taskId);
                    if (filePath != null && Files.exists(filePath)) {
                        Resource resource = new UrlResource(filePath.toUri());
                        return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                        "attachment; filename=\""
                                                + filePath.getFileName().toString() + "\"")
                                .body(resource);
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Generated log file not found for task ID: " + taskId);
                    }
                case PENDING, IN_PROGRESS: // Объединенные кейсы
                    return ResponseEntity.status(HttpStatus.ACCEPTED)
                            .body("Log report generation is "
                                    + taskInfo.status().toString().toLowerCase()
                                    + " for task ID: " + taskId);
                case FAILED:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Log report generation failed for task ID: "
                                    + taskId + ". Error: " + taskInfo.errorMessage());
                default:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Unknown status for task ID: " + taskId);
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            String errorMessage;
            if (e instanceof MalformedURLException) {
                errorMessage = "Error creating URL for the log file: " + e.getMessage();
            } else {
                errorMessage = "Error accessing the log file: " + e.getMessage();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }
}