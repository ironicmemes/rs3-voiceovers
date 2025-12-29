package com.rs3voiceovers;

public enum AmascutP3 {
    ZEBAK_BABA("Crondis/Apmeken"),
    AKKHA_KEPHRI("Het/Scabaras");

    AmascutP3(String option) {
        this.option = option;
    }

    private final String option;

    public String getOption() {
        return this.option;
    }

    public String toString() {
        return this.option;
    }
}
