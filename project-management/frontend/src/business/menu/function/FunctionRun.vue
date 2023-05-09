<template>
  <span></span>
</template>
<script>
import {getSocket, runCodeSnippet} from "../../../api/custom-func";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export default {
  name: 'FunctionRun',
  components: {},
  props: {
    environment: Object,
    debug: Boolean,
    reportId: String,
    runData: Object,
    type: String,
    envMap: Map,
    isStop: Boolean,
  },
  data() {
    return {
      result: {},
      loading: false,
      requestResult: "",
      reqNumber: 0,
      websocket: {}
    }
  },

  watch: {
    // 初始化
    reportId() {
      this.debugSocket()
    },
    isStop() {
      if (!this.isStop && this.websocket && this.websocket.close instanceof Function) {
        this.websocket.close();
      }
    }
  },
  methods: {
    onOpen() {
      this.run();
    },
    onDebugMessage(e) {
      if (e.data && e.data.startsWith("result_")) {
        try {
          let data = e.data.substring(7);
          this.websocket.close();
          this.$emit('runRefresh', JSON.parse(data));
        } catch (e) {
          this.websocket.close();
          this.$emit('runRefresh', "");
        }
      }
    },
    debugSocket() {
      this.websocket = getSocket(this.reportId);
      this.websocket.onmessage = this.onDebugMessage;
      this.websocket.onopen = this.onOpen;
    },
    run() {
      this.runData.id = this.reportId;
      this.runData.projectId = getCurrentProjectID();
      runCodeSnippet(this.runData).then(res => {
        this.requestResult = res.data;
      }).catch(() => {
        this.$emit('errorRefresh', {});
      })
    }
  }
}
</script>
