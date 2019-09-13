package rocks.milspecsg.msdatasync.service.implementation.data;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import rocks.milspecsg.msdatasync.model.core.SerializedItemStack;
import rocks.milspecsg.msdatasync.model.core.Snapshot;
import rocks.milspecsg.msdatasync.service.data.ApiInventorySerializer;

import java.util.*;
import java.util.stream.Collectors;

public class ApiSpongeInventorySerializer extends ApiInventorySerializer<Snapshot, Key, User, Inventory, ItemStackSnapshot> {

    private static char SEPARATOR = '_';
    private static char PERIOD_REPLACEMENT = '-';

    @Override
    public boolean serializeInventory(Snapshot snapshot, Inventory inventory, int maxSlots) {
        try {
            boolean success = true;
            List<SerializedItemStack> itemStacks = new ArrayList<>();
            Iterator<Inventory> iterator = inventory.slots().iterator();

            for (int i = 0; i < maxSlots; i++) {
                if (!iterator.hasNext()) break;
                Inventory slot = iterator.next();
                SerializedItemStack serializedItemStack = new SerializedItemStack();
                ItemStack stack = slot.peek().orElse(ItemStack.empty());
                DataContainer dc = stack.toContainer();
                try {
                    serializedItemStack.properties = serialize(dc.getValues(false));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("[MSDataSync] There was an error while serializing slot " + i + " with item " + stack.getType().getId() + "! Will not add this item to snapshot!");
                    success = false;
                    continue;
                }
                itemStacks.add(serializedItemStack);
            }
            snapshot.itemStacks = itemStacks;
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean serializeInventory(Snapshot snapshot, Inventory inventory) {
        return serializeInventory(snapshot, inventory, 41);
    }

    @Override
    public boolean serialize(Snapshot snapshot, User user) {
        return serializeInventory(snapshot, user.getInventory());
    }

    @Override
    public boolean deserializeInventory(Snapshot snapshot, Inventory inventory, ItemStackSnapshot fallbackItemStackSnapshot) {
        try {
            inventory.clear();
            Iterator<SerializedItemStack> stacks = snapshot.itemStacks.iterator();
            for (Iterator<Inventory> slots = inventory.slots().iterator(); slots.hasNext(); ) {
                Inventory slot = slots.next();
                if (stacks.hasNext()) {
                    SerializedItemStack stack = stacks.next();
                    try {
                        DataContainer dc = DataContainer.createNew(DataView.SafetyMode.ALL_DATA_CLONED);
                        deserialize(stack.properties).forEach(dc::set);
                        ItemStack is = ItemStack.builder().fromContainer(dc).build();
                        slot.set(is);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (!(inventory instanceof PlayerInventory)) {
                    if (slots.hasNext()) {
                        slot.set(fallbackItemStackSnapshot.createStack());
                    } else {
                        slot.set(exitWithoutSavingItemStackSnapshot.createStack());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean deserializeInventory(Snapshot snapshot, Inventory inventory) {
        return deserializeInventory(snapshot, inventory, defaultFallbackItemStackSnapshot);
    }

    @Override
    public boolean deserialize(Snapshot snapshot, User user) {
        return deserializeInventory(snapshot, user.getInventory());
    }

    private static Map<String, Object> serialize(Map<DataQuery, Object> values) {
        Map<String, Object> result = new HashMap<>();
        values.forEach((dq, o) -> {
            String s = dq.asString(SEPARATOR).replace('.', PERIOD_REPLACEMENT);
            if (o instanceof Map) {
                Object m = serialize((Map<DataQuery, Object>) o);
                result.put(s, m);
            } else if (o instanceof List) {
                List<?> list = (List<?>) o;
                List<Object> r1 = new ArrayList<>();
                list.forEach(li -> {
                    if (li instanceof DataContainer) {
                        r1.add(serialize(((DataContainer) li).getValues(false)));
                    } else if (li instanceof String) {
                        r1.add(((String) li).replace('.', PERIOD_REPLACEMENT));
                    }
                });
                result.put(s, r1);
            } else {
                result.put(s, o);
            }
        });
        return result;
    }

    private static Map<DataQuery, Object> deserialize(Map<String, Object> values) {
        Map<DataQuery, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> e : values.entrySet()) {
            String s = e.getKey();
            Object o = e.getValue();
            if (o == null) {
                continue;
            }
            s = s.replace('-', '.');
            DataQuery dq = DataQuery.of(SEPARATOR, s);
            if (o instanceof Map) {
                Map<String, Object> m = (Map<String, Object>) o;
                Map<DataQuery, Object> r1 = new HashMap<>();
                for (Map.Entry<String, Object> mapEntry : m.entrySet()) {
                    String s1 = mapEntry.getKey();
                    Object m1 = mapEntry.getValue();
                    if (m1 == null) {
                        continue;
                    } else if (m1 instanceof List) {
                        m1 = ((List<?>) m1).stream().filter(Objects::nonNull).collect(Collectors.toList());
                    }
                    Object value = m1;
                    s1 = s1.replace(PERIOD_REPLACEMENT, '.');
                    if (m1 instanceof Map) {
                        try {
                            Map<DataQuery, Object> v = deserialize((Map<String, Object>) m1);
                            DataContainer dc = DataContainer.createNew(DataView.SafetyMode.ALL_DATA_CLONED);
                            v.forEach(dc::set);
                            if (s1.equals("ench")) {
                                value = ImmutableList.of(dc);
                            } else {
                                value = dc;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    r1.put(DataQuery.of(SEPARATOR, s1), value);
                }
                result.put(dq, r1);
            } else if (!s.equals("ItemType") && o instanceof String) {
                String n = o.toString().replace(PERIOD_REPLACEMENT, '.');
                result.put(dq, n);
            } else if (o instanceof List) {
                result.put(dq, ((List<?>) o).stream().filter(Objects::nonNull).collect(Collectors.toList()));
            } else {
                result.put(dq, o);
            }
        }
        return result;
    }

    private ItemStackSnapshot defaultFallbackItemStackSnapshot =
        ItemStack.builder().itemType(ItemTypes.BARRIER).quantity(1).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Not an actual slot")).build().createSnapshot();

    @Override
    public ItemStackSnapshot getDefaultFallbackItemStackSnapshot() {
        return defaultFallbackItemStackSnapshot;
    }

    private ItemStackSnapshot exitWithoutSavingItemStackSnapshot =
        ItemStack.builder().itemType(ItemTypes.GOLD_INGOT).quantity(1).add(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Exit without saving")).build().createSnapshot();

    @Override
    public ItemStackSnapshot getExitWithoutSavingItemStackSnapshot() {
        return exitWithoutSavingItemStackSnapshot;
    }
}
