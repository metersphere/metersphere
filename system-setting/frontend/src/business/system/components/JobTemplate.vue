<template>
  <ms-drawer :visible="dialogVisible" :size="50" @close="handleClose" direction="right"
             :show-full-screen="false" :is-show-close="false">
    <div>
      <el-row class="header">
        <el-col :span="24" class="buttons">
          <el-button size="mini" @click="handleClose">{{ $t('commons.cancel') }}</el-button>
          <el-button type="primary" size="mini" @click="confirm" @keydown.enter.native.prevent>
            {{ $t('commons.confirm') }}
          </el-button>
        </el-col>
      </el-row>
      <div class="ms-code">
        <ms-code-edit class="ms-code" :enable-format="false" mode="yaml" :data.sync="template" theme="eclipse"
                      :modes="['yaml']"
                      ref="codeEdit"/>
      </div>
    </div>
  </ms-drawer>
</template>

<script>
const TEMPLATE = `apiVersion: batch/v1
kind: Job
metadata:
  labels:
    test-id: \${TEST_ID}
  name: \${JOB_NAME}
spec:
  parallelism: 1
  template:
    metadata:
      labels:
        test-id: \${TEST_ID}
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - podAffinityTerm:
                labelSelector:
                  matchExpressions:
                    - key: test-id
                      operator: In
                      values:
                        - \${TEST_ID}
                topologyKey: kubernetes.io/hostname
              weight: 100
      containers:
        - command:
            - sh
            - -c
            - /run-test.sh
          env:
            - name: START_TIME
              value: "\${START_TIME}"
            - name: GRANULARITY
              value: "\${GRANULARITY}"
            - name: JMETER_REPORTS_TOPIC
              value: \${JMETER_REPORTS_TOPIC}
            - name: METERSPHERE_URL
              value: \${METERSPHERE_URL}
            - name: RESOURCE_ID
              value: \${RESOURCE_ID}
            - name: BACKEND_LISTENER
              value: "\${BACKEND_LISTENER}"
            - name: BOOTSTRAP_SERVERS
              value: \${BOOTSTRAP_SERVERS}
            - name: RATIO
              value: "\${RATIO}"
            - name: TEST_ID
              value: \${TEST_ID}
            - name: THREAD_NUM
              value: "\${THREAD_NUM}"
            - name: HEAP
              value: \${HEAP}
            - name: REPORT_ID
              value: \${REPORT_ID}
            - name: RESOURCE_INDEX
              value: "\${RESOURCE_INDEX}"
            - name: LOG_TOPIC
              value: \${LOG_TOPIC}
            - name: GC_ALGO
              value: \${GC_ALGO}
          image: \${JMETER_IMAGE}
          imagePullPolicy: IfNotPresent
          name: jmeter
          ports:
            - containerPort: 60000
              protocol: TCP
          volumeMounts:
            - mountPath: /test
              name: test-files
            - mountPath: /jmeter-log
              name: log-files
      restartPolicy: Never
      volumes:
        - emptyDir: {}
          name: test-files
        - emptyDir: {}
          name: log-files
`;

import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
import MsDrawer from "metersphere-frontend/src/components/MsDrawer";

export default {
  name: "JobTemplate",
  components: {
    MsDrawer,
    MsDialogFooter,
    MsCodeEdit
  },
  data() {
    return {
      dialogVisible: false,
      template: '',
    };
  },
  methods: {
    open(template) {
      this.dialogVisible = true;
      this.template = template || TEMPLATE;
      listenGoBack(this.handleClose);
    },
    handleClose() {
      this.template = TEMPLATE;
      this.dialogVisible = false;
      removeGoBackListener(this.handleClose);
    },
    confirm() {
      this.dialogVisible = false;
      this.$emit("saveJobTemplate", this.template);
    }
  }
}
</script>

<style scoped>

.ms-drawer {
  padding: 10px;
}

.ms-code {
  height: calc(97vh);
}

.buttons .el-button {
  float: right;
}

.buttons .el-button:nth-child(2) {
  margin-right: 15px;
}

.header {
  position: fixed;
  top: 15px;
  right: 50px;
  z-index: 10000;
}
</style>
