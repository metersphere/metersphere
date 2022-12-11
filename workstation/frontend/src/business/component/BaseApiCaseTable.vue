<template>
  <div>
    <ms-table
      :table-is-loading="this.loading"
      :data="tableData"
      :condition="condition"
      :page-size="pageSize"
      :total="total"
      :batch-operators="buttons"
      :enable-selection="true"
      :fields.sync="fields"
      :field-key="fieldKey"
      row-key="id"
      operator-width="190px"
      @refresh="change"
      ref="caseTable"
    >
        <span v-for="(item) in fields" :key="item.key">

          <ms-table-column
            prop="num"
            label="ID"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="80px"
            sortable>
            <template slot-scope="scope">
              <el-tooltip :content="$t('commons.edit')">
                <a style="cursor:pointer" @click="openNewCase(scope.row)"> {{ scope.row.num }} </a>
              </el-tooltip>
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="name"
            sortable
            min-width="160px"
            :label="$t('test_track.case.name')">
            <template slot-scope="scope">
              <el-tooltip :content="$t('commons.edit')">
                <a style="cursor:pointer" @click="openNewCase(scope.row)"> {{ scope.row.name }} </a>
              </el-tooltip>
            </template>
          </ms-table-column>

          <ms-table-column
            prop="priority"
            :filters="priorityFilters"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            sortable
            :label="$t('test_track.case.priority')">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.priority"></priority-table-item>
            </template>
          </ms-table-column>

          <ms-table-column
            prop="caseStatus"
            :filters="caseStatusFilters"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            :label="$t('commons.status')">
            <template v-slot:default="scope">
              <plan-status-table-item :value="scope.row.caseStatus"/>
            </template>
          </ms-table-column>

          <ms-table-column
            prop="execResult"
            :filters="statusFilters"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            :label="$t('test_track.plan_view.execute_result')">
            <template v-slot:default="scope">
              <el-link :disabled="!scope.row.execResult || scope.row.execResult==='PENDING'">
                <ms-api-report-status :status="scope.row.execResult"/>
              </el-link>
            </template>
          </ms-table-column>

           <ms-table-column
             prop="passRate"
             :field="item"
             :fields-width="fieldsWidth"
             min-width="100px"
             :label="$t('commons.pass_rate')">
           </ms-table-column>

          <ms-table-column
            sortable="custom"
            prop="path"
            min-width="180px"
            :field="item"
            :fields-width="fieldsWidth"
            :label="'API'+ $t('api_test.definition.api_path')"/>

          <ms-table-column v-if="item.id==='tags'" prop="tags" width="120px" :label="$t('commons.tag')">
            <template v-slot:default="scope">
              <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                      :show-tooltip="scope.row.tags.length===1&&itemName.length*12<=120"
                      :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
              <span/>
            </template>
          </ms-table-column>

           <ms-table-column
             :label="$t('project.version.name')"
             :field="item"
             :fields-width="fieldsWidth"
             :filters="versionFilters"
             min-width="100px"
             prop="versionId">
          <template v-slot:default="scope">
            <span>{{ scope.row.versionName }}</span>
          </template>
        </ms-table-column>

          <ms-table-column
            prop="environment"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.environment')"
          >
          </ms-table-column>

          <ms-table-column
            prop="createUser"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.create_user')"/>

          <ms-table-column
            sortable="updateTime"
            min-width="160px"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('api_test.definition.api_last_time')"
            prop="updateTime">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | datetimeFormat }}</span>
            </template>
          </ms-table-column>
          <ms-table-column
            prop="createTime"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.create_time')"
            sortable
            min-width="180px">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | datetimeFormat }}</span>
            </template>
          </ms-table-column>
        </span>


    </ms-table>

    <ms-table-pagination
      :change="change"
      :current-page.sync="currentPage"
      :page-size.sync="pageSize"
      :total="total"/>

  </div>
</template>

<script>
import PriorityTableItem from "@/business/module/track/PriorityTableItem";
import MsTag from "metersphere-frontend/src/components/MsTag";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import {getCustomTableHeader, getCustomTableWidth} from "metersphere-frontend/src/utils/tableUtils";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import PlanStatusTableItem from "@/business/module/plan/PlanStatusTableItem";
import {REPORT_STATUS} from "@/business/component/js/commons";
import {getApiReportDetail} from "@/api/api";
import MsApiReportStatus from "@/business/module/api/ApiReportStatus";

export default {
  name: "BaseApiCaseTable",
  data() {
    return {
      currentPage: 1,
      pageSize: 10,
      fields: getCustomTableHeader('API_CASE', []),
      fieldsWidth: getCustomTableWidth('API_CASE'),
      fieldKey: "API_CASE",
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      statusFilters: REPORT_STATUS,
      caseStatusFilters: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
        {text: this.$t('test_track.plan.plan_status_completed'), value: 'Completed'},
      ],
    }
  },
  props: {
    loading: Boolean,
    tableData: Array,
    buttons: Array,
    total: Number,
    versionFilters: Array,
    apiDefinitionId: String,
    condition: {
      type: Object,
      default() {
        return {};
      }
    },
  },
  components: {
    PriorityTableItem,
    MsTag,
    MsTable,
    MsTableColumn,
    MsTablePagination,
    PlanStatusTableItem,
    MsApiReportStatus
  },
  methods: {
    change() {
      this.$emit("getCaseListById",
        this.apiDefinitionId,
        this.currentPage,
        this.pageSize
      );
    },
    getSelectIds() {
      return this.$refs.caseTable.selectIds
    },
    openNewCase(row) {
      this.$emit('openNewCase', row)
    },
    getStatusClass(status) {
      switch (status) {
        case "success":
          return "ms-success";
        case "error":
          return "ms-error";
        case "Running":
          return "ms-running";
        default:
          return "ms-unexecute";
      }
    },
    getStatusTitle(status) {
      switch (status) {
        case "success":
          return this.$t('api_test.automation.success');
        case "error":
          return this.$t('api_test.automation.fail');
        case "Running":
          return this.$t('commons.testing');
        default:
          return this.$t('api_test.home_page.detail_card.unexecute');
      }
    },
  }
}
</script>

<style scoped>

</style>
