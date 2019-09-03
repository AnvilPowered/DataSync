package rocks.milspecsg.msdatasync.model.core;

import org.mongodb.morphia.annotations.Entity;
import rocks.milspecsg.msrepository.model.Dbo;

import java.util.List;
import java.util.Map;

@Entity("snapshots")
public class Snapshot extends Dbo {

    public String name;

    public Map<String, Object> keys;

    public List<SerializedItemStack> itemStacks;

}
