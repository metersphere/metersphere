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
          <el-link type="primary" class="project-link" @click="switchProject">{{projectName ? projectName :
            $t('test_track.switch_project') }}
          </el-link>
          <node-tree class="node-tree"
                     @nodeSelectEvent="nodeChange"
                     @refresh="refresh"
                     :tree-nodes="treeNodes"
                     ref="nodeTree"/>
        </el-aside>

        <el-container>
          <el-main class="case-content">
            <ms-table-header :condition.sync="condition" @search="search" title="" :show-create="false"/>
            <el-table
              :data="testCases"
              @filter-change="filter"
              row-key="id"
              @mouseleave.passive="leave"
              v-el-table-infinite-scroll="scrollLoading"
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

            <div v-if="!lineStatus" style="text-align: center">{{$t('test_track.review_view.last_page')}}</div>
            <div style="text-align: center">共 {{total}} 条</div>
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
  import elTableInfiniteScroll from 'el-table-infinite-scroll';

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
    directives: {
      'el-table-infinite-scroll': elTableInfiniteScroll
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
        pageSize: 50,
        currentPage: 1,
        total: 0,
        lineStatus: true,
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
        this.condition.planId = this.planId;
      },
      selectNodeIds() {
        if (this.dialogFormVisible) {
          this.search();
        }
      },
      projectId() {
        this.condition.projectId = this.projectId;
        this.getProjectNode();
      }
    },
    updated() {
      this.toggleSelection(this.testCases);
    },
    methods: {
      openTestCaseRelevanceDialog() {
        this.getProject();
        this.dialogFormVisible = true;
      },
      saveCaseRelevance() {
        let param = {};
        param.planId = this.planId;
        param.testCaseIds = [...this.selectIds];
        param.request = this.condition;
        // 选择全选则全部加入到评审，无论是否加载完全部
        if (this.testCases.length === param.testCaseIds.length) {
          param.testCaseIds = ['all'];
        }
        this.result = this.$post('/test/plan/relevance', param, () => {
          this.selectIds.clear();
          this.$success(this.$t('commons.save_success'));
          this.dialogFormVisible = false;
          this.$emit('refresh');
        });
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      search() {
        this.currentPage = 1;
        this.testCases = [];
        this.getTestCases(true);
      },
      getTestCases(flag) {
        if (this.planId) {
          this.condition.planId = this.planId;
        }
        if (this.selectNodeIds && this.selectNodeIds.length > 0) {
          this.condition.nodeIds = this.selectNodeIds;
        } else {
          this.condition.nodeIds = [];
        }
        if (this.projectId) {
          this.condition.projectId = this.projectId;
          this.result = this.$post(this.buildPagePath('/test/case/name'), this.condition, response => {
            let data = response.data;
            this.total = data.itemCount;
            let tableData = data.listObject;
            tableData.forEach(item => {
              item.checked = false;
            });
            flag ? this.testCases = tableData : this.testCases = this.testCases.concat(tableData);
            this.lineStatus = tableData.length === 50 && this.testCases.length < this.total;
          });
        }

      },
      handleSelectAll(selection) {
        if (selection.length > 0) {
          this.testCases.forEach(item => {
            this.selectIds.add(item.id);
          });
        } else {
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
      refresh() {
        this.close();
      },
      scrollLoading() {
        if (this.dialogFormVisible && this.lineStatus) {
          this.currentPage += 1;
          this.getTestCases();
        }
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
        this.lineStatus = false;
        this.selectIds.clear();
        this.selectNodeIds = [];
        this.selectNodeNames = [];
      },
      filter(filters) {
        _filter(filters, this.condition);
        this.search();
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
          this.result = this.$post("/test/plan/project/", {planId: this.planId}, res => {
            let data = res.data;
            if (data) {
              this.projects = data;
              this.projectId = data[0].id;
              this.projectName = data[0].name;
              this.search();
            }
          })
        }
      },
      switchProject() {
        this.$refs.switchProject.open({id: this.planId, url: '/test/plan/project/', type: 'plan'});
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
          {testPlanId: this.planId, projectId: this.projectId}, response => {
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
