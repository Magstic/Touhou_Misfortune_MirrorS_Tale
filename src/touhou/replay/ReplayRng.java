package touhou.replay;

public final class ReplayRng {
    private int state;

    public ReplayRng() {
        state = 1;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public int nextInt() {
        state = (state * 1103515245) + 12345;
        return state;
    }
}
