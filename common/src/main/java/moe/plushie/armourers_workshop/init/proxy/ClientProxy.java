package moe.plushie.armourers_workshop.init.proxy;

import moe.plushie.armourers_workshop.ArmourersWorkshop;
import moe.plushie.armourers_workshop.api.common.IBlockTintColorProvider;
import moe.plushie.armourers_workshop.api.common.IItemPropertiesProvider;
import moe.plushie.armourers_workshop.api.common.IItemTintColorProvider;
import moe.plushie.armourers_workshop.builder.client.render.PaintingHighlightPlacementRenderer;
import moe.plushie.armourers_workshop.compatibility.api.AbstractItemTransformType;
import moe.plushie.armourers_workshop.compatibility.client.AbstractBufferSource;
import moe.plushie.armourers_workshop.compatibility.client.AbstractPoseStack;
import moe.plushie.armourers_workshop.compatibility.core.data.AbstractDataSerializer;
import moe.plushie.armourers_workshop.core.capability.SkinWardrobe;
import moe.plushie.armourers_workshop.core.client.animation.AnimationEngine;
import moe.plushie.armourers_workshop.core.client.bake.SkinBakery;
import moe.plushie.armourers_workshop.core.client.bake.SkinPreloadManager;
import moe.plushie.armourers_workshop.core.client.render.HighlightPlacementRenderer;
import moe.plushie.armourers_workshop.core.client.skinrender.SkinRendererManager2;
import moe.plushie.armourers_workshop.core.client.texture.TextureManager;
import moe.plushie.armourers_workshop.core.data.DataPackType;
import moe.plushie.armourers_workshop.core.data.cache.AutoreleasePool;
import moe.plushie.armourers_workshop.core.data.slot.SkinSlotType;
import moe.plushie.armourers_workshop.core.data.ticket.Tickets;
import moe.plushie.armourers_workshop.core.skin.SkinLoader;
import moe.plushie.armourers_workshop.core.skin.part.SkinPartTypes;
import moe.plushie.armourers_workshop.init.ModConfig;
import moe.plushie.armourers_workshop.init.ModConfigSpec;
import moe.plushie.armourers_workshop.init.ModConstants;
import moe.plushie.armourers_workshop.init.ModContext;
import moe.plushie.armourers_workshop.init.ModDebugger;
import moe.plushie.armourers_workshop.init.ModItems;
import moe.plushie.armourers_workshop.init.ModKeyBindings;
import moe.plushie.armourers_workshop.init.client.ClientWardrobeHandler;
import moe.plushie.armourers_workshop.init.environment.EnvironmentExecutor;
import moe.plushie.armourers_workshop.init.environment.EnvironmentType;
import moe.plushie.armourers_workshop.init.platform.DataPackManager;
import moe.plushie.armourers_workshop.init.platform.EnvironmentManager;
import moe.plushie.armourers_workshop.init.platform.EventManager;
import moe.plushie.armourers_workshop.init.platform.ItemTooltipManager;
import moe.plushie.armourers_workshop.init.platform.event.client.ClientPlayerEvent;
import moe.plushie.armourers_workshop.init.platform.event.client.ItemTooltipEvent;
import moe.plushie.armourers_workshop.init.platform.event.client.RegisterColorHandlersEvent;
import moe.plushie.armourers_workshop.init.platform.event.client.RegisterItemPropertyEvent;
import moe.plushie.armourers_workshop.init.platform.event.client.RegisterModelEvent;
import moe.plushie.armourers_workshop.init.platform.event.client.RegisterTextureEvent;
import moe.plushie.armourers_workshop.init.platform.event.client.RenderFrameEvent;
import moe.plushie.armourers_workshop.init.platform.event.client.RenderHighlightEvent;
import moe.plushie.armourers_workshop.init.platform.event.client.RenderLivingEntityEvent;
import moe.plushie.armourers_workshop.init.platform.event.client.RenderSpecificHandEvent;
import moe.plushie.armourers_workshop.library.data.GlobalSkinLibrary;
import moe.plushie.armourers_workshop.library.data.SkinLibraryManager;
import moe.plushie.armourers_workshop.library.data.impl.MinecraftAuth;
import moe.plushie.armourers_workshop.utils.TickUtils;
import moe.plushie.armourers_workshop.utils.TypedRegistry;
import moe.plushie.armourers_workshop.utils.ext.OpenResourceLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class ClientProxy {

    public static void init() {
        //            SkinResourceManager.init();
        MinecraftAuth.init(() -> Minecraft.getInstance().getUser().getUuid(), () -> Minecraft.getInstance().getUser().getAccessToken());
        ClientWardrobeHandler.init();
        SkinRendererManager2.init();
        ModKeyBindings.init();
        ModDebugger.init();

        EnvironmentExecutor.willSetup(EnvironmentType.CLIENT, () -> () -> {
            var resourceManager = Minecraft.getInstance().getResourceManager();
            var packLoader = DataPackManager.byType(DataPackType.CLIENT_RESOURCES);
            ((ReloadableResourceManager) resourceManager).registerReloadListener(packLoader);
        });

        register();
    }

    private static void register() {
        // register custom item property.
        EventManager.listen(RegisterItemPropertyEvent.class, event -> TypedRegistry.findEntries(Item.class).forEach(it -> {
            var item = it.get();
            if (item instanceof IItemPropertiesProvider provider) {
                provider.createModelProperties((key, property) -> event.register(key, item, property));
            }
            event.register(ModConstants.key("type"), Items.CROSSBOW, ((itemStack, level, entity, id) -> 1));
        }));

        // register item/block color handler.
        EventManager.listen(RegisterColorHandlersEvent.Item.class, event -> TypedRegistry.findEntries(Item.class).forEach(it -> {
            var item = it.get();
            if (item instanceof IItemTintColorProvider provider) {
                event.register(provider, item);
            }
        }));
        EventManager.listen(RegisterColorHandlersEvent.Block.class, event -> TypedRegistry.findEntries(Block.class).forEach(it -> {
            var block = it.get();
            if (block instanceof IBlockTintColorProvider provider) {
                event.register(provider, block);
            }
        }));

        // register custom model.
        EventManager.listen(RegisterModelEvent.class, event -> SkinPartTypes.registeredTypes().forEach(partType -> {
            var rl = ArmourersWorkshop.getCustomModel(partType.getRegistryName());
            var resourceManager = Minecraft.getInstance().getResourceManager().asResourceManager();
            if (resourceManager.hasResource(OpenResourceLocation.create(rl.getNamespace(), "models/item/" + rl.getPath() + ".json"))) {
                event.register(rl);
            }
        }));
        // register custom sprite
        EventManager.listen(RegisterTextureEvent.class, event -> Stream.of(SkinSlotType.values()).forEach(slotType -> {
            event.register(slotType.getIconSprite());
        }));

        EventManager.listen(ClientPlayerEvent.LoggingIn.class, event -> {
            var player = event.getPlayer();
            if (player == null || !player.equals(EnvironmentManager.getPlayer())) {
                return; // other players join
            }
            SkinBakery.start();
            SkinPreloadManager.start();
            TextureManager.getInstance().start();
            AnimationEngine.start();
        });
        EventManager.listen(ClientPlayerEvent.LoggingOut.class, event -> {
            var player = event.getPlayer();
            if (player != null && !player.equals(EnvironmentManager.getPlayer())) {
                return; // other players leave
            }
            AnimationEngine.stop();
            SkinPreloadManager.stop();
            SkinBakery.stop();
            Tickets.invalidateAll();
            TextureManager.getInstance().stop();
            SkinLoader.getInstance().stop();
            GlobalSkinLibrary.getInstance().disconnect();
            SkinLibraryManager.getClient().getPublicSkinLibrary().reset();
            SkinLibraryManager.getClient().getPrivateSkinLibrary().reset();
            ModContext.reset();
            ModConfigSpec.COMMON.apply(null);
        });

        EventManager.listen(ClientPlayerEvent.Clone.class, event -> {
            // we can use the old wardrobe data until the next wardrobe sync packet.
            var oldWardrobe = SkinWardrobe.of(event.getOldPlayer());
            var newWardrobe = SkinWardrobe.of(event.getNewPlayer());
            if (newWardrobe != null && oldWardrobe != null) {
                var tag = new CompoundTag();
                oldWardrobe.serialize(AbstractDataSerializer.wrap(tag, event.getPlayer()));
                newWardrobe.deserialize(AbstractDataSerializer.wrap(tag, event.getPlayer()));
            }
        });

        EventManager.listen(RenderFrameEvent.Pre.class, event -> {
            AutoreleasePool.begin();
            TickUtils.tick(event.isPaused() || event.isFrozen()); // respect the /tick frozen command.
            SkinPreloadManager.tick(event.isPaused());
        });

        EventManager.listen(RenderFrameEvent.Post.class, event -> {
            AutoreleasePool.end();
        });

        // listen the block highlight events.
        EventManager.listen(RenderHighlightEvent.Block.class, event -> {
            var player = EnvironmentManager.getPlayer();
            if (player == null) {
                return;
            }
            // hidden hit box at inside
            // if (event.getTarget().isInside()) {
            //     BlockState state = player.level.getBlockState(event.getTarget().getBlockPos());
            //     if (state.is(ModBlocks.BOUNDING_BOX)) {
            //         event.setCanceled(true);
            //         return;
            //     }
            // }
            var poseStack = AbstractPoseStack.wrap(event.getPoseStack());
            var buffers = AbstractBufferSource.wrap(event.getMultiBufferSource());
            ItemStack itemStack = player.getMainHandItem();
            if (ModConfig.Client.enableEntityPlacementHighlight && itemStack.is(ModItems.MANNEQUIN.get())) {
                HighlightPlacementRenderer.renderEntity(player, event.getTarget(), event.getCamera(), poseStack, buffers);
            }
            if (ModConfig.Client.enableBlockPlacementHighlight && itemStack.is(ModItems.SKIN.get())) {
                HighlightPlacementRenderer.renderBlock(itemStack, player, event.getTarget(), event.getCamera(), poseStack, buffers);
            }
            if (ModConfig.Client.enablePaintToolPlacementHighlight && itemStack.is(ModItems.BLENDING_TOOL.get())) {
                PaintingHighlightPlacementRenderer.renderPaintTool(itemStack, player, event.getTarget(), event.getCamera(), poseStack, buffers);
            }
        });

        EventManager.listen(RenderLivingEntityEvent.Pre.class, event -> {
            ClientWardrobeHandler.onRenderLivingEntityPre(event.getEntity(), event.getPartialTicks(), event.getPackedLight(), event.getPoseStack(), event.getMultiBufferSource(), event.getRenderer());
        });
        EventManager.listen(RenderLivingEntityEvent.Setup.class, event -> {
            ClientWardrobeHandler.onRenderLivingEntity(event.getEntity(), event.getPartialTicks(), event.getPackedLight(), event.getPoseStack(), event.getMultiBufferSource(), event.getRenderer());
        });
        EventManager.listen(RenderLivingEntityEvent.Post.class, event -> {
            ClientWardrobeHandler.onRenderLivingEntityPost(event.getEntity(), event.getPartialTicks(), event.getPackedLight(), event.getPoseStack(), event.getMultiBufferSource(), event.getRenderer());
        });

        EventManager.listen(RenderSpecificHandEvent.class, event -> {
            if (!ModConfig.enableFirstPersonSkinRenderer()) {
                return;
            }
            var transformType = AbstractItemTransformType.FIRST_PERSON_LEFT_HAND;
            if (event.getHand() == InteractionHand.MAIN_HAND) {
                transformType = AbstractItemTransformType.FIRST_PERSON_RIGHT_HAND;
            }
            ClientWardrobeHandler.onRenderSpecificHand(event.getPlayer(), 0, event.getPackedLight(), transformType, event.getPoseStack(), event.getMultiBufferSource(), () -> {
                event.setCancelled(true);
            });
        });

        EventManager.listen(ItemTooltipEvent.Gather.class, ItemTooltipManager::gatherSkinTooltip);
        EventManager.listen(ItemTooltipEvent.Render.class, ItemTooltipManager::renderSkinTooltip);
    }
}
