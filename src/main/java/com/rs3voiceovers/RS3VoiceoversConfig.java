package com.rs3voiceovers;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("rs3 voiceovers")
public interface RS3VoiceoversConfig extends Config
{

    @ConfigItem(
            position = 0,
            keyName = "voiceoverVolume",
            name = "Voiceover volume",
            description = "Adjust how loud audio voiceovers are played."
            //section = SECTION_GENERAL_ANNOUNCEMENT_SETTINGS,
    )
    default int voiceoverVolume() {
        return 30;
    }

    @ConfigSection(
            name = "TzKal-Zuk",
            description = "TzKal-Zuk audio settings.",
            position = 1
    )
    String zuk = "zukSection";

    @ConfigItem(
            position = 1,
            keyName = "zuk",
            name = "TzKal-Zuk",
            description = "Adds TzKal-Zuk audio to the Inferno and Jad Challenges.",
            section = zuk
    )
    default boolean zuk() {
        return true;
    }

    @ConfigSection(
            name = "Zemouregal",
            description = "Zemouregal audio settings.",
            position = 2
    )
    String zemouregal = "zemouregalSection";


    @ConfigItem(
            position = 2,
            keyName = "zemouregal",
            name = "Zemouregal and Vorkath",
            description = "Adds Zemouregal (with audio) to the Vorkath encounter.",
            section = zemouregal
    )
    default boolean zemouregal() {
        return true;
    }

    @ConfigSection(
            name = "Amascut",
            description = "Amascut audio settings.",
            position = 4
    )
    String amascut = "amascutSection";

    @ConfigItem(
            position = 4,
            keyName = "amascut",
            name = "Amascut, the Devourer",
            description = "Adds Amascut (with audio) to the Wardens encounter in the Tombs of Amascut.",
            section = amascut
    )
    default boolean amascut() {
        return true;
    }

    @ConfigItem(
            position = 5,
            keyName = "amascutP3",
            name = "Amascut P3 Callout",
            description = "Determines whether Amascut calls out to Crondis/Apmeken or Het/Scabaras in P3.",
            section = amascut
    )
    default AmascutP3 amascutP3() {
        return AmascutP3.ZEBAK_BABA;
    }

    @ConfigSection(
            name = "God Wars Dungeon",
            description = "God Wars Dungeon audio settings.",
            position = 6
    )
    String gwd = "gwdSection";

    @ConfigItem(
            position = 6,
            keyName = "graardor",
            name = "General Graardor",
            description = "Adds audio for General Graardor's overhead text + death.",
            section = gwd
    )
    default boolean graardor() {
        return true;
    }

    @ConfigItem(
            position = 7,
            keyName = "kreearra",
            name = "Kree'arra",
            description = "Adds audio for Kree'arra's overhead text.",
            section = gwd
    )
    default boolean kreearra() {
        return true;
    }

    @ConfigItem(
            position = 8,
            keyName = "kril",
            name = "K'ril Tsutsaroth",
            description = "Adds audio for K'ril Tsutsaroth's overhead text + death.",
            section = gwd
    )
    default boolean kril() {
        return true;
    }

    @ConfigItem(
            position = 9,
            keyName = "zilyana",
            name = "Commander Zilyana",
            description = "Adds audio for Commander Zilyana's overhead text + death.",
            section = gwd
    )
    default boolean zilyana() {
        return true;
    }

    @ConfigItem(
            position = 10,
            keyName = "zilyanaHurt",
            name = "Zilyana hurt noises",
            description = "Adds the noises Commander Zilyana makes in RS3 when she takes damage.",
            section = gwd
    )
    default boolean zilyanaHurt() {
        return false;
    }


}
