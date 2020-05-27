#!groovy

pipeline {
    agent any
    
    tools {
        jdk 'oracle-jdk8-latest'
    }
    
    options {
        timestamps() 
        skipStagesAfterUnstable()
    }

    triggers {
        upstream(upstreamProjects: "Codewind/codewind-eclipse/${env.BRANCH_NAME}", threshold: hudson.model.Result.SUCCESS)
    }

    parameters {
        string(name: "APPSODY_VERSION", defaultValue: "0.6.2", description: "Appsody executable version to download")
    }

    stages {

        stage("Download dependency binaries") {
            steps {
                dir("dev/src/main/resources/cwctl") {
                    sh """#!/usr/bin/env bash
                        export VSCODE_REPO="https://github.com/eclipse/codewind-vscode.git"
                        export CW_VSCODE_BRANCH=master

                        # the command below will echo the head commit if the branch exists, else it just exits
                        if [[ -n \$(git ls-remote --heads \$VSCODE_REPO ${env.BRANCH_NAME}) ]]; then
                            echo "Will pull scripts from  matching ${env.BRANCH_NAME} branch on \$VSCODE_REPO"
                            export CW_VSCODE_BRANCH=${env.BRANCH_NAME}
                        else
                            echo "Will pull scripts from \$CW_VSCODE_BRANCH branch on \$VSCODE_REPO - no matching branch"
                        fi

                        export INSTALLER_REPO="https://github.com/eclipse/codewind-installer.git"
                        export CW_CLI_BRANCH=master

                        # the command below will echo the head commit if the branch exists, else it just exits
                        if [[ -n \$(git ls-remote --heads \$INSTALLER_REPO ${env.BRANCH_NAME}) ]]; then
                            echo "Will pull binaries from  matching ${env.BRANCH_NAME} branch on \$INSTALLER_REPO"
                            export CW_CLI_BRANCH=${env.BRANCH_NAME}
                        else
                            echo "Will pull binaries from \$CW_CLI_BRANCH branch on \$INSTALLER_REPO - no matching branch"
                        fi

                        export APPSODY_VERSION=${params.APPSODY_VERSION}
                        ./meta-pull.sh 
                        ./pull.sh
                    """
                }
                dir("dev") {
                    sh """#!/usr/bin/env bash
                        ./gradlew copyDependencies --stacktrace
                    """
                }
            }
        }

        stage("Create install-version.properties") {
            steps {
                sh """
                   ci-scripts/set-install-version.sh ${env.BRANCH_NAME} dev/src/main/resources
                """
            }
        }

        stage('Build') {
            options {
                timeout(time: 30, unit: 'MINUTES') 
            }

            steps {
                script {
                    println("Starting codewind-intellij build ...")
                        
                    def sys_info = sh(script: "uname -a", returnStdout: true).trim()
                    println("System information: ${sys_info}")
                    println("JAVE_HOME: ${JAVA_HOME}")
                    
                    sh '''
                        java -version
                        which java    
                    '''
                    
                    dir('dev') { sh './gradlew buildPlugin --stacktrace' }
                }
            }
        } 
        
        stage('Deploy') {
            steps {
                sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
                    println("Deploying codewind-intellij to download area...")
                  
                    sh '''
                        export REPO_NAME="codewind-intellij"
                        export OUTPUT_NAME="codewind-intellij"
                        export OUTPUT_DIR="$WORKSPACE/dev/build/distributions"
                        export DOWNLOAD_AREA_URL="https://archive.eclipse.org/codewind/$REPO_NAME"
                        export LATEST_DIR="latest"
                        export BUILD_INFO="build_info.properties"
                        export UPDATE_PLUGINS_XML="updatePlugins.xml"
                        export sshHost="genie.codewind@projects-storage.eclipse.org"
                        export deployDir="/home/data/httpd/archive.eclipse.org/codewind/$REPO_NAME"
                        export TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
                    
                        if [ -z $CHANGE_ID ]; then
                            UPLOAD_DIR="$GIT_BRANCH/$BUILD_ID"
                            BUILD_URL="$DOWNLOAD_AREA_URL/$UPLOAD_DIR"
                  
                            ssh $sshHost rm -rf $deployDir/$GIT_BRANCH/$LATEST_DIR
                            ssh $sshHost mkdir -p $deployDir/$GIT_BRANCH/$LATEST_DIR
                            
                            cp $OUTPUT_DIR/$OUTPUT_NAME*.zip $OUTPUT_DIR/$OUTPUT_NAME.zip
                            scp $OUTPUT_DIR/$OUTPUT_NAME.zip $sshHost:$deployDir/$GIT_BRANCH/$LATEST_DIR/$OUTPUT_NAME.zip
                            
                            echo "# Build date: $TIMESTAMP" >> $OUTPUT_DIR/$BUILD_INFO
                            echo "build_info.url=$BUILD_URL" >> $OUTPUT_DIR/$BUILD_INFO
                            SHA1=$(sha1sum ${OUTPUT_DIR}/${OUTPUT_NAME}.zip | cut -d ' ' -f 1)
                            echo "build_info.SHA-1=${SHA1}" >> $OUTPUT_DIR/$BUILD_INFO
                            scp $OUTPUT_DIR/$BUILD_INFO $sshHost:$deployDir/$GIT_BRANCH/$LATEST_DIR/$BUILD_INFO

                            echo '<?xml version="1.0" encoding="UTF-8"?>' >> $OUTPUT_DIR/$UPDATE_PLUGINS_XML
                            echo '<!-- Build date: '$TIMESTAMP '-->' >> $OUTPUT_DIR/$UPDATE_PLUGINS_XML
                            echo '<plugins>' >> $OUTPUT_DIR/$UPDATE_PLUGINS_XML
                            echo '    <plugin id="org.eclipse.codewind.intellij" url="'$DOWNLOAD_AREA_URL/$GIT_BRANCH/$LATEST_DIR/$OUTPUT_NAME.zip'" version="'$TIMESTAMP'">' >> $OUTPUT_DIR/$UPDATE_PLUGINS_XML
                            echo '        <idea-version since-build="201.7223.91" until-build="999.*"/>' >> $OUTPUT_DIR/$UPDATE_PLUGINS_XML
                            echo '        <name>Codewind</name>' >> $OUTPUT_DIR/$UPDATE_PLUGINS_XML
                            echo '        <description>Adds support for developing cloud-native, containerized web applications.</description>' >> $OUTPUT_DIR/$UPDATE_PLUGINS_XML
                            echo '    </plugin>' >> $OUTPUT_DIR/$UPDATE_PLUGINS_XML
                            echo '</plugins>' >> $OUTPUT_DIR/$UPDATE_PLUGINS_XML    

                            scp $OUTPUT_DIR/$UPDATE_PLUGINS_XML $sshHost:$deployDir/$GIT_BRANCH/$LATEST_DIR/$UPDATE_PLUGINS_XML
                
                            rm $OUTPUT_DIR/$BUILD_INFO
                            rm $OUTPUT_DIR/$UPDATE_PLUGINS_XML
                            rm $OUTPUT_DIR/$OUTPUT_NAME.zip
                        else
                            UPLOAD_DIR="pr/$CHANGE_ID/$BUILD_ID"
                        fi
                        
                        ssh $sshHost rm -rf $deployDir/${UPLOAD_DIR}
                        ssh $sshHost mkdir -p $deployDir/${UPLOAD_DIR}
                        scp -r $OUTPUT_DIR/* $sshHost:$deployDir/${UPLOAD_DIR}
                    '''
                }
            }
        }

        stage("Report") {
            when {
                beforeAgent true
                triggeredBy 'UpstreamCause'
            }

            options {
                skipDefaultCheckout()
            }

            steps {
                mail to: 'jspitman@ca.ibm.com',
                subject: "${currentBuild.currentResult}: Upstream triggered build for ${currentBuild.fullProjectName}",
                body: "${currentBuild.absoluteUrl}\n${currentBuild.getBuildCauses()[0].shortDescription} had status ${currentBuild.currentResult}"
            }
        }
    }    
}
