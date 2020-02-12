/*
 *   DataSync - AnvilPowered
 *   Copyright (C) 2020 Cableguy20
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

package org.anvilpowered.datasync.common.data.registry;

import com.google.inject.Singleton;
import org.anvilpowered.anvil.api.data.key.Keys;
import org.anvilpowered.anvil.base.data.registry.BaseExtendedRegistry;

@Singleton
public class CommonRegistry extends BaseExtendedRegistry {

    public CommonRegistry() {
        setDefault(Keys.BASE_SCAN_PACKAGE, "org.anvilpowered.datasync.common.model");
    }
}
