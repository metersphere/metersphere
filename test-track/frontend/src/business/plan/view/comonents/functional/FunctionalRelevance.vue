<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :enable-full-screen="false"
    @close="close"
    :flag="isTestPlan"
    :multiple-project="multipleProject"
    :is-saving="isSaving"
    ref="baseRelevance">

    <template v-slot:aside>
      <node-tree class="node-tree"
                 :scroll="false"
                 v-loading="nodeResult.loading"
                 local-suffix="test_case"
                 default-label="未规划用例"
                 @nodeSelectEvent="nodeChange"
                 :tree-nodes="treeNodes"
                 ref="nodeTree"/>
    </template>

    <ms-table-header :condition.sync="page.condition" @search="search" title="" :show-create="false">
      <template v-slot:searchBarBefore>
        <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion" margin-right="20"/>
      </template>
    </ms-table-header>

    <ms-table
      v-loading="page.loading"
      :data="page.data"
      :condition="page.condition"
      :total="page.total"
      :page-size.sync="page.pageSize"
      :screen-height="screenHeight"
      row-key="id"
      :reserve-option="true"
      :page-refresh="pageRefresh"
      @handlePageChange="getTestCases"
      @selectCountChange="setSelectCounts"
      @order="getTestCases"
      @filter="search"
      ref="table">

      <ms-table-column
        v-if="!customNum"
        prop="num"
        sortable
        :label="$t('commons.id')">
      </ms-table-column>
      <ms-table-column
        v-if="customNum"
        prop="customNum"
        sortable
        :label="$t('commons.id')">
      </ms-table-column>

      <ms-table-column prop="name" :label="$t('commons.name')"/>

      <ms-table-column
        v-if="versionEnable && versionFilters"
        prop="versionId"
        :filters="versionFilters"
        :label="$t('commons.version')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.versionName }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="priority"
        :filters="priorityOptions"
        sortable
        :label="$t('test_track.case.priority')"
        width="120px">
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.priority" :priority-options="priorityOptions"/>
        </template>
      </ms-table-column>

      <test-plan-case-status-table-item
        sortable
        prop="lastExecuteResult"/>

      <test-case-review-status-table-item sortable/>

      <ms-table-column prop="tags" :label="$t('commons.tag')" width="90px" :show-overflow-tooltip="false">
        <template v-slot:default="scope">
          <el-tooltip class="item" effect="dark" placement="top">
            <div v-html="getTagToolTips(scope.row.tags)" slot="content"></div>
            <div class="oneLine">
              <ms-tag
                v-for="(itemName, index) in scope.row.tags"
                :key="index"
                type="success"
                effect="plain"
                :show-tooltip="scope.row.tags.length === 1 && itemName.length * 12 <= 100"
                :content="itemName"
                style="margin-left: 0px; margin-right: 2px" />
            </div>
          </el-tooltip>
        </template>
      </ms-table-column>

      <ms-update-time-column/>
      <ms-create-time-column/>

    </ms-table>

    <ms-table-pagination :change="pageChange" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize"
                         :total="page.total"/>
  </test-case-relevance-base>

</template>

<script>

