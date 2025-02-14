package moe.plushie.armourers_workshop.core.client.skinrender.patch;

import com.mojang.blaze3d.vertex.PoseStack;
import moe.plushie.armourers_workshop.api.math.IPoseStack;
import moe.plushie.armourers_workshop.core.armature.thirdparty.EpicFlightTransformProvider;
import moe.plushie.armourers_workshop.core.client.bake.BakedArmatureTransformer;
import moe.plushie.armourers_workshop.core.client.other.EntityRenderData;
import moe.plushie.armourers_workshop.core.client.other.thirdparty.EpicFlightModel;
import moe.plushie.armourers_workshop.core.client.skinrender.SkinRendererManager2;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;

public class EpicFightEntityRendererPatch<T extends LivingEntity> extends EntityRenderPatch<T> {

    private EntityModel<?> entityModel;

    private IPoseStack overridePoseStack;
    private EpicFlightModel transformerModel;

    public EpicFightEntityRendererPatch(EntityRenderData renderData) {
        super(renderData);
    }

    public static <T extends LivingEntity> void activate(T entity, float partialTicks, int packedLight, PoseStack poseStackIn, LivingEntityRenderer<?, ?> entityRenderer, Consumer<EpicFightEntityRendererPatch<T>> handler) {
        _activate(EpicFightEntityRendererPatch.class, entity, partialTicks, packedLight, poseStackIn, entityRenderer, handler, renderData -> {
            var model = EpicFlightModel.ofNullable(entityRenderer.getModel());
            if (model != null) {
                return new EpicFightEntityRendererPatch<>(renderData);
            }
            return null;
        });
    }

    public static <T extends LivingEntity> void apply(T entity, PoseStack poseStackIn, MultiBufferSource bufferSourceIn, Consumer<EpicFightEntityRendererPatch<T>> handler) {
        _apply(EpicFightEntityRendererPatch.class, entity, poseStackIn, bufferSourceIn, handler);
    }

    public static <T extends LivingEntity> void deactivate(T entity, Consumer<EpicFightEntityRendererPatch<T>> handler) {
        _deactivate(EpicFightEntityRendererPatch.class, entity, handler);
    }

    @Override
    protected final void onInit(T entity, float partialTicks, int packedLight, PoseStack poseStackIn, EntityRenderer<?> entityRenderer) {
        if (entityRenderer instanceof LivingEntityRenderer) {
            onInit(entity, partialTicks, packedLight, poseStackIn, (LivingEntityRenderer<?, ?>) entityRenderer);
        }
    }

    protected void onInit(T entity, float partialTicks, int packedLight, PoseStack poseStackIn, LivingEntityRenderer<?, ?> entityRenderer) {
        super.onInit(entity, partialTicks, packedLight, poseStackIn, entityRenderer);
        var entityModel = entityRenderer.getModel();
        if (this.entityModel != entityModel) {
            this.entityModel = entityModel;
            this.transformerModel = EpicFlightModel.ofNullable(entityModel);
            this.transformer = createTransformer(entity, transformerModel, entityRenderer);
        }
    }


    public void setFirstPerson(boolean isFirstPerson) {
        if (transformer == null) {
            return;
        }
        if (isFirstPerson) {
            transformer.setFilter(joint -> !joint.getName().equals("Head") && !joint.getName().equals("Chest") && !joint.getName().equals("Torso"));
        } else {
            transformer.setFilter(null);
        }
    }

    public void setMesh(Object mesh) {
        if (transformerModel != null) {
            transformerModel.linkTo(mesh);
        }
    }

    public void setTransformProvider(EpicFlightTransformProvider transformProvider) {
        if (transformerModel != null) {
            transformerModel.setAssociatedObject(transformProvider, EpicFlightTransformProvider.KEY);
        }
    }

    public void setOverridePose(IPoseStack pose) {
        overridePoseStack = pose;
    }

    public IPoseStack getOverridePose() {
        if (overridePoseStack != null) {
            return overridePoseStack;
        }
        return pluginContext.getPoseStack();
    }

    private BakedArmatureTransformer createTransformer(Entity entity, EpicFlightModel model, LivingEntityRenderer<?, ?> entityRenderer) {
        if (model != null) {
            var transformer = SkinRendererManager2.EPICFIGHT.getTransformer(entity.getType(), model);
            if (transformer != null) {
                return BakedArmatureTransformer.create(transformer, entityRenderer);
            }
        }
        return null;
    }
}
