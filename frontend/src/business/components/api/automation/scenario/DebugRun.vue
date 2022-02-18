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
    executeType: String,
    runMode: String,
    debug: Boolean,
    reportId: String,
    runData: Object,
    saved: Boolean,
    environmentType: String,
    environmentGroupId: String
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
    sort(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (stepArray[i] && TYPE_TO_C.get(stepArray[i].type) && !stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
            stepArray[i].document = {type: "JSON", data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}};
          }
          if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
            stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.sort(stepArray[i].hashTree);
          }
        }
      }
    },
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
      this.sort(testPlan.hashTree);
      let reqObj = {
        id: this.reportId, reportId: this.reportId, scenarioName: this.runData.name, saved: this.saved, runMode: this.runMode, executeType: this.executeType,
        scenarioId: this.runData.id, testElement: testPlan, projectId: getCurrentProjectID(), environmentMap: strMapToObj(map),
        environmentType: this.environmentType, environmentGroupId: this.environmentGroupId, environmentJson: JSON.stringify(strMapToObj(map))
      };
      if (this.runData.variables) {
        reqObj.variables = this.runData.variables;
      }
      this.$emit('runRefresh', {});
      saveScenario('/api/automation/run/debug', reqObj, this.runData.hashTree, this, (response) => {
        this.runId = response.data;
      });
    },
  }
}
</script>
