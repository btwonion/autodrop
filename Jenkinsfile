pipeline {
    agent {
        docker {
            image 'gradle:jdk17'
            args '-v /var/cache/gradle:/tmp/gradle-user-home:rw'
        }
    }

    stages {
        stage('Build') {
            steps {
                script {
                    try {
                        sh 'gradle build'
                    } catch (err) {
                        sh 'gradle clean build --refresh-dependencies'
                    }
                }
            }
        }
    }
}