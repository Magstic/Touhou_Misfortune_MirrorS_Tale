package touhou.battle;

public final class BattleHudModel implements BattleHudData {
    private int level;
    private int hiScore;
    private int score;
    private int player;
    private int bomb;
    private int power;
    private int graze;

    public int getLevel() {
        return level;
    }

    public int getHiScore() {
        return hiScore;
    }

    public int getScore() {
        return score;
    }

    public int getPlayer() {
        return player;
    }

    public int getBomb() {
        return bomb;
    }

    public int getPower() {
        return power;
    }

    public int getGraze() {
        return graze;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setHiScore(int hiScore) {
        this.hiScore = hiScore;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public void setBomb(int bomb) {
        this.bomb = bomb;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public void setGraze(int graze) {
        this.graze = graze;
    }
}
