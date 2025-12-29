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
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatMessageManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

public class Zemouregal implements Boss {
    private static final int UNGAEL_REGION_ID = 9023;

    @Getter @Setter
    private NPC boss;
    @Getter @Setter
    private RuneLiteObject zemouregal;
    @Getter @Setter
    private int zemouregalStatus = Status.INACTIVE.getStatus();
    @Getter @Setter
    private LocalPoint zemouregalLocation;
    @Getter @Setter
    private FakeNpcHandler fakeNpcHandler = new FakeNpcHandler();

    private int ticksSinceEncounterStart = -1;
    private int ticksSinceLastQuote = -1;
    private int antiSpam = 0;
    private int turnTimer = 0;

    private int lastPhrase = -1;

    private boolean zemoFaceVorkath = false;

    private final Random random = new Random();

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
        fakeNpcHandler.setClient(client);
        fakeNpcHandler.setChatMessageManager(chatMessageManager);
        fakeNpcHandler.setOverlay(overlay);
    }

    private boolean isOnUngael() {
        return client.getMapRegions() != null && ArrayUtils.contains(client.getMapRegions(), UNGAEL_REGION_ID) && client.getTopLevelWorldView().isInstance();
    }

    public void onClientTick(ClientTick event) {
        if (turnTimer > 0)
            turnTimer--;

        if (isOnUngael() && zemouregal != null) {
            int oldOrientation = zemouregal.getOrientation();

            if (zemoFaceVorkath)
                zemouregal.setOrientation(1536);
            else
                fakeNpcHandler.facePlayer(zemouregal);

            if (zemouregalStatus == Status.ZEMOUREGAL_START.getStatus() && fakeNpcHandler.isOnFinalAnimationFrame(zemouregal)) {
                zemouregalStatus = Status.ZEMOUREGAL_CAST.getStatus();
                setAnimation(CustomAnimation.ZEMOUREGAL_CAST_1.getAnimationID());
            }

            if (zemouregalStatus == Status.ZEMOUREGAL_CAST.getStatus() && fakeNpcHandler.isOnFinalAnimationFrame(zemouregal)) {
                zemoFaceVorkath = false;
                zemouregalStatus = Status.ZEMOUREGAL_IDLE.getStatus();
                setAnimation(CustomAnimation.ZEMOUREGAL_IDLE.getAnimationID());
            }

            if (zemouregalStatus == Status.ZEMOUREGAL_SPELL_WINDUP.getStatus() && fakeNpcHandler.isOnFinalAnimationFrame(zemouregal)) {
                zemouregalStatus = Status.ZEMOUREGAL_SPELL.getStatus();
                setAnimation(CustomAnimation.ZEMOUREGAL_CAST_6.getAnimationID());
            }

            if (zemouregalStatus == Status.ZEMOUREGAL_SPELL.getStatus() && fakeNpcHandler.isOnFinalAnimationFrame(zemouregal)) {
                zemouregalStatus = Status.ZEMOUREGAL_IDLE.getStatus();
                setAnimation(CustomAnimation.ZEMOUREGAL_IDLE.getAnimationID());
            }

            if (zemouregalStatus == Status.ZEMOUREGAL_IDLE.getStatus() && turnTimer == 0 && Math.abs(zemouregal.getOrientation() - oldOrientation) >= 5) {
                zemouregalStatus = Status.ZEMOUREGAL_TURN.getStatus();
                turnTimer = 20;
                setAnimation(CustomAnimation.ZEMOUREGAL_WALK_1.getAnimationID());
            }

            if (zemouregalStatus == Status.ZEMOUREGAL_TURN.getStatus() && turnTimer == 0 && zemouregal.getOrientation() == oldOrientation) {
                zemouregalStatus = Status.ZEMOUREGAL_IDLE.getStatus();
                turnTimer = 20;
                setAnimation(CustomAnimation.ZEMOUREGAL_IDLE.getAnimationID());
            }

            if (zemouregalStatus == Status.ZEMOUREGAL_END.getStatus()) {
                zemouregalStatus = Status.ZEMOUREGAL_DESPAWN.getStatus();
                setAnimation(CustomAnimation.ZEMOUREGAL_DESPAWN.getAnimationID());
            }

            if (zemouregalStatus == Status.ZEMOUREGAL_DESPAWN.getStatus() && fakeNpcHandler.isOnFinalAnimationFrame(zemouregal)) {
                resetVorkath();
            }
        }
    }

    public void onGameTick(GameTick event) {
        // Reset variables if no longer in boss arena
        if (!isOnUngael())
            resetVorkath();
        else
            fakeNpcHandler.onGameTick(event);

        if (ticksSinceEncounterStart >= 0)
            ticksSinceEncounterStart++;
        if (ticksSinceLastQuote >= 0)
            ticksSinceLastQuote++;

        if (antiSpam > 0)
            antiSpam--;

        // Random interval dialogue - Zemouregal
        if (isOnUngael() && zemouregal != null) {

            int quoteChance = 110 - ticksSinceLastQuote * 2;
            if (quoteChance < 1)
                quoteChance = 1;

            if (ticksSinceEncounterStart % 100 == 0 && random.nextInt(3) == 0 && antiSpam == 0) {
                switch (random.nextInt(5)) {
                    case 0:
                        zemouregalChatMessage("This piteous joke ends now.");
                        soundEngine.playClip(Sound.VORKATH_UNCOMMON_1, executor);
                        break;
                    case 1:
                        zemouregalChatMessage("DIE!");
                        soundEngine.playClip(Sound.VORKATH_UNCOMMON_2, executor);
                        break;
                    case 2:
                        zemouregalChatMessage("Say farewell, little one!");
                        soundEngine.playClip(Sound.VORKATH_UNCOMMON_3, executor);
                        break;
                    case 3:
                        zemouregalChatMessage("Cut them down without mercy!");
                        soundEngine.playClip(Sound.VORKATH_UNCOMMON_4, executor);
                        break;
                    case 4:
                        zemouregalChatMessage("Cast my glorious vengeance down upon them!");
                        soundEngine.playClip(Sound.VORKATH_UNCOMMON_5, executor);
                        break;
                }
                switch (random.nextInt(4)) {
                    case 0:
                        zemouregalStatus = Status.ZEMOUREGAL_SPELL.getStatus();
                        setAnimation(CustomAnimation.ZEMOUREGAL_CAST_2.getAnimationID());
                        break;
                    case 1:
                        zemouregalStatus = Status.ZEMOUREGAL_SPELL.getStatus();
                        setAnimation(CustomAnimation.ZEMOUREGAL_CAST_3.getAnimationID());
                        break;
                    case 2:
                        zemouregalStatus = Status.ZEMOUREGAL_SPELL.getStatus();
                        setAnimation(CustomAnimation.ZEMOUREGAL_CAST_4.getAnimationID());
                        break;
                    case 3:
                        zemouregalStatus = Status.ZEMOUREGAL_SPELL_WINDUP.getStatus();
                        setAnimation(CustomAnimation.ZEMOUREGAL_CAST_5.getAnimationID());
                        break;
                }
                antiSpam = 5;
                ticksSinceLastQuote = 0;
            }
            else if (ticksSinceLastQuote >= 15 && random.nextInt(quoteChance) == 0 && antiSpam == 0) {
                int x = random.nextInt(12);
                while (x == lastPhrase)
                    x = random.nextInt(12);
                lastPhrase = x;
                switch (x) {
                    case 0:
                        zemouregalChatMessage("Your struggle, like your life, is meaningless.");
                        soundEngine.playClip(Sound.VORKATH_COMMON_1, executor);
                        break;
                    case 1:
                        zemouregalChatMessage("No one can stand against my mighty undead!");
                        soundEngine.playClip(Sound.VORKATH_COMMON_2, executor);
                        break;
                    case 2:
                        zemouregalChatMessage("No escape, you puny knave!");
                        soundEngine.playClip(Sound.VORKATH_COMMON_3, executor);
                        break;
                    case 3:
                        zemouregalChatMessage("You WILL yield to my might!");
                        soundEngine.playClip(Sound.VORKATH_COMMON_4, executor);
                        break;
                    case 4:
                        zemouregalChatMessage("Be gone, gnawing pest!");
                        soundEngine.playClip(Sound.VORKATH_COMMON_5, executor);
                        break;
                    case 5:
                        zemouregalChatMessage("You will be one with my undead troops.");
                        soundEngine.playClip(Sound.VORKATH_COMMON_6, executor);
                        break;
                    case 6:
                        zemouregalChatMessage("These are your final moments, halfwit!");
                        soundEngine.playClip(Sound.VORKATH_COMMON_7, executor);
                        break;
                    case 7:
                        zemouregalChatMessage("I'll make a feast of your blood, and be strengthened!");
                        soundEngine.playClip(Sound.VORKATH_COMMON_8, executor);
                        break;
                    case 8:
                        zemouregalChatMessage("Why do you bother to test me?");
                        soundEngine.playClip(Sound.VORKATH_COMMON_9, executor);
                        break;
                    case 9:
                        zemouregalChatMessage("You thought you were a match for my majesty?");
                        soundEngine.playClip(Sound.VORKATH_COMMON_10, executor);
                        break;
                    case 10:
                        zemouregalChatMessage("I shall rend the very flesh from your bones!");
                        soundEngine.playClip(Sound.VORKATH_COMMON_11, executor);
                        break;
                    case 11:
                        zemouregalChatMessage("Pah! Pitiful.");
                        soundEngine.playClip(Sound.VORKATH_COMMON_12, executor);
                        break;
                }
                antiSpam = 5;
                ticksSinceLastQuote = 0;
            }
        }
    }

    public void onNpcChanged(NpcChanged event) {
        if (event.getNpc() == null)
            return;

        int npcID = event.getNpc().getId();

        // Vorkath combat begins
        if (event.getOld().getId() == 8058 && npcID == 8061) {
            boss = event.getNpc();
            ticksSinceEncounterStart = 0;
            ticksSinceLastQuote = 0;
        }
        // Vorkath initiated
        if (event.getOld().getId() == 8059 && npcID == 8058) {
            boss = event.getNpc();
            resetVorkath();
            zemouregal = fakeNpcHandler.createFakeNpc(NpcID.ZEMOUREGAL, CustomAnimation.ZEMOUREGAL_SPAWN.getAnimationID(),
                         boss.getLocalLocation().getX() - 384, boss.getLocalLocation().getY() - 256);
            fakeNpcHandler.getOverlay().setMaxTextHeightOffset(380);
            fakeNpcHandler.getOverlay().setMinTextHeightOffset(320);
            zemouregalStatus = Status.ZEMOUREGAL_START.getStatus();
            zemoFaceVorkath = true;

            switch (random.nextInt(3)) {
                case 0:
                    zemouregalChatMessage("Obey your master, you dumb puppet!");
                    soundEngine.playClip(Sound.VORKATH_START_1, executor);
                    break;
                case 1:
                    zemouregalChatMessage("The dragon WILL be mine!");
                    soundEngine.playClip(Sound.VORKATH_START_2, executor);
                    break;
                case 2:
                    zemouregalChatMessage("Rise you cursed weakling, I shan't tell you again!");
                    soundEngine.playClip(Sound.VORKATH_START_3, executor);
                    break;
            }
        }
    }

    public void onActorDeath(ActorDeath actorDeath)
    {
        Actor actor = actorDeath.getActor();
        if (actor instanceof Player) {
            Player player = (Player) actor;
            if (isOnUngael() && zemouregal != null && player == client.getLocalPlayer()) {
                switch(random.nextInt(5)) {
                    case 0:
                        zemouregalChatMessage("Say hello to Death for me!");
                        soundEngine.playClip(Sound.VORKATH_DEFEAT_1, executor);
                        break;
                    case 1:
                        zemouregalChatMessage("Death is, quite frankly, too good for you.");
                        soundEngine.playClip(Sound.VORKATH_DEFEAT_2, executor);
                        break;
                    case 2:
                        zemouregalChatMessage("Finally, an end to your meddling.");
                        soundEngine.playClip(Sound.VORKATH_DEFEAT_3, executor);
                        break;
                    case 3:
                        zemouregalChatMessage("Death protects you? I meant to have you for my own...");
                        soundEngine.playClip(Sound.VORKATH_DEFEAT_4, executor);
                        break;
                    case 4:
                        zemouregalChatMessage("Your life is payment for this folly!");
                        soundEngine.playClip(Sound.VORKATH_DEFEAT_5, executor);
                        break;
                }
                antiSpam = 5;
                ticksSinceLastQuote = 0;
            }
        }

        if (Objects.equals(actor.getName(), "Vorkath") && zemouregal != null) {
            switch(random.nextInt(5)) {
                case 0:
                    fakeNpcHandler.addOverheadText(zemouregal, "Zemouregal", "Bah! Useless!",3,true);
                    soundEngine.playClip(Sound.VORKATH_VICTORY_1, executor);
                    break;
                case 1:
                    fakeNpcHandler.addOverheadText(zemouregal, "Zemouregal", "Are you determined to fail?",3,true);
                    soundEngine.playClip(Sound.VORKATH_VICTORY_2, executor);
                    break;
                case 2:
                    fakeNpcHandler.addOverheadText(zemouregal, "Zemouregal", "GET UP! - Why must you test my patience?",3,true);
                    soundEngine.playClip(Sound.VORKATH_VICTORY_3, executor);
                    break;
                case 3:
                    fakeNpcHandler.addOverheadText(zemouregal, "Zemouregal", "Urgh, must I be forever plagued by lesser beings?",3,true);
                    soundEngine.playClip(Sound.VORKATH_VICTORY_4, executor);
                    break;
                case 4:
                    fakeNpcHandler.addOverheadText(zemouregal, "Zemouregal", "Inadequate failure...",3,true);
                    soundEngine.playClip(Sound.VORKATH_VICTORY_5, executor);
                    break;
            }
            ticksSinceLastQuote = 0;
            antiSpam = 5;
            zemouregalStatus = Status.ZEMOUREGAL_END.getStatus();
        }
    }

    public void onHitsplatApplied(HitsplatApplied event) {
        // Stepping on Vorkath acid
        if (event.getActor().getName() != null               &&
                "Vorkath".equals(event.getActor().getName()) &&
                event.getHitsplat().getHitsplatType() == 6 && antiSpam == 0) {

            switch(random.nextInt(4)) {
                case 0:
                    zemouregalChatMessage("Suffocate this land with bile!");
                    soundEngine.playClip(Sound.VORKATH_POISON_1, executor);
                    break;
                case 1:
                    zemouregalChatMessage("Spew your venom, my malformed pet!");
                    soundEngine.playClip(Sound.VORKATH_POISON_2, executor);
                    break;
                case 2:
                    zemouregalChatMessage("Yes, heal the dumb beast!");
                    soundEngine.playClip(Sound.VORKATH_POISON_3, executor);
                    break;
                case 3:
                    zemouregalChatMessage("Drown in your own gore!");
                    soundEngine.playClip(Sound.VORKATH_POISON_4, executor);
                    break;
            }
            antiSpam = 10;
            ticksSinceLastQuote = 0;
        }
    }

    public void zemouregalChatMessage(String message) {
        if (zemouregal != null) {
            fakeNpcHandler.addOverheadText(zemouregal, "Zemouregal", message);
        }
    }

    public void setAnimation(int animationID) {
        if (zemouregal.getAnimationController() != null)
            zemouregal.getAnimationController().setAnimation(client.loadAnimation(animationID));
    }

    public void resetVorkath() {
        zemouregalStatus = Status.INACTIVE.getStatus();
        antiSpam = 0;
        zemoFaceVorkath = false;
        turnTimer = 0;

        if (isOnUngael())
            fakeNpcHandler.getOverlay().setOverheadText("");

        if (zemouregal != null)
            zemouregal.setActive(false);

        zemouregal = null;
        lastPhrase = -1;
        ticksSinceEncounterStart = -1;
        ticksSinceLastQuote = -1;
    }
}
