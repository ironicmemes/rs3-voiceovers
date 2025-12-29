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

public class Zilyana implements Boss {
    @Getter @Setter
    private SoundEngine soundEngine;
    @Getter @Setter
    private ScheduledExecutorService executor;
    @Getter @Setter
    private Client client;

    private final Random random = new Random();

    @Getter @Setter
    private boolean zilyanaHurt = false;

    public void startUp(Client client, SoundEngine soundEngine, ScheduledExecutorService executor, ChatMessageManager chatMessageManager, RS3VoiceoversOverlay overlay) {
        this.client = client;
        this.soundEngine = soundEngine;
        this.executor = executor;
    }

    public void onOverheadTextChanged(OverheadTextChanged event) {
        if (event.getActor() != null && event.getActor().getName() != null && event.getOverheadText() != null) {
            if (event.getActor().getName().equals("Commander Zilyana")) {
                String text = Text.removeTags(event.getOverheadText());
                switch (text) {
                    case "All praise Saradomin!":
                        soundEngine.playClip(Sound.ZILYANA_OVERHEAD_1, executor);
                        break;
                    case "Attack! Find the Godsword!":
                        soundEngine.playClip(Sound.ZILYANA_OVERHEAD_2, executor);
                        break;
                    case "Death to the enemies of the light!":
                        soundEngine.playClip(Sound.ZILYANA_OVERHEAD_3, executor);
                        break;
                    case "Forward! Our allies are with us!":
                        soundEngine.playClip(Sound.ZILYANA_OVERHEAD_4, executor);
                        break;
                    case "Good will always triumph!":
                        soundEngine.playClip(Sound.ZILYANA_OVERHEAD_5, executor);
                        break;
                    case "In the name of Saradomin!":
                        soundEngine.playClip(Sound.ZILYANA_OVERHEAD_6, executor);
                        break;
                    case "May Saradomin be my sword!":
                        soundEngine.playClip(Sound.ZILYANA_OVERHEAD_7, executor);
                        break;
                    case "Saradomin is with us!":
                        soundEngine.playClip(Sound.ZILYANA_OVERHEAD_8, executor);
                        break;
                    case "Saradomin lend me strength!":
                        soundEngine.playClip(Sound.ZILYANA_OVERHEAD_9, executor);
                        break;
                    case "Slay the evil ones!":
                        soundEngine.playClip(Sound.ZILYANA_OVERHEAD_10, executor);
                        break;
                }
            }
        }
    }

    public void onSoundEffectPlayed(SoundEffectPlayed event) {
        if (event.getSoundId() == 3860) {
            event.consume();
            switch(random.nextInt(3)) {
                case 0:
                    soundEngine.playClip(Sound.ZILYANA_DEATH_1, executor);
                    break;
                case 1:
                    soundEngine.playClip(Sound.ZILYANA_DEATH_2, executor);
                    break;
                case 2:
                    soundEngine.playClip(Sound.ZILYANA_DEATH_3, executor);
                    break;
            }
        }

        if (event.getSoundId() == 3858 && zilyanaHurt) {
            event.consume();
            switch (random.nextInt(4)) {
                case 0:
                    soundEngine.playClip(Sound.ZILYANA_HURT_1, executor);
                    break;
                case 1:
                    soundEngine.playClip(Sound.ZILYANA_HURT_2, executor);
                    break;
                case 2:
                    soundEngine.playClip(Sound.ZILYANA_HURT_3, executor);
                    break;
                case 3:
                    soundEngine.playClip(Sound.ZILYANA_HURT_4, executor);
                    break;
            }
        }
    }
}
