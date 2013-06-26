package de.binarytree.plugins.qualitygates.checks.dependencyanalyzer;

import hudson.model.Action;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.util.logging.Logger;

import de.binarytree.plugins.qualitygates.checks.dependencyanalyzer.persistence.BuildResultSerializer;
import de.binarytree.plugins.qualitygates.checks.dependencyanalyzer.result.BuildResult;

public class DependencyAnalyzerPublisherAction implements Action {
	private final static Logger LOGGER = Logger
			.getLogger(DependencyAnalyzerPublisherAction.class.getName());

	private AbstractBuild<?, ?> build;
	// This field is marked transient to avoid build descriptor pollution
	private transient BuildResult analysis;

	public DependencyAnalyzerPublisherAction() {
		super();
	}

	public DependencyAnalyzerPublisherAction(AbstractBuild<?, ?> build,
			BuildResult analysis) {
		this.build = build;
		this.analysis = analysis;
	}

	public String getDisplayName() {
		return "";
	}

	public String getIconFileName() {
		return Const.ICON_URL;
	}

	public String getUrlName() {
		return Const.MODULE_URL;
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}

	public BuildResult getBuildResult() {
		if (analysis == null) {
			// we are consulting a previous build, getting result from file
			try {
				analysis = BuildResultSerializer.deserialize(build.getRootDir());
			} catch (IOException e) {
				LOGGER.severe("Error getting result from disk");
			}
		}
		return analysis;
	}
}
