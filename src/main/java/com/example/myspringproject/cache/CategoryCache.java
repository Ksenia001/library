package com.example.myspringproject.cache;

import com.example.myspringproject.model.Category;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CategoryCache {
    private static final Logger log =
            LoggerFactory.getLogger(CategoryCache.class);
    private final Map<String, List<Category>> cache = new HashMap<>();
    private static final int SIZE = 100;

    public void put(String key, List<Category> value) {
        if (cache.size() >= SIZE) {
            String oldestKey = cache.keySet().iterator().next();
            cache.remove(oldestKey);
            log.debug("Removed oldest key from cache: {}", oldestKey);
        }
        cache.put(key, value);
        log.info("New request added to cache. Key: {}. Current cache size: {}", key, cache.size());
    }

    public List<Category> get(String key) {
        log.info("Retrieving from cache with key: {}", key);
        return cache.get(key);
    }

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public void clear() {
        log.info("Cache cleared. Previous size: {}", cache.size());
        cache.clear();
    }
}