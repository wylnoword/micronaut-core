/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.cli.profile

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeCheckingMode
import io.micronaut.cli.interactive.completers.StringsCompleter
import io.micronaut.cli.io.IOUtils
import io.micronaut.cli.io.support.Resource
import io.micronaut.cli.profile.commands.CommandRegistry

import io.micronaut.cli.profile.commands.PicocliCompleter
import io.micronaut.cli.util.CliSettings
import io.micronaut.cli.util.VersionInfo
import jline.console.completer.Completer
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector
import picocli.CommandLine

/**
 * Abstract implementation of the profile class
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
@ToString(includes = ['name'])
abstract class AbstractProfile implements Profile {
    protected final Resource profileDir
    protected String name
    protected List<Profile> parentProfiles
    protected Map<String, Command> commandsByName
    protected ProfileRepository profileRepository
    protected List<Dependency> dependencies = []
    protected List<String> repositories = []
    protected List<String> jvmArgs = []
    protected List<String> parentNames = []
    protected List<String> buildRepositories = []
    protected List<String> buildPlugins = []
    protected List<String> buildExcludes = []
    protected List<String> skeletonExcludes = []
    protected List<String> binaryExtensions = []
    protected List<String> executablePatterns = []
    protected final List<Command> internalCommands = []
    protected List<String> buildMerge = null
    protected List<Feature> features = []
    protected Set<String> defaultFeaturesNames = []
    protected Set<String> requiredFeatureNames = []
    protected List<OneOfFeatureGroup> oneOfFeatureGroups = []
    protected String parentTargetFolder
    protected final ClassLoader classLoader
    protected ExclusionDependencySelector exclusionDependencySelector = new ExclusionDependencySelector()
    protected String description = ""
    protected String instructions = ""
    protected String version = VersionInfo.getVersion(CliSettings)
    protected Boolean abstractProfile = false


    AbstractProfile() {

    }

    AbstractProfile(Resource profileDir) {
        this(profileDir, AbstractProfile.getClassLoader())
    }

    AbstractProfile(Resource profileDir, ClassLoader classLoader) {
        this.classLoader = classLoader
        this.profileDir = profileDir


        def url = profileDir.getURL()
        def jarFile = IOUtils.findJarFile(url)
        def pattern = ~/.+-(\d.+)\.jar/


        def path
        if (jarFile != null) {
            path = jarFile.name
        } else if (url != null) {
            def p = url.path
            path = p.substring(0, p.indexOf('.jar') + 4)
        }
        if (path) {
            def matcher = pattern.matcher(path)
            if (matcher.matches()) {
                this.version = matcher.group(1)
            }
        }
    }

    String getVersion() {
        return version
    }

    boolean isAbstract() {
        abstractProfile
    }

    String getDescription() {
        description
    }

    String getInstructions() {
        instructions
    }

    Set<String> getBinaryExtensions() {
        Set<String> calculatedBinaryExtensions = []
        def parents = getExtends()
        for (profile in parents) {
            calculatedBinaryExtensions.addAll(profile.binaryExtensions)
        }
        calculatedBinaryExtensions.addAll(binaryExtensions)
        return calculatedBinaryExtensions
    }

    Set<String> getExecutablePatterns() {
        Set<String> calculatedExecutablePatterns = []
        def parents = getExtends()
        for (profile in parents) {
            calculatedExecutablePatterns.addAll(profile.executablePatterns)
        }
        calculatedExecutablePatterns.addAll(executablePatterns)
        return calculatedExecutablePatterns
    }

    @Override
    Iterable<Feature> getDefaultFeatures() {
        getFeatures().findAll() { Feature f -> defaultFeaturesNames.contains(f.name) }
    }

    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    Iterable<OneOfFeatureGroup> getOneOfFeatures() {
        List<Feature> features = getFeatures().toList()

        oneOfFeatureGroups.each {
            it.initialize(features)
        }

        oneOfFeatureGroups
    }

    @Override
    Iterable<Feature> getRequiredFeatures() {
        def requiredFeatureInstances = getFeatures().findAll() { Feature f -> requiredFeatureNames.contains(f.name) }
        if (requiredFeatureInstances.size() != requiredFeatureNames.size()) {
            throw new IllegalStateException("One or more required features were not found on the classpath. Required features: $requiredFeatureNames")
        }
        return requiredFeatureInstances
    }

    @Override
    Iterable<Feature> getFeatures() {
        Set<Feature> calculatedFeatures = []
        calculatedFeatures.addAll(features)
        def parents = getExtends()
        for (profile in parents) {
            calculatedFeatures.addAll profile.features
        }
        return calculatedFeatures
    }

    @Override
    List<String> getBuildMergeProfileNames() {
        if (buildMerge != null) {
            return this.buildMerge
        } else {
            List<String> mergeNames = []
            for (parent in getExtends()) {
                mergeNames.add(parent.name)
            }
            mergeNames.add(name)
            return mergeNames
        }
    }

    @Override
    List<String> getBuildRepositories() {
        List<String> calculatedRepositories = []
        if (buildRepositories.empty) {
            def parents = getExtends()
            for (profile in parents) {
                calculatedRepositories.addAll(profile.buildRepositories)
            }
        } else {
            calculatedRepositories.addAll(buildRepositories)
        }
        return calculatedRepositories
    }

    @Override
    List<String> getBuildPlugins() {
        List<String> calculatedPlugins = []
        def parents = getExtends()
        for (profile in parents) {
            def dependencies = profile.buildPlugins
            for (dep in dependencies) {
                if (!buildExcludes.contains(dep))
                    calculatedPlugins.add(dep)
            }
        }
        calculatedPlugins.addAll(buildPlugins)
        return calculatedPlugins
    }

    @Override
    List<String> getRepositories() {
        List<String> calculatedRepositories = []
        if (repositories.empty) {
            def parents = getExtends()
            for (profile in parents) {
                calculatedRepositories.addAll(profile.repositories)
            }
        } else {
            calculatedRepositories.addAll(repositories)
        }
        return calculatedRepositories
    }


    @Override
    List<String> getJvmArgs() {
        jvmArgs
    }

    List<Dependency> getDependencies() {
        List<Dependency> calculatedDependencies = []
        def parents = getExtends()
        for (profile in parents) {
            def dependencies = profile.dependencies
            for (dep in dependencies) {
                if (exclusionDependencySelector.selectDependency(dep)) {
                    calculatedDependencies.add(dep)
                }
            }
        }
        calculatedDependencies.addAll(dependencies)
        return calculatedDependencies
    }

    ProfileRepository getProfileRepository() {
        return profileRepository
    }

    void setProfileRepository(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository
    }

    Resource getProfileDir() {
        return profileDir
    }

    @Override
    Resource getTemplate(String path) {
        return profileDir.createRelative("templates/$path")
    }

    @Override
    Iterable<Profile> getExtends() {
        return parentNames.collect() { String name ->
            def parent = profileRepository.getProfile(name, true)
            if (parent == null) {
                throw new IllegalStateException("Profile [$name] declares an invalid dependency on parent profile [$name]")
            }
            return parent
        }
    }

    @Override
    Iterable<Completer> getCompleters(ProjectContext context) {
        def commands = getCommands(context)

        Collection<Completer> completers = []

        for (Command cmd in commands) {
            completers << new StringsCompleter(cmd.name) // commandNameCompleter
            completers << new PicocliCompleter(cmd.commandSpec)
        }

        return completers
    }

    @Override
    Command getCommand(ProjectContext context, String name) {
        getCommands(context)
        return commandsByName[name]
    }

    @Override
    Iterable<Command> getCommands(ProjectContext context) {
        if (commandsByName == null) {
            commandsByName = [:]
            List excludes = []
            def registerCommand = { Command command ->
                new CommandLine(command) // initialize @Spec
                def name = command.name
                if (!commandsByName.containsKey(name) && !excludes.contains(name)) {
                    if (command instanceof ProfileRepositoryAware) {
                        ((ProfileRepositoryAware) command).setProfileRepository(profileRepository)
                    }
                    commandsByName[name] = command
                    def desc = command.commandSpec
                    def synonyms = desc?.aliases()
                    if (synonyms) {
                        for (syn in synonyms) {
                            commandsByName[syn] = command
                        }
                    }
                    if (command instanceof ProjectContextAware) {
                        ((ProjectContextAware) command).projectContext = context
                    }
                    if (command instanceof ProfileCommand) {
                        ((ProfileCommand) command).profile = this
                    }
                }
            }

            CommandRegistry.findCommands(this).each(registerCommand)

            def parents = getExtends()
            if (parents) {
                registerParentCommands(context, parents, registerCommand)
            }
        }
        return commandsByName.values()
    }

    protected void registerParentCommands(ProjectContext context, Iterable<Profile> parents, Closure registerCommand) {
        for (parent in parents) {
            parent.getCommands(context).each registerCommand

            def extended = parent.extends
            if (extended) {
                registerParentCommands context, extended, registerCommand
            }
        }
    }

    @Override
    boolean hasCommand(ProjectContext context, String name) {
        getCommands(context) // ensure initialization
        return commandsByName.containsKey(name)
    }

    @Override
    boolean handleCommand(ExecutionContext context) {
        def parseResult = context.parseResult
        while (parseResult.hasSubcommand()) { parseResult = parseResult.subcommand() }
        Command cmd = parseResult.commandSpec().userObject() as Command
        return cmd.handle(context)
    }

    @Override
    String getParentSkeletonDir() {
        this.parentTargetFolder
    }

    @Override
    File getParentSkeletonDir(File parent) {
        if (parentSkeletonDir) {
            new File(parent, parentSkeletonDir)
        } else {
            parent
        }
    }

    List<String> getSkeletonExcludes() {
        this.skeletonExcludes
    }

    List<String> getSkeletonExecutables() {
        ["**/gradlew*", "**/mnw*", "**/mvnw*", "**/build-native-image*"]
    }

    List<String> getSkeletonBinaryExtensions() {
        ['png','gif','jpg','jpeg','ico','icns','pdf','zip','jar','class']
    }
}
