package com.example.myspringproject.cache;

import com.example.myspringproject.model.Book;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BookCache {
    private final Map<String, List<Book>> cache = new HashMap<>();
    private static final int SIZE = 100;

    public void put(String key, List<Book> value) {
        if (cache.size() >= SIZE) {
            String oldestKey = cache.keySet().iterator().next();
            cache.remove(oldestKey);
        }
        cache.put(key, value);
    }

    public List<Book> get(String key) {
        return cache.get(key);
    }

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public void clear() {
        cache.clear();
    }
}
