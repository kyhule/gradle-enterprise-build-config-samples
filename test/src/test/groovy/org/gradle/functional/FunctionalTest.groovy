package org.gradle.functional

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class FunctionalTest extends Specification {

    @Rule
    TemporaryFolder testProjectDir

    private File settingsFile
    private File buildFile

    @Shared
    private String snippetsDir = new File('..').absolutePath

    def setup() {
        settingsFile = testProjectDir.newFile('settings.gradle')
        buildFile = testProjectDir.newFile('build.gradle')
    }

    @Unroll
    def "snippet #snippetTitle can be configured"() {
        given:
        settingsFile()
        buildFileWithAppliedSnippet(snippet)

        when:
        def result = build('help', '--no-scan')

        then:
        result.task(':help').outcome == SUCCESS

        where:
        snippetTitle               | snippet
        'basic-publishing'         | scriptSnippet('basic-publishing.gradle')
        'capture-task-input-files' | scriptSnippet('capture-task-input-files.gradle')
        'tags-android'             | scriptSnippet('tags-android.gradle')
        'tags-basic'               | scriptSnippet('tags-basic.gradle')
    }

    @Unroll
    def "snippet #snippetTitle, which does background work, can be configured"() {
        given:
        settingsFile()
        buildFileWithAppliedSnippet(snippet)
        initGitRepo()

        when:
        def result = build('help', '-DgeServer=https://scans.gradle.com')

        then:
        result.task(':help').outcome == SUCCESS
        !result.output.contains('Build scan background action failed')

        where:
        snippetTitle | snippet
        'git'        | scriptSnippet('git.gradle')
        'gist'       | scriptSnippet('gist.gradle')
    }

    @Requires({ !System.getProperty('gistToken').isEmpty() })
    def "gists can be published"() {
        given:
        settingsFile()
        buildFileWithAppliedSnippet(scriptSnippet('gist.gradle'))
        initGitRepo()

        and: 'A modified file so there is a diff when generating a gist'
        buildFile << "\n\n"

        when:
        def result = build('help', "-PgistToken=${System.getProperty('gistToken')}")

        then:
        result.task(':help').outcome == SUCCESS
        !result.output.contains("User has not set 'gistToken'. Cannot publish gist.")
        !result.output.contains("Unable to publish diff to Gist")
    }

    private File settingsFile() {
        settingsFile << "rootProject.name = 'scan-snippets-test'"
    }

    private File buildFileWithAppliedSnippet(def snippet) {
        buildFile << """
        plugins {
            id 'com.gradle.build-scan' version '2.2.1'
        }
        
        apply from: '$snippet'
        """
    }

    private def scriptSnippet(String snippetName) {
        return new File(snippetsDir, snippetName).toString()
    }

    private void initGitRepo() {
        executeCommandInTempDir("git init")
        executeCommandInTempDir("git config user.email 'dev-null@gradle.com'")
        executeCommandInTempDir("git config user.name 'Dev Null'")
        executeCommandInTempDir("git add .")
        executeCommandInTempDir("git commit -m 'Hello'")
    }

    private void executeCommandInTempDir(String command) {
        command.execute([], testProjectDir.root).with {
            waitFor()
            println "$command exit value: ${exitValue()}"
        }
    }

    private BuildResult build(String... args) {
        return gradleRunnerFor(*args).build()
    }

    private GradleRunner gradleRunnerFor(String... args) {
        return GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments(*args + '--stacktrace')
            .forwardOutput()
    }
}