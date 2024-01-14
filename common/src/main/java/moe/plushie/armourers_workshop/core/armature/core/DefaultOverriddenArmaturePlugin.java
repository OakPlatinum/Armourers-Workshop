package moe.plushie.armourers_workshop.core.armature.core;

import moe.plushie.armourers_workshop.api.client.model.IModel;
import moe.plushie.armourers_workshop.api.client.model.IModelPart;
import moe.plushie.armourers_workshop.api.skin.property.ISkinProperty;
import moe.plushie.armourers_workshop.core.armature.ArmaturePlugin;
import moe.plushie.armourers_workshop.core.client.other.SkinOverriddenManager;
import moe.plushie.armourers_workshop.core.client.other.SkinRenderContext;
import moe.plushie.armourers_workshop.core.client.other.SkinRenderData;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperty;
import moe.plushie.armourers_workshop.init.ModDebugger;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class DefaultOverriddenArmaturePlugin extends ArmaturePlugin {

    private final ArrayList<IModelPart> applying = new ArrayList<>();
    private final HashMap<ISkinProperty<Boolean>, Collection<IModelPart>> overrides = new HashMap<>();

    public DefaultOverriddenArmaturePlugin(IModel model, HashMap<String, Collection<String>> overrides) {
        overrides.forEach((key, names) -> {
            // NOTE: we assume that all default values is false.
            ISkinProperty<Boolean> property = SkinProperty.normal(key, false);
            Collection<IModelPart> parts = buildParts(names, model);
            this.overrides.put(property, parts);
        });
    }

    @Override
    public void activate(Entity entity, SkinRenderContext context) {
        SkinRenderData renderData = context.getRenderData();
        SkinOverriddenManager overriddenManager = renderData.getOverriddenManager();

        overriddenManager.willRender(entity);

        // Limit the players limbs if they have a skirt equipped.
        // A proper lady should not swing her legs around!
        if (entity instanceof LivingEntity && renderData.isLimitLimbs()) {
            ((LivingEntity) entity).applyLimitLimbs();
        }


        // apply
        overrides.forEach((key, value) -> {
            if (overriddenManager.contains(key)) {
                value.forEach(this::hidden);
            }
        });
    }

    @Override
    public void deactivate(Entity entity, SkinRenderContext context) {
        SkinRenderData renderData = context.getRenderData();
        SkinOverriddenManager overriddenManager = renderData.getOverriddenManager();

        overriddenManager.didRender(entity);

        applying.forEach(it -> it.setVisible(true));
        applying.clear();
    }

    @Override
    public boolean freeze() {
        return !overrides.isEmpty();
    }

    private void hidden(IModelPart part) {
        // ..
        if (ModDebugger.modelOverride) {
            return;
        }
        if (part.isVisible()) {
            part.setVisible(false);
            applying.add(part);
        }
    }

    private Collection<IModelPart> buildParts(Collection<String> names, IModel model) {
        // '*' will wildcard all parts.
        if (names.contains("*")) {
            return model.getAllParts();
        }
        // find all parts and remove duplicates.
        LinkedHashMap<String, IModelPart> parts = new LinkedHashMap<>();
        for (String name : names) {
            IModelPart part = model.getPart(name);
            if (part != null) {
                parts.put(name, part);
            }
        }
        return parts.values();
    }
}
