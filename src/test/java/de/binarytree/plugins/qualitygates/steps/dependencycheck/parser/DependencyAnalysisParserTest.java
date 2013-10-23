package de.binarytree.plugins.qualitygates.steps.dependencycheck.parser;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.binarytree.plugins.qualitygates.steps.dependencycheck.result.DependencyProblemType;

public class DependencyAnalysisParserTest extends AbstractParserTestUtils {
    private final static String UNUSED_DEPENDENCY_1 = "org.apache.maven:maven-artifact-manager:jar:2.0:compile";

    private final static String UNUSED_DEPENDENCY_2 = "org.apache.maven:maven-artifact:jar:2.0:compile";

    private final static String UNDECLARED_DEPENDENCY_1 = "org.apache.maven:maven-model:jar:2.0.2:compile";

    private final static String UNDECLARED_DEPENDENCY_2 = "org.codehaus.plexus:plexus-utils:jar:1.1:compile";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testParseLogWithOnlyUnusedButColors() throws Exception {

        File file = getFile("log_build_with_colors");
        String content = fileToString(file);

        Map<DependencyProblemType, List<String>> result = DependencyAnalysisParser.parseDependencyAnalyzeSection(
                content).getMapOfViolations();

        verifyNumberOfProblemTypes(result, 1);
        verifyNumberOfUnusedDeclarations(result, 2);
    }

    private String fileToString(File file) throws FileNotFoundException, IOException {
        FileReader reader = new FileReader(file);

        String content = IOUtils.toString(reader);
        return content;
    }

    @Test
    public void testParseLogWithUnusedAndUndeclaredInBracketlessFormat() throws Exception {

        File file = getFile("log_build_with_dependency_analyze_no_brackets");
        String content = fileToString(file);

        Map<DependencyProblemType, List<String>> result = DependencyAnalysisParser.parseDependencyAnalyzeSection(
                content).getMapOfViolations();

        verifyNumberOfProblemTypes(result, 2);
        verifyNumberOfUnusedDeclarations(result, 2);
        verifyNumberOfUndeclaredDependencies(result, 2);
    }

    @Test
    public void testParseLogWithUnusedAndUndeclaredWhereValidDependenciesAreAlsoListed() throws Exception {

        File file = getFile("log_build_with_dependency_analyze_only_with_pretext");
        String content = fileToString(file);

        Map<DependencyProblemType, List<String>> result = DependencyAnalysisParser.parseDependencyAnalyzeSection(
                content).getMapOfViolations();

        verifyNumberOfProblemTypes(result, 2);
        verifyNumberOfUnusedDeclarations(result, 4);
        verifyNumberOfUndeclaredDependencies(result, 11);
    }

    @Test
    public void testParseLogWithUnusedAndUndeclared() throws Exception {

        File file = getFile("log_build_with_dependency_analyze");
        String content = fileToString(file);

        Map<DependencyProblemType, List<String>> result = DependencyAnalysisParser.parseDependencyAnalyzeSection(
                content).getMapOfViolations();

        verifyNumberOfProblemTypes(result, 2);
        verifyNumberOfUnusedDeclarations(result, 2);
        verifyNumberOfUndeclaredDependencies(result, 2);
    }

    @Test
    public void testParseLogWithOnlyUnused() throws Exception {

        File file = getFile("log_build_only_unused");
        String content = fileToString(file);

        Map<DependencyProblemType, List<String>> result = DependencyAnalysisParser.parseDependencyAnalyzeSection(
                content).getMapOfViolations();

        verifyNumberOfProblemTypes(result, 1);
        verifyNumberOfUnusedDeclarations(result, 2);
    }

    private void verifyNumberOfUndeclaredDependencies(Map<DependencyProblemType, List<String>> result,
            int numberOfUnusedDependencies) {
        List<String> list = result.get(DependencyProblemType.UNDECLARED);
        Assert.assertEquals("Must have " + numberOfUnusedDependencies + " undeclared used dependencies",
                numberOfUnusedDependencies, list.size());
    }

    private void verifyNumberOfUnusedDeclarations(Map<DependencyProblemType, List<String>> result,
            int numberOfUnusedDeclarations) {
        List<String> list = result.get(DependencyProblemType.UNUSED);
        Assert.assertEquals("Must have " + numberOfUnusedDeclarations + " unused declared dependencies",
                numberOfUnusedDeclarations, list.size());
    }

    private void verifyNumberOfProblemTypes(Map<DependencyProblemType, List<String>> result, int numberOfProblemTypes) {
        assertEquals(numberOfProblemTypes, result.size());
    }

    @Test
    public void testParseDependencyAnalyzeSection() throws Exception {
        File file = getFile("dependency_analyze_section_1");
        String content = fileToString(file);

        Map<DependencyProblemType, List<String>> result = DependencyAnalysisParser.parseDependencyAnalyzeSection(
                content).getMapOfViolations();

        verifyNumberOfProblemTypes(result, 2);
        List<String> list = result.get(DependencyProblemType.UNUSED);
        verifyNumberOfUnusedDeclarations(result, 2);
        Assert.assertTrue("Unused declared dependencies must contains " + UNUSED_DEPENDENCY_1,
                list.contains(UNUSED_DEPENDENCY_1));
        Assert.assertTrue("Unused declared dependencies must contains " + UNUSED_DEPENDENCY_2,
                list.contains(UNUSED_DEPENDENCY_2));

        list = result.get(DependencyProblemType.UNDECLARED);
        Assert.assertEquals("Must have 2 undeclared used dependencies", 2, list.size());
        Assert.assertTrue("Used undeclared dependencies must contains " + UNDECLARED_DEPENDENCY_1,
                list.contains(UNDECLARED_DEPENDENCY_1));
        Assert.assertTrue("Used undeclared dependencies must contains " + UNDECLARED_DEPENDENCY_2,
                list.contains(UNDECLARED_DEPENDENCY_2));
    }

}
