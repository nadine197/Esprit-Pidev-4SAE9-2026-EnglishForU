// ─────────────────────────────────────────────────────────────────
// Jenkins Declarative Pipeline — Microservices CI/CD
// Updated with:
// ✅ Clean Workspace
// ✅ Test Docker Access
// ─────────────────────────────────────────────────────────────────

pipeline {

    agent any

    environment {
        REGISTRY        = "${env.REGISTRY ?: 'docker.io/khalilsahnoun'}"
        TAG             = "${env.BUILD_NUMBER}"
        DOCKER_BUILDKIT = "1"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timeout(time: 60, unit: 'MINUTES')
        timestamps()
    }

    stages {

        // ── 0. Clean old workspace ──────────────────────────────
        stage('Clean Workspace') {
            steps {
                deleteDir()
            }
        }
        options {
           skipDefaultCheckout(true)
         }
        // ── 1. Checkout ─────────────────────────────────────────
        stage('Checkout') {
            steps {
                 cleanWs()
                checkout scm
                sh 'echo "Branch: ${BRANCH_NAME:-main} | Build: ${BUILD_NUMBER}"'
            }
        }

        // ── 2. Test Docker Access ───────────────────────────────
        stage('Test Docker') {
            steps {
                sh 'docker version'
                sh 'docker ps'
            }
        }

        // ── 3. Build Images ─────────────────────────────────────
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

        // ── 4. Smoke Test ───────────────────────────────────────
        stage('Smoke Test') {
            steps {
                sh """
                    REGISTRY=${REGISTRY} TAG=${TAG} docker compose up -d --no-build

                    echo "Waiting Eureka..."
                    timeout 120 sh -c 'until wget -q --spider http://localhost:8761/actuator/health; do sleep 5; done'

                    echo "Waiting Config..."
                    timeout 120 sh -c 'until wget -q --spider http://localhost:8888/actuator/health; do sleep 5; done'

                    echo "Waiting Gateway..."
                    timeout 120 sh -c 'until wget -q --spider http://localhost:8090/actuator/health; do sleep 5; done'

                    echo "All healthy ✅"
                """
            }

            post {
                always {
                    sh 'docker compose down -v --remove-orphans || true'
                }
            }
        }

        // ── 5. Push Images ──────────────────────────────────────
        stage('Push Images') {
            when {
                branch 'main'
            }

            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {

                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    '''

                    script {
                        def services = [
                            'eureka-server',
                            'config-server',
                            'gateway',
                            'user-service',
                            'appointment-service',
                            'club-service',
                            'course-service',
                            'discussion-service',
                            'package-service',
                            'quiz-service',
                            'frontend'
                        ]

                        services.each { svc ->
                            sh "docker push ${REGISTRY}/${svc}:${TAG}"
                            sh "docker tag ${REGISTRY}/${svc}:${TAG} ${REGISTRY}/${svc}:latest"
                            sh "docker push ${REGISTRY}/${svc}:latest"
                        }
                    }
                }
            }
        }

        // ── 6. Deploy ───────────────────────────────────────────
        stage('Deploy') {
            when {
                branch 'main'
            }

            steps {
                sh """
                    REGISTRY=${REGISTRY} TAG=${TAG} docker compose up -d --no-build --remove-orphans
                """
            }
        }
    }

    post {
        always {
            sh 'docker image prune -f || true'
        }

        success {
            echo '✅ Pipeline Success'
        }

        failure {
            echo '❌ Pipeline Failed'
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Helper Function
// ─────────────────────────────────────────────────────────────────

def buildService(String service, String context) {
    sh """
        docker build \
        -t ${REGISTRY}/${service}:${TAG} \
        --label build=${BUILD_NUMBER} \
        ${context}
    """
}
