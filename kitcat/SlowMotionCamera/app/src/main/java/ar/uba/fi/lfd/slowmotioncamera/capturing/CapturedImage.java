package ar.uba.fi.lfd.slowmotioncamera.capturing;

import java.util.Date;

/**
 * Created by fluidodinamica on 27/04/15.
 */
public class CapturedImage {
    private Date timestamp;
    private byte[] data;

    public CapturedImage(byte[] data) {
        this.timestamp = new Date();
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
