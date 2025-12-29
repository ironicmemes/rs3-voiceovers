package com.rs3voiceovers.bosses;

import com.rs3voiceovers.RS3VoiceoversOverlay;
import com.rs3voiceovers.sound.Sound;
import com.rs3voiceovers.sound.SoundEngine;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.util.Text;

import java.util.concurrent.ScheduledExecutorService;

public class Kreearra implements Boss {
    @Getter @Setter
    private SoundEngine soundEngine;
    @Getter @Setter
    private ScheduledExecutorService executor;
    @Getter @Setter
    private Client client;

    public void startUp(Client client, SoundEngine soundEngine, ScheduledExecutorService executor, ChatMessageManager chatMessageManager, RS3VoiceoversOverlay overlay) {
        this.client = client;
        this.soundEngine = soundEngine;
        this.executor = executor;
    }

    public void onOverheadTextChanged(OverheadTextChanged event) {
        if (event.getActor() != null && event.getActor().getName() != null && event.getOverheadText() != null) {
            if (event.getActor().getName().equals("Kree'arra")) {
                String text = Text.removeTags(event.getOverheadText());
                switch (text) {
                    case "Kraaaw!":
                        soundEngine.playClip(Sound.KREEARRA_KRAAW, executor);
                        break;
                    case "Skreeeee!":
                        soundEngine.playClip(Sound.KREEARRA_SKREE, executor);
                        break;
                }
            }
        }
    }

}
