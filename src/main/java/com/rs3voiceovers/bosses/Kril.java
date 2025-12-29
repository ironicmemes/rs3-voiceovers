package com.rs3voiceovers.bosses;

import com.rs3voiceovers.RS3VoiceoversOverlay;
import com.rs3voiceovers.sound.Sound;
import com.rs3voiceovers.sound.SoundEngine;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.util.Text;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

public class Kril implements Boss {
    @Getter @Setter
    private SoundEngine soundEngine;
    @Getter @Setter
    private ScheduledExecutorService executor;
    @Getter @Setter
    private Client client;

    private final Random random = new Random();

    public void startUp(Client client, SoundEngine soundEngine, ScheduledExecutorService executor, ChatMessageManager chatMessageManager, RS3VoiceoversOverlay overlay) {
        this.client = client;
        this.soundEngine = soundEngine;
        this.executor = executor;
    }

    public void onOverheadTextChanged(OverheadTextChanged event) {
        if (event.getActor() != null && event.getActor().getName() != null && event.getOverheadText() != null) {
            if (event.getActor().getName().equals("K'ril Tsutsaroth")) {
                String text = Text.removeTags(event.getOverheadText());
                switch (text) {
                    case "Attack!":
                        soundEngine.playClip(Sound.KRIL_OVERHEAD_1, executor);
                        break;
                    case "Attack them, you dogs!":
                        soundEngine.playClip(Sound.KRIL_OVERHEAD_2, executor);
                        break;
                    case "Death to Saradomin's dogs!":
                        soundEngine.playClip(Sound.KRIL_OVERHEAD_3, executor);
                        break;
                    case "Flay them all!":
                        soundEngine.playClip(Sound.KRIL_OVERHEAD_4, executor);
                        break;
                    case "Forward!":
                        soundEngine.playClip(Sound.KRIL_OVERHEAD_5, executor);
                        break;
                    case "Kill them, you cowards!":
                        soundEngine.playClip(Sound.KRIL_OVERHEAD_6, executor);
                        break;
                    case "No retreat!":
                        soundEngine.playClip(Sound.KRIL_OVERHEAD_7, executor);
                        break;
                    case "Rend them limb from limb!":
                        soundEngine.playClip(Sound.KRIL_OVERHEAD_8, executor);
                        break;
                    case "The Dark One will have their souls!":
                        soundEngine.playClip(Sound.KRIL_OVERHEAD_9, executor);
                        break;
                    case "Zamorak curse them!":
                        soundEngine.playClip(Sound.KRIL_OVERHEAD_10, executor);
                        break;
                    case "YARRRRRRR!":
                        soundEngine.playClip(Sound.KRIL_YARRR, executor);
                        break;
                }
            }
        }
    }

    public void onSoundEffectPlayed(SoundEffectPlayed event) {
        if (event.getSoundId() == 3864) {
            event.consume();
            soundEngine.playClip(Sound.KRIL_DEATH, executor);
        }
    }
}
