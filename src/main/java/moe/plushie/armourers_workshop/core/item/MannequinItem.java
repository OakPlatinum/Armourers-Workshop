package moe.plushie.armourers_workshop.core.item;

import moe.plushie.armourers_workshop.init.common.AWConstants;
import moe.plushie.armourers_workshop.init.common.ModEntities;
import moe.plushie.armourers_workshop.core.entity.MannequinEntity;
import moe.plushie.armourers_workshop.core.texture.PlayerTextureDescriptor;
import moe.plushie.armourers_workshop.init.common.ModItems;
import moe.plushie.armourers_workshop.utils.MannequinRayTraceResult;
import moe.plushie.armourers_workshop.utils.TranslateUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class MannequinItem extends FlavouredItem {

    public MannequinItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack of(@Nullable PlayerEntity player, float scale) {
        ItemStack itemStack = new ItemStack(ModItems.MANNEQUIN);
        CompoundNBT entityTag = new CompoundNBT();
        if (scale != 1.0f) {
            entityTag.putFloat(AWConstants.NBT.ENTITY_SCALE, scale);
        }
        if (player != null) {
            PlayerTextureDescriptor descriptor = new PlayerTextureDescriptor(player.getGameProfile());
            entityTag.put(AWConstants.NBT.ENTITY_TEXTURE, descriptor.serializeNBT());
        }
        if (entityTag.size() != 0) {
            CompoundNBT nbt = itemStack.getOrCreateTag();
            nbt.put(AWConstants.NBT.ENTITY, entityTag);
        }
        return itemStack;

    }

    public static boolean isSmall(ItemStack itemStack) {
        CompoundNBT entityTag = itemStack.getTagElement(AWConstants.NBT.ENTITY);
        if (entityTag != null) {
            return entityTag.getBoolean(AWConstants.NBT.ENTITY_IS_SMALL);
        }
        return false;
    }

    public static void setScale(ItemStack itemStack, float scale) {
        CompoundNBT entityTag = itemStack.getOrCreateTagElement(AWConstants.NBT.ENTITY);
        entityTag.putFloat(AWConstants.NBT.ENTITY_SCALE, scale);
    }

    public static float getScale(ItemStack itemStack) {
        CompoundNBT entityTag = itemStack.getTagElement(AWConstants.NBT.ENTITY);
        if (entityTag == null || !entityTag.contains(AWConstants.NBT.ENTITY_SCALE, Constants.NBT.TAG_FLOAT)) {
            return 1.0f;
        }
        return entityTag.getFloat(AWConstants.NBT.ENTITY_SCALE);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (context.getHand() != Hand.MAIN_HAND) {
            return ActionResultType.FAIL;
        }
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.FAIL;
        }
        World world = context.getLevel();
        Vector3d origin = new Vector3d(player.getX(), player.getY(), player.getZ());
        MannequinRayTraceResult rayTraceResult = MannequinRayTraceResult.test(player, origin, context.getClickLocation(), context.getClickedPos());
        ItemStack itemStack = context.getItemInHand();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            MannequinEntity entity = ModEntities.MANNEQUIN.create(serverWorld, itemStack.getTag(), null, context.getPlayer(), rayTraceResult.getBlockPos(), SpawnReason.SPAWN_EGG, true, true);
            if (entity == null) {
                return ActionResultType.FAIL;
            }
            Vector3d clickedLocation = rayTraceResult.getLocation();
            entity.absMoveTo(clickedLocation.x(), clickedLocation.y(), clickedLocation.z(), 0.0f, 0.0f);
            entity.setYBodyRot(rayTraceResult.getRotation());

            world.addFreshEntity(entity);
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundCategory.BLOCKS, 0.75F, 0.8F);

            itemStack.shrink(1);
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.FAIL;
    }

    @Override
    public String getDescriptionId(ItemStack itemStack) {
        float scale = getScale(itemStack);
        if (scale <= 0.5f) {
            return super.getDescriptionId(itemStack) + ".small";
        }
        if (scale >= 2.0f) {
            return super.getDescriptionId(itemStack) + ".big";
        }
        return super.getDescriptionId(itemStack);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable World world, List<ITextComponent> tooltips, ITooltipFlag flag) {
        super.appendHoverText(itemStack, world, tooltips, flag);
        PlayerTextureDescriptor descriptor = PlayerTextureDescriptor.of(itemStack);
        if (descriptor.getName() != null) {
            tooltips.add(TranslateUtils.subtitle("item.armourers_workshop.rollover.user", descriptor.getName()));
        }
        if (descriptor.getURL() != null) {
            tooltips.add(TranslateUtils.subtitle("item.armourers_workshop.rollover.url", descriptor.getURL()));
        }
    }
}
