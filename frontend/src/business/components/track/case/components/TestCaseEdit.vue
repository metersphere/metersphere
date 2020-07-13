<template>

  <div>

    <el-dialog
      :title="operationType == 'edit' ? ( readOnly ? $t('test_track.case.view_case') : $t('test_track.case.edit_case')) : $t('test_track.case.create')"
      :visible.sync="dialogFormVisible" width="65%">

      <el-form :model="form" :rules="rules" ref="caseFrom" v-loading="result.loading">

        <el-row>
          <el-col :span="8" :offset="1">
            <el-form-item
              :placeholder="$t('test_track.case.input_name')"
              :label="$t('test_track.case.name')"
              :label-width="formLabelWidth"
              prop="name">
              <el-input class="case-name" :disabled="readOnly" v-model="form.name"></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="11" :offset="2">
            <el-form-item :label="$t('test_track.case.module')" :label-width="formLabelWidth" prop="module">
              <el-select
                v-model="form.module"
                :disabled="readOnly"
                :placeholder="$t('test_track.case.input_module')"
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
            <el-form-item :label="$t('test_track.case.maintainer')" :label-width="formLabelWidth" prop="maintainer">
              <el-select :disabled="readOnly" v-model="form.maintainer"
                         :placeholder="$t('test_track.case.input_maintainer')" filterable>
                <el-option
                  v-for="item in maintainerOptions"
                  :key="item.id"
                  :label="item.id + ' (' + item.name + ')'"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('test_track.case.priority')" :label-width="formLabelWidth" prop="priority">
              <el-select :disabled="readOnly" v-model="form.priority" clearable
                         :placeholder="$t('test_track.case.input_priority')">
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
            <el-form-item :label="$t('test_track.case.type')" :label-width="formLabelWidth" prop="type">
              <el-select @change="typeChange" :disabled="readOnly" v-model="form.type"
                         :placeholder="$t('test_track.case.input_type')">
                <el-option :label="$t('commons.functional')" value="functional"></el-option>
                <el-option :label="$t('commons.performance')" value="performance"></el-option>
                <el-option :label="$t('commons.api')" value="api"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('test_track.case.method')" :label-width="formLabelWidth" prop="method">
              <el-select :disabled="readOnly" v-model="form.method" :placeholder="$t('test_track.case.input_method')">
                <el-option
                  v-for="item in methodOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="form.method && form.method == 'auto'">
          <el-col :span="10" :offset="1">
            <el-form-item :label="$t('test_track.case.relate_test')" :label-width="formLabelWidth" prop="testId">
              <el-select filterable :disabled="readOnly" v-model="form.testId"
                         :placeholder="$t('test_track.case.input_type')">
                <el-option
                  v-for="item in testOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row style="margin-top: 15px;">
          <el-col :offset="2">{{$t('test_track.case.prerequisite')}}:</el-col>
        </el-row>
        <el-row type="flex" justify="center" style="margin-top: 10px;">
          <el-col :span="20">
            <el-form-item prop="prerequisite">
              <el-input :disabled="readOnly" v-model="form.prerequisite"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 4}"
                        :rows="2"
                        :placeholder="$t('test_track.case.input_prerequisite')"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="form.method && form.method != 'auto'" style="margin-bottom: 10px">
          <el-col :offset="2">{{$t('test_track.case.steps')}}:</el-col>
        </el-row>

        <el-row v-if="form.method && form.method != 'auto'" type="flex" justify="center">
          <el-col :span="20">
            <el-table
              :data="form.steps"
              class="tb-edit"
              border
              size="mini"
              :default-sort="{prop: 'num', order: 'ascending'}"
              highlight-current-row>
              <el-table-column :label="$t('test_track.case.number')" prop="num" min-width="15%"></el-table-column>
              <el-table-column :label="$t('test_track.case.step_desc')" prop="desc" min-width="35%">
                <template v-slot:default="scope">
                  <el-input
                    size="mini"
                    v-if="!readOnly"
                    type="textarea"
                    :autosize="{ minRows: 2, maxRows: 4}"
                    :rows="2"
                    v-model="scope.row.desc"
                    :placeholder="$t('commons.input_content')"
                    clearable></el-input>
                  <span>{{scope.row.desc}}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('test_track.case.expected_results')" prop="result" min-width="35%">
                <template v-slot:default="scope">
                  <el-input
                    size="mini"
                    v-if="!readOnly"
                    type="textarea"
                    :autosize="{ minRows: 2, maxRows: 4}"
                    :rows="2"
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
                    :disabled="readOnly"
                    icon="el-icon-plus"
                    circle size="mini"
                    @click="handleAddStep(scope.$index, scope.row)"></el-button>
                  <el-button
                    type="danger"
                    icon="el-icon-delete"
                    circle size="mini"
                    @click="handleDeleteStep(scope.$index, scope.row)"
                    :disabled="readOnly || scope.$index == 0 ? true : false"></el-button>
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
            <el-form-item prop="remark">
              <el-input v-model="form.remark"
                        :autosize="{ minRows: 2, maxRows: 4}"
                        type="textarea"
                        :disabled="readOnly"
                        :rows="2"
                        :placeholder="$t('commons.input_content')"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template v-slot:footer>
        <el-switch v-if="operationType == 'add'"
                   v-model="isCreateContinue"
                   :active-text="$t('test_track.case.save_create_continue')">
        </el-switch>
        <ms-dialog-footer v-if="!readOnly"
                          @cancel="dialogFormVisible = false"
                          @confirm="saveCase"/>
      </template>

    </el-dialog>

  </div>


