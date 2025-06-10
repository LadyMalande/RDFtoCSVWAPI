package org.rdftocsvconverter.RDFtoCSVW.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ExpensiveComputationService {

    // This result will be cached for 60 seconds
    @Cacheable(value = "computationCache", key = "#input", unless = "#result == null")
    public String expensiveComputation(String input) {
        // Simulate long computation
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "Result for " + input + " at " + System.currentTimeMillis();
    }

    // Clear cache for specific key
    @CacheEvict(value = "computationCache", key = "#input")
    public void clearCache(String input) {
        // Method just acts as a trigger for cache eviction
    }

    // Clear entire cache
    @CacheEvict(value = "computationCache", allEntries = true)
    public void clearAllCache() {
        // Method just acts as a trigger for cache eviction
    }
}
