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

import java.util.Set;
import java.util.stream.Collectors;

import org.hawkular.agent.monitor.util.Util;

/**
 * @author Joel Takvorian
 */
class InventoryBlob {
    protected final String feed;
    protected final String type;
    protected final String id;
    protected final Set<String> resourceTypes;

    private InventoryBlob(String feed, String type, String id, Set<String> resourceTypes) {
        this.feed = feed;
        this.type = type;
        this.id = id;
        this.resourceTypes = resourceTypes;
    }

    static InventoryBlob resource(String feed, String id, Set<String> resourceTypes) {
        return new InventoryBlob(feed, "r", id, resourceTypes);
    }

    static InventoryBlob resourceType(String feed, String id) {
        return new InventoryBlob(feed, "rt", id, null);
    }

    static InventoryBlob metricType(String feed, String id) {
        return new InventoryBlob(feed, "mt", id, null);
    }

    String getFeed() {
        return feed;
    }

    String getType() {
        return type;
    }

    String getId() {
        return id;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InventoryBlob that = (InventoryBlob) o;

        if (feed != null ? !feed.equals(that.feed) : that.feed != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override public int hashCode() {
        int result = feed != null ? feed.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return name();
    }

    public String name() {
        return "inventory." + feed + "." + type + "." + id;
    }

    public String encodedName() {
        return Util.urlEncode(name());
    }

    public WithData full(byte[] data) {
        return new WithData(feed, type, id, resourceTypes, data);
    }

    static class WithData extends InventoryBlob {
        private byte[] inventoryStructure;

        private WithData(String feed,
                         String type,
                         String id,
                         Set<String> resourceTypes,
                         byte[] inventoryStructure) {
            super(feed, type, id, resourceTypes);
            this.inventoryStructure = inventoryStructure;
        }

        BlobStoreEntry toStoreEntry() {
            BlobStoreEntry def = new BlobStoreEntry(name(), inventoryStructure);
            def.addTag("module", "inventory");
            def.addTag("feed", feed);
            def.addTag("type", type);
            def.addTag("id", id);
            if (resourceTypes != null) {
                def.addTag("restypes",
                        "|" + resourceTypes.stream().collect(Collectors.joining("|")) + "|");
            }
            return def;
        }
    }
}
