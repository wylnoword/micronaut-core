package io.micronaut.cli.config.features

import io.micronaut.cli.config.dependencies.Dependency
import io.micronaut.cli.profile.Feature

class AbstractFeature {

    String name = this.class.name.toLowerCase()

    abstract String description

    abstract Set<Feature> dependentFeatures

    abstract List<String> buildPlugins
    abstract Set<Dependency> buildDependencies

    abstract Set<Dependency> dependencies
}
