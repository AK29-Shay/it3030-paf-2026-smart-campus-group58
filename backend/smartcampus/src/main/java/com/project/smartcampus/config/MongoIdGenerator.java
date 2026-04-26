package com.project.smartcampus.config;

import java.util.concurrent.atomic.AtomicLong;

public final class MongoIdGenerator {

    private static final AtomicLong SEQUENCE = new AtomicLong(System.currentTimeMillis());

    private MongoIdGenerator() {
    }

    public static Long nextId() {
        return SEQUENCE.incrementAndGet();
    }
}