pipeline {
	agent any

    environment {
		IMAGE_NAME = "abirrsahaa/movie-boooking-app"
        TAG = "latest"
    }

    stages {
		stage('Checkout') {
			steps {
				git branch: 'main', url: 'https://github.com/Aakshigulati098/Movie_Booking_System'
            }
        }

        stage('Build Docker Image') {
			steps {
				bat "docker build -t %IMAGE_NAME%:%TAG% ."
            }
        }

        stage('Push to DockerHub') {
			steps {
				withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
					bat """
                        echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin
                        docker push %IMAGE_NAME%:%TAG%
                    """
                }
            }
        }
    }
}
