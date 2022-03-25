<template>
  <div>
    <el-tabs type="card">
      <el-tab-pane>
        <template v-slot:label>
          <tab-pane-count :title="$t('commons.api_case')" :count="apiSize"/>
        </template>
        <api-case-failure-result :is-db="isDb" :is-all="isAll" :is-error-report="isErrorReport" :is-un-execute="isUnExecute" :share-id="shareId" :is-share="isShare"
                                 :report="report" :is-template="isTemplate" :plan-id="planId" @setSize="setApiSize"/>
      </el-tab-pane>
      <el-tab-pane>
        <template v-slot:label>
          <tab-pane-count :title="$t('commons.scenario_case')" :count="scenarioSize"/>
        </template>
        <api-scenario-failure-result :is-db="isDb" :is-all="isAll" :is-error-report="isErrorReport" :is-un-execute="isUnExecute" :share-id="shareId" :is-share="isShare"
                                     :report="report" :is-template="isTemplate" :plan-id="planId"
                                     @setSize="setScenarioSize"/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import ApiScenarioFailureResult
  from "@/business/components/track/plan/view/comonents/report/detail/component/ApiScenarioFailureResult";
import ApiCaseFailureResult
  from "@/business/components/track/plan/view/comonents/report/detail/component/ApiCaseFailureResult";
import TabPaneCount from "@/business/components/track/plan/view/comonents/report/detail/component/TabPaneCount";

export default {
  name: "ApiCases",
  components: {
    TabPaneCount,
    ApiCaseFailureResult,
    ApiScenarioFailureResult, StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem
  },
  props: {
    planId: String,
    isTemplate: Boolean,
    isShare: Boolean,
    report: {},
    shareId: String,
    isAll: Boolean,
    isErrorReport: Boolean,
    isUnExecute:Boolean,
    isDb: Boolean
  },
  data() {
    return {
      apiSize: 0,
      scenarioSize: 0,
    }
  },
  mounted() {
  },
  watch: {
    apiSize() {
      this.$emit('setSize', this.apiSize + this.scenarioSize);
    },
    scenarioSize() {
      this.$emit('setSize', this.apiSize + this.scenarioSize);
    },
  },
  methods: {
    setApiSize(size) {
      this.apiSize = size;
    },
    setScenarioSize(size) {
      this.scenarioSize = size;
    },
  }
}
</script>

<style scoped>

</style>
