package com.curseddomain.client.render.entity;

import com.curseddomain.ModMain;
import com.curseddomain.entity.cursedspirit.Grade4Curse;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class Grade4CurseModel extends HierarchicalModel<Grade4Curse> {
   public static final ModelLayerLocation LAYER = new ModelLayerLocation(ModMain.id("grade_4_curse"), "main");
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart jaw;
   private final ModelPart leftArm;
   private final ModelPart rightArm;
   private final ModelPart leftLeg;
   private final ModelPart rightLeg;

   public Grade4CurseModel(ModelPart root) {
      this.root = root;
      this.body = root.getChild("body");
      this.jaw = this.body.getChild("jaw");
      this.leftArm = this.body.getChild("left_arm");
      this.rightArm = this.body.getChild("right_arm");
      this.leftLeg = root.getChild("left_leg");
      this.rightLeg = root.getChild("right_leg");
   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -9.0F, -4.5F, 10.0F, 9.0F, 9.0F).texOffs(0, 26).addBox(1.0F, -12.0F, -2.0F, 4.0F, 3.0F, 4.0F),
         PartPose.offset(0.0F, 15.0F, 0.0F)
      );
      body.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 18).addBox(-4.0F, 0.0F, -5.0F, 8.0F, 2.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.5F));
      body.addOrReplaceChild(
         "left_arm", CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 15.0F, 2.0F), PartPose.offset(5.5F, -7.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "right_arm", CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 15.0F, 2.0F), PartPose.offset(-5.5F, -7.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_leg", CubeListBuilder.create().texOffs(40, 18).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F), PartPose.offset(2.5F, 15.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_leg", CubeListBuilder.create().texOffs(52, 18).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F), PartPose.offset(-2.5F, 15.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(Grade4Curse entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch) {
      this.root.getAllParts().forEach(ModelPart::resetPose);
      float walk = Mth.cos(limbSwing * 0.66F) * 1.2F * limbSwingAmount;
      this.leftLeg.xRot = walk;
      this.rightLeg.xRot = -walk;
      this.body.yRot = headYaw * (float) (Math.PI / 180.0) * 0.5F;
      this.body.xRot = 0.25F + headPitch * (float) (Math.PI / 180.0) * 0.3F + Mth.sin(age * 0.08F) * 0.05F;
      this.body.zRot = Mth.sin(age * 0.05F) * 0.08F + Mth.cos(limbSwing * 0.66F) * 0.12F * limbSwingAmount;
      this.body.y = this.body.y + Mth.abs(Mth.sin(limbSwing * 0.66F)) * -1.5F * limbSwingAmount;
      this.leftArm.xRot = -walk * 0.6F + Mth.sin(age * 0.07F) * 0.1F;
      this.rightArm.xRot = walk * 0.6F + Mth.sin(age * 0.07F + 1.3F) * 0.1F;
      this.leftArm.zRot = -0.12F - Mth.sin(age * 0.09F) * 0.05F;
      this.rightArm.zRot = 0.12F + Mth.sin(age * 0.09F) * 0.05F;
      float bite = entity.isAggressive() ? Mth.abs(Mth.sin(age * 0.6F)) * 0.6F : Mth.abs(Mth.sin(age * 0.05F)) * 0.15F;
      this.jaw.xRot = bite;
      if (this.attackTime > 0.0F) {
         float swing = Mth.sin(this.attackTime * (float) Math.PI);
         this.leftArm.xRot -= swing * 1.8F;
         this.rightArm.xRot -= swing * 1.8F;
      }
   }
}
