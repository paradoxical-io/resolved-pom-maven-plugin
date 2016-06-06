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
import org.apache.maven.repository.legacy.metadata.ArtifactMetadata;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFileFilterRequest;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

@Mojo(name = "resolve-pom", defaultPhase = LifecyclePhase.VALIDATE)
public class ResolvePomMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.artifact}", readonly = true, required = true)
    private Artifact artifact;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.file}", required = true, readonly = true)
    private File pomFile;

    @Parameter(defaultValue = "${project.build.directory}/resolved-pom/pom.xml", required = true, readonly = false)
    private File resolvedPomFile;

    @Parameter(required = true)
    private Map<String, String> properties;

    @Component(role = MavenResourcesFiltering.class, hint = "default")
    private MavenResourcesFiltering resourcesFiltering;

    @Component(role = MavenFileFilter.class, hint = "default")
    private MavenFileFilter fileFilter;

    @Component
    private MavenProjectHelper projectHelper;

    public void execute()
        throws MojoExecutionException {
        //        ( File from, File to, boolean filtering, MavenProject mavenProject,
        //        List<String> filters, boolean escapedBackslashesInFilePath, String encoding,
        //        MavenSession mavenSession, Properties additionalProperties

        final Properties additionalProperties = new Properties();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            additionalProperties.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
        }

        resolvedPomFile.getParentFile().mkdirs();

        final MavenFileFilterRequest mavenFileFilterRequest = new MavenFileFilterRequest(
            pomFile, // from
            resolvedPomFile, // to
            true, // filtering
            null, // mavenProject
            new ArrayList<String>(), // filters
            false, // escapedBackslashesInFilePath?
            null, // encoding -!!!!
            null, // mavenSession
            additionalProperties // additionalProperties
        );

        try {
            fileFilter.copyFile(mavenFileFilterRequest);
        }
        catch (MavenFilteringException e) {
            getLog().error("Failed to create filtered pom");
            throw new MojoExecutionException("Failed to create a filtred pom", e);
        }

        artifact.addMetadata(new ProjectArtifactMetadata(artifact, resolvedPomFile) {
            @Override
            public Object getKey() {
                return super.getKey();
            }

            @Override
            public void merge(final org.apache.maven.artifact.metadata.ArtifactMetadata metadata) {
                // don't do anything
                getLog().info("Go a request to merge metadata for: " + metadata.getKey() + " " + metadata.getRemoteFilename());
            }
        });

        for (ArtifactMetadata metadata : artifact.getMetadataList()) {
            getLog().info(String.format("metadata: %s - %s", metadata.getKey(), metadata.getClass().getSimpleName()));
        }

        projectHelper.attachArtifact(project, "pom", resolvedPomFile);
    }
}
