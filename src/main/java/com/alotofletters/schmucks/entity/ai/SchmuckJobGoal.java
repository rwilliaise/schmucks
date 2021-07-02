package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Used as a base class for all jobs.
 */
public abstract class SchmuckJobGoal extends MoveToTargetPosGoal {
	protected final SchmuckEntity schmuck;

	public SchmuckJobGoal(SchmuckEntity schmuck, double speed, int range, int heightDifference) {
		super(schmuck, speed, range, heightDifference);
		this.schmuck = schmuck;
	}

	@Override
	protected int getInterval(PathAwareEntity mob) {
		SchmucksConfig config = AutoConfig.getConfigHolder(SchmucksConfig.class).getConfig();
		int min = config.jobInterval.min;
		if (min < config.jobInterval.max) {
			min += mob.getRandom().nextInt(config.jobInterval.max - min);
		}
		return min;
	}

	public ItemStack getItem(Predicate<ItemStack> predicate) {
		return this.schmuck.getInventory().getStack(getSlot(this.schmuck, predicate));
	}

	public static ItemStack getItem(SchmuckEntity schmuck, Predicate<ItemStack> predicate) {
		return schmuck.getInventory().getStack(getSlot(schmuck, predicate));
	}

	public static int getSlot(SchmuckEntity schmuck, Predicate<ItemStack> predicate) {
		SimpleInventory inv = schmuck.getInventory();
		for (int i = 0; i < 3; i++) {
			ItemStack stack = inv.getStack(i);
			if (predicate.test(stack)) {
				return i;
			}
		}
		return -1;
	}

	public boolean hasItem(Predicate<ItemStack> predicate) {
		return !this.getItem(predicate).isEmpty();
	}

	public static boolean hasItem(SchmuckEntity schmuck, Predicate<ItemStack> predicate) {
		return !getItem(schmuck, predicate).isEmpty();
	}

	@Override
	public boolean canStart() {
		return !this.schmuck.isSitting() && super.canStart();
	}
}
