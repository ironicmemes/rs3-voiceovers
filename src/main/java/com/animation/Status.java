package com.animation;


import lombok.Getter;

public enum Status {
    INACTIVE(0),

    //Zemouregal
    ZEMOUREGAL_START(1),
    ZEMOUREGAL_CAST(2),
    ZEMOUREGAL_IDLE(3),
    ZEMOUREGAL_TURN(4),
    ZEMOUREGAL_SPELL(5),
    ZEMOUREGAL_END(6),
    ZEMOUREGAL_DESPAWN(7),

    //AMASCUT
    AMASCUT_START(8),
    AMASCUT_IDLE(9),
    AMASCUT_TELEPORT_OUT(10),
    AMASCUT_TELEPORT_IN(11),
    AMASCUT_CAST(12),
    AMASCUT_INTO_DOWN(13),
    AMASCUT_DOWN(14),
    AMASCUT_SUMMON_PHANTOM(15),
    AMASCUT_SUMMON_SKULLS(16),
    AMASCUT_CHANNEL(17),
    AMASCUT_ENRAGE_1(18),
    AMASCUT_ENRAGE_2(19),
    AMASCUT_ENRAGE_3(20),
    AMASCUT_END(21),
    ;

    @Getter
    private final int status;

    Status(int statusID) {
        status = statusID;
    }


}