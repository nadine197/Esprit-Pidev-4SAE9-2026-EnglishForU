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

        stage('Checkout') {
            steps {
                checkout scm
                sh 'echo "Branch: ${GIT_BRANCH}  |  Build: ${BUILD_NUMBER}"'
                sh 'ls -la monitoring/'
            }
        }

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
                                mvn clean verify sonar:sonar \
                                    -Dsonar.projectKey=${svc.name} \
                                    -Dsonar.host.url=http://sonarqube:9000 \
                                    -Dsonar.token=${SONAR_TOKEN} \
                                    -DskipTests -B 
                                cd -
                            """
                        }
                    }
                }
            }
        }

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
         stage('prometheus') {
    steps {
        sh """
            docker build \
                --tag ${REGISTRY}/prometheus:${TAG} \
                --file ./monitoring/Dockerfile.prometheus \
                --label "git.commit=${env.GIT_COMMIT ?: 'local'}" \
                --label "build.number=${BUILD_NUMBER}" \
                ./monitoring
        """
    }
}
            }
        }

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
                            'quiz-service', 'frontend', 'prometheus'
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

        stage('Smoke Test') {
            steps {
                echo "Skipping smoke test — deploying directly to local Docker Desktop"
            }
        }

      stage('Deploy') {
          steps {
              sh """
                  REGISTRY=${REGISTRY} TAG=${TAG} \
                  docker compose down --remove-orphans || true

                  docker rm -f \
                      eureka-server config-server gateway \
                      user-service appointment-service club-service \
                      course-service discussion-service package-service \
                      quiz-service frontend \
                      sonar-db sonarqube prometheus grafana postgres || true

                  docker network rm englishforu-pipeline_microservices-net microservices-net || true

                  sleep 3

                  REGISTRY=${REGISTRY} TAG=${TAG} \
                  docker compose up -d --no-build postgres

                  sleep 25

                  docker exec postgres psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname='GestionUserPI'" | grep -q 1 || docker exec postgres psql -U postgres -c "CREATE DATABASE \\"GestionUserPI\\""
                  docker exec postgres psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname='GestionAppointPI'" | grep -q 1 || docker exec postgres psql -U postgres -c "CREATE DATABASE \\"GestionAppointPI\\""
                  docker exec postgres psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname='QuizPI'" | grep -q 1 || docker exec postgres psql -U postgres -c "CREATE DATABASE \\"QuizPI\\""
                  docker exec postgres psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname='GestionPackagePI'" | grep -q 1 || docker exec postgres psql -U postgres -c "CREATE DATABASE \\"GestionPackagePI\\""
                  docker exec postgres psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname='DiscussionPI'" | grep -q 1 || docker exec postgres psql -U postgres -c "CREATE DATABASE \\"DiscussionPI\\""
                  docker exec postgres psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname='CoursePI'" | grep -q 1 || docker exec postgres psql -U postgres -c "CREATE DATABASE \\"CoursePI\\""
                  docker exec postgres psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname='ClubEventPI'" | grep -q 1 || docker exec postgres psql -U postgres -c "CREATE DATABASE \\"ClubEventPI\\""

                  docker exec postgres psql -U postgres -c "\\l"

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

def buildService(String service, String context) {
    sh """
        docker build \
            --tag ${REGISTRY}/${service}:${TAG} \
            --label "git.commit=${env.GIT_COMMIT ?: 'local'}" \
            --label "build.number=${BUILD_NUMBER}" \
            ${context}
    """
}
