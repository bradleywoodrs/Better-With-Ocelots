package bradleywoodrs.betterwithocelots.entities.ocelot;

import bradleywoodrs.betterwithocelots.*;
import bradleywoodrs.betterwithocelots.entities.ocelot.ai.*;
import bradleywoodrs.betterwithocelots.entities.ocelot.pathfinding.EntityNavigation;
import bradleywoodrs.betterwithocelots.items.BetterWithOcelotsItems;
import bradleywoodrs.betterwithocelots.items.ItemMilkOfEternalYouth;
import bradleywoodrs.betterwithocelots.BetterWithOcelotsConfig;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.Global;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.animal.MobAnimal;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.entity.vehicle.EntityBoat;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.collection.NamespaceID;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.helper.UUIDHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

import static bradleywoodrs.betterwithocelots.BetterWithOcelots.MOD_ID;

// ported from 1.2.5 deobf code (EntityOcelot/EntityTameable/EntityAnimal/EntityAgeable/EntityAIMate/EntityLookHelper/EntityMoveHelper)
public class MobOcelot extends MobAnimal {
	private static final int DATA_CAT_TYPE = 13;
	private static final int DATA_TAMED = 14;
	private static final int DATA_SITTING = 16;
	private static final int DATA_OWNER = 17;
	private static final int DATA_TEMPTING = 20;
	private static final int DATA_FURNI_SITTING = 21;
	private static final int DATA_HEAD_YAW = 22;
	private static final int DATA_GROWING_AGE = 23;
	private static final int DATA_PERMANENT_KITTEN = 24;
	private static final byte EVENT_TAME_FAIL = 6;
	private static final byte EVENT_TAME_SUCCESS = 7;
	private static final byte EVENT_PERMANENT_KITTEN = 8;
	private static final byte EVENT_BREED = 9;
	private static final byte EVENT_IN_LOVE = 10;
	private static final byte EVENT_LOVE_END = 11;
	private static final int HEAD_YAW_SYNC_STEP = 4;
	private static final String PERMANENT_KITTEN_PARTICLE = "puffrgb";
	private static final int YOUTHFUL_PARTICLE_TICKS = 20;
	private static final double MIN_MOVE_DIST_SQ = 2.500000277905201E-7;
	private static final String[] TEXTURES = {
		"/assets/betterwithocelots/textures/entity/cat/ozelot.png",
		"/assets/betterwithocelots/textures/entity/cat/cat_black.png",
		"/assets/betterwithocelots/textures/entity/cat/cat_red.png",
		"/assets/betterwithocelots/textures/entity/cat/cat_siamese.png",
		"/assets/betterwithocelots/textures/entity/cat/cat_all_black.png",
		"/assets/betterwithocelots/textures/entity/cat/cat_british_shorthair.png",
		"/assets/betterwithocelots/textures/entity/cat/cat_calico.png",
		"/assets/betterwithocelots/textures/entity/cat/cat_jellie.png",
		"/assets/betterwithocelots/textures/entity/cat/cat_persian.png",
		"/assets/betterwithocelots/textures/entity/cat/cat_ragdoll.png",
		"/assets/betterwithocelots/textures/entity/cat/cat_tabby.png",
		"/assets/betterwithocelots/textures/entity/cat/cat_white.png"
	};
	public static final int CAT_VARIANTS = TEXTURES.length - 1;
	public static final float SPEED_SNEAK = 0.18F;
	public static final float SPEED_WALK = 0.23F;
	public static final float SPEED_FOLLOW = 0.3F;
	public static final float SPEED_SPRINT = 0.4F;
	private static final double SHOULDER_SIDE = 0.4125;
	private static final double SHOULDER_HEIGHT = 1.4;
	private static final double BOAT_FORWARD = 0.6;
	private static final double BOAT_HEIGHT = 0.7;
	private static final int TICKS_PER_MINUTE = 1200;
	private static final int LOVE_TICKS = 600;
	private static final int BREED_COOLDOWN_TICKS = 6000;
	private static final int GROW_UP_MINUTES = BetterWithOcelotsConfig.CFG.getInt("Cats.growUpMinutes");
	private static final boolean KITTENS_GROW_UP = GROW_UP_MINUTES >= 0;
	private static final int KITTEN_AGE = -(KITTENS_GROW_UP ? GROW_UP_MINUTES : 20) * TICKS_PER_MINUTE;
	private boolean sitOrder;
	private boolean boatCarried;
	@Nullable private Entity attackTarget;
	private int idleTicks;
	private int growingAge;
	private int inLove;
	private int youthfulParticleTicks;

