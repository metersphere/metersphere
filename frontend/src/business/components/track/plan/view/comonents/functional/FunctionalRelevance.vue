<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :flag="isTestPlan"
    :multiple-project="multipleProject"
    :is-saving="isSaving"
    ref="baseRelevance">

    <template v-slot:aside>
      <node-tree class="node-tree"
                 :is-display="openType"
                 v-loading="result.loading"
                 @nodeSelectEvent="nodeChange"
                 :tree-nodes="treeNodes"
                 ref="nodeTree"/>
    </template>

    <ms-table-header :condition.sync="page.condition" @search="getTestCases" title="" :show-create="false"/>

    <ms-table
      v-loading="page.result.loading"
      :data="page.data"
      :condition="page.condition"
      :total="page.total"
      :page-size.sync="page.pageSize"
      :screen-height="null"
      @handlePageChange="getTestCases"
      @refresh="getTestCases"
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
        prop="priority"
        :filters="priorityFilters"
        sortable
        :label="$t('test_track.case.priority')">
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.priority"/>
        </template>
      </ms-table-column>

      <ms-table-column prop="tags" :label="$t('commons.tag')">
        <template v-slot:default="scope">
          <ms-tag v-for="(itemName, index)  in scope.row.tags" :key="index" type="success" effect="plain"
                  :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
          <span/>
        </template>
      </ms-table-column>

      <ms-table-column
        sortable
        :label="$t('commons.create_time')"
        prop="createTime">
        <template v-slot="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        sortable
        :label="$t('commons.update_time')"
        prop="updateTime">
        <template v-slot="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </ms-table-column>

    </ms-table>

    <ms-table-pagination :change="getTestCases" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize" :total="page.total"/>
  </test-case-relevance-base>

</template>

<script>

import NodeTree from '../../../../common/NodeTree';
import PriorityTableItem from "../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../common/tableItems/planview/TypeTableItem";
import MsTableSearchBar from "../../../../../common/components/MsTableSearchBar";
import MsTableAdvSearchBar from "../../../../../common/components/search/MsTableAdvSearchBar";
import MsTableHeader from "../../../../../common/components/MsTableHeader";
import TestCaseRelevanceBase from "../base/TestCaseRelevanceBase";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTag from "@/business/components/common/components/MsTag";

export default {
  name: "FunctionalRelevance",
  components: {
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
  },
  data() {
    return {
      openType: 'relevance',
      result: {},
      isSaving:false,
      treeNodes: [],
      selectNodeIds: [],
      selectNodeNames: [],
      projectId: '',
      projectName: '',
      projects: [],
      customNum: false,
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ]
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
    }
  },
  watch: {
    selectNodeIds() {
      this.getTestCases();
    },
    projectId() {
      this.page.condition.projectId = this.projectId;
      this.getProjectNode();
      this.getTestCases();
      this.getProject();
    }
  },
  methods: {
    open() {
      this.isSaving = false;
      this.$refs.baseRelevance.open();
      if (this.$refs.table) {
        this.$refs.table.clear();
      }
      if (this.projectId) {
        this.getProjectNode(this.projectId);
      }
    },
    setProject(projectId) {
      this.projectId = projectId;
    },
    getProject() {
      this.$get("/project/get/" + this.projectId, result => {
        let data = result.data;
        if (data) {
          this.customNum = data.customNum;
        }
      })
    },
    getTestCases() {
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
    saveCaseRelevance(item) {
      this.isSaving = true;
      let param = {};
      param.ids = this.$refs.table.selectIds;
      param.request = this.page.condition;
      param.checked = item;
      this.save(param, this);
    },
    nodeChange(node, nodeIds, nodeNames) {
      this.selectNodeIds = nodeIds;
      this.selectNodeNames = nodeNames;
    },
    close() {
      this.selectNodeIds = [];
      this.selectNodeNames = [];
      this.$refs.table.clear();
    },
    getProjectNode(projectId) {
      const index = this.projects.findIndex(project => project.id === projectId);
      if (index !== -1) {
        this.projectName = this.projects[index].name;
      }
      if (projectId) {
        this.projectId = projectId;
      }
      this.getNodeTree(this);
    }
  }
}
</script>

<style scoped>
</style>
