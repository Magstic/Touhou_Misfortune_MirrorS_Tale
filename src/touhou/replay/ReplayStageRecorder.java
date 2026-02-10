package touhou.replay;

public final class ReplayStageRecorder {
    private final ReplayInputBuffer full = new ReplayInputBuffer();

    private final byte[] dialoguePageCounts = new byte[ReplayHeader.DIALOGUE_MAX];
    private int dialoguePageCountLen;

    private boolean recording;
    private int bossStartFrame;

    public ReplayStageRecorder() {
        reset();
    }

    public void reset() {
        full.clear();
        recording = false;
        bossStartFrame = -1;

        for (int i = 0; i < dialoguePageCounts.length; i++) {
            dialoguePageCounts[i] = 0;
        }
        dialoguePageCountLen = 0;
    }

    public void start() {
        reset();
        recording = true;
    }

    public void stop() {
        recording = false;
    }

    public boolean isRecording() {
        return recording;
    }

    public int getBossStartFrame() {
        return bossStartFrame;
    }

    public int getFramePos() {
        return full.getPos();
    }

    public void markBossStart(int frameIndex) {
        if (!recording) {
            return;
        }
        if (bossStartFrame >= 0) {
            return;
        }
        if (frameIndex < 0) {
            frameIndex = 0;
        }
        if (frameIndex > ReplayInputBuffer.FRAME_COUNT) {
            frameIndex = ReplayInputBuffer.FRAME_COUNT;
        }
        bossStartFrame = frameIndex;
    }

    public void onFrame(int keys, int pressed, int fireActionPressed) {
        if (!recording) {
            return;
        }
        byte b = ReplayKeyCodec.encode(keys, pressed, fireActionPressed);
        full.putNext(b);
    }

    public void recordDialoguePageCount(int encounterIndex, int pageCount) {
        if (!recording) {
            return;
        }
        if (encounterIndex < 0 || encounterIndex >= ReplayHeader.DIALOGUE_MAX) {
            return;
        }
        if (pageCount < 0) {
            pageCount = 0;
        }
        if (pageCount > 255) {
            pageCount = 255;
        }
        dialoguePageCounts[encounterIndex] = (byte) (pageCount & 0xFF);
        if (encounterIndex >= dialoguePageCountLen) {
            dialoguePageCountLen = encounterIndex + 1;
        }
    }

    public byte[] encodeFull() {
        return ReplayCodec.encode(full.raw());
    }

    public byte[] encodeBossOnly() {
        byte[] bossFrames = new byte[ReplayInputBuffer.FRAME_COUNT];
        int start = bossStartFrame;
        if (start < 0) {
            start = ReplayInputBuffer.FRAME_COUNT;
        }
        if (start > ReplayInputBuffer.FRAME_COUNT) {
            start = ReplayInputBuffer.FRAME_COUNT;
        }

        byte[] src = full.raw();
        int dst = 0;
        for (int i = start; i < ReplayInputBuffer.FRAME_COUNT && dst < ReplayInputBuffer.FRAME_COUNT; i++) {
            bossFrames[dst++] = src[i];
        }

        return ReplayCodec.encode(bossFrames);
    }

    public void fillHeaderForSave(ReplayHeader header, int flag, int difficulty, int chara, int stage, int score, int type, int mode, int spellId) {
        if (header == null) {
            return;
        }
        header.clear();
        header.setFlag(flag);
        header.setDifficulty(difficulty);
        header.setChara(chara);
        header.setStage(stage);
        header.setScore(score);
        header.setType(type);
        header.setMode(mode);
        header.setSpellId(spellId);
        header.setBossStartFrame(bossStartFrame);

        header.setDialoguePageCounts(dialoguePageCounts, dialoguePageCountLen);
    }
}
