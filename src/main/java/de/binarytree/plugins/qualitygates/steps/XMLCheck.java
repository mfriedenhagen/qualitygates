package de.binarytree.plugins.qualitygates.steps;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.kohsuke.stapler.QueryParameter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.binarytree.plugins.qualitygates.GateStep;
import de.binarytree.plugins.qualitygates.GateStepDescriptor;

/**
 * This check relates a given expression to a given target file. Nevertheless,
 * it does not implement logic to determine the result of the check based an
 * matching or not matching the expression in the given file.
 * 
 * @author Marcel von Maltitz
 * 
 */
public abstract class XMLCheck extends GateStep {

    private final String expression;

    private final String targetFile;

    public XMLCheck(String expression, String targetFile) {
        this.expression = expression;
        this.targetFile = targetFile;
    }

    public String getExpression() {
        return this.expression;
    }

    public String getTargetFile() {
        return this.targetFile;
    }

    /**
     * Analyzes the given input stream whether it matches the expression defined
     * during construction.
     * 
     * @param stream
     *            the stream to analyze
     * @return a list of nodes matching the predefined expression
     * @throws javax.xml.parsers.ParserConfigurationException
     *             when the parser was misconfigured.
     * @throws SAXException
     *             when the stream cannot be parsed as XML
     * @throws IOException
     *             when the target file cannot be read
     * @throws XPathExpressionException
     *             when the expression does not compile
     */
    protected NodeList getMatchingNodes(InputStream stream)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory
                .newInstance();
        domFactory.setNamespaceAware(false);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(stream);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile(this.getExpression());

        return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
    }

    /**
     * Whether or not the target file is a valid file reference in workspace
     * 
     * @param build
     *            the build of which the workspace has to be used
     * @return wether or not the target file is a valid file reference
     * @throws IOException
     *             when the file cannot be accessed
     * @throws InterruptedException
     */
    protected boolean buildHasFileInWorkspace(AbstractBuild<?, ?> build)
            throws IOException, InterruptedException {
        return generateFilePathFromPathStringRelativeToBuild(build).exists();
    }

    private FilePath generateFilePathFromPathStringRelativeToBuild(
            AbstractBuild<?, ?> build) {
        return build.getModuleRoot().child(this.targetFile);
    }

    /**
     * Reads the target file defined by its path and returns an input stream
     * 
     * @param build
     *            the build of which the workspace has to be used
     * @return an input stream of the target file
     * @throws IOException
     *             when the target file cannot be accessed or read
     */
    protected InputStream obtainInputStreamOfTargetfileRelativeToBuild(
            AbstractBuild<?, ?> build) throws IOException {
        FilePath pom = build.getModuleRoot().child(this.targetFile);
        return pom.read();
    }

    @Override
    public String toString() {
        return super.toString() + "[" + this.getDescription() + "]";
    }

    public abstract static class XMLCheckDescriptor extends GateStepDescriptor {

        /**
         * Checks the validity of the XPath expression
         * 
         * @param value
         *            the expression to be checked
         * @return whether the given expression is valid
         */
        public FormValidation doCheckExpression(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation
                        .error("XPath expression must not be empty");
            } else {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                try {
                    xpath.compile(value);
                } catch (XPathExpressionException e) {
                    return FormValidation
                            .error("XPath expression is not valid.");
                }
                return FormValidation.ok();
            }
        }

        /**
         * Whether the given target file is a valid reference. This does not
         * check, whether the file actually and currently exists.
         * 
         * @param value the target file path
         * @return whether the given file path is valid 
         */
        public FormValidation doCheckTargetFile(@QueryParameter String value) {
            final FormValidation formValidation;
            if (value.length() == 0) {
                formValidation = FormValidation.error("Target file must not be empty");
            } else if (value.contains("..")) {
                formValidation = FormValidation
                        .error("Parent directory '..' may not be referenced");
            } else if (value.startsWith("/")) {
                formValidation = FormValidation.error("Path may not be absolute.");
            } else {
                formValidation = FormValidation.ok();
            }
            return formValidation;
        }

    }
}
