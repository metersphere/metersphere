<template>
  <div></div>
</template>
<script>
import {getCurrentProjectID, strMapToObj} from "@/common/js/utils";
import {createComponent} from "../../definition/components/jmeter/components";
import {saveScenario} from "@/business/components/api/automation/api-automation";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";

export default {
  name: 'MsDebugRun',
  components: {},
  props: {
    environment: Map,
    debug: Boolean,
    reportId: String,
    runData: Object,
    saved: Boolean,
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
    run() {
      let testPlan = createComponent('TestPlan');
      testPlan.clazzName = TYPE_TO_C.get(testPlan.type);
      let threadGroup = createComponent('ThreadGroup');
      threadGroup.clazzName = TYPE_TO_C.get(threadGroup.type);
      threadGroup.hashTree = [];
      threadGroup.name = this.reportId;
      threadGroup.enableCookieShare = this.runData.enableCookieShare;
      threadGroup.onSampleError = this.runData.onSampleError;
      let map = this.environment;
      this.runData.projectId = getCurrentProjectID();
      this.runData.clazzName = TYPE_TO_C.get(this.runData.type);
      threadGroup.hashTree.push(this.runData);
      testPlan.hashTree.push(threadGroup);
      let reqObj = {
        id: this.reportId, reportId: this.reportId, scenarioName: this.runData.name, saved: this.saved,
        scenarioId: this.runData.id, testElement: testPlan, projectId: getCurrentProjectID(), environmentMap: strMapToObj(map)
      };
      saveScenario('/api/automation/run/debug', reqObj, this.runData.hashTree, this, (response) => {
        this.runId = response.data;
        this.$emit('runRefresh', {});
      });
    },
  }
}
</script>
