<template>

  <div>

    <el-dialog :title="$t('test_track.create')" :visible.sync="dialogFormVisible" width="65%">

      <el-form :model="form" :rules="rules" ref="caseFrom">

        <el-row>
          <el-col :span="8" :offset="1">
            <el-form-item
              :placeholder="$t('test_track.input_name')"
              :label="$t('test_track.name')"
              :label-width="formLabelWidth"
              prop="name">
              <el-input v-model="form.name"></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="11" :offset="2">
            <el-form-item :label="$t('test_track.module')" :label-width="formLabelWidth" prop="module">
              <el-select
                v-model="form.module"
                :placeholder="$t('test_track.input_module')"
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
            <el-form-item :label="$t('test_track.maintainer')" :label-width="formLabelWidth" prop="maintainer">
              <el-select v-model="form.maintainer" :placeholder="$t('test_track.input_maintainer')" filterable>
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
            <el-form-item :label="$t('test_track.priority')" :label-width="formLabelWidth" prop="priority">
              <el-select v-model="form.priority" clearable :placeholder="$t('test_track.input_priority')">
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
            <el-form-item :label="$t('test_track.type')" :label-width="formLabelWidth" prop="type">
              <el-select v-model="form.type" :placeholder="$t('test_track.input_type')">
                <el-option :label="$t('commons.functional')" value="functional"></el-option>
                <el-option :label="$t('commons.performance')" value="performance"></el-option>
                <el-option :label="$t('commons.interface')" value="interface"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('test_track.method')" :label-width="formLabelWidth" prop="method">
              <el-select v-model="form.method" :placeholder="$t('test_track.input_method')">
                <el-option :label="$t('test_track.manual')" value="manual"></el-option>
                <el-option :label="$t('test_track.auto')" value="auto"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row style="margin-top: 15px;">
          <el-col :offset="2">{{$t('test_track.prerequisite')}}:</el-col>
        </el-row>
        <el-row type="flex" justify="center" style="margin-top: 10px;">
          <el-col :span="20">
            <el-form-item>
              <el-input v-model="form.prerequisite"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 4}"
                        :rows="2"
                        :placeholder="$t('test_track.input_prerequisite')"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row style="margin-bottom: 10px">
          <el-col :offset="2">{{$t('test_track.steps')}}:</el-col>
        </el-row>
        <el-row type="flex" justify="center">
          <el-col :span="20">
            <el-table
              :data="form.steps"
              class="tb-edit"
              border
              :default-sort = "{prop: 'num', order: 'ascending'}"
              highlight-current-row>
              <el-table-column :label="$t('test_track.number')" prop="num" min-width="15%"></el-table-column>
              <el-table-column :label="$t('test_track.step_desc')" prop="desc" min-width="35%">
                <template v-slot:default="scope">
                  <el-input
                    size="small"
                    v-model="scope.row.desc"
                    :placeholder="$t('commons.input_content')"
                    clearable></el-input>
                  <span>{{scope.row.desc}}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('test_track.expected_results')" prop="result" min-width="35%">
                <template v-slot:default="scope">
                  <el-input
                    size="small"
                    v-model="scope.row.result"
                    :placeholder="$t('commons.input_content')"
                    clearable></el-input>
                  <span>{{scope.row.result}}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('commons.input_content')" min-width="15%">
                <template v-slot:default="scope">
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
          <el-col :offset="2">{{$t('commons.remark')}}:</el-col>
        </el-row>
        <el-row type="flex" justify="center">
          <el-col :span="20">
            <el-form-item>
              <el-input v-model="form.remark"
                        :autosize="{ minRows: 2, maxRows: 4}"
                        type="textarea"
                        :rows="2"
                        :placeholder="$t('commons.input_content')"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template v-slot:footer>
        <div class="dialog-footer">
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
      </template>
    </el-dialog>

  </div>


</template>

<script>

  import {CURRENT_PROJECT} from '../../../../../common/constants';

  export default {
      name: "TestCaseEdit",
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
            name :[{required: true, message: this.$t('test_track.input_name'), trigger: 'blur'}],
            module :[{required: true, message: this.$t('test_track.input_module'), trigger: 'change'}],
            maintainer :[{required: true, message: this.$t('test_track.input_maintainer'), trigger: 'change'}],
            priority :[{required: true, message: this.$t('test_track.input_priority'), trigger: 'change'}],
            type :[{required: true, message: this.$t('test_track.input_type'), trigger: 'change'}],
            method :[{required: true, message: this.$t('test_track.input_method'), trigger: 'change'}]
          },
          formLabelWidth: "120px",
          operationType: ''
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
          this.$refs['caseFrom'].validate((valid) => {
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
