package de.binarytree.plugins.qualitygates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.binarytree.plugins.qualitygates.result.BuildResultAction;

public class PluginIT {

    private static final String INDEX_URL = "/job/Project%20One/6/";

    private static final String REPORT_URL = INDEX_URL + BuildResultAction.URL + "/";

    /** Our logger. */
    private static final Logger LOG = LoggerFactory.getLogger(PluginIT.class);

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private JenkinsRule.WebClient webClient;

    private final static String reportLinkText = "Execution Results of Quality Gates";

    @Before
    public void setUp() throws Exception {
        webClient = j.createWebClient();
    }

    /**
     * {@inheritDoc}. Deletes the hudson instance directory on teardown to avoid leakage of testdirectories.
     */
    @After
    public void tearDown() throws Exception {
        final File rootDir = j.jenkins.getRootDir();
        LOG.info("Deleting " + rootDir + " in tearDown");
        FileUtils.deleteDirectory(rootDir);
    }

    @Test
    @LocalData
    public void testIndexPageShowsLinkToQualityLine() throws IOException, SAXException {
        final String gateIcon = "24x24/qualitygate_icon.png";
        checkHtmlOutput(gateIcon, INDEX_URL);
        checkHtmlOutput(reportLinkText, INDEX_URL);
    }

    @Test
    @LocalData
    public void testIndexPageShowsBriefInformationAboutQualityLineResult() throws IOException, SAXException {
        final String description = "Gates successful";
        final String termination = "Reasons for termination";
        checkHtmlOutput(description, INDEX_URL);
        checkHtmlOutput(termination, INDEX_URL);
    }

    @Test
    @LocalData
    public void testLinkOnIndexPageLeadsToReportPage() throws IOException, SAXException {
        HtmlPage htmlPage = webClient.goTo(INDEX_URL);
        HtmlAnchor reportLink = htmlPage.getAnchorByText(reportLinkText);
        assertNotNull(reportLink);
        checkCurrentUrl(REPORT_URL, reportLink.click());
    }

    @Test
    @LocalData
    public void testReportPageShowsReport() throws IOException, SAXException {
        HtmlPage htmlPage = webClient.goTo(REPORT_URL);
        List<Object> gates = htmlPage.getByXPath("//div[@gatereport]/li[@class='gate']");
        System.out.println(gates.toString());
    }

    @Test
    @LocalData
    public void testReportPageShowsGates() throws IOException, SAXException {
        HtmlPage htmlPage = webClient.goTo(REPORT_URL);
        final String title = reportLinkText;
        final String firstGate = "Gate \"Build stability\"";
        final String secondGate = "Gate \"Manual Gate\"";
        final String thirdGate = "Gate \"Static analysis\"";
        checkHtmlOutput(title, htmlPage);
        checkHtmlOutput(firstGate, REPORT_URL);
        checkHtmlOutput(secondGate, REPORT_URL);
        checkHtmlOutput(thirdGate, REPORT_URL);
    }

    @Test
    @LocalData
    public void testReportPageGatesHaveCorrectState() throws IOException, SAXException {
        HtmlPage htmlPage = webClient.goTo(REPORT_URL);
        List<HtmlListItem> elements = htmlPage.getByXPath("//div[@id='gatereport']//li[contains(@class,'gate')]");
        HtmlListItem firstGate = elements.get(0);
        HtmlListItem secondGate = elements.get(1);
        HtmlListItem thirdGate = elements.get(2);

        String success = "SUCCESS";
        String not_built = "NOT_BUILT";
        checkClassAttributeContains(firstGate, success);
        checkClassAttributeContains(secondGate, not_built);
        checkClassAttributeContains(thirdGate, not_built);
    }

    private void checkClassAttributeContains(HtmlListItem element, String value) {
        assertTrue(
                "Attribute " + element.getClassAttribute() + " of element " + element + " does not contain " + value,
                element.getClassAttribute().contains(value));
    }

    private void checkCurrentUrl(final String expectedUrl, final Page page) {
        URL url = page.getWebResponse().getUrl();
        System.out.println(url);
        String actualPath = url.getPath();
        assertEquals("URL should be " + expectedUrl + " but was " + actualPath, expectedUrl, actualPath);
    }

    private void checkHtmlOutput(final String htmlNeedle, final String relative) throws IOException, SAXException {
        final HtmlPage htmlPage = webClient.goTo(relative + "?html=true");
        checkHtmlOutput(htmlNeedle, htmlPage);
    }

    private void checkHtmlOutput(final String htmlNeedle, final HtmlPage htmlPage) {
        final String html = htmlPage.asXml();
        assertTrue(htmlNeedle + " not found in " + html, html.contains(htmlNeedle));
    }
}
