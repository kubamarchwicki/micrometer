/**
 * Copyright 2017 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument.binder.okhttp3;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.httpcomponents.OkHttpClientCacheMetrics;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import ru.lanwen.wiremock.ext.WiremockResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests for {@link OkHttpClientCacheMetrics}.
 *
 * @author Jakub Marchwicki
 */
@ExtendWith(WiremockResolver.class)
class OkHttpClientCacheMetricsTest {

    @TempDir
    File cacheDirectory;

    private final static Integer CACHE_SIZE_BYTES = 1024 * 1024 * 2;

    private MeterRegistry registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());

    @Test
    void gaugeCacheSize(@WiremockResolver.Wiremock WireMockServer server) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(new Cache(cacheDirectory, CACHE_SIZE_BYTES))
                .build();
        new OkHttpClientCacheMetrics(client.cache(), "okhttp", Tags.of("foo", "bar"))
                .bindTo(registry);

        server.stubFor(any(anyUrl()).willReturn(
                aResponse().withHeader("Cache-Control", "public", "max-age=3600")
        ));
        Request request = new Request.Builder()
                .url(server.baseUrl())
                .build();
        client.newCall(request).execute().close();

        assertThat(registry.get("cache.size")
                .tags("foo", "bar", "cache", "okhttp")
                .gauge().value()).isGreaterThan(0);
    }

    @Test
    void gaugeCacheMax(@WiremockResolver.Wiremock WireMockServer server) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(new Cache(cacheDirectory, CACHE_SIZE_BYTES))
                .build();
        new OkHttpClientCacheMetrics(client.cache(), "okhttp", Tags.of("foo", "bar"))
                .bindTo(registry);

        assertThat(registry.get("cache.size")
                .tags("foo", "bar", "cache", "okhttp", "size", "max")
                .gauge().value()).isEqualTo(CACHE_SIZE_BYTES.doubleValue());
    }

    @Test
    void countGlobalRequestCounts(@WiremockResolver.Wiremock WireMockServer server) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(new Cache(cacheDirectory, CACHE_SIZE_BYTES))
                .build();
        new OkHttpClientCacheMetrics(client.cache(), "okhttp", Tags.of("foo", "bar"))
                .bindTo(registry);

        server.stubFor(any(anyUrl()));
        Request request = new Request.Builder()
                .url(server.baseUrl())
                .build();
        client.newCall(request).execute().close();
        client.newCall(request).execute().close();
        client.newCall(request).execute().close();

        assertThat(registry.get("cache.gets")
                .tags("foo", "bar", "result", "total")
                .functionCounter().count()).isEqualTo(3L);
        assertThat(registry.get("cache.gets")
                .tags("foo", "bar", "result", "miss")
                .functionCounter().count()).isEqualTo(3L);
        assertThat(registry.get("cache.gets")
                .tags("foo", "bar", "result", "hit")
                .functionCounter().count()).isEqualTo(0L);
    }

    @Test
    void countGlobalCacheRequestCount(@WiremockResolver.Wiremock WireMockServer server) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(new Cache(cacheDirectory, CACHE_SIZE_BYTES))
                .build();
        new OkHttpClientCacheMetrics(client.cache(), "okhttp", Tags.of("foo", "bar"))
                .bindTo(registry);

        server.stubFor(any(anyUrl()).willReturn(
                aResponse().withHeader("Cache-Control", "public", "max-age=3600")
        ));
        Request request = new Request.Builder()
                .url(server.baseUrl())
                .build();
        client.newCall(request).execute().close();
        client.newCall(request).execute().close();
        client.newCall(request).execute().close();

        assertThat(registry.get("cache.gets")
                .tags("foo", "bar", "result", "total")
                .functionCounter().count()).isEqualTo(3L);
        assertThat(registry.get("cache.gets")
                .tags("foo", "bar", "result", "miss")
                .functionCounter().count()).isEqualTo(1L);
        assertThat(registry.get("cache.gets")
                .tags("foo", "bar", "result", "hit")
                .functionCounter().count()).isEqualTo(2L);
    }
}
