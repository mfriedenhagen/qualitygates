package de.binarytree.plugins.qualitygates.steps.dependencycheck.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * Cut the log file in sections for each maven goal.
 * 
 * @author Vincent Sellier
 * 
 */
public class BuildLogFileParser {
    public static final Logger LOGGER = Logger.getLogger(BuildLogFileParser.class.getName());

    private static final String LOG_LEVEL_REGEX = "^\\s*(\\[)?(INFO|WARNING)(\\]|:)? ";

    private static final Pattern GOAL_START = Pattern.compile(LOG_LEVEL_REGEX + "---.*");

    private static final Pattern END_OF_BUILD = Pattern.compile(LOG_LEVEL_REGEX + "[-]*$");

    // To limit selection to maven output (filtering [HUDSON] tags)
    private static final Pattern MAVEN_OUTPUT = Pattern.compile(LOG_LEVEL_REGEX + ".*");

    private static final Pattern BANNED_OUTPUT = Pattern.compile("Found Banned Dependency:.*");

    /**
     * This enumeration defines regex to match the start lines for maven log sections to be parsed.
     * 
     * @author Marcel von Maltitz
     * 
     */
    public enum Goal {
        DEPENDENCY_ANALYSE(LOG_LEVEL_REGEX + "--- maven-dependency-plugin:[^:]+:analyze(-only| ).*", MAVEN_OUTPUT), BANNED_DEPENDENCY_ANALYSE(
                LOG_LEVEL_REGEX + "--- maven-enforcer-plugin:[^:]+:enforce.*", BANNED_OUTPUT);

        private Pattern pattern;

        private Pattern linePattern;

        private Goal(String regex, Pattern linePattern) {
            pattern = Pattern.compile(regex);
            this.linePattern = linePattern;
        }

        public static Goal getMatchingGoal(String line) {
            Goal[] goals = Goal.values();

            for (Goal goal : goals) {
                Pattern pattern = goal.pattern;
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    return goal;
                }
            }
            return null;
        }
    }

    private boolean parsed = false;

    private Map<Goal, String> goalsLog = new HashMap<Goal, String>();

    public void parseLogFile(File logFile) throws IOException {
        LOGGER.fine("Parsing " + logFile.getAbsolutePath());
        FileInputStream input = new FileInputStream(logFile);

        List<String> lines = IOUtils.readLines(input);

        Iterator<String> lineIterator = lines.iterator();

        while (lineIterator.hasNext()) {
            String line = lineIterator.next();
            line = eliminateColorEscapeCodes(line);

            Goal goal = Goal.getMatchingGoal(line);
            if (goal != null) {
                processSectionOfGoal(lineIterator, goal);
            }
        }

        parsed = true;
    }

    private void processSectionOfGoal(Iterator<String> lineIterator, Goal goal) {
        StringBuilder section = new StringBuilder();

        String formerSection = goalsLog.get(goal);
        if (formerSection != null) {
            section.append(formerSection);
        }

        extractSection(lineIterator, goal, section);
        goalsLog.put(goal, section.toString());
    }

    private String eliminateColorEscapeCodes(String line) {
        return line.replaceAll("\u001b\\[8mha:[^=]+==\u001b\\[0m", "");
    }

    private void extractSection(Iterator<String> lineIterator, Goal goal, StringBuilder section) {
        String line;
        // Pass the search section to only keep content of the section
        boolean inSection = true;
        while (lineIterator.hasNext() && inSection) {
            line = lineIterator.next();
            line = eliminateColorEscapeCodes(line);
            if (GOAL_START.matcher(line).matches() || END_OF_BUILD.matcher(line).matches()) {
                inSection = false;
            } else {
                if (goal.linePattern.matcher(line).matches()) {
                    section.append(line).append("\n");
                }
            }

        }
    }

    public String getContentOfSectionFor(Goal goal) {
        if (!parsed) {
            throw new IllegalStateException("No log file was parsed");
        }

        return goalsLog.get(goal);
    }

}
