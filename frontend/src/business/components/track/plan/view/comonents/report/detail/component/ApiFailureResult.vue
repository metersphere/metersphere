<template>
  <div>
    <el-tabs type="card">
      <el-tab-pane label="接口用例">
        <api-case-failure-result :plan-id="planId"/>
      </el-tab-pane>
      <el-tab-pane label="场景用例">
        <api-scenario-failure-result :plan-id="planId"/>
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
export default {
  name: "ApiFailureResult",
  components: {
    ApiCaseFailureResult,
    ApiScenarioFailureResult, StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem},
  props: {
    planId: String
  },
  data() {
    return {
    }
  },
  mounted() {
  },
  methods: {
  }
}
</script>

<style scoped>

</style>
