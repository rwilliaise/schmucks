package com.alotofletters.schmucks.item;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Objects;

/**
 * A schmuck in item form.
 */
public class SchmuckItem extends TooltipItem {
	public SchmuckItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		if (!(world instanceof ServerWorld)) {
			return ActionResult.SUCCESS;
		} else {
			ItemStack itemStack = context.getStack();
			BlockPos blockPos = context.getBlockPos();
			Direction direction = context.getSide();
			BlockState blockState = world.getBlockState(blockPos);

			BlockPos blockPos3;
			if (blockState.getCollisionShape(world, blockPos).isEmpty()) {
				blockPos3 = blockPos;
			} else {
				blockPos3 = blockPos.offset(direction);
			}

			SchmuckEntity entity = (SchmuckEntity) Schmucks.SCHMUCK.spawnFromItemStack((ServerWorld)world,
				itemStack,
				context.getPlayer(),
				blockPos3,
				SpawnReason.EVENT,
				true,
				!Objects.equals(blockPos, blockPos3) && direction == Direction.UP);
			if (entity != null) {
				entity.setOwner(context.getPlayer());
				itemStack.decrement(1);
			}

			return ActionResult.CONSUME;
		}
	}
}
