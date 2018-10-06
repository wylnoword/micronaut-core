package io.micronaut.cli.config.features

class Java extends AbstractFeature {

    @Override
    List<String> getBuildPlugins() {
        ["java",
         "net.ltgt.apt-eclipse:0.18",
         "net.ltgt.apt-idea:0.18"]
    }
}