	private double lookX, lookY, lookZ;
	private float lookYawSpeed, lookPitchSpeed;
	private boolean looking;
	private float headYaw, headYawO;

	private double moveX, moveY, moveZ;
	private float moveSpeedTarget;
	private boolean moveUpdate;

	private final EntityNavigation navigator = new EntityNavigation(this);
	private final AITaskSelector tasks = new AITaskSelector(this);
	private final AITaskSelector targetTasks = new AITaskSelector(this);
	private final AITempt aiTempt;

	public MobOcelot(World world) {
		super(world);
		this.textureIdentifier = NamespaceID.getPermanent(MOD_ID, "ocelot");
		this.setSize(0.6F, 0.8F);
		this.scoreValue = 50;
		this.doRandomWalk = false;

		this.tasks.addTask(2, new AISit(this));
		this.aiTempt = this.tasks.addTask(3, new AITempt(this));
		this.tasks.addTask(4, new AIAvoidPlayer(this));
		this.tasks.addTask(5, new AIFollowOwner(this));
		this.tasks.addTask(6, new AISitOnBlock(this));
		this.tasks.addTask(6, new AIShoulder(this));
		this.tasks.addTask(6, new AIBoatLook(this));
		this.tasks.addTask(7, new AILeap(this));
		this.tasks.addTask(8, new AIAttack(this));
		this.tasks.addTask(9, new AIMate(this));
		this.tasks.addTask(10, new AIWander(this));
		this.tasks.addTask(11, new AIWatchPlayer(this));
		this.targetTasks.addTask(1, new AITargetChicken(this));
	}

	public EntityNavigation getNavigator() {
		return this.navigator;
	}

	public Random getRandom() {
		return this.random;
	}

	@Override
	public float getBlockPathWeight(TilePosc pos) {
		return super.getBlockPathWeight(pos);
	}

	public boolean hasSitOrder() {
		return this.sitOrder;
	}

	public void setSitOrder(boolean sitOrder) {
		this.sitOrder = sitOrder;
	}

	@Nullable
	public Entity getAttackTarget() {
		return this.attackTarget;
	}

	public void setAttackTarget(@Nullable Entity attackTarget) {
		this.attackTarget = attackTarget;
	}

	public int getIdleTicks() {
		return this.idleTicks;
	}

	@Override
	public boolean sendsDeathMessage(Entity entityKilledBy) {
		return super.sendsDeathMessage(entityKilledBy) || this.isTamed();
	}

	@Override
	public boolean isPickable() {
		return !(this.vehicle instanceof Player) && super.isPickable();
	}

	@Override
	public boolean isInWall() {
		return !(this.vehicle instanceof Player) && super.isInWall();
	}

