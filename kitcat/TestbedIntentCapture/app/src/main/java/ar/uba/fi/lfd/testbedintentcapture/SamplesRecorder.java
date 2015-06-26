package ar.uba.fi.lfd.testbedintentcapture;

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
    }

    public void recordSample(Date timestamp, int... data) throws IOException {
        String time = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(timestamp);
        this.writer.write(time);
        for (int digit: data) {
            this.writer.write(',');
            this.writer.write(String.valueOf(digit));
        }
        this.writer.write('\n');
    }

    public void dispose() throws IOException {
        if (this.writer != null) {
            this.writer.close();
            this.writer = null;
        }
    }
}
