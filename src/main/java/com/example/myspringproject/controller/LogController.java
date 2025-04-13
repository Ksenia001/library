package com.example.myspringproject.controller;

import com.example.myspringproject.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@Tag(name = "Logs", description = "API for managing logs")
public class LogController {
    private static final String LOG_FILE_PATH = "logs/library";

    @Operation(summary = "Get logs by date", description = "Retrieves logs by date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logs retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Logs not found")
    })
    @GetMapping
    public ResponseEntity<Resource> getLogByDate(
        @Parameter(description = "Log's date", example = "2025-04-13")
        @RequestParam("date") LocalDate date) throws IOException {

        String dateString = date.toString();
        Path logPath = Paths.get(LOG_FILE_PATH + "-" + dateString + ".log");
        if (!Files.exists(logPath)) {
            throw new EntityNotFoundException("Log file was not found with date: " + dateString);
        }

        Resource resource = new UrlResource(logPath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + "library-" + dateString + ".log" + "\"")
                .body(resource);
    }
}