</template>

<script>

  import {CURRENT_PROJECT, WORKSPACE_ID, TokenKey} from '../../../../../common/js/constants';
  import MsDialogFooter from '../../../common/components/MsDialogFooter'


  export default {
    name: "TestCaseEdit",
    components: {MsDialogFooter},
    data() {
      return {
        result: {},
        dialogFormVisible: false,
        form: {
          name: '',
          module: '',
          maintainer: '',
          priority: '',
          type: '',
          method: '',
          prerequisite: '',
          testId: '',
          steps: [{
            num: 1,
            desc: '',
            result: ''
          }],
          remark: '',
        },
        moduleOptions: [],
        maintainerOptions: [],
        methodOptions: [],
        testOptions: [],
        workspaceId: '',
        rules: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
            {max: 30, message: this.$t('test_track.length_less_than') + '30', trigger: 'blur'}
          ],
          module: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
          maintainer: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          priority: [{required: true, message: this.$t('test_track.case.input_priority'), trigger: 'change'}],
          type: [{required: true, message: this.$t('test_track.case.input_type'), trigger: 'change'}],
          testId: [{required: true, message: '请选择测试', trigger: 'change'}],
          method: [{required: true, message: this.$t('test_track.case.input_method'), trigger: 'change'}],
          prerequisite: [{max: 300, message: this.$t('test_track.length_less_than') + '300', trigger: 'blur'}],
          remark: [{max: 300, message: this.$t('test_track.length_less_than') + '300', trigger: 'blur'}]
        },
        formLabelWidth: "120px",
        operationType: '',
        isCreateContinue: false
      };
    },
    props: {
      treeNodes: {
        type: Array
      },
      readOnly: {
        type: Boolean,
        default: true
      },
      selectNode: {
        type: Object
      },
      currentProject: {
        type: Object
      }
    },
    mounted() {
      this.getSelectOptions();
    },
    watch: {
      treeNodes() {
        this.getModuleOptions();
      }
    },
    methods: {
      open(testCase) {
        this.resetForm();
        this.operationType = 'add';
        if (testCase) {
          //修改
          this.operationType = 'edit';
          //复制
          if (testCase.name === '') {
            this.operationType = 'add';
          }
          let tmp = {};
          Object.assign(tmp, testCase);
          tmp.steps = JSON.parse(testCase.steps);
          Object.assign(this.form, tmp);
          this.form.module = testCase.nodeId;
        } else {
          if (this.selectNode.data) {
            this.form.module = this.selectNode.data.id;
          } else {
            if (this.moduleOptions.length > 0) {
              this.form.module = this.moduleOptions[0].id;
            }
          }
          let user = JSON.parse(localStorage.getItem(TokenKey));
          this.form.priority = 'P3';
          this.form.type = 'functional';
          this.form.method = 'manual';
          this.form.maintainer = user.id;
        }

        this.getSelectOptions();
        this.dialogFormVisible = true;
      },
      handleAddStep(index, data) {
        let step = {};
        step.num = data.num + 1;
        step.desc = null;
        step.result = null;
        this.form.steps.forEach(step => {
          if (step.num > data.num) {
            step.num++;
          }
        });
        this.form.steps.splice(index + 1, 0, step);
      },
      handleDeleteStep(index, data) {
        this.form.steps.splice(index, 1);
        this.form.steps.forEach(step => {
          if (step.num > data.num) {
            step.num--;
          }
        });
      },
      saveCase() {
        this.$refs['caseFrom'].validate((valid) => {
          if (valid) {
            let param = this.buildParam();
            if (this.validate(param)) {
              this.result = this.$post('/test/case/' + this.operationType, param, () => {
                this.$success(this.$t('commons.save_success'));
                if (this.operationType == 'add' && this.isCreateContinue) {
                  this.form.name = '';
                  this.form.prerequisite = '';
                  this.form.steps = [{
                    num: 1,
                    desc: '',
                    result: ''
                  }];
                  this.form.remark = '';
                  this.$emit("refresh");
                  return;
                }
                this.dialogFormVisible = false;
                this.$emit("refresh");
              });
            }
          } else {
            return false;
          }
        });
      },
      buildParam() {
        let param = {};
        Object.assign(param, this.form);
        param.steps = JSON.stringify(this.form.steps);
        param.nodeId = this.form.module;
        this.moduleOptions.forEach(item => {
          if (this.form.module === item.id) {
            param.nodePath = item.path;
          }
        });
        if (this.currentProject) {
          param.projectId = this.currentProject.id;
        }
        param.name = param.name.trim();
        if (param.method != 'auto') {
          param.testId = null;
        }
        return param;
      },
      validate(param) {
        for (let i = 0; i < param.steps.length; i++) {
          if ((param.steps[i].desc && param.steps[i].desc.length > 300) ||
            (param.steps[i].result && param.steps[i].result.length > 300)) {
            this.$warning(this.$t('test_track.case.step_desc') + ","
              + this.$t('test_track.case.expected_results') + this.$t('test_track.length_less_than') + '300');
            return false;
          }
        }
        if (param.name == '') {
          this.$warning(this.$t('test_track.case.input_name'));
          return false;
        }
        return true;
      },
      typeChange() {
        this.form.testId = '';
        this.getMethodOptions();
        this.getTestOptions()
      },
      getModuleOptions() {
        let moduleOptions = [];
        this.treeNodes.forEach(node => {
          this.buildNodePath(node, {path: ''}, moduleOptions);
        });
        this.moduleOptions = moduleOptions;
      },
      getMaintainerOptions() {
        let workspaceId = localStorage.getItem(WORKSPACE_ID);
        this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
          this.maintainerOptions = response.data;
        });
      },
      getTestOptions() {
        this.testOptions = [];
        if (this.currentProject && this.form.type != '' && this.form.type != 'functional') {
          this.result = this.$get('/' + this.form.type + '/list/' + this.currentProject.id, response => {
            this.testOptions = response.data;
          });
        }
      },
      getMethodOptions() {
        if (!this.form.type || this.form.type != 'functional') {
          this.methodOptions = [
            {value: 'auto', label: this.$t('test_track.case.auto')},
            {value: 'manual', label: this.$t('test_track.case.manual')}
          ];
        } else {
          this.form.method = 'manual';
          this.methodOptions = [{value: 'manual', label: this.$t('test_track.case.manual')}]
        }
      },
      getSelectOptions() {
        this.getModuleOptions();
        this.getMaintainerOptions();
        this.getTestOptions();
        this.getMethodOptions();
      },
      buildNodePath(node, option, moduleOptions) {
        //递归构建节点路径
        option.id = node.id;
        option.path = option.path + '/' + node.name;
        moduleOptions.push(option);
        if (node.children) {
          for (let i = 0; i < node.children.length; i++) {
            this.buildNodePath(node.children[i], {path: option.path}, moduleOptions);
          }
        }
      },
      resetForm() {
        //防止点击修改后，点击新建触发校验
        if (this.$refs['caseFrom']) {
          this.$refs['caseFrom'].validate((valid) => {
            this.$refs['caseFrom'].resetFields();
            this.form.name = '';
            this.form.module = '';
            this.form.type = '';
            this.form.method = '';
            this.form.maintainer = '';
            this.form.priority = '';
            this.form.prerequisite = '';
            this.form.remark = '';
            this.form.testId = '';
            this.form.steps = [{
              num: 1,
              desc: '',
              result: ''
            }];
            return true;
          });
        }
      }
    }
  }
</script>

<style scoped>

  .tb-edit .el-textarea {
    display: none;
  }

  .tb-edit .current-row .el-textarea {
    display: block;
  }

  .tb-edit .current-row .el-textarea + span {
    display: none;
  }

  .el-switch {
    margin-bottom: 10px;
  }

  .case-name {
    width: 194px;
  }

</style>
