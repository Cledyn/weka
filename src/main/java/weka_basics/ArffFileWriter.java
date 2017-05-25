package weka_basics;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class ArffFileWriter {

    public static void writeDataToFile(Instances toSave, String newFileName) throws IOException, URISyntaxException {

        URL resourcesDir = ArffFileWriter.class.getResource("/");
        String newFilePath = Paths.get(resourcesDir.toURI()) + "/" + newFileName;
        ArffSaver saver = new ArffSaver();
        saver.setInstances(toSave);
        saver.setFile(new File(newFilePath));
        saver.writeBatch();

    }
}
