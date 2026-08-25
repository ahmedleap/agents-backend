// Environment verification template — confirms the CI agent has Maven, a JDK, and
// JUnit working together before any real application pipeline is written on top.
pipeline {
    agent any

    environment {
        IMAGE_NAME = "env-check"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Verification Image') {
            steps {
                // Building the image alone proves Maven/JDK/JUnit resolve and compile
                // correctly, since the Dockerfile runs `mvn test` during the build.
                sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} ."
            }
        }

        stage('Report Versions') {
            steps {
                // Re-run against the built image so the tool versions show up
                // directly in this stage's console output.
                sh "docker run --rm --entrypoint sh ${IMAGE_NAME}:${BUILD_NUMBER} -c 'java -version && mvn -version'"
            }
        }

        stage('Test') {
            steps {
                sh "docker run --rm ${IMAGE_NAME}:${BUILD_NUMBER}"
            }
        }
    }

    post {
        success {
            echo "Environment check passed: Maven, JDK, and JUnit are installed and working."
        }
        failure {
            echo "Environment check failed — see console output for the missing/broken tool."
        }
    }
}
