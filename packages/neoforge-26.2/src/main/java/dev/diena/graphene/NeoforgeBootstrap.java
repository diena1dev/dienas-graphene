package io.github.trethore.graphene;

import io.github.trethore.graphene.internal.cef.GrapheneCefRuntime;
import io.github.trethore.graphene.internal.platform.GraphenePlatformServices;
import io.github.trethore.graphene.internal.runtime.GrapheneRuntimeController;
import io.github.trethore.graphene.neoforge.internal.platform.NeoForgePlatformServices;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = NeoforgeBootstrap.MOD_ID, dist = Dist.CLIENT)
public final class NeoforgeBootstrap {
  public static final String MOD_ID = "grapheneui";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  public NeoforgeBootstrap() {
    System.setProperty("java.awt.headless", "false");
    GraphenePlatformServices platformServices = NeoForgePlatformServices.create();
    GrapheneRuntimeController controller = GrapheneRuntimeController.instance();
    controller.install(platformServices);
    controller.installBrowserRuntime(
        new GrapheneCefRuntime(
            platformServices.startupPresenter(),
            platformServices.mainThreadExecutor(),
            platformServices.nativeWindow(),
            platformServices.externalBrowser(),
            platformServices.contextMenuPresenter(),
            platformServices.fileDialogPresenter(),
            platformServices.jsDialogPresenter()));
    LOGGER.info("Installed {} platform services", MOD_ID);
  }
}
