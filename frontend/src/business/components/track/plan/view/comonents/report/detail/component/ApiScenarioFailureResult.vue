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
            prop="creatorName"/>
          <ms-table-column
            :label="$t('test_track.case.priority')"
            :width="80">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.level" ref="priority"/>
            </template>
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
        <ms-api-report :share-id="shareId" :is-share="isShare" :template-report="response" :is-template="isTemplate" :infoDb="true" :report-id="reportId"/>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import {getPlanScenarioFailureCase, getSharePlanScenarioFailureCase} from "@/network/test-plan";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsApiReport from "@/business/components/api/automation/report/ApiReportDetail";
export default {
  name: "ApiScenarioFailureResult",
  components: {
    MsApiReport,
    MsTableColumn, MsTable, StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem},
  props: {
    planId: String,
    isTemplate: Boolean,
    report: Object,
    isShare: Boolean,
    shareId: String
  },
  data() {
    return {
      scenarioCases:  [],
      result: {},
      reportId: null,
      response: {}
    }
  },
  mounted() {
    this.getScenarioApiCase();
  },
  methods: {
    getScenarioApiCase() {
      if (this.isTemplate) {
        this.scenarioCases = this.report.scenarioFailureResult;
        if (this.scenarioCases && this.scenarioCases.length > 0) {
          this.rowClick(this.scenarioCases[0]);
        }
      } else if (this.isShare) {
        this.result = getSharePlanScenarioFailureCase(this.shareId, this.planId, (data) => {
          this.scenarioCases = data;
          if (data && data.length > 0) {
            this.reportId = data[0].reportId;
          }
        });
      } else {
        this.result = getPlanScenarioFailureCase(this.planId, (data) => {
          this.scenarioCases = data;
          if (data && data.length > 0) {
            this.reportId = data[0].reportId;
          }
        });
      }
    },
    rowClick(row) {
      if (this.isTemplate) {
        this.response = row.response;
      } else {
        this.reportId = row.reportId;
      }
    }
  }
}
</script>

<style scoped>
</style>
