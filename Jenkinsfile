 properties([ [ $class: 'ThrottleJobProperty',
                categories: ['metersphere'], 
                limitOneJobWithMatchingParams: false,
                maxConcurrentPerNode: 1,
                maxConcurrentTotal: 1,
                paramsToUseForLimit: '',
                throttleEnabled: true,
                throttleOption: 'category' ] ])
                
pipeline {
    agent {
        node {
            label 'metersphere'
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
                    sh '''
                        export JAVA_HOME=/opt/jdk-11
                        export CLASSPATH=$JAVA_HOME/lib:$CLASSPATH
                        export PATH=$JAVA_HOME/bin:/opt/mvnd/bin:$PATH
                        java -version
                        mvnd clean package -Drevision=${BRANCH_NAME} -DskipAntRunForJenkins --settings ./settings.xml
                        mkdir -p backend/target/dependency && (cd backend/target/dependency; jar -xf ../*.jar)
                    '''
                }
            }
        }
        stage('Docker build & push') {
            steps {
                sh '''
                 su - metersphere -c "cd ${WORKSPACE} && docker buildx create --use && docker buildx build --build-arg MS_VERSION=\${TAG_NAME:-\$BRANCH_NAME}-\${GIT_COMMIT:0:8} -t ${IMAGE_PREFIX}/${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME} --platform linux/amd64,linux/arm64 . --push"
                '''
            }
        }
    }
    post('Notification') {
        always {
            sh "echo \$WEBHOOK\n"
            withCredentials([string(credentialsId: 'wechat-bot-webhook', variable: 'WEBHOOK')]) {
                qyWechatNotification failNotify: true, mentionedId: '', mentionedMobile: '', webhookUrl: "$WEBHOOK"
            }
        }
    }
}
