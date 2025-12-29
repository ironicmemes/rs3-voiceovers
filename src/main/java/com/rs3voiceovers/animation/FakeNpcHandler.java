package com.rs3voiceovers.animation;

import com.rs3voiceovers.RS3VoiceoversOverlay;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.plugins.Plugin;

public class FakeNpcHandler extends Plugin {

    @Getter @Setter
    private int overheadTextTimer = 0;

    @Getter @Setter
    private Client client;
    @Getter @Setter
    private ChatMessageManager chatMessageManager;
    @Getter @Setter
    private RS3VoiceoversOverlay overlay;

    public boolean isOnFinalAnimationFrame(RuneLiteObject runeLiteObject) {
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
    public static Model createFakeNpcModel(Client client, int NpcID) {
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

    public static ModelData createModel(Client client, ModelData... data)
    {
        return client.mergeModels(data);
    }

    public static ModelData createModel(Client client, int... data)
    {
        ModelData[] modelData = new ModelData[data.length];
        for (int i = 0; i < data.length; i++)
        {
            modelData[i] = client.loadModelData(data[i]);
        }
        return client.mergeModels(modelData);
    }

    public RuneLiteObject createFakeNpc(int modelID, int animationID, int x, int y) {
        RuneLiteObject fakeNpc = new RuneLiteObject(client);
        fakeNpc.setModel(createFakeNpcModel(client, modelID));

        LocalPoint fakeNpcLocation = new LocalPoint(x, y, client.getLocalPlayer().getWorldView());
        fakeNpc.setLocation(fakeNpcLocation, client.getLocalPlayer().getWorldView().getPlane());

        AnimationController animations = new AnimationController(client, animationID);
        fakeNpc.setAnimationController(animations);
        overlay.setRuneLiteObject(fakeNpc);

        fakeNpc.setActive(true);

        return fakeNpc;
    }

    public void facePlayer(RuneLiteObject runeLiteObject) {
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

    public void faceDirection(RuneLiteObject runeLiteObject, int direction) {
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

    public void onGameTick(GameTick event) {
        if (overheadTextTimer > 0)
            overheadTextTimer--;
        else if (overlay != null)
            overlay.setOverheadText("");
    }
}
