package com.rs3voiceovers;

import com.animation.CustomAnimation;
import com.animation.Status;
import com.sound.SoundEngine;
import com.sound.SoundFileManager;
import com.sound.Sound;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.AnimationChanged;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
@PluginDescriptor(
        name = "RS3 Voiceovers",
        description = "Adds RS3 voiceovers to certain encounters (Inferno, Vorkath, Wardens)",
        tags = {"rs3", "runescape 3", "voice", "inferno", "zuk", "tzkal", "vorkath", "zemouregal", "amascut", "wardens"}
)

public class RS3VoiceoversPlugin extends Plugin {
    private static final int INFERNO_REGION_ID = 9043;
    private static final int UNGAEL_REGION_ID = 9023;
    private static final int WARDENS_ARENA_1 = 15184;
    private static final int WARDENS_ARENA_2 = 15696;
    private static final Pattern TZHAAR_WAVE_MESSAGE = Pattern.compile("Wave: (\\d+)");

    @Getter @Setter
    private NPC boss;
    @Getter @Setter
    private RuneLiteObject fakeNpc;
    @Getter @Setter
    private int fakeNpcStatus = Status.INACTIVE.getStatus();
    @Getter @Setter
    private LocalPoint fakeNpcLocation;
    @Getter @Setter

    private int messageDelay = -1;
    private int overheadTextTimer = 0;
    private int antiSpam = 0;

    private boolean zukStart = false;
    private boolean zukFight = false;
    private boolean jadHealers = false;
    private boolean zukHealers = false;
    private int lastPhrase = -1;

    private int zemoShutUpTick = -1;
    private boolean zemoFaceVorkath = false;
    private int zemoTurnTimer = 0;

    private int amascutMonologue = 0;
    private int phase = 0;
    private boolean tumekenP2 = false;
    private int skulls = 0;


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
        log.debug("RS3 Voiceovers started!");
        overlay = new RS3VoiceoversOverlay(client);
        overlayManager.add(overlay);

