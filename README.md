[![License](https://img.shields.io/badge/License-EPL%202.0-red.svg?label=license&logo=eclipse)](https://www.eclipse.org/legal/epl-2.0/)
[![Build Status](https://ci.eclipse.org/codewind/buildStatus/icon?job=Codewind%2Fcodewind-intellij%2Fmaster)](https://ci.eclipse.org/codewind/job/Codewind/job/codewind-intellij/job/master/)
[![Chat](https://img.shields.io/static/v1.svg?label=chat&message=mattermost&color=145dbf)](https://mattermost.eclipse.org/eclipse/channels/eclipse-codewind)

# Codewind for IntelliJ
Create and develop cloud-native, containerized web applications from IntelliJ

## Installing Codewind

Prerequisites
- Install [IntelliJ Community Edition](https://www.jetbrains.com/idea/download/).
- Install Docker.
- If you use Linux, you also need to install Docker Compose.

## Contributing
Submit issues and contributions:
- [Submitting issues](https://github.com/eclipse/codewind/issues)
- [Contributing](CONTRIBUTING.md)

## Developing

1. Download IntelliJ Community Edition from https://www.jetbrains.com/idea/download/
2. Clone this repository and run a gradle build
```
git clone https://github.com/eclipse/codewind-intellij
cd codewind-intellij/dev/
src/main/resources/cwctl/meta-pull.sh
src/main/resources/cwctl/pull.sh
./gradlew copyDependencies build
```
- If running the gradlew step, you get an error message indicating that the `org.jetbrains.intellij` plugin cannot be found, check if your environment is configured to use an IBM JDK.  If so, configure it to use an Oracle JDK or use the one provided by the IntelliJ installation.  
3. Import the `codewind-intellij/dev` folder into IntelliJ, selecting `Import project from external model` and choosing the `Gradle` option (see [Import a project](https://www.jetbrains.com/help/idea/import-project-or-module-wizard.html) in the IntelliJ help)

Use the `dev (latest) [runIde]` run configuration to run and test your changes.

To build a plugin .zip file which can be installed into IntelliJ:
```
git clone https://github.com/eclipse/codewind-intellij
cd codewind-intellij/dev/
src/main/resources/cwctl/meta-pull.sh
src/main/resources/cwctl/pull.sh
./gradlew copyDependencies buildPlugin
```
The built plugin .zip file will be found in the `codewind-intellij/dev/build/distributions/` folder.
