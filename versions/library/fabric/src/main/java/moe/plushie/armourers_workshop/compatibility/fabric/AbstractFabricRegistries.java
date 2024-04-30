package moe.plushie.armourers_workshop.compatibility.fabric;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.common.IItemTag;
import moe.plushie.armourers_workshop.utils.TypedRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import manifold.ext.rt.api.auto;

@Available("[1.21, )")
public class AbstractFabricRegistries {

    public static final auto BLOCKS = TypedRegistry.create(Block.class, BuiltInRegistries.BLOCK);

    public static final auto ITEMS = TypedRegistry.create(Item.class, BuiltInRegistries.ITEM);
    public static final auto ITEM_GROUPS = TypedRegistry.create(CreativeModeTab.class, BuiltInRegistries.CREATIVE_MODE_TAB);
    public static final auto ITEM_LOOT_FUNCTIONS = TypedRegistry.create(LootItemFunctionType.class, BuiltInRegistries.LOOT_FUNCTION_TYPE);
    public static final auto ITEM_TAGS = TypedRegistry.factory(IItemTag.class, registryName -> {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, registryName);
        return itemStack -> itemStack.is(tag);
    });

    public static final auto MENU_TYPES = TypedRegistry.create(MenuType.class, BuiltInRegistries.MENU);
    public static final auto ENTITY_TYPES = TypedRegistry.create(EntityType.class, BuiltInRegistries.ENTITY_TYPE);
    public static final auto BLOCK_ENTITY_TYPES = TypedRegistry.create(BlockEntityType.class, BuiltInRegistries.BLOCK_ENTITY_TYPE);
    public static final auto SOUND_EVENTS = TypedRegistry.create(SoundEvent.class, BuiltInRegistries.SOUND_EVENT);

    public static final auto ENTITY_DATA_SERIALIZER = TypedRegistry.map(EntityDataSerializer.class, (registryName, value) -> {
        // register to real item.
        EntityDataSerializers.registerSerializer(value);
    });

    public static final auto DATA_COMPONENT_TYPES = TypedRegistry.create(DataComponentType.class, BuiltInRegistries.DATA_COMPONENT_TYPE);
}
