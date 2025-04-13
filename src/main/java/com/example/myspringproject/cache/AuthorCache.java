package com.example.myspringproject.cache;

import com.example.myspringproject.model.Author;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;

@Component
public class AuthorCache {
    private final Map<String, List<Author>> cache = new HashMap<>();
    private static final int SIZE = 100;

    public void put(String key, List<Author> value) {
        if (cache.size() >= SIZE) {
            String oldestKey = cache.keySet().iterator().next();
            cache.remove(oldestKey);
        }
        cache.put(key, value);

        Logger logger = Logger.getLogger(AuthorCache.class.getName());
        String msg = String.format("Новый запрос добавлен в кэш. Текущий размер кэша: %s",
                cache.size());
        logger.info(msg);
    }

    public List<Author> get(String key) {
        Logger logger = Logger.getLogger(AuthorCache.class.getName());
        String msg = "Запрос из кэша с ключом: " + key;
        logger.info(msg);

        return cache.get(key);
    }

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public void clear() {
        Logger logger = Logger.getLogger(AuthorCache.class.getName());
        String msg = String.format("Кэш очищен. Текущий размер кэша: %s", cache.size());
        logger.info(msg);

        cache.clear();
    }
}