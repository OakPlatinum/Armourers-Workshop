package moe.plushie.armourers_workshop.compatibility;

import com.mojang.blaze3d.vertex.BufferUploader;
import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.core.client.other.SkinRenderExecutor;
import moe.plushie.armourers_workshop.core.client.other.VertexArrayObject;
import moe.plushie.armourers_workshop.core.client.other.VertexIndexObject;
import moe.plushie.armourers_workshop.core.client.shader.Shader;
import moe.plushie.armourers_workshop.core.client.shader.ShaderVertexGroup;
import moe.plushie.armourers_workshop.core.client.shader.ShaderVertexObject;
import moe.plushie.armourers_workshop.utils.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL15;

@Available("[1.18, )")
@Environment(EnvType.CLIENT)
public class AbstractShader extends Shader {

    @Override
    public void begin() {
        super.begin();
        // yep we reset it.
        RenderSystem.getExtendedModelViewStack().pushPose();
        RenderSystem.getExtendedModelViewStack().setIdentity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.resetTextureMatrix();
        // ..
        BufferUploader.reset();
    }

    @Override
    public void end() {
        super.end();
        // ..
        RenderSystem.getExtendedModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public void apply(ShaderVertexGroup group, Runnable action) {
        // we let the vanilla's rendering system normal call rendering once,
        // and then insert our the rendering content in end stage.
        SkinRenderExecutor.execute(group.getRenderType(), () -> super.apply(group, action));
    }
}
