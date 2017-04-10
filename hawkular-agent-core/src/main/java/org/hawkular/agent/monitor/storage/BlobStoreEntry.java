/*
 * Copyright 2015-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.agent.monitor.storage;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * For h*metrics "StoreEntry" json
 * @author Joel Takvorian
 */
class BlobStoreEntry implements Serializable {
    @JsonProperty("key")
    private final String key;
    @JsonProperty("value")
    private final byte[] value;
    @JsonProperty("tags")
    private final Map<String, String> tags = new TreeMap<>();

    BlobStoreEntry(String key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    void addTag(String key, String value) {
        tags.put(key, value);
    }

    public String getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public Map<String, String> getTags() {
        return tags;
    }
}
