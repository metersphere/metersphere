<template>
    <div class="container">
      <el-table
        row-key="id"
        :data="failureTestCases">
        <el-table-column
          prop="num"
          :label="$t('commons.id')"
          show-overflow-tooltip>
          <template v-slot:default="{row}">
            {{row.isCustomNum ? row.customNum : row.num }}
          </template>
        </el-table-column>
        <el-table-column
          prop="name"
          :label="$t('commons.name')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="priority"
          column-key="priority"
          :label="$t('test_track.case.priority')">
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority" ref="priority"/>
          </template>
        </el-table-column>

<!--        <el-table-column-->
<!--          prop="nodePath"-->
<!--          :label="$t('test_track.case.module')"-->
<!--          show-overflow-tooltip>-->
<!--        </el-table-column>-->

        <el-table-column
          prop="projectName"
          :label="$t('test_track.case.project_name')"
          show-overflow-tooltip>
        </el-table-column>

        <el-table-column
          prop="executorName"
          :label="$t('test_track.plan_view.executor')">
        </el-table-column>

        <el-table-column
          prop="status"
          column-key="status"
          :label="$t('test_track.plan_view.execute_result')">
          <template v-slot:default="scope">
            <status-table-item :value="'Failure'"/>
          </template>
        </el-table-column>

        <el-table-column
          prop="updateTime"
          :label="$t('commons.update_time')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

</template>

<script>
import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import {getPlanFunctionFailureCase, getSharePlanFunctionFailureCase} from "@/network/test-plan";
export default {
  name: "FunctionalFailureResult",
  components: {StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem},
  props: {
    planId: String,
    isTemplate: Boolean,
    isShare: Boolean,
    report: {},
    shareId: String
  },
  data() {
    return {
      failureTestCases:  []
    }
  },
  mounted() {
    this.getFailureTestCase();
  },
  methods: {
    getFailureTestCase() {
      if (this.isTemplate) {
        this.failureTestCases = this.report.functionFailureCases;
      } else if (this.isShare) {
        getSharePlanFunctionFailureCase(this.shareId, this.planId, (data) => {
          this.failureTestCases = data;
        });
      } else {
        getPlanFunctionFailureCase(this.planId, (data) => {
          this.failureTestCases = data;
        });
      }
    }
  }
}
</script>

<style scoped>

</style>
