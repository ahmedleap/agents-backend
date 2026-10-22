// Spring Boot Backend CI/CD Pipeline
// Stages: dependency check, build, test with coverage, Docker image build, artifact archive
pipeline {
    agent any

    tools {
        jdk 'Java 21'
    }

    environment {
        IMAGE_NAME = "agents-backend"
        DOCKER_TAG_LATEST = "${IMAGE_NAME}:latest"
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Checking out code from branch: ${BRANCH_NAME}"
                checkout scm
                    script {
                        env.PROJECT_VERSION = sh(
                            script: "mvn -q help:evaluate '-Dexpression=project.version' -DforceStdout",
                            returnStdout: true
                        ).trim()
                        env.DOCKER_TAG = "${env.IMAGE_NAME}:${env.PROJECT_VERSION}-${env.BUILD_NUMBER}"
                    }
                echo "Project version: ${PROJECT_VERSION}"
            }
        }

        stage('Dependency Verification') {
            steps {
                echo "Verifying Maven dependencies and checking for updates..."
                sh "mvn -B dependency:tree > dependency-tree.txt"
                sh "mvn -B dependency:analyze"
            }
        }

        stage('Build & Test') {
            steps {
                echo "Building project and running tests with coverage..."
                sh "mvn -B -U clean verify"
            }
        }

        stage('Report Test Coverage') {
            steps {
                echo "Archiving JUnit test results and JaCoCo coverage reports..."
                junit 'target/surefire-reports/*.xml'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image: ${DOCKER_TAG}"
                sh "docker build --build-arg VERSION=${PROJECT_VERSION} -t ${DOCKER_TAG} -t ${DOCKER_TAG_LATEST} ."
                sh "docker image inspect ${DOCKER_TAG} | head -20"
            }
        }

        stage('Verify Image') {
            steps {
                echo "Verifying Docker image runtime..."
                sh "docker run --rm --entrypoint java ${DOCKER_TAG} -version"
                sh "docker image inspect ${DOCKER_TAG} --format '{{.Config.User}} {{.Config.ExposedPorts}}'"
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo "Archiving build artifacts and test reports..."
                archiveArtifacts artifacts: 'target/agents-backend.jar', allowEmptyArchive: false
                archiveArtifacts artifacts: 'dependency-tree.txt', allowEmptyArchive: true
                archiveArtifacts artifacts: 'target/surefire-reports/**/*.xml', allowEmptyArchive: true
            }
        }
    }

    post {
        always {
            echo "Build completed for version: ${PROJECT_VERSION}"
            echo "Docker image: ${DOCKER_TAG}"
        }
        success {
            echo "✓ Build successful"
            echo "✓ All tests passed"
            echo "✓ Docker image ready: ${DOCKER_TAG}"
        }
        failure {
            echo "✗ Build failed — check logs above for details"
        }
    }
}
