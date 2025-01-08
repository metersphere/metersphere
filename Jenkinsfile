pipeline {
    agent {
        node {
            label 'metersphere'
        }
    }
    environment {
        IMAGE_PREFIX = 'registry.cn-qingdao.aliyuncs.com/metersphere'
        JAVA_HOME = '/opt/jdk-17'
    }
    stages {
        stage('Preparation') {
            steps {
                script {
                    REVISION = ""
                    BUILD_SDK = false
                    if (env.BRANCH_NAME.startsWith("v") ) {
                        REVISION = env.BRANCH_NAME.substring(1)
                    } else {
                        REVISION = env.BRANCH_NAME
                    }
                    if (params.buildSdk) {
                        BUILD_SDK = true
                    }
                    if (params.frontendLink != null && !params.frontendLink.equals("")) {
                        env.FRONTEND_LINK = params.frontendLink
                    }
                    env.REVISION = "${REVISION}"
                    env.BUILD_SDK = "${BUILD_SDK}"
                    echo "REVISION=${REVISION}, BUILD_SDK=${BUILD_SDK}"
                }
            }
        }
        stage('SDK') {
            when { environment name: 'BUILD_SDK', value: 'true' }
            steps {
                configFileProvider([configFile(fileId: 'metersphere-maven', targetLocation: 'settings.xml')]) {
                    sh '''#!/bin/bash -xe
                        export JAVA_HOME=/opt/jdk-17
                        export CLASSPATH=$JAVA_HOME/lib:$CLASSPATH
                        export PATH=$JAVA_HOME/bin:/opt/apache-maven-3.8.3/bin:$PATH
                        java -version
                        mvn deploy -N -Drevision=${REVISION} --settings ./settings.xml
                        mvn clean deploy -Drevision=${REVISION} -pl framework,framework/sdk-parent,framework/sdk-parent/domain,framework/sdk-parent/sdk,framework/sdk-parent/xpack-interface,framework/sdk-parent/jmeter --settings ./settings.xml

                        # 复制前端代码
                        if [ -n "${FRONTEND_LINK}" ]; then
                            cp -r framework/sdk-parent/frontend ${FRONTEND_LINK}/frontend/.tmp_npm
                        fi
                    '''
                }
            }
        }
        stage('Build/Test') {
            when { environment name: 'BUILD_SDK', value: 'false' }
            steps {
                configFileProvider([configFile(fileId: 'metersphere-maven', targetLocation: 'settings.xml')]) {
                    sh '''#!/bin/bash -xe
                        export JAVA_HOME=/opt/jdk-17
                        export CLASSPATH=$JAVA_HOME/lib:$CLASSPATH
                        export PATH=$JAVA_HOME/bin:/opt/apache-maven-3.8.3/bin:$PATH
                        java -version
                        mvn clean package -Drevision=${REVISION} --settings ./settings.xml

                        frameworks=('framework/eureka' 'framework/gateway')
                        for library in "${frameworks[@]}";
                        do
                            mkdir -p $library/target/dependency && (cd $library/target/dependency; jar -xf ../*.jar)
                        done

                        LOCAL_REPOSITORY=$(mvn help:evaluate -Dexpression=settings.localRepository --settings ./settings.xml -q -DforceStdout)

                        libraries=('api-test' 'performance-test' 'project-management' 'system-setting' 'test-track' 'report-stat' 'workstation')
                        for library in "${libraries[@]}";
                        do
                            mkdir -p $library/backend/target/dependency && (cd $library/backend/target/dependency; jar -xf ../*.jar; cp $LOCAL_REPOSITORY/io/metersphere/metersphere-xpack/${REVISION}/metersphere-xpack-${REVISION}.jar ./BOOT-INF/lib/)
                        done
                    '''
                }
            }
        }
        stage('Docker build & push') {
            when { environment name: 'BUILD_SDK', value: 'false' }
            steps {
                script {
                    for (int i=0; i<10; i++) {
                        try {
                            sh '''#!/bin/bash -xe
                            cd ${WORKSPACE}
                            libraries=('framework/eureka' 'framework/gateway' 'api-test' 'performance-test' 'project-management' 'report-stat' 'system-setting' 'test-track' 'workstation')
                            for library in "${libraries[@]}";
                            do
                                IMAGE_NAME=${library#*/}
                                docker --config /home/metersphere/.docker buildx build --no-cache --build-arg MS_VERSION=\${TAG_NAME:-\$BRANCH_NAME}-\${GIT_COMMIT:0:8} -t ${IMAGE_PREFIX}/${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME} --platform linux/amd64,linux/arm64 ./$library --push
                            done
                            '''
                            break
                        } catch (Exception e) {
                            sleep 10
                            continue
                        }
                    }
                }
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
