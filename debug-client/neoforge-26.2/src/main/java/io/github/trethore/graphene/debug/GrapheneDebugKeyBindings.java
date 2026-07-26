package io.github.trethore.graphene.debug;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT)
final class GrapheneDebugKeyBindings {
  private static final KeyMapping.Category CATEGORY =
      KeyMapping.Category.register(Identifier.fromNamespaceAndPath(GrapheneDebugClient.ID, "main"));
  private static final KeyMapping OPEN_BROWSER =
      new KeyMapping(
          "key.grapheneui-debug.open_browser",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_F10,
          CATEGORY);

  private GrapheneDebugKeyBindings() {}

  @SubscribeEvent
  static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
    event.registerCategory(CATEGORY);
    event.register(OPEN_BROWSER);
  }

  @SubscribeEvent
  static void onClientTick(ClientTickEvent.Post event) {
    while (OPEN_BROWSER.consumeClick()) {
      Minecraft.getInstance().setScreenAndShow(GrapheneBrowserDebugScreen.instance());
    }
  }
}
