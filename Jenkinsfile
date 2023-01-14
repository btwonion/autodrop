pipeline {
    agent {
        docker {
            image 'gradle:jdk17'
        }
    }

    stages {
        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh 'gradle build'
            }
        }

        stage('Rebuild with dependency reload') {
            when {
                expression {
                    currentBuild.result == 'FAILED'
                }
            }

            steps {
                sh 'gradle clean build --refresh-dependencies'
            }
        }
    }
}
