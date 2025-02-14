package moe.plushie.armourers_workshop.core.skin.property;

import moe.plushie.armourers_workshop.api.skin.property.ISkinProperties;
import moe.plushie.armourers_workshop.api.skin.property.ISkinProperty;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IInputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IOutputStream;
import moe.plushie.armourers_workshop.utils.ObjectUtils;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SkinProperties implements ISkinProperties {

    public static final SkinProperties EMPTY = new SkinProperties();

    protected final LinkedHashMap<String, Object> properties;

    public SkinProperties() {
        this.properties = new LinkedHashMap<>();
    }

    public SkinProperties(SkinProperties properties) {
        this.properties = new LinkedHashMap<>(properties.properties);
    }

    public SkinProperties(CompoundTag tag) {
        this();
        this.readFromNBT(tag);
    }

    public SkinProperties(LinkedHashMap<String, Object> properties) {
        this.properties = properties;
    }


    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public <T> T get(ISkinProperty<T> property) {
        var value = properties.getOrDefault(property.getKey(), property.getDefaultValue());
        return ObjectUtils.unsafeCast(value);
    }

    @Override
    public <T> void put(ISkinProperty<T> property, T value) {
        if (Objects.equals(value, property.getDefaultValue())) {
            properties.remove(property.getKey());
        } else {
            properties.put(property.getKey(), value);
        }
    }

    public void putAll(SkinProperties properties) {
        this.properties.putAll(properties.properties);
    }

    public void clear() {
        properties.clear();
    }

    @Override
    public <T> void remove(ISkinProperty<T> property) {
        properties.remove(property.getKey());
    }

    @Override
    public <T> boolean containsKey(ISkinProperty<T> property) {
        return properties.containsKey(property.getKey());
    }

    @Override
    public <T> boolean containsValue(ISkinProperty<T> property) {
        return properties.containsValue(property.getKey());
    }

    public void put(String key, Object value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return properties.entrySet();
    }

    public ArrayList<String> getPropertiesList() {
        var list = new ArrayList<String>();
        for (int i = 0; i < properties.size(); i++) {
            var key = (String) properties.keySet().toArray()[i];
            list.add(key + ":" + properties.get(key));
        }
        return list;
    }

    public void writeToStream(IOutputStream stream) throws IOException {
        stream.writeInt(properties.size());
        for (int i = 0; i < properties.size(); i++) {
            var key = (String) properties.keySet().toArray()[i];
            var value = properties.get(key);
            stream.writeString(key);
            if (value instanceof String stringValue) {
                stream.writeByte(DataTypes.STRING.ordinal());
                stream.writeString(stringValue);
            }
            if (value instanceof Integer intValue) {
                stream.writeByte(DataTypes.INT.ordinal());
                stream.writeInt(intValue);
            }
            if (value instanceof Double doubleValue) {
                stream.writeByte(DataTypes.DOUBLE.ordinal());
                stream.writeDouble(doubleValue);
            }
            if (value instanceof Boolean boolValue) {
                stream.writeByte(DataTypes.BOOLEAN.ordinal());
                stream.writeBoolean(boolValue);
            }
            if (value instanceof Collection<?> listValue) {
                stream.writeByte(DataTypes.LIST.ordinal());
                stream.writeInt(listValue.size());
                // TODO: NO IMPL
            }
            if (value instanceof SkinProperties compoundValue) {
                stream.writeByte(DataTypes.COMPOUND.ordinal());
                compoundValue.writeToStream(stream);
            }
        }
    }

    public void readFromStream(IInputStream stream) throws IOException {
        int count = stream.readInt();
        for (int i = 0; i < count; i++) {
            var key = stream.readString();
            var byteType = stream.readByte();
            var type = DataTypes.byId(byteType);
            if (type == null) {
                throw new IOException("Error loading skin properties " + byteType);
            }
            properties.put(key, switch (type) {
                case STRING -> stream.readString();
                case INT -> stream.readInt();
                case DOUBLE -> stream.readDouble();
                case BOOLEAN -> stream.readBoolean();
                case LIST -> {
                    int size = stream.readInt();
                    // TODO: NO IMPL
                    yield new ArrayList<>(size);
                }
                case COMPOUND -> {
                    var properties1 = new SkinProperties();
                    properties1.readFromStream(stream);
                    yield properties1;
                }
            });
        }
    }

    public SkinProperties slice(int index) {
        return new SkinProperties.Stub(this, index);
    }

    public SkinProperties copy() {
        return new SkinProperties(this);
    }

    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        writeToNBT(tag);
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkinProperties that)) return false;
        return properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    @Override
    public String toString() {
        return "SkinProperties [properties=" + properties + "]";
    }

    public void readFromNBT(CompoundTag nbt) {
        for (String key : nbt.getAllKeys()) {
            Tag value = nbt.get(key);
            if (value instanceof StringTag stringTag) {
                properties.put(key, stringTag.getAsString());
            } else if (value instanceof IntTag intTag) {
                properties.put(key, intTag.getAsInt());
            } else if (value instanceof FloatTag floatTag) {
                properties.put(key, floatTag.getAsFloat());
            } else if (value instanceof DoubleTag doubleTag) {
                properties.put(key, doubleTag.getAsDouble());
            } else if (value instanceof ByteTag byteTag) {
                properties.put(key, byteTag.getAsByte() != 0);
            } else if (value instanceof ListTag listTag) {
                // TODO: NO IMPL
            } else if (value instanceof CompoundTag compoundTag) {
                var compoundValue = new SkinProperties();
                compoundValue.readFromNBT(compoundTag);
                properties.put(key, compoundValue);
            }
        }
    }

    public void writeToNBT(CompoundTag nbt) {
        properties.forEach((key, value) -> {
            if (value instanceof String stringValue) {
                nbt.putString(key, stringValue);
            } else if (value instanceof Integer intValue) {
                nbt.putInt(key, intValue);
            } else if (value instanceof Float floatValue) {
                nbt.putDouble(key, floatValue);
            } else if (value instanceof Double doubleValue) {
                nbt.putDouble(key, doubleValue);
            } else if (value instanceof Boolean boolValue) {
                nbt.putBoolean(key, boolValue);
            } else if (value instanceof Collection<?> listValue) {
                // TODO: NO IMPL
            } else if (value instanceof SkinProperties compoundValue) {
                var compoundTag = new CompoundTag();
                compoundValue.writeToNBT(compoundTag);
                nbt.put(key, compoundTag);
            }
        });
    }

    public enum DataTypes {
        STRING, INT, DOUBLE, BOOLEAN, LIST, COMPOUND;

        @Nullable
        public static DataTypes byId(int id) {
            if (id >= 0 & id < DataTypes.values().length) {
                return DataTypes.values()[id];
            }
            return null;
        }
    }

    public static class Stub extends SkinProperties {
        private final int index;

        public Stub(SkinProperties paranet, int index) {
            super(paranet.properties);
            this.index = index;
        }

        @Override
        public <T> void put(ISkinProperty<T> property, T value) {
            String indexedKey = getResolvedKey(property);
            if (indexedKey != null) {
                properties.put(indexedKey, value);
            } else {
                properties.put(property.getKey(), value);
            }
        }

        @Override
        public <T> void remove(ISkinProperty<T> property) {
            String indexedKey = getResolvedKey(property);
            if (indexedKey != null) {
                properties.remove(indexedKey);
            } else {
                properties.remove(property.getKey());
            }
        }

        @Override
        public <T> T get(ISkinProperty<T> property) {
            var indexedKey = getResolvedKey(property);
            Object value;
            if (indexedKey != null && properties.containsKey(indexedKey)) {
                value = properties.getOrDefault(indexedKey, property.getDefaultValue());
            } else {
                value = properties.getOrDefault(property.getKey(), property.getDefaultValue());
            }
            return ObjectUtils.unsafeCast(value);
        }

        @Override
        public <T> boolean containsKey(ISkinProperty<T> property) {
            String indexedKey = getResolvedKey(property);
            if (indexedKey != null && properties.containsKey(indexedKey)) {
                return true;
            }
            return properties.containsKey(property.getKey());
        }

        @Override
        public <T> boolean containsValue(ISkinProperty<T> property) {
            var indexedKey = getResolvedKey(property);
            if (indexedKey != null && properties.containsValue(indexedKey)) {
                return true;
            }
            return properties.containsValue(property.getKey());
        }

        @Nullable
        private <T> String getResolvedKey(ISkinProperty<T> property) {
            if (property instanceof SkinProperty<?> property1) {
                if (property1.isMultipleKey()) {
                    return property.getKey() + index;
                }
            }
            return null;
        }
    }


    public static class Changes extends SkinProperties {

        public Changes() {
        }

        public Changes(CompoundTag tag) {
            readFromNBT(tag);
        }

        @Override
        public <T> void put(ISkinProperty<T> property, T value) {
            properties.put(property.getKey(), value);
        }

        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            writeToNBT(tag);
            return tag;
        }
    }
}