	@Override
	public boolean collidesWith(Entity entity) {
		if (!(entity instanceof Player)) {
			return true;
		}

		Player player = (Player) entity;
		return !player.uuid.equals(this.getOcelotOwner()) || this.isOcelotSitting();
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_CAT_TYPE, (byte) 0, Byte.class);
		this.entityData.define(DATA_TAMED, (byte) 0, Byte.class);
		this.entityData.define(DATA_SITTING, (byte) 0, Byte.class);
		this.entityData.define(DATA_OWNER, null, UUID.class);
		this.entityData.define(DATA_TEMPTING, (byte) 0, Byte.class);
		this.entityData.define(DATA_FURNI_SITTING, (byte) 0, Byte.class);
		this.entityData.define(DATA_HEAD_YAW, (byte) 0, Byte.class);
		this.entityData.define(DATA_GROWING_AGE, (byte) 0, Byte.class);
		this.entityData.define(DATA_PERMANENT_KITTEN, (byte) 0, Byte.class);
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("CatType", this.getCatType());
		tag.putBoolean("Tamed", this.isTamed());
		tag.putBoolean("Sitting", this.isOcelotSitting() || this.sitOrder);
		tag.putInt("Age", this.getGrowingAge());
		tag.putInt("InLove", this.inLove);
		tag.putBoolean("PermanentKitten", this.isPermanentKitten());
		UUIDHelper.writeToTag(tag, this.getOcelotOwner(), "OwnerUUID");
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.setCatType(tag.getInteger("CatType"));
		this.setTamed(tag.getBoolean("Tamed"));
		this.sitOrder = tag.getBoolean("Sitting");
		this.setGrowingAge(tag.getInteger("Age"));
		this.inLove = tag.getInteger("InLove");
		this.setPermanentKitten(tag.getBoolean("PermanentKitten"));
		UUID ownerUUID = UUIDHelper.readFromTag(tag, "OwnerUUID");
		if (ownerUUID == null) {
			String name = tag.getString("Owner");
			if (!name.isEmpty()) {
				UUIDHelper.runConversionAction(name, (uuid) -> {
					this.setOcelotOwner(uuid);
					this.setTamed(true);
				}, null);
			}
		} else {
			this.setOcelotOwner(ownerUUID);
			this.setTamed(true);
		}
	}

	@Override
	protected boolean canDespawn() {
		return !this.isTamed();
	}

	@Override
	public String getEntityTexture() {
		int type = this.getCatType();
		return type > 0 && type < TEXTURES.length ? TEXTURES[type] : TEXTURES[0];
	}

	@Override
	public int getMaxHealth() {
		return 10;
	}

	@Override
	protected void checkFallDamage(double yd, boolean onGround) {
	}

	@Override
	public String getLivingSound() {
		if (!this.isTamed()) {
			return null;
		}
		if (this.isInLove()) {
			return MOD_ID + ":mob.ocelot.purr";
		}
		return this.random.nextInt(4) == 0 ? MOD_ID + ":mob.ocelot.purreow" : MOD_ID + ":mob.ocelot.meow";
	}

	@Override
	protected String getHurtSound() {
		return MOD_ID + ":mob.ocelot.hitt";
	}

	@Override
	protected String getDeathSound() {
		return MOD_ID + ":mob.ocelot.hitt";
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	protected float getPitchModifier() {
		return this.isKitten() ? 0.5F : 0.0F;
	}

	@Override
	public boolean canSpawnHere() {
		if (this.random.nextInt(3) == 0) {
			return false;
		}

		int x = MathHelper.floor(this.x);
		int y = MathHelper.floor(this.bb.minY);
		int z = MathHelper.floor(this.z);
		if (y < 63) {
			return false;
		}
		if (Blocks.hasTag(this.world.getBlockId(x, y, z), BlockTags.PREVENT_MOB_SPAWNS)) {
			return false;
		}
		if (!this.world.checkIfAABBIsClear(this.bb) || !this.world.getCubes(this, this.bb).isEmpty() || this.world.getIsAnyLiquid(this.bb)) {
			return false;
		}

		Block<?> below = Blocks.blocksList[this.world.getBlockId(x, y - 1, z)];
		return below != null && (below == Blocks.GRASS || below.getMaterial() == Materials.LEAVES);
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	public boolean hurt(Entity attacker, int damage, DamageType type) {
		this.sitOrder = false;
		if (this.inLove > 0) {
			this.inLove = 0;
			this.playEffect(EVENT_LOVE_END);
		}
		if (!this.world.isClientSide && this.vehicle != null) {
			this.vehicle.ejectRider();
		}
		return super.hurt(attacker, damage, type);
	}

	@Override
	public boolean interact(@NotNull Player player) {
		ItemStack itemstack = player.inventory.getCurrentItem();
		if (itemstack != null && itemstack.itemID == Items.LABEL.id && itemstack.hasCustomName()) {
			return super.interact(player);
		}

		boolean holdingFood = Item.hasTag(itemstack, BetterWithOcelotsTags.CATS_FAVOURITE_ITEM);
		boolean holdingSpecialFood = Item.hasTag(itemstack, BetterWithOcelotsTags.CATS_SPECIAL_ITEM);
		boolean holdingAnyFish = holdingFood || holdingSpecialFood;
		if (!this.isTamed()) {
			if (this.aiTempt.isRunning() && holdingAnyFish && player.distanceToSqr(this) < 9.0) {
				BetterWithOcelotsItems.consumeHeldItem(player, itemstack);
				if (!this.world.isClientSide) {
					if (holdingSpecialFood || this.random.nextInt(3) == 0) {
						this.setTamed(true);
						this.setCatType(1 + this.random.nextInt(CAT_VARIANTS));
						this.setOcelotOwner(player.uuid);
						player.addStat(BetterWithOcelots.CATS_TAMED_STAT, 1);
						this.sitOrder = true;
						this.playEffect(EVENT_TAME_SUCCESS);
					} else {
						this.playEffect(EVENT_TAME_FAIL);
					}
				}
			}
			return true;
		}
		if (ItemMilkOfEternalYouth.isMilkOfEternalYouth(itemstack) && player.uuid.equals(this.getOcelotOwner()) && this.isKitten() && !this.isPermanentKitten()) {
			if (player.getGamemode().hasBlockConsumption()) {
				player.inventory.setItem(player.inventory.getCurrentSlot(), ((ItemMilkOfEternalYouth) itemstack.getItem()).getContainerItemStack(itemstack));
			}
			if (!this.world.isClientSide) {
				this.setPermanentKitten(true);
				this.playEffect(EVENT_PERMANENT_KITTEN);
			}
			return true;
		}
		if (player.uuid.equals(this.getOcelotOwner()) && itemstack == null && player.vehicle instanceof EntityBoat && player.passenger == null && !this.isPassenger()) {
			if (!this.world.isClientSide) {
				this.sitOrder = false;
				this.boatCarried = true;
				this.getNavigator().clearPath();
				this.startRiding(player);
			}
			return true;
		}
		if (player.uuid.equals(this.getOcelotOwner()) && !this.world.isClientSide && !holdingAnyFish) {
			this.sitOrder = !(this.isOcelotSitting() || this.isFurniSitting());
		}
		if (holdingAnyFish && this.getHealth() < this.getMaxHealth()) {
			BetterWithOcelotsItems.consumeHeldItem(player, itemstack);
			this.heal(holdingSpecialFood ? this.getMaxHealth() : 2);
			return true;
		}
		if (holdingAnyFish && this.getGrowingAge() == 0 && player.uuid.equals(this.getOcelotOwner())) {
			BetterWithOcelotsItems.consumeHeldItem(player, itemstack);
			if (!this.world.isClientSide) {
				this.inLove = LOVE_TICKS;
				this.attackTarget = null;
				this.playEffect(EVENT_IN_LOVE);
			}
			return true;
		}
		return player.uuid.equals(this.getOcelotOwner());
	}

	public boolean isInLove() {
		return this.inLove > 0;
	}

	public int getGrowingAge() {
		return this.world.isClientSide ? this.entityData.getByte(DATA_GROWING_AGE) : this.growingAge;
	}

	public void setGrowingAge(int age) {
		this.growingAge = age;
		this.entityData.set(DATA_GROWING_AGE, (byte) Integer.signum(age));
	}

	public boolean isKitten() {
		return this.getGrowingAge() < 0;
	}

	public boolean isPermanentKitten() {
		return (this.entityData.getByte(DATA_PERMANENT_KITTEN) & 1) != 0;
	}

	public void setPermanentKitten(boolean permanent) {
		byte data = this.entityData.getByte(DATA_PERMANENT_KITTEN);
		this.entityData.set(DATA_PERMANENT_KITTEN, (byte) (permanent ? (data | 1) : (data & -2)));
	}

	public boolean canMateWith(MobOcelot other) {
		return other != this && this.isTamed() && other.isTamed() && this.isInLove() && other.isInLove();
	}

	public void breedWith(MobOcelot mate) {
		MobOcelot child = createChild(mate);
		this.setGrowingAge(BREED_COOLDOWN_TICKS);
		mate.setGrowingAge(BREED_COOLDOWN_TICKS);
		this.inLove = 0;
		mate.inLove = 0;
		child.setGrowingAge(KITTEN_AGE);
		child.moveTo(this.x, this.y, this.z, 0.0F, 0.0F);
		this.world.entityJoinedWorld(child);
		this.playEffect(EVENT_BREED);
		if (!this.world.isClientSide && child.isTamed()) {
			Player owner = this.world.getPlayerEntityByUUID(child.getOcelotOwner());
			if (owner != null && owner.getStat(BetterWithOcelots.CATS_TAMED_STAT) == 0) {
				owner.addStat(BetterWithOcelots.CATS_TAMED_STAT, 1);
			}
		}
	}

	private MobOcelot createChild(MobOcelot mate) {
		MobOcelot child = new MobOcelot(this.world);
		if (this.isTamed()) {
			child.setOcelotOwner(this.getOcelotOwner());
			child.setTamed(true);
			child.setCatType(this.random.nextBoolean() ? this.getCatType() : mate.getCatType());
		}
		return child;
	}

	private void playEffect(byte event) {
		if (this.world.isClientSide) return;
		if (Global.isServer) {
			this.world.sendTrackedEntityStatusUpdatePacket(this, event);
		} else {
			this.handleEntityEvent(event, 0.0F);
		}
	}

	private void showHearts() {
		if (Global.isServer) return;
		for (int i = 0; i < 7; ++i) {
			this.world.spawnParticle("heart",
				this.x + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth,
				this.y + 0.5 + (this.random.nextFloat() * this.bbHeight),
				this.z + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth,
				this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, 0, false);
		}
	}

	void showHeartsOrSmokeFX(boolean success) {
		this.showParticles(success ? "heart" : "smoke", 7);
	}

	private void showParticles(String particleType, int count) {
		if (Global.isServer) return;
		for (int i = 0; i < count; ++i) {
			double motionX = this.random.nextGaussian() * 0.02;
			double motionY = this.random.nextGaussian() * 0.02;
			double motionZ = this.random.nextGaussian() * 0.02;
			this.world.spawnParticle(particleType,
				this.x + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth,
				this.y + 0.5 + (this.random.nextFloat() * this.bbHeight),
				this.z + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth,
				motionX, motionY, motionZ, 0, false);
		}
	}

	private void showYouthfulParticles(int count) {
		if (Global.isServer) return;
		for (int i = 0; i < count; ++i) {
			this.world.spawnParticle(PERMANENT_KITTEN_PARTICLE,
				this.x + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth,
				this.y + 0.5 + (this.random.nextFloat() * this.bbHeight),
				this.z + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth,
				0.85, 0.7, 1.0, -1, false);
		}
	}

	@Override
	public void handleEntityEvent(byte event, float attackedAtYaw) {
		if (event == EVENT_TAME_SUCCESS) {
			this.showHeartsOrSmokeFX(true);
		} else if (event == EVENT_TAME_FAIL) {
			this.showHeartsOrSmokeFX(false);
		} else if (event == EVENT_BREED) {
			this.showHearts();
		} else if (event == EVENT_IN_LOVE) {
			this.inLove = LOVE_TICKS;
			this.showHearts();
		} else if (event == EVENT_LOVE_END) {
			this.inLove = 0;
		} else if (event == EVENT_PERMANENT_KITTEN) {
			this.showYouthfulParticles(12);
			this.youthfulParticleTicks = YOUTHFUL_PARTICLE_TICKS;
		} else {
			super.handleEntityEvent(event, attackedAtYaw);
		}
	}

	public boolean isOcelotSitting() {
		return (this.entityData.getByte(DATA_SITTING) & 1) != 0;
	}

	public void setOcelotSitting(boolean flag) {
		byte data = this.entityData.getByte(DATA_SITTING);
		this.entityData.set(DATA_SITTING, (byte) (flag ? (data | 1) : (data & -2)));
	}

	public boolean isFurniSitting() {
		return (this.entityData.getByte(DATA_FURNI_SITTING) & 1) != 0;
	}

	public void setFurniSitting(boolean flag) {
		byte data = this.entityData.getByte(DATA_FURNI_SITTING);
		this.entityData.set(DATA_FURNI_SITTING, (byte) (flag ? (data | 1) : (data & -2)));
	}

	public boolean isBoatRiding() {
		return this.vehicle instanceof Player && ((Player) this.vehicle).vehicle instanceof EntityBoat;
	}

	public boolean isSittingAny() {
		return this.isOcelotSitting() || this.isFurniSitting() || this.isPassenger();
	}

	@Override
	public void rideTick() {
		super.rideTick();
		if (!(this.vehicle instanceof Player)) {
			this.boatCarried = false;
			return;
		}
		Player player = (Player) this.vehicle;
		boolean inBoat = player.vehicle instanceof EntityBoat;
		if (!this.world.isClientSide && (player.isSneaking() || (this.boatCarried && !inBoat) || !(this.isKitten() || this.boatCarried) || !player.uuid.equals(this.getOcelotOwner()))) {
			this.boatCarried = false;
			player.ejectRider();
			return;
		}
		float facing = inBoat ? ((EntityBoat) player.vehicle).yRot + 90.0F : player.yBodyRot;
		float yaw = facing * MathHelper.DEG_TO_RAD;
		if (inBoat) {
			this.setPos(player.x - MathHelper.sin(yaw) * BOAT_FORWARD, player.bb.minY + BOAT_HEIGHT, player.z + MathHelper.cos(yaw) * BOAT_FORWARD);
		} else this.setPos(player.x + MathHelper.cos(yaw) * SHOULDER_SIDE, player.bb.minY + SHOULDER_HEIGHT, player.z + MathHelper.sin(yaw) * SHOULDER_SIDE);
		this.yRot = this.yBodyRot = this.yBodyRotO + wrap(facing - this.yBodyRotO);
	}

	@Override
	public double getRidingHeight() {
		return 0.0;
	}

	@Override
	public boolean passesThroughRiderTargeting(ItemStack stack) {
		return true;
	}

	public @Nullable UUID getOcelotOwner() {
		return this.entityData.getUUID(DATA_OWNER);
	}

	public void setOcelotOwner(UUID uuid) {
		this.entityData.set(DATA_OWNER, uuid);
	}

	@Nullable
	public Player getOwnerEntity() {
		UUID owner = this.getOcelotOwner();
		return owner == null ? null : this.world.getPlayerEntityByUUID(owner);
	}

	public int getCatType() {
		return this.entityData.getByte(DATA_CAT_TYPE);
	}

	public void setCatType(int type) {
		this.entityData.set(DATA_CAT_TYPE, (byte) type);
	}

	public boolean isTamed() {
		return (this.entityData.getByte(DATA_TAMED) & 1) != 0;
	}

	public void setTamed(boolean tamed) {
		byte data = this.entityData.getByte(DATA_TAMED);
		this.entityData.set(DATA_TAMED, (byte) (tamed ? (data | 1) : (data & -2)));
	}

	public boolean isTempting() {
		return (this.entityData.getByte(DATA_TEMPTING) & 1) != 0;
	}

	public void setTempting(boolean flag) {
		byte data = this.entityData.getByte(DATA_TEMPTING);
		this.entityData.set(DATA_TEMPTING, (byte) (flag ? (data | 1) : (data & -2)));
	}

	@Override
	protected void updateAI() {
		float yawBefore = this.yRot;
		float pitchBefore = this.xRot;
		this.target = null;
		this.pathToEntity = null;
		super.updateAI();
		this.target = null;
		this.pathToEntity = null;
		this.yRot = yawBefore;
		this.xRot = pitchBefore;
		this.randomYawVelocity = 0.0F;
		Player near = this.world.getClosestPlayerToEntity(this, 32.0);
		this.idleTicks = near != null ? 0 : this.idleTicks + 1;
		if (this.attackTarget != null && !this.attackTarget.isAlive()) {
			this.attackTarget = null;
		}

		this.targetTasks.update();
		this.tasks.update();
		this.navigator.update();
		boolean sneaking = this.moveUpdate && this.moveSpeedTarget == SPEED_SNEAK;
		boolean sprinting = this.moveUpdate && this.moveSpeedTarget == SPEED_SPRINT;
		if (this.isTempting() != sneaking) this.setTempting(sneaking);
		if (this.isSprinting() != sprinting) this.setSprinting(sprinting);
		updateMoveHelper();
		updateLookHelper();
	}

	public void setLook(Entity entity, float yawSpeed, float pitchSpeed) {
		this.lookX = entity.x;
		this.lookY = entity instanceof Mob ? entity.y + ((Mob) entity).getHeadHeight() : (entity.bb.minY + entity.bb.maxY) / 2.0;
		this.lookZ = entity.z;
		this.lookYawSpeed = yawSpeed;
		this.lookPitchSpeed = pitchSpeed;
		this.looking = true;
	}

	public void setLook(double x, double y, double z, float yawSpeed, float pitchSpeed) {
		this.lookX = x;
		this.lookY = y;
		this.lookZ = z;
		this.lookYawSpeed = yawSpeed;
		this.lookPitchSpeed = pitchSpeed;
		this.looking = true;
	}

	private void updateLookHelper() {
		this.xRot = 0.0F;
		float neck = !this.navigator.noPath() || this.isPassenger() ? 75.0F : 100.0F;
		float relative = MathHelper.clamp(wrap(this.headYaw - this.yBodyRot), -neck, neck);
		float desired = 0.0F;
		float speed = 10.0F;
		if (this.looking) {
			this.looking = false;
			double dx = this.lookX - this.x;
			double dy = this.lookY - (this.y + this.getHeadHeight());
			double dz = this.lookZ - this.z;
			double horizontal = MathHelper.sqrt(dx * dx + dz * dz);
			float targetYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
			float targetPitch = (float) -(Math.atan2(dy, horizontal) * 180.0 / Math.PI);
			this.xRot = limitAngle(this.xRot, targetPitch, this.lookPitchSpeed);
			desired = wrap(targetYaw - this.yBodyRot);
			if (Math.abs(desired) > 160.0F &&Math.signum(desired) != Math.signum(relative)) {
				desired = relative >= 0.0F ? neck : -neck;
			}
			desired = MathHelper.clamp(desired, -neck, neck);
			speed = this.lookYawSpeed;
		}
		relative += MathHelper.clamp(desired - relative, -speed, speed);
		this.headYaw = this.yBodyRot + relative;
		byte encoded = (byte) MathHelper.floor_float(this.headYaw * 256.0F / 360.0F);
		int diff = Math.abs((byte) (encoded - this.entityData.getByte(DATA_HEAD_YAW)));
		if (diff >= HEAD_YAW_SYNC_STEP || (diff != 0 && this.headYaw == this.headYawO)) {
			this.entityData.set(DATA_HEAD_YAW, encoded);
		}
	}

	private static float wrap(float angle) {
		while (angle < -180.0F) angle += 360.0F;
		while (angle >= 180.0F) angle -= 360.0F;
		return angle;
	}

	@Override
	public void tick() {
		this.headYawO = this.headYaw;
		super.tick();
		if (!this.world.isClientSide) {
			int age = this.getGrowingAge();
			if (age < 0) {
				if (KITTENS_GROW_UP && !this.isPermanentKitten()) this.setGrowingAge(age + 1);
			} else if (age > 0) {
				this.setGrowingAge(age - 1);
			}
		}
		if (this.getGrowingAge() != 0) {
			this.inLove = 0;
		}
		if (this.youthfulParticleTicks > 0) {
			this.youthfulParticleTicks--;
			this.showYouthfulParticles(1);
		}
		float height = this.isKitten() ? 0.4F : 0.8F;
		if (this.bbHeight != height) {
			this.setSize(height * 0.75F, height);
			this.setPos(this.x, this.y, this.z);
		}
		if (this.inLove > 0) {
			--this.inLove;
			if (this.inLove % 10 == 0 && !Global.isServer) {
				this.world.spawnParticle("heart",
					this.x + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth,
					this.y + 0.5 + (this.random.nextFloat() * this.bbHeight),
					this.z + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth,
					this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, 0, false);
			}
		}
		if (this.isMultiplayerEntity) {
			this.headYaw = this.entityData.getByte(DATA_HEAD_YAW) * 360.0F / 256.0F;
		}
		while (this.headYaw - this.headYawO < -180.0F) this.headYawO -= 360.0F;
		while (this.headYaw - this.headYawO >= 180.0F) this.headYawO += 360.0F;
	}

	public float getHeadYaw(float partialTick) {
		return this.headYawO + (this.headYaw - this.headYawO) * partialTick;
	}

	public void setMoveTo(double x, double y, double z, float speed) {
		this.moveX = x;
		this.moveY = y;
		this.moveZ = z;
		this.moveSpeedTarget = speed;
		this.moveUpdate = true;
	}

	private void updateMoveHelper() {
		this.moveForward = 0.0F;
		if (!this.moveUpdate) return;
		this.moveUpdate = false;
		int footY = MathHelper.floor(this.bb.minY + 0.5);
		double dx = this.moveX - this.x;
		double dz = this.moveZ - this.z;
		double dy = this.moveY - footY;
		if (dx * dx + dy * dy + dz * dz < MIN_MOVE_DIST_SQ) return;
		float targetYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
		this.yRot = limitAngle(this.yRot, targetYaw, 30.0F);
		this.speed = this.moveSpeedTarget;
		this.moveForward = this.moveSpeedTarget;
		if (dy > 0.0 && dx * dx + dz * dz < 1.0) {
			this.isJumping = true;
		}
	}

	private static float limitAngle(float current, float target, float max) {
		float delta = target - current;
		while (delta < -180.0F) delta += 360.0F;
		while (delta >= 180.0F) delta -= 360.0F;
		if (delta > max) delta = max;
		if (delta < -max) delta = -max;
		return current + delta;
	}
}
