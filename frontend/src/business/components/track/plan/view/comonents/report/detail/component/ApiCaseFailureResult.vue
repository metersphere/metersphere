<template>
  <el-container>
    <ms-aside-container width="500px" :default-hidden-bottom-top="200" :enable-auto-height="true">
      <el-card>
        <el-scrollbar>
          <ms-table v-loading="result.loading"
                    :show-select-all="false"
                    :screen-height="null"
                    :enable-selection="false"
                    :highlight-current-row="true"
                    @refresh="getScenarioApiCase"
                    @handleRowClick="rowClick"
                    :data="apiCases">

            <ms-table-column
                :width="80"
                :label="$t('commons.id')"
                prop="num">
            </ms-table-column>

            <ms-table-column
                :label="$t('commons.name')"
                prop="name">
            </ms-table-column>

            <ms-table-column
                :label="$t('commons.create_user')"
                prop="creatorName"/>

            <ms-table-column
                :label="$t('test_track.case.priority')"
                :width="80"
                prop="priority">
              <template v-slot:default="scope">
                <priority-table-item :value="scope.row.priority" ref="priority"/>
              </template>
            </ms-table-column>

            <ms-table-column
                :width="80"
                :label="$t('test_track.plan_view.execute_result')"
                prop="lastResult">
              <template v-slot:default="scope">
                <status-table-item v-if="scope.row.execResult === 'success'" :value="'Pass'"/>
                <status-table-item v-else-if="scope.row.execResult === 'error'" :value="'Failure'"/>
                <status-table-item v-else-if="scope.row.execResult === 'STOP'" :value="'STOP'"/>
                <status-table-item v-else-if="scope.row.execResult === 'errorReportResult'"
                                   :value="'ErrorReportResult'"/>
                <status-table-item v-else-if="scope.row.execResult === 'Timeout'" :value="'Timeout'"/>
                <status-table-item v-else :value="'Prepare'"/>
              </template>
            </ms-table-column>
          </ms-table>
        </el-scrollbar>
      </el-card>
    </ms-aside-container>
    <ms-main-container>
      <el-card v-if="showResponse">
        <ms-request-result-tail :response="response" ref="debugResult"/>
      </el-card>
      <div class="empty" v-else>{{ $t('test_track.plan.load_case.content_empty') }}</div>
    </ms-main-container>
  </el-container>
</template>

<script>
import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import {
  getPlanApiAllCase, getPlanApiErrorReportCase,
  getPlanApiFailureCase, getPlanApiUnExecuteCase,
  getSharePlanApiAllCase, getSharePlanApiErrorReportCase,
  getSharePlanApiFailureCase, getSharePlanApiUnExecuteCase
} from "@/network/test-plan";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import {getApiReport, getShareApiReport} from "@/network/api";
import MsRequestResultTail from "@/business/components/api/definition/components/response/RequestResultTail";
import MsAsideContainer from "@/business/components/common/components/MsAsideContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";

export default {
  name: "ApiCaseFailureResult",
  components: {
    MsMainContainer,
    MsAsideContainer,
    MsRequestResultTail,
    MsTableColumn, MsTable, StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem
  },
  props: {
    planId: String,
    isTemplate: Boolean,
    report: Object,
    isShare: Boolean,
    shareId: String,
    isAll: Boolean,
    isErrorReport: Boolean,
    isUnExecute: Boolean,
    isDb: Boolean
  },
  data() {
    return {
      apiCases: [],
      result: {},
      response: {},
      showResponse: false
    }
  },
  watch: {
    apiCases() {
      if (this.apiCases) {
        this.$emit('setSize', this.apiCases.length);
      } else {
        this.apiCases = [];
      }
    }
  },
  mounted() {
    this.getScenarioApiCase();
  },
  methods: {
    getScenarioApiCase() {
      if (this.isTemplate || this.isDb) {
        if (this.isErrorReport) {
          this.apiCases = this.report.errorReportCases ? this.report.errorReportCases : [];
        } else if (this.isUnExecute) {
          this.apiCases = this.report.unExecuteCases ? this.report.unExecuteCases : [];
        } else if (this.isAll) {
          this.apiCases = this.report.apiAllCases ? this.report.apiAllCases : [];
        } else {
          this.apiCases = this.report.apiFailureCases ? this.report.apiFailureCases : [];
        }
      } else if (this.isShare) {
        if (this.isErrorReport) {
          this.result = getSharePlanApiErrorReportCase(this.shareId, this.planId, (data) => {
            this.apiCases = data;
          });
        } else if (this.isUnExecute) {
          this.result = getSharePlanApiUnExecuteCase(this.shareId, this.planId, (data) => {
            this.apiCases = data;
          });
        } else if (this.isAll) {
          this.result = getSharePlanApiAllCase(this.shareId, this.planId, (data) => {
            this.apiCases = data;
          });
        } else {
          this.result = getSharePlanApiFailureCase(this.shareId, this.planId, (data) => {
            this.apiCases = data;
          });
        }
      } else {
        if (this.isErrorReport) {
          this.result = getPlanApiErrorReportCase(this.planId, (data) => {
            this.apiCases = data;
          });
        } else if (this.isUnExecute) {
          this.result = getPlanApiUnExecuteCase(this.planId, (data) => {
            this.apiCases = data;
          });
        } else if (this.isAll) {
          this.result = getPlanApiAllCase(this.planId, (data) => {
            this.apiCases = data;
          });
        } else {
          this.result = getPlanApiFailureCase(this.planId, (data) => {
            this.apiCases = data;
          });
        }
      }
    },
    rowClick(row) {
      this.showResponse = false;
      if (this.isTemplate) {
        if (row.response) {
          this.showResponse = true;
          this.response = JSON.parse(row.response);
        }
      } else if (this.isShare) {
        getShareApiReport(this.shareId, row.id, (data) => {
          if (data && data.content) {
            this.showResponse = true;
            this.response = JSON.parse(data.content);
          }
        });
      } else {
        if (row.reportId) {
          let url = "/api/definition/report/get/" + row.reportId;
          this.$get(url, response => {
            if (response.data) {
              let data = response.data;
              if (data && data.content) {
                this.showResponse = true;
                try {
                  this.response = JSON.parse(data.content);
                } catch (e) {
                  this.response = {};
                }
              }
            }
          });
        } else {
          getApiReport(row.id, (data) => {
            if (data && data.content) {
              this.showResponse = true;
              this.response = JSON.parse(data.content);
            }
          });
        }
      }
    }
  }
}
</script>

<style scoped>

.el-card >>> .el-card__body {
  height: 550px;
}

/deep/ .text-container .pane {
  height: 550px !important;
}

.ms-aside-container {
  border: 0px;
  height: 550px;
  padding: 10px 0px 0px 10px;
}

.ms-main-container {
  height: 580px;
}
</style>
