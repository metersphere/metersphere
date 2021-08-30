<template>
  <div>
    <el-row class="scenario-info">
      <el-col :span="7">

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
            :label="'创建人'"
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
            :label="'执行结果'"
            prop="lastResult">
            <template v-slot:default="scope">
              <status-table-item v-if="scope.row.execResult === 'success'" :value="'Pass'"/>
              <status-table-item v-if="scope.row.execResult === 'error'" :value="'Failure'"/>
              <status-table-item v-if="scope.row.execResult != 'error' && scope.row.execResult != 'success'" :value="'Prepare'"/>
            </template>
          </ms-table-column>
        </ms-table>
      </el-col>
      <el-col :span="17" v-if="apiCases.length > 0">
        <el-card v-if="showResponse">
          <ms-request-result-tail :response="response" ref="debugResult"/>
        </el-card>
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
  getPlanApiAllCase,
  getPlanApiFailureCase,
  getSharePlanApiAllCase,
  getSharePlanApiFailureCase
} from "@/network/test-plan";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import {getApiReport, getShareApiReport} from "@/network/api";
import MsRequestResultTail from "@/business/components/api/definition/components/response/RequestResultTail";
export default {
  name: "ApiCaseFailureResult",
  components: {
    MsRequestResultTail,
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
      apiCases:  [],
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
        if (this.isAll) {
          this.apiCases = this.report.apiAllCases ? this.report.apiAllCases : [];
        } else {
          this.apiCases = this.report.apiFailureCases ? this.report.apiFailureCases : [];
        }
      } else if (this.isShare) {
        if (this.isAll) {
          this.result = getSharePlanApiAllCase(this.shareId, this.planId, (data) => {
            this.apiCases = data;
          });
        } else {
          this.result = getSharePlanApiFailureCase(this.shareId, this.planId, (data) => {
            this.apiCases = data;
          });
        }
      } else {
        if (this.isAll) {
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
        // todo
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
</script>

<style scoped>
</style>
