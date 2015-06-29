package ar.uba.fi.lfd.testbedcamerapreviewapi1.capturing;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fluidodinamica on 27/04/15.
 */
public class SamplesRecorder {
    private String captureFolder;
    private Writer writer;
    private Date startingTime;
    private long elapsedMillisecs;
    private int recordsQty;

    public SamplesRecorder(String captureFolder) {
        this.captureFolder = captureFolder;
    }


    public void init() throws FileNotFoundException, UnsupportedEncodingException {
        File imagesFolder = new File(this.captureFolder);
        if (!imagesFolder.exists())
            imagesFolder.mkdirs();
        String label = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(new Date());
        String filename = String.format("samples_%s.txt", label);
        File output = new File(imagesFolder, filename);
        FileOutputStream stream = new FileOutputStream(output);
        this.writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF8"));
        this.startingTime = new Date();
    }

    public void recordSample(Date timestamp, int... data) throws IOException {
        String time = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(timestamp);
        this.writer.write(time);
        for (int digit: data) {
            this.writer.write(',');
            this.writer.write(String.valueOf(digit));
        }
        this.writer.write('\n');
        this.recordsQty++;
    }

    public void dispose() throws IOException {
        Date endingTime = new Date();
        this.elapsedMillisecs = endingTime.getTime() - this.startingTime.getTime();

        if (this.writer != null) {
            this.writer.close();
            this.writer = null;
        }
    }

    public long getElapsedMillisecs() {
        return this.elapsedMillisecs;
    }

    public int getRecordsQty() {
        return this.recordsQty;
    }
}
