pipeline {
    agent {
        docker {
            image 'gradle:jdk17'
        }
    }

    stages {
        stage('Build') {
            try {
                steps {
                    sh 'chmod +x gradlew'
                    sh 'gradle build'
                }
            }
            catch (e) {
                sh 'gradle clean build --refresh-dependencies'
            }
        }
    }
}
