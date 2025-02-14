package moe.plushie.armourers_workshop.builder.client.render;

import com.apple.library.uikit.UIColor;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import moe.plushie.armourers_workshop.api.armature.IJointTransform;
import moe.plushie.armourers_workshop.api.client.IBufferSource;
import moe.plushie.armourers_workshop.api.math.IPoseStack;
import moe.plushie.armourers_workshop.api.skin.ISkinPartType;
import moe.plushie.armourers_workshop.builder.blockentity.AdvancedBuilderBlockEntity;
import moe.plushie.armourers_workshop.builder.client.gui.advancedbuilder.guide.AbstractAdvancedGuideRenderer;
import moe.plushie.armourers_workshop.builder.client.gui.advancedbuilder.guide.AdvancedBlockGuideRenderer;
import moe.plushie.armourers_workshop.builder.client.gui.advancedbuilder.guide.AdvancedBoatGuideRenderer;
import moe.plushie.armourers_workshop.builder.client.gui.advancedbuilder.guide.AdvancedHorseGuideRenderer;
import moe.plushie.armourers_workshop.builder.client.gui.advancedbuilder.guide.AdvancedHumanGuideRenderer;
import moe.plushie.armourers_workshop.builder.client.gui.advancedbuilder.guide.AdvancedItemGuideRenderer;
import moe.plushie.armourers_workshop.builder.client.gui.advancedbuilder.guide.AdvancedMinecartGuideRenderer;
import moe.plushie.armourers_workshop.compatibility.api.AbstractItemTransformType;
import moe.plushie.armourers_workshop.compatibility.client.AbstractPoseStack;
import moe.plushie.armourers_workshop.compatibility.client.renderer.AbstractBlockEntityRenderer;
import moe.plushie.armourers_workshop.core.client.bake.BakedArmature;
import moe.plushie.armourers_workshop.core.client.bake.BakedSkinPart;
import moe.plushie.armourers_workshop.core.client.other.PlaceholderManager;
import moe.plushie.armourers_workshop.core.client.other.SkinModelManager;
import moe.plushie.armourers_workshop.core.client.other.SkinRenderTesselator;
import moe.plushie.armourers_workshop.core.data.ticket.Tickets;
import moe.plushie.armourers_workshop.core.data.transform.SkinItemTransforms;
import moe.plushie.armourers_workshop.core.skin.document.SkinDocument;
import moe.plushie.armourers_workshop.core.skin.document.SkinDocumentNode;
import moe.plushie.armourers_workshop.core.skin.document.SkinDocumentType;
import moe.plushie.armourers_workshop.core.skin.document.SkinDocumentTypes;
import moe.plushie.armourers_workshop.core.skin.part.SkinPartTypes;
import moe.plushie.armourers_workshop.init.ModDebugger;
import moe.plushie.armourers_workshop.utils.MathUtils;
import moe.plushie.armourers_workshop.utils.RenderSystem;
import moe.plushie.armourers_workshop.utils.ShapeTesselator;
import moe.plushie.armourers_workshop.utils.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

@Environment(EnvType.CLIENT)
public class AdvancedBuilderBlockRenderer<T extends AdvancedBuilderBlockEntity> extends AbstractBlockEntityRenderer<T> {

    private static final ImmutableMap<SkinDocumentType, AbstractAdvancedGuideRenderer> GUIDES = ImmutableMap.<SkinDocumentType, AbstractAdvancedGuideRenderer>builder()

            .put(SkinDocumentTypes.GENERAL_ARMOR_HEAD, new AdvancedHumanGuideRenderer())
            .put(SkinDocumentTypes.GENERAL_ARMOR_CHEST, new AdvancedHumanGuideRenderer())
            .put(SkinDocumentTypes.GENERAL_ARMOR_FEET, new AdvancedHumanGuideRenderer())
            .put(SkinDocumentTypes.GENERAL_ARMOR_LEGS, new AdvancedHumanGuideRenderer())
            .put(SkinDocumentTypes.GENERAL_ARMOR_WINGS, new AdvancedHumanGuideRenderer())
            .put(SkinDocumentTypes.GENERAL_ARMOR_OUTFIT, new AdvancedHumanGuideRenderer())

            .put(SkinDocumentTypes.ITEM, new AdvancedItemGuideRenderer())
            .put(SkinDocumentTypes.ITEM_AXE, new AdvancedItemGuideRenderer())
            .put(SkinDocumentTypes.ITEM_HOE, new AdvancedItemGuideRenderer())
            .put(SkinDocumentTypes.ITEM_SHOVEL, new AdvancedItemGuideRenderer())
            .put(SkinDocumentTypes.ITEM_PICKAXE, new AdvancedItemGuideRenderer())

