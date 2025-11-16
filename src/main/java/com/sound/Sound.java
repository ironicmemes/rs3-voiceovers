package com.sound;

import lombok.Getter;

public enum Sound {
    //TzKal-Zuk
    ZUK_FIRST_WAVE_1("TzKal-Zuk_-_A_challenger_approaches....wav"),
    ZUK_FIRST_WAVE_2("TzKal-Zuk_-_What_makes_you_think_you_have_what_it_takes.wav"),
    ZUK_FIRST_WAVE_3("TzKal-Zuk_-_Interesting..._you_return.wav"),
    ZUK_WAVE_DEFAULT_1("TzKal-Zuk_-_Show_me_you_are_worthy!.wav"),
    ZUK_WAVE_DEFAULT_2("TzKal-Zuk_-_Fight,_worm!_Or_crawl_and_die!.wav"),
    ZUK_WAVE_DEFAULT_3("TzKal-Zuk_-_To_the_slaughter!.wav"),
    ZUK_WAVE_DEFAULT_4("TzKal-Zuk_-_Prove_your_worth.wav"),
    ZUK_WAVE_DEFAULT_5("TzKal-Zuk_-_Charge!.wav"),
    ZUK_WAVE_DEFAULT_6("TzKal-Zuk_-_Into_the_fray!.wav"),
    ZUK_WAVE_DEFAULT_7("TzKal-Zuk_-_The_unworthy_will_burn.wav"),
    ZUK_BLOB("TzKal-Zuk_-_Die,_and_be_reborn_in_flame!.wav"),
    ZUK_MELEE("TzKal-Zuk_-_Rise,_warrior_of_the_flame!.wav"),
    ZUK_RANGER("TzKal-Zuk_-_Rise,_ashen_ranger!.wav"),
    ZUK_MAGE("TzKal-Zuk_-_Rise,_mage_of_embers!.wav"),
    ZUK_SINGLE_JAD("TzKal-Zuk_-_Rise_Jad!.wav"),
    ZUK_TRIPLE_JAD_1("TzKal-Zuk_-_Enough._Jad_will_end_this.wav"),
    ZUK_TRIPLE_JAD_2("TzKal-Zuk_-_This_has_gone_on_too_long_-_kill_them!.wav"),
    ZUK_START_1("TzKal-Zuk_-_Now_-_the_true_battle_begins!.wav"),
    ZUK_START_2("TzKal-Zuk_-_Out_of_the_way,_weaklings!.wav"),
    ZUK_START_3("TzKal-Zuk_-_Perhaps_you_are_worthy.wav"),
    ZUK_SET_1("TzKal-Zuk_-_All_of_you__attack_.wav"),
    ZUK_SET_2("TzKal-Zuk_-_Rise,_scions_of_the_Kiln!.wav"),
    ZUK_SUMMONED_JAD("TzKal-Zuk_-_Unleash_Jad!.wav"),
    ZUK_JAD_HEALERS("TzKal-Zuk_-_HurKot._End_this,_now!.wav"),
    ZUK_HEALERS_1("TzKal-Zuk_-_Drag_them_to_the_molten_deep!.wav"),
    ZUK_HEALERS_2("TzKal-Zuk_-_I_grow_weary_of_this._End_it_quickly.wav"),
    ZUK_HEALERS_3("TzKal-Zuk_-_Flames_unending....wav"),
    ZUK_VICTORY_1("TzKal-Zuk_-_Impossible....wav"),
    ZUK_VICTORY_2("TzKal-Zuk_-_Not..._since_Bandos....wav"),
    ZUK_VICTORY_3("TzKal-Zuk_-_Finally..._After_thousands_of_years....wav"),
    ZUK_DEFEAT_1("TzKal-Zuk_-_Pathetic.wav"),
    ZUK_DEFEAT_2("TzKal-Zuk_-_Worthless._Just_like_the_rest.wav"),
    ZUK_DEFEAT_3("TzKal-Zuk_-_You_have_failed,_as_expected.wav"),