import NodeTree from 'metersphere-frontend/src/components/module/MsNodeTree';
import PriorityTableItem from "../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../common/tableItems/planview/TypeTableItem";
import MsTableSearchBar from "metersphere-frontend/src/components/MsTableSearchBar";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import TestCaseRelevanceBase from "../base/TestCaseRelevanceBase";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTag from "metersphere-frontend/src/components/MsTag";
import MsCreateTimeColumn from "metersphere-frontend/src/components/table/MsCreateTimeColumn";
import MsUpdateTimeColumn from "metersphere-frontend/src/components/table/MsUpdateTimeColumn";
import StatusTableItem from "@/business/common/tableItems/planview/StatusTableItem";
import ReviewStatus from "@/business/case/components/ReviewStatus";
import TestCaseReviewStatusTableItem from "@/business/common/tableItems/TestCaseReviewStatusTableItem";
import TestPlanCaseStatusTableItem from "@/business/common/tableItems/TestPlanCaseStatusTableItem";
import {TEST_CASE_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import {getProjectApplicationConfig} from "@/api/project-application";
import {getVersionFilters} from "@/business/utils/sdk-utils";
import {getTestTemplate} from "@/api/custom-field-template";
import {initTestCaseConditionComponents} from "@/business/case/test-case";

export default {
  name: "FunctionalRelevance",
  components: {
    TestPlanCaseStatusTableItem,
    TestCaseReviewStatusTableItem,
    ReviewStatus,
    StatusTableItem,
    MsUpdateTimeColumn,
    MsCreateTimeColumn,
    MsTag,
    MsTablePagination,
    MsTable,
    MsTableColumn,
    TestCaseRelevanceBase,
    NodeTree,
    PriorityTableItem,
    TypeTableItem,
    MsTableSearchBar,
    MsTableAdvSearchBar,
    MsTableHeader,
    'VersionSelect': MxVersionSelect,
  },
  mounted() {
    this.getVersionOptions();
  },
  data() {
    return {
      nodeResult: {},
      isSaving: false,
      loading: false,
      treeNodes: [],
      selectNodeIds: [],
      selectNodeNames: [],
      projectId: '',
      projectName: '',
      projects: [],
      customNum: false,
      screenHeight: '400',
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      versionFilters: null,
      testCaseTemplate: {},
      pageRefresh: false,
      priorityOptions: []
    };
  },
  props: {
    page: {
      type: Object
    },
    isTestPlan: {
      type: Boolean
    },
    getTableData: {
      type: Function
    },
    getNodeTree: {
      type: Function
    },
    save: {
      type: Function
    },
    multipleProject: {
      type: Boolean,
      default: true
    },
    versionEnable: {
      type: Boolean,
      default: false
    },
  },
  watch: {
    projectId(val) {
      if (!this.projectId) {
        return;
      }
      this.setConditionModuleIdParam();
      this.page.condition.projectId = this.projectId;
      this.page.condition.versionId = null;
      this.getProjectNodeForce();
      this.getTestCases();
      this.getCustomNum();
      this.getVersionOptions();
    }
  },
  methods: {
    open() {
      this.page.condition = {custom: false, components: TEST_CASE_CONFIGS};
      this.isSaving = false;
      this.$refs.baseRelevance.open();
      if (this.$refs.table) {
        this.$refs.table.clear();
        this.$refs.table.clearSort();
      }
      if (this.projectId) {
        this.getProjectNode(this.projectId);
        this.getTestCases();
      }
    },
    setProject(projectId) {
      this.projectId = projectId;
      this.pushCustomFieldToCondition(this.projectId);
    },
    setConditionModuleIdParam() {
      this.page.condition.components.forEach(component => {
        if (component.key === 'moduleIds') {
          component.options.params = {"projectId": this.projectId};
        }
      });
    },
    getCustomNum() {
      getProjectApplicationConfig('CASE_CUSTOM_NUM')
        .then(result => {
          let data = result.data;
          if (data && data.typeValue === 'true') {
            this.customNum = true;
          } else {
            this.customNum = false;
          }
        });
    },
    search() {
      // 添加搜索条件时，当前页设置成第一页
      this.page.currentPage = 1;
      this.getTestCases();
      this.getProjectNode(this.projectId, this.page.condition);
    },
    getTestCases(data) {
      this.pageRefresh = data === "page";
      let condition = this.page.condition;
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        condition.nodeIds = this.selectNodeIds;
      } else {
        condition.nodeIds = [];
      }
      condition.projectId = this.projectId;
      if (this.projectId) {
        this.getTableData();
      }
    },
    pageChange() {
      this.getTestCases("page")
    },
    saveCaseRelevance(item) {
      this.isSaving = true;
      let param = {};
      param.ids = this.$refs.table.selectIds;
      param.request = this.page.condition;
      param.checked = item;
      this.save(param, this);
    },
    nodeChange(node, nodeIds, nodeNames) {
      this.page.condition.selectAll = false;
      this.$refs.table.condition.selectAll = false;
      this.selectNodeIds = nodeIds;
      this.selectNodeNames = nodeNames;
      this.getTestCases();
    },
    close() {
      this.selectNodeIds = [];
      this.selectNodeNames = [];
      this.projectId = '';
      this.$refs.table.clear();
    },
    getProjectNode(projectId, condition) {
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        return;
      }
      this.getProjectNodeForce(projectId, condition);
    },
    getProjectNodeForce(projectId = this.projectId, condition = this.page.condition) {
      const index = this.projects.findIndex(project => project.id === projectId);
      if (index !== -1) {
        this.projectName = this.projects[index].name;
      }
      if (projectId) {
        this.projectId = projectId;
      }
      this.getNodeTree(this, condition);
    },
    getVersionOptions() {
      getVersionFilters(this.projectId)
        .then(r => this.versionFilters = r.data);
    },
    changeVersion(currentVersion) {
      this.page.condition.versionId = currentVersion || null;
      this.getTestCases();
      this.getProjectNode(this.projectId, this.page.condition);
    },
    setSelectCounts(data) {
      this.$refs.baseRelevance.selectCounts = data;
    },
    pushCustomFieldToCondition(projectId) {
      getTestTemplate(projectId).then(data => {
        this.testCaseTemplate = data;
        this.page.condition.components = initTestCaseConditionComponents(this.page.condition, this.testCaseTemplate.customFields);
        this.testCaseTemplate.customFields.forEach(item => {
          if (item.name === '用例等级') {
            this.priorityOptions = item.options;
          }
        });
      });
    },
    getTagToolTips(tags) {
      try {
        let showTips = '';
        tags.forEach((item) => {
          showTips += item + ',';
        });
        return showTips.substr(0, showTips.length - 1);
      } catch (e) {
        return '';
      }
    },
  }
};
</script>

<style scoped>


.oneLine {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.node-tree {
  max-height: calc(75vh - 120px);
  overflow-y: auto;
}
</style>
