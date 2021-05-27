package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.util.List;

/**
 * Makes Schmucks target minions instead of the player. This creates more interesting battles, and also reduces player
 * death (which we want!)
 */
public class SchmuckTargetMinions extends Goal {
	private SchmuckEntity mob;

	public SchmuckTargetMinions(SchmuckEntity mob) {
		this.mob = mob;
	}

	@Override
	public boolean canStart() {
		if (this.mob.getTarget() != null) {
			return checkForMinions(this.mob.getTarget());
		}
		if (this.mob.getAttacker() != null) {
			return checkForMinions(this.mob.getAttacker());
		}
		return false;
	}

	public boolean checkForMinions(LivingEntity owner) {
		if (owner instanceof PlayerEntity && owner != this.mob.getOwner()) { // make sure we are not attacking our army
			double d = this.mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
			Box box = Box.method_29968(this.mob.getPos()).expand(d, 10.0D, d);
			return this.mob.world.getEntitiesByClass(TameableEntity.class,
					box,
					tameableEntity -> tameableEntity.getOwner() == owner).size() > 0;
		}
		return false;
	}

	@Override
	public void start() {
		double d = this.mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
		Box box = Box.method_29968(this.mob.getPos()).expand(d, 10.0D, d);
		List<TameableEntity> nearby = this.mob.world.getEntitiesByClass(TameableEntity.class,
				box,
				tameableEntity -> tameableEntity.getOwner() == this.mob.getAttacker());
		if (nearby.size() > 0) {
			this.mob.setTarget(nearby.get(this.mob.getRandom().nextInt(nearby.size())));
		}
		super.start();
	}
}
