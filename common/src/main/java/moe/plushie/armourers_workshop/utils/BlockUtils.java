package moe.plushie.armourers_workshop.utils;

import moe.plushie.armourers_workshop.api.common.IBlockSnapshot;
import moe.plushie.armourers_workshop.api.painting.IPaintable;
import moe.plushie.armourers_workshop.builder.block.SkinCubeBlock;
import moe.plushie.armourers_workshop.builder.data.undo.UndoManager;
import moe.plushie.armourers_workshop.builder.data.undo.action.NamedUserAction;
import moe.plushie.armourers_workshop.builder.data.undo.action.SetBlockAction;
import moe.plushie.armourers_workshop.init.platform.event.common.BlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;

public final class BlockUtils {

    private static final ThreadLocal<Map<BlockEntity, Runnable>> pending = ThreadLocal.withInitial(() -> null);

    public static void beginCombiner() {
        Map<BlockEntity, Runnable> queue = pending.get();
        if (queue == null) {
            pending.set(new IdentityHashMap<>());
        }
    }

    public static <T extends BlockEntity> void combine(T blockEntity, Runnable handler) {
        Map<BlockEntity, Runnable> queue = pending.get();
        if (queue == null) {
            handler.run();
            blockEntity.setChanged();
            return;
        }
        queue.put(blockEntity, handler);
    }

    public static void snapshot(BlockEvent event) {
        Component reason = null;
        // only work in server side
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getLevel() instanceof Level)) {
            return;
        }
        // is place skin cube block.
        BlockState blockState = event.getState();
        if (blockState != null && blockState.getBlock() instanceof SkinCubeBlock) {
            reason = Component.translatable("chat.armourers_workshop.undo.placeBlock");
        }
        // is break skin cube block.
        IBlockSnapshot snapshot = event.getSnapshot();
        if (blockState == null && snapshot.getState().getBlock() instanceof SkinCubeBlock) {
            reason = Component.translatable("chat.armourers_workshop.undo.breakBlock");
        }
        // when reason is null, we can't snapshot it.
        if (reason == null) {
            return;
        }
        NamedUserAction group = new NamedUserAction(reason);
        group.push(new SetBlockAction((Level) event.getLevel(), event.getPos(), snapshot.getState(), snapshot.getTag()));
        UndoManager.of(player.getUUID()).push(group);
    }

    public static void endCombiner() {
        var queue = pending.get();
        if (queue == null) {
            return;
        }
        queue.forEach((k, v) -> {
            v.run();
            k.setChanged();
        });
        pending.set(null);
    }

    public static void performBatch(Runnable handler) {
        try {
            beginCombiner();
            handler.run();
        } finally {
            endCombiner();
        }
    }

//    public static int determineOrientation(BlockPos pos, LivingEntity entity) {
//        return determineOrientation(pos.getX(), pos.getY(), pos.getZ(), entity);
//    }
//
//    public static int determineOrientation(int x, int y, int z, LivingEntity entity) {
//        if (MathHelper.abs((float) entity.posX - x) < 2.0F && MathHelper.abs((float) entity.posZ - z) < 2.0F) {
//            double d0 = entity.posY + entity.getEyeHeight() - entity.getYOffset();
//
//            if (d0 - y > 2.0D) { return 1; }
//            if (y - d0 > 0.0D) { return 0; }
//        }
//
//        int l = MathHelper.floor(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
//        return l == 0 ? 2 : (l == 1 ? 5 : (l == 2 ? 3 : (l == 3 ? 4 : 0)));
//    }
//
//    public static int determineOrientationSide(LivingEntity entity) {
//        int rotation = MathHelper.floor(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
//        rotation = determineOrientationSideMeta(rotation);
//        return rotation;
//    }
//
//    public static int determineOrientationSideMeta(int metadata) {
//        // up = 1
//        // down = 0
//        // north = 2
//        // south = 3
//        // east = 5
//        // west = 4
//        return metadata == 0 ? 3 : (metadata == 3 ? 5 : (metadata == 1 ? 4 : 2));
//    }
//
//    public static EnumFacing determineDirectionSideMeta(int metadata) {
//        return EnumFacing.getFront(determineOrientationSideMeta(metadata));
//    }
//
//    public static EnumFacing determineDirectionSide(LivingEntity entity) {
//        return EnumFacing.getFront(determineOrientationSide(entity));
//    }
//
//    public static ICubeColour getColourFromBlockEntity(Level world, BlockPos pos) {
//        BlockEntity te = world.getBlockEntity(pos);
//        if (te != null & te instanceof IPantable) {
//            return ((IPantable)te).getColour();
//        }
//        return new CubeColour();
//    }
//
//    public static void dropInventoryBlocks(Level world, BlockPos pos) {
//        BlockEntity te = world.getBlockEntity(pos);
//        if (te != null & te instanceof Container) {
//            dropInventoryBlocks(world, (Container)te, pos);
//        }
//    }
//
//    public static void dropInventoryBlocks(Level world, Container inventory, BlockPos pos) {
//        for (int i = 0; i < inventory.getSizeInventory(); i++) {
//            ItemStack stack = inventory.removeStackFromSlot(i);
//            if (stack != null) {
//                UtilItems.spawnItemInWorld(world, pos, stack);
//            }
//        }
//    }


    public static ArrayList<BlockPos> findTouchingBlockFaces(Level level, BlockPos pos, Direction facing, int radius, boolean restrictPlane) {
        ArrayList<BlockPos> blockFaces = new ArrayList<>();
        ArrayList<BlockPos> openList = new ArrayList<>();
        HashSet<BlockPos> closedList = new HashSet<>();

        BlockPos startPos = pos.relative(facing);
        openList.add(startPos);

        Direction[] sides = Direction.values();

        boolean first = true;

        while (!openList.isEmpty()) {
            var loc = openList.remove(0);
            var blockEntity = level.getBlockEntity(loc);
            if (blockEntity instanceof IPaintable) {
                if (!restrictPlane) {
                    blockFaces.add(loc);
                } else if (samePlane(loc, pos, facing)) {
                    blockFaces.add(loc);
                }
            }
            for (var side : sides) {
                var sideLoc = loc.relative(side);
                if (closedList.contains(sideLoc)) {
                    continue;
                }
                closedList.add(sideLoc);
                if (getDistance(sideLoc, pos) < radius & hasPaintableBlock(level, sideLoc)) {
                    openList.add(sideLoc);
                }
            }
            if (closedList.size() > 5000) {
                break;
            }
        }

        return blockFaces;
    }


    public static double getDistance(BlockPos src, BlockPos dst) {
        double d0 = src.getX() - dst.getX();
        double d1 = src.getY() - dst.getY();
        double d2 = src.getZ() - dst.getZ();
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    private static boolean hasPaintableBlock(Level level, BlockPos pos) {
        for (int ix = 0; ix < 3; ix++) {
            for (int iy = 0; iy < 3; iy++) {
                for (int iz = 0; iz < 3; iz++) {
                    BlockEntity stateValid = level.getBlockEntity(pos.offset(ix - 1, iy - 1, iz - 1));
                    if (stateValid instanceof IPaintable) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean samePlane(BlockPos src, BlockPos dst, Direction direction) {
        if (direction.getStepX() == 0 || src.getX() == dst.getX()) {
            if (direction.getStepY() == 0 || src.getY() == dst.getY()) {
                return direction.getStepZ() == 0 || src.getZ() == dst.getZ();
            }
        }
        return false;
    }
}
