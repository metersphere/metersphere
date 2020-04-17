<template>

  <div>

    <el-dialog :title="$t('test_track.relevance_test_case')"
               :visible.sync="dialogFormVisible"
               @close="close"
               width="50%">

      <el-container class="main-content">
        <el-aside class="node-tree" width="250px">
          <plan-node-tree
            :tree-nodes="treeNodes"
            :plan-id="planId"
            :showAll=true
            @nodeSelectEvent="getCaseNameByNodeIds"
            ref="tree"></plan-node-tree>
        </el-aside>

        <el-container>
          <el-main class="case-content" v-loading="result.loading">
            <el-scrollbar style="height:100%">
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
                    :label="$t('test_track.name')"
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
        <div class="dialog-footer">
          <el-button
            @click="dialogFormVisible = false">
            {{$t('test_track.cancel')}}
          </el-button>
          <el-button
            type="primary"
            @click="saveCaseRelevance">
            {{$t('test_track.confirm')}}
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>


</template>

<script>

  import PlanNodeTree from './PlanNodeTree';

    export default {
      name: "TestCaseRelevance",
      components: {PlanNodeTree},
      data() {
        return {
          result: {},
          dialogFormVisible: false,
          isCheckAll: false,
          testCases: [],
          selectIds: new Set(),
          treeNodes: []
        };
      },
      props: {
        planId: {
          type: String
        }
      },
      watch: {
        planId() {
          this.getCaseNames();
        }
      },
      methods: {
        openTestCaseRelevanceDialog() {
          this.getCaseNames();
          this.dialogFormVisible = true;
        },
        saveCaseRelevance(){
          let param = {};
          param.planId = this.planId;
          param.testCaseIds = [...this.selectIds];
          this.$post('/test/plan/relevance' , param, () => {
            this.selectIds.clear();
            this.$message.success("保存成功");
            this.dialogFormVisible = false;
            this.$emit('refresh');
          });
        },
        getCaseNames(nodeIds) {
          let param = {};
          if (this.planId) {
            param.planId = this.planId;
          }
          if (nodeIds && nodeIds.length > 0){
            param.nodeIds = nodeIds;
          }
          this.result = this.$post('/test/case/name', param, response => {
            this.testCases = response.data;
            this.testCases.forEach(item => {
              item.checked = false;
            });
          });
        },
        getCaseNameByNodeIds(nodeIds) {
          this.dialogFormVisible = true;
          this.getCaseNames(nodeIds);
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
        close() {
          this.selectIds.clear();
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
    /*border-radius: 1px;*/
    /*padding-top: 5px ;*/
    /*height: 100%;*/
    margin-right: 10px;
    /*box-shadow: 0 2px 4px rgba(0, 0, 0, .12), 0 0 6px rgba(0, 0, 0, .04);*/
  }

  .el-header {
    background-color: darkgrey;
    color: #333;
    line-height: 60px;
  }

  .el-aside {
    /*color: #333;*/
    /*background-color: rgb(238, 241, 246)*/
  }
  .case-content {
    height: 100%;
  }

  .main-content {
    min-height: 300px;
  }

</style>
