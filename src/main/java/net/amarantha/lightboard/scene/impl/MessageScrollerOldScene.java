package net.amarantha.lightboard.scene.impl;

import net.amarantha.lightboard.scene.OldScene;
import net.amarantha.lightboard.scene.OldSceneManager;
import net.amarantha.lightboard.surface.LightBoardSurface;
import net.amarantha.lightboard.updater.MessageUpdater;
import net.amarantha.lightboard.zone.old.TextZone_Old;

import javax.inject.Inject;

public class MessageScrollerOldScene extends OldScene {

    @Inject private OldSceneManager sceneManager;

    @Inject private TextZone_Old zone;

    @Inject private MessageUpdater updater;

    public MessageScrollerOldScene(LightBoardSurface surface) {
        super("Message Scroller");
    }

    @Override
    public void build() {

        zone.scrollUp().setScrollTick(20);

        zone.addScrollCompleteHandler(() -> {
            zone.advanceMessage();
            zone.resetScroll();
            sceneManager.advanceScene();
        });

        updater.setZone(zone);
        updater.setDataRefresh(10000);

        registerZones(zone);
        registerUpdaters(updater);

    }
}