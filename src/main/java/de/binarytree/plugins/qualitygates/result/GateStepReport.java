package de.binarytree.plugins.qualitygates.result;

import hudson.model.Result;
import de.binarytree.plugins.qualitygates.GateStep;

/**
 * This class represents the report of performed gate step. The result is reported as well a (optional) reason of the
 * result. The reason is not optional, when the step was not successful.
 * 
 * @author Marcel von Maltitz
 * 
 */
public class GateStepReport {

    private String stepName;

    private String description;

    private Result result = Result.NOT_BUILT;

    private String reason;

    private GateStep step;

    /**
     * Creates a new GateStepReport for the given {@link Check}.
     * 
     * @param check
     *            the check for which this report shall be created
     */
    public GateStepReport(GateStep check) {
        this.step = check;
        this.stepName = check.getDisplayName();
        this.description = check.getDescription();
    }

    public GateStep getStep() {
        return this.step;
    }

    public String getDescription() {
        return this.description;
    }

    public String getStepName() {
        return this.stepName;
    }

    /**
     * Returns the {@link Result} of this report.
     * 
     * @return the result of this report
     */
    public Result getResult() {
        return this.result;
    }

    /**
     * Returns the reason for the result of this report. When the result is positive, the reason may be empty.
     * 
     * @return the reason for the result of this report
     */
    public String getReason() {
        return this.reason;
    }

    /**
     * Documents the result of the corresponding step.
     * 
     * Note: Negative results also need a supplied reason of failure.
     * 
     * @param result
     *            the result for the corresponding step
     */
    public void setResult(Result result) {
        if (isNegative(result)) {
            throwExceptionDueToMissingReason();
        }
        this.result = result;
    }

    /**
     * Documents the result of the corresponding step. Note: Negative results also need a supplied reason of failure.
     * 
     * @param result
     *            the result for the corresponding step
     * @param the
     *            reason which lead to this result
     */
    public void setResult(Result result, String reason) {
        if (isNegative(result) && isEmpty(reason)) {
            throwExceptionDueToMissingReason();
        }
        // It's okay to have positive results and a reason, even if not
        // necessary
        this.result = result;
        this.reason = reason;
    }

    private boolean isNegative(Result result) {
        return Result.FAILURE.equals(result) || Result.UNSTABLE.equals(result);
    }

    private boolean isEmpty(String reason) {
        return (reason == null) || reason.isEmpty();
    }

    private void throwExceptionDueToMissingReason() {
        throw new IllegalArgumentException("Negative results need a reason");
    }

    /**
     * Whether or not this report references the same step as the given report
     * 
     * @param other
     *            the other report
     * @return whether or not both reports reference the same step
     */
    public boolean referencesSameStepAs(GateStepReport other) {
        return this.step == other.step;
    }

    /**
     * Whether or not this report references the given check.
     * 
     * @param check
     *            the check to be tested
     * @return whether or not this report references the given check
     */
    public boolean references(GateStep check) {
        return this.step.equals(check);
    }
}
