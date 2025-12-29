package com.rs3voiceovers.bosses;

import com.rs3voiceovers.RS3VoiceoversOverlay;
import com.rs3voiceovers.animation.CustomAnimation;
import com.rs3voiceovers.animation.FakeNpcHandler;
import com.rs3voiceovers.animation.Status;
import com.rs3voiceovers.sound.Sound;
import com.rs3voiceovers.sound.SoundEngine;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatMessageManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Zuk implements Boss {
    private static final int INFERNO_REGION_ID = 9043;
    private static final Pattern TZHAAR_WAVE_MESSAGE = Pattern.compile("Wave: (\\d+)");
    private static final Pattern JAD_CHALLENGE_MESSAGE = Pattern.compile("You enter the Inferno for");
    private static final Pattern JAD_CHALLENGE_VICTORY_MESSAGE = Pattern.compile("Your completion count for TzHaar-Ket-Rak's");

    @Getter @Setter
    private NPC boss;

    private int ticksSinceEncounterStart = -1;
    private int ticksSinceLastQuote = -1;
    private int antiSpam = 0;
    private int messageDelay = -1;

    private boolean infernoStart = false;
    private boolean zukFight = false;
    private boolean unpaused = false;
    private int unpauseBuffer = 0;
    private boolean isInJadChallenge = false;
    private int jadChallengeCheck = -8;
    private boolean loggingIn = false;

    private int lastPhrase = -1;

    private final Random random = new Random();

    @Getter @Setter
    private SoundEngine soundEngine;
    @Getter @Setter
    private ScheduledExecutorService executor;
    @Getter @Setter
    private Client client;
    @Getter @Setter
    private FakeNpcHandler fakeNpcHandler = new FakeNpcHandler();
    @Getter @Setter
    private RuneLiteObject fakeZuk;
    @Getter @Setter
    private int fakeZukStatus = Status.INACTIVE.getStatus();

    public void startUp(Client client, SoundEngine soundEngine, ScheduledExecutorService executor, ChatMessageManager chatMessageManager, RS3VoiceoversOverlay overlay) {
        this.client = client;
        this.soundEngine = soundEngine;
        this.executor = executor;
        fakeNpcHandler.setClient(client);
        fakeNpcHandler.setChatMessageManager(chatMessageManager);
        fakeNpcHandler.setOverlay(overlay);
    }

    private boolean isInInfernoArea() {
        return client.getMapRegions() != null && ArrayUtils.contains(client.getMapRegions(), INFERNO_REGION_ID);
    }

    private boolean isInInferno() {
        return !isInJadChallenge && client.getMapRegions() != null && ArrayUtils.contains(client.getMapRegions(), INFERNO_REGION_ID);
    }
    
    public void onChatMessage(ChatMessage event) {
        String message = event.getMessage();

        // Inferno start quote
        if (isInInferno()) {
            Matcher matcher = TZHAAR_WAVE_MESSAGE.matcher(message);
            if (matcher.find()) {
                int wave = Integer.parseInt(matcher.group(1));
                if (wave == 1 || unpaused)
                    ticksSinceEncounterStart = 0;
                if (wave == 69)
                    zukFight = true;
            }
        }

        // Jad challenge detect
        Matcher m = JAD_CHALLENGE_MESSAGE.matcher(message);
        if (m.find()) {
            isInJadChallenge = true;
            jadChallengeCheck = 15;
        }

        // Jad challenge victory quote
        Matcher matcher = JAD_CHALLENGE_VICTORY_MESSAGE.matcher(message);
        if (matcher.find()) {
            switch(random.nextInt(6)) {
                case 0:
                    zukChatMessage("Ungh. not bad...");
                    soundEngine.playClip(Sound.ZUK_JAD_VICTORY_1, executor);
                    break;
                case 1:
                    zukChatMessage("A champion rises.");
                    soundEngine.playClip(Sound.ZUK_JAD_VICTORY_2, executor);
                    break;
                case 2:
                    zukChatMessage("You have earned your right to live. For now.");
                    soundEngine.playClip(Sound.ZUK_JAD_VICTORY_3, executor);
                    break;
                case 3:
                    zukChatMessage("Hmph. Interesting...");
                    soundEngine.playClip(Sound.ZUK_JAD_VICTORY_4, executor);
                    break;
                case 4:
                    zukChatMessage("It appears you have exceeded my expectations.");
                    soundEngine.playClip(Sound.ZUK_JAD_VICTORY_5, executor);
                    break;
                case 5:
                    zukChatMessage("Not...so weak as you seem.");
                    soundEngine.playClip(Sound.ZUK_JAD_VICTORY_6, executor);
                    break;
            }
        }
    }

    public void onClientTick(ClientTick event) {
        if (fakeZukStatus == Status.ZUK_ATTACK.getStatus() && fakeNpcHandler.isOnFinalAnimationFrame(fakeZuk)) {
            fakeZukStatus = Status.ZUK_IDLE.getStatus();
            if (fakeZuk.getAnimationController() != null)
                fakeZuk.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.ZUK_IDLE.getAnimationID()));
        }
    }

    public void onGameTick(GameTick event) {
        if (jadChallengeCheck > 0)
            jadChallengeCheck--;

        if (jadChallengeCheck == 0 && !isInInfernoArea())
            isInJadChallenge = false;

        if (!isInJadChallenge) {
            if (fakeZuk != null) {
                fakeZuk.setActive(false);
                fakeNpcHandler.getOverlay().setOverheadText("");
            }
            fakeZuk = null;
        }

        // Jad challenge start quote
        if (jadChallengeCheck == 1 && isInInfernoArea()) {
            fakeZuk = fakeNpcHandler.createFakeNpc(NpcID.TZKALZUK, CustomAnimation.ZUK_ATTACK.getAnimationID(), 7104, 8256);
            fakeNpcHandler.getOverlay().setMaxTextHeightOffset(730);
            fakeNpcHandler.getOverlay().setMinTextHeightOffset(670);
            fakeZuk.setOrientation(0);
            fakeZukStatus = Status.ZUK_ATTACK.getStatus();

            switch(random.nextInt(3)) {
                case 0:
                    zukChatMessage("Rise, Jad!");
                    soundEngine.playClip(Sound.ZUK_JAD_1, executor);
                    break;
                case 1:
                    zukChatMessage("Unleash Jad!");
                    soundEngine.playClip(Sound.ZUK_JAD_2, executor);
                    break;
                case 2:
                    zukChatMessage("Enough. Jad will end this.");
                    soundEngine.playClip(Sound.ZUK_JAD_3, executor);
                    break;
            }
        }

        if (unpauseBuffer > 0)
            unpauseBuffer--;

        if (ticksSinceEncounterStart >= 0)
            ticksSinceEncounterStart++;
        if (ticksSinceLastQuote >= 0)
            ticksSinceLastQuote++;

        // Interval dialogue - Inferno waves
        if (isInInferno() && !zukFight) {
            int quoteChance = 250 - ticksSinceLastQuote;
            if (quoteChance < 1)
                quoteChance = 1;

            // Start/Unpausing of Inferno
            if (ticksSinceEncounterStart == 2) {
                if (!unpaused) {
                    zukChatMessage("A challenger approaches...");
                    soundEngine.playClip(Sound.ZUK_FIRST_WAVE_1, executor);
                    messageDelay = client.getTickCount() + 6;
                    infernoStart = true;
                }
                else {
                    zukChatMessage("Interesting...you return.");
                    soundEngine.playClip(Sound.ZUK_RELOG, executor);
                    unpaused = false;
                }
                ticksSinceLastQuote = 0;
            }

            // Delayed messages
            if (infernoStart && client.getTickCount() == messageDelay) {
                zukChatMessage("What makes you think you have what it takes?");
                soundEngine.playClip(Sound.ZUK_FIRST_WAVE_2, executor);
                infernoStart = false;
                ticksSinceLastQuote = 0;
            }

            // Random Interval dialogue
            if (ticksSinceLastQuote > 125 && random.nextInt(quoteChance) == 0 && antiSpam == 0) {
                int x = random.nextInt(12);
                while (x == lastPhrase)
                    x = random.nextInt(12);
                lastPhrase = x;
                switch (x) {
                    case 0:
                        zukChatMessage("Show me you are worthy!");
                        soundEngine.playClip(Sound.ZUK_WAVES_1, executor);
                        break;
                    case 1:
                        zukChatMessage("Fight, worm! Or crawl and die!");
                        soundEngine.playClip(Sound.ZUK_WAVES_2, executor);
                        break;
                    case 2:
                        zukChatMessage("To the slaughter!");
                        soundEngine.playClip(Sound.ZUK_WAVES_3, executor);
                        break;
                    case 3:
                        zukChatMessage("Prove your worth!");
                        soundEngine.playClip(Sound.ZUK_WAVES_4, executor);
                        break;
                    case 4:
                        zukChatMessage("Charge!");
                        soundEngine.playClip(Sound.ZUK_WAVES_5, executor);
                        break;
                    case 5:
                        zukChatMessage("Into the fray!");
                        soundEngine.playClip(Sound.ZUK_WAVES_6, executor);
                        break;
                    case 6:
                        zukChatMessage("The unworthy will burn.");
                        soundEngine.playClip(Sound.ZUK_WAVES_7, executor);
                        break;
                    case 7:
                        zukChatMessage("All of you, attack!");
                        soundEngine.playClip(Sound.ZUK_WAVES_8, executor);
                        break;
                    case 8:
                        zukChatMessage("Rise, scions of the Kiln!");
                        soundEngine.playClip(Sound.ZUK_WAVES_9, executor);
                        break;
                    case 9:
                        zukChatMessage("Fight or die!");
                        soundEngine.playClip(Sound.ZUK_WAVES_10, executor);
                        break;
                    case 10:
                        zukChatMessage("Die, and be reborn in flame!");
                        soundEngine.playClip(Sound.ZUK_WAVES_11, executor);
                        break;
                    case 11:
                        zukChatMessage("To ashes!");
                        soundEngine.playClip(Sound.ZUK_WAVES_12, executor);
                        break;
                }
                ticksSinceLastQuote = 0;
            }
        }

        // Random interval dialogue - Zuk
        if (isInInferno() && zukFight && boss != null) {

            int quoteChance = 13 - ticksSinceLastQuote / 10;
            if (quoteChance < 1)
                quoteChance = 1;

            if (ticksSinceEncounterStart == 600 && antiSpam == 0) {
                switch (random.nextInt(2)) {
                    case 0:
                        zukChatMessage("I grow weary of this. End it quickly!");
                        soundEngine.playClip(Sound.ZUK_ENRAGED_2, executor);
                        break;
                    case 1:
                        zukChatMessage("Fall, and burn to ash!");
                        soundEngine.playClip(Sound.ZUK_ENRAGED_1, executor);
                        break;
                }
                ticksSinceLastQuote = 0;
            }
            else if (ticksSinceEncounterStart % 10 == 0 && ticksSinceLastQuote >= 20 && antiSpam == 0 && random.nextInt(quoteChance) == 0) {
                int x = random.nextInt(14);
                while (x == lastPhrase)
                    x = random.nextInt(14);
                lastPhrase = x;
                switch (x) {
                    case 0:
                        zukChatMessage("Drag them to the molten deep!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_1, executor);
                        break;
                    case 1:
                        zukChatMessage("Flames unending...");
                        soundEngine.playClip(Sound.ZUK_FIGHT_2, executor);
                        break;
                    case 2:
                        zukChatMessage("Break beneath me!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_3, executor);
                        break;
                    case 3:
                        zukChatMessage("Tremble before me!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_4, executor);
                        break;
                    case 4:
                        zukChatMessage("Suffer!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_5, executor);
                        break;
                    case 5:
                        zukChatMessage("Sear!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_6, executor);
                        break;
                    case 6:
                        zukChatMessage("Burn!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_7, executor);
                        break;
                    case 7:
                        zukChatMessage("Die!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_8, executor);
                        break;
                    case 8:
                        zukChatMessage("Flames consume you!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_9, executor);
                        break;
                    case 9:
                        zukChatMessage("Fall, and burn to ash!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_10, executor);
                        break;
                    case 10:
                        zukChatMessage("Be scourged by flame!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_11, executor);
                        break;
                    case 11:
                        zukChatMessage("Begone!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_12, executor);
                        break;
                    case 12:
                        zukChatMessage("Ful's flame burns within me!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_13, executor);
                        break;
                    case 13:
                        zukChatMessage("Burn to nothing!");
                        soundEngine.playClip(Sound.ZUK_FIGHT_14, executor);
                        break;
                }
                ticksSinceLastQuote = 0;
            }
        }

        if (!isInInfernoArea())
            resetInferno();
        else
            fakeNpcHandler.onGameTick(event);

        if (unpaused && unpauseBuffer == 0 && !isInInfernoArea())
            unpaused = false;

        if (antiSpam > 0)
            antiSpam--;
    }

    public void onNpcSpawned(NpcSpawned event) {
        int npcID = event.getNpc().getId();

        // TzKal-Zuk combat initiated
        if (npcID == 7706) {
            boss = event.getNpc();
            zukFight = true;
            ticksSinceEncounterStart = 0;
            ticksSinceLastQuote = 0;
            switch (random.nextInt(3)) {
                case 0:
                    zukChatMessage("Now - the true battle begins!");
                    boss.setOverheadCycle(250);
                    soundEngine.playClip(Sound.ZUK_START_1, executor);
                    break;
                case 1:
                    zukChatMessage("Out of the way, weaklings!");
                    boss.setOverheadCycle(250);
                    soundEngine.playClip(Sound.ZUK_START_2, executor);
                    break;
                case 2:
                    zukChatMessage("It appears you have exceeded my expectations. Perhaps you are worthy... to fall before my might!");
                    boss.setOverheadCycle(500);
                    soundEngine.playClip(Sound.ZUK_START_3, executor);
                    break;
            }
        }
    }

    public void onActorDeath(ActorDeath event) {
        Actor actor = event.getActor();
        if (actor instanceof Player)
        {
            Player player = (Player) actor;
            if (isInInfernoArea() && player == client.getLocalPlayer())
            {
                switch(random.nextInt(3)) {
                    case 0:
                        zukChatMessage("Pathetic.");
                        soundEngine.playClip(Sound.ZUK_DEFEAT_1, executor);
                        break;
                    case 1:
                        zukChatMessage("Worthless. Just like the rest.");
                        soundEngine.playClip(Sound.ZUK_DEFEAT_2, executor);
                        break;
                    case 2:
                        zukChatMessage("You have failed, as expected.");
                        soundEngine.playClip(Sound.ZUK_DEFEAT_3, executor);
                        break;
                }
                antiSpam = 10;
            }
        }

        if (Objects.equals(actor.getName(), "TzKal-Zuk")) {
            switch(random.nextInt(3)) {
                case 0:
                    zukChatMessage("Impossible...");
                    soundEngine.playClip(Sound.ZUK_VICTORY_1, executor);
                    break;
                case 1:
                    zukChatMessage("Not.. since Bandos...");
                    soundEngine.playClip(Sound.ZUK_VICTORY_2, executor);
                    break;
                case 2:
                    zukChatMessage("Finally... After thousands of years...");
                    soundEngine.playClip(Sound.ZUK_VICTORY_3, executor);
                    break;
            }
            boss.setOverheadCycle(300);
            antiSpam = 10;
        }
    }

    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGING_IN || event.getGameState() == GameState.HOPPING)
            loggingIn = true;

        if (event.getGameState() == GameState.LOGGED_IN && loggingIn) {
            unpaused = true;
            unpauseBuffer = 4;
            loggingIn = false;
            resetInferno();
        }
    }


    public void zukChatMessage(String message) {
        if (fakeZuk != null) {
            fakeNpcHandler.addOverheadText(fakeZuk, "TzKal-Zuk", message);
        }
        else {
            client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", message, null);
            if (zukFight && boss != null) {
                boss.setOverheadCycle(150);
                boss.setOverheadText(message);
            }
        }
    }

    public void resetInferno() {
        infernoStart = false;
        zukFight = false;
        lastPhrase = -1;
        messageDelay = -1;
        ticksSinceEncounterStart = -1;
        ticksSinceLastQuote = -1;
    }
}
