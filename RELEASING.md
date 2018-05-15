# How to Release

[Dokka](https://github.com/Kotlin/dokka) is used for documentation, and the 
[Bintray Gradle Plugin](https://github.com/bintray/gradle-bintray-plugin) 
is used for uploading to Maven Central and JCenter.

## Setup

Add the following variables with the correct values to either your gradle home 
`gradle.properties` file, or to your system environment variables.

* `BINTRAY_USER`
* `BINTRAY_API_KEY`
* `BINTRAY_GPG_PASSWORD`
* `SONATYPE_USER`
* `SONATYPE_PASSWORD` 

## Steps

1. Bump the version number in the root `build.gradle` file 
2. Run `./gradlew dokka` to update the documentation 
3. Run `./gradlew bintrayUpload`