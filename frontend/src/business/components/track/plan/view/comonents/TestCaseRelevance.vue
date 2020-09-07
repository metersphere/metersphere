<template>

  <div>

    <el-dialog :title="$t('test_track.plan_view.relevance_test_case')"
               :visible.sync="dialogFormVisible"
               @close="close"
               width="60%" v-loading="result.loading"
               :close-on-click-modal="false"
               top="50px">

      <el-container class="main-content">
        <el-aside class="tree-aside" width="250px">
          <el-link type="primary" class="project-link" @click="switchProject">{{projectName ? projectName : '切换项目' }}</el-link>
          <node-tree class="node-tree"
                     @nodeSelectEvent="nodeChange"
                     @refresh="refresh"
                     :tree-nodes="treeNodes"
                     ref="nodeTree"/>
        </el-aside>

        <el-container>
          <el-main class="case-content">
            <ms-table-header :condition.sync="condition" @search="getCaseNames" title="" :show-create="false"/>
            <el-table
              :data="testCases"
              @filter-change="filter"
              row-key="id"
              @select-all="handleSelectAll"
              @select="handleSelectionChange"
              height="50vh"
              ref="table">

              <el-table-column
                type="selection"></el-table-column>

              <el-table-column
                prop="name"
                :label="$t('test_track.case.name')"
                style="width: 100%">
                <template v-slot:default="scope">
                  {{scope.row.name}}
                </template>
              </el-table-column>
              <el-table-column
                prop="priority"
                :filters="priorityFilters"
                column-key="priority"
                :label="$t('test_track.case.priority')"
                show-overflow-tooltip>
                <template v-slot:default="scope">
                  <priority-table-item :value="scope.row.priority"/>
                </template>
              </el-table-column>
              <el-table-column
                prop="type"
                :filters="typeFilters"
                column-key="type"
                :label="$t('test_track.case.type')"
                show-overflow-tooltip>
                <template v-slot:default="scope">
                  <type-table-item :value="scope.row.type"/>
                </template>
              </el-table-column>
            </el-table>
            <div style="text-align: center">共 {{testCases.length}} 条</div>
          </el-main>
        </el-container>
      </el-container>

      <template v-slot:footer>
        <ms-dialog-footer @cancel="dialogFormVisible = false" @confirm="saveCaseRelevance"/>
      </template>

    </el-dialog>

    <switch-project ref="switchProject" @getProjectNode="getProjectNode"/>
  </div>

</template>