            .put(SkinDocumentTypes.ITEM_SWORD, new AdvancedItemGuideRenderer())
            .put(SkinDocumentTypes.ITEM_SHIELD, new AdvancedItemGuideRenderer())
            .put(SkinDocumentTypes.ITEM_BOW, new AdvancedItemGuideRenderer())
            .put(SkinDocumentTypes.ITEM_TRIDENT, new AdvancedItemGuideRenderer())

            .put(SkinDocumentTypes.ENTITY_BOAT, new AdvancedBoatGuideRenderer())
            .put(SkinDocumentTypes.ENTITY_MINECART, new AdvancedMinecartGuideRenderer())

            .put(SkinDocumentTypes.ENTITY_HORSE, new AdvancedHorseGuideRenderer())

            .put(SkinDocumentTypes.BLOCK, new AdvancedBlockGuideRenderer())

            .build();


    ImmutableSet<ISkinPartType> USE_ITEM_TRANSFORMERS = ImmutableSet.<ISkinPartType>builder()
            .add(SkinPartTypes.ITEM)
            .add(SkinPartTypes.ITEM_AXE)
            .add(SkinPartTypes.ITEM_HOE)
            .add(SkinPartTypes.ITEM_SHOVEL)
            .add(SkinPartTypes.ITEM_PICKAXE)
            .add(SkinPartTypes.ITEM_SWORD)
            .add(SkinPartTypes.ITEM_SHIELD)
            .add(SkinPartTypes.ITEM_BOW0)
            .add(SkinPartTypes.ITEM_BOW1)
            .add(SkinPartTypes.ITEM_BOW2)
            .add(SkinPartTypes.ITEM_BOW3)
            .add(SkinPartTypes.ITEM_TRIDENT)
            .build();

    public static ArrayList<Vector3f> OUTPUTS = new ArrayList<>();
    public static HashSet<BakedSkinPart> RESULTS = new HashSet<>();

    public static void setOutput(int i, Vector3f pt) {
        while (i >= OUTPUTS.size()) {
            OUTPUTS.add(Vector3f.ZERO);
        }
        OUTPUTS.set(i, pt);
    }

    public static void setResult(Collection<BakedSkinPart> results) {
        RESULTS.clear();
        RESULTS.addAll(results);
    }

    public AdvancedBuilderBlockRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float partialTicks, IPoseStack poseStack, IBufferSource bufferSource, int light, int overlay) {
        poseStack.pushPose();
        poseStack.translate(entity.offset.getX(), entity.offset.getY(), entity.offset.getZ());
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.scale(entity.carmeScale.getX(), entity.carmeScale.getY(), entity.carmeScale.getZ());

        poseStack.scale(-MathUtils.SCALE, -MathUtils.SCALE, MathUtils.SCALE);

        var document = entity.getDocument();
        var settings = document.getSettings();

//        IGuideRenderer guideRenderer = rendererManager.getRenderer(SkinPartTypes.BIPPED_HEAD);
//        if (guideRenderer != null) {
//            poseStack.pushPose();
////            poseStack.translate(0, -rect2.getMinY(), 0);
//            poseStack.scale(16, 16, 16);
//            guideRenderer.render(poseStack, renderData, 0xf000f0, OverlayTexture.NO_OVERLAY, buffers);
//            poseStack.popPose();
//        }

        if (settings.showsOrigin()) {
            poseStack.scale(-1, -1, 1);
            ShapeTesselator.vector(Vector3f.ZERO, 16, poseStack, bufferSource);
            poseStack.scale(-1, -1, 1);
        }

        if (settings.showsHelperModel()) {
            var guideRenderer = GUIDES.get(document.getType());
            if (guideRenderer != null) {
                guideRenderer.render(document, poseStack, light, overlay, bufferSource);
            }
        }

        var armature = BakedArmature.defaultBy(document.getType().getSkinType());
        renderNode(document, document.getRoot(), armature, 0, poseStack, bufferSource, light, overlay);

        poseStack.popPose();

        if (ModDebugger.advancedBuilder) {
            var blockState = entity.getBlockState();
            var pos = entity.getBlockPos();
            poseStack.pushPose();
            poseStack.translate(-pos.getX(), -pos.getY(), -pos.getZ());
            ShapeTesselator.stroke(entity.getRenderBoundingBox(blockState), UIColor.RED, poseStack, bufferSource);
            var origin = entity.getRenderOrigin();
            poseStack.translate(origin.getX(), origin.getY(), origin.getZ());
            ShapeTesselator.vector(Vector3f.ZERO, 1, poseStack, bufferSource);
            poseStack.translate(entity.carmeOffset.getX(), entity.carmeOffset.getY(), entity.carmeOffset.getZ());
//            poseStack.mulPose(new OpenQuaternionf(-entity.carmeRot.getX(), entity.carmeRot.getY(), entity.carmeRot.getZ(), true));
            ShapeTesselator.vector(Vector3f.ZERO, 1, poseStack, bufferSource);

            poseStack.popPose();
        }

//        renderOutput(entity, partialTicks, poseStack, buffers, light, overlay);
    }


    protected void renderNode(SkinDocument document, SkinDocumentNode node, BakedArmature armature, float partialTicks, IPoseStack poseStack, IBufferSource bufferSource, int light, int overlay) {
        // when the node is disabled, it does not to rendering.
        if (!node.isEnabled()) {
            return;
        }
        poseStack.pushPose();

        // apply joint transform.
        if (armature != null && node.isLocked()) {
            IJointTransform transform = armature.getTransform(node.getType());
            if (transform != null) {
                transform.apply(poseStack);
            }
        }

        // only item
        if (node.isLocked() && USE_ITEM_TRANSFORMERS.contains(node.getType())) {
            applyTransform(poseStack, document.getRoot(), document.getItemTransforms());
        }

        // apply node transform.
        node.getTransform().apply(poseStack);

        if (node.isLocator()) {
            poseStack.scale(-1, -1, 1);
            ShapeTesselator.vector(Vector3f.ZERO, 16, poseStack, bufferSource);
            poseStack.scale(-1, -1, 1);
        }

        var descriptor = node.getSkin();
        var tesselator = SkinRenderTesselator.create(descriptor, Tickets.RENDERER);
        if (tesselator != null) {
            tesselator.setLightmap(0xf000f0);
            tesselator.setPartialTicks(partialTicks);
            tesselator.setAnimationTicks(0);

            tesselator.setPoseStack(poseStack);
            tesselator.setBufferSource(bufferSource);
            tesselator.setModelViewStack(AbstractPoseStack.create(RenderSystem.getExtendedModelViewStack()));

            tesselator.draw();
        }
        for (var child : node.children()) {
            renderNode(document, child, armature, partialTicks, poseStack, bufferSource, light, overlay);
        }
        poseStack.popPose();
    }


    public void renderOutput(T entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        var pos = entity.getBlockPos();
        poseStack.pushPose();
        poseStack.translate((float) (-pos.getX()), (float) (-pos.getY()), (float) (-pos.getZ()));
//        for (Vector3f v : OUTPUTS) {
//            RenderSystem.drawPoint(poseStack, v, 1.0F, buffers);
//        }
        if (OUTPUTS.size() >= 2) {
//            Vector3f pt1 = OUTPUTS.get(0);
//            Vector3f pt2 = OUTPUTS.get(1);
//            Vector3f pt3 = OUTPUTS.get(2);
//            RenderSystem.drawLine(poseStack, pt1.getX(), pt1.getY(), pt1.getZ(), pt2.getX(), pt2.getY(), pt2.getZ(), UIColor.YELLOW, buffers);
//            drawLine(pose, pt2.getX(), pt2.getY(), pt2.getZ(), pt3.getX(), pt3.getY(), pt3.getZ(), UIColor.MAGENTA, builder);
        }

        poseStack.popPose();
    }

    protected void applyTransform(IPoseStack poseStack, SkinDocumentNode node, SkinItemTransforms itemTransforms) {
        if (itemTransforms != null) {
            var itemTransform = itemTransforms.get(AbstractItemTransformType.THIRD_PERSON_RIGHT_HAND);
            if (itemTransform != null) {
                poseStack.translate(0, -2, -2);
                itemTransform.apply(poseStack);
            }
        } else {
            poseStack.translate(0, -2, -2);
            var entity = PlaceholderManager.MANNEQUIN.get();
            var model = SkinModelManager.getInstance().getModel(node.getType(), null, ItemStack.EMPTY, entity);
            var f1 = 16f;
            var f2 = 1 / 16f;
            poseStack.scale(f1, f1, f1);
            model.applyTransform(poseStack, false, AbstractItemTransformType.THIRD_PERSON_RIGHT_HAND);
            poseStack.scale(f2, f2, f2);
        }
    }

    @Override
    public int getViewDistance() {
        return 272;
    }

    @Override
    public boolean shouldRenderOffScreen(T entity) {
        return true;
    }
}
