package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Picks up items in a radius. Incredibly similar to the foxes pickup item goal.
 */
public class SchmuckPickUpItemGoal extends Goal {
	private static final Predicate<ItemEntity> PICKABLE_DROP_FILTER = (itemEntity) -> !itemEntity.cannotPickup() && itemEntity.isAlive();

	private final SchmuckEntity schmuck;

	public SchmuckPickUpItemGoal(SchmuckEntity schmuck) {
		this.schmuck = schmuck;
		this.setControls(EnumSet.of(Control.MOVE));
	}

	@Override
	public boolean canStart() {
		if (!schmuck.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
			return false;
		} else if (schmuck.getAttacker() == null) {
			if (schmuck.getRandom().nextInt(10) != 0) {
				return false;
			} else {
				List<ItemEntity> list = schmuck.world.getEntitiesByClass(ItemEntity.class,
						schmuck.getBoundingBox().expand(8.0D, 8.0D, 8.0D),
						PICKABLE_DROP_FILTER);
				return !list.isEmpty() && schmuck.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
			}
		} else {
			return false;
		}
	}

	public void tick() {
		List<ItemEntity> list = schmuck.world.getEntitiesByClass(ItemEntity.class,
				schmuck.getBoundingBox().expand(8.0D, 8.0D, 8.0D),
				PICKABLE_DROP_FILTER);
		ItemStack itemStack = schmuck.getEquippedStack(EquipmentSlot.MAINHAND);
		if (itemStack.isEmpty() && !list.isEmpty()) {
			schmuck.getNavigation().startMovingTo(list.get(0), 1.2D);
		}
	}

	public void start() {
		List<ItemEntity> list = schmuck.world.getEntitiesByClass(ItemEntity.class,
				schmuck.getBoundingBox().expand(8.0D, 8.0D, 8.0D),
				PICKABLE_DROP_FILTER);
		if (!list.isEmpty()) {
			schmuck.getNavigation().startMovingTo(list.get(0), 1.2D);
		}
	}
}
