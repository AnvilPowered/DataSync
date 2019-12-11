/*
 *     MSDataSync - MilSpecSG
 *     Copyright (C) 2019 Cableguy20
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package rocks.milspecsg.msdatasync.model.core.serializeditemstack;

import rocks.milspecsg.msrepository.datastore.json.annotation.JsonEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonEntity
public class JsonSerializedItemStack implements SerializedItemStack {

    private Map<String, Object> properties;

    @Override
    public Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        this.properties = Objects.requireNonNull(properties, "properties cannot be null");
    }
}