<script>

  import NodeTree from '../../../common/NodeTree';
  import MsDialogFooter from '../../../../common/components/MsDialogFooter'
  import PriorityTableItem from "../../../common/tableItems/planview/PriorityTableItem";
  import TypeTableItem from "../../../common/tableItems/planview/TypeTableItem";
  import {_filter} from "../../../../../../common/js/utils";
  import MsTableSearchBar from "../../../../common/components/MsTableSearchBar";
  import MsTableAdvSearchBar from "../../../../common/components/search/MsTableAdvSearchBar";
  import MsTableHeader from "../../../../common/components/MsTableHeader";
  import {TEST_CASE_CONFIGS} from "../../../../common/components/search/search-components";
  import SwitchProject from "../../../case/components/SwitchProject";

  export default {
    name: "TestCaseRelevance",
    components: {
      NodeTree,
      MsDialogFooter,
      PriorityTableItem,
      TypeTableItem,
      MsTableSearchBar,
      MsTableAdvSearchBar,
      MsTableHeader,
      SwitchProject
    },
    data() {
      return {
        result: {},
        dialogFormVisible: false,
        isCheckAll: false,
        testCases: [],
        selectIds: new Set(),
        treeNodes: [],
        selectNodeIds: [],
        selectNodeNames: [],
        projectId: '',
        projectName: '',
        projects: [],
        condition: {
          components: TEST_CASE_CONFIGS
        },
        priorityFilters: [
          {text: 'P0', value: 'P0'},
          {text: 'P1', value: 'P1'},
          {text: 'P2', value: 'P2'},
          {text: 'P3', value: 'P3'}
        ],
        typeFilters: [
          {text: this.$t('commons.functional'), value: 'functional'},
          {text: this.$t('commons.performance'), value: 'performance'},
          {text: this.$t('commons.api'), value: 'api'}
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
        this.initData();
      },
      selectNodeIds() {
        this.getCaseNames();
      },
      projectId() {
        this.getProjectNode();
      }
    },
    updated() {
      this.toggleSelection(this.testCases);
    },
    methods: {
      openTestCaseRelevanceDialog() {
        this.initData();
        this.dialogFormVisible = true;
      },
      saveCaseRelevance() {
        let param = {};
        param.planId = this.planId;
        param.testCaseIds = [...this.selectIds];
        this.result = this.$post('/test/plan/relevance', param, () => {
          this.selectIds.clear();
          this.$success(this.$t('commons.save_success'));
          this.dialogFormVisible = false;
          this.$emit('refresh');
        });
      },
      getCaseNames() {
        if (this.planId) {
          // param.planId = this.planId;
          this.condition.planId = this.planId;
        }
        if (this.selectNodeIds && this.selectNodeIds.length > 0) {
          // param.nodeIds = this.selectNodeIds;
          this.condition.nodeIds = this.selectNodeIds;
        } else {
          this.condition.nodeIds = [];
        }

        if (this.projectId) {
          this.condition.projectId = this.projectId;
          this.result = this.$post('/test/case/name', this.condition, response => {
            this.testCases = response.data;
            this.testCases.forEach(item => {
              item.checked = false;
            });
          });
        }

      },
      handleSelectAll(selection) {
        if (selection.length > 0) {
          this.testCases.forEach(item => {
            this.selectIds.add(item.id);
          });
        } else {
          // this.selectIds.clear();
          this.testCases.forEach(item => {
            if (this.selectIds.has(item.id)) {
              this.selectIds.delete(item.id);
            }
          });
        }
      },
      handleSelectionChange(selection, row) {
        if (this.selectIds.has(row.id)) {
          this.selectIds.delete(row.id);
        } else {
          this.selectIds.add(row.id);
        }
      },
      nodeChange(nodeIds, nodeNames) {
        this.selectNodeIds = nodeIds;
        this.selectNodeNames = nodeNames;
      },
      initData() {
        this.getCaseNames();
        this.getAllNodeTreeByPlanId();
        this.getProject();
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
          this.result = this.$post("/case/node/list/all/plan", param , response => {
            this.treeNodes = response.data;
          });
        }
      },
      close() {
        this.selectIds.clear();
        this.selectNodeIds = [];
        this.selectNodeNames = [];
      },
      filter(filters) {
        _filter(filters, this.condition);
        this.initData();
      },
      toggleSelection(rows) {
        rows.forEach(row => {
          this.selectIds.forEach(id => {
            if (row.id === id) {
              // true 是为选中
              this.$refs.table.toggleRowSelection(row, true)
            }
          })
        })
      },
      getProject() {
        if (this.planId) {
          this.$get("/test/plan/project/" + this.planId,res => {
            let data = res.data;
            if (data) {
              this.projects = data;
              this.projectId = data[0].id;
              this.projectName = data[0].name;
            }
          })
        }
      },
      switchProject() {
        this.$refs.switchProject.open(this.planId);
      },
      getProjectNode(projectId) {
        const index = this.projects.findIndex(project => project.id === projectId);
        if (index !== -1) {
          this.projectName = this.projects[index].name;
        }
        if (projectId) {
          this.projectId = projectId;
        }
        this.result = this.$post("/case/node/list/all/plan",
          {testPlanId: this.planId, projectId: this.projectId} , response => {
          this.treeNodes = response.data;
        });

        this.selectNodeIds = [];
      }
    }
  }
</script>

<style scoped>

  .tb-edit .el-input {
    display: none;
    color: black;
  }

  .tb-edit .current-row .el-input {
    display: block;

  }

  .tb-edit .current-row .el-input + span {
    display: none;

  }

  .node-tree {
    margin-right: 10px;
  }

  .el-header {
    background-color: darkgrey;
    color: #333;
    line-height: 60px;
  }

  .case-content {
    padding: 0px 20px;
    height: 100%;
    /*border: 1px solid #EBEEF5;*/
  }

  .tree-aside {
    min-height: 300px;
    max-height: 100%;
  }

  .main-content {
    min-height: 300px;
    height: 100%;
    /*border: 1px solid #EBEEF5;*/
  }

  .project-link {
    float: right;
    margin-right: 12px;
    margin-bottom: 10px;
  }

</style>
