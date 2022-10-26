pipeline {
    agent {
        node {
            label 'metersphere'
        }
    }
    triggers {
        cron('0 3 * * *')
    }
    environment {
        IMAGE_PREFIX = 'registry.cn-qingdao.aliyuncs.com/metersphere'
        JAVA_HOME = '/opt/jdk-11'
    }
    stages {
        stage('Preparation') {
            steps {
                script {
                    REVISION = ""
                    BUILD_SDK = false
                    BUILD_PARENT = false
                    if (env.BRANCH_NAME.startsWith("v") ) {
                        REVISION = env.BRANCH_NAME.substring(1)
                    } else {
                        REVISION = env.BRANCH_NAME
                    }
                    if (params.buildSdk) {
                        BUILD_SDK = true
                    }
                    if (params.buildParent) {
                        BUILD_PARENT = true
                    }
                    if (params.frontendLink != null && !params.frontendLink.equals("")) {
                        env.FRONTEND_LINK = params.frontendLink
                    }
                    env.REVISION = "${REVISION}"
                    env.BUILD_SDK = "${BUILD_SDK}"
                    env.BUILD_PARENT = "${BUILD_PARENT}"
                    echo "REVISION=${REVISION}, BUILD_SDK=${BUILD_SDK}, BUILD_PARENT=${BUILD_PARENT}"
                }
            }
        }
        stage('POM') {
            when { environment name: 'BUILD_PARENT', value: 'true' }
            steps {
                configFileProvider([configFile(fileId: 'metersphere-maven', targetLocation: 'settings.xml'), configFile(fileId: 'metersphere-npmrc', targetLocation: '.npmrc')]) {
                    sh '''#!/bin/bash -xe
                        export JAVA_HOME=/opt/jdk-11
                        export CLASSPATH=$JAVA_HOME/lib:$CLASSPATH
                        export PATH=$JAVA_HOME/bin:$PATH
                        java -version
                        ./mvnw install -N -Drevision=${REVISION} --settings ./settings.xml
                    '''
                }
            }
        }
        stage('SDK') {
            when { environment name: 'BUILD_SDK', value: 'true' }
            steps {
                configFileProvider([configFile(fileId: 'metersphere-maven', targetLocation: 'settings.xml'), configFile(fileId: 'metersphere-npmrc', targetLocation: '.npmrc')]) {
                    sh '''#!/bin/bash -xe
                        export JAVA_HOME=/opt/jdk-11
                        export CLASSPATH=$JAVA_HOME/lib:$CLASSPATH
                        export PATH=$JAVA_HOME/bin:$PATH
                        java -version
                        ./mvnw install -N -Drevision=${REVISION} --settings ./settings.xml
                        ./mvnw clean install -Drevision=${REVISION} -pl framework,framework/sdk-parent,framework/sdk-parent/domain,framework/sdk-parent/sdk,framework/sdk-parent/xpack-interface --settings ./settings.xml

                        # 复制前端代码
                        if [ -n "${FRONTEND_LINK}" ]; then
                            cp -r framework/sdk-parent/frontend ${FRONTEND_LINK}/frontend/.tmp_npm
                        fi
                    '''
                }
            }
        }
        stage('Build/Test') {
            when {
                allOf {
                    environment name: 'BUILD_SDK', value: 'false';
                    environment name: 'BUILD_PARENT', value: 'false'
                }
            }
            steps {
                configFileProvider([configFile(fileId: 'metersphere-maven', targetLocation: 'settings.xml'), configFile(fileId: 'metersphere-npmrc', targetLocation: '.npmrc')]) {
                    sh '''#!/bin/bash -xe
                        export JAVA_HOME=/opt/jdk-11
                        export CLASSPATH=$JAVA_HOME/lib:$CLASSPATH
                        export PATH=$JAVA_HOME/bin:$PATH
                        java -version
                        ./mvnw clean package -Drevision=${REVISION} --settings ./settings.xml

                        frameworks=('framework/eureka' 'framework/gateway')
                        for library in "${frameworks[@]}";
                        do
                            mkdir -p $library/target/dependency && (cd $library/target/dependency; jar -xf ../*.jar)
                        done

                        LOCAL_REPOSITORY=$(./mvnw help:evaluate -Dexpression=settings.localRepository --settings ./settings.xml -q -DforceStdout)

                        libraries=('api-test' 'performance-test' 'project-management' 'system-setting' 'test-track' 'report-stat')
                        for library in "${libraries[@]}";
                        do
                            mkdir -p $library/backend/target/dependency && (cd $library/backend/target/dependency; jar -xf ../*.jar; cp $LOCAL_REPOSITORY/io/metersphere/metersphere-xpack/${REVISION}/metersphere-xpack-${REVISION}.jar ./BOOT-INF/lib/)
                        done
                    '''
                }
            }
        }
        stage('Docker build & push') {
            when {
                allOf {
                    environment name: 'BUILD_SDK', value: 'false';
                    environment name: 'BUILD_PARENT', value: 'false'
                }
            }
            steps {
                script {
                    for (int i=0; i<10; i++) {
                        try {
                            sh '''#!/bin/bash -xe
                            cd ${WORKSPACE}
                            libraries=('framework/eureka' 'framework/gateway' 'api-test' 'performance-test' 'project-management' 'report-stat' 'system-setting' 'test-track')
                            for library in "${libraries[@]}";
                            do
                                IMAGE_NAME=${library#*/}
                                docker --config /home/metersphere/.docker buildx build --build-arg MS_VERSION=\${TAG_NAME:-\$BRANCH_NAME}-\${GIT_COMMIT:0:8} -t ${IMAGE_PREFIX}/${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME} --platform linux/amd64,linux/arm64 ./$library --push
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
            script {
                try {
                    if ("$BUILD_PARENT".equals("true") || "$BUILD_SDK".equals("true")) {
                        println "JOB_NAME=$JOB_NAME, BUILD_NUMBER=$BUILD_NUMBER, BUILD_URL=$BUILD_URL"
                        Hudson.instance.getItemByFullName("$JOB_NAME").builds.each {
                            if(it.number == Integer.parseInt("$BUILD_NUMBER")) {
                                println 'Deleting build number ' + it.number
                                it.delete()
                                break;
                            }
                        }
                    }
                } catch (NoSuchElementException) {
                }
            }
            withCredentials([string(credentialsId: 'wechat-bot-webhook', variable: 'WEBHOOK')]) {
                qyWechatNotification failNotify: true, mentionedId: '', mentionedMobile: '', webhookUrl: "$WEBHOOK"
            }
        }
    }
}
