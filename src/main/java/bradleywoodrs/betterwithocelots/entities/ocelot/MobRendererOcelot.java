package bradleywoodrs.betterwithocelots.entities.ocelot;

import bradleywoodrs.betterwithocelots.misc.Holidays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.util.helper.MathHelper;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
// ported from 1.2.5 deobf code (RenderOcelot/ModelOcelot)
public class MobRendererOcelot extends MobRenderer<MobOcelot> {
	private static final String MODEL_KEY = "main";

	private static final float BODY_REST = (float) Math.PI / 2F;
	private static final float TAIL_REST = 0.9F;
	private static final float TAIL2_REST = 1.727876F;

	public MobRendererOcelot() {
		super(0.3F);
		setModel(MODEL_KEY, "geometry.ocelot", 0.0);
		setModel(Holidays.HAT_WITCH, "geometry.ocelot.hat_witch", 0.0);
		setModel(Holidays.HAT_SANTA, "geometry.ocelot.hat_santa", 0.0);
	}

	@Override
	protected int maxRenderLayer(MobOcelot entity) {
		return Holidays.HAT != null && (entity.isTamed() || entity.isKitten()) ? 1 : 0;
	}

	@Override
	protected void preRenderTransform(MobOcelot entity, double x, double y, double z, float yaw, float partialTick) {
		super.preRenderTransform(entity, x, y, z, yaw, partialTick);
		if (entity.isKitten()) {
			GLRenderer.modelM4f().scale(0.5F, 0.5F, 0.5F);
		}
		if (entity.isTamed()) {
			GLRenderer.modelM4f().scale(0.8F, 0.8F, 0.8F);
		}
	}

	@Override
	public float getShadowSize(MobOcelot entity) {
		float size = super.getShadowSize(entity);
		return entity.isKitten() ? size * 0.5F : size;
	}

	@Override
	protected StaticEntityModel getAndSetupModelForLayer(MobOcelot ocelot, float scale, float partialTick, int layer) {
		if (layer == 1) {
			return setupHatModel();
		}

		StaticEntityModel model = getModel(MODEL_KEY);
		model.resetBones();

		float limbSwing = getLimbSwing(ocelot, partialTick);
		float limbYaw = getLimbYaw(ocelot, partialTick);
		float headYaw = ocelot.getHeadYaw(partialTick) * MathHelper.DEG_TO_RAD - getBodyYaw(ocelot, partialTick);
		float headPitch = getHeadPitch(ocelot, partialTick);

		BoneTransform headScale = bone(model, "headScale");
		if (ocelot.isKitten()) {
			headScale.scaleX = headScale.scaleY = headScale.scaleZ = 1.5;
			headScale.posY = -3.0;
			headScale.posZ = 6.0;
		}

		BoneTransform head = bone(model, "head");
		BoneTransform body = bone(model, "body");
		BoneTransform leg1 = bone(model, "leg1");
		BoneTransform leg2 = bone(model, "leg2");
		BoneTransform leg3 = bone(model, "leg3");
		BoneTransform leg4 = bone(model, "leg4");
		BoneTransform tail = bone(model, "tail");
		BoneTransform tail2 = bone(model, "tail2");

		boolean sitting = ocelot.isOcelotSitting() || ocelot.isFurniSitting() || ocelot.isPassenger();
		if (ocelot.isTempting()) {
			body.posY = -1.0;
			head.posY = -1.0;
			tail.rotX = 0.8F - TAIL_REST;
		} else if (ocelot.isSprinting()) {
			tail.rotX = (float) Math.PI / 2F - TAIL_REST;
			tail2.posY = 5.0;
		} else if (sitting) {
			body.rotX = (float) Math.PI / 4F - BODY_REST;
			body.posY = 4.0;
			body.posZ = 5.0;

			head.posY = 3.3;
			head.posZ = 1.0;

			tail.posY = -8.0;
			tail.posZ = -2.0;
			tail.rotX = 1.727876F - TAIL_REST;

			tail2.posY = -2.0;
			tail2.posZ = -0.8;
			tail2.rotX = 2.670354F - TAIL2_REST;

			if (ocelot.isBoatRiding()) {
				tail.rotX = (float) Math.PI / 2F - TAIL_REST;
				tail.rotY = 2.53F;
				tail.posZ = -1.0;

				tail2.posX = 4.6;
				tail2.posY = -3.0;
				tail2.posZ = -13.6;
				tail2.rotX = (float) Math.PI / 2F - TAIL2_REST;
				tail2.rotY = -2.87F;
			}

			leg3.rotX = leg4.rotX = -0.15707964F;
			leg3.posY = leg4.posY = -2.0;
			leg3.posZ = leg4.posZ = -2.0;

			leg1.rotX = leg2.rotX = -(float) Math.PI / 2F;
			leg1.posY = leg2.posY = -3.0;
			leg1.posZ = leg2.posZ = -4.0;
		}
		head.rotX = headPitch;
		head.rotY = headYaw;
		if (!sitting) {
			float swing = limbSwing * 0.6662F;
			if (ocelot.isSprinting()) {
				float frontLegAngle = MathHelper.cos(swing) * 1.0F * limbYaw;
				float backLegAngle = MathHelper.cos(swing + (float) Math.PI) * 1.0F * limbYaw;
				leg1.rotX = frontLegAngle;
				leg2.rotX = frontLegAngle;
				leg3.rotX = backLegAngle;
				leg4.rotX = backLegAngle;
				tail2.rotX = ((float) Math.PI / 10F) * MathHelper.cos(limbSwing) * limbYaw;
			} else {
				leg1.rotX = MathHelper.cos(swing) * 1.0F * limbYaw;
				leg2.rotX = MathHelper.cos(swing + (float) Math.PI) * 1.0F * limbYaw;
				leg3.rotX = MathHelper.cos(swing + (float) Math.PI) * 1.0F * limbYaw;
				leg4.rotX = MathHelper.cos(swing) * 1.0F * limbYaw;
				tail2.rotX = ((float) Math.PI / 4F) * MathHelper.cos(limbSwing) * limbYaw;
			}
		}

		return model;
	}

	private StaticEntityModel setupHatModel() {
		if (Holidays.HAT == null) {
			return null;
		}

		bindTexture("/assets/betterwithocelots/textures/holidays/" + Holidays.HAT + ".png");
		StaticEntityModel hat = getModel(Holidays.HAT);
		hat.resetBones();

		StaticEntityModel cat = getModel(MODEL_KEY);
		copyBone(cat, hat, "headScale");
		copyBone(cat, hat, "head");
		return hat;
	}

	private static void copyBone(StaticEntityModel from, StaticEntityModel to, String name) {
		BoneTransform src = from.getTransform(name);
		BoneTransform dst = to.getTransform(name);
		if (src == null || dst == null) {
			return;
		}
		dst.posX = src.posX;
		dst.posY = src.posY;
		dst.posZ = src.posZ;
		dst.rotX = src.rotX;
		dst.rotY = src.rotY;
		dst.rotZ = src.rotZ;
		dst.scaleX = src.scaleX;
		dst.scaleY = src.scaleY;
		dst.scaleZ = src.scaleZ;
	}

	private static BoneTransform bone(StaticEntityModel model, String name) {
		BoneTransform transform = model.getTransform(name);
		return transform != null ? transform : new BoneTransform();
	}
}
