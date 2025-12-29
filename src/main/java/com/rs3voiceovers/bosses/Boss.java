package com.rs3voiceovers.bosses;

import com.rs3voiceovers.RS3VoiceoversOverlay;
import com.rs3voiceovers.sound.SoundEngine;
import net.runelite.api.Client;
import net.runelite.client.chat.ChatMessageManager;

import java.util.concurrent.ScheduledExecutorService;

public interface Boss {
    public void startUp(Client client, SoundEngine soundEngine, ScheduledExecutorService executor, ChatMessageManager chatMessageManager, RS3VoiceoversOverlay overlay);
}
