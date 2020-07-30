package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.MavenBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, create a buildType with id = 'VerifyTestFrancois'
in the root project, and delete the patch script.
*/
create(DslContext.projectId, BuildType({
    id("VerifyTestFrancois")
    name = "Verify_test-francois"

    vcs {
        root(DslContext.settingsRoot)

        cleanCheckout = true
    }

    steps {
        maven {
            goals = "verify"
            pomLocation = "common-custom-user-data-maven-extension/pom.xml"
            runnerArgs = "-Dgradle.enterprise.url=https://e.grdev.net -Dgradle.enterprise.storage.directory=%system.teamcity.build.tempDir%/.gradle-enterprise"
            mavenVersion = custom {
                path = "%teamcity.tool.maven.3.6.3%"
            }
            userSettingsSelection = "settings.xml"
            localRepoScope = MavenBuildStep.RepositoryScope.BUILD_CONFIGURATION
            jdkHome = "%linux.java8.oracle.64bit%"
        }
    }

    triggers {
        vcs {
            branchFilter = "+:<default>"
        }
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }
}))

