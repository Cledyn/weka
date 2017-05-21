package weka_basics.predicate;

import lombok.AllArgsConstructor;
import weka.core.Instance;

import java.util.function.Predicate;

@AllArgsConstructor
public class DenyTooHighLoanPredicate implements Predicate<Instance> {
    private double maxLoanVal;
    private int loanValueIndex;

    @Override
    public boolean test(Instance instance) {
        return maxLoanVal >=instance.value(loanValueIndex);
    }
}
