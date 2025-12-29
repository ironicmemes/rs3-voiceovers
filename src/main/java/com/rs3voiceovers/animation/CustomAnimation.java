package com.rs3voiceovers.animation;


import lombok.Getter;

public enum CustomAnimation {
    // Zuk
    ZUK_IDLE(7564),
    ZUK_ATTACK(7566),

    // Zemouregal
    ZEMOUREGAL_IDLE(9874),
    ZEMOUREGAL_WALK_1(9875),
    ZEMOUREGAL_WALK_2(9876),
    ZEMOUREGAL_CAST_1(9879),
    ZEMOUREGAL_CAST_2(9878),
    ZEMOUREGAL_CAST_3(9877),
    ZEMOUREGAL_CAST_4(9898),
    ZEMOUREGAL_CAST_5(10148),
    ZEMOUREGAL_CAST_6(10153),
    ZEMOUREGAL_SPAWN(9880),
    ZEMOUREGAL_DESPAWN(9872),

    // Amascut
    AMASCUT_SPAWN(11261),
    AMASCUT_IDLE_1(10169),
    AMASCUT_IDLE_2(11698),
    AMASCUT_WALK_1(10170),
    AMASCUT_DOWN_ENTER(10394),
    AMASCUT_DOWN(10395),
    AMASCUT_DOWN_EXIT(11131),
    AMASCUT_CAST_1(11424), // bend the knee
    AMASCUT_CAST_2(10676), // melee command
    AMASCUT_CAST_3(11133), // slow bow shot
    AMASCUT_CAST_4(11423), // mage command
    AMASCUT_CAST_5(10503), // sweep into handsup, like tele
    AMASCUT_CAST_6(10655), // raise hands then back down (use for spawns?)
    AMASCUT_CAST_7(11060), // raise hands then back down slower (use for spawns?)
    AMASCUT_CAST_8(9816), // destruction
    AMASCUT_CAST_9(11517), // arms out
    AMASCUT_CAST_10(11645),
    AMASCUT_HURT(10501),
    AMASCUT_TELEPORT_OUT(10543), // quick fade and up
    AMASCUT_TELEPORT_IN(10956), // quick fade and down
    AMASCUT_CHANNEL(11283),
    AMASCUT_DESPAWN(11315),
    AMASCUT_ENRAGE_1(11875),
    AMASCUT_ENRAGE_2(11876),
    AMASCUT_ENRAGE_3(11877),
    AMASCUT_ENRAGE_4(11878),

    ;

    @Getter
    private final int animationID;

    CustomAnimation(int animID) {
        animationID = animID;
    }


}
