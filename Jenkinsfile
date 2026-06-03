pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'jdk-21'
    }

    environment {
        PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
        DOCKERHUB_CREDENTIALS_ID = 'docker-hub'
        DOCKERHUB_REPO_BACKEND = 'mustah21/study-planner-backend'
        DOCKERHUB_REPO_FRONTEND = 'mustah21/study-planner-frontend'
        DOCKER_IMAGE_TAG = 'p1'
    }

    stages {

        stage('Checkout') {
            steps {
                git credentialsId: 'Github',
                        url: 'https://github.com/MarcusHoanggg/Personalized-Study-Planner',
                        branch: 'main'
            }
        }

        stage('Build') {
            steps {
                withCredentials([file(credentialsId: 'application-yaml', variable: 'APP_YAML')]) {
                    bat """
                        copy "%APP_YAML%" "Backend\\src\\main\\resources\\application.yaml"
                        cd Backend
                        mvn clean install -DskipTests
                    """
                }
            }
        }

        stage('Test') {
            steps {
                dir('Backend') {
                    bat 'mvn test'
                    bat 'if exist target\\site\\jacoco\\jacoco.xml (echo JACOCO FOUND) else (echo JACOCO NOT FOUND)'
                }
            }
        }
        stage('SonarQube Analysis') {
            steps {
                dir('Backend') {
                    withSonarQubeEnv('SonarQubeServer') {
                        withCredentials([string(credentialsId: 'study-planner-sonar-token', variable: 'SONAR_TOKEN')]) {
                            bat 'mvn sonar:sonar -Dsonar.token=%SONAR_TOKEN%'
                        }
                    }
                }
            }
        }
        stage('Publish Test Results') {
            steps {
                junit 'Backend/**/target/surefire-reports/*.xml'
            }
        }


        stage('Build and Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker-hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        bat 'docker logout'
                        bat 'docker login -u %DOCKER_USER% -p %DOCKER_PASS%'
                        bat 'docker build -t %DOCKER_USER%/study-planner-backend:p1 ./Backend'
                        bat 'docker build -t %DOCKER_USER%/study-planner-frontend:p1 ./Frontend'
                        bat 'docker push %DOCKER_USER%/study-planner-backend:p1'
                        bat 'docker push %DOCKER_USER%/study-planner-frontend:p1'
                    }
                }
            }
        }

    }
}