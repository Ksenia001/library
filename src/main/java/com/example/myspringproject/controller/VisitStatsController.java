package com.example.myspringproject.controller;

import com.example.myspringproject.service.VisitTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats/visits")
@Tag(name = "Visit Statistics", description = "API for viewing website visit statistics")
public class VisitStatsController {

    private final VisitTrackingService visitTrackingService;

    public VisitStatsController(VisitTrackingService visitTrackingService) {
        this.visitTrackingService = visitTrackingService;
    }

    @Operation(summary = "Get total site visits",
            description = "Retrieves the total number of visits across all tracked URLs.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved total site visits")
    })
    @GetMapping("/total")
    public ResponseEntity<Map<String, Long>> getTotalSiteVisits() {
        return ResponseEntity.ok(Map.of(
                "totalSiteVisits", visitTrackingService.getTotalSiteVisits()));
    }

    @Operation(summary = "Get visits for all URLs",
            description = "Retrieves the number of visits for each tracked URL.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved URL visit counts")
    })
    @GetMapping("/by-url")
    public ResponseEntity<Map<String, Long>> getAllUrlVisits() {
        return ResponseEntity.ok(visitTrackingService.getAllUrlVisits());
    }
}