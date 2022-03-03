/*
 * Copyright 2017 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument.binder.jersey.server;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Tag;
import org.glassfish.jersey.server.monitoring.RequestEvent;

/**
 * Provides {@link Tag Tags} for Jersey request metrics.
 *
 * @deprecated Scheduled for removal in 2.0.0, please use {@code io.micrometer.binder.jersey.server.JerseyTagsProvider}
 * @author Michael Weirauch
 * @since 1.8.0
 */
@Deprecated
public interface JerseyTagsProvider {

    /**
     * Provides tags to be associated with metrics for the given {@code event}.
     *
     * @param event
     *            the request event
     * @return tags to associate with metrics recorded for the request
     */
    Iterable<Tag> httpRequestTags(RequestEvent event);

    /**
     * Provides tags to be associated with the
     * {@link LongTaskTimer} which instruments the
     * given long-running {@code event}.
     *
     * @param event
     *            the request event
     * @return tags to associate with metrics recorded for the request
     */
    Iterable<Tag> httpLongRequestTags(RequestEvent event);

}
