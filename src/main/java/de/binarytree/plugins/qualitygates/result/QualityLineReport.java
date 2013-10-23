package de.binarytree.plugins.qualitygates.result;

import hudson.model.Result;

import java.util.LinkedList;
import java.util.List;

import de.binarytree.plugins.qualitygates.Gate;

/**
 * This class represents a report about the execution of a quality line. It holds all reports of the gates contained in
 * the corresponding quality line.
 * 
 * @author Marcel von Maltitz
 * 
 */
public class QualityLineReport extends ListContainer<GateReport> {

    private List<GateReport> gates() {
        return this.getItems();
    }

    /**
     * Returns the number of gate reports.
     * 
     * @return the number of gate reports
     */
    public int getNumberOfGates() {
        return gates().size();
    }

    /**
     * Adds a new {@link GateReport} to the end of this quality line report. If the report is already contained in this
     * quality line report, as defined by {@link #isSameItem(GateReport, GateReport)}, the old report is replaced by the
     * given without changing the order of the gate reports.
     * 
     * @param gateReport
     *            the {@link GateReport} to be added
     */
    public void addGateReport(GateReport gateReport) {
        this.addOrReplaceItem(gateReport);
    }

    /**
     * Returns a list of all gate reports contained in this quality line report.
     * 
     * @return a list of gate reports as descried above
     */
    public List<GateReport> getGateReports() {
        return new LinkedList<GateReport>(this.gates());
    }

    /**
     * Returns the {@link GateReport} {@link Result} for the given {@link Gate} if it has been reported. Returns
     * {@link Result#NOT_BUILT} otherwise.
     * 
     * @param gate
     *            the gate of which the report shall be returned
     * @return see above
     */
    public Result getResultFor(Gate gate) {
        GateReport report = this.getGateReportFor(gate);
        if (report != null) {
            return report.getResult();
        }
        return Result.NOT_BUILT;
    }

    /**
     * Returns the {@link GateReport} for the given {@link Gate}
     * 
     * @param gate
     *            the gate of which the report shall be returned
     * @return the report for the given gate
     */
    public GateReport getGateReportFor(Gate gate) {
        for (GateReport report : this.gates()) {
            if (report.belongsTo(gate)) {
                return report;
            }
        }
        return null;
    }

    @Override
    protected boolean isSameItem(GateReport a, GateReport b) {
        return a.referencesSameGateAs(b);
    }

    /**
     * Returns the number of the gates which have been successfully evaluated. Equals {@link #getNumberOfGates()} when
     * all gates have been successful.
     * 
     * @return the number of successful gates
     */
    public int getNumberOfSuccessfulGates() {
        int count = 0;
        for (GateReport result : this.gates()) {
            if (resultCausedTermination(result)) {
                break;
            }
            count++;
        }
        return count;
    }

    /**
     * Returns a list of strings which tell about the reasons why the quality line terminated. These reasons are
     * provided by the gate report of all failed gates.
     * 
     * @return a list of termination reasons
     */
    public List<String> getReasonsOfTermination() {
        List<String> reasons = new LinkedList<String>();
        for (GateReport report : this.gates()) {
            if (resultCausedTermination(report)) {
                reasons = report.getReasonOfFailure();
                break;
            }
        }
        return reasons;
    }

    private boolean resultCausedTermination(GateReport report) {
        return report.getResult().isWorseOrEqualTo(Result.FAILURE);
    }
}
