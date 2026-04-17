// ─────────────────────────────────────────────────────────────────
//  Jenkins Declarative Pipeline — Microservices CI/CD
//
//  Setup checklist (Docker Desktop Jenkins):
//  1. Jenkins container must mount the Docker socket:
//       -v /var/run/docker.sock:/var/run/docker.sock
//  2. Install plugins: Docker Pipeline, Pipeline, Git
//  3. Add credential "REGISTRY_CREDENTIALS" (user/pass) in
//       Jenkins → Credentials → System → Global
//  4. Set REGISTRY env var in Jenkins → Manage → System:
//       REGISTRY = local   (local dev, no push)
//       REGISTRY = docker.io/yourorg   (Docker Hub)
// ─────────────────────────────────────────────────────────────────

pipeline {

    agent any

    environment {
        REGISTRY        = "${env.REGISTRY ?: 'local'}"
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

        // ── 1. Checkout ──────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                sh 'echo "Branch: ${BRANCH_NAME:-local}  |  Build: ${BUILD_NUMBER}"'
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

        // ── 3. Smoke-test with Docker Compose ───────────────────
        // Spins up the full stack, waits for eureka + config + gateway,
        // runs a basic HTTP health-check, then tears everything down.
        stage('Smoke Test') {
            steps {
                sh """
                    REGISTRY=${REGISTRY} TAG=${TAG} \
                    docker compose up -d --no-build

                    echo "Waiting for eureka..."
                    timeout 120 sh -c 'until wget -q --spider http://localhost:8761/actuator/health; do sleep 5; done'

                    echo "Waiting for config-server..."
                    timeout 120 sh -c 'until wget -q --spider http://localhost:8888/actuator/health; do sleep 5; done'

                    echo "Waiting for gateway..."
                    timeout 120 sh -c 'until wget -q --spider http://localhost:8090/actuator/health; do sleep 5; done'

                    echo "All core services healthy ✅"
                """
            }
            post {
                always {
                    sh 'docker compose down -v --remove-orphans || true'
                }
            }
        }

        // ── 4. Push images ───────────────────────────────────────
        // Skipped when REGISTRY=local (local dev builds).
        // Runs on main or develop branches pointing to a real registry.
        stage('Push Images') {
            when {
                allOf {
                    not { environment name: 'REGISTRY', value: 'local' }
                    anyOf { branch 'main'; branch 'develop' }
                }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin ${REGISTRY}'
                    script {
                        def services = [
                            'eureka-server', 'config-server', 'gateway',
                            'user-service', 'appointment-service', 'club-service',
                            'course-service', 'discussion-service', 'package-service',
                            'quiz-service', 'frontend'
                        ]
                        services.each { svc ->
                            sh "docker push ${REGISTRY}/${svc}:${TAG}"
                            // Tag latest only on main
                            if (env.BRANCH_NAME == 'main') {
                                sh """
                                    docker tag ${REGISTRY}/${svc}:${TAG} ${REGISTRY}/${svc}:latest
                                    docker push ${REGISTRY}/${svc}:latest
                                """
                            }
                        }
                    }
                }
            }
        }

        // ── 5. Deploy (main only) ────────────────────────────────
        stage('Deploy') {
            when { branch 'main' }
            steps {
                // ── Option A: deploy on the same Docker Desktop host ──
                sh """
                    REGISTRY=${REGISTRY} TAG=${TAG} \
                    docker compose up -d --no-build --remove-orphans
                """
                // ── Option B: deploy to remote server via SSH ──────────
                // sh "ssh user@server 'cd /opt/app && REGISTRY=${REGISTRY} TAG=${TAG} docker compose pull && docker compose up -d --remove-orphans'"
            }
        }
    }

    // ── Post-pipeline cleanup ────────────────────────────────────
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

// ─────────────────────────────────────────────────────────────────
//  Helper: build a single service image
// ─────────────────────────────────────────────────────────────────
def buildService(String service, String context) {
    sh """
        docker build \
            --tag ${REGISTRY}/${service}:${TAG} \
            --label "git.commit=${env.GIT_COMMIT ?: 'local'}" \
            --label "build.number=${BUILD_NUMBER}" \
            ${context}
    """
}
