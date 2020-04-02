<template>

  <div>

    <el-dialog :title="$t('test_track.create')"
               :visible.sync="dialogFormVisible"
               width="65%">

      <el-container style="min-height: 350px">
        <el-aside class="node_tree" width="200px" style="background-color: rgb(238, 241, 246)">
          <plan-node-tree></plan-node-tree>
        </el-aside>

        <el-container>
          <el-header >
            <el-checkbox ></el-checkbox>
          </el-header>
          <el-main style="height: 100px;">
            <el-scrollbar style="height:100%">
                <el-table
                  :data="testCases">
                  <el-table-column
                    prop="name"
                    style="width: 100%">
                    <template slot="header">
                      <el-checkbox v-model="checkAll"></el-checkbox>
                      用例名称
                    </template>
                    <template slot-scope="scope">
                      <el-checkbox v-model="scope.row.checked"></el-checkbox>
                      {{scope.row.name}}
                    </template>
                  </el-table-column>
                </el-table>
            </el-scrollbar>
          </el-main>
        </el-container>

      </el-container>



      <div slot="footer" class="dialog-footer">
        <el-button
          @click="dialogFormVisible = false">
          {{$t('test_track.cancel')}}
        </el-button>
        <el-button
          type="primary"
          @click="saveCase">
          {{$t('test_track.confirm')}}
        </el-button>
      </div>
    </el-dialog>


  </div>


</template>

<script>

  import {CURRENT_PROJECT} from '../../../../../common/constants';
  import PlanNodeTree from './PlanNodeTree';

    export default {
      name: "TestCaseEdit",
      components: {PlanNodeTree},
      data() {
        return {
          dialogFormVisible: false,
          count: 6,
          checkAll: false,
          testCases: [],
          form: {
            name: '',
          }

        };
      },
      methods: {
        openTestCaseEditDialog(testCase) {
          this.resetForm();
          this.operationType = 'add';
          if(testCase){
            //修改
            this.operationType = 'edit';
            let tmp = {};
            Object.assign(tmp, testCase);
            tmp.steps = JSON.parse(testCase.steps);
            Object.assign(this.form, tmp);
            this.form.module = testCase.nodeId;
          }
          this.dialogFormVisible = true;
        },
        handleAddStep(index, data) {
          let step = {};
          step.num = data.num + 1;
          step.desc = null;
          step.result = null;
          this.form.steps.forEach(step => {
            if(step.num > data.num){
              step.num ++;
            }
          });
          this.form.steps.push(step);
        },
        handleDeleteStep(index, data) {
          this.form.steps.splice(index, 1);
          this.form.steps.forEach(step => {
            if(step.num > data.num){
              step.num --;
            }
          });
        },
        saveCase(){
          this.$refs['relevanceFrom'].validate((valid) => {
            if (valid) {
              let param = {};
              Object.assign(param, this.form);
              param.steps = JSON.stringify(this.form.steps);
              param.nodeId = this.form.module;
              this.moduleOptions.forEach(item => {
                if(this.form.module === item.id){
                  param.nodePath = item.path;
                }
              });
              if(localStorage.getItem(CURRENT_PROJECT)) {
                param.projectId = JSON.parse(localStorage.getItem(CURRENT_PROJECT)).id;
              }
              this.$post('/test/case/' + this.operationType, param, () => {
                this.$message.success(this.$t('commons.save_success'));
                this.dialogFormVisible = false;
                this.$emit("refresh");
              });
            } else {
              return false;
            }
          });
        }
        ,
        resetForm() {
          if (this.$refs['relevanceFrom']) {
            this.$refs['relevanceFrom'].resetFields();
          }

        },
        load () {
          this.count += 2
        },
        getCaseNames(planId) {
          if(planId){
            let param = {};
            param.planId = planId;
            this.$post('/test/case/name/all', param, response => {
              this.testCases = response.data;
              this.testCases.forEach(item => {
                item.checked = false;
              });
            });
          }
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

  .node_tree{
    /*border-radius: 1px;*/
    /*padding-top: 5px ;*/
    /*height: 100%;*/
    /*box-shadow: 0 2px 4px rgba(0, 0, 0, .12), 0 0 6px rgba(0, 0, 0, .04);*/
  }

  .el-header {
    background-color: darkgrey;
    color: #333;
    line-height: 60px;
    height: 1%;
  }

  .el-aside {
    color: #333;
  }

</style>
