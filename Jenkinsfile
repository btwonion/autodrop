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
                catch (err) {
                    sh 'gradle clean build --refresh-dependencies'
                }
            }
        }
    }
}
