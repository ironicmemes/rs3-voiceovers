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

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Amascut implements Boss {
    private static final int WARDENS_ARENA_1 = 15184;
    private static final int WARDENS_ARENA_2 = 15696;

    @Getter @Setter
    private NPC boss;
    @Getter @Setter
    private RuneLiteObject amascut;
    @Getter @Setter
    private int amascutStatus = Status.INACTIVE.getStatus();
    @Getter @Setter
    private FakeNpcHandler fakeNpcHandler = new FakeNpcHandler();

    private int ticksSinceEncounterStart = -1;
    private int ticksSinceLastQuote = -1;
    private int antiSpam = 0;
    private int turnTimer = 0;
    private int messageDelay = -1;

    private int lastPhrase = -1;

    private int amascutMonologue = 0;
    private int amascutTeleportTimer = 0;
    private int phase = 0;
    @Getter @Setter
    private boolean tumekenP3 = true;
    private int projectileImpactTick = -1;
    private int specialProjectile = 0;


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

    private boolean isInWardensArena() {
        return client.getMapRegions() != null && (ArrayUtils.contains(client.getMapRegions(), WARDENS_ARENA_1) ||
                ArrayUtils.contains(client.getMapRegions(), WARDENS_ARENA_2));
    }

    private void amascutTeleport() {
        if (++amascutTeleportTimer > 10 && amascutStatus != Status.AMASCUT_TELEPORT_OUT.getStatus()) {
            amascutStatus = Status.AMASCUT_TELEPORT_OUT.getStatus();
            setAnimation(CustomAnimation.AMASCUT_TELEPORT_OUT.getAnimationID());
        }
    }

    public void onChatMessage(ChatMessage event) {
        String message = event.getMessage();

        // Wardens start
        if (isInWardensArena()) {
            if (message.equals("Challenge started: The Wardens.")) {
                resetWardens();

                amascut = fakeNpcHandler.createFakeNpc(NpcID.AMASCUT_11696, CustomAnimation.AMASCUT_SPAWN.getAnimationID(), 6208, 6592);
                fakeNpcHandler.getOverlay().setMaxTextHeightOffset(380);
                fakeNpcHandler.getOverlay().setMinTextHeightOffset(320);
                amascut.setOrientation(1024);
                amascutStatus = Status.AMASCUT_START.getStatus();

                fakeNpcHandler.addOverheadText(amascut,"Amascut","Destruction. Annihilation. My vision for this world.", 10, true);
                soundEngine.playClip(Sound.AMASCUT_START_1, executor);
                amascutMonologue = 1;
                messageDelay = client.getTickCount() + 10;
                antiSpam = 45;
                phase = 1;
                ticksSinceEncounterStart = 0;
                ticksSinceLastQuote = 0;
            }

            Matcher matcher = Pattern.compile("Challenge complete: The Wardens.(.+?)").matcher(message);
            if (matcher.find() && amascut != null) {
                switch (random.nextInt(3)) {
                    case 0:
                        amascutChatMessage("Impossible...");
                        soundEngine.playClip(Sound.AMASCUT_VICTORY_1, executor);
                        break;
                    case 1:
                        amascutChatMessage("This cannot be the end...");
                        soundEngine.playClip(Sound.AMASCUT_VICTORY_2, executor);
                        break;
                    case 2:
                        amascutChatMessage("NO!");
                        soundEngine.playClip(Sound.AMASCUT_VICTORY_3, executor);
                        break;
                }
                amascutMonologue = -1;
                amascutStatus = Status.AMASCUT_END.getStatus();
                setAnimation(CustomAnimation.AMASCUT_DESPAWN.getAnimationID());
            }

            if (message.equals("Your party failed to complete the challenge. You may try again...") ||
                message.equals("You failed to survive the Tombs of Amascut.")) {

                if (amascut != null)
                    amascutChatMessage("Your souls are mine!");
                else
                    client.addChatMessage(ChatMessageType.PUBLICCHAT, "Amascut", "Your souls are mine!", null);

                soundEngine.playClip(Sound.AMASCUT_DEFEAT_3, executor);
                resetWardens();
            }
        }
    }

    public void onClientTick(ClientTick event) {
        if (turnTimer > 0)
            turnTimer--;

        // Amascut animations
        if (isInWardensArena() && amascut != null) {
            int oldOrientation = amascut.getOrientation();

            if (phase == 3) {
                if (amascutStatus == Status.AMASCUT_SUMMON_PHANTOM_1.getStatus())
                    fakeNpcHandler.faceDirection(amascut, 1280);
                else if (amascutStatus == Status.AMASCUT_SUMMON_PHANTOM_2.getStatus())
                    fakeNpcHandler.faceDirection(amascut, 640);
                else
                    fakeNpcHandler.faceDirection(amascut, 1024);
            }
            else if (amascutStatus != Status.AMASCUT_START.getStatus()) {
                fakeNpcHandler.facePlayer(amascut);
            }

            if ((amascutStatus == Status.AMASCUT_START.getStatus()              ||
                 amascutStatus == Status.AMASCUT_CAST.getStatus()               ||
                 amascutStatus == Status.AMASCUT_SUMMON_PHANTOM_2.getStatus()   ||
                 amascutStatus == Status.AMASCUT_TELEPORT_IN.getStatus())       && fakeNpcHandler.isOnFinalAnimationFrame(amascut)) {

                amascutStatus = Status.AMASCUT_IDLE.getStatus();

                if (phase == 3)
                    setAnimation(CustomAnimation.AMASCUT_IDLE_2.getAnimationID());
                else
                    setAnimation(CustomAnimation.AMASCUT_IDLE_1.getAnimationID());
            }

            if (amascutStatus == Status.AMASCUT_TELEPORT_OUT.getStatus() && fakeNpcHandler.isOnFinalAnimationFrame(amascut)) {
                amascutStatus = Status.AMASCUT_TELEPORT_IN.getStatus();
                setAnimation(CustomAnimation.AMASCUT_TELEPORT_IN.getAnimationID());

                LocalPoint fakeNpcLocation;

                if (client.getLocalPlayer().getLocalLocation().getX() > 6592)
                    fakeNpcLocation = new LocalPoint(7744, 6848, client.getLocalPlayer().getWorldView());
                else if (client.getLocalPlayer().getLocalLocation().getX() < 5696)
                    fakeNpcLocation = new LocalPoint(4672, 6848, client.getLocalPlayer().getWorldView());
                else if (client.getLocalPlayer().getLocalLocation().getY() < 6464)
                    fakeNpcLocation = new LocalPoint(6208, 6336, client.getLocalPlayer().getWorldView());
                else
                    fakeNpcLocation = new LocalPoint(6208, 6592, client.getLocalPlayer().getWorldView());

                amascutTeleportTimer = -20;
                amascut.setLocation(fakeNpcLocation, client.getLocalPlayer().getWorldArea().getPlane());

                if (fakeNpcLocation.getY() == 6848 && antiSpam == 0) {
                    amascutChatMessage("The mice fight back?");
                    soundEngine.playClip(Sound.AMASCUT_TELEPORT_1, executor);
                    antiSpam = 5;
                }
                if (fakeNpcLocation.getX() == 6208 && antiSpam == 0) {
                    amascutChatMessage("You dare to turn your back on me?");
                    soundEngine.playClip(Sound.AMASCUT_TELEPORT_2, executor);
                    antiSpam = 5;
                }
            }

            if (amascutStatus == Status.AMASCUT_IDLE.getStatus() && turnTimer == 0 && Math.abs(amascut.getOrientation() - oldOrientation) >= 5) {
                amascutStatus = Status.AMASCUT_TURN.getStatus();
                turnTimer = 20;
                setAnimation(CustomAnimation.AMASCUT_WALK_1.getAnimationID());
            }

            if (amascutStatus == Status.AMASCUT_TURN.getStatus() && turnTimer == 0 && amascut.getOrientation() == oldOrientation) {
                amascutStatus = Status.AMASCUT_IDLE.getStatus();
                turnTimer = 100;
                if (phase == 3)
                    setAnimation(CustomAnimation.AMASCUT_IDLE_2.getAnimationID());
                else
                    setAnimation(CustomAnimation.AMASCUT_IDLE_1.getAnimationID());
            }

            if (amascutStatus == Status.AMASCUT_SUMMON_PHANTOM_1.getStatus() && fakeNpcHandler.isOnFinalAnimationFrame(amascut)) {
                amascutStatus = Status.AMASCUT_SUMMON_PHANTOM_2.getStatus();
                switch(random.nextInt(3)) {
                    case 0:
                        setAnimation(CustomAnimation.AMASCUT_CAST_6.getAnimationID());
                        break;
                    case 1:
                        setAnimation(CustomAnimation.AMASCUT_CAST_7.getAnimationID());
                        break;
                    case 2:
                        setAnimation(CustomAnimation.AMASCUT_CAST_10.getAnimationID());
                        break;
                }
            }

            if (amascutStatus == Status.AMASCUT_ENRAGE_1.getStatus() && fakeNpcHandler.isOnFinalAnimationFrame(amascut)) {
                amascutStatus = Status.AMASCUT_ENRAGE_2.getStatus();
                setAnimation(CustomAnimation.AMASCUT_ENRAGE_2.getAnimationID());
            }

            if (amascutStatus == Status.AMASCUT_ENRAGE_2.getStatus() && fakeNpcHandler.isOnFinalAnimationFrame(amascut)) {
                amascutStatus = Status.AMASCUT_ENRAGE_3.getStatus();
                setAnimation(CustomAnimation.AMASCUT_ENRAGE_3.getAnimationID());
            }

            if (amascutStatus == Status.AMASCUT_ENRAGE_3.getStatus() && fakeNpcHandler.isOnFinalAnimationFrame(amascut)) {
                if (amascut.getAnimationController() != null)
                    amascut.getAnimationController().setFrame(16);
            }

            if (amascutStatus == Status.AMASCUT_END.getStatus() && fakeNpcHandler.isOnFinalAnimationFrame(amascut)) {
                resetWardens();
            }
        }
    }

    public void onGameTick(GameTick event) {
        if (ticksSinceEncounterStart >= 0)
            ticksSinceEncounterStart++;
        if (ticksSinceLastQuote >= 0)
            ticksSinceLastQuote++;

        // Delayed messages
        if (amascutMonologue > 0 && client.getTickCount() == messageDelay) {
            switch(amascutMonologue) {
                case 1:
                    fakeNpcHandler.addOverheadText(amascut, "Amascut","How close it came to becoming a reality.", 8, true);
                    amascutMonologue = 2;
                    messageDelay = client.getTickCount() + 8;
                    soundEngine.playClip(Sound.AMASCUT_START_2, executor);
                    break;
                case 2:
                    amascutChatMessage("Picture the heroes who would have risen against me.");
                    amascutMonologue = 3;
                    messageDelay = client.getTickCount() + 5;
                    soundEngine.playClip(Sound.AMASCUT_START_3, executor);
                    break;
                case 3:
                    amascutChatMessage("The challenges they would have overcome.");
                    amascutMonologue = 4;
                    messageDelay = client.getTickCount() + 4;
                    soundEngine.playClip(Sound.AMASCUT_START_4, executor);
                    break;
                case 4:
                    amascutChatMessage("And the rewards they would have reaped.");
                    amascutMonologue = 5;
                    messageDelay = client.getTickCount() + 5;
                    soundEngine.playClip(Sound.AMASCUT_START_5, executor);
                    break;
                case 5:
                    fakeNpcHandler.addOverheadText(amascut, "Amascut","Who among you is worthy of facing a goddess?", 7, true);
                    amascutMonologue = 6;
                    messageDelay = client.getTickCount() + 7;
                    soundEngine.playClip(Sound.AMASCUT_START_6, executor);
                    break;
                case 6:
                    amascutChatMessage("Show me...");
                    amascutMonologue = 0;
                    messageDelay = -1;
                    soundEngine.playClip(Sound.AMASCUT_START_7, executor);
                    break;
                case 7:
                    switch(random.nextInt(3)) {
                        case 0:
                            amascutChatMessage("FALL TO THE SHADOW!");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_5, executor);
                            break;
                        case 1:
                            amascutChatMessage("YOU ARE NOTHING!");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_6, executor);
                            break;
                        case 2:
                            amascutChatMessage("MWAHAHAHA!");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_7, executor);
                            break;
                    }
                    amascutStatus = Status.AMASCUT_CAST.getStatus();
                    setAnimation(CustomAnimation.AMASCUT_CAST_5.getAnimationID());
                    amascutMonologue = 0;
                    messageDelay = -1;
                    break;
                case 8:
                    amascutChatMessage("It is not merely my power you face!");
                    amascutMonologue = 9;
                    messageDelay = client.getTickCount() + 10;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_1, executor);
                    break;
                case 9:
                    if (!tumekenP3) {
                        amascutChatMessage("Het...Scabaras...Bear witness!");
                        soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_2, executor);
                    }
                    else {
                        amascutChatMessage("Bring forth Crondis and Apmeken!");
                        soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_3, executor);
                    }
                    amascutMonologue = 10;
                    messageDelay = client.getTickCount() + 50;
                    amascutStatus = Status.AMASCUT_SUMMON_PHANTOM_1.getStatus();
                    switch(random.nextInt(3)) {
                        case 0:
                            setAnimation(CustomAnimation.AMASCUT_CAST_6.getAnimationID());
                            break;
                        case 1:
                            setAnimation(CustomAnimation.AMASCUT_CAST_7.getAnimationID());
                            break;
                        case 2:
                            setAnimation(CustomAnimation.AMASCUT_CAST_10.getAnimationID());
                            break;
                    }
                    break;
                case 10:
                    amascutChatMessage("There can be no light without darkness.");
                    amascutMonologue = 11;
                    messageDelay = client.getTickCount() + 20;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_4, executor);
                    break;
                case 11:
                    amascutChatMessage("Your light will be snuffed out, once and for all!");
                    amascutMonologue = 12;
                    messageDelay = client.getTickCount() + 50;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_5, executor);
                    break;
                case 12:
                    amascutChatMessage("Our father will be reborn!");
                    amascutMonologue = 13;
                    messageDelay = client.getTickCount() + 20;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_6, executor);
                    break;
                case 13:
                    amascutChatMessage("His soul will be judged and condemned!");
                    amascutMonologue = 14;
                    messageDelay = client.getTickCount() + 20;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_7, executor);
                    break;
                case 14:
                    amascutChatMessage("I will have my vengeance!");
                    amascutMonologue = 15;
                    messageDelay = client.getTickCount() + 40;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_8, executor);
                    break;
                case 15:
                    amascutChatMessage("Tumeken's heart...I will claw it out myself if I have to!");
                    amascutMonologue = 16;
                    messageDelay = client.getTickCount() + 40;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_9, executor);
                    break;
                case 16:
                    amascutChatMessage("Was I nothing to him?");
                    amascutMonologue = 17;
                    messageDelay = client.getTickCount() + 40;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_10, executor);
                    break;
                case 17:
                    amascutChatMessage("So be it...");
                    amascutMonologue = 18;
                    messageDelay = client.getTickCount() + 15;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_11, executor);
                    break;
                case 18:
                    amascutChatMessage("Come, mortal.");
                    amascutMonologue = 19;
                    messageDelay = client.getTickCount() + 10;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_12, executor);
                    break;
                case 19:
                    amascutChatMessage("If I cannot judge his soul, then I will condemn yours in its place.");
                    amascutMonologue = 20;
                    messageDelay = client.getTickCount() + 40;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_13, executor);
                    break;
                case 20:
                    amascutChatMessage("I am the one who devours.");
                    amascutMonologue = 21;
                    messageDelay = client.getTickCount() + 25;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_14, executor);
                    break;
                case 21:
                    amascutChatMessage("I AM");
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_15, executor);
                    amascutMonologue = 22;
                    messageDelay = client.getTickCount() + 15;
                    amascutStatus = Status.AMASCUT_ENRAGE_1.getStatus();
                    setAnimation(CustomAnimation.AMASCUT_ENRAGE_1.getAnimationID());
                    break;
                case 22:
                    amascutChatMessage("THE GOD");
                    amascutMonologue = 23;
                    messageDelay = client.getTickCount() + 15;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_16, executor);
                    break;
                case 23:
                    amascutChatMessage("OF");
                    amascutMonologue = 24;
                    messageDelay = client.getTickCount() + 15;
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_17, executor);
                    break;
                case 24:
                    amascutChatMessage("DESTRUCTION!");
                    soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_18, executor);
                    amascutMonologue = 0;
                    messageDelay = -1;
                    break;
                default:
                    break;
            }
        }

        if (isInWardensArena() && amascut != null) {
            if (phase != 3) {
                if (amascutTeleportTimer < 0)
                    amascutTeleportTimer++;

                // Teleport based on player location
                if (client.getLocalPlayer().getLocalLocation().getX() > 6720) {
                    if (amascut.getLocation().getX() != 7744) {
                        amascutTeleport();
                    }
                }
                else if (client.getLocalPlayer().getLocalLocation().getX() < 5696) {
                    if (amascut.getLocation().getX() != 4672) {
                        amascutTeleport();
                    }
                }
                else if (client.getLocalPlayer().getLocalLocation().getY() > 6464) {
                    if (amascut.getLocation().getY() != 6592) {
                        amascutTeleport();
                    }
                }
                else if (client.getLocalPlayer().getLocalLocation().getY() < 6464) {
                    if (amascut.getLocation().getY() != 6336) {
                        amascutTeleport();
                    }
                }
                else if (amascutTeleportTimer > 0)
                    amascutTeleportTimer--;

                // P1 quotes
                if (phase == 1) {
                    if ((ticksSinceEncounterStart - 15) % 50 == 0 && antiSpam == 0) {
                        int x = random.nextInt(4);
                        while (x == lastPhrase)
                            x = random.nextInt(4);
                        lastPhrase = x;
                        switch (x) {
                            case 0:
                                amascutChatMessage("Bend the knee!");
                                soundEngine.playClip(Sound.AMASCUT_OBELISK_1, executor);
                                break;
                            case 1:
                                amascutChatMessage("Grovel!");
                                soundEngine.playClip(Sound.AMASCUT_OBELISK_2, executor);
                                break;
                            case 2:
                                amascutChatMessage("Pathetic.");
                                soundEngine.playClip(Sound.AMASCUT_OBELISK_3, executor);
                                break;
                            case 3:
                                amascutChatMessage("Weak.");
                                soundEngine.playClip(Sound.AMASCUT_OBELISK_4, executor);
                                break;
                        }
                        setAnimation(CustomAnimation.AMASCUT_CAST_1.getAnimationID());
                        amascutStatus = Status.AMASCUT_CAST.getStatus();
                        ticksSinceLastQuote = 0;
                        antiSpam = 20;
                    }
                }

                // P2 quotes
                if (phase == 2) {
                    if (ticksSinceEncounterStart % 50 == 0 && antiSpam == 0) {
                        int x = random.nextInt(6);
                        while (x == lastPhrase)
                            x = random.nextInt(6);
                        lastPhrase = x;
                        switch (x) {
                            case 0:
                                amascutChatMessage("Tear them apart!");
                                soundEngine.playClip(Sound.AMASCUT_P2_1, executor);
                                break;
                            case 1:
                                amascutChatMessage("Even with your strength combined, you cannot stop me!");
                                soundEngine.playClip(Sound.AMASCUT_P2_2, executor);
                                break;
                            case 2:
                                amascutChatMessage("Resourcefulness will not save you!");
                                soundEngine.playClip(Sound.AMASCUT_P2_3, executor);
                                break;
                            case 3:
                                amascutChatMessage("I am a god!");
                                soundEngine.playClip(Sound.AMASCUT_P2_4, executor);
                                break;
                            case 4:
                                amascutChatMessage("Death is the only peace!");
                                soundEngine.playClip(Sound.AMASCUT_P2_5, executor);
                                break;
                            case 5:
                                amascutChatMessage("Your companions will betray you... they always do.");
                                soundEngine.playClip(Sound.AMASCUT_P2_6, executor);
                                break;
                        }
                        ticksSinceLastQuote = 0;
                    }
                }

                // P2 special attack projectile hits player
                if (client.getTickCount() == projectileImpactTick && antiSpam == 0) {
                    switch (specialProjectile) {
                        case 2204:
                            amascutChatMessage("All strength withers!");
                            amascutStatus = Status.AMASCUT_CAST.getStatus();
                            soundEngine.playClip(Sound.AMASCUT_ARCANE_1, executor);
                            setAnimation(CustomAnimation.AMASCUT_CAST_2.getAnimationID());
                            break;
                        case 2206:
                            amascutChatMessage("I will not suffer this.");
                            amascutStatus = Status.AMASCUT_CAST.getStatus();
                            soundEngine.playClip(Sound.AMASCUT_ARCANE_2, executor);
                            setAnimation(CustomAnimation.AMASCUT_CAST_3.getAnimationID());
                            break;
                        case 2208:
                            amascutChatMessage("Your soul is WEAK!");
                            amascutStatus = Status.AMASCUT_CAST.getStatus();
                            soundEngine.playClip(Sound.AMASCUT_ARCANE_3, executor);
                            setAnimation(CustomAnimation.AMASCUT_CAST_4.getAnimationID());
                            break;
                    }
                    antiSpam = 10;
                }
            }
        }

        if (!isInWardensArena())
            resetWardens();
        else
            fakeNpcHandler.onGameTick(event);

        if (antiSpam > 0)
            antiSpam--;
    }

    public void onNpcChanged(NpcChanged event) {
        int npcID = event.getNpc().getId();

        if (amascut != null) {
            // Warden P2 initiated
            if (phase == 1 && 11753 <= npcID && npcID <= 11757) {
                boss = event.getNpc();
                phase = 2;
                ticksSinceEncounterStart = 0;
                ticksSinceLastQuote = 0;
            }
        }
    }

    public void onNpcSpawned(NpcSpawned event) {
        int npcID = event.getNpc().getId();

        // Warden P3 initiated
        if (npcID == 11761 || npcID == 11762) {
            boss = event.getNpc();
            phase = 3;
            ticksSinceEncounterStart = 0;
            ticksSinceLastQuote = 0;

            amascut = fakeNpcHandler.createFakeNpc(NpcID.AMASCUT_11696, CustomAnimation.AMASCUT_IDLE_2.getAnimationID(),
                    boss.getLocalLocation().getX()+384, boss.getLocalLocation().getY());
            fakeNpcHandler.getOverlay().setMaxTextHeightOffset(680);
            fakeNpcHandler.getOverlay().setMinTextHeightOffset(620);
            amascut.setOrientation(1024);
            amascutStatus = Status.AMASCUT_IDLE.getStatus();
            amascutMonologue = 8;
            messageDelay = client.getTickCount() + 25;

            switch (random.nextInt(6)) {
                case 0:
                    amascutChatMessage("Watch as this vision becomes reality!");
                    soundEngine.playClip(Sound.AMASCUT_P3_START_1, executor);
                    break;
                case 1:
                    amascutChatMessage("Behold! The fate of this world!");
                    soundEngine.playClip(Sound.AMASCUT_P3_START_2, executor);
                    break;
                case 2:
                    amascutChatMessage("Prove that you have the will to stand against the vision of a GODDESS!");
                    soundEngine.playClip(Sound.AMASCUT_P3_START_3, executor);
                    break;
                case 3:
                    amascutChatMessage("I will tear this world asunder!");
                    soundEngine.playClip(Sound.AMASCUT_P3_START_4, executor);
                    break;
                case 4:
                    amascutChatMessage("Let us see if these delusions of grandeur can withstand The Devourer's maw!");
                    soundEngine.playClip(Sound.AMASCUT_P3_START_5, executor);
                    break;
                case 5:
                    amascutChatMessage("Behold the inevitable future!");
                    soundEngine.playClip(Sound.AMASCUT_P3_START_6, executor);
                    break;
            }
        }
    }

    public void onAnimationChanged(AnimationChanged event) {
        if (boss != null && event.getActor() == boss) {
            if (amascut != null) {
                // Warden transition from P2 to P3
                if (boss.getAnimation() == 9662) {
                    switch (random.nextInt(4)) {
                        case 0:
                            amascutChatMessage("I WILL NOT BE DENIED.");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_1, executor);
                            break;
                        case 1:
                            amascutChatMessage("I WILL NOT BE SUBJUGATED BY A MORTAL!");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_2, executor);
                            break;
                        case 2:
                            amascutChatMessage("It is NOT over!");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_3, executor);
                            break;
                        case 3:
                            amascutChatMessage("ENOUGH!");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_4, executor);
                            break;
                    }
                    amascutStatus = Status.AMASCUT_CAST.getStatus();
                    setAnimation(CustomAnimation.AMASCUT_CAST_9.getAnimationID());
                    amascutMonologue = 7;
                    messageDelay = client.getTickCount() + 5;
                    antiSpam = 20;
                }

                // Warden P3 skulls failed
                if (boss.getAnimation() == 9681) {
                    amascutChatMessage("DESTRUCTION!");
                    switch (random.nextInt(3)) {
                        case 0:
                            soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_18, executor);
                            break;
                        case 1:
                            soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_19, executor);
                            break;
                        case 2:
                            soundEngine.playClip(Sound.AMASCUT_P3_MONOLOGUE_20, executor);
                            break;
                    }
                    amascutStatus = Status.AMASCUT_CAST.getStatus();
                    setAnimation(CustomAnimation.AMASCUT_CAST_8.getAnimationID());
                }
            }
        }
    }

    public void onActorDeath(ActorDeath actorDeath)
    {
        Actor actor = actorDeath.getActor();
        if (actor instanceof Player)
        {
            if (isInWardensArena() && amascut != null) {
                switch(random.nextInt(2)) {
                    case 0:
                        amascutChatMessage("Unworthy!");
                        soundEngine.playClip(Sound.AMASCUT_DEFEAT_1, executor);
                        break;
                    case 1:
                        amascutChatMessage("There is no place for you in my world.");
                        soundEngine.playClip(Sound.AMASCUT_DEFEAT_2, executor);
                        break;
                }
            }
        }
    }

    public void onProjectileMoved(ProjectileMoved event) {
        int projectileID = event.getProjectile().getId();
        if (projectileID == 2204 || projectileID == 2206 || projectileID == 2208) {
            projectileImpactTick = client.getTickCount() + event.getProjectile().getRemainingCycles() / 30;
            specialProjectile = projectileID;
        }
    }

    public void amascutChatMessage(String message) {
        if (amascut != null) {
            fakeNpcHandler.addOverheadText(amascut, "Amascut", message);
        }
    }

    public void setAnimation(int animationID) {
        if (amascut.getAnimationController() != null)
            amascut.getAnimationController().setAnimation(client.loadAnimation(animationID));
    }

    public void resetWardens() {
        amascutStatus = Status.INACTIVE.getStatus();
        antiSpam = 0;
        amascutMonologue = 0;
        turnTimer = 0;
        phase = 0;
        messageDelay = -1;
        tumekenP3 = true;
        if (amascut != null)
            amascut.setActive(false);
        amascut = null;
        lastPhrase = -1;
        ticksSinceEncounterStart = -1;
        ticksSinceLastQuote = -1;
        specialProjectile = 0;
        if (isInWardensArena())
            fakeNpcHandler.getOverlay().setOverheadText("");
    }
}
