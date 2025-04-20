package com.example.myspringproject.cache;

import com.example.myspringproject.model.Author;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthorCache {
    private static final Logger log = LoggerFactory.getLogger(AuthorCache.class);
    private final Map<String, List<Author>> cache = new HashMap<>();
    private static final int SIZE = 100;

    public void put(String key, List<Author> value) {
        if (cache.size() >= SIZE) {
            String oldestKey = cache.keySet().iterator().next();
            cache.remove(oldestKey);
            log.debug("Removed oldest key from cache: {}", oldestKey);
        }
        cache.put(key, value);
        log.info("New request added to cache. Key: {}. Current cache size: {}", key, cache.size());
    }

    public List<Author> get(String key) {
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
