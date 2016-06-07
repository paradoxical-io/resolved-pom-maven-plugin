package io.paradoxical;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFileFilterRequest;
import org.apache.maven.shared.filtering.MavenFilteringException;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

@Mojo(name = "resolve-pom", defaultPhase = LifecyclePhase.INITIALIZE)
public class ResolvePomMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.artifact}", readonly = true, required = true)
    private Artifact artifact;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.file}", required = true, readonly = true)
    private File pomFile;

    @Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}")
    protected String encoding;

    @Parameter(defaultValue = "${project.build.directory}/resolved-pom.xml", required = true)
    private File resolvedPomFile;

    @Parameter(required = true)
    private Map<String, String> properties;

    @Component(role = MavenFileFilter.class, hint = "default")
    private MavenFileFilter fileFilter;

    @Component
    private MavenProjectHelper projectHelper;

    public void execute() throws MojoExecutionException {

        final Properties additionalProperties = new Properties();
        final boolean enableFiltering = true;

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            additionalProperties.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
        }

        getLog().info(String.format("Filtering pom at %s to %s", pomFile, resolvedPomFile));

        final File outputDirectory = resolvedPomFile.getParentFile();
        final boolean createdOutputDirectory = outputDirectory.mkdirs();

        getLog().debug(String.format("Ensure output directory %s : %s", outputDirectory.getName(), createdOutputDirectory));

        try {
            final boolean createdResolvedPomFile = resolvedPomFile.createNewFile();
            getLog().debug(String.format("Ensure output file %s : %s", resolvedPomFile.getName(), createdResolvedPomFile));
        }
        catch (Exception e) {
            throw new MojoExecutionException("Error creating output file", e);
        }

        final MavenFileFilterRequest mavenFileFilterRequest = new MavenFileFilterRequest(
            pomFile, // from
            resolvedPomFile, // to
            enableFiltering, // filtering
            null, // mavenProject - setting null means not to pull in project properties :)
            new ArrayList<String>(), // filter .properties files to use
            false, // escapedBackslashesInFilePath? not sure...
            encoding, // encoding - Should probably set this to project.build.sourceEncoding
            null, // mavenSession - setting to null means no session properties :)
            additionalProperties // additionalProperties
        );

        try {
            fileFilter.copyFile(mavenFileFilterRequest);
        }
        catch (MavenFilteringException e) {
            getLog().error("Failed to create filtered pom");
            throw new MojoExecutionException("Failed to create a filtered pom", e);
        }

        getLog().info(String.format("Filtered pom to %s", resolvedPomFile));

        artifact.addMetadata(new ResolvedProjectArtifactMetadata(artifact, resolvedPomFile));

        project.setFile(resolvedPomFile);

        // Not needed after the above method was found
        //        projectHelper.attachArtifact(project, "pom", resolvedPomFile);
    }

    private class ResolvedProjectArtifactMetadata extends ProjectArtifactMetadata {

        public ResolvedProjectArtifactMetadata(final Artifact artifact, final File file) {
            super(artifact, file);
        }

        @Override
        public void merge(final org.apache.maven.artifact.metadata.ArtifactMetadata metadata) {
            // don't do anything
            getLog().info("Got a request to merge metadata for: " + metadata.getKey() + " " + metadata.getRemoteFilename());
        }
    }
}
