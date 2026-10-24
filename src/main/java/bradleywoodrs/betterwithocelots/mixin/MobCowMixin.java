package bradleywoodrs.betterwithocelots.mixin;

import bradleywoodrs.betterwithocelots.interfaces.IMobCowMixin;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.Global;
import net.minecraft.core.entity.animal.MobAnimal;
import net.minecraft.core.entity.animal.MobCow;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MobCow.class, remap = false)
public abstract class MobCowMixin extends MobAnimal implements IMobCowMixin {
	@Unique private static final String YOUTHFUL_TAG = "betterwithocelots:Youthful";
	@Unique private static final String YOUTHFUL_PARTICLE = "puffrgb";
	@Unique private static final int DATA_YOUTHFUL = 9;

	protected MobCowMixin(World world) {
		super(world);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void betterwithocelots$defineYouthful(World world, CallbackInfo ci) {
		this.entityData.define(DATA_YOUTHFUL, (byte) 0, Byte.class);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void betterwithocelots$saveYouthful(CompoundTag tag, CallbackInfo ci) {
		tag.putBoolean(YOUTHFUL_TAG, this.betterwithocelots$isYouthful());
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void betterwithocelots$readYouthful(CompoundTag tag, CallbackInfo ci) {
		this.betterwithocelots$setYouthful(tag.getBoolean(YOUTHFUL_TAG));
	}

	@Inject(method = "onLivingUpdate", at = @At("TAIL"))
	private void betterwithocelots$youthfulParticles(CallbackInfo ci) {
		if (!Global.isServer && this.betterwithocelots$isYouthful() && this.random.nextInt(3) == 0) {
			this.world.spawnParticle(YOUTHFUL_PARTICLE,
				this.x + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth,
				this.y + 0.5 + (this.random.nextFloat() * this.bbHeight),
				this.z + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth,
				0.85, 0.7, 1.0, -1, false);
		}
	}

	@Override
	public boolean betterwithocelots$isYouthful() {
		return this.entityData.getByte(DATA_YOUTHFUL) != 0;
	}

	@Override
	public void betterwithocelots$setYouthful(boolean youthful) {
		this.entityData.set(DATA_YOUTHFUL, (byte) (youthful ? 1 : 0));
	}
}
