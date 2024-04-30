package moe.plushie.armourers_workshop.compatibility.extensions.net.minecraft.server.packs.resources.ResourceManager;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.common.IPackResources;
import moe.plushie.armourers_workshop.utils.ObjectUtils;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.ArrayList;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

@Available("[1.18, )")
@Extension
public class Converter {

    public static ResourceManager asBundleManager(@This ResourceManager resourceManager) {
        ArrayList<PackResources> resources = new ArrayList<>();
        resourceManager.listPacks().forEach(it -> {
            // bundle data only contain data pack on mods.
            IPackResources res = ObjectUtils.safeCast(it, IPackResources.class);
            if (res != null && res.isModBundled()) {
                resources.add(it);
            }
        });
        return new MultiPackResourceManager(PackType.SERVER_DATA, resources);
    }
}
