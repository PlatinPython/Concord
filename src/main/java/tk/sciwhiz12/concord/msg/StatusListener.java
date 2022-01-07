package tk.sciwhiz12.concord.msg;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import tk.sciwhiz12.concord.ChatBot;
import tk.sciwhiz12.concord.ConcordConfig;

public class StatusListener {
    private final ChatBot bot;

    public StatusListener(ChatBot bot) {
        this.bot = bot;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    void onServerStarted(ServerStartedEvent event) {
        if (!ConcordConfig.SERVER_START.get()) return;

        Messaging.sendToChannel(bot.getDiscord(),
            new TranslatableComponent("message.concord.server.start").getString());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    void onServerStopping(ServerStoppingEvent event) {
        if (!ConcordConfig.SERVER_STOP.get()) return;

        Messaging.sendToChannel(bot.getDiscord(),
            new TranslatableComponent("message.concord.server.stop").getString());
    }
}