        executor.submit(() -> SoundFileManager.prepareSoundFiles(okHttpClient));
    }

    @Override
    protected void shutDown() throws Exception {
        log.debug("RS3 Voiceovers stopped!");
        overlayManager.remove(overlay);
        overlay = null;
    }

    private boolean isInInferno() {
        return client.getMapRegions() != null && ArrayUtils.contains(client.getMapRegions(), INFERNO_REGION_ID);
    }

    private boolean isInUngael() {
        return client.getMapRegions() != null && ArrayUtils.contains(client.getMapRegions(), UNGAEL_REGION_ID);
    }

    private boolean isInWardensArena() {
        return client.getMapRegions() != null && (ArrayUtils.contains(client.getMapRegions(), WARDENS_ARENA_1) ||
                                                  ArrayUtils.contains(client.getMapRegions(), WARDENS_ARENA_2));
    }

    private boolean isInBossArena() {
        return isInInferno() || isInUngael() || isInWardensArena();
    }

    private boolean isOnFinalAnimationFrame(RuneLiteObject runeLiteObject) {
        if (runeLiteObject.getAnimationController().getAnimation() != null)
            return (runeLiteObject.getAnimationController().getAnimation().getNumFrames() - 1) == runeLiteObject.getAnimationController().getFrame();

        return false;
    }

    public void addOverheadText(RuneLiteObject runeLiteObject, String name, String overheadText) {
        String chatMessage = new ChatMessageBuilder()
                .append(ChatColorType.NORMAL)
                .append(overheadText)
                .build();
        overheadTextTimer = 5;
        if (overlay != null)
            overlay.setOverheadText(overheadText);
        chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.PUBLICCHAT)
                .name(name)
                .runeLiteFormattedMessage(chatMessage)
                .timestamp((int) (System.currentTimeMillis() / 1000))
                .build());
    }

    public void addOverheadText(RuneLiteObject runeLiteObject, String name, String overheadText, int ticksToRemoveOverheadText, boolean createChatMessage) {
        String chatMessage = new ChatMessageBuilder()
                .append(ChatColorType.NORMAL)
                .append(overheadText)
                .build();
        overheadTextTimer = ticksToRemoveOverheadText;
        if (overlay != null)
            overlay.setOverheadText(overheadText);
        if (createChatMessage) {
            chatMessageManager.queue(QueuedMessage.builder()
                    .type(ChatMessageType.PUBLICCHAT)
                    .name(name)
                    .runeLiteFormattedMessage(chatMessage)
                    .timestamp((int) (System.currentTimeMillis() / 1000))
                    .build());
        }
    }

    // I copied this from Zoinkwiz's questhelper plugin I hope that's fine
    private static Model createFakeNpcModel(Client client, int NpcID) {
        NPCComposition npc = client.getNpcDefinition(NpcID);
        int[] models = npc.getModels();
        short[] coloursToReplace = npc.getColorToReplace();
        short[] coloursToReplaceWith = npc.getColorToReplaceWith();
        ModelData mdf = createModel(client, models);

        if (coloursToReplace != null && coloursToReplaceWith != null && coloursToReplace.length == coloursToReplaceWith.length) {
            for (int i=0; i < coloursToReplace.length; i++)
            {
                mdf.recolor(coloursToReplace[i], coloursToReplaceWith[i]);
            }
        }
        return mdf.cloneColors()
                .light();
    }

    private static ModelData createModel(Client client, ModelData... data)
    {
        return client.mergeModels(data);
    }

    private static ModelData createModel(Client client, int... data)
    {
        ModelData[] modelData = new ModelData[data.length];
        for (int i = 0; i < data.length; i++)
        {
            modelData[i] = client.loadModelData(data[i]);
        }
        return client.mergeModels(modelData);
    }

    private void createFakeNpc(int modelID, int animationID, int x, int y) {
        fakeNpc = new RuneLiteObject(client);
        fakeNpc.setModel(createFakeNpcModel(client, modelID));
        fakeNpcLocation = new LocalPoint(x, y, client.getLocalPlayer().getWorldView());
        fakeNpc.setLocation(fakeNpcLocation, client.getLocalPlayer().getWorldView().getPlane());
        AnimationController animations = new AnimationController(client, animationID);
        fakeNpc.setAnimationController(animations);
        overlay.setRuneLiteObject(fakeNpc);
        fakeNpc.setActive(true);
    }

    private void facePlayer(RuneLiteObject runeLiteObject) {
        if (runeLiteObject == null)
            return;

        int xDiff = runeLiteObject.getLocation().getX() - client.getLocalPlayer().getLocalLocation().getX();
        int yDiff = runeLiteObject.getLocation().getY() - client.getLocalPlayer().getLocalLocation().getY();

        double angle = Math.toDegrees(Math.atan2(xDiff, yDiff));
        if (angle < 0)
            angle += 360;

        int trueAngle = (int)(angle * 5.688888888888888);

        int rotation;
        int delta = trueAngle - runeLiteObject.getOrientation();
        if ((0 < delta && delta < 1024) || (-1024 > delta && delta > -2048)) {
            rotation = Math.min(20, Math.abs(delta));
        }
        else {
            rotation = Math.max(-20, delta);
        }

        int newOrientation = runeLiteObject.getOrientation() + rotation;
        if (newOrientation < 0)
            newOrientation += 2048;
        else if (newOrientation >= 2048)
            newOrientation -= 2048;

        runeLiteObject.setOrientation(newOrientation);
    }

    private void faceDirection(RuneLiteObject runeLiteObject, int direction) {
        if (runeLiteObject == null)
            return;

        int rotation;
        int delta = direction - runeLiteObject.getOrientation();
        if ((0 < delta && delta < 1024) || (-1024 > delta && delta > -2048)) {
            rotation = Math.min(20, Math.abs(delta));
        }
        else {
            rotation = Math.max(-20, delta);
        }

        int newOrientation = runeLiteObject.getOrientation() + rotation;
        if (newOrientation < 0)
            newOrientation += 2048;
        else if (newOrientation >= 2048)
            newOrientation -= 2048;

        runeLiteObject.setOrientation(newOrientation);
    }

    private void zemoSpellcast() {
        if (fakeNpc != null && boss != null && !boss.isDead()) {
            fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.ZEMOUREGAL_CAST_2.getAnimationID()));
            fakeNpcStatus = Status.ZEMOUREGAL_SPELL.getStatus();
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event){
        final String message = event.getMessage();

        if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
            return;

        // Inferno start of wave quotes
        if (isInInferno() && config.zuk()) {
            Matcher matcher = TZHAAR_WAVE_MESSAGE.matcher(message);
            if (matcher.find()) {
                int wave = Integer.parseInt(matcher.group(1));
                Random random = new Random();
                switch(wave) {
                    case 1:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "A challenger approaches...", null);
                        soundEngine.playClip(Sound.ZUK_FIRST_WAVE_1, executor);
                        messageDelay = client.getTickCount() + 6;
                        zukStart = true;
                        break;
                    case 4:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Die, and be reborn in flame!", null);
                        soundEngine.playClip(Sound.ZUK_BLOB, executor);
                        break;
                    case 9:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Rise, warrior of flame!", null);
                        soundEngine.playClip(Sound.ZUK_MELEE, executor);
                        break;
                    case 18:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Rise, ashen ranger!", null);
                        soundEngine.playClip(Sound.ZUK_RANGER, executor);
                        break;
                    case 35:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Rise, mage of embers!", null);
                        soundEngine.playClip(Sound.ZUK_MAGE, executor);
                        break;
                    case 67:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Rise, Jad!", null);
                        soundEngine.playClip(Sound.ZUK_SINGLE_JAD, executor);
                        break;
                    case 68:
                        switch(random.nextInt(2)) {
                            case 0:
                                client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Enough, Jad will end this!", null);
                                soundEngine.playClip(Sound.ZUK_TRIPLE_JAD_1, executor);
                                break;
                            case 1:
                                client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "This has gone on too long - kill them!", null);
                                soundEngine.playClip(Sound.ZUK_TRIPLE_JAD_2, executor);
                                break;
                        }
                        break;
                    case 69:
                        zukFight = true;
                        zukHealers = true;
                        break;
                    default:
                        int x = random.nextInt(7);
                        while (x == lastPhrase)
                            x = random.nextInt(7);
                        lastPhrase = x;
                        switch(x) {
                            case 0:
                                client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Show me you are worthy!", null);
                                soundEngine.playClip(Sound.ZUK_WAVE_DEFAULT_1, executor);
                                break;
                            case 1:
                                client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Fight, worm! Or crawl and die!", null);
                                soundEngine.playClip(Sound.ZUK_WAVE_DEFAULT_2, executor);
                                break;
                            case 2:
                                client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "To the slaughter!", null);
                                soundEngine.playClip(Sound.ZUK_WAVE_DEFAULT_3, executor);
                                break;
                            case 3:
                                client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Prove your worth!", null);
                                soundEngine.playClip(Sound.ZUK_WAVE_DEFAULT_4, executor);
                                break;
                            case 4:
                                client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Charge!", null);
                                soundEngine.playClip(Sound.ZUK_WAVE_DEFAULT_5, executor);
                                break;
                            case 5:
                                client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Into the fray!", null);
                                soundEngine.playClip(Sound.ZUK_WAVE_DEFAULT_6, executor);
                                break;
                            case 6:
                                client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "The unworthy will burn.", null);
                                soundEngine.playClip(Sound.ZUK_WAVE_DEFAULT_7, executor);
                                break;
                        }
                }
            }
        }

        if (isInWardensArena() && config.amascut()) {
            if (message.equals("Challenge started: The Wardens.")) {
                resetWardens();

                createFakeNpc(NpcID.AMASCUT_11696, CustomAnimation.AMASCUT_SPAWN.getAnimationID(), 6208, 6592);
                overlay.setMaxTextHeightOffset(380);
                overlay.setMinTextHeightOffset(320);
                fakeNpc.setOrientation(1024);
                fakeNpcStatus = Status.AMASCUT_START.getStatus();

                addOverheadText(fakeNpc, "Amascut", "Destruction. Annihilation. My vision for this world.", 10, true);
                soundEngine.playClip(Sound.AMASCUT_START_1, executor);
                amascutMonologue = 1;
                messageDelay = client.getTickCount() + 10;
                antiSpam = 45;
                phase = 1;
            }

            if (message.equals("<col=ff8e32>A large ball of energy is shot your way...</col>") && fakeNpc != null && antiSpam == 0) {
                //<col=3366ff> <col=ff8e32>
                addOverheadText(fakeNpc, "Amascut", "Bend the knee!");
                fakeNpcStatus = Status.AMASCUT_CAST.getStatus();
                soundEngine.playClip(Sound.AMASCUT_OBELISK_1, executor);
                fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_CAST_1.getAnimationID()));
                antiSpam = 40;
            }

            if (message.equals("<col=ff3045>The warden throws an arcane scimitar.</col>") && fakeNpc != null) {
                addOverheadText(fakeNpc, "Amascut", "All strength withers!");
                fakeNpcStatus = Status.AMASCUT_CAST.getStatus();
                soundEngine.playClip(Sound.AMASCUT_ARCANE_1, executor);
                fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_CAST_2.getAnimationID()));
            }

            if (message.equals("<col=229628>The warden fires an arcane arrow.</col>") && fakeNpc != null) {
                addOverheadText(fakeNpc, "Amascut", "I will not suffer this.");
                fakeNpcStatus = Status.AMASCUT_CAST.getStatus();
                soundEngine.playClip(Sound.AMASCUT_ARCANE_2, executor);
                fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_CAST_3.getAnimationID()));
            }

            if (message.equals("<col=a53fff>The warden launches an arcane spell.</col>") && fakeNpc != null) {
                addOverheadText(fakeNpc, "Amascut", "Your soul is WEAK!");
                fakeNpcStatus = Status.AMASCUT_CAST.getStatus();
                soundEngine.playClip(Sound.AMASCUT_ARCANE_3, executor);
                fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_CAST_4.getAnimationID()));
            }

            Matcher matcher = Pattern.compile("Challenge complete: The Wardens.(.+?)").matcher(message);
            if (matcher.find() && fakeNpc != null) {
                Random random = new Random();
                switch (random.nextInt(2)) {
                    case 0:
                        addOverheadText(fakeNpc, "Amascut", "Impossible...");
                        soundEngine.playClip(Sound.AMASCUT_VICTORY_1, executor);
                        break;
                    case 1:
                        addOverheadText(fakeNpc, "Amascut", "This cannot be the end...");
                        soundEngine.playClip(Sound.AMASCUT_VICTORY_2, executor);
                        break;
                }
                amascutMonologue = 0;
                fakeNpcStatus = Status.AMASCUT_END.getStatus();
                fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_DESPAWN.getAnimationID()));
            }

            if (message.equals("Your party failed to complete the challenge. You may try again..."))
                resetWardens();
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        // Zemouregal animations
        if (isInUngael() && fakeNpc != null) {
            int zemoOldOrientation = fakeNpc.getOrientation();

            if (zemoFaceVorkath)
                fakeNpc.setOrientation(1536);
            else
                facePlayer(fakeNpc);

            if (fakeNpcStatus == Status.ZEMOUREGAL_START.getStatus() && isOnFinalAnimationFrame(fakeNpc)) {
                fakeNpcStatus = Status.ZEMOUREGAL_CAST.getStatus();
                fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.ZEMOUREGAL_CAST_1.getAnimationID()));
            }

            if (fakeNpcStatus == Status.ZEMOUREGAL_CAST.getStatus() && isOnFinalAnimationFrame(fakeNpc)) {
                zemoFaceVorkath = false;
                fakeNpcStatus = Status.ZEMOUREGAL_IDLE.getStatus();
                fakeNpc.setAnimation(client.loadAnimation(CustomAnimation.ZEMOUREGAL_IDLE.getAnimationID()));
            }

            if (fakeNpcStatus == Status.ZEMOUREGAL_SPELL.getStatus() && isOnFinalAnimationFrame(fakeNpc)) {
                fakeNpcStatus = Status.ZEMOUREGAL_IDLE.getStatus();
                fakeNpc.setAnimation(client.loadAnimation(CustomAnimation.ZEMOUREGAL_IDLE.getAnimationID()));
            }

            if (zemoTurnTimer > 0)
                zemoTurnTimer--;

            if (fakeNpcStatus == Status.ZEMOUREGAL_IDLE.getStatus() && zemoTurnTimer == 0 && Math.abs(fakeNpc.getOrientation() - zemoOldOrientation) >= 5) {
                fakeNpcStatus = Status.ZEMOUREGAL_TURN.getStatus();
                zemoTurnTimer = 20;
                fakeNpc.setAnimation(client.loadAnimation(CustomAnimation.ZEMOUREGAL_WALK_1.getAnimationID()));
            }

            if (fakeNpcStatus == Status.ZEMOUREGAL_TURN.getStatus() && zemoTurnTimer == 0 && fakeNpc.getOrientation() == zemoOldOrientation) {
                fakeNpcStatus = Status.ZEMOUREGAL_IDLE.getStatus();
                zemoTurnTimer = 20;
                fakeNpc.setAnimation(client.loadAnimation(CustomAnimation.ZEMOUREGAL_IDLE.getAnimationID()));
            }

            if (fakeNpcStatus == Status.ZEMOUREGAL_END.getStatus()) {
                fakeNpcStatus = Status.ZEMOUREGAL_DESPAWN.getStatus();
                fakeNpc.setAnimation(client.loadAnimation(CustomAnimation.ZEMOUREGAL_DESPAWN.getAnimationID()));
            }

            if (fakeNpcStatus == Status.ZEMOUREGAL_DESPAWN.getStatus() && isOnFinalAnimationFrame(fakeNpc)) {
                resetVorkath();
            }
        }

        // Amascut animations
        if (isInWardensArena() && fakeNpc != null) {
            if (fakeNpcStatus == Status.AMASCUT_SUMMON_PHANTOM.getStatus()) {
                if (skulls == 2)
                    faceDirection(fakeNpc, 1280);
                if (skulls == 3)
                    faceDirection(fakeNpc, 640);
            }
            else if (phase == 3){
                faceDirection(fakeNpc, 1024);
            }

            if ((fakeNpcStatus == Status.AMASCUT_START.getStatus()           ||
                 fakeNpcStatus == Status.AMASCUT_CAST.getStatus()            ||
                 fakeNpcStatus == Status.AMASCUT_TELEPORT_IN.getStatus())    && isOnFinalAnimationFrame(fakeNpc)) {

                fakeNpcStatus = Status.AMASCUT_IDLE.getStatus();

                if (phase == 3)
                    fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_IDLE_2.getAnimationID()));
                else
                    fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_IDLE_1.getAnimationID()));
            }

            if (fakeNpcStatus == Status.AMASCUT_TELEPORT_OUT.getStatus() && isOnFinalAnimationFrame(fakeNpc)) {
                fakeNpcStatus = Status.AMASCUT_TELEPORT_IN.getStatus();
                fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_TELEPORT_IN.getAnimationID()));
                if (tumekenP2) {
                    fakeNpcLocation = new LocalPoint(7744, 6848, client.getLocalPlayer().getWorldView());
                    fakeNpc.setOrientation(512);
                }
                else {
                    fakeNpcLocation = new LocalPoint(4672,6838, client.getLocalPlayer().getWorldView());
                    fakeNpc.setOrientation(1536);
                }
                fakeNpc.setLocation(fakeNpcLocation, client.getLocalPlayer().getWorldArea().getPlane());
            }

            if (fakeNpcStatus == Status.AMASCUT_INTO_DOWN.getStatus() && isOnFinalAnimationFrame(fakeNpc)) {
                fakeNpcStatus = Status.AMASCUT_DOWN.getStatus();
                fakeNpc.setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_DOWN.getAnimationID()));
            }

            if (fakeNpcStatus == Status.AMASCUT_SUMMON_SKULLS.getStatus() && isOnFinalAnimationFrame(fakeNpc)) {
                skulls++;
                if (skulls == 2 || skulls == 3) {
                    fakeNpcStatus = Status.AMASCUT_SUMMON_PHANTOM.getStatus();
                    Random random = new Random();
                    switch(random.nextInt(3)) {
                        case 0:
                            fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_CAST_6.getAnimationID()));
                            break;
                        case 1:
                            fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_CAST_7.getAnimationID()));
                            break;
                        case 2:
                            fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_CAST_10.getAnimationID()));
                            break;
                    }
                }
                else {
                    fakeNpcStatus = Status.AMASCUT_CHANNEL.getStatus();
                    fakeNpc.setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_CHANNEL.getAnimationID()));
                }
            }

            if (fakeNpcStatus == Status.AMASCUT_SUMMON_PHANTOM.getStatus() && isOnFinalAnimationFrame(fakeNpc)) {
                fakeNpcStatus = Status.AMASCUT_CHANNEL.getStatus();
                fakeNpc.setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_CHANNEL.getAnimationID()));
            }

            if (fakeNpcStatus == Status.AMASCUT_CHANNEL.getStatus() && fakeNpc.getAnimationController().getFrame() == 24) {
                fakeNpc.getAnimationController().setFrame(9);
            }

            if (fakeNpcStatus == Status.AMASCUT_ENRAGE_1.getStatus() && isOnFinalAnimationFrame(fakeNpc)) {
                fakeNpcStatus = Status.AMASCUT_ENRAGE_2.getStatus();
                fakeNpc.setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_ENRAGE_2.getAnimationID()));
            }

            if (fakeNpcStatus == Status.AMASCUT_ENRAGE_2.getStatus() && isOnFinalAnimationFrame(fakeNpc)) {
                fakeNpcStatus = Status.AMASCUT_ENRAGE_3.getStatus();
                fakeNpc.setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_ENRAGE_3.getAnimationID()));
            }

            if (fakeNpcStatus == Status.AMASCUT_ENRAGE_3.getStatus() && isOnFinalAnimationFrame(fakeNpc)) {
                fakeNpc.getAnimationController().setFrame(16);
            }

            if (fakeNpcStatus == Status.AMASCUT_END.getStatus() && isOnFinalAnimationFrame(fakeNpc)) {
                resetWardens();
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        // Reset variables if no longer in boss arena
        if (!isInBossArena())
            fullReset();

        if (!config.zuk())
            resetInferno();

        if (!config.zemouregal())
            resetVorkath();

        if (!config.amascut())
            resetWardens();

        // Delayed messages
        if (zukStart && client.getTickCount() == messageDelay){
            client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "What makes you think you have what it takes?", null);
            soundEngine.playClip(Sound.ZUK_FIRST_WAVE_2, executor);
            zukStart = false;
        }

        if (amascutMonologue > 0 && client.getTickCount() == messageDelay){
            Random random = new Random();
            switch(amascutMonologue) {
                case 1:
                    addOverheadText(fakeNpc, "Amascut","How close it came to becoming a reality.", 8, true);
                    amascutMonologue = 2;
                    messageDelay = client.getTickCount() + 8;
                    soundEngine.playClip(Sound.AMASCUT_START_2, executor);
                    break;
                case 2:
                    addOverheadText(fakeNpc,"Amascut", "Picture the heroes who would have risen against me.");
                    amascutMonologue = 3;
                    messageDelay = client.getTickCount() + 5;
                    soundEngine.playClip(Sound.AMASCUT_START_3, executor);
                    break;
                case 3:
                    addOverheadText(fakeNpc,"Amascut", "The challenges would they have overcome.");
                    amascutMonologue = 4;
                    messageDelay = client.getTickCount() + 4;
                    soundEngine.playClip(Sound.AMASCUT_START_4, executor);
                    break;
                case 4:
                    addOverheadText(fakeNpc, "Amascut","And the rewards would they have reaped.");
                    amascutMonologue = 5;
                    messageDelay = client.getTickCount() + 5;
                    soundEngine.playClip(Sound.AMASCUT_START_5, executor);
                    break;
                case 5:
                    addOverheadText(fakeNpc,"Amascut", "Who among you is worthy of facing a goddess?", 7, true);
                    amascutMonologue = 6;
                    messageDelay = client.getTickCount() + 7;
                    soundEngine.playClip(Sound.AMASCUT_START_6, executor);
                    break;
                case 6:
                    addOverheadText(fakeNpc,"Amascut", "Show me...");
                    amascutMonologue = 0;
                    messageDelay = -1;
                    soundEngine.playClip(Sound.AMASCUT_START_7, executor);
                    break;
                case 7:
                    switch(random.nextInt(4)) {
                        case 0:
                            addOverheadText(fakeNpc,"Amascut", "FALL TO THE SHADOW!");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_3, executor);
                            break;
                        case 1:
                            addOverheadText(fakeNpc,"Amascut", "YOU ARE NOTHING!");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_4, executor);
                            break;
                        case 2:
                            addOverheadText(fakeNpc,"Amascut", "MWAHAHAHA!");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_5, executor);
                            break;
                        case 3:
                            addOverheadText(fakeNpc,"Amascut", "BEHOLD! THE FATE OF THIS WORLD!");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_6, executor);
                            break;
                    }
                    fakeNpcStatus = Status.AMASCUT_CAST.getStatus();
                    fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_CAST_5.getAnimationID()));
                    amascutMonologue = 0;
                    messageDelay = -1;
                    break;
                case 8:
                    switch(random.nextInt(2)) {
                        case 0:
                            addOverheadText(fakeNpc,"Amascut", "Bring forth Crondis!");
                            soundEngine.playClip(Sound.AMASCUT_ZEBAK_1, executor);
                            break;
                        case 1:
                            addOverheadText(fakeNpc,"Amascut", "Crondis... It should have never come to this.");
                            soundEngine.playClip(Sound.AMASCUT_ZEBAK_2, executor);
                            break;
                    }
                    amascutMonologue = 0;
                    messageDelay = -1;
                    break;
                case 9:
                    switch(random.nextInt(2)) {
                        case 0:
                            addOverheadText(fakeNpc,"Amascut", "I am so sorry, Apmeken.");
                            soundEngine.playClip(Sound.AMASCUT_BABA_1, executor);
                            break;
                        case 1:
                            addOverheadText(fakeNpc,"Amascut", "I am sorry, Apmeken.");
                            soundEngine.playClip(Sound.AMASCUT_BABA_2, executor);
                            break;
                    }
                    amascutMonologue = 0;
                    messageDelay = -1;
                    break;
                case 10:
                    switch(random.nextInt(2)) {
                        case 0:
                            addOverheadText(fakeNpc,"Amascut", "Het...bear witness!");
                            soundEngine.playClip(Sound.AMASCUT_AKKHA_1, executor);
                            break;
                        case 1:
                            addOverheadText(fakeNpc,"Amascut", "Forgive me, Het.");
                            soundEngine.playClip(Sound.AMASCUT_AKKHA_2, executor);
                            break;
                    }
                    amascutMonologue = 0;
                    messageDelay = -1;
                    break;
                case 11:
                    switch(random.nextInt(2)) {
                        case 0:
                            addOverheadText(fakeNpc,"Amascut", "Scabaras!");
                            soundEngine.playClip(Sound.AMASCUT_KEPHRI_1, executor);
                            break;
                        case 1:
                            addOverheadText(fakeNpc,"Amascut", "Scabaras...");
                            soundEngine.playClip(Sound.AMASCUT_KEPHRI_2, executor);
                            break;
                    }
                    amascutMonologue = 0;
                    messageDelay = -1;
                    break;
                case 12:
                    addOverheadText(fakeNpc,"Amascut", "THE GOD");
                    amascutMonologue = 13;
                    messageDelay = client.getTickCount() + 15;
                    soundEngine.playClip(Sound.AMASCUT_ENRAGE_5, executor);
                    break;
                case 13:
                    addOverheadText(fakeNpc,"Amascut", "OF");
                    amascutMonologue = 14;
                    messageDelay = client.getTickCount() + 15;
                    soundEngine.playClip(Sound.AMASCUT_ENRAGE_6, executor);
                    break;
                case 14:
                    addOverheadText(fakeNpc,"Amascut", "DESTRUCTION!");
                    soundEngine.playClip(Sound.AMASCUT_ENRAGE_7, executor);
                    amascutMonologue = 0;
                    messageDelay = -1;
                    break;
                default:
                    break;
            }
        }

        if (antiSpam > 0)
            antiSpam--;

        if (overheadTextTimer > 0)
            overheadTextTimer--;
        else if (overlay != null)
            overlay.setOverheadText("");

        if (client.getTickCount() == zemoShutUpTick)
            antiSpam = 12;
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event) {
        if (event.getNpc() == null)
            return;

        int npcID = event.getNpc().getId();

        if (config.zemouregal()) {
            // Vorkath combat begins
            if (event.getOld().getId() == 8058 && npcID == 8061) {
                boss = event.getNpc();
                zemoShutUpTick = client.getTickCount() + 28;
            }
            // Vorkath initiated
            if (event.getOld().getId() == 8059 && npcID == 8058) {
                resetVorkath();
                createFakeNpc(NpcID.ZEMOUREGAL, CustomAnimation.ZEMOUREGAL_SPAWN.getAnimationID(), 5824, 8128);
                overlay.setMaxTextHeightOffset(380);
                overlay.setMinTextHeightOffset(320);
                fakeNpcStatus = Status.ZEMOUREGAL_START.getStatus();
                zemoFaceVorkath = true;

                Random random = new Random();
                switch (random.nextInt(3)) {
                    case 0:
                        addOverheadText(fakeNpc, "Zemouregal", "Obey your master, you dumb puppet!");
                        soundEngine.playClip(Sound.VORKATH_START_1, executor);
                        break;
                    case 1:
                        addOverheadText(fakeNpc, "Zemouregal", "The dragon WILL be mine!");
                        soundEngine.playClip(Sound.VORKATH_START_2, executor);
                        break;
                    case 2:
                        addOverheadText(fakeNpc, "Zemouregal", "Rise you cursed weakling, I shan't tell you again!");
                        soundEngine.playClip(Sound.VORKATH_START_3, executor);
                        break;
                }
            }
        }

        if (config.amascut() && fakeNpc != null) {
            // Warden P2 initiated
            if (phase == 1 && 11753 <= npcID && npcID <= 11757) {
                boss = event.getNpc();
                addOverheadText(fakeNpc, "Amascut", "The mice fight back?");
                soundEngine.playClip(Sound.AMASCUT_P2_START, executor);
                phase = 2;
            }

            if (11753 <= npcID && npcID <= 11757 && npcID != 11755) {
                fakeNpcStatus = Status.AMASCUT_TELEPORT_OUT.getStatus();
                fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_TELEPORT_OUT.getAnimationID()));

                if (npcID == 11756 || npcID == 11757)
                    tumekenP2 = true;
            }
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc() == null)
            return;

        int npcID = event.getNpc().getId();

        if (config.zuk()) {
            // TzKal-Zuk combat initiated
            if (npcID == 7706) {
                Random random = new Random();
                boss = event.getNpc();
                switch (random.nextInt(3)) {
                    case 0:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Now - the true battle begins!", null);
                        boss.setOverheadCycle(225);
                        boss.setOverheadText("Now - the true battle begins!");
                        soundEngine.playClip(Sound.ZUK_START_1, executor);
                        break;
                    case 1:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Out of the way, weaklings!", null);
                        boss.setOverheadCycle(225);
                        boss.setOverheadText("Out of the way, weaklings!");
                        soundEngine.playClip(Sound.ZUK_START_2, executor);
                        break;
                    case 2:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "It appears you have exceeded my expectations. Perhaps you are worthy... to fall before my might!", null);
                        boss.setOverheadCycle(550);
                        boss.setOverheadText("It appears you have exceeded my expectations. Perhaps you are worthy... to fall before my might!");
                        soundEngine.playClip(Sound.ZUK_START_3, executor);
                        break;
                }
            }

            // Zuk set spawn
            if (zukFight && npcID == 7703) {
                Random random = new Random();
                boss.setOverheadCycle(225);
                switch (random.nextInt(2)) {
                    case 0:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "All of you, attack!", null);
                        boss.setOverheadText("All of you, attack!");
                        soundEngine.playClip(Sound.ZUK_SET_1, executor);
                        break;
                    case 1:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Rise, scions of the Kiln!", null);
                        boss.setOverheadText("Rise, scions of the Kiln!");
                        soundEngine.playClip(Sound.ZUK_SET_2, executor);
                        break;
                }
            }

            // Zuk Jad spawn
            if (zukFight && npcID == 7704) {
                client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Unleash Jad!", null);
                boss.setOverheadCycle(200);
                boss.setOverheadText("Unleash Jad!");
                soundEngine.playClip(Sound.ZUK_SUMMONED_JAD, executor);
                jadHealers = true;
            }

            // Zuk Jad's healers spawn
            if (jadHealers && npcID == 7705) {
                client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "HurKot. End this now!", null);
                boss.setOverheadCycle(225);
                boss.setOverheadText("HurKot. End this now!");
                soundEngine.playClip(Sound.ZUK_JAD_HEALERS, executor);
                jadHealers = false;
            }

            // Zuk's healers spawn
            if (zukHealers && npcID == 7708) {
                Random random = new Random();
                switch (random.nextInt(3)) {
                    case 0:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Drag them to the molten deep!", null);
                        boss.setOverheadCycle(250);
                        boss.setOverheadText("Drag them to the molten deep!");
                        soundEngine.playClip(Sound.ZUK_HEALERS_1, executor);
                        break;
                    case 1:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "I grow weary of this. End it quickly!", null);
                        boss.setOverheadCycle(300);
                        boss.setOverheadText("I grow weary of this. End it quickly!");
                        soundEngine.playClip(Sound.ZUK_HEALERS_2, executor);
                        break;
                    case 2:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Flames unending...", null);
                        boss.setOverheadCycle(250);
                        boss.setOverheadText("Flames unending...");
                        soundEngine.playClip(Sound.ZUK_HEALERS_3, executor);
                        break;
                }
                zukHealers = false;
            }
        }

        if (config.zemouregal() && fakeNpc != null) {
            // Vorkath zombified spawn
            if (npcID == 8063) {
                antiSpam = 12;
                Random random = new Random();
                switch (random.nextInt(3)) {
                    case 0:
                        addOverheadText(fakeNpc, "Zemouregal", "Your struggle, like your life, is meaningless.");
                        soundEngine.playClip(Sound.VORKATH_SPAWN_1, executor);
                        break;
                    case 1:
                        addOverheadText(fakeNpc, "Zemouregal", "No one can stand against my mighty undead!");
                        soundEngine.playClip(Sound.VORKATH_SPAWN_2, executor);
                        break;
                    case 2:
                        addOverheadText(fakeNpc, "Zemouregal", "No escape, you puny knave!");
                        soundEngine.playClip(Sound.VORKATH_SPAWN_3, executor);
                        break;
                }
            }
        }

        if (config.amascut()) {
            // Warden P3 initiated
            if (npcID == 11761 || npcID == 11762) {
                boss = event.getNpc();
                phase = 3;

                createFakeNpc(NpcID.AMASCUT_11696, CustomAnimation.AMASCUT_IDLE_2.getAnimationID(), 6592, 5440);
                overlay.setMaxTextHeightOffset(680);
                overlay.setMinTextHeightOffset(620);
                fakeNpc.setOrientation(1024);
                fakeNpcStatus = Status.AMASCUT_IDLE.getStatus();
            }

            // Zebak's Phantom
            if (npcID == 11774 && fakeNpc != null)
                amascutMonologue = 8;

            // Ba-ba's Phantom
            if (npcID == 11775 && fakeNpc != null)
                amascutMonologue = 9;

            // Akkha's Phantom
            if (npcID == 11777 && fakeNpc != null)
                amascutMonologue = 10;

            // Kephri's Phantom
            if (npcID == 11776 && fakeNpc != null)
                amascutMonologue = 11;
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (event.getActor() == null)
            return;

        if (boss != null && event.getActor() == boss) {
            if (config.zemouregal() && fakeNpc != null) {
                // Vorkath poison rapid fire spec
                if (boss.getAnimation() == 7957) {
                    zemoShutUpTick = client.getTickCount() + 56;
                    zemoSpellcast();
                    Random random = new Random();
                    switch (random.nextInt(4)) {
                        case 0:
                            addOverheadText(fakeNpc, "Zemouregal", "Suffocate this land with bile!");
                            soundEngine.playClip(Sound.VORKATH_POISON_1, executor);
                            break;
                        case 1:
                            addOverheadText(fakeNpc, "Zemouregal", "Spew your venom, my malformed pet!");
                            soundEngine.playClip(Sound.VORKATH_POISON_2, executor);
                            break;
                        case 2:
                            addOverheadText(fakeNpc, "Zemouregal", "Cut them down without mercy!");
                            soundEngine.playClip(Sound.VORKATH_POISON_3, executor);
                            break;
                        case 3:
                            addOverheadText(fakeNpc, "Zemouregal", "Drown in your own gore!");
                            soundEngine.playClip(Sound.VORKATH_POISON_4, executor);
                            break;
                    }
                }

                // Vorkath deadly dragonfire attack
                if (boss.getAnimation() == 7960) {
                    zemoSpellcast();
                    if (antiSpam == 0) {
                        Random random = new Random();
                        switch (random.nextInt(2)) {
                            case 0:
                                addOverheadText(fakeNpc, "Zemouregal", "DIE!");
                                soundEngine.playClip(Sound.VORKATH_FIREBOMB_1, executor);
                                break;
                            case 1:
                                addOverheadText(fakeNpc, "Zemouregal", "Say farewell, little one!");
                                soundEngine.playClip(Sound.VORKATH_FIREBOMB_2, executor);
                                break;
                        }
                        antiSpam = 12;
                    }
                }
            }

            if (config.amascut() && fakeNpc != null) {
                // Warden P2 down / P3 skulls destroyed
                if (boss.getAnimation() == 9670 || boss.getAnimation() == 9680) {
                    addOverheadText(fakeNpc, "Amascut", "EURGH");
                    Random random = new Random();
                    switch (random.nextInt(6)) {
                        case 0:
                            soundEngine.playClip(Sound.AMASCUT_HURT_1, executor);
                            break;
                        case 1:
                            soundEngine.playClip(Sound.AMASCUT_HURT_2, executor);
                            break;
                        case 2:
                            soundEngine.playClip(Sound.AMASCUT_HURT_3, executor);
                            break;
                        case 3:
                            soundEngine.playClip(Sound.AMASCUT_HURT_4, executor);
                            break;
                        case 4:
                            soundEngine.playClip(Sound.AMASCUT_HURT_5, executor);
                            break;
                        case 5:
                            soundEngine.playClip(Sound.AMASCUT_HURT_6, executor);
                            break;
                    }
                }

                // Warden P2 down
                if (boss.getAnimation() == 9670) {
                    fakeNpcStatus = Status.AMASCUT_INTO_DOWN.getStatus();
                    fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_DOWN_ENTER.getAnimationID()));
                }

                // Warden P3 skulls destroyed
                if (boss.getAnimation() == 9680) {
                    fakeNpcStatus = Status.AMASCUT_CAST.getStatus();
                    fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_HURT.getAnimationID()));
                }

                // Warden P2 end of down
                if (boss.getAnimation() == 9672) {
                    addOverheadText(fakeNpc, "Amascut", "NO!");
                    fakeNpcStatus = Status.AMASCUT_CAST.getStatus();
                    fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_DOWN_EXIT.getAnimationID()));
                    soundEngine.playClip(Sound.AMASCUT_RECOVER, executor);
                }

                // Warden P3 summon skulls
                if (boss.getAnimation() == 9682) {
                    addOverheadText(fakeNpc, "Amascut", "ENOUGH!");
                    Random random = new Random();
                    switch (random.nextInt(3)) {
                        case 0:
                            soundEngine.playClip(Sound.AMASCUT_SKULLS_1, executor);
                            break;
                        case 1:
                            soundEngine.playClip(Sound.AMASCUT_SKULLS_2, executor);
                            break;
                        case 2:
                            soundEngine.playClip(Sound.AMASCUT_SKULLS_3, executor);
                            break;
                    }
                    messageDelay = client.getTickCount() + 2;
                    fakeNpcStatus = Status.AMASCUT_SUMMON_SKULLS.getStatus();
                    fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_CAST_9.getAnimationID()));
                }

                // Warden transition from P2 to P3
                if (boss.getAnimation() == 9662) {
                    Random random = new Random();
                    switch (random.nextInt(2)) {
                        case 0:
                            addOverheadText(fakeNpc, "Amascut", "I WILL NOT BE DENIED.");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_1, executor);
                            break;
                        case 1:
                            addOverheadText(fakeNpc, "Amascut", "I WILL NOT BE SUBJUGATED BY A MORTAL!");
                            soundEngine.playClip(Sound.AMASCUT_P2_END_2, executor);
                            break;
                    }
                    fakeNpcStatus = Status.AMASCUT_CAST.getStatus();
                    fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_DOWN_EXIT.getAnimationID()));
                    amascutMonologue = 7;
                    messageDelay = client.getTickCount() + 5;
                }

                // Warden P3 skulls failed
                if (boss.getAnimation() == 9681) {
                    Random random = new Random();
                    addOverheadText(fakeNpc, "Amascut", "DESTRUCTION!");
                    switch (random.nextInt(3)) {
                        case 0:
                            soundEngine.playClip(Sound.AMASCUT_ENRAGE_7, executor);
                            break;
                        case 1:
                            soundEngine.playClip(Sound.AMASCUT_ENRAGE_8, executor);
                            break;
                        case 2:
                            soundEngine.playClip(Sound.AMASCUT_ENRAGE_9, executor);
                            break;
                    }
                    fakeNpcStatus = Status.AMASCUT_CAST.getStatus();
                    fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_CAST_8.getAnimationID()));
                }

                // Warden P3 enrage
                if (boss.getAnimation() == 9684) {
                    Random random = new Random();
                    switch (random.nextInt(4)) {
                        case 0:
                            addOverheadText(fakeNpc, "Amascut", "It is NOT over!");
                            soundEngine.playClip(Sound.AMASCUT_ENRAGE_1, executor);
                            break;
                        case 1:
                            addOverheadText(fakeNpc, "Amascut", "Prove that you have the will to stand against the vision of a GODDESS!");
                            soundEngine.playClip(Sound.AMASCUT_ENRAGE_2, executor);
                            break;
                        case 2:
                            addOverheadText(fakeNpc, "Amascut", "I WILL TEAR THIS WORLD ASUNDER!");
                            soundEngine.playClip(Sound.AMASCUT_ENRAGE_3, executor);
                            break;
                        case 3:
                            addOverheadText(fakeNpc, "Amascut", "I AM");
                            soundEngine.playClip(Sound.AMASCUT_ENRAGE_4, executor);
                            amascutMonologue = 12;
                            messageDelay = client.getTickCount() + 15;
                            break;
                    }
                    fakeNpcStatus = Status.AMASCUT_ENRAGE_1.getStatus();
                    fakeNpc.getAnimationController().setAnimation(client.loadAnimation(CustomAnimation.AMASCUT_ENRAGE_1.getAnimationID()));
                }
            }
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath actorDeath)
    {
        Actor actor = actorDeath.getActor();
        if (actor instanceof Player)
        {
            Player player = (Player) actor;
            if (isInInferno() && config.zuk() && player == client.getLocalPlayer())
            {
                Random random = new Random();
                switch(random.nextInt(3)) {
                    case 0:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Pathetic.", null);
                        if (zukFight && boss != null) {
                            boss.setOverheadCycle(250);
                            boss.setOverheadText("Pathetic.");
                        }
                        soundEngine.playClip(Sound.ZUK_DEFEAT_1, executor);
                        break;
                    case 1:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Worthless. Just like the rest.", null);
                        if (zukFight && boss != null) {
                            boss.setOverheadCycle(275);
                            boss.setOverheadText("Worthless. Just like the rest.");
                        }
                        soundEngine.playClip(Sound.ZUK_DEFEAT_2, executor);
                        break;
                    case 2:
                        client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "You have failed, as expected.", null);
                        if (zukFight && boss != null) {
                            boss.setOverheadCycle(275);
                            boss.setOverheadText("You have failed, as expected.");
                        }
                        soundEngine.playClip(Sound.ZUK_DEFEAT_3, executor);
                        break;
                }
            }

            if (isInUngael() && config.zemouregal() && fakeNpc != null && player == client.getLocalPlayer())
            {
                Random random = new Random();
                switch(random.nextInt(4)) {
                    case 0:
                        addOverheadText(fakeNpc, "Zemouregal", "Say hello to Death for me!");
                        soundEngine.playClip(Sound.VORKATH_DEFEAT_1, executor);
                        break;
                    case 1:
                        addOverheadText(fakeNpc, "Zemouregal", "Death is, quite frankly, too good for you.");
                        soundEngine.playClip(Sound.VORKATH_DEFEAT_2, executor);
                        break;
                    case 2:
                        addOverheadText(fakeNpc, "Zemouregal", "Finally, an end to your meddling.");
                        soundEngine.playClip(Sound.VORKATH_DEFEAT_3, executor);
                        break;
                    case 3:
                        addOverheadText(fakeNpc, "Zemouregal", "Death protects you? I meant to have you for my own...");
                        soundEngine.playClip(Sound.VORKATH_DEFEAT_4, executor);
                        break;
                }
            }

            if (isInWardensArena() && config.amascut() && fakeNpc != null)
            {
                Random random = new Random();
                switch(random.nextInt(5)) {
                    case 0:
                        addOverheadText(fakeNpc, "Amascut", "Unworthy!");
                        soundEngine.playClip(Sound.AMASCUT_DEFEAT_1, executor);
                        break;
                    case 1:
                        addOverheadText(fakeNpc, "Amascut", "Your souls are mine!");
                        soundEngine.playClip(Sound.AMASCUT_DEFEAT_2, executor);
                        break;
                    case 2:
                        addOverheadText(fakeNpc, "Amascut", "Pathetic.");
                        soundEngine.playClip(Sound.AMASCUT_DEFEAT_3, executor);
                        break;
                    case 3:
                        addOverheadText(fakeNpc, "Amascut", "Weak.");
                        soundEngine.playClip(Sound.AMASCUT_DEFEAT_4, executor);
                        break;
                    case 4:
                        addOverheadText(fakeNpc, "Amascut", "There is no place for you in my world!");
                        soundEngine.playClip(Sound.AMASCUT_DEFEAT_5, executor);
                        break;
                }
            }
        }

        if (Objects.equals(actor.getName(), "TzKal-Zuk") && config.zuk()) {
            Random random = new Random();
            boss.setOverheadCycle(300);
            switch(random.nextInt(3)) {
                case 0:
                    client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Impossible...", null);
                    boss.setOverheadText("Impossible...");
                    soundEngine.playClip(Sound.ZUK_VICTORY_1, executor);
                    break;
                case 1:
                    client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Not.. since Bandos...", null);
                    boss.setOverheadText("Not.. since Bandos...");
                    soundEngine.playClip(Sound.ZUK_VICTORY_2, executor);
                    break;
                case 2:
                    client.addChatMessage(ChatMessageType.PUBLICCHAT, "TzKal-Zuk", "Finally... After thousands of years...", null);
                    boss.setOverheadText("Finally... After thousands of years...");
                    soundEngine.playClip(Sound.ZUK_VICTORY_3, executor);
                    break;
            }
        }

        if (Objects.equals(actor.getName(), "Vorkath") && config.zemouregal() && fakeNpc != null) {
            Random random = new Random();
            switch(random.nextInt(4)) {
                case 0:
                    addOverheadText(fakeNpc, "Zemouregal", "Bah! Useless!",3,true);
                    soundEngine.playClip(Sound.VORKATH_VICTORY_1, executor);
                    break;
                case 1:
                    addOverheadText(fakeNpc, "Zemouregal", "Are you determined to fail?",3,true);
                    soundEngine.playClip(Sound.VORKATH_VICTORY_2, executor);
                    break;
                case 2:
                    addOverheadText(fakeNpc, "Zemouregal", "GET UP! - Why must you test my patience?",3,true);
                    soundEngine.playClip(Sound.VORKATH_VICTORY_3, executor);
                    break;
                case 3:
                    addOverheadText(fakeNpc, "Zemouregal", "Urgh, must I be forever plagued by lesser beings?",3,true);
                    soundEngine.playClip(Sound.VORKATH_VICTORY_4, executor);
                    break;
            }
            fakeNpcStatus = Status.ZEMOUREGAL_END.getStatus();
        }
    }

    public void resetInferno() {
        zukStart = false;
        zukFight = false;
        zukHealers = false;
        jadHealers = false;
        lastPhrase = -1;
        messageDelay = -1;
    }

    public void resetVorkath() {
        fakeNpcStatus = Status.INACTIVE.getStatus();
        zemoShutUpTick = -1;
        antiSpam = 0;
        zemoFaceVorkath = false;
        zemoTurnTimer = 0;
        if (fakeNpc != null)
            fakeNpc.setActive(false);
        fakeNpc = null;
    }

    public void resetWardens() {
        fakeNpcStatus = Status.INACTIVE.getStatus();
        antiSpam = 0;
        amascutMonologue = 0;
        phase = 0;
        skulls = 0;
        messageDelay = -1;
        tumekenP2 = false;
        if (fakeNpc != null)
            fakeNpc.setActive(false);
        fakeNpc = null;
    }

    public void fullReset() {
        resetInferno();
        resetVorkath();
        resetWardens();
    }

    @Provides
    RS3VoiceoversConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RS3VoiceoversConfig.class);
	}
}
