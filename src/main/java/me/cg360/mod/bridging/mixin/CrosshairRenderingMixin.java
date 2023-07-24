package me.cg360.mod.bridging.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.cg360.mod.bridging.BridgingMod;
import me.cg360.mod.bridging.compat.BridgingCrosshairTweaks;
import me.cg360.mod.bridging.raytrace.ReacharoundTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class CrosshairRenderingMixin {

    @Unique
    private static final int ICON_SIZE = 31;

    @Shadow private int screenHeight;
    @Shadow private int screenWidth;

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At(value = "TAIL"))
    public void renderPlacementAssistMarker(GuiGraphics gui, CallbackInfo ci) {
        if(ReacharoundTracker.currentTarget == null) return;
        if(BridgingCrosshairTweaks.forceHidden) return;
        if(this.minecraft.options.hideGui) return;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        int w = this.screenWidth;
        int h = this.screenHeight;

        int textureOffset = ReacharoundTracker.isInVerticalOrientation()
                ? 0
                : 32;

        int x = (w - ICON_SIZE) / 2;
        int y = ((h - ICON_SIZE) / 2);

        y += BridgingCrosshairTweaks.yShift;
        y += this.minecraft.options.renderDebug ? 15 : 0;

        gui.blit(
                BridgingMod.PLACEMENT_ICONS_TEXTURE, x, y,
                textureOffset, 0,
                ICON_SIZE, ICON_SIZE
        );

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

}