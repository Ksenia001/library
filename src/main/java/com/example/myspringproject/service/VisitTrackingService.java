package com.example.myspringproject.service;

import java.util.Map;

public interface VisitTrackingService {
    void trackVisit(String url);

    long getTotalVisits(String url);

    Map<String, Long> getAllUrlVisits();

    long getTotalSiteVisits();
}