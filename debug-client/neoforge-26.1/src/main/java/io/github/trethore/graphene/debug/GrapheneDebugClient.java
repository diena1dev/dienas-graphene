package io.github.trethore.graphene.debug;

import io.github.trethore.graphene.api.Graphene;
import io.github.trethore.graphene.api.GrapheneContext;
import io.github.trethore.graphene.api.config.GrapheneConfig;
import io.github.trethore.graphene.api.config.GrapheneGlobalConfig;
import io.github.trethore.graphene.api.config.GrapheneRemoteDebugConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = GrapheneDebugClient.ID, dist = Dist.CLIENT)
public final class GrapheneDebugClient {
  public static final String ID = "grapheneui_debug";
  private static final Logger LOGGER = LoggerFactory.getLogger(GrapheneDebugClient.class);
  private static GrapheneContext context;

  public static GrapheneContext context() {
    if (context == null) {
      throw new IllegalStateException("Graphene debug client has not been initialized");
    }
    return context;
  }

  public GrapheneDebugClient() {
    context =
        Graphene.register(
            ID,
            GrapheneConfig.builder()
                .global(
                    GrapheneGlobalConfig.builder()
                        .allowBrowserFileAccess()
                        .remoteDebugging(
                            GrapheneRemoteDebugConfig.builder()
                                .randomPort()
                                .allowedOrigins("https://chrome-devtools-frontend.appspot.com")
                                .build())
                        .build())
                .build());
    NeoForge.EVENT_BUS.addListener(
        ClientStoppingEvent.class, event -> GrapheneBrowserDebugScreen.closeSession());
    LOGGER.info("Graphene debug client initialized");
  }
}
