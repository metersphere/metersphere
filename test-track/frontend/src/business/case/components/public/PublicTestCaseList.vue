<template>

  <span>
    <ms-search
      :condition.sync="condition"
      @search="search">
    </ms-search>

    <ms-table
      v-loading="loading"
      operator-width="170px"
      row-key="id"
      :data="page.data"
      :condition="condition"
      :total="page.total"
      :page-size.sync="page.pageSize"
      :operators="operators"
      :screen-height="screenHeight"
      :batch-operators="batchButtons"
      :remember-order="true"
      :row-order-group-id="projectId"
      :row-order-func="editTestCaseOrder"
      :fields.sync="fields"
      :field-key="tableHeaderKey"
      @handlePageChange="initTableData"
      @order="initTableData"
      @filter="search"
      ref="table">

      <span v-for="(item, index) in fields" :key="index">

       <ms-table-column
         prop="projectName"
         :fields-width="fieldsWidth"
         :label="$t('test_track.case.project')"
         v-if="item.id === 'projectName'"
         min-width="150px">
       </ms-table-column>

        <ms-table-column
          v-if="!customNum"
          :field="item"
          :fields-width="fieldsWidth"
          prop="num"
          sortable
          :label="$t('commons.id')"
          min-width="80">
          <template v-slot:default="scope">
            <el-tooltip :content="$t('commons.edit')">
              <a style="cursor:pointer" @click="handleEdit(scope.row)"> {{ scope.row.num }} </a>
            </el-tooltip>
          </template>
        </ms-table-column>

        <ms-table-column
          v-if="item.id === 'num' && customNum"
          :field="item"
          :fields-width="fieldsWidth"
          prop="customNum"
          sortable
          :label="$t('commons.id')"
          min-width="80">
          <template v-slot:default="scope">
            <el-tooltip :content="$t('commons.edit')">
              <a style="cursor:pointer" @click="handleEdit(scope.row)"> {{ scope.row.num }} </a>
            </el-tooltip>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="name"
          sortable
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.name')"
          min-width="120"
        />

        <ms-table-column :label="$t('test_track.case.case_desc')" prop="desc" :field="item" min-width="100px">
          <template v-slot:default="scope">
            <el-link @click.stop="getCase(scope.row.id)" style="color:#783887;">{{ $t('commons.preview') }}</el-link>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="createUser"
          min-width="120"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.create_user')"
          :filters="userFilter">
           <template v-slot:default="scope">
            {{ scope.row.createName }}
          </template>
        </ms-table-column>

        <ms-table-column
          prop="status"
          min-width="120"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.case.case_status')">
           <template v-slot:default="scope">
            {{ statusMap[scope.row.status] ? statusMap[scope.row.status] : $t(scope.row.status) }}
          </template>
        </ms-table-column>

        <ms-table-column
          sortable
          prop="priority"
          min-width="120"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.case.priority')"/>

        <test-case-review-status-table-item
          :field="item"
          :fields-width="fieldsWidth"/>

        <test-plan-case-status-table-item
          prop="lastExecuteResult"
          :field="item"
          :fields-width="fieldsWidth"/>

        <ms-table-column
          prop="tags"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.tag')"
          min-width="80">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :show-tooltip="scope.row.tags.length===1&&itemName.length*12<=80"
                    :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
            <span/>
          </template>
        </ms-table-column>

        <ms-table-column
          v-if="versionEnable"
          :label="$t('project.version.name')"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="100px"
          prop="versionId">
           <template v-slot:default="scope">
            <span>{{ scope.row.versionName }}</span>
          </template>
        </ms-table-column>

        <ms-update-time-column :field="item"
                               :fields-width="fieldsWidth"/>

        <ms-create-time-column :field="item"
                               :fields-width="fieldsWidth"/>

      </span>

    </ms-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize"
                         :total="page.total"/>

    <test-case-preview ref="testCasePreview" :loading="rowCaseResult.loading"/>

    <list-item-delete-confirm
      @handleDelete="_handleDeleteVersion"
      ref="apiDeleteConfirm"/>

    <batch-move :public-enable="true"
                @refresh="refresh"
                @copyPublic="copyPublic"
                ref="testBatchMove"/>

  </span>

</template>

<script>

