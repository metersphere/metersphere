<template>

  <div>

    <el-dialog :close-on-click-modal="false"
               @close="close"
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
          <el-col :span="9" :offset="1">
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
          <el-col :span="9" :offset="1" v-if="form.testId=='other'">
            <el-form-item :label="$t('test_track.case.test_name')" :label-width="formLabelWidth" prop="testId">
              <el-input v-model="form.otherTestName" :placeholder="$t('test_track.case.input_test_case')"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="margin-top: 15px;">
          <el-col :offset="2">{{ $t('test_track.case.prerequisite') }}:</el-col>
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
          <el-col :offset="2">{{ $t('test_track.case.steps') }}:</el-col>
        </el-row>

        <el-row v-if="form.method && form.method != 'auto'" type="flex" justify="center">
          <el-col :span="20">
            <el-table
              v-if="isStepTableAlive"
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
                    class="table-edit-input"
                    size="mini"
                    :disabled="readOnly"
                    type="textarea"
                    :autosize="{ minRows: 1, maxRows: 6}"
                    :rows="2"
                    v-model="scope.row.desc"
                    :placeholder="$t('commons.input_content')"
                    clearable/>
                </template>
              </el-table-column>
              <el-table-column :label="$t('test_track.case.expected_results')" prop="result" min-width="35%">
                <template v-slot:default="scope">
                  <el-input
                    class="table-edit-input"
                    size="mini"
                    :disabled="readOnly"
                    type="textarea"
                    :autosize="{ minRows: 1, maxRows: 6}"
                    :rows="2"
                    v-model="scope.row.result"
                    :placeholder="$t('commons.input_content')"
                    clearable/>
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
                    :disabled="readOnly || (scope.$index == 0 && form.steps.length <= 1)"></el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>

        <el-row style="margin-top: 15px;margin-bottom: 10px">
          <el-col :offset="2">{{ $t('commons.remark') }}:</el-col>
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

import {TokenKey, WORKSPACE_ID} from '../../../../../common/js/constants';
import MsDialogFooter from '../../../common/components/MsDialogFooter'
import {listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";
import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";

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
        otherTestName: '',
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
          {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
        ],
        module: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
        maintainer: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        priority: [{required: true, message: this.$t('test_track.case.input_priority'), trigger: 'change'}],
        type: [{required: true, message: this.$t('test_track.case.input_type'), trigger: 'change'}],
        testId: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
        method: [{required: true, message: this.$t('test_track.case.input_method'), trigger: 'change'}],
        prerequisite: [{max: 500, message: this.$t('test_track.length_less_than') + '500', trigger: 'blur'}],
        remark: [{max: 500, message: this.$t('test_track.length_less_than') + '500', trigger: 'blur'}]
      },
      formLabelWidth: "120px",
      operationType: '',
      isCreateContinue: false,
      isStepTableAlive: true
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
    },
    currentProject() {
      this.getTestOptions();
    }
  },
  methods: {
    reload() {
      this.isStepTableAlive = false;
      this.$nextTick(() => (this.isStepTableAlive = true));
    },
    open(testCase) {
      this.resetForm();

      if (window.history && window.history.pushState) {
        history.pushState(null, null, document.URL);
        window.addEventListener('popstate', this.close);
      }
      listenGoBack(this.close);
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
      this.reload();
      this.dialogFormVisible = true;
    },
    handleAddStep(index, data) {
      let step = {};
      step.num = data.num + 1;
      step.desc = "";
      step.result = "";
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
    close() {
      //移除监听，防止监听其他页面
      removeGoBackListener(this.close);
      this.dialogFormVisible = false;
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
              // 发送广播，刷新 head 上的最新列表
              TrackEvent.$emit(LIST_CHANGE);
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
          this.testOptions.unshift({id: 'other', name: this.$t('test_track.case.other')})
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
          this.form.testName = '';
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

.el-switch {
  margin-bottom: 10px;
}

.case-name {
  width: 194px;
}

</style>
