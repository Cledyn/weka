package weka_basics;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.Objects;


public class ArffFileReader {

    public static Instances getDataFromFile(String dataFilePath) throws Exception {
        String realPath = Objects.isNull(dataFilePath) ? ArffFileReader.class.getResource("/212700L1_2final_java.arff").getFile() : dataFilePath;
        DataSource source = new DataSource(realPath);
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);
        return data;
    }

    public static Instances createEmpty() throws Exception {
        return getDataFromFile(ArffFileReader.class.getResource("/empty.arff").getFile());
    }
}