import MsTablePagination from 'metersphere-frontend/src/components/pagination/TablePagination';
import {TEST_CASE_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import {TEST_CASE_LIST} from "metersphere-frontend/src/utils/constants";
import StatusTableItem from "@/business/common/tableItems/planview/StatusTableItem";
import ReviewStatus from "@/business/case/components/ReviewStatus";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {
  buildBatchParam,
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField,
  getPageInfo,
  initCondition,
} from "metersphere-frontend/src/utils/tableUtils";
import PlanStatusTableItem from "@/business/common/tableItems/plan/PlanStatusTableItem";
import {getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {getUUID, parseTag} from "metersphere-frontend/src/utils"
import {hasLicense} from "metersphere-frontend/src/utils/permission"
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsUpdateTimeColumn from "metersphere-frontend/src/components/table/MsUpdateTimeColumn";
import MsCreateTimeColumn from "metersphere-frontend/src/components/table/MsCreateTimeColumn";
import TestPlanCaseStatusTableItem from "@/business/common/tableItems/TestPlanCaseStatusTableItem";
import TestCaseReviewStatusTableItem from "@/business/common/tableItems/TestCaseReviewStatusTableItem";
import MsSearch from "metersphere-frontend/src/components/search/MsSearch";

import BatchMove from "@/business/case/components/BatchMove";
import TestCasePreview from "@/business/case/components/TestCasePreview";
import {
  deletePublicTestCaseVersion,
  editTestCaseOrder,
  getTestCase,
  getTestCaseStep, getTestCaseVersions,
  testCasePublicBatchCopy,
  testCasePublicBatchDeleteToGc,
  testCasePublicList,
} from "@/api/testCase";

import ListItemDeleteConfirm from "metersphere-frontend/src/components/ListItemDeleteConfirm";
import {TEST_CASE_STATUS_MAP} from "@/business/constants/table-constants";
import {mapState} from "pinia";
import {useStore} from "@/store";
import {getProjectMemberUserFilter} from "@/api/user";
import {operationConfirm} from "@/business/utils/sdk-utils";
import TypeTableItem from "@/business/common/tableItems/planview/TypeTableItem";


export default {
  name: "PublicTestCaseList",
  components: {
    TypeTableItem,
    BatchMove,
    ListItemDeleteConfirm,
    MsUpdateTimeColumn,
    MsCreateTimeColumn,
    MsTag,
    TestCasePreview,
    MsTableColumn,
    MsTable,
    PlanStatusTableItem,
    MsTablePagination,
    StatusTableItem,
    ReviewStatus,
    TestCaseReviewStatusTableItem,
    TestPlanCaseStatusTableItem,
    MsSearch
  },
  data() {
    return {
      addPublic: false,
      type: TEST_CASE_LIST,
      tableHeaderKey: "TRACK_PUBLIC_TEST_CASE",
      screenHeight: 'calc(100vh - 185px)',
      loading: false,
      condition: {
        components: TEST_CASE_CONFIGS,
        filters: {},
        custom: false,
      },
      batchButtons: [
        {
          name: this.$t('test_track.case.batch_copy'),
          handleClick: this.handleBatchCopy,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_COPY']
        },
        {
          name: this.$t('test_track.case.batch_delete_case'),
          handleClick: this.handleDeleteBatchToPublic,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_DELETE'],
        },
      ],
      operators: [
        {
          tip: this.$t('commons.view'), icon: "el-icon-view",
          exec: this.handleEditShow,
          permissions: ['PROJECT_TRACK_CASE:READ'],
        },
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit,
          permissions: ['PROJECT_TRACK_CASE:READ+EDIT'],
          isDisable: !this.isOwner
        },
        {
          tip: this.$t('commons.copy'), icon: "el-icon-copy-document", type: "success",
          exec: this.handleCopyPublic,
          permissions: ['PROJECT_TRACK_CASE:READ+COPY']
        },
        {
          tip: this.$t('commons.remove'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDeleteToGc,
          permissions: ['PROJECT_TRACK_CASE:READ+DELETE'],
          isDisable: !this.isOwner
        }
      ],
      page: getPageInfo(),
      fields: getCustomTableHeader('TRACK_PUBLIC_TEST_CASE'),
      fieldsWidth: getCustomTableWidth('TRACK_PUBLIC_TEST_CASE'),
      rowCase: {},
      rowCaseResult: {},
      userFilter: []
    };
  },
  props: {
    treeNodes: {
      type: Array
    },
    publicEnable: {
      type: Boolean,
      default: false,
    },
    currentVersion: String,
    versionEnable: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    ...mapState(useStore, {
      selectNodeIds: 'testCaseSelectNodeIds',
      selectNode: 'testCaseSelectNode',
      moduleOptions: 'testCaseModuleOptions',
      customNum: 'currentProjectIsCustomNum'
    }),
    statusMap() {
      return TEST_CASE_STATUS_MAP;
    },
    editTestCaseOrder() {
      return editTestCaseOrder;
    },
    currentUser() {
      return getCurrentUserId();
    }
  },
  created: function () {
    this.$emit('setCondition', this.condition);
    this.initTableData();
    getProjectMemberUserFilter((data) => {
      this.userFilter = data;
    });

    // 切换tab之后版本查询
    this.condition.versionId = this.currentVersion;
  },
  watch: {
    selectNodeIds() {
      this.page.currentPage = 1;
      initCondition(this.condition, false);
      this.initTableData();
    },
    condition() {
      this.$emit('setCondition', this.condition);
    },
    currentVersion() {
      this.condition.versionId = this.currentVersion;
      this.initTableData();
    },
  },
  methods: {
    initTableData(nodeIds) {
      this.condition.nodeIds = [];
      initCondition(this.condition, this.condition.selectAll);
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);
      this.condition.versionId = this.currentVersion || null;
      this.condition.filters.reviewStatus = ["Prepare", "Pass", "UnPass"];
      if (nodeIds && Array.isArray(nodeIds) && nodeIds.length > 0) {
        this.condition.nodeIds = nodeIds;
        this.condition.workspaceId = getCurrentWorkspaceId();
      }
      this.getData();
    },
    getData() {
      if (this.projectId) {
        this.condition.projectId = this.projectId;
        this.$emit('setCondition', this.condition);
        this.condition.casePublic = true;
        this.condition.workspaceId = getCurrentWorkspaceId();
        this.loading = true;
        testCasePublicList({pageNum: this.page.currentPage, pageSize: this.page.pageSize}, this.condition)
          .then(response => {
            this.loading = false;
            let data = response.data;
            this.page.total = data.itemCount;
            this.page.data = data.listObject;
            parseTag(this.page.data);
          });
      }
    },
    search() {
      // 添加搜索条件时，当前页设置成第一页
      this.page.currentPage = 1;
      this.initTableData();
      this.$emit('search');
    },
    handleEdit(testCase) {
      let TestCaseData = this.$router.resolve({
        path: '/track/case/all',
        query: {
          redirectID: getUUID(),
          dataType: "testCase",
          dataSelectRange: testCase.id,
          projectId: testCase.projectId
        }
      });
      window.open(TestCaseData.href, '_blank');
    },
    handleEditShow(testCase, column) {
      if (column.label !== this.$t('test_track.case.case_desc')) {
        getTestCase(testCase.id)
          .then(response => {
            let testCase = response.data;
            this.$emit('testCaseEditShow', testCase);
          });
      }
    },
    handleDeleteToGc(testCase) {
      getTestCaseVersions(testCase.id)
        .then(response => {
          if (hasLicense() && this.versionEnable && response.data.length > 1) {
            // 删除提供列表删除和全部版本删除
            this.$refs.apiDeleteConfirm.open(testCase, this.$t('test_track.case.delete_confirm'));
          } else {
            operationConfirm(this.$t('test_track.case.delete_confirm') + '\'' + testCase.name + '\'', () => {
              this._handleDeleteVersion(testCase, false);
            });
          }
        });
    },
    _handleDeleteVersion(testCase, deleteCurrentVersion) {
      if (deleteCurrentVersion) {
        // 删除指定版本
        deletePublicTestCaseVersion(testCase.versionId, testCase.refId)
          .then(() => {
            this.$success(this.$t('commons.delete_success'));
            this.$refs.apiDeleteConfirm.close();
            this.$emit("refreshPublic");
          })
      } else {
        // 删除全部版本
        let param = buildBatchParam(this, this.$refs.table.selectIds);
        param.ids.push(testCase.id);
        testCasePublicBatchDeleteToGc(param)
          .then(() => {
            this.$success(this.$t('commons.delete_success'));
            this.$refs.apiDeleteConfirm.close();
            this.$emit("refreshPublic");
          })
      }
    },
    isOwner(testCase) {
      return testCase.maintainer === this.currentUser || testCase.createUser === this.currentUser;
    },
    getCase(id) {
      this.$refs.testCasePreview.open();
      this.rowCaseResult.loading = true;
      getTestCaseStep(id)
        .then(response => {
          this.rowCase = response.data;
          this.rowCase.steps = JSON.parse(this.rowCase.steps);
          if (!this.rowCase.steps || this.rowCase.length < 1) {
            this.rowCase.steps = [{
              num: 1,
              desc: '',
              result: ''
            }];
          }
          if (!this.rowCase.stepModel) {
            this.rowCase.stepModel = "STEP";
          }
          this.$refs.testCasePreview.setData(this.rowCase);
        });
    },
    handleCopyPublic(testCase) {
      this.$refs.table.selectIds.push(testCase.id);
      this.$refs.testBatchMove.open(this.treeNodes, this.$refs.table.selectIds, this.moduleOptions);
    },
    refresh() {
      this.$refs.table.clear();
      this.$emit('refreshAll');
    },
    refreshAll() {
      this.$refs.table.clear();
      this.$emit('refreshAll');
    },
    handleDeleteBatchToPublic() {
      operationConfirm(this.$t('test_track.case.delete_confirm'), () => {
        let param = buildBatchParam(this, this.$refs.table.selectIds);
        testCasePublicBatchDeleteToGc(param)
          .then(() => {
            this.$refs.table.clear();
            this.$emit("refreshPublic");
            this.$success(this.$t('commons.delete_success'));
          });
      });
    },
    handleBatchCopy() {
      this.$refs.testBatchMove.open(this.treeNodes, this.$refs.table.selectIds, this.moduleOptions);
    },
    copyPublic(param) {
      param.condition = this.condition;
      param.projectId = this.projectId;
      param.condition.projectId = null;
      param.condition.ids = null;
      this.loading = true;
      testCasePublicBatchCopy(param)
        .then(() => {
          this.loading = false;
          this.$success(this.$t('commons.save_success'));
          this.$refs.testBatchMove.close();
          this.refresh();
        });
    },
  }
};
</script>

<style scoped>
.el-table {
  cursor: pointer;
}

.el-tag {
  margin-left: 10px;
}

:deep(.el-table) {
  overflow: auto;
}
</style>
