package com.healthcare.service;

import com.healthcare.model.Doctor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thread-safe LRU Cache for Doctor entities built using LinkedHashMap.
 * Demonstrates Collections framework mastery with access-order eviction.
 */
public class DoctorCache {
    private final int capacity;
    private final Map<Integer, Doctor> cache;

    public DoctorCache(int capacity) {
        this.capacity = capacity;
        // LinkedHashMap with accessOrder=true enables LRU behavior
        this.cache = Collections.synchronizedMap(new LinkedHashMap<Integer, Doctor>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Doctor> eldest) {
                return size() > capacity;
            }
        });
    }

    public Doctor get(int doctorId) {
        return cache.get(doctorId);
    }

    public void put(Doctor doctor) {
        if (doctor != null && doctor.getId() != null) {
            cache.put(doctor.getId(), doctor);
        }
    }

    public void invalidate(int doctorId) {
        cache.remove(doctorId);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }
}
