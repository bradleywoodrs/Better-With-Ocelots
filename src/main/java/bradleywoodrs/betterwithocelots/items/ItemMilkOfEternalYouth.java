package bradleywoodrs.betterwithocelots.items;

import bradleywoodrs.betterwithocelots.interfaces.IMobCowMixin;
import net.minecraft.core.block.entity.TileEntityActivator;
import net.minecraft.core.entity.animal.MobCow;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.IItemContainer;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemBucket;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemMilkOfEternalYouth extends Item implements IItemContainer {
	private static final String BUCKET_TYPE_TAG = "BucketType";

	public ItemMilkOfEternalYouth(String name, String namespaceId, int id) {
		super(name, namespaceId, id);
	}

	@Override
	public String getLanguageKey(ItemStack stack) {
		if (isInSteelBucket(stack)) return super.getLanguageKey(stack) + ".steel";
		if (isInIronBucket(stack)) return super.getLanguageKey(stack) + ".iron";
		return super.getLanguageKey(stack);
	}

	@Override
	public String getTranslatedDescription(ItemStack stack) {
		return I18n.getInstance().translateKey(this.getKey() + ".desc");
	}

	@Override
	public String getTranslatedName(ItemStack stack) {
		return TextFormatting.PURPLE + super.getTranslatedName(stack);
	}

	@Override
	public ItemStack onUse(ItemStack stack, World world, Player player) {
		world.playSoundAtEntity(player, player, "random.bite", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
		if (!world.isClientSide) {
			player.sendTranslatedChatMessage("betterwithocelots.milk_of_eternal_youth.drink");
		}
		return this.getContainerItemStack(stack);
	}

	@Override
	public void onUseByActivator(ItemStack stack, World world, TileEntityActivator activator, Random random, TilePosc pos, Direction direction, double offX, double offY, double offZ) {
		ItemBucket bucket = getBucket(stack);
		boolean canTopUpMilk = bucket != null && getCharges(stack) < bucket.maxCharges;
		if (!canTopUpMilk) return;
		TilePos target = pos.add(direction, new TilePos());
		IMobCowMixin cow = findYouthfulCow(world, target);
		if (cow == null) return;
		ItemBucket.setCharges(stack, getCharges(stack) + 1);
		cow.betterwithocelots$setYouthful(false);
		playActivatorMilkSound(world, target);
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack getContainerItemStack(ItemStack stack) {
		ItemStack container = stack.copy();
		container.stackSize = 1;
		int charges = getCharges(container);
		if (charges > 1) {
			ItemBucket.setCharges(container, charges - 1);
			return container;
		}
		ItemBucket bucket = getBucket(stack);
		return new ItemStack(bucket != null ? bucket : Items.BUCKET_IRON);
	}

	public static List<ItemStack> milkBucketVariants() {
		List<ItemStack> stacks = new ArrayList<>();
		for (ItemBucket bucket : List.of((ItemBucket) Items.BUCKET_IRON, (ItemBucket) Items.BUCKET_STEEL)) {
			stacks.add(createItemStack(bucket, bucket.maxCharges));
		}
		return stacks;
	}

	public static boolean isMilkOfEternalYouth(@Nullable ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemMilkOfEternalYouth;
	}

	public static boolean isEmptyBucket(@Nullable ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemBucket && ItemBucket.STATE_EMPTY.equals(ItemBucket.getState(stack));
	}

	public static ItemStack createItemStack(Item bucket, int charges) {
		ItemStack milk = new ItemStack(BetterWithOcelotsItems.MILK_OF_ETERNAL_YOUTH);
		milk.getData().putInt(BUCKET_TYPE_TAG, bucket.id);
		ItemBucket.setCharges(milk, charges);
		return milk;
	}

	@Nullable
	public static ItemBucket getBucket(ItemStack milk) {
		Item bucket = Item.getItem(milk.getData().getIntegerOrDefault(BUCKET_TYPE_TAG, 0));
		return bucket instanceof ItemBucket ? (ItemBucket) bucket : null;
	}

	public static boolean isInSteelBucket(ItemStack milk) {
		return getBucket(milk) == Items.BUCKET_STEEL;
	}

	public static boolean isInIronBucket(ItemStack milk) {
		return getBucket(milk) == Items.BUCKET_IRON;
	}

	public static int getCharges(ItemStack milk) {
		return Math.max(1, ItemBucket.getCharges(milk));
	}

	public static boolean handleActivatorFill(ItemStack bucket, World world, TileEntityActivator activator, TilePosc pos, Direction direction) {
		TilePos target = pos.add(direction, new TilePos());
		IMobCowMixin cow = findYouthfulCow(world, target);
		if (cow == null) return false;
		ItemStack milk = createItemStack(bucket.getItem(), 1);
		if (bucket.stackSize > 1) {
			int slot = findEmptyActivatorSlot(activator);
			if (slot < 0) return true;
			--bucket.stackSize;
			activator.setItem(slot, milk);
		} else {
			int slot = findActivatorSlot(activator, bucket);
			if (slot < 0) return false;
			activator.setItem(slot, milk);
		}
		cow.betterwithocelots$setYouthful(false);
		playActivatorMilkSound(world, target);
		return true;
	}

	@Nullable
	private static IMobCowMixin findYouthfulCow(World world, TilePos target) {
		AABBd box = new AABBd(target.x, target.y, target.z, target.x + 1, target.y + 1, target.z + 1);
		for (Object entity : world.getEntitiesWithinAABB(MobCow.class, box)) {
			if (entity instanceof IMobCowMixin && ((IMobCowMixin) entity).betterwithocelots$isYouthful()) {
				return (IMobCowMixin) entity;
			}
		}
		return null;
	}

	private static int findEmptyActivatorSlot(TileEntityActivator activator) {
		for (int i = 0; i < activator.getContainerSize(); i++) {
			if (!activator.locked(i) && activator.getItem(i) == null) return i;
		}
		return -1;
	}

	private static int findActivatorSlot(TileEntityActivator activator, ItemStack stack) {
		for (int i = 0; i < activator.getContainerSize(); i++) {
			if (activator.getItem(i) == stack) return i;
		}
		return -1;
	}

	private static void playActivatorMilkSound(World world, TilePos target) {
		world.playSoundEffect(null, SoundCategory.WORLD_SOUNDS, target.x + 0.5, target.y + 0.5, target.z + 0.5, "item.pickup", 2.2F, 0.5F);
	}
}
