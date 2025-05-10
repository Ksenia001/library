package com.example.myspringproject.service.impl;

import com.example.myspringproject.service.VisitTrackingService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VisitTrackingServiceImpl implements VisitTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(VisitTrackingServiceImpl.class);
    private final Map<String, AtomicLong> urlVisits = new ConcurrentHashMap<>();
    private final AtomicLong totalSiteVisits = new AtomicLong(0);

    @Override
    public void trackVisit(String url) {
        urlVisits.computeIfAbsent(url, k -> new AtomicLong(0)).incrementAndGet();
        long currentTotal = totalSiteVisits.incrementAndGet();
        logger.info(
                "Tracked visit for URL: {}. "
                        + "Current visits for this URL: {}. Total site visits: {}",
                url, urlVisits.get(url).get(), currentTotal);
    }

    @Override
    public long getTotalVisits(String url) {
        AtomicLong count = urlVisits.get(url);
        return (count != null) ? count.get() : 0;
    }

    @Override
    public Map<String, Long> getAllUrlVisits() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        urlVisits.forEach((url, count) -> result.put(url, count.get()));
        return result;
    }

    @Override
    public long getTotalSiteVisits() {
        return totalSiteVisits.get();
    }
}