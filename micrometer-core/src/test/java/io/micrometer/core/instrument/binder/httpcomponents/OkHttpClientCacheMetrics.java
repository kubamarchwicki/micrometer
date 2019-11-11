package io.micrometer.core.instrument.binder.httpcomponents;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import okhttp3.Cache;

import java.io.IOException;

public class OkHttpClientCacheMetrics extends CacheMeterBinder {

    private final Cache cache;

    public OkHttpClientCacheMetrics(Cache cache, String cacheName, Iterable<Tag> tags) {
        super(cache, cacheName, tags);
        this.cache = cache;
    }

    @Override
    protected Long size() {
        try {
            return cache.size();
        } catch (IOException ex) {
            //TODO: should log?
            return -1L;
        }
    }

    @Override
    protected long hitCount() {
        return cache.hitCount();
    }

    @Override
    protected Long missCount() {
        return Long.valueOf(cache.networkCount());
    }

    @Override
    protected Long evictionCount() {
        return null;
    }

    @Override
    protected long putCount() {
        return 0;
    }

    @Override
    protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
        FunctionCounter.builder("cache.gets", cache, c -> c.requestCount())
                .tags(getTagsWithCacheName()).tag("result", "total")
                .description("The number of HTTP requests issued since this cache was created.")
                .register(registry);

        Gauge.builder("cache.size", () -> cache.maxSize())
                .tags(getTagsWithCacheName()).tag("size", "max")
                .description("Maximum cache size.")
                .register(registry);
    }
}
