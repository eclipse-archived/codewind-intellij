#!groovy​

pipeline {
    agent any
    
    tools {
        jdk 'oracle-jdk8-latest'
    }
    
    options {
        timestamps() 
        skipStagesAfterUnstable()
    }
    
    stages {

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
                    
                    dir('dev') { sh '''
                        src/main/resources/cwctl/meta-pull.sh
                        APPSODY_VERSION=0.4.4 src/main/resources/cwctl/pull.sh
                        ./gradlew copyDependencies buildPlugin --stacktrace
                    ''' }
                }
            }
        } 
        
        stage('Deploy') {
            steps {
                sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
                  println("Deploying codewind-intellij to download area...")
                  
                  sh '''
                  	if [ -z $CHANGE_ID ]; then
    					UPLOAD_DIR="$GIT_BRANCH/$BUILD_ID"

                  		ssh genie.codewind@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/codewind/codewind-intellij/$GIT_BRANCH/latest
                  		ssh genie.codewind@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/codewind/codewind-intellij/$GIT_BRANCH/latest
                  		scp -r ${WORKSPACE}/dev/build/distributions/* genie.codewind@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/codewind/codewind-intellij/$GIT_BRANCH/latest    					
					else
    					UPLOAD_DIR="pr/$CHANGE_ID/$BUILD_ID"
					fi
 
                  	ssh genie.codewind@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/codewind/codewind-intellij/${UPLOAD_DIR}
                  	ssh genie.codewind@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/codewind/codewind-intellij/${UPLOAD_DIR}
                  	scp -r ${WORKSPACE}/dev/build/distributions/* genie.codewind@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/codewind/codewind-intellij/${UPLOAD_DIR}                  	
                  '''
                }
            }
        }       
    }    
}