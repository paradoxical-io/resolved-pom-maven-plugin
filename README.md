# resolved-pom-maven-plugin

## What

Install with
```
<plugin>
    <groupId>io.paradoxical</groupId>
    <artifactId>resolved-pom-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <properties>
            <revision>${revision}</revision>
        </properties>
    </configuration>
    <executions>
        <execution>
            <id>resolve-my-pom</id>
            <phase>validate</phase>
            <goals>
                <goal>resolve-pom</goal>
            </goals>
        </execution>
        <execution>
            <!-- If you're gpg signing artifacts -->
            <id>touch-pom-signature</id>
            <phase>install</phase>
            <goals>
                <goal>touch-signature</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Why