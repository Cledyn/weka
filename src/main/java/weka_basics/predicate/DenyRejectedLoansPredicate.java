package weka_basics.predicate;

import lombok.AllArgsConstructor;
import weka.core.Instance;

import java.util.function.Predicate;

@AllArgsConstructor
public class DenyRejectedLoansPredicate implements Predicate<Instance> {
    private int loanStatusIndex;
    private String loanStatusToReject;

    @Override
    public boolean test(Instance instance) {
        return !loanStatusToReject.equals(instance.stringValue(loanStatusIndex));
    }
}
