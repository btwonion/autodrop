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
                sh './gradlew build'
            }
        }

        stage('Rebuild with dependency reload') {
            when {
                expression {
                    currentBuild.result == 'FAILED'
                }
            }

            steps {
                sh './gradlew clean build --refresh-dependencies'
            }
        }
    }
}
