pipeline {
    agent {
        node {
            label 'metersphere'
        }
    }
    options { quietPeriod(600) }
    parameters { 
        string(name: 'IMAGE_NAME', defaultValue: 'metersphere', description: '构建后的 Docker 镜像名称')
        string(name: 'IMAGE_PREFIX', defaultValue: 'registry.cn-qingdao.aliyuncs.com/metersphere', description: '构建后的 Docker 镜像带仓库名的前缀')
    }
    stages {
        stage('Build/Test') {
            steps {
                configFileProvider([configFile(fileId: 'metersphere-maven', targetLocation: 'settings.xml')]) {
                    sh "mvn clean package --settings ./settings.xml"
                }
            }
        }
        stage('Docker build & push') {
            steps {
                sh "docker build --build-arg MS_VERSION=\${TAG_NAME:-\$BRANCH_NAME}-\${GIT_COMMIT:0:8} -t ${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME} ."
                sh "docker tag ${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME} ${IMAGE_PREFIX}/${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME}"
                sh "docker push ${IMAGE_PREFIX}/${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME}"
            }
        }
    }
    post('Notification') {
        always {
            sh "echo \$WEBHOOK\n"
            withCredentials([string(credentialsId: 'wechat-bot-webhook', variable: 'WEBHOOK')]) {
                qyWechatNotification failSend: true, mentionedId: '', mentionedMobile: '', webhookUrl: "$WEBHOOK"
            }
        }
    }
}
