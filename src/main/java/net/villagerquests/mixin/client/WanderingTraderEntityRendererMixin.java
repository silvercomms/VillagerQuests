package net.villagerquests.mixin.client;

import java.util.List;

import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.WanderingTraderEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.villagerquests.VillagerQuestsClient;
import net.villagerquests.VillagerQuestsMain;
import net.villagerquests.accessor.MerchantAccessor;
import net.villagerquests.accessor.PlayerAccessor;
import net.villagerquests.data.Quest;
import net.villagerquests.feature.QuestEntityModel;

@Mixin(WanderingTraderEntityRenderer.class)
public abstract class WanderingTraderEntityRendererMixin extends MobEntityRenderer<WanderingTraderEntity, VillagerResemblingModel<WanderingTraderEntity>> {
    private static final Identifier QUEST_TEXTURE = new Identifier("villagerquests:textures/entity/quest.png");
    private QuestEntityModel<MerchantEntity> questModel;

    public WanderingTraderEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context, new VillagerResemblingModel<>(context.getPart(EntityModelLayers.WANDERING_TRADER)), 0.5F);

    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onConstructor(EntityRendererFactory.Context context, CallbackInfo info) {
        this.questModel = new QuestEntityModel<>(context.getModelLoader().getModelPart(VillagerQuestsClient.QUEST_LAYER));
    }

    @Override
    public void render(WanderingTraderEntity mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
        if (VillagerQuestsMain.CONFIG.showQuestIcon && this.dispatcher.getSquaredDistanceToCamera(mobEntity) < VillagerQuestsMain.CONFIG.iconDistace
                && mobEntity.world.getBlockState(mobEntity.getBlockPos().up(2)).isAir()) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            PlayerEntity player = minecraftClient.player;
            List<Integer> merchantQuestList = ((MerchantAccessor) mobEntity).getQuestIdList();
            if (player != null && !merchantQuestList.isEmpty()) {
                List<Integer> playerFinishedQuestIdList = ((PlayerAccessor) player).getPlayerFinishedQuestIdList();
                List<Integer> playerQuestIdList = ((PlayerAccessor) player).getPlayerQuestIdList();
                matrixStack.push();
                float height = VillagerQuestsMain.CONFIG.flatQuestIcon ? mobEntity.getHeight() + 1.1F : mobEntity.getHeight() + 2.0F;
                matrixStack.translate(0.0D, height, 0.0D);

                if (this.hasLabel(mobEntity))
                    matrixStack.translate(0.0D, 0.3D, 0.0D);
                // method 35828 is the last Vec3f method in the Quaternion class
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.dispatcher.getRotation().getEulerAnglesXYZ(new Vector3f()).y()));

                if (VillagerQuestsMain.CONFIG.flatQuestIcon)
                    matrixStack.scale(-0.1F, -0.1F, 0.1F);
                else
                    matrixStack.scale(-1.0F, -1.0F, 1.0F);

                Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
                TextRenderer textRenderer = this.getTextRenderer();
                VertexConsumer vertexConsumers = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(QUEST_TEXTURE));

                for (int u = 0; u < merchantQuestList.size(); u++) {
                    int questId = merchantQuestList.get(u);
                    boolean containsQuest = playerQuestIdList.contains(questId);
                    if (containsQuest && ((PlayerAccessor) player).isOriginalQuestGiver(mobEntity.getUuid(), questId) && Quest.getQuestById(questId).canCompleteQuest(player)) {
                        if (VillagerQuestsMain.CONFIG.flatQuestIcon) {
                            float h = (float) (-textRenderer.getWidth((StringVisitable) Text.of("!")) / 2);
                            textRenderer.draw(Text.of("!"), h, 0.0F, 0xFFFBD500, false, matrix4f, vertexConsumerProvider, false, 0, i);
                        } else {
                            this.questModel.questionMark = false;
                            this.questModel.render(matrixStack, vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
                            this.questModel.setAngles(mobEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
                        }
                    } else if (!containsQuest && !playerFinishedQuestIdList.contains(questId)) {
                        if (VillagerQuestsMain.CONFIG.flatQuestIcon) {
                            float h = (float) (-textRenderer.getWidth((StringVisitable) Text.of("?")) / 2);
                            textRenderer.draw(Text.of("?"), h, 0.0F, 0xFFFBD500, false, matrix4f, vertexConsumerProvider, false, 0, i);
                        } else {
                            this.questModel.questionMark = true;
                            this.questModel.render(matrixStack, vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
                            this.questModel.setAngles(mobEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
                        }
                        break;
                    }
                }
                matrixStack.pop();
            }

        }

    }

}
