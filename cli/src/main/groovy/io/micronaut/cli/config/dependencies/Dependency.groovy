package io.micronaut.cli.config.dependencies

class Dependency {

    Dependency(String scope, String coords) {
        this.scope = scope
        this.coords = coords
    }

    String scope
    String coords
}
