package weka_entrophy.processor;

import com.google.common.collect.Lists;
import weka.core.Attribute;
import weka.core.Instances;
import weka_basics.ArffFileReader;

import java.util.Arrays;
import java.util.List;

public class Processor {

    Instances data;

    public Instances getData() {
        return data;
    }

    private Attribute evalAttr; //atrybut wybierany do klasyfikatora
    private Attribute classAttr; //atrybut decyzyjny
    private int[][] classAttrFreqMatrix;
    private int noOfEvalAttrGroups, noOfClassAttrGroups;
    private double[] attributeProbability, classProbability;
    private double[][] conditionalClassProbability;
    private double logBase = 2;

    public Processor(Instances data, String evalAttr, String classAttr) {
        this.data = data;
        this.evalAttr = data.attribute(evalAttr);
        this.classAttr = data.attribute(classAttr);
        this.noOfEvalAttrGroups = data.numDistinctValues(this.evalAttr);
        this.noOfClassAttrGroups = data.numClasses();

        classAttrFreqMatrix = calculateOccurrencesMatrix();
        classProbability = calculateProbabilityForClasses();
        attributeProbability = calculateProbabilityForAttributes();
        conditionalClassProbability = calculateConditionalProbabilityForClasses();
    }

    private void evaluate(){
        System.out.printf("Eval attr\t\t\tClass attr\tEval attr h\t Class attr h\t Relative h\t Gain Ratio\n");
        System.out.printf("%s\t%s\t%f\t%f\t%f\t%f\n\n",this.evalAttr.name(), this.classAttr.name(), evalAttrEntropy(), classAttrEntropy(),hClassAttribute(), getGainRatio());
    }
    private double getGainRatio(){
        double hAttr = evalAttrEntropy();
        return hAttr==0.0 ? 0: (classAttrEntropy() - hClassAttribute())/hAttr;
    }
    private int[][] calculateOccurrencesMatrix() {
        noOfClassAttrGroups = classAttr.numValues();
        noOfEvalAttrGroups = evalAttr.numValues();
        classAttrFreqMatrix = new int[noOfClassAttrGroups][noOfEvalAttrGroups];
        for (int instanceNum = 0; instanceNum < data.numInstances(); instanceNum++) {
            classAttrFreqMatrix[(int) data.instance(instanceNum).value(classAttr)]
                    [(int) data.instance(instanceNum).value(evalAttr)]++;
        }
//        System.out.println("classAttrFreqMatrix "+Arrays.deepToString(classAttrFreqMatrix));
        return classAttrFreqMatrix;
    }

    private double evalAttrEntropy() {
        double entropy = 0.0;
        for (int attributeIndex = 0; attributeIndex < noOfEvalAttrGroups; attributeIndex++) {
            entropy += attributeProbability[attributeIndex] * (Math.log(attributeProbability[attributeIndex]) / Math.log(logBase));
        }
        return entropy==0.0 ? 0.0 : -entropy;

    }

    private double classAttrEntropy() {
        double entropy = 0.0;
        for (int classIndex = 0; classIndex < noOfClassAttrGroups; classIndex++) {
            entropy += classProbability[classIndex] * Math.log(classProbability[classIndex]) / Math.log(logBase);
        }
        return entropy==0.0? 0.0: -entropy;
    }

    private double hClassAttribute() {
        double entropy = 0.0;
        double tmpAttrProb;
        double tmpConditionalEntropy;

        for (int attributeIndex = 0; attributeIndex < noOfEvalAttrGroups; attributeIndex++) {
            tmpConditionalEntropy = 0.0;
            tmpAttrProb = attributeProbability[attributeIndex];
            for (int classIndex = 0; classIndex < noOfClassAttrGroups; classIndex++) {
                tmpConditionalEntropy += conditionalClassProbability[classIndex][attributeIndex]
                        * Math.log(conditionalClassProbability[classIndex][attributeIndex]) / Math.log(logBase);
            }
            if(tmpConditionalEntropy!=0.0)
                entropy += tmpAttrProb * (-tmpConditionalEntropy);
        }
        return entropy;
    }

    private double[][] calculateConditionalProbabilityForClasses() {
        double[][] conditionalProbabilityForClasses = new double[noOfClassAttrGroups][noOfEvalAttrGroups];
        double[] attributesValuesTotal = new double[noOfEvalAttrGroups];
        for (int classIndex = 0; classIndex < noOfClassAttrGroups; classIndex++) {
            for (int attributeIndex = 0; attributeIndex < noOfEvalAttrGroups; attributeIndex++) {
                conditionalProbabilityForClasses[classIndex][attributeIndex] = classAttrFreqMatrix[classIndex][attributeIndex];
                attributesValuesTotal[attributeIndex] += classAttrFreqMatrix[classIndex][attributeIndex];
            }
        }
        for (int classIndex = 0; classIndex < noOfClassAttrGroups; classIndex++) {
            for (int attributeIndex = 0; attributeIndex < noOfEvalAttrGroups; attributeIndex++) {
                conditionalProbabilityForClasses[classIndex][attributeIndex] = conditionalProbabilityForClasses[classIndex][attributeIndex]
                        / attributesValuesTotal[attributeIndex];
            }
        }

//        System.out.println("Conditional Classes prob: " + Arrays.deepToString(conditionalProbabilityForClasses));
        return conditionalProbabilityForClasses;
    }

    private double[] calculateProbabilityForAttributes() {
        double[] probabilityForAttributesInGroups = new double[noOfEvalAttrGroups];
        for (int attrGroupIndex = 0; attrGroupIndex < noOfEvalAttrGroups; attrGroupIndex++) {
            for (int classGroupIndex = 0; classGroupIndex < noOfClassAttrGroups; classGroupIndex++) {
                probabilityForAttributesInGroups[attrGroupIndex] += classAttrFreqMatrix[classGroupIndex][attrGroupIndex];
            }
            probabilityForAttributesInGroups[attrGroupIndex] /= data.numInstances();
        }
//        System.out.println("Attr prob: " + Arrays.toString(probabilityForAttributesInGroups));
        return probabilityForAttributesInGroups;
    }

    private double[] calculateProbabilityForClasses() {
        double[] probabilityForClasses = new double[noOfClassAttrGroups];
        for (int classIndex = 0; classIndex < noOfClassAttrGroups; classIndex++) {
            for (int attributeIndex = 0; attributeIndex < noOfEvalAttrGroups; attributeIndex++) {
                probabilityForClasses[classIndex] += classAttrFreqMatrix[classIndex][attributeIndex];
            }
            probabilityForClasses[classIndex] /= data.numInstances();
        }
        System.out.println("Classes prob: " + Arrays.toString(probabilityForClasses));
        return probabilityForClasses;

    }


    public static void main(String[] args) throws Exception {
        Instances data = ArffFileReader.getDataFromFile("D:\\workspace\\repos\\weka\\src\\main\\resources\\212700L3_1_discretized.arff");
        List<Processor> results = Lists.newArrayList(new Processor(data, "miesieczny dochod netto", "status pozyczki"),
                new Processor(data, "ktore rolowanie", "status pozyczki"),
                new Processor(data, "okres w jakim pobieral dochod", "status pozyczki"),
                new Processor(data, "okres w jakim bedzie pobieral dochod", "status pozyczki"),
                new Processor(data, "plec", "status pozyczki"),
                new Processor(data, "wiek", "status pozyczki"),
                new Processor(data, "kwota kredytu", "status pozyczki"),
                new Processor(data, "rodzaj zrodla dochodu", "status pozyczki"));
        results.stream().forEach(Processor::evaluate);
    }

}
