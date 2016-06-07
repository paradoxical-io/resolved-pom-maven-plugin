# resolved-pom-maven-plugin

## What

Install with
```
<plugin>
    <groupId>io.paradoxical</groupId>
    <artifactId>resolved-pom-maven-plugin</artifactId>
    <version>1.0</version>
    <configuration>
        <properties>
            <revision>${revision}</revision>
        </properties>
    </configuration>
    <executions>
        <execution>
            <id>resolve-my-pom</id>
            <phase>initialize</phase>
            <goals>
                <goal>resolve-pom</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Why

If you ever wanted to auto version your artifacts,
you may have found [this][Release:DeadAndBurried] article describing what appears to be a simple way of doing so.

The problem comes when you try it, `${revision}` doesn't get replaced in the distributed pom files (the ones up on maven central.)
This causes _huge_ issues with maven transitive dependency resolution. There was a [fix][Release-Pom-Fix] posted a while ago to address some of the problems
However this solution doesn't address when you need to GPG sign your artifacts (like you do to relase to maven central.)

With this plugin, you can use the `${revision}` style versioning and still __easily__ deploy to maven central

[Release:DeadAndBurried]: https://axelfontaine.com/blog/dead-burried.html
[Release-Pom-Fix]: https://axelfontaine.com/blog/maven-releases-steroids-2.html

## Other plugins
After this project was built I found another plugin that does the basically same thing (and in some ways was better!)

- https://github.com/sap-production/resolve-pom-maven-plugin

The only difference is that it replaces every property in the pom with it's filtered versions
where as this plugin uses a white-listed approach.

## Maven Internals

I spent a __good__ amount of time looking through maven plugins and
internals to figure out the easiest way to trick maven into doing what I want :)

Here is some of the resources i used:

### Internals
-  [Filtering]: Understanding what filtering is doing

[Filtering]: https://github.com/finalist/Maven-Filtering/blob/master/src/main/java/org/apache/maven/shared/filtering/DefaultMavenFileFilter.java


### Plugins
- [Resources][Resources-Mojo]: useful as a how to do filtering reference
- [Install][Install-Mojo]: this is where poms are installed by default, also useful for the stupid lastModified check they do on files
- [Gpg:Sign][GpgSign-Mojo]: Useful to figure out what order signatures are added
- [Nexus:Deploy][Nexus:Deploy-Mojo]: If you want to understand that crazy that is the nexus deploy plugin

[Resources-Mojo]: https://github.com/apache/maven-plugins/blob/trunk/maven-resources-plugin/src/main/java/org/apache/maven/plugins/resources/ResourcesMojo.java
[Install-Mojo]: https://github.com/apache/maven-plugins/blob/trunk/maven-install-plugin/src/main/java/org/apache/maven/plugin/install/InstallMojo.java
[GpgSign-Mojo]: https://github.com/apache/maven-plugins/blob/trunk/maven-gpg-plugin/src/main/java/org/apache/maven/plugin/gpg/GpgSignAttachedMojo.java
[Nexus:Deploy-Mojo]: https://github.com/sonatype/nexus-maven-plugins/blob/master/staging/maven-plugin/src/main/java/org/sonatype/nexus/maven/staging/deploy/DeployMojo.java

### Misc

- Nexus staging plugin options: https://github.com/sonatype/nexus-maven-plugins/tree/master/staging/maven-plugin
- Maven plugin execution order: http://www.mkyong.com/maven/maven-plugin-execution-order-in-same-phase/
- Plexus utils: the deep dark stuff of maven. https://github.com/codehaus-plexus/plexus-utils/blob/master/src/main/java/org/codehaus/plexus/util/FileUtils.java

