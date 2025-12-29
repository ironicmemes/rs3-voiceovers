package com.rs3voiceovers.animation;


import lombok.Getter;

public enum Status {
    INACTIVE(0),

    // Zemouregal
    ZEMOUREGAL_START(1),
    ZEMOUREGAL_CAST(2),
    ZEMOUREGAL_IDLE(3),
    ZEMOUREGAL_TURN(4),
    ZEMOUREGAL_SPELL(5),
    ZEMOUREGAL_SPELL_WINDUP(6),
    ZEMOUREGAL_END(7),
    ZEMOUREGAL_DESPAWN(8),

    // Amascut
    AMASCUT_START(9),
    AMASCUT_IDLE(10),
    AMASCUT_TURN(11),
    AMASCUT_TELEPORT_OUT(12),
    AMASCUT_TELEPORT_IN(13),
    AMASCUT_CAST(14),
    AMASCUT_INTO_DOWN(15),
    AMASCUT_DOWN(16),
    AMASCUT_SUMMON_PHANTOM_1(17),
    AMASCUT_SUMMON_PHANTOM_2(18),
    AMASCUT_CHANNEL(19),
    AMASCUT_ENRAGE_1(20),
    AMASCUT_ENRAGE_2(21),
    AMASCUT_ENRAGE_3(22),
    AMASCUT_END(23),

    // Fake Zuk
    ZUK_IDLE(24),
    ZUK_ATTACK(25),

    ;

    @Getter
    private final int status;

    Status(int statusID) {
        status = statusID;
    }


}
