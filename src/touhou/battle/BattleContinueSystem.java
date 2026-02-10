package touhou.battle;

import touhou.GameProgress;

public final class BattleContinueSystem {
    private final GameProgress progress;

    private int remainingContinues;

    public BattleContinueSystem(GameProgress progress) {
        this.progress = progress;
    }

    public void resetForNewRun(int gamemode, boolean spellPractice) {
        if (spellPractice) {
            remainingContinues = 0;
            return;
        }
        // Mirrors DoJa : s_continue = 3 for normal runs.
        remainingContinues = (gamemode == 0) ? 3 : 0;
    }

    public int getRemainingContinues() {
        return remainingContinues;
    }

    public void setRemainingContinues(int v) {
        remainingContinues = v;
    }

    public boolean canOfferContinue(int gamemode, boolean spellPractice) {
        if (spellPractice) {
            return false;
        }
        if (gamemode != 0) {
            return false;
        }
        return remainingContinues > 0;
    }

    public boolean consumeContinueAndRecord() {
        if (remainingContinues <= 0) {
            return false;
        }

        remainingContinues--;

        if (progress != null) {
            progress.incContinueCount();
        }

        return true;
    }
}
