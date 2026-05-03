pipeline {

    agent any

    environment {
        REGISTRY        = "khalilsahnoun"
        TAG             = "${env.BUILD_NUMBER}"
        DOCKER_BUILDKIT = "1"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timeout(time: 90, unit: 'MINUTES')
        timestamps()
    }

    stages {

        // ── 1. Checkout ──────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                sh 'echo "Branch: ${GIT_BRANCH}  |  Build: ${BUILD_NUMBER}"'
            }
        }
// ── 1.5. SonarQube Analysis ──────────────────────────────────
stage('SonarQube Analysis') {
    steps {
        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
            script {
                def services = [
                    [name: 'eureka-server',       path: './backend/eureka-server'],
                    [name: 'config-server',        path: './backend/config-server'],
                    [name: 'gateway',              path: './backend/Gateway'],
                    [name: 'user-service',         path: './backend/user-management'],
                    [name: 'appointment-service',  path: './backend/Appointment'],
                    [name: 'club-service',         path: './backend/ClubEvent'],
                    [name: 'course-service',       path: './backend/course-service'],
                    [name: 'discussion-service',   path: './backend/discussion-service'],
                    [name: 'package-service',      path: './backend/package_service'],
                    [name: 'quiz-service',         path: './backend/Quiz'],
                ]
                services.each { svc ->
                    sh """
                        cd ${svc.path}
                        mvn sonar:sonar \
                            -Dsonar.projectKey=${svc.name} \
                            -Dsonar.host.url=http://sonarqube:9000 \
                            -Dsonar.token=${SONAR_TOKEN} \
                            -DskipTests -B || true
                        cd -
                    """
                }
            }
        }
    }
}
        // ── 2. Build all images in parallel ─────────────────────
        stage('Build Images') {
            parallel {
                stage('eureka-server') {
                    steps { script { buildService('eureka-server', './backend/eureka-server') } }
                }
                stage('config-server') {
                    steps { script { buildService('config-server', './backend/config-server') } }
                }
                stage('gateway') {
                    steps { script { buildService('gateway', './backend/Gateway') } }
                }
                stage('user-service') {
                    steps { script { buildService('user-service', './backend/user-management') } }
                }
                stage('appointment-service') {
                    steps { script { buildService('appointment-service', './backend/Appointment') } }
                }
                stage('club-service') {
                    steps { script { buildService('club-service', './backend/ClubEvent') } }
                }
                stage('course-service') {
                    steps { script { buildService('course-service', './backend/course-service') } }
                }
                stage('discussion-service') {
                    steps { script { buildService('discussion-service', './backend/discussion-service') } }
                }
                stage('package-service') {
                    steps { script { buildService('package-service', './backend/package_service') } }
                }
                stage('quiz-service') {
                    steps { script { buildService('quiz-service', './backend/Quiz') } }
                }
                stage('frontend') {
                    steps { script { buildService('frontend', './frontend') } }
                }
            }
        }

        // ── 3. Push Images to Docker Hub ─────────────────────────
        stage('Push Images') {
        
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                    script {
                        def services = [
                            'eureka-server', 'config-server', 'gateway',
                            'user-service', 'appointment-service', 'club-service',
                            'course-service', 'discussion-service', 'package-service',
                            'quiz-service', 'frontend'
                        ]
                        services.each { svc ->
                            sh "docker push ${REGISTRY}/${svc}:${TAG}"
                            sh """
                                docker tag ${REGISTRY}/${svc}:${TAG} ${REGISTRY}/${svc}:latest
                                docker push ${REGISTRY}/${svc}:latest
                            """
                        }
                    }
                }
            }
        }

       // ── 4. Smoke Test (skipped - deploy directly) ────────────
        stage('Smoke Test') {
            steps {
                echo "Skipping smoke test — deploying directly to local Docker Desktop"
            }
        }
        // ── 5. Deploy ────────────────────────────────────────────
        stage('Deploy') {
            steps {
                sh """
                    REGISTRY=${REGISTRY} TAG=${TAG} \
                    docker compose down --remove-orphans || true
        
                    REGISTRY=${REGISTRY} TAG=${TAG} \
                    docker compose up -d --no-build --remove-orphans
                """
            }
}

    }

    post {
        always {
            sh 'docker image prune -f || true'
        }
        success {
            echo "✅ Pipeline passed — build ${BUILD_NUMBER}"
        }
        failure {
            echo "❌ Pipeline failed — check console output above"
        }
    }
}

// ── Helper function ──────────────────────────────────────────────
def buildService(String service, String context) {
    sh """
        docker build \
            --tag ${REGISTRY}/${service}:${TAG} \
            --label "git.commit=${env.GIT_COMMIT ?: 'local'}" \
            --label "build.number=${BUILD_NUMBER}" \
            ${context}
    """
}
