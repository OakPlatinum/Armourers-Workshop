package moe.plushie.armourers_workshop.core.client.other;

import com.google.common.collect.Iterators;
import com.mojang.blaze3d.vertex.PoseStack;
import moe.plushie.armourers_workshop.compatibility.api.AbstractItemTransformType;
import moe.plushie.armourers_workshop.core.client.bake.BakedSkin;
import moe.plushie.armourers_workshop.core.data.color.ColorScheme;
import moe.plushie.armourers_workshop.utils.ObjectUtils;
import moe.plushie.armourers_workshop.utils.PoseStackWrapper;
import moe.plushie.armourers_workshop.utils.PoseUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

@Environment(EnvType.CLIENT)
public class SkinRenderContext {

    public static final SkinRenderContext EMPTY  = new SkinRenderContext(new PoseStack());
    private static final Iterator<SkinRenderContext> POOL = Iterators.cycle(ObjectUtils.makeItems(100, i -> new SkinRenderContext(new PoseStack())));

    private int lightmap = 0xf000f0;
    private int overlay = 0;
    private float partialTicks = 0;

    private MultiBufferSource buffers;

    private SkinRenderData renderData;
    private SkinRenderBufferSource bufferProvider;

    private SkinItemSource itemSource;

    private ColorScheme colorScheme = ColorScheme.EMPTY;
    private AbstractItemTransformType transformType = AbstractItemTransformType.NONE;

    private final PoseStack defaultPoseStack;
    private final PoseStackWrapper usingPoseStack;

    public SkinRenderContext(PoseStack poseStack) {
        this.defaultPoseStack = poseStack;
        this.usingPoseStack = PoseUtils.wrap(poseStack);
    }

    public static SkinRenderContext alloc(SkinRenderData renderData, int light, float partialTick, AbstractItemTransformType transformType, PoseStack poseStack, MultiBufferSource buffers) {
        SkinRenderContext context = POOL.next();
        context.setRenderData(renderData);
        context.setLightmap(light);
        context.setPartialTicks(partialTick);
        context.setTransformType(transformType);
        context.setPose(poseStack);
        context.setBuffers(buffers);
        return context;
    }

    public static SkinRenderContext alloc(SkinRenderData renderData, int light, float partialTick, PoseStack poseStack, MultiBufferSource buffers) {
        return alloc(renderData, light, partialTick, AbstractItemTransformType.NONE, poseStack, buffers);
    }

    public void release() {
        this.lightmap = 0xf000f0;
        this.partialTicks = 0;

        this.colorScheme = ColorScheme.EMPTY;
        this.transformType = AbstractItemTransformType.NONE;
        this.itemSource = SkinItemSource.EMPTY;

        this.usingPoseStack.set(defaultPoseStack);

        this.bufferProvider = null;
        this.renderData = null;
        this.buffers = null;
    }

    public void pushPose() {
        usingPoseStack.pushPose();
    }

    public void popPose() {
        usingPoseStack.popPose();
    }

    public PoseStackWrapper pose() {
        return usingPoseStack;
    }

    public void setLightmap(int lightmap) {
        this.lightmap = lightmap;
    }

    public int getLightmap() {
        return lightmap;
    }

    public void setOverlay(int overlay) {
        this.overlay = overlay;
    }

    public int getOverlay() {
        return overlay;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public void setColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setTransformType(AbstractItemTransformType transformType) {
        this.transformType = transformType;
    }

    public AbstractItemTransformType getTransformType() {
        return transformType;
    }

    public void setReferenced(SkinItemSource itemSource) {
        this.itemSource = itemSource;
    }


    public SkinItemSource getReferenced() {
        if (this.itemSource != null) {
            return this.itemSource;
        }
        return SkinItemSource.EMPTY;
    }

    public void setRenderData(SkinRenderData renderData) {
        this.renderData = renderData;
    }

    public SkinRenderData getRenderData() {
        return renderData;
    }

    public void setPose(PoseStack pose) {
        this.usingPoseStack.set(pose);
    }

    public void setBuffers(MultiBufferSource buffers) {
        this.buffers = buffers;
    }

    public MultiBufferSource getBuffers() {
        return buffers;
    }

    public SkinRenderBufferSource.ObjectBuilder getBuffer(@NotNull BakedSkin skin) {
        if (bufferProvider != null) {
            return bufferProvider.getBuffer(skin);
        }
        SkinVertexBufferBuilder bufferBuilder = SkinVertexBufferBuilder.getBuffer(buffers);
        return bufferBuilder.getBuffer(skin);
    }

    public void setBufferProvider(SkinRenderBufferSource bufferProvider) {
        this.bufferProvider = bufferProvider;
    }
}
