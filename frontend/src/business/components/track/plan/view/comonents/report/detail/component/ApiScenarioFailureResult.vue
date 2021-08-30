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
            <template v-slot:default="{row}">
              <status-table-item v-if="row.lastResult === 'Success'" :value="'Pass'"/>
              <status-table-item v-if="row.lastResult === 'Fail'" :value="'Failure'"/>
              <status-table-item v-if="row.lastResult != 'Fail' && row.lastResult != 'Success'" :value="'Prepare'"/>
            </template>
          </ms-table-column>
        </ms-table>
      </el-col>
      <el-col :span="16" v-if="scenarioCases && scenarioCases.length > 0">
        <ms-api-report v-if="showResponse" :share-id="shareId" :is-share="isShare" :template-report="response" :is-template="isTemplate" :infoDb="true" :report-id="reportId"/>
        <div class="empty" v-else>内容为空</div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import {
  getPlanScenarioAllCase,
  getPlanScenarioFailureCase,
  getSharePlanScenarioAllCase,
  getSharePlanScenarioFailureCase
} from "@/network/test-plan";
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
    shareId: String,
    isAll: Boolean,
    isDb: Boolean
  },
  data() {
    return {
      scenarioCases:  [],
      result: {},
      reportId: null,
      response: {},
      showResponse: false
    }
  },
  mounted() {
    this.getScenarioApiCase();
  },
  watch: {
    scenarioCases() {
      if (this.scenarioCases) {
        this.$emit('setSize', this.scenarioCases.length);
      }
    }
  },
  methods: {
    getScenarioApiCase() {
      if (this.isTemplate || this.isDb) {
        if (this.isAll) {
          this.scenarioCases = this.report.scenarioAllCases ? this.report.scenarioAllCases : [];
        } else {
          this.scenarioCases = this.report.scenarioFailureCases ? this.report.scenarioFailureCases : [];
        }
      } else if (this.isShare) {
        if (this.isAll) {
          this.result = getSharePlanScenarioAllCase(this.shareId, this.planId, (data) => {
            this.scenarioCases = data;
          });
        } else {
          this.result = getSharePlanScenarioFailureCase(this.shareId, this.planId, (data) => {
            this.scenarioCases = data;
          });
        }
      } else {
        if (this.isAll) {
          this.result = getPlanScenarioAllCase(this.planId, (data) => {
            this.scenarioCases = data;
          });
        } else {
          this.result = getPlanScenarioFailureCase(this.planId, (data) => {
            this.scenarioCases = data;
          });
        }
      }
    },
    rowClick(row) {
      this.showResponse = false;
      if (this.isTemplate) {
        if (row.response) {
          this.showResponse = true;
          this.response = row.response;
        }
      } else {
        if (row.reportId) {
          this.showResponse = true;
          this.reportId = row.reportId;
        }
      }
    }
  }
}
</script>

<style scoped>
</style>
