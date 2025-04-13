package com.example.myspringproject.cache;

import com.example.myspringproject.model.Category;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;

@Component
public class CategoryCache {
    private final Map<String, List<Category>> cache = new HashMap<>();
    private static final int SIZE = 100;

    public void put(String key, List<Category> value) {
        if (cache.size() >= SIZE) {
            String oldestKey = cache.keySet().iterator().next();
            cache.remove(oldestKey);
        }
        cache.put(key, value);

        Logger logger = Logger.getLogger(CategoryCache.class.getName());
        String msg = String.format("Новый запрос добавлен в кэш. Текущий размер кэша: %s",
                cache.size());
        logger.info(msg);
    }

    public List<Category> get(String key) {
        Logger logger = Logger.getLogger(CategoryCache.class.getName());
        String msg = "Запрос из кэша с ключом: " + key;
        logger.info(msg);

        return cache.get(key);
    }

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public void clear() {
        Logger logger = Logger.getLogger(CategoryCache.class.getName());
        String msg = String.format("Кэш очищен. Текущий размер кэша: %s", cache.size());
        logger.info(msg);
        cache.clear();
    }
}
