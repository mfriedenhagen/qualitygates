package de.binarytree.plugins.qualitygates.steps.dependencycheck.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import de.binarytree.plugins.qualitygates.steps.dependencycheck.result.MavenDependencyAnalysisResult;
import de.binarytree.plugins.qualitygates.steps.dependencycheck.result.DependencyProblemType;

/**
 * Parse the content of dependency:* sections and organize the detected
 * problems.
 * 
 * @author Vincent Sellier
 */
public final class DependencyAnalysisParser {
    private DependencyAnalysisParser() {
    }

    private static final Pattern ARTIFACT_PATTERN = Pattern
            .compile(".*:.*:.*:.*:.*");

    public static enum DependencyProblemTypesDetection {
        UNUSED(DependencyProblemType.UNUSED, ".*Unused declared.*"), UNDECLARED(
                DependencyProblemType.UNDECLARED, ".*Used undeclared.*");

        private Pattern pattern;

        private DependencyProblemType problemType;

        private DependencyProblemTypesDetection(
                DependencyProblemType problemType, String regex) {
            this.problemType = problemType;
            pattern = Pattern.compile(regex);
        }

        private DependencyProblemType getProblemType() {
            return problemType;
        }

        public static DependencyProblemType matchAny(String line) {
            for (DependencyProblemTypesDetection problem : DependencyProblemTypesDetection
                    .values()) {
                if (problem.pattern.matcher(line).matches()) {
                    return problem.getProblemType();
                }
            }
            return null;
        }
    };

    public static MavenDependencyAnalysisResult parseDependencyAnalyzeSection(String content)
            throws IOException {

        MavenDependencyAnalysisResult result = new MavenDependencyAnalysisResult();
        List<String> lines = IOUtils.readLines(new StringReader(content));

        DependencyProblemType currentProblemType = null;
        for (String line : lines) {
            if (!StringUtils.isBlank(line)) {
                DependencyProblemType problemType = DependencyProblemTypesDetection
                        .matchAny(line);
                if (problemType != null) {
                    currentProblemType = problemType;
                } else {
                    if (currentProblemType != null
                            && ARTIFACT_PATTERN.matcher(line).matches()) {
                        // removing log level
                        String violatingDependency = line.substring(
                                line.lastIndexOf(']') + 1).trim();
                        result.addViolation(currentProblemType,
                                violatingDependency);
                    }
                }
            }
        }

        return result;
    }
}
