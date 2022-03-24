package xyz.novaserver.updatenotifier.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.novaserver.updatenotifier.client.UpdateNotifier;
import xyz.novaserver.updatenotifier.client.gui.UpdateNotificationScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    @Inject(at = @At(value = "RETURN"), method = "render")
    protected void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!UpdateNotifier.UPDATE_SCREEN_SHOWN && UpdateNotifier.UPDATE_DATA.isUpdateAvailable()) {
            MinecraftClient.getInstance().setScreen(new UpdateNotificationScreen((TitleScreen) (Object) this));
        }
    }
}
