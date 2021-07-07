<template>
  <div></div>
</template>
<script>
import {getCurrentProjectID, strMapToObj} from "@/common/js/utils";
  import {createComponent} from "../../definition/components/jmeter/components";
import {saveScenario} from "@/business/components/api/automation/api-automation";

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
        let threadGroup = createComponent('ThreadGroup');
        threadGroup.hashTree = [];
        threadGroup.name = this.reportId;
        threadGroup.enableCookieShare = this.runData.enableCookieShare;
        threadGroup.onSampleError = this.runData.onSampleError;
        let map = this.environment;
        this.runData.projectId = getCurrentProjectID();
        threadGroup.hashTree.push(this.runData);
        testPlan.hashTree.push(threadGroup);
        let reqObj = {id: this.reportId, reportId: this.reportId, scenarioName: this.runData.name,saved:this.saved,
          scenarioId: this.runData.id, testElement: testPlan, projectId: getCurrentProjectID(), environmentMap: strMapToObj(map)};
        saveScenario('/api/automation/run/debug', reqObj, this.runData.hashTree, (response) => {
          this.runId = response.data;
          this.$emit('runRefresh', {});
        });
      },
    }
  }
</script>
