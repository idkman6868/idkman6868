package com.curseddomain.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;

final class ShrineModel {
   private static final List<ShrineModel.Piece> PIECES = build();

   private ShrineModel() {
   }

   private static List<ShrineModel.Piece> build() {
      List<ShrineModel.Piece> list = new ArrayList<>();
      BlockState platform = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
      BlockState bone = Blocks.BONE_BLOCK.defaultBlockState();
      BlockState pillar = (BlockState)Blocks.CRIMSON_STEM.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Axis.Y);
      BlockState wall = Blocks.DARK_OAK_PLANKS.defaultBlockState();
      BlockState roof = Blocks.DEEPSLATE_TILES.defaultBlockState();
      BlockState trim = Blocks.RED_NETHER_BRICKS.defaultBlockState();
      BlockState skull = (BlockState)Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 8);

      for (int x = -4; x <= 4; x++) {
         for (int z = -4; z <= 4; z++) {
            list.add(new ShrineModel.Piece(x, 0, z, Math.abs(x) != 4 && Math.abs(z) != 4 ? platform : bone));
            if ((Math.abs(x) == 4 || Math.abs(z) == 4) && (x + z) % 3 == 0) {
               list.add(new ShrineModel.Piece(x, 1, z, skull));
            }
         }
      }

      for (int[] c : new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}, {-3, 0}, {3, 0}}) {
         for (int y = 1; y <= 4; y++) {
            list.add(new ShrineModel.Piece(c[0], y, c[1], pillar));
         }
      }

      for (int x = -2; x <= 2; x++) {
         list.add(new ShrineModel.Piece(x, 4, -3, x % 2 == 0 ? bone : trim));
      }

      for (int x = -5; x <= 5; x++) {
         for (int zx = -5; zx <= 5; zx++) {
            list.add(new ShrineModel.Piece(x, 5, zx, Math.abs(x) != 5 && Math.abs(zx) != 5 ? roof : trim));
         }
      }

      for (int x = -2; x <= 2; x++) {
         for (int zx = -2; zx <= 2; zx++) {
            if (Math.abs(x) == 2 || Math.abs(zx) == 2) {
               for (int y = 6; y <= 7; y++) {
                  list.add(new ShrineModel.Piece(x, y, zx, Math.abs(x) == 2 && Math.abs(zx) == 2 ? pillar : wall));
               }
            }
         }
      }

      for (int x = -3; x <= 3; x++) {
         for (int zxx = -3; zxx <= 3; zxx++) {
            list.add(new ShrineModel.Piece(x, 8, zxx, Math.abs(x) != 3 && Math.abs(zxx) != 3 ? roof : trim));
         }
      }

      for (int y = 9; y <= 10; y++) {
         list.add(new ShrineModel.Piece(0, y, 0, trim));
      }

      list.add(new ShrineModel.Piece(0, 11, 0, skull));
      return list;
   }

   static void render(PoseStack stack, MultiBufferSource buffers, float rise, float time) {
      BlockRenderDispatcher blocks = Minecraft.getInstance().getBlockRenderer();
      stack.pushPose();
      stack.translate(0.0F, -12.0F * (1.0F - rise), -9.0F);

      for (ShrineModel.Piece piece : PIECES) {
         stack.pushPose();
         stack.translate(piece.x - 0.5, piece.y, piece.z - 0.5);
         blocks.renderSingleBlock(piece.state, stack, buffers, 10485920, OverlayTexture.NO_OVERLAY);
         stack.popPose();
      }

      stack.popPose();
   }

   private record Piece(int x, int y, int z, BlockState state) {
   }
}
