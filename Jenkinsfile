#!groovy

pipeline {
    agent {
        node {
            label 'metersphere'
        }
    }
    triggers {
       // pollSCM('50 * * * *')
    }
    environment {
        IMAGE_PREFIX = 'registry.fit2cloud.com/metersphere'
        IMAGE_NAME = 'metersphere'
        JAVA_HOME = '/opt/jdk-21'
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
                        export JAVA_HOME=/opt/jdk-21
                        export CLASSPATH=$JAVA_HOME/lib:$CLASSPATH
                        export PATH=$JAVA_HOME/bin:/opt/apache-maven-3.8.3/bin:$PATH
                        java -version
                        ./mvnw deploy -N -Drevision=${REVISION} --settings ./settings.xml
                        ./mvnw clean deploy -T 1C -B -Drevision=${REVISION} -DskipTests -DskipAntRunForJenkins -pl !app --file backend/pom.xml  --settings ./settings.xml
                    '''
                }
            }
        }
        stage('Build/Test') {
            when { environment name: 'BUILD_SDK', value: 'false' }
            steps {
                configFileProvider([configFile(fileId: 'metersphere-maven', targetLocation: 'settings.xml')]) {
                    sh '''#!/bin/bash -xe
                        export JAVA_HOME=/opt/jdk-21
                        export CLASSPATH=$JAVA_HOME/lib:$CLASSPATH
                        export PATH=$JAVA_HOME/bin:/opt/apache-maven-3.8.3/bin:$PATH
                        java -version
                        
                        # 删除本地缓存
                        LOCAL_REPOSITORY=$(./mvnw help:evaluate -Dexpression=settings.localRepository --settings ./settings.xml -q -DforceStdout)
                        rm -rf $LOCAL_REPOSITORY/io/metersphere/metersphere-jmeter-assertions/*
                        rm -rf $LOCAL_REPOSITORY/io/metersphere/metersphere-jmeter-functions/*
                        rm -rf $LOCAL_REPOSITORY/io/metersphere/monitoring-engine/*

                        ./mvnw clean install -Drevision=${REVISION} -DskipTests --settings ./settings.xml
                        mkdir -p backend/app/target/dependency && (cd backend/app/target/dependency && jar -xf ../*.jar);
                    '''
                }
            }
        }
        stage('Community build & push') {
            when { environment name: 'BUILD_SDK', value: 'false' }
            steps {
                configFileProvider([configFile(fileId: 'metersphere-maven', targetLocation: 'settings.xml')]) {
                    sh '''#!/bin/bash -xe
                    export JAVA_HOME=/opt/jdk-21
                    export CLASSPATH=$JAVA_HOME/lib:$CLASSPATH
                    export PATH=$JAVA_HOME/bin:/opt/apache-maven-3.8.3/bin:$PATH
                    
                    LOCAL_REPOSITORY=$(./mvnw help:evaluate -Dexpression=settings.localRepository --settings ./settings.xml -q -DforceStdout)
    
                    libraries=('general-xpack-impl')
                    for library in "${libraries[@]}";
                    do
                        cp -rf $LOCAL_REPOSITORY/io/metersphere/$library/${REVISION}/$library-${REVISION}.jar backend/app/target/dependency/BOOT-INF/lib/
                    done
    
                    docker --config /home/metersphere/.docker buildx build --no-cache --build-arg MS_VERSION=\${TAG_NAME:-\$BRANCH_NAME}-\${GIT_COMMIT:0:8} -t ${IMAGE_PREFIX}/${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME} --platform linux/amd64,linux/arm64 . --push
                    '''
                }
            }
        }
        stage('Build Standalone') {
            when {
                expression {
                    def reg = "^\\d+\\.\\w+\$"
                    return env.REVISION.matches(reg) && env.BUILD_SDK == 'false'
                }
            }
            steps {
                script {
                    build job: "../metersphere-standalone/${BRANCH_NAME}", quietPeriod: 10, wait: false
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
