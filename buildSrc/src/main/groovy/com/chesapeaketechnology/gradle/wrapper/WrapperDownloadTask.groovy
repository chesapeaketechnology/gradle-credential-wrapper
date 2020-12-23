package com.chesapeaketechnology.gradle.wrapper

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class WrapperDownloadTask extends DefaultTask {
    String gradleVersion = ""
    String gradleDownloadBase = 'https://services.gradle.org/distributions'
    File destinationDir

    @Override
    String toString() {
        return super.toString()
    }

    @TaskAction
    def downloadWrapper() {
        println "Downloading gradle version $gradleVersion..."
        destinationFile.bytes = new URL(downloadUrl).bytes
    }

    @OutputFile
    File getDestinationFile() {
        new File(destinationDir, downloadFileName)
    }

    String getDistributionNameBase() {
        "gradle-$gradleVersion"
    }

    String getDownloadFileName() {
        "$distributionNameBase-bin.zip"
    }

    String getDownloadUrl() {
        "$gradleDownloadBase/$downloadFileName"
    }
}
