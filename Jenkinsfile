pipeline {
    agent {
        node {
            label 'metersphere-buildx'
        }
    }
    options { quietPeriod(600) }
    environment { 
        IMAGE_NAME = 'metersphere'
        IMAGE_PREFIX = 'registry.cn-qingdao.aliyuncs.com/metersphere'
    }
    stages {
        stage('Build/Test') {
            steps {
                configFileProvider([configFile(fileId: 'metersphere-maven', targetLocation: 'settings.xml')]) {
//                     sh "cd frontend"
//                     sh "yarn install"
//                     sh "cd .."
                    sh "./mvnw clean package --settings ./settings.xml"
                    sh "mkdir -p backend/target/dependency && (cd backend/target/dependency; jar -xf ../*.jar)"
                }
            }
        }
        stage('Docker build & push') {
            steps {
                 sh "docker buildx build --build-arg MS_VERSION=\${TAG_NAME:-\$BRANCH_NAME}-\${GIT_COMMIT:0:8} -t ${IMAGE_PREFIX}/${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME} --platform linux/amd64,linux/arm64 . --push"
            }
        }
    }
    post('Notification') {
        always {
            sh "echo \$WEBHOOK\n"
            withCredentials([string(credentialsId: 'wechat-bot-webhook', variable: 'WEBHOOK')]) {
                qyWechatNotification failSend: true, mentionedId: '', mentionedMobile: '', webhookUrl: "$WEBHOOK"
            }
            sh "./mvnw clean"
        }
    }
}
