pipeline {
    agent {
        docker {
            image 'gradle:jdk17'
        }
    }

    stages {
        stage('Build') {
            steps {
                try {
                    sh 'chmod +x gradlew'
                    sh 'gradle build'
                } 
                catch (Exception e) {
                    sh 'gradle clean build --refresh-dependencies'
                }
            }
        }
    }
}
