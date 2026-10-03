package com.curseddomain.client.render.entity;

import com.curseddomain.ModMain;
import com.curseddomain.shikigami.DivineDogEntity;
import com.curseddomain.shikigami.NueEntity;
import com.curseddomain.shikigami.TransfiguredHumanEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class ShikigamiModels {
   public static final ModelLayerLocation DIVINE_DOG = new ModelLayerLocation(ModMain.id("divine_dog"), "main");
   public static final ModelLayerLocation NUE = new ModelLayerLocation(ModMain.id("nue"), "main");
   public static final ModelLayerLocation TRANSFIGURED_HUMAN = new ModelLayerLocation(ModMain.id("transfigured_human"), "main");

   private ShikigamiModels() {
   }

   public static LayerDefinition dogLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.5F, -7.0F, 8.0F, 7.0F, 14.0F), PartPose.offset(0.0F, 13.0F, 0.0F));
      PartDefinition head = root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 21)
            .addBox(-3.5F, -4.0F, -6.0F, 7.0F, 7.0F, 7.0F)
            .texOffs(28, 21)
            .addBox(-2.0F, -1.0F, -9.0F, 4.0F, 3.0F, 3.0F)
            .texOffs(44, 0)
            .addBox(-3.2F, -7.0F, -3.0F, 2.0F, 3.0F, 1.0F)
            .texOffs(44, 0)
            .addBox(1.2F, -7.0F, -3.0F, 2.0F, 3.0F, 1.0F),
         PartPose.offset(0.0F, 11.0F, -6.5F)
      );
      head.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(28, 28).addBox(-4.5F, -4.5F, 0.0F, 9.0F, 9.0F, 4.0F), PartPose.ZERO);

      for (int i = 0; i < 4; i++) {
         float x = i % 2 == 0 ? -2.5F : 2.5F;
         float z = i < 2 ? -5.0F : 5.0F;
         root.addOrReplaceChild("leg" + i, CubeListBuilder.create().texOffs(44, 8).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offset(x, 16.0F, z));
      }

      root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 9.0F), PartPose.offset(0.0F, 11.0F, 6.5F));
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition nueLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition body = root.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.5F, -4.5F, 7.0F, 7.0F, 10.0F), PartPose.offset(0.0F, 12.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(0, 17).addBox(-3.5F, -6.0F, -4.0F, 7.0F, 6.0F, 6.0F).texOffs(26, 17).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 3.0F, 2.0F),
         PartPose.offset(0.0F, -2.0F, -4.0F)
      );
      body.addOrReplaceChild(
         "left_wing", CubeListBuilder.create().texOffs(0, 30).addBox(0.0F, -0.5F, -4.0F, 16.0F, 1.0F, 9.0F), PartPose.offset(3.5F, -2.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "right_wing", CubeListBuilder.create().texOffs(0, 40).addBox(-16.0F, -0.5F, -4.0F, 16.0F, 1.0F, 9.0F), PartPose.offset(-3.5F, -2.0F, 0.0F)
      );
      body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(34, 0).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 1.0F, 7.0F), PartPose.offset(0.0F, 1.0F, 5.0F));
      body.addOrReplaceChild("talons", CubeListBuilder.create().texOffs(34, 8).addBox(-2.5F, 0.0F, -1.0F, 5.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 3.5F, 0.0F));
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition humanLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition torso = root.addOrReplaceChild(
         "torso",
         CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
         PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.45F, 0.0F, 0.0F)
      );
      torso.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 8.0F).texOffs(32, 0).addBox(1.0F, -10.0F, -2.0F, 4.0F, 4.0F, 4.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3F, 0.2F, 0.25F)
      );
      torso.addOrReplaceChild(
         "left_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 19.0F, 3.0F), PartPose.offset(5.5F, 1.0F, 0.0F)
      );
      torso.addOrReplaceChild(
         "right_arm", CubeListBuilder.create().texOffs(52, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 15.0F, 3.0F), PartPose.offset(-5.5F, 1.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F), PartPose.offset(2.0F, 14.0F, 1.0F)
      );
      root.addOrReplaceChild(
         "right_leg", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F), PartPose.offset(-2.0F, 14.0F, 1.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static class DogModel extends HierarchicalModel<DivineDogEntity> {
      private final ModelPart root;
      private final ModelPart head;
      private final ModelPart tail;
      private final ModelPart[] legs = new ModelPart[4];

      public DogModel(ModelPart root) {
         this.root = root;
         this.head = root.getChild("head");
         this.tail = root.getChild("tail");

         for (int i = 0; i < 4; i++) {
            this.legs[i] = root.getChild("leg" + i);
         }
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(DivineDogEntity entity, float limbSwing, float amount, float age, float headYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         this.head.yRot = headYaw * (float) (Math.PI / 180.0);
         this.head.xRot = headPitch * (float) (Math.PI / 180.0);
         float swing = Mth.cos(limbSwing * 0.66F) * 1.3F * amount;
         this.legs[0].xRot = swing;
         this.legs[1].xRot = -swing;
         this.legs[2].xRot = -swing;
         this.legs[3].xRot = swing;
         this.tail.xRot = 0.6F + Mth.sin(age * 0.1F) * 0.1F;
         this.tail.yRot = Mth.sin(age * (entity.getTarget() != null ? 0.6F : 0.25F)) * 0.5F;
         if (entity.getTarget() != null) {
            this.head.xRot += 0.2F;
         }
      }
   }

   public static class HumanModel extends HierarchicalModel<TransfiguredHumanEntity> {
      private final ModelPart root;
      private final ModelPart torso;
      private final ModelPart leftArm;
      private final ModelPart rightArm;
      private final ModelPart leftLeg;
      private final ModelPart rightLeg;

      public HumanModel(ModelPart root) {
         this.root = root;
         this.torso = root.getChild("torso");
         this.leftArm = this.torso.getChild("left_arm");
         this.rightArm = this.torso.getChild("right_arm");
         this.leftLeg = root.getChild("left_leg");
         this.rightLeg = root.getChild("right_leg");
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(TransfiguredHumanEntity entity, float limbSwing, float amount, float age, float headYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         float swing = Mth.cos(limbSwing * 0.6F) * 1.1F * amount;
         this.leftLeg.xRot = swing;
         this.rightLeg.xRot = -swing;
         this.leftArm.xRot = -0.5F - swing * 0.7F + Mth.sin(age * 0.11F) * 0.15F;
         this.rightArm.xRot = -0.3F + swing * 0.7F;
         this.torso.zRot = Mth.sin(age * 0.07F) * 0.12F;
         if (this.attackTime > 0.0F) {
            this.leftArm.xRot = this.leftArm.xRot - Mth.sin(this.attackTime * (float) Math.PI) * 1.6F;
         }
      }
   }

   public static class NueModel extends HierarchicalModel<NueEntity> {
      private final ModelPart root;
      private final ModelPart body;
      private final ModelPart leftWing;
      private final ModelPart rightWing;
      private final ModelPart tail;

      public NueModel(ModelPart root) {
         this.root = root;
         this.body = root.getChild("body");
         this.leftWing = this.body.getChild("left_wing");
         this.rightWing = this.body.getChild("right_wing");
         this.tail = this.body.getChild("tail");
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(NueEntity entity, float limbSwing, float amount, float age, float headYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         float flap = Mth.sin(age * 0.45F) * 0.7F;
         this.leftWing.zRot = flap;
         this.rightWing.zRot = -flap;
         this.body.y = this.body.y + Mth.sin(age * 0.45F + 1.0F) * 0.8F;
         this.body.xRot = entity.getTarget() != null ? 0.35F : 0.1F;
         this.tail.xRot = 0.15F + Mth.sin(age * 0.2F) * 0.1F;
      }
   }
}
