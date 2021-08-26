<template>
  <div class="card-container">
    <ms-table-header :condition.sync="condition" @search="initTableData"
                     :show-create="false" :tip="$t('commons.search_by_id_name_tag')">

      <!-- 不显示 “全部用例” 标题,使标题为空 -->
      <template v-slot:title>
        <span></span>
      </template>

      <template v-slot:button>
        <ms-table-button v-permission="['PROJECT_TRACK_CASE:READ']" v-if="!showMyTestCase" icon="el-icon-s-custom"
                         :content="$t('test_track.plan_view.my_case')" @click="searchMyTestCase"/>
        <ms-table-button v-permission="['PROJECT_TRACK_CASE:READ']" v-if="showMyTestCase" icon="el-icon-files"
                         :content="$t('test_track.plan_view.all_case')" @click="searchMyTestCase"/>
        <ms-table-button v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']" icon="el-icon-connection"
                         :content="$t('test_track.plan_view.relevance_test_case')"
                         @click="$emit('openTestCaseRelevanceDialog')"/>
      </template>
    </ms-table-header>

    <ms-table
      v-loading="result.loading"
      field-key="TEST_PLAN_FUNCTION_TEST_CASE"
      :data="tableData"
      :condition="condition"
      :total="total"
      :page-size.sync="pageSize"
      :operators="operators"
      :screen-height="screenHeight"
      :batch-operators="buttons"
      @handlePageChange="initTableData"
      @handleRowClick="handleEdit"
      :fields.sync="fields"
      @refresh="initTableData"
      ref="table">

      <span v-for="item in fields" :key="item.key">
        <ms-table-column
          v-if="item.id == 'num'"
          prop="customNum"
          sortable="custom"
          :fields-width="fieldsWidth"
          :label="$t('commons.id')"
          min-width="120px"/>

        <ms-table-column
          prop="name"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.name')"
          min-width="120px"/>

        <ms-table-column
          prop="priority"
          :field="item"
          :fields-width="fieldsWidth"
          :filters="priorityFilters"
          sortable="custom"
          min-width="120px"
          :label="$t('test_track.case.priority')">
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority" ref="priority"/>
          </template>
        </ms-table-column>

        <ms-table-column
            prop="tags"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.tag')"
            min-width="120px">
          <template v-slot:default="scope">
            <ms-tag v-for="(tag, index) in scope.row.showTags" :key="tag + '_' + index" type="success" effect="plain"
                    :content="tag" style="margin-left: 0px; margin-right: 2px"/>
            <span/>
          </template>
        </ms-table-column>
          <ms-table-column
              sortable
              prop="createTime"
              :field="item"
              :fields-width="fieldsWidth"
              :label="$t('commons.create_time')"
              min-width="120px">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
            prop="nodePath"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('test_track.case.module')"
            min-width="120px"/>

        <ms-table-column
            prop="projectName"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.plan.plan_project')"
          min-width="120px"/>

        <ms-table-column
          prop="issuesContent"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.issue.issue')"
          min-width="80px">
          <template v-slot:default="scope">
            <el-popover
              placement="right"
              width="400"
              trigger="hover">
              <el-table border class="adjust-table" :data="scope.row.issuesContent" style="width: 100%">
                <ms-table-column prop="title" :label="$t('test_track.issue.title')" show-overflow-tooltip/>
                <ms-table-column prop="description" :label="$t('test_track.issue.description')">
                  <template v-slot:default="scope">
                    <el-popover
                      placement="left"
                      width="400"
                      trigger="hover"
                    >
                      <ckeditor :editor="editor" disabled :config="editorConfig"
                                v-model="scope.row.description"/>
                      <el-button slot="reference" type="text">{{ $t('test_track.issue.preview') }}</el-button>
                    </el-popover>
                  </template>
                </ms-table-column>
                <ms-table-column prop="platform" :label="$t('test_track.issue.platform')"/>
              </el-table>
              <el-button slot="reference" type="text">{{ scope.row.issuesSize }}</el-button>
            </el-popover>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="executor"
          :filters="executorFilters"
          min-width="100px"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.plan_view.executor')">
          <template v-slot:default="scope">
            {{scope.row.executorName}}
          </template>
        </ms-table-column>

        <!-- 责任人(创建该用例时所关联的责任人) -->
        <ms-table-column
          prop="maintainerName"
          :filters="maintainerFilters"
          min-width="100px"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('api_test.definition.request.responsible')"/>

        <ms-table-column
          prop="status"
          :filters="statusFilters"
          min-width="100px"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.plan_view.execute_result')">
          <template v-slot:default="scope">
            <span @click.stop="clickt = 'stop'">
              <el-dropdown class="test-case-status" @command="statusChange">
                <span class="el-dropdown-link">
                  <status-table-item :value="scope.row.status"/>
                </span>
                <el-dropdown-menu slot="dropdown" chang>
                  <el-dropdown-item :disabled="!hasEditPermission" :command="{id: scope.row.id, status: 'Pass'}">
                    {{ $t('test_track.plan_view.pass') }}
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!hasEditPermission"
                                    :command="{id: scope.row.id, status: 'Failure'}">
                    {{ $t('test_track.plan_view.failure') }}
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!hasEditPermission"
                                    :command="{id: scope.row.id, status: 'Blocking'}">
                    {{ $t('test_track.plan_view.blocking') }}
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!hasEditPermission" :command="{id: scope.row.id, status: 'Skip'}">
                    {{ $t('test_track.plan_view.skip') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>
          </template>
        </ms-table-column>

        <ms-table-column
          sortable
          prop="updateTime"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.update_time')"
          min-width="120px">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>

        <ms-table-column v-for="field in testCaseTemplate.customFields" :key="field.id"
                         :filters="field.name === '用例等级' ? priorityFilters : null"
                         :field="item"
                         column-key="priority"
                         :fields-width="fieldsWidth"
                         :label="field.name"
                         :min-width="90"
                         :prop="field.name">
          <template v-slot="scope">
              <span v-if="field.name === '用例等级'">
                  <priority-table-item :value="getCustomFieldValue(scope.row, field) ? getCustomFieldValue(scope.row, field) : scope.row.priority"/>
              </span>
            <span v-else>
                {{getCustomFieldValue(scope.row, field)}}
              </span>
          </template>
        </ms-table-column>

      </span>
    </ms-table>

    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

    <functional-test-case-edit
      ref="testPlanTestCaseEdit"
      :search-param.sync="condition"
      @refresh="initTableData"
      :is-read-only="isReadOnly"
      @refreshTable="search"/>

    <batch-edit ref="batchEdit" @batchEdit="batchEdit"
                :type-arr="typeArr" :value-arr="valueArr" :dialog-title="$t('test_track.case.batch_edit_case')"/>
  </div>
</template>

<script>
import ExecutorEdit from '../ExecutorEdit';
import StatusEdit from '../StatusEdit';
import FunctionalTestCaseEdit from "./FunctionalTestCaseEdit";
import MsTipButton from '../../../../../common/components/MsTipButton';
import MsTablePagination from '../../../../../common/pagination/TablePagination';
import MsTableHeader from '../../../../../common/components/MsTableHeader';
import MsTableButton from '../../../../../common/components/MsTableButton';
import NodeBreadcrumb from '../../../../common/NodeBreadcrumb';

import {
  TEST_PLAN_FUNCTION_TEST_CASE,
  TokenKey,
} from "@/common/js/constants";
import {getCurrentProjectID, hasPermission} from "@/common/js/utils";
import PriorityTableItem from "../../../../common/tableItems/planview/PriorityTableItem";
import StatusTableItem from "../../../../common/tableItems/planview/StatusTableItem";
import TypeTableItem from "../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../common/tableItems/planview/MethodTableItem";
import MsTableOperator from "../../../../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../../../../common/components/MsTableOperatorButton";
import {TEST_CASE_CONFIGS} from "../../../../../common/components/search/search-components";
import BatchEdit from "../../../../case/components/BatchEdit";
import ClassicEditor from "@ckeditor/ckeditor5-build-classic";
import {hub} from "@/business/components/track/plan/event-bus";
import MsTag from "@/business/components/common/components/MsTag";
import {
  buildBatchParam, checkTableRowIsSelected,
  getCustomFieldValue, getCustomTableWidth,
  getTableHeaderWithCustomFields,
  initCondition,
} from "@/common/js/tableUtils";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import {getProjectMember} from "@/network/user";
import {getTestTemplate} from "@/network/custom-field-template";

export default {
  name: "FunctionalTestCaseList",
  components: {
    MsTableColumn,
    MsTable,
    FunctionalTestCaseEdit,
    MsTableOperatorButton,
    MsTableOperator,
    MethodTableItem,
    TypeTableItem,
    StatusTableItem,
    PriorityTableItem, StatusEdit, ExecutorEdit, MsTipButton, MsTablePagination,
    MsTableHeader, NodeBreadcrumb, MsTableButton,
    BatchEdit, MsTag
  },
  data() {
    return {
      // updata: false,
      type: TEST_PLAN_FUNCTION_TEST_CASE,
      fields: [],
      fieldsWidth: getCustomTableWidth('TEST_PLAN_FUNCTION_TEST_CASE'),
      screenHeight: 'calc(100vh - 275px)',
      tableLabel: [],
      result: {},
      deletePath: "/test/case/delete",
      condition: {
        components: TEST_CASE_CONFIGS
      },
      showMyTestCase: false,
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      status: 'default',
      testPlan: {},
      isReadOnly: false,
      hasEditPermission: false,
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      methodFilters: [
        {text: this.$t('test_track.case.manual'), value: 'manual'},
        {text: this.$t('test_track.case.auto'), value: 'auto'}
      ],
      statusFilters: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan_view.pass'), value: 'Pass'},
        {text: this.$t('test_track.plan_view.failure'), value: 'Failure'},
        {text: this.$t('test_track.plan_view.blocking'), value: 'Blocking'},
        {text: this.$t('test_track.plan_view.skip'), value: 'Skip'},
        {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
      ],
      executorFilters: [],
      maintainerFilters: [],
      showMore: false,
      buttons: [
        {
          name: this.$t('test_track.case.batch_edit_case'), handleClick: this.handleBatchEdit,
          permissions: ['PROJECT_TRACK_PLAN:READ+CASE_BATCH_EDIT']
        },
        {
          name: this.$t('test_track.case.batch_unlink'), handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_TRACK_PLAN:READ+CASE_BATCH_DELETE']
        }
      ],
      operators: [
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit,
          permissions: ['PROJECT_TRACK_PLAN:READ+RUN']
        },
        {
          tip: this.$t('test_track.plan_view.cancel_relevance'), icon: "el-icon-unlock", type: "danger",
          exec: this.handleDelete,
          permissions: ['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']
        }
      ],
      typeArr: [
        {id: 'status', name: this.$t('test_track.plan_view.execute_result')},
        {id: 'executor', name: this.$t('test_track.plan_view.executor')},
      ],
      valueArr: {
        executor: [],
        status: [
          {name: this.$t('test_track.plan_view.pass'), id: 'Pass'},
          {name: this.$t('test_track.plan_view.failure'), id: 'Failure'},
          {name: this.$t('test_track.plan_view.blocking'), id: 'Blocking'},
          {name: this.$t('test_track.plan_view.skip'), id: 'Skip'}
        ]
      },
      editor: ClassicEditor,
      editorConfig: {
        // 'increaseIndent','decreaseIndent'
        toolbar: [],
      },
      selectDataRange: "all",
      testCaseTemplate: {},

    };
  },
  props: {
    planId: {
      type: String
    },
    clickType: String,
    selectNodeIds: {
      type: Array
    },
  },
  watch: {
    planId() {
      this.refreshTableAndPlan();
    },
    selectNodeIds() {
      this.condition.selectAll = false;
      this.search();
    },
    tableLabel: {
      handler(newVal) {
        this.updata = !this.updata;
      },
      deep: true
    }
  },
  mounted() {
    hub.$on("openFailureTestCase", row => {
      this.isReadOnly = true;
      this.condition.status = 'Failure';
      this.$refs.testPlanTestCaseEdit.openTestCaseEdit(row);
    });
    this.refreshTableAndPlan();
    this.hasEditPermission = hasPermission('PROJECT_TRACK_PLAN:READ+EDIT');
    this.getMaintainerOptions();
    this.getTemplateField();
  },
  beforeDestroy() {
    hub.$off("openFailureTestCase");
  },
  methods: {
    getTemplateField() {
      this.result.loading = true;
      let p1 = getProjectMember((data) => {
        this.members = data;
      });
      let p2 = getTestTemplate();
      Promise.all([p1, p2]).then((data) => {
        let template = data[1];
        this.result.loading = true;
        this.testCaseTemplate = template;
        this.fields = getTableHeaderWithCustomFields('TEST_PLAN_FUNCTION_TEST_CASE', this.testCaseTemplate.customFields);
        this.result.loading = false;
        this.$refs.table.reloadTable();
      });
    },
    getCustomFieldValue(row, field) {
      return getCustomFieldValue(row, field, this.members);
    },
    initTableData() {
      initCondition(this.condition, this.condition.selectAll);
      this.autoCheckStatus();
      if (this.planId) {
        // param.planId = this.planId;
        this.condition.planId = this.planId;
      }
      if (this.clickType) {
        if (this.status == 'default') {
          this.condition.status = this.clickType;
        } else {
          this.condition.status = null;
        }
        this.status = 'all';
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        // param.nodeIds = this.selectNodeIds;
        this.condition.nodeIds = this.selectNodeIds;
      }
      this.condition.projectId = getCurrentProjectID();
      if (this.planId) {
        this.result = this.$post(this.buildPagePath('/test/plan/case/list'), this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          for (let i = 0; i < this.tableData.length; i++) {
            if (this.tableData[i]) {
              if (this.tableData[i].customFields) {
                this.tableData[i].customFields = JSON.parse(this.tableData[i].customFields);
              }
              this.$set(this.tableData[i], "showTags", JSON.parse(this.tableData[i].tags));
              this.$set(this.tableData[i], "issuesSize", this.tableData[i].issuesCount);
              this.$set(this.tableData[i], "issuesContent", JSON.parse(this.tableData[i].issues));
            }
          }
          if (this.$refs.table) {
            this.$refs.table.clear();
          }
          checkTableRowIsSelected(this, this.$refs.table);
        });
      }
    },
    autoCheckStatus() {
      if (!this.planId) {
        return;
      }
      this.$post('/test/plan/autoCheck/' + this.planId, (response) => {
      });
    },
    showDetail(row, event, column) {
      this.isReadOnly = !this.hasEditPermission;
      this.$refs.testPlanTestCaseEdit.openTestCaseEdit(row);
    },
    refresh() {
      this.condition = {components: TEST_CASE_CONFIGS};
      this.$refs.table.clear();
      this.$emit('refresh');
    },
    breadcrumbRefresh() {
      this.showMyTestCase = false;
      this.refresh();
    },
    refreshTableAndPlan() {
      this.getTestPlanById();
      this.initTableData();
    },
    refreshTestPlanRecent() {
      let param = {};
      param.id = this.planId;
      param.updateTime = Date.now();
      this.$post('/test/plan/edit', param);
    },
    search() {
      this.initTableData();
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleEdit(testCase, index) {
      this.isReadOnly = false;
      this.$refs.testPlanTestCaseEdit.openTestCaseEdit(testCase);
    },
    handleDelete(testCase) {
      this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + ' ' + testCase.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(testCase);
          }
        }
      });
    },
    handleDeleteBatch() {
      if (this.tableData.length < 1) {
        this.$warning(this.$t('test_track.plan_view.no_case_relevance'));
        return;
      }
      this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let param = buildBatchParam(this, this.$refs.table.selectIds);
            this.$post('/test/plan/case/batch/delete', param, () => {
              this.$refs.table.clear();
              this.$emit("refresh");
              this.$success(this.$t('test_track.cancel_relevance_success'));
            });
          }
        }
      });
    },
    _handleDelete(testCase) {
      let testCaseId = testCase.id;
      this.result = this.$post('/test/plan/case/delete/' + testCaseId, {}, () => {
        this.$emit("refresh");
        this.$success(this.$t('test_track.cancel_relevance_success'));
      });
    },
    searchMyTestCase() {
      this.showMyTestCase = !this.showMyTestCase;
      if (this.showMyTestCase) {
        let user = JSON.parse(localStorage.getItem(TokenKey));
        this.condition.executor = user.id;
      } else {
        this.condition.executor = null;
      }
      this.initTableData();
    },
    statusChange(param) {
      this.$post('/test/plan/case/edit', param, () => {
        for (let i = 0; i < this.tableData.length; i++) {
          if (this.tableData[i].id == param.id) {
            this.tableData[i].status = param.status;
            break;
          }
        }
      });
    },
    getTestPlanById() {
      if (this.planId) {
        this.$post('/test/plan/get/' + this.planId, {}, response => {
          this.testPlan = response.data;
          this.refreshTestPlanRecent();
        });
      }
    },
    batchEdit(form) {
      let param = buildBatchParam(this, this.$refs.table.selectIds);
      param[form.type] = form.value;
      param.ids = this.$refs.table.selectIds;
      this.$post('/test/plan/case/batch/edit', param, () => {
        this.$refs.table.clear();
        this.status = '';
        this.$post('/test/plan/edit/status/' + this.planId);
        this.$success(this.$t('commons.save_success'));
        this.$emit('refresh');
      });
    },
    handleBatchEdit() {
      this.getMaintainerOptions();
      this.$refs.batchEdit.open();
    },
    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.valueArr.executor = response.data;
        this.executorFilters = response.data.map(u => {
          return {text: u.name, value: u.id};
        });
        this.maintainerFilters = response.data.map(u => {
          return {text: u.id + '(' + u.name + ')', value: u.id};
        });
      });
    },
  }
};
</script>

<style scoped>

.search {
  margin-left: 10px;
  width: 240px;
}

.test-case-status, .el-table {
  cursor: pointer;
}

.el-tag {
  margin-left: 10px;
}

/*.ms-table-header >>> .table-title {*/
/*  height: 0px;*/
/*}*/

/*/deep/ .el-table__fixed-body-wrapper {*/
/*  top: 59px !important;*/
/*}*/

.ms-table-header {
  margin-bottom: 10px;
}
</style>
