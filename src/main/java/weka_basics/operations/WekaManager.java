package weka_basics.operations;

import weka_basics.ArtffFileReader;
import weka_basics.ArtffFileWriter;
import weka_basics.predicate.DenyRejectedLoansPredicate;
import weka_basics.predicate.DenyTooHighLoanPredicate;
import lombok.AllArgsConstructor;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;

@AllArgsConstructor
public class WekaManager {

    private static final int LOAN_STATUS_INDEX = 1;
    private static final Integer LOAN_VALUE_INDEX = 4;
    private Instances data;

    public static WekaManager create(String filePath) throws Exception {
        Instances data = ArtffFileReader.getDataFromFile(filePath);
        return new WekaManager(data);
    }

    private Instances removeRecordsByAttrIndex(int attributeIndex, Instances source) throws Exception {
        Remove removeTool = getRemoveAttrByIndex(attributeIndex);
        return Filter.useFilter(source, removeTool);
    }

    private Remove getRemoveAttrByIndex(int attrToRemoveIndex) throws Exception {
        String[] options = new String[2];
        options[0] = "-R";
        options[1] = Integer.toString(attrToRemoveIndex);
        Remove removeTool = new Remove();
        removeTool.setOptions(options);
        removeTool.setInputFormat(data);
        return removeTool;
    }

    private Instances removeRejectedLoans() throws Exception {
        Instances empty = ArtffFileReader.createEmpty();
        data.stream().filter(new DenyRejectedLoansPredicate(0, "odmowa")).forEach(empty::add);
        return empty;
    }

    private Instances removeLoansGreaterThan(double maxAcceptableLoan) throws Exception {
        Instances empty = ArtffFileReader.createEmpty();
        data.stream().filter(new DenyTooHighLoanPredicate(maxAcceptableLoan, LOAN_VALUE_INDEX)).forEach(empty::add);
        return empty;
    }

    private Instances getProperData(double maxLoanValue) throws Exception {
        Instances empty = ArtffFileReader.createEmpty();
        data.stream().filter(k -> !"odmowa".equals(k.stringValue(0)) && maxLoanValue >= k.value(4)).forEach(empty::add);
        //instances bez odmów i z maks kwotą pożyczki 900 zł
        return removeRecordsByAttrIndex(LOAN_STATUS_INDEX, empty);
    }

    private Instances removeLoansGreaterThan(int attrIndex, String maxAcceptableValue) throws Exception {
        RemoveWithValues filter = new RemoveWithValues();
        System.out.println("FILTER OPTIONS: " + filter.listOptions().toString());
        String[] options = new String[4];
        options[0] = "-C";   // attribute index
        options[1] = Integer.toString(attrIndex);
        options[2] = "-S";   // match if value is smaller than
        options[3] = maxAcceptableValue;   // 10
        filter.setOptions(options);
        filter.setInvertSelection(true);
        filter.setInputFormat(data);
        return Filter.useFilter(data, filter);
    }

    public static void main(String[] args) throws Exception {
        WekaManager mng = create(null);
        Instances removedRejectedLoans = mng.removeRejectedLoans();
        ArtffFileWriter.writeDataToFile(removedRejectedLoans, "removed_rejected_loans_v2.arff");

        Instances loansLowerThan900 = mng.removeLoansGreaterThan(5, "900.0");
        ArtffFileWriter.writeDataToFile(loansLowerThan900, "loans_lower_than_900_v2.arff");

        Instances dataWithoutStatus = mng.removeRecordsByAttrIndex(LOAN_STATUS_INDEX, mng.data);
        ArtffFileWriter.writeDataToFile(dataWithoutStatus, "data_without_status_v2.arff");

        Instances loansLowerThan900AndOnlyAccepted = mng.getProperData(900);
        ArtffFileWriter.writeDataToFile(loansLowerThan900AndOnlyAccepted, "merged_v2.arff");
    }
}
