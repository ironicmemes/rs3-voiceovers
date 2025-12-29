package com.rs3voiceovers.bosses;

import com.rs3voiceovers.RS3VoiceoversOverlay;
import com.rs3voiceovers.sound.Sound;
import com.rs3voiceovers.sound.SoundEngine;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.util.Text;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

public class Graardor implements Boss {
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
            if (event.getActor().getName().equals("General Graardor")) {
                String text = Text.removeTags(event.getOverheadText());
                switch (text) {
                    case "All glory to Bandos!":
                        soundEngine.playClip(Sound.GRAARDOR_OVERHEAD_1, executor);
                        break;
                    case "Brargh!":
                        soundEngine.playClip(Sound.GRAARDOR_OVERHEAD_2, executor);
                        break;
                    case "Break their bones!":
                        soundEngine.playClip(Sound.GRAARDOR_OVERHEAD_3, executor);
                        break;
                    case "CHAAARGE!":
                        soundEngine.playClip(Sound.GRAARDOR_OVERHEAD_4, executor);
                        break;
                    case "Crush them underfoot!":
                        soundEngine.playClip(Sound.GRAARDOR_OVERHEAD_5, executor);
                        break;
                    case "Death to our enemies!":
                        soundEngine.playClip(Sound.GRAARDOR_OVERHEAD_6, executor);
                        break;
                    case "For the glory of the Big High War God!":
                        soundEngine.playClip(Sound.GRAARDOR_OVERHEAD_7, executor);
                        break;
                    case "GRRRAAAAAR!":
                        soundEngine.playClip(Sound.GRAARDOR_OVERHEAD_8, executor);
                        break;
                    case "Split their skulls!":
                        soundEngine.playClip(Sound.GRAARDOR_OVERHEAD_9, executor);
                        break;
                    case "We feast on the bones of our enemies tonight!":
                        soundEngine.playClip(Sound.GRAARDOR_OVERHEAD_10, executor);
                        break;
                }
            }
        }
    }

    public void onSoundEffectPlayed(SoundEffectPlayed event) {
        if (event.getSoundId() == 3838) {
            event.consume();
            switch(random.nextInt(2)) {
                case 0:
                    soundEngine.playClip(Sound.GRAARDOR_DEATH_1, executor);
                    break;
                case 1:
                    soundEngine.playClip(Sound.GRAARDOR_DEATH_2, executor);
                    break;
            }
        }
    }
}
