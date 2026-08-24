package ru.feathercape.mixin;

import ru.feathercape.client.AnimatedCapeFeature;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerCapeModel;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void feathercape$addCape(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        PlayerEntityRenderer<?,?> self = (PlayerEntityRenderer<?,?>)(Object)this;
        LoadedEntityModels models = ctx.entityModels();
        PlayerCapeModel capeModel = new PlayerCapeModel(models.getModelPart(EntityModelLayers.PLAYER_CAPE));
        ((FeatureAdder)(Object)self).feathercape$add(new AnimatedCapeFeature(
            (net.minecraft.client.render.entity.feature.FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel>)(Object)self,
            capeModel
        ));
    }

    public interface FeatureAdder {
        void feathercape$add(FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> feature);
    }
}
