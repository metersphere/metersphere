<template>

  <div class="case-main-layout">
    <div class="case-main-layout-left" style="float: left; display: inline-block">
      <ms-table-count-bar :count-content="$t('case.all_case_content') + ' (' + page.total + ')'"></ms-table-count-bar>
    </div>

    <div class="case-main-layout-right" style="float: right; display: flex">
      <!-- 简单搜索框 -->
      <ms-new-ui-search :condition.sync="condition" @search="search" style="float: left" />

      <!-- 高级搜索框  -->
      <ms-table-adv-search :condition.sync="condition" @search="search" ref="advanceSearch"/>

      <!-- 表头自定义显示Popover  -->
      <ms-table-header-custom-popover :fields.sync="fields" :field-key="tableHeaderKey" @reload="reloadTable" />
    </div>

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
      :max-height="maxHeight"
      :batch-operators="batchButtons"
      :remember-order="true"
      :disable-header-config="true"
      :row-order-group-id="projectId"
      :row-order-func="editTestCaseOrder"
      :fields.sync="fields"
      :field-key="tableHeaderKey"
      :enable-header-drag="true"
      @handlePageChange="initTableData"
      @order="initTableData"
      @filter="search"
      @callBackSelect="callBackSelect"
      @callBackSelectAll="callBackSelectAll"
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
          :column-key="'num'"
          :prop="'num'"
          sortable="custom"
          :label="$t('commons.id')"
          min-width="80">
          <template v-slot:default="scope">
            <a style="cursor:pointer"> {{ scope.row.num }} </a>
          </template>
        </ms-table-column>

        <ms-table-column
          v-if="item.id === 'num' && customNum"
          :fields-width="fieldsWidth"
          :column-key="'customNum'"
          prop="customNum"
          sortable="custom"
          :label="$t('commons.id')"
          min-width="80">
          <template v-slot:default="scope">
            <a style="cursor:pointer"> {{ scope.row.customNum }} </a>
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
             {{ getCreateUserName(scope.row.createUser) }}
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
          :show-overflow-tooltip="false"
          min-width="180">
          <template v-slot:default="scope">
            <el-tooltip class="item" effect="dark" placement="top">
                <div v-html="getTagToolTips(scope.row.tags)" slot="content"></div>
                <div class="oneLine">
                  <ms-single-tag
                    v-for="(itemName, index) in parseColumnTag(scope.row.tags)"
                    :key="index"
                    type="success"
                    effect="plain"
                    :show-tooltip="scope.row.tags.length === 1 && itemName.length * 12 <= 100"
                    :content="itemName"
                    style="margin-left: 0px; margin-right: 2px"/>
                </div>
              </el-tooltip>
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

    <ms-table-batch-operator-group v-if="selectCounts > 0" :batch-operators="batchButtons" :select-counts="selectCounts" @clear="clearTableSelect"/>

    <home-pagination v-if="page.data.length > 0 && selectCounts === 0" :change="initTableData" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize"
                     :total="page.total" layout="total, prev, pager, next, sizes, jumper" style="margin-top: 19px"/>

    <test-case-preview ref="testCasePreview" :loading="rowCaseResult.loading"/>

    <public-test-case-show ref="publicTestCaseShow" :condition.sync="condition" :page-size.sync="page.pageSize" :page-total.sync="page.total"/>

    <list-item-delete-confirm
      @handleDelete="_handleDeleteVersion"
      ref="apiDeleteConfirm"/>

    <batch-move :public-enable="true"
                @refresh="refresh"
                @copyPublic="copyPublic"
                ref="testBatchMove"/>

  </div>

</template>

