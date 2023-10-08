package io.ejekta.bountiful.client

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.toast.Toast
import net.minecraft.client.toast.ToastManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper


@Environment(value = EnvType.CLIENT)
class BountyToast(
    private val type: Type,
    private val title: Text,
    private val description: Text?,
    private val hasProgressBar: Boolean
) :
    Toast {
    private var visibility = Toast.Visibility.SHOW
    private var lastTime: Long = 0
    private var lastProgress = 0f
    private var progress = 0f
    override fun draw(matrices: MatrixStack, manager: ToastManager, startTime: Long): Toast.Visibility {
        RenderSystem.setShaderTexture(0, TEXTURE)
        val helper: DrawableHelper = if (manager.client.currentScreen != null) manager.client.currentScreen!! else manager.client.inGameHud
            helper.drawTexture(matrices, 0, 0, 0, 96, this.width, this.height)
        type.drawIcon(matrices, 6, 6)
        if (description == null) {
            manager.client.textRenderer.draw(matrices, title, 30.0f, 12.0f, -11534256)
        } else {
            manager.client.textRenderer.draw(matrices, title, 30.0f, 7.0f, -11534256)
            manager.client.textRenderer.draw(matrices, description, 30.0f, 18.0f, -16777216)
        }
        if (hasProgressBar) {
            DrawableHelper.fill(matrices, 3, 28, 157, 29, -1)
            val f = MathHelper.clampedLerp(lastProgress, progress, (startTime - lastTime).toFloat() / 100.0f)
            val i = if (progress >= lastProgress) -16755456 else -11206656
            DrawableHelper.fill(matrices, 3, 28, (3.0f + 154.0f * f).toInt(), 29, i)
            lastProgress = f
            lastTime = startTime
        }
        return visibility
    }

    fun hide() {
        visibility = Toast.Visibility.HIDE
    }

    fun setProgress(progress: Float) {
        this.progress = progress
    }

    @Environment(value = EnvType.CLIENT)
    enum class Type(private val textureSlotX: Int, private val textureSlotY: Int) {
        MOVEMENT_KEYS(0, 0),
        MOUSE(1, 0),
        TREE(2, 0),
        RECIPE_BOOK(0, 1),
        WOODEN_PLANKS(1, 1),
        SOCIAL_INTERACTIONS(2, 1),
        RIGHT_CLICK(3, 1);

        fun drawIcon(matrices: MatrixStack?, x: Int, y: Int) {
            RenderSystem.enableBlend()
            val helper: DrawableHelper = if (MinecraftClient.getInstance().currentScreen != null) MinecraftClient.getInstance().currentScreen!! else MinecraftClient.getInstance().inGameHud
            helper.drawTexture(matrices, x, y, 176 + textureSlotX * 20, textureSlotY * 20, 20, 20)
        }
    }

    companion object {
        val TEXTURE = Identifier("bountiful","textures/gui/bountiful_toast.png")
    }
}
