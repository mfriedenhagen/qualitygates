package de.binarytree.plugins.qualitygates.steps;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.binarytree.plugins.qualitygates.result.GateStepReport;

/**
 * This check tests, whether a given expression is contained in a given file. If
 * a match is found, the check is successful. It can be configured, whether the
 * content of the match shall be reported in the corresponding report as a
 * reason of the result.
 * 
 * @author Marcel von Maltitz
 * 
 */
public class XPathExpressionCheck extends XMLCheck {

    private boolean reportContent;

    /**
     * Constructs a new check analyzing the given targetFile using the given
     * expression. When reportContent is true, the content of the matched
     * expression is returned as result reason.
     * 
     * @param targetFile
     *            the file to be examined
     * @param expression
     *            the XPath expression to match
     * @param reportContent
     *            whether or not the content of the matched expression shall be
     *            reported using the result reason
     */
    @DataBoundConstructor
    public XPathExpressionCheck(String targetFile, String expression,
            boolean reportContent) {
        super(expression, targetFile);
        this.reportContent = reportContent;
    }

    public boolean getReportContent() {
        return this.reportContent;
    }

    @Override
    public void doStep(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener, GateStepReport checkReport) {
        try {
            matchExpression(build, checkReport);
        } catch (IOException e) {
            failStepWithExceptionAsReason(checkReport, e);
        } catch (ParserConfigurationException e) {
            failStepWithExceptionAsReason(checkReport, e);
        } catch (SAXException e) {
            failStepWithExceptionAsReason(checkReport, e);
        } catch (XPathExpressionException e) {
            failStepWithExceptionAsReason(checkReport, e);
        }
    }

    private void matchExpression(AbstractBuild<?, ?> build,
            GateStepReport checkReport) throws IOException,
            ParserConfigurationException, SAXException,
            XPathExpressionException {
        InputStream stream = this
                .obtainInputStreamOfTargetfileRelativeToBuild(build);
        String content = this.getTextContentForXPathExpression(stream);
        this.setCheckResult(checkReport, content);
    }

    private void setCheckResult(GateStepReport checkReport, String content) {
        if (content != null && content.length() > 0) {
            if (reportContent) {
                checkReport.setResult(Result.SUCCESS, "Content: " + content);
            } else {
                checkReport.setResult(Result.SUCCESS, "");
            }
        } else {
            checkReport.setResult(Result.FAILURE, this.getExpression()
                    + " not found in " + this.getTargetFile());
        }
    }

    private String getTextContentForXPathExpression(InputStream stream)
            throws XPathExpressionException, ParserConfigurationException,
            SAXException, IOException {
        NodeList nodes = getMatchingNodes(stream);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        } else {
            return "";
        }
    }

    @Override
    public String getDescription() {
        return "Occurence of " + this.getExpression() + " in "
                + this.getTargetFile();
    }

    @Extension
    public static class DescriptorImpl extends XMLCheckDescriptor {

        @Override
        public String getDisplayName() {
            return "Check XML file for the occurence of an XPath expression";
        }

    }
}
