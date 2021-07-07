<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :plan-id="planId"
    :flag="true"
    ref="baseRelevance">

    <template v-slot:aside>
      <node-tree class="node-tree"
                 :is-display="openType"
                 v-loading="result.loading"
                 @nodeSelectEvent="nodeChange"
                 :tree-nodes="treeNodes"
                 ref="nodeTree"/>
    </template>

    <ms-table-header :condition.sync="page.condition" @search="search" title="" :show-create="false"/>

    <ms-table
      v-loading="page.result.loading"
      :data="page.data"
      :condition="page.condition"
      :total="page.total"
      :page-size.sync="page.pageSize"
      :screen-height="null"
      @handlePageChange="search"
      @refresh="search"
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

    <ms-table-pagination :change="search" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize" :total="page.total"/>
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
import {buildPagePath, getPageDate, getPageInfo} from "@/common/js/tableUtils";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTag from "@/business/components/common/components/MsTag";


export default {
  name: "TestCaseFunctionalRelevance",
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
      treeNodes: [],
      selectNodeIds: [],
      selectNodeNames: [],
      projectId: '',
      projectName: '',
      projects: [],
      page: getPageInfo(),
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
    planId: {
      type: String
    }
  },
  watch: {
    planId() {
      this.page.condition.planId = this.planId;
    },
    selectNodeIds() {
      this.search();
    },
    projectId() {
      this.page.condition.projectId = this.projectId;
      this.getProjectNode();
      this.search();
      this.getProject();
    }
  },
  methods: {
    open() {
      this.$refs.baseRelevance.open();
      if (this.$refs.table) {
        this.$refs.table.clear();
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
    saveCaseRelevance(item) {
      let param = {};
      param.planId = this.planId;
      param.ids = this.$refs.table.selectIds;
      param.request = this.page.condition;
      param.checked = item
      this.result = this.$post('/test/plan/relevance', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.baseRelevance.close();
        this.$emit('refresh');
      });
    },
    search() {
      this.getTestCases();
    },
    getTestCases() {
      let condition = this.page.condition;
      if (this.planId) {
        condition.planId = this.planId;
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        condition.nodeIds = this.selectNodeIds;
      } else {
        condition.nodeIds = [];
      }
      if (this.projectId) {
        condition.projectId = this.projectId;
        this.page.result = this.$post(buildPagePath('/test/case/relate', this.page), condition, response => {
          getPageDate(response, this.page);
          let data = this.page.data;
          data.forEach(item => {
            item.checked = false;
            item.tags = JSON.parse(item.tags);
          });
        });
      }
    },
    nodeChange(node, nodeIds, nodeNames) {
      this.selectNodeIds = nodeIds;
      this.selectNodeNames = nodeNames;
    },
    refresh() {
      this.close();
    },
    getAllNodeTreeByPlanId() {
      if (this.planId) {
        let param = {
          testPlanId: this.planId,
          projectId: this.projectId
        };
        this.result = this.$post("/case/node/list/all/plan", param, response => {
          this.treeNodes = response.data;
        });
      }
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
      this.$refs.nodeTree.result = this.$post("/case/node/list/all/plan",
        {testPlanId: this.planId, projectId: this.projectId}, response => {
          this.treeNodes = response.data;
        });
      this.selectNodeIds = [];
    }
  }
}
</script>

<style scoped>
</style>
