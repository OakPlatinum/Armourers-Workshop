package moe.plushie.armourers_workshop.compatibility.client.layer;

import moe.plushie.armourers_workshop.api.annotation.Available;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;

@Available("[1.21, )")
@Environment(EnvType.CLIENT)
public class AbstractSkinnableLayers {

    public static final Class<?> VILLAGER_PROFESSION = VillagerProfessionLayer.class;

    public static final Class<?> DROWNED_OUTER = DrownedOuterLayer.class;

    public static final Class<?> STRAY_CLOTHING = SkeletonClothingLayer.class;
}
