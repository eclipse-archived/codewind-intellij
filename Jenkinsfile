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

    parameters {
        string(name: "APPSODY_VERSION", defaultValue: "0.4.6", description: "Appsody executable version to download")
    }

    stages {

        stage("Download dependency binaries") {
            steps {
                dir("dev") {
                    sh """#!/usr/bin/env bash
                        echo "Downloading codewind-intellij dependency binaries ..."
                        export APPSODY_VERSION=${params.APPSODY_VERSION}
                        ./src/main/resources/cwctl/meta-pull.sh 
                        ./src/main/resources/cwctl/pull.sh
                        ./gradlew copyDependencies --stacktrace
                    """
                }
            }
        }

        stage('Build') {
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
                        export DOWNLOAD_AREA_URL="https://download.eclipse.org/codewind/$REPO_NAME"
                        export LATEST_DIR="latest"
                        export BUILD_INFO="build_info.properties"
                        export sshHost="genie.codewind@projects-storage.eclipse.org"
                        export deployDir="/home/data/httpd/download.eclipse.org/codewind/$REPO_NAME"
                    
                        if [ -z $CHANGE_ID ]; then
                            UPLOAD_DIR="$GIT_BRANCH/$BUILD_ID"
                            BUILD_URL="$DOWNLOAD_AREA_URL/$UPLOAD_DIR"
                  
                            ssh $sshHost rm -rf $deployDir/$GIT_BRANCH/$LATEST_DIR
                            ssh $sshHost mkdir -p $deployDir/$GIT_BRANCH/$LATEST_DIR
                            
                            cp $OUTPUT_DIR/$OUTPUT_NAME*.zip $OUTPUT_DIR/$OUTPUT_NAME.zip
                            scp $OUTPUT_DIR/$OUTPUT_NAME.zip $sshHost:$deployDir/$GIT_BRANCH/$LATEST_DIR/$OUTPUT_NAME.zip
                        
                            echo "# Build date: $(date +%F-%T)" >> $OUTPUT_DIR/$BUILD_INFO
                            echo "build_info.url=$BUILD_URL" >> $OUTPUT_DIR/$BUILD_INFO
                            SHA1=$(sha1sum ${OUTPUT_DIR}/${OUTPUT_NAME}.zip | cut -d ' ' -f 1)
                            echo "build_info.SHA-1=${SHA1}" >> $OUTPUT_DIR/$BUILD_INFO
                            scp $OUTPUT_DIR/$BUILD_INFO $sshHost:$deployDir/$GIT_BRANCH/$LATEST_DIR/$BUILD_INFO
                
                            rm $OUTPUT_DIR/$BUILD_INFO
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
    }    
}