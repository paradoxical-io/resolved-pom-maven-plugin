package io.paradoxical;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.repository.legacy.metadata.ArtifactMetadata;

import java.io.File;

@SuppressWarnings("unused")
@Mojo(name = "touch-signature", defaultPhase = LifecyclePhase.INSTALL)
public class TouchSigPomMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.artifact}", readonly = true, required = true)
    private Artifact artifact;

    @Parameter(defaultValue = "${project.build.directory}/gpg/target/resolved-pom/pom.xml.asc", required = true, readonly = false)
    private File resolvedPomFileSig;

    public void execute() throws MojoExecutionException, MojoFailureException {
        for (ArtifactMetadata metadata : artifact.getMetadataList()) {
            getLog().info(String.format("metadata: %s - %s", metadata.getKey(), metadata.getClass().getName()));
        }

        final long lastModified = resolvedPomFileSig.lastModified();

        final boolean setLastModified = resolvedPomFileSig.setLastModified(lastModified + 1000);

        if(setLastModified) {
            getLog().info("update last modified time to: " + resolvedPomFileSig.lastModified());
        }
        else {
            getLog().warn("unable to set last modified time");
        }
    }
}
