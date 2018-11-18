package io.micronaut.cli.config.profiles

import io.micronaut.cli.config.dependencies.Dependency
import io.micronaut.cli.config.features.AbstractFeature
import io.micronaut.cli.config.features.AnnotationApi
import io.micronaut.cli.config.features.Java
import io.micronaut.cli.profile.AbstractProfile


class Base extends AbstractProfile {
    @Override
    String getName() {
        "base"
    }

    @Override
    String getDescription() {
        "The base profile"
    }

    @Override
    Set<AbstractFeature> getDefaultFeatures() {
        [new Java()]
    }

    @Override
    Set<AbstractFeature> getRequiredFeatures() {
        [new AnnotationApi()]
    }

    @Override
    List<String> getRepositories() {
        [
                "mavenLocal()",
                "mavenCentral()"
        ]
    }

    @Override
    List<String> getJvmArgs() {
        [
                "-noverify",
                "-XX:TieredStopAtLevel=1"
        ]
    }

    @Override
    List<String> getBuildPlugins() {
        [
                "io.spring.dependency-management:1.0.6.RELEASE",
                "com.github.johnrengelman.shadow:4.0.0", "application"
        ]
    }

    @Override
    List<Dependency> getDependencies() {
        [new Dependency("runtime", "ch.qos.logback:logback-classic:1.2.3")]
    }
}
