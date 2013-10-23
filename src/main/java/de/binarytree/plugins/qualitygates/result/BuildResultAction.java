package de.binarytree.plugins.qualitygates.result;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.ProminentProjectAction;
import hudson.model.StreamBuildListener;
import hudson.model.AbstractBuild;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import de.binarytree.plugins.qualitygates.QualityLineEvaluator;
import de.binarytree.plugins.qualitygates.steps.manualcheck.ManualCheckFinder;
import de.binarytree.plugins.qualitygates.steps.manualcheck.ManualCheckFinder.ManualCheckManipulator;

/**
 * This class realizes publishing the gate report via a dedicated URL-subspace /qualitygates/
 * 
 * @author Marcel von Maltitz
 * 
 */
public class BuildResultAction implements ProminentProjectAction {

    private static final String ICONS_PREFIX = "/plugin/qualitygates/images/24x24/";

    private QualityLineEvaluator gateEvaluator;

    public BuildResultAction(QualityLineEvaluator gateEvaluator) {
        this.gateEvaluator = gateEvaluator;
    }

    public QualityLineReport getQualityLineReport() {
        return this.gateEvaluator.getLatestResults();
    }

    public String getIconFileName() {
        return ICONS_PREFIX + "qualitygate_icon.png";
    }

    public String getDisplayName() {
        return "Execution Results of Quality Gates";
    }

    public String getUrlName() {
        return "qualitygates";
    }

    /**
     * Approves the next not_build manual check as long as it has the id provided by the request parameter "id".
     * 
     * @param req
     *            the stapler request provided by Jenkins
     * @param res
     *            the stapler response provided by Jenkins
     * @throws IOException
     *             when saving the build fails or the redirection by stapler is not possible
     */
    public void doApprove(StaplerRequest req, StaplerResponse res) throws IOException {
        manipulateManualCheck(req, res, true);
    }

    /**
     * Disapproves the next not_build manual check as long as it has the id provided by the request parameter "id".
     * 
     * @param req
     *            the stapler request provided by Jenkins
     * @param res
     *            the stapler response provided by Jenkins
     * @throws IOException
     *             when saving the build fails or the redirection by stapler is not possible
     */

    public void doDisapprove(StaplerRequest req, StaplerResponse res) throws IOException {
        manipulateManualCheck(req, res, false);
    }

    private void manipulateManualCheck(StaplerRequest req, StaplerResponse res, boolean manualCheckShallBeApproved)
            throws IOException {
        if (req.hasParameter("id")) {
            String hashIdOfCheck = req.getParameter("id");
            ManualCheckFinder finder = new ManualCheckFinder(this.getQualityLineReport());
            ManualCheckManipulator manipulator = finder.findCheckForGivenHash(hashIdOfCheck);
            if (manipulator.hasItem()) {
                if (manualCheckShallBeApproved) {
                    manipulator.approve();
                } else {
                    manipulator.disapprove();
                }
                rerunQualityLineEvaluation(req);
            }
        }
        res.sendRedirect(".");
    }

    private void rerunQualityLineEvaluation(StaplerRequest req) throws IOException {
        AbstractBuild<?, ?> build = getFormerBuild(req);
        BuildListener listener = new StreamBuildListener(getLogfileAppender(build));
        Launcher launcher = this.getLauncher(listener);
        this.gateEvaluator.evaluate(build, launcher, listener);
        build.save();
    }

    private AbstractBuild<?, ?> getFormerBuild(StaplerRequest req) {
        return req.findAncestorObject(AbstractBuild.class);
    }

    private FileOutputStream getLogfileAppender(AbstractBuild<?, ?> build) throws FileNotFoundException {
        return new FileOutputStream(build.getLogFile(), true);
    }

    protected Launcher getLauncher(BuildListener listener) {
        return Jenkins.getInstance().createLauncher(listener);
    }

}
