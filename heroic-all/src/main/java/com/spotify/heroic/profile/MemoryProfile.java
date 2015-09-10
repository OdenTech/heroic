/*
 * Copyright (c) 2015 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.heroic.profile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.heroic.HeroicConfig;
import com.spotify.heroic.HeroicProfile;
import com.spotify.heroic.aggregationcache.AggregationCacheModule;
import com.spotify.heroic.aggregationcache.InMemoryAggregationCacheBackendConfig;
import com.spotify.heroic.cluster.ClusterManagerModule;
import com.spotify.heroic.elasticsearch.ManagedConnectionFactory;
import com.spotify.heroic.elasticsearch.StandaloneClientSetup;
import com.spotify.heroic.elasticsearch.index.RotatingIndexMapping;
import com.spotify.heroic.metadata.MetadataManagerModule;
import com.spotify.heroic.metadata.MetadataModule;
import com.spotify.heroic.metadata.elasticsearch.ElasticsearchMetadataModule;
import com.spotify.heroic.metric.MetricManagerModule;
import com.spotify.heroic.metric.MetricModule;
import com.spotify.heroic.metric.memory.MemoryMetricModule;
import com.spotify.heroic.suggest.SuggestManagerModule;
import com.spotify.heroic.suggest.SuggestModule;
import com.spotify.heroic.suggest.elasticsearch.ElasticsearchSuggestModule;

public class MemoryProfile implements HeroicProfile {
    @Override
    public HeroicConfig build() throws Exception {
        // @formatter:off
        // final SuggestManagerModule suggest = SuggestManagerModule.create(suggestModules, null);
        return HeroicConfig.builder()
            .cluster(
                ClusterManagerModule.builder()
                .tags(ImmutableMap.of("site", "local"))
                .build()
            )
            .metric(
                MetricManagerModule.builder()
                    .backends(ImmutableList.<MetricModule>of(
                        MemoryMetricModule.builder()
                            .build()
                    ))
                    .build()
            )
            .metadata(
                MetadataManagerModule.builder()
                    .backends(ImmutableList.<MetadataModule>of(
                        ElasticsearchMetadataModule.builder()
                            .connection(
                                ManagedConnectionFactory.builder()
                                .clientSetup(
                                    StandaloneClientSetup.builder().build()
                                )
                                .index(
                                    RotatingIndexMapping.builder()
                                    .pattern("heroic-metadata-v1-%s")
                                    .build()
                                )
                                .build()
                            )
                            .build()
                    ))
                    .build()
            )
            .suggest(
                SuggestManagerModule.builder()
                    .backends(ImmutableList.<SuggestModule>of(
                        ElasticsearchSuggestModule.builder()
                            .connection(
                                ManagedConnectionFactory.builder()
                                .clientSetup(
                                    StandaloneClientSetup.builder().build()
                                )
                                .index(
                                    RotatingIndexMapping.builder()
                                    .pattern("heroic-suggest-v1-%s")
                                    .build()
                                )
                                .build()
                            )
                            .build()
                    ))
                    .build()
            )
            .cache(
                AggregationCacheModule.builder()
                    .backend(InMemoryAggregationCacheBackendConfig.builder().build())
                    .build()
            )
            .build();
        // @formatter:on
    }

    @Override
    public String description() {
        return "Profile that sets up a completely in-memory, generated data, heroic instance.";
    }
}