    //Vorkath (and Zemouregal)
    VORKATH_START_1("Zemouregal_-_Obey_your_master,_you_dumb_puppet.wav"),
    VORKATH_START_2("Zemouregal_-_The_dragon_WILL_be_mine!.wav"),
    VORKATH_START_3("Zemouregal_-_Rise_you_cursed_weakling,_I_shan't_tell_you_again!.wav"),
    VORKATH_SPAWN_1("Zemouregal_-_Your_struggle__like_your_life__is_meaningless.wav"),
    VORKATH_SPAWN_2("Zemouregal_-_No-one_can_stand_against_my_mighty_Undead!.wav"),
    VORKATH_SPAWN_3("Zemouregal_-_No_escape.wav"),
    VORKATH_POISON_1("Zemouregal_-_Suffocate_this_land_with_bile!.wav"),
    VORKATH_POISON_2("Zemouregal_-_Spew_your_venom,_my_malformed_pet!.wav"),
    VORKATH_POISON_3("Zemouregal_-_Cut_them_down_without_mercy!.wav"),
    VORKATH_POISON_4("Zemouregal_-_Drown_in_your_own_gore!.wav"),
    VORKATH_FIREBOMB_1("Zemouregal_-_DIE!.wav"),
    VORKATH_FIREBOMB_2("Zemouregal_-_Say_farewell.wav"),
    VORKATH_VICTORY_1("Zemouregal_-_Bah!_Useless!.wav"),
    VORKATH_VICTORY_2("Zemouregal_-_Are_you_determined_to_fail_.wav"),
    VORKATH_VICTORY_3("Zemouregal_-_GET_UP!_-_Why_must_you_test_my_patience_.wav"),
    VORKATH_VICTORY_4("Zemouregal_-_Urgh,_must_I_be_forever_plagued_by_lesser_beings_.wav"),
    VORKATH_DEFEAT_1("Zemouregal_-_Say_hello_to_Death_for_me!.wav"),
    VORKATH_DEFEAT_2("Zemouregal_-_Death_is,_quite_frankly,_too_good_for_you.wav"),
    VORKATH_DEFEAT_3("Zemouregal_-_Finally,_an_end_to_your_meddling.wav"),
    VORKATH_DEFEAT_4("Zemouregal_-_Death_protects_you__I_meant_to_have_you_for_my_own...wav"),

