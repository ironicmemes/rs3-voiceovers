package com.rs3voiceovers;
import java.awt.*;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;

public class RS3VoiceoversOverlay extends Overlay {
    @Inject
    private final Client client;

    @Getter @Setter
    private String overheadText = "";

    @Getter @Setter
    private int maxTextHeightOffset = 0;

    @Getter @Setter
    private int minTextHeightOffset = 0;

    @Getter @Setter
    private RuneLiteObject runeLiteObject;

    public RS3VoiceoversOverlay(Client client) {
        this.client = client;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D g) {
        if (runeLiteObject == null)
            return null;

        LocalPoint localPoint = runeLiteObject.getLocation();
        if (localPoint == null)
            return null;

        int plane = client.getLocalPlayer().getWorldArea().getPlane();
        int heightOffset = runeLiteObject.getModel().getModelHeight();

        if (heightOffset > maxTextHeightOffset)
            heightOffset = maxTextHeightOffset;
        if (heightOffset < minTextHeightOffset)
            heightOffset = minTextHeightOffset;

        Point canvasPoint = Perspective.localToCanvas(client, localPoint, plane, heightOffset);

        if (canvasPoint == null) {
            return null;
        }

        g.setFont(FontManager.getRunescapeBoldFont());
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(overheadText);
        int x = canvasPoint.getX() - textWidth / 2;
        int y = canvasPoint.getY() + 20;

        g.setColor(Color.BLACK);
        g.drawString(overheadText, x + 1, y + 1);
        g.setColor(Color.YELLOW);
        g.drawString(overheadText, x, y);

        return null;
    }
}
