package ru.ks.tv;

public class KioskModeApp {
    public static boolean isInLockMode;

    public static boolean isInLockMode() {
        return isInLockMode;
    }

    public static void setIsInLockMode(boolean isInLockMode) {
        KioskModeApp.isInLockMode = isInLockMode;
    }
}
