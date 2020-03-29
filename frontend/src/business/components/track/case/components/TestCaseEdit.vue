<template>

  <div>

    <el-dialog title="新建用例" :visible.sync="dialogFormVisible" width="65%">

      <el-form :model="form" :rules="rules" ref="caseFrom">

        <el-row>
          <el-col :span="8" :offset="1">
            <el-form-item
              placeholder="请输入内容"
              label="用例名称"
              :label-width="formLabelWidth"
              prop="name">
              <el-input v-model="form.name"></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="11" :offset="2">
            <el-form-item label="所属模块" :label-width="formLabelWidth" prop="module">
              <el-select
                v-model="form.module"
                placeholder="请选择模块"
                filterable>
                <el-option
                  v-for="item in moduleOptions"
                  :key="item.id"
                  :label="item.path"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="10" :offset="1">
            <el-form-item label="维护人" :label-width="formLabelWidth" prop="maintainer">
              <el-select v-model="form.maintainer" placeholder="请选择维护人" filterable>
                <el-option
                  v-for="item in maintainerOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" :label-width="formLabelWidth" prop="priority">
              <el-select v-model="form.priority" clearable placeholder="请选择优先级">
                <el-option label="P0" value="P0"></el-option>
                <el-option label="P1" value="P1"></el-option>
                <el-option label="P2" value="P2"></el-option>
                <el-option label="P3" value="P3"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="10" :offset="1">
            <el-form-item label="用例类型" :label-width="formLabelWidth" prop="type">
              <el-select v-model="form.type" placeholder="请选择用例类型">
                <el-option label="功能测试" value="functional"></el-option>
                <el-option label="性能测试" value="performance"></el-option>
                <el-option label="接口测试" value="interface"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="测试方式" :label-width="formLabelWidth" prop="method">
              <el-select v-model="form.method" placeholder="请选择测试方式">
                <el-option label="手动" value="manual"></el-option>
                <el-option label="自动" value="auto"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row style="margin-top: 15px;">
          <el-col :offset="2">前置条件:</el-col>
        </el-row>
        <el-row type="flex" justify="center" style="margin-top: 10px;">
          <el-col :span="20">
            <el-form-item>
              <el-input v-model="form.prerequisite"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 4}"
                        :rows="2"
                        placeholder="请输入前置条件"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row style="margin-bottom: 10px">
          <el-col :offset="2">执行步骤:</el-col>
        </el-row>
        <el-row type="flex" justify="center">
          <el-col :span="20">
            <el-table
              :data="form.steps"
              class="tb-edit"
              border
              :default-sort = "{prop: 'num', order: 'ascending'}"
              highlight-current-row>
              <el-table-column label="编号" prop="num" min-width="15%"></el-table-column>
              <el-table-column label="步骤描述" prop="desc" min-width="35%">
                <template slot-scope="scope">
                  <el-input
                    size="small"
                    v-model="scope.row.desc"
                    placeholder="请输入内容"
                    clearable></el-input>
                  <span>{{scope.row.desc}}</span>
                </template>
              </el-table-column>
              <el-table-column label="预期结果" prop="result" min-width="35%">
                <template slot-scope="scope">
                  <el-input
                    size="small"
                    v-model="scope.row.result"
                    placeholder="请输入内容"
                    clearable></el-input>
                  <span>{{scope.row.result}}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" min-width="15%">
                <template slot-scope="scope">
                  <el-button
                    type="primary"
                    icon="el-icon-plus"
                    circle size="mini"
                    @click="handleAddStep(scope.$index, scope.row)"></el-button>
                  <el-button
                    type="danger"
                    icon="el-icon-delete"
                    circle size="mini"
                    @click="handleDeleteStep(scope.$index, scope.row)"
                    :disabled="scope.$index == 0 ? true : false"></el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>

        <el-row style="margin-top: 15px;margin-bottom: 10px">
          <el-col :offset="2">备注:</el-col>
        </el-row>
        <el-row type="flex" justify="center">
          <el-col :span="20">
            <el-form-item>
              <el-input v-model="form.remark"
                        :autosize="{ minRows: 2, maxRows: 4}"
                        type="textarea"
                        :rows="2"
                        placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button
          @click="dialogFormVisible = false">
          取 消
        </el-button>
        <el-button
          type="primary"
          @click="saveCase">
          确 定
        </el-button>
      </div>
    </el-dialog>

  </div>


</template>

<script>
    export default {
      name: "CreateCaseDialog",
      data() {
        return {
          dialogFormVisible: false,
          form: {
            name: '',
            module: '',
            maintainer: '',
            priority: '',
            type: '',
            method: '',
            prerequisite: '',
            steps: [{
              num: 1 ,
              desc: '',
              result: ''
            }],
            remark: '',
          },
          moduleOptions: [],
          maintainerOptions: [],
          rules:{
            name :[{required: true, message: '请输入用例名称', trigger: 'blur'}],
            module :[{required: true, message: '请选择模块', trigger: 'change'}],
            maintainer :[{required: true, message: '请选择维护人', trigger: 'change'}],
            priority :[{required: true, message: '请选择优先级', trigger: 'change'}],
            type :[{required: true, message: '请选择用例类型', trigger: 'change'}],
            method :[{required: true, message: '请选择测试方式', trigger: 'change'}]
          },
          formLabelWidth: "120px",
          operationType: ''
        };
      },
      props: {
        projectId: null
      },
      methods: {
        opentestCaseEditDialog(testCase) {
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
          this.$refs['caseFrom'].validate((valid) => {
            if (valid) {
              let param = {};
              Object.assign(param, this.form);
              param.steps = JSON.stringify(this.form.steps);
              param.nodeId = this.form.module;
              param.projectId = this.projectId;
              this.$post('/test/case/' + this.operationType, param, () => {
                this.$message.success("保存成功！");
                this.resetForm();
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
          if (this.$refs['caseFrom']) {
            this.$refs['caseFrom'].resetFields();
          }
          this.form.name = '';
          this.form.module = '';
          this.form.type = '';
          this.form.method = '';
          this.form.maintainer = '';
          this.form.priority = '';
          this.form.prerequisite = '';
          this.form.remark = '';
          this.form.steps = [{
            num: 1 ,
            desc: '',
            result: ''
          }];
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

</style>
