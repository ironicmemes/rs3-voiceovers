package com.rs3voiceovers;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("rs3 voiceovers")
public interface RS3VoiceoversConfig extends Config
{

    @ConfigItem(
            position = 1,
            keyName = "voiceoverVolume",
            name = "Voiceover volume",
            description = "Adjust how loud audio voiceovers are played."
            //section = SECTION_GENERAL_ANNOUNCEMENT_SETTINGS,
    )
    default int voiceoverVolume() {
        return 100;
    }

    @ConfigItem(
            position = 2,
            keyName = "zuk",
            name = "TzKal-Zuk",
            description = "Adds TzKal-Zuk audio to the Inferno."
    )
    default boolean zuk() {
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "zemouregal",
            name = "Zemouregal and Vorkath",
            description = "Adds Zemouregal (with audio) to the Vorkath encounter."
    )
    default boolean zemouregal() {
        return true;
    }

    @ConfigItem(
            position = 4,
            keyName = "amascut",
            name = "Amascut, the Devourer",
            description = "Adds Amascut (with audio) to the Wardens encounter in the Tombs of Amascut."
    )
    default boolean amascut() {
        return true;
    }
}
