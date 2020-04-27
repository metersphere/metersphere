<template>

  <div>

    <el-dialog :title="$t('test_track.plan_view.relevance_test_case')"
               :visible.sync="dialogFormVisible"
               @close="close"
               width="50%">

      <el-container class="main-content">
        <el-aside class="tree-aside" width="250px">
          <node-tree class="node-tree"
                     @nodeSelectEvent="nodeChange"
                     @refresh="refresh"
                     :tree-nodes="treeNodes"
                     ref="nodeTree"/>
        </el-aside>

        <el-container>
          <el-main class="case-content" v-loading="result.loading">
            <el-scrollbar>
                <el-table
                  :data="testCases"
                  row-key="id"
                  @select-all="handleSelectAll"
                  @select="handleSelectionChange"
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
                </el-table>
            </el-scrollbar>
          </el-main>
        </el-container>
      </el-container>

      <template v-slot:footer>
        <ms-dialog-footer @cancel="dialogFormVisible = false" @confirm="saveCaseRelevance"/>
      </template>

    </el-dialog>
  </div>

</template>

<script>

  import NodeTree from '../../../common/NodeTree';
  import MsDialogFooter from '../../../../common/components/MsDialogFooter'

  export default {
      name: "TestCaseRelevance",
      components: {NodeTree, MsDialogFooter},
      data() {
        return {
          result: {},
          dialogFormVisible: false,
          isCheckAll: false,
          testCases: [],
          selectIds: new Set(),
          treeNodes: [],
          selectNodeIds: [],
          selectNodeNames: []
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
        }
      },
      methods: {
        openTestCaseRelevanceDialog() {
          this.initData();
          this.dialogFormVisible = true;
        },
        saveCaseRelevance(){
          let param = {};
          param.planId = this.planId;
          param.testCaseIds = [...this.selectIds];
          this.$post('/test/plan/relevance' , param, () => {
            this.selectIds.clear();
            this.$success("保存成功");
            this.dialogFormVisible = false;
            this.$emit('refresh');
          });
        },
        getCaseNames() {
          let param = {};
          if (this.planId) {
            param.planId = this.planId;
          }
          if (this.selectNodeIds && this.selectNodeIds.length > 0){
            param.nodeIds = this.selectNodeIds;
          }
          this.result = this.$post('/test/case/name', param, response => {
            this.testCases = response.data;
            this.testCases.forEach(item => {
              item.checked = false;
            });
          });
        },
        handleSelectAll(selection) {
          if(selection.length > 0){
            this.testCases.forEach(item => {
              this.selectIds.add(item.id);
            });
          } else {
            this.selectIds.clear();
          }
        },
        handleSelectionChange(selection, row) {
          if(this.selectIds.has(row.id)){
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
        },
        refresh() {
          this.close();
        },
        getAllNodeTreeByPlanId() {
          if (this.planId) {
            this.result = this.$get("/case/node/list/all/plan/" + this.planId, response => {
              this.treeNodes = response.data;
            });
          }
        },
        close() {
          this.selectIds.clear();
          this.selectNodeIds = [];
          this.selectNodeNames = [];
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
  .tb-edit .current-row .el-input+span {
    display: none;

  }

  .node-tree{
    margin-right: 10px;
  }

  .el-header {
    background-color: darkgrey;
    color: #333;
    line-height: 60px;
  }

  .case-content {
    height: 500px;
    /*border: 1px solid #EBEEF5;*/
  }

  .main-content {
    min-height: 300px;
    /*border: 1px solid #EBEEF5;*/
  }

  .el-scrollbar {
    height: 100%;
  }

</style>
