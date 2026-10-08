// Spring Boot Backend CI/CD Pipeline
// Stages: dependency check, build, test with coverage, Docker image build, artifact archive
pipeline {
    agent any

    tools {
        jdk 'Java 21'
    }

    environment {
        // Extract version from pom.xml for consistency across builds
        PROJECT_VERSION = sh(
            script: "grep -m1 '<version>' pom.xml | sed 's/.*<version>\\([^<]*\\)<\\/version>.*/\\1/' | xargs",
            returnStdout: true
        ).toString().trim()
        IMAGE_NAME = "agents-backend"
        DOCKER_TAG = "${IMAGE_NAME}:${PROJECT_VERSION}-${BUILD_NUMBER}"
        DOCKER_TAG_LATEST = "${IMAGE_NAME}:latest"
        SONAR_HOST_URL = credentials('sonarqube-url')
        SONAR_LOGIN = credentials('sonarqube-token')
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Checking out code from branch: ${BRANCH_NAME}"
                checkout scm
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

        stage('SonarQube Code Quality Analysis') {
            steps {
                echo "Running SonarQube analysis..."
                sh '''
                    mvn -B -U sonar:sonar \
                        -Dsonar.projectKey=agents-backend \
                        -Dsonar.projectName="Agents Backend" \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.login=${SONAR_LOGIN} \
                        -Dsonar.qualitygate.wait=true \
                        -Dsonar.sources=src/main \
                        -Dsonar.tests=src/test \
                        -Dsonar.java.binaries=target/classes \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                '''
            }
        }
            steps {
                echo "Building Docker image: ${DOCKER_TAG}"
                sh "docker build --build-arg VERSION=${PROJECT_VERSION} -t ${DOCKER_TAG} -t ${DOCKER_TAG_LATEST} ."
                sh "docker image inspect ${DOCKER_TAG} | head -20"
            }
        }

        stage('Verify Image') {
            steps {
                echo "Verifying Docker image runs without errors..."
                sh "docker run --rm ${DOCKER_TAG} java -version"
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
