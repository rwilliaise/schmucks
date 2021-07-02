package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;

public class SchmuckRepairGoal extends Goal {

	private final SchmuckEntity schmuck;

	public SchmuckRepairGoal(SchmuckEntity schmuck) {
		this.schmuck = schmuck;
	}

	@Override
	public boolean canStart() {
		return this.schmuck.getMainHandStack().getDamage() > 0
				&& hasPureMagic();
	}

	@Override
	public void tick() {
		if (this.hasPureMagic()) {
			ItemStack itemStack = SchmuckJobGoal.getItem(schmuck, stack -> stack.isOf(Schmucks.PURE_MAGIC));
			ItemStack mainHand = this.schmuck.getMainHandStack();
			// TODO
		}
	}

	private boolean hasPureMagic() {
		return SchmuckJobGoal.hasItem(schmuck, stack -> stack.isOf(Schmucks.PURE_MAGIC));
	}
}
