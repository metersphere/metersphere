<template>
  <div>
    <el-row class="scenario-info">
      <el-col :span="8">

        <ms-table v-loading="result.loading"
                  :show-select-all="false"
                  :screen-height="null"
                  :enable-selection="false"
                  :highlight-current-row="true"
                  @refresh="getScenarioApiCase"
                  @handleRowClick="rowClick"
                  :data="scenarioCases">

          <ms-table-column
            :width="80"
            :label="$t('commons.id')"
            prop="customNum">
          </ms-table-column>

          <ms-table-column
            :label="$t('commons.name')"
            prop="name">
          </ms-table-column>

          <ms-table-column
            :label="'创建人'"
            prop="creatorName">

          <ms-table-column
            :label="$t('test_track.case.priority')"
            :width="80"
            prop="level">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.level" ref="priority"/>
            </template>
          </ms-table-column>

          </ms-table-column>
          <ms-table-column
            :width="70"
            :label="'步骤数'"
            prop="stepTotal">
          </ms-table-column>
          <ms-table-column
            :width="80"
            :label="'执行结果'"
            prop="lastResult">
            <status-table-item :value="'Failure'"/>
          </ms-table-column>
        </ms-table>
      </el-col>
      <el-col :span="16" v-if="scenarioCases.length > 0">
        <ms-api-report @refresh="search" :infoDb="true" :report-id="reportId"/>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import {getPlanScenarioFailureCase} from "@/network/test-plan";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsApiReport from "@/business/components/api/automation/report/ApiReportDetail";
export default {
  name: "ApiScenarioFailureResult",
  components: {
    MsApiReport,
    MsTableColumn, MsTable, StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem},
  props: {
    planId: String
  },
  data() {
    return {
      scenarioCases:  [],
      result: {},
      report: {},
      reportId: null
    }
  },
  mounted() {
    this.getScenarioApiCase();
  },
  methods: {
    getScenarioApiCase() {
      this.result = getPlanScenarioFailureCase(this.planId, (data) => {
        this.scenarioCases = data;
        if (data && data.length > 0) {
          this.reportId = data[0].reportId;
        }
      });
    },
    rowClick(row) {
      this.reportId = row.reportId;
    }
  }
}
</script>

<style scoped>
</style>