    //Amascut
    AMASCUT_START_1("Amascut_-_Destruction._Annihilation._My_vision_for_this_world.wav"),
    AMASCUT_START_2("Amascut_-_How_close_it_came_to_becoming_a_reality.wav"),
    AMASCUT_START_3("Amascut_-_Picture_the_heroes_who_would_have_risen_against_me.wav"),
    AMASCUT_START_4("Amascut_-_The_challenges_they_would_have_overcome.wav"),
    AMASCUT_START_5("Amascut_-_And_the_rewards_they_would_have_reaped.wav"),
    AMASCUT_START_6("Amascut_-_Who_among_you_is_worthy_of_facing_a_goddess_.wav"),
    AMASCUT_START_7("Amascut_-_Show_me....wav"),
    AMASCUT_OBELISK_1("Amascut_-_Bend_the_knee.wav"),
    AMASCUT_OBELISK_2("Amascut_-_Bend_the_knee_2.wav"),
    AMASCUT_ARCANE_1("Amascut_-_All_strength_withers.wav"),
    AMASCUT_ARCANE_2("Amascut_-_I_will_not_suffer_this.wav"),
    AMASCUT_ARCANE_3("Amascut_-_Your_soul_is_WEAK.wav"),
    AMASCUT_P2_START("Amascut_-_The_mice_fight_back....wav"),
    AMASCUT_P2_END_1("Amascut_-_I_will_not_be_denied.wav"),
    AMASCUT_P2_END_2("Amascut_-_I_WILL_NOT_BE_SUJUGATED_BY_A_MORTAL!.wav"),
    AMASCUT_P2_END_3("Amascut_-_Fall_to_the_shadow.wav"),
    AMASCUT_P2_END_4("Amascut_-_You_are_nothing!.wav"),
    AMASCUT_P2_END_5("Amascut_-_Mwahahaha.wav"),
    AMASCUT_P2_END_6("Amascut_-_Behold!_The_fate_of_this_world.wav"),
    AMASCUT_SKULLS_1("Amascut_-_ENOUGH.wav"),
    AMASCUT_SKULLS_2("Amascut_-_ENOUGH_2.wav"),
    AMASCUT_SKULLS_3("Amascut_-_ENOUGH_3.wav"),
    AMASCUT_ZEBAK_1("Amascut_-_Bring_forth_Crondis.wav"),
    AMASCUT_ZEBAK_2("Amascut_-_Crondis_._It_should_have_never_come_to_this.wav"),
    AMASCUT_BABA_1("Amascut_-_I_am_so_sorry__Apmeken.wav"),
    AMASCUT_BABA_2("Amascut_-_I_am_sorry__Apmeken.wav"),
    AMASCUT_AKKHA_1("Amascut_-_Het_bear_witness!.wav"),
    AMASCUT_AKKHA_2("Amascut_-_Forgive_me__Het.wav"),
    AMASCUT_KEPHRI_1("Amascut_-_Scabaras!.wav"),
    AMASCUT_KEPHRI_2("Amascut_-_Scabaras....wav"),
    AMASCUT_HURT_1("Amascut_-_Hurt_1.wav"),
    AMASCUT_HURT_2("Amascut_-_Hurt_2.wav"),
    AMASCUT_HURT_3("Amascut_-_Hurt_3.wav"),
    AMASCUT_HURT_4("Amascut_-_Hurt_4.wav"),
    AMASCUT_HURT_5("Amascut_-_Hurt_5.wav"),
    AMASCUT_HURT_6("Amascut_-_Hurt_6.wav"),
    AMASCUT_RECOVER("Amascut_-_No!_2.wav"),
    AMASCUT_ENRAGE_1("Amascut_-_It_is_NOT_over!.wav"),
    AMASCUT_ENRAGE_2("Amascut_-_Prove_that_you_have_the_will_to_stand_against_the_vision_of_a_goddess!.wav"),
    AMASCUT_ENRAGE_3("Amascut_-_I_will_tear_this_world_asunder!.wav"),
    AMASCUT_ENRAGE_4("Amascut_-_I_AM.wav"),
    AMASCUT_ENRAGE_5("Amascut_-_THE_GOD.wav"),
    AMASCUT_ENRAGE_6("Amascut_-_OF.wav"),
    AMASCUT_ENRAGE_7("Amascut_-_DESTRUCTION.wav"),
    AMASCUT_ENRAGE_8("Amascut_-_DESTRUCTION_2.wav"),
    AMASCUT_ENRAGE_9("Amascut_-_DESTRUCTION_3.wav"),
    AMASCUT_VICTORY_1("Amascut_-_Impossible....wav"),
    AMASCUT_VICTORY_2("Amascut_-_This_cannot_be_the_end....wav"),
    AMASCUT_DEFEAT_1("Amascut_-_Unworthy!.wav"),
    AMASCUT_DEFEAT_2("Amascut_-_Your_souls_are_mine!.wav"),
    AMASCUT_DEFEAT_3("Amascut_-_Pathetic.wav"),
    AMASCUT_DEFEAT_4("Amascut_-_Weak.wav"),
    AMASCUT_DEFEAT_5("Amascut_-_There_is_no_place_1.wav"),


    ;

    @Getter
    private final String resourceName;

    Sound(String resNam) {
        resourceName = resNam;
    }

    @Override
    public String toString() {
        return resourceName;
    }
}
