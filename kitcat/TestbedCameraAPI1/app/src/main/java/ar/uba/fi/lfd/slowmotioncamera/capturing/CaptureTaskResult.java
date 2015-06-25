package ar.uba.fi.lfd.slowmotioncamera.capturing;

/**
 * Created by fluidodinamica on 28/04/15.
 */
public class CaptureTaskResult {
    private int capturesQty;
    private long milliseconds;


    public CaptureTaskResult(int capturesQty, long milliseconds) {
        this.capturesQty = capturesQty;
        this.milliseconds = milliseconds;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public int getCapturesQty() {
        return capturesQty;
    }

    public int getFPS() {
        return (int) (1000 * capturesQty / milliseconds);
    }
}
