package com.rs3voiceovers;

import com.rs3voiceovers.bosses.*;
import com.rs3voiceovers.sound.SoundEngine;
import com.rs3voiceovers.sound.SoundFileManager;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.util.concurrent.ScheduledExecutorService;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import okhttp3.OkHttpClient;

@Slf4j
@PluginDescriptor(
        name = "RS3 Voiceovers",
        description = "Adds RS3 voiceovers to certain encounters (Inferno, Vorkath, Wardens, GWD).",
        tags = {"rs3", "boss", "runescape 3", "voice", "inferno", "zuk", "tzkal", "vorkath", "toa", "amascut", "gwd"}
)

public class RS3VoiceoversPlugin extends Plugin {
    private final Zemouregal zemouregal = new Zemouregal();
    private final Amascut amascut = new Amascut();
    private final Zuk zuk = new Zuk();
    private final Graardor graardor = new Graardor();
    private final Kreearra kreearra = new Kreearra();
    private final Kril kril = new Kril();
    private final Zilyana zilyana = new Zilyana();

    @Inject
    private SoundEngine soundEngine;
    @Inject
    private ScheduledExecutorService executor;
    @Inject
    private Client client;
    @Inject
    private RS3VoiceoversConfig config;
    @Inject
    private EventBus eventBus;
    @Inject
    private ChatMessageManager chatMessageManager;
    @Inject
    private OkHttpClient okHttpClient;

    @Inject
    private OverlayManager overlayManager;
    private RS3VoiceoversOverlay overlay;

    @Override
    protected void startUp() throws Exception {
        log.debug("RS3 Voiceovers started.");
        overlay = new RS3VoiceoversOverlay(client);

        bossStartup(zuk);
        bossStartup(zemouregal);
        bossStartup(amascut);
        bossStartup(graardor);
        bossStartup(kreearra);
        bossStartup(kril);
        bossStartup(zilyana);

        overlayManager.add(overlay);

        executor.submit(() -> SoundFileManager.prepareSoundFiles(okHttpClient));
    }

    @Override
    protected void shutDown() throws Exception {
        log.debug("RS3 Voiceovers stopped.");
        overlayManager.remove(overlay);
        overlay = null;
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
            return;

        if (config.zuk())
            zuk.onChatMessage(event);

        if (config.amascut())
            amascut.onChatMessage(event);
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        if (config.zuk())
            zuk.onClientTick(event);

        if (config.zemouregal())
            zemouregal.onClientTick(event);

        if (config.amascut())
            amascut.onClientTick(event);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (config.zuk())
            zuk.onGameTick(event);
        else
            zuk.resetInferno();

        if (config.zemouregal())
            zemouregal.onGameTick(event);
        else
            zemouregal.resetVorkath();

        if (config.amascut())
            amascut.onGameTick(event);
        else
            amascut.resetWardens();
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event) {
        if (event.getNpc() == null)
            return;

        if (config.zemouregal())
            zemouregal.onNpcChanged(event);

        if (config.amascut())
            amascut.onNpcChanged(event);
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc() == null)
            return;

        if (config.zuk())
            zuk.onNpcSpawned(event);

        if (config.amascut())
            amascut.onNpcSpawned(event);
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (event.getActor() == null)
            return;

        if (config.amascut())
            amascut.onAnimationChanged(event);
    }

    @Subscribe
    public void onActorDeath(ActorDeath event)
    {
        if (config.zuk())
            zuk.onActorDeath(event);

        if (config.zemouregal())
            zemouregal.onActorDeath(event);

        if (config.amascut())
            amascut.onActorDeath(event);
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (config.zemouregal())
            zemouregal.onHitsplatApplied(event);
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        if (config.amascut())
            amascut.onProjectileMoved(event);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (config.zuk())
            zuk.onGameStateChanged(event);
    }

    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged event) {
        if (config.graardor())
            graardor.onOverheadTextChanged(event);

        if (config.zilyana())
            zilyana.onOverheadTextChanged(event);

        if (config.kreearra())
            kreearra.onOverheadTextChanged(event);

        if (config.kril())
            kril.onOverheadTextChanged(event);
    }

    @Subscribe
    public void onSoundEffectPlayed(SoundEffectPlayed event) {
        if (config.graardor())
            graardor.onSoundEffectPlayed(event);

        if (config.zilyana())
            zilyana.onSoundEffectPlayed(event);

        if (config.kril())
            kril.onSoundEffectPlayed(event);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        amascut.setTumekenP3(config.amascutP3().toString().equals("Crondis/Apmeken"));

        zilyana.setZilyanaHurt(config.zilyanaHurt());
    }

    @Provides
    RS3VoiceoversConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(RS3VoiceoversConfig.class);
	}

    void bossStartup(Boss boss) {
        boss.startUp(client, soundEngine, executor, chatMessageManager, overlay);
    }
}
