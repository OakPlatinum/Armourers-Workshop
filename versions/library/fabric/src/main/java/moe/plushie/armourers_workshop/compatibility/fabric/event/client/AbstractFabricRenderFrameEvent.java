package moe.plushie.armourers_workshop.compatibility.fabric.event.client;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.registry.IEventHandler;
import moe.plushie.armourers_workshop.init.platform.event.client.RenderFrameEvent;
import moe.plushie.armourers_workshop.init.platform.fabric.event.ClientFrameRenderEvents;
import net.minecraft.client.Minecraft;

@Available("[1.21, )")
public class AbstractFabricRenderFrameEvent {

    public static IEventHandler<RenderFrameEvent.Pre> preFactory() {
        return subscriber -> ClientFrameRenderEvents.START.register(client -> subscriber.accept(ReusableEvent.INSTANCE));
    }

    public static IEventHandler<RenderFrameEvent.Post> postFactory() {
        return subscriber -> ClientFrameRenderEvents.END.register(client -> subscriber.accept(ReusableEvent.INSTANCE));
    }

    private static class ReusableEvent implements RenderFrameEvent.Pre, RenderFrameEvent.Post {

        private static final ReusableEvent INSTANCE = new ReusableEvent();

        @Override
        public boolean isPaused() {
            return Minecraft.getInstance().isPaused();
        }

        @Override
        public boolean isFrozen() {
            var minecraft = Minecraft.getInstance();
            return minecraft.level != null && !minecraft.level.tickRateManager().runsNormally();
        }
    }
}
