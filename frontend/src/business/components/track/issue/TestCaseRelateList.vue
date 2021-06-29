<template>
    <test-case-relevance-base
      @setProject="setProject"
      @save="save"
      ref="baseRelevance">

      <template v-slot:aside>
        <ms-node-tree class="node-tree"
                   v-loading="result.loading"
                   @nodeSelectEvent="nodeChange"
                   :tree-nodes="treeNodes"
                   ref="nodeTree"/>
      </template>

      <el-card>

        <ms-table-header :condition="condition" @search="initTableData" title="" :show-create="false"/>
        <ms-table
          v-loading="result.loading"
          :data="tableData"
          :condition="condition"
          :total="total"
          :page-size.sync="pageSize"
          :show-select-all="false"
          @handlePageChange="initTableData"
          @refresh="initTableData"
          ref="table">

          <ms-table-column
            :label="$t('commons.id')"
            prop="num">
          </ms-table-column>

          <ms-table-column
            :label="$t('commons.name')"
            prop="name">
          </ms-table-column>

          <ms-table-column
            :label="$t('test_track.case.priority')"
            prop="name">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.priority" ref="priority"/>
            </template>
          </ms-table-column>

          <ms-table-column
            :label="$t('test_track.case.type')"
            prop="type">
            <template v-slot:default="scope">
              <type-table-item :value="scope.row.type"/>
            </template>
          </ms-table-column>

          <ms-table-column
            :label="$t('test_track.case.module')"
            prop="nodePath">
          </ms-table-column>

          <ms-table-column
            :label="$t('test_track.plan.plan_project')"
            prop="projectName">
          </ms-table-column>

        </ms-table>

        <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total"/>
      </el-card>

    </test-case-relevance-base>
</template>

<script>
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import {CUSTOM_FIELD_LIST} from "@/common/js/default-table-header";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsEditDialog from "@/business/components/common/components/MsEditDialog";
import TestCaseRelevanceBase from "@/business/components/track/plan/view/comonents/base/TestCaseRelevanceBase";
import MsNodeTree from "@/business/components/track/common/NodeTree";
import PriorityTableItem from "@/business/components/track/common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "@/business/components/track/common/tableItems/planview/TypeTableItem";
export default {
  name: "TestCaseRelateList",
  components: {
    TypeTableItem,
    PriorityTableItem,
    MsNodeTree,
    TestCaseRelevanceBase,
    MsEditDialog,
    MsTableHeader,
    MsTablePagination, MsTableButton, MsTableColumn, MsTable},
  data() {
    return {
      tableData: [],
      condition: {},
      visible: false,
      total: 0,
      pageSize: 10,
      currentPage: 1,
      projectId: '',
      result: {},
      treeNodes: [],
      projects: [],
      selectNodeIds: [],
    };
  },
  props: [
    'testCaseContainIds'
  ],
  watch: {
    selectNodeIds() {
      this.initTableData();
    },
    projectId() {
      this.getProjectNode();
      this.initTableData();
    },
  },
  computed: {
    fields() {
      return CUSTOM_FIELD_LIST;
    },

  },
  methods: {
    initTableData() {
      this.condition.projectId = this.projectId;
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        this.condition.nodeIds = this.selectNodeIds;
      } else {
        this.condition.nodeIds = [];
      }
      if (this.projectId) {
        this.condition.projectId = this.projectId;
        this.condition.testCaseContainIds = Array.from(this.testCaseContainIds)
        this.result = this.$post('/test/case/relate/issue/' + +this.currentPage + '/' + this.pageSize, this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
        });
      }
    },
    nodeChange(node, nodeIds, nodeNames) {
      this.selectNodeIds = nodeIds;
      this.selectNodeNames = nodeNames;
    },
    getProjectNode(projectId) {
      if (projectId) {
        this.projectId = projectId;
      }
      this.$refs.nodeTree.result = this.$post("/case/node/list/project",
        {projectId: this.projectId}, response => {
          this.treeNodes = response.data;
        });
      this.selectNodeIds = [];
    },
    open() {
      this.$refs.baseRelevance.open();
      this.initTableData();
    },
    save() {
      this.$emit('save', this.$refs.table.selectRows);
      this.$refs.table.clear();
      this.$refs.baseRelevance.close();
    },
    setProject(projectId) {
      this.projectId = projectId;
    }
  }
};
</script>

<style scoped>

</style>
