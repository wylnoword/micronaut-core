package io.micronaut.cli.config.profiles

import io.micronaut.cli.profile.AbstractProfile

class Federation extends AbstractProfile {
    @Override
    String getName() {
        "federation"
    }
}
