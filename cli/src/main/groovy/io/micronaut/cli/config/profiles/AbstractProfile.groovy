package io.micronaut.cli.config.profiles

import io.micronaut.cli.config.dependencies.Dependency
import io.micronaut.cli.config.features.AbstractFeature

abstract class AbstractProfile {

    String name = this.class.name.toLowerCase()
    
    abstract String description

    abstract Set<AbstractFeature> defaultFeatures
    abstract Set<AbstractFeature> requiredFeatures
    abstract Set<AbstractFeature> oneOfFeatures

    abstract List<String> repositories

    abstract  List<String> skeletonExecutables
    abstract  List<String> skeletonBinaryExtensions

    abstract  List<String> jvmArgs

    abstract List<String> buildPlugins
    abstract List<String> buildRepositories

    abstract Set<Dependency> dependencies
}
