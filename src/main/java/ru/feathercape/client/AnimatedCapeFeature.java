package ru.feathercape.client;

import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerCapeModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public final class AnimatedCapeFeature extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final PlayerCapeModel capeModel;

    public AnimatedCapeFeature(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context,
                               PlayerCapeModel capeModel) {
        super(context);
        this.capeModel = capeModel;
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                       PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        if (state.playerName == null) return;
        CapeManager.Animation cape = CapeManager.get(state.playerName.getString());
        if (cape == null || cape.texture() == null || state.spectator) return;
        capeModel.setAngles(state);
        render(capeModel, cape.texture(), matrices, queue, light, state, 0xFFFFFFFF, 0);
    }
}