<script>
import MsSingleTag from "metersphere-frontend/src/components/new-ui/MsSingleTag";
import HomePagination from '@/business/home/components/pagination/HomePagination';
import MsTableAdvSearch from "metersphere-frontend/src/components/new-ui/MsTableAdvSearch";
import MsTableHeaderCustomPopover from 'metersphere-frontend/src/components/new-ui/MsTableHeaderCustomPopover'
import MsNewUiSearch from "metersphere-frontend/src/components/new-ui/MsSearch";
import MsTableCountBar from 'metersphere-frontend/src/components/table/MsTableCountBar';
import MsTableBatchOperatorGroup from "metersphere-frontend/src/components/new-ui/MsTableBatchOperatorGroup";
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
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsUpdateTimeColumn from "metersphere-frontend/src/components/table/MsUpdateTimeColumn";
import MsCreateTimeColumn from "metersphere-frontend/src/components/table/MsCreateTimeColumn";
import TestPlanCaseStatusTableItem from "@/business/common/tableItems/TestPlanCaseStatusTableItem";
import TestCaseReviewStatusTableItem from "@/business/common/tableItems/TestCaseReviewStatusTableItem";
import BatchMove from "@/business/case/components/BatchMove";
import TestCasePreview from "@/business/case/components/TestCasePreview";
import {
  deletePublicTestCaseVersion, editTestCaseOrder, getEditSimpleTestCase,
  getTestCaseStep, getTestCaseVersions, testCasePublicBatchCopy,
  testCasePublicBatchDeleteToGc, testCasePublicList,
} from "@/api/testCase";
import ListItemDeleteConfirm from "metersphere-frontend/src/components/ListItemDeleteConfirm";
import {TEST_CASE_STATUS_MAP} from "@/business/constants/table-constants";
import {mapState} from "pinia";
import {useStore} from "@/store";
import {getProjectMemberUserFilter} from "@/api/user";
import TypeTableItem from "@/business/common/tableItems/planview/TypeTableItem";
import {getVersionFilters} from "@/business/utils/sdk-utils";
import {getTagToolTips, openCaseEdit, parseColumnTag} from "@/business/case/test-case";
import PublicTestCaseShow from "@/business/case/components/public/PublicTestCaseShow"


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
    MsTableBatchOperatorGroup,
    MsTableCountBar,
    MsNewUiSearch,
    MsTableAdvSearch,
    MsTableHeaderCustomPopover,
    HomePagination,
    PublicTestCaseShow,
    MsSingleTag
  },
  data() {
    return {
      addPublic: false,
      type: TEST_CASE_LIST,
      tableHeaderKey: "TRACK_PUBLIC_TEST_CASE",
      screenHeight: 'calc(100vh - 185px)',
      maxHeight: 'calc(100vh - 287px)',
      loading: false,
      condition: {
        components: TEST_CASE_CONFIGS,
        filters: {},
        custom: false,
      },
      batchButtons: [
        {
          name: this.$t('test_track.case.batch_copy_btn'),
          handleClick: this.handleBatchCopy,
          permissions: ['PROJECT_TRACK_CASE:READ+BATCH_COPY']
        },
        {
          name: this.$t('test_track.case.public_remove'),
          handleClick: this.handleDeleteBatchToPublic,
          isDelete: true
        }
      ],
      operators: [
        {
          tip: this.$t('commons.view'), icon: "el-icon-view",
          exec: this.handleEditShow,
          isTextButton: true,
          permissions: ['PROJECT_TRACK_CASE:READ'],
        },
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit,
          isTextButton: true,
          permissions: ['PROJECT_TRACK_CASE:READ+EDIT'],
          isDisable: !this.isOwner
        },
        {
          isMoreOperate: true,
          childOperate: [
            {
              tip: this.$t('commons.copy'), icon: "el-icon-copy-document",
              exec: this.handleCopyPublic,
              permissions: ['PROJECT_TRACK_CASE:READ+COPY']
            },
            {
              tip: this.$t('test_track.case.public_remove'), icon: "el-icon-delete",
              exec: this.handleDeleteToGc,
              permissions: ['PROJECT_TRACK_CASE:READ+DELETE'],
              isDisable: !this.isOwner,
              isDivide: true,
              isActive: true
            }
          ]
        }
      ],
      page: getPageInfo(),
      fields: getCustomTableHeader('TRACK_PUBLIC_TEST_CASE'),
      fieldsWidth: getCustomTableWidth('TRACK_PUBLIC_TEST_CASE'),
      rowCase: {},
      rowCaseResult: {},
      userFilter: [],
      versionFilters: [],
      selectCounts: 0
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
      selectNodeIds: 'testCasePublicSelectNodeIds',
      selectNode: 'testCasePublicSelectNode',
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
    this.getVersionOptions();

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
    reloadTable() {
      this.$refs.table.resetHeader();
    },
    initTableData(nodeIds) {
      this.condition.nodeIds = [];
      initCondition(this.condition, this.condition.selectAll);
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        if (!this.selectNode || this.selectNode.data.id !== 'root') {
          // 优化：如果当前选中节点是root节点，则不添加过滤条件
          this.condition.nodeIds = this.selectNodeIds;
        }
      }
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
        // 公共用例暂不支持所属项目过滤
        // this.condition.projectId = this.projectId;
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
    clearTableSelect() {
      this.$refs.table.clear();
      this.selectCounts = 0;
    },
    callBackSelect(selection) {
      this.selectCounts = this.$refs.table.selectDataCounts;
    },
    callBackSelectAll(selection) {
      this.selectCounts = this.$refs.table.selectDataCounts;
    },
    handleEdit(testCase) {
      // 这个接口会校验权限
      getEditSimpleTestCase(testCase.id)
        .then(() => {
          openCaseEdit({caseId: testCase.id, projectId: testCase.projectId}, this);
        })
        .catch(() => {});
    },
    handleEditShow(testCase, column) {
      this.$refs.publicTestCaseShow.open(testCase.id);
      this.$refs.publicTestCaseShow.tableData = this.page.data;
      this.$refs.publicTestCaseShow.currentPage = this.page.currentPage;
    },
    handleDeleteToGc(testCase) {
      getTestCaseVersions(testCase.id)
        .then(response => {
          if (hasLicense() && this.versionEnable && response.data.length > 1) {
            // 删除提供列表删除和全部版本删除
            this.$refs.apiDeleteConfirm.open(testCase, this.$t('test_track.case.delete_confirm'));
          } else {
            let title = this.$t('case.public.remove') + ": " + testCase.name + "?";
            this.$confirm(this.$t('test_track.case.public_batch_delete_tip'), title, {
                cancelButtonText: this.$t("commons.cancel"),
                confirmButtonText: this.$t("test_track.case.public_remove"),
                customClass: 'custom-confirm-delete',
                callback: action => {
                  if (action === "confirm") {
                    this._handleDeleteVersion(testCase, false);
                  }
                }
              }
            );
          }
        });
    },
    _handleDeleteVersion(testCase, deleteCurrentVersion) {
      if (deleteCurrentVersion) {
        // 删除指定版本
        deletePublicTestCaseVersion(testCase.versionId, testCase.refId)
          .then(() => {
            this.$success(this.$t('commons.delete_success'), false);
            this.$refs.apiDeleteConfirm.close();
            this.$emit("refreshPublic");
          })
      } else {
        // 删除全部版本
        let param = buildBatchParam(this, this.$refs.table.selectIds);
        param.ids.push(testCase.id);
        testCasePublicBatchDeleteToGc(param)
          .then(() => {
            this.$success(this.$t('commons.delete_success'), false);
            this.$refs.apiDeleteConfirm.close();
            this.$emit("refreshPublic");
          })
      }
    },
    getTagToolTips(tags) {
      return getTagToolTips(tags);
    },
    parseColumnTag(tags) {
      return parseColumnTag(tags);
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
      this.$refs.testBatchMove.open(false, testCase.name, this.treeNodes, 1, this.$refs.table.selectIds, this.moduleOptions);
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
      let title = this.$t('case.public.batch_remove_confirm', [this.$refs.table.selectIds.length]);
      this.$confirm(this.$t('test_track.case.public_batch_delete_tip'), title, {
          cancelButtonText: this.$t("commons.cancel"),
          confirmButtonText: this.$t("test_track.case.public_remove"),
          customClass: 'custom-confirm-delete',
          callback: action => {
            if (action === "confirm") {
              let param = buildBatchParam(this, this.$refs.table.selectIds);
              testCasePublicBatchDeleteToGc(param)
                .then(() => {
                  this.clearTableSelect();
                  this.$emit("refreshPublic");
                  this.$success(this.$t('commons.delete_success'), false);
                });
            }
          }
        }
      );
    },
    handleBatchCopy() {
      let firstSelectRow = this.$refs.table.selectRows.values().next().value;
      this.$refs.testBatchMove.open(false, firstSelectRow.name, this.treeNodes, this.selectCounts, this.$refs.table.selectIds, this.moduleOptions);
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
          this.$refs.testBatchMove.btnDisable = false;
          this.$success(this.$t('commons.save_success'), false);
          this.$refs.testBatchMove.close();
          this.refresh();
        });
    },
    getVersionOptions() {
      if (hasLicense()) {
        // 暂时去掉列表版本筛选, 保留但是查询逻辑待后续需求来更正
        getVersionFilters(getCurrentWorkspaceId())
          .then(r =>  this.versionFilters = r.data);
      }
    },
    getCreateUserName(userId) {
      let user = this.userFilter.filter(item => item.value === userId);
      return user.length > 0 ? user[0].text : "";
    },
  }
};
</script>

<style scoped>
.el-tag {
  margin-left: 10px;
}

:deep(button.el-button.el-button--default.el-button--mini) {
  box-sizing: border-box;
  width: 32px;
  height: 32px;
  background: #FFFFFF;
  border: 1px solid #BBBFC4;
  border-radius: 4px;
  flex: none;
  order: 5;
  align-self: center;
  flex-grow: 0;
  margin-left: 12px;
}

:deep(button.el-button.el-button--default.el-button--mini:hover) {
  color: #783887;
  border: 1px solid #783887;
}

:deep(button.el-button.el-button--default.el-button--mini:focus) {
  color: #783887;
  border: 1px solid #783887;
}
</style>
