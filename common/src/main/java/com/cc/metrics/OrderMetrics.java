package com.cc.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class OrderMetrics {
    public static final AtomicLong payingCount = new AtomicLong(0);
    public static final AtomicLong paySuccess = new AtomicLong(0);
    public static final AtomicLong payFailed = new AtomicLong(0);
}
