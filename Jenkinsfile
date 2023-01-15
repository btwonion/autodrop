pipeline {
    agent {
        docker {
            image 'gradle:jdk17'
        }
    }

    try {
        stages {
            stage('Build') {
                steps {
                    sh 'chmod +x gradlew'
                    sh 'gradle build'
                }
            }
        }
    } catch (all) {
        sh 'gradle clean build --refresh-dependencies'
    }
}
