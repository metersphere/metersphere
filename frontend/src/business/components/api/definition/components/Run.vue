<template>
  <span></span>
</template>
<script>
import {getBodyUploadFiles, getCurrentProjectID, strMapToObj} from "@/common/js/utils";
import ThreadGroup from "./jmeter/components/thread-group";
import TestPlan from "./jmeter/components/test-plan";

export default {
  name: 'MsRun',
  components: {},
  props: {
    environment: Object,
    debug: Boolean,
    reportId: String,
    runData: Array,
    type: String,
    envMap: Map
  },
  data() {
    return {
      result: {},
      loading: false,
      runId: "",
      reqNumber: 0,
    }
  },

  watch: {
    // 初始化
    reportId() {
      this.run()
    }
  },
  methods: {
    initWebSocket() {
      let protocol = "ws://";
      if (window.location.protocol === 'https:') {
        protocol = "wss://";
      }
      let runMode = this.debug ? "debug" : "run";
      const uri = protocol + window.location.host + "/api/definition/run/report/" + this.runId + "/" + runMode;
      this.websocket = new WebSocket(uri);
      this.websocket.onmessage = this.onMessage;
      this.websocket.onopen = this.onOpen;
      this.websocket.onerror = this.onError;
      this.websocket.onclose = this.onClose;
    },
    onOpen() {
    },
    onError(e) {
      this.$emit('runRefresh', {});
    },
    onMessage(e) {
      if (e.data) {
        let data = JSON.parse(e.data);
        this.$emit('runRefresh', data);
      }
    },
    onClose(e) {
      if (e.code === 1005) {
        return;
      }
    },
    run() {
      let projectId = getCurrentProjectID();
      // 如果envMap不存在，是单接口调用
      if (!this.envMap || this.envMap.size === 0) {
        projectId = getCurrentProjectID();
      } else {
        // 场景步骤下接口调用
        if (this.runData.projectId) {
          projectId = this.runData.projectId;
        }
      }

      let testPlan = new TestPlan();
      let threadGroup = new ThreadGroup();
      threadGroup.hashTree = [];
      testPlan.hashTree = [threadGroup];
      this.runData.forEach(item => {
        item.projectId = projectId;
        threadGroup.hashTree.push(item);
      })
      let reqObj = {id: this.reportId, testElement: testPlan, type: this.type, projectId: projectId, environmentMap: strMapToObj(this.envMap)};
      let bodyFiles = getBodyUploadFiles(reqObj, this.runData);
      if (this.runData[0].url) {
        reqObj.name = this.runData[0].url;
      } else {
        reqObj.name = this.runData[0].path;
      }
      let url = "";
      if (this.debug) {
        reqObj.reportId = this.reportId;
        url = "/api/definition/run/debug";
      } else {
        url = "/api/definition/run";
      }
      this.$fileUpload(url, null, bodyFiles, reqObj, response => {
        this.runId = response.data;
        this.initWebSocket();
        this.$emit('autoCheckStatus');  //   执行结束后，自动更新计划状态
      }, error => {
        this.$emit('errorRefresh', {});
      });
    }
  }
}
</script>
