<template>

  <div>

    <el-dialog :close-on-click-modal="false"
               :title="operationType == 'edit' ? $t('test_track.plan.edit_plan') : $t('test_track.plan.create_plan')"
               :visible.sync="dialogFormVisible"
               @close="close"
               width="65%">

      <el-form :model="form" :rules="rules" ref="planFrom">

        <el-row>
          <el-col :span="8" :offset="1">
            <el-form-item
              :placeholder="$t('test_track.plan.input_name')"
              :label="$t('test_track.plan.plan_name')"
              :label-width="formLabelWidth"
              prop="name">
              <el-input v-model="form.name"></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="11" :offset="2">
            <el-form-item :label="$t('test_track.plan.plan_project')" :label-width="formLabelWidth" prop="projectIds">
              <el-select
                :disabled="(form.status == null) ? false : true"
                v-model="form.projectIds"
                :placeholder="$t('test_track.plan.input_plan_project')"
                multiple
                style="width: 100%"
                collapse-tags
                filterable>
                <el-option
                  v-for="item in projects"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="10" :offset="1">
            <el-form-item :label="$t('test_track.plan.plan_principal')" :label-width="formLabelWidth" prop="principal">
              <el-select v-model="form.principal" :placeholder="$t('test_track.plan.input_plan_principal')" filterable>
                <el-option
                  v-for="item in principalOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item :label="$t('test_track.plan.plan_stage')" :label-width="formLabelWidth" prop="stage">
              <el-select v-model="form.stage" clearable :placeholder="$t('test_track.plan.input_plan_stage')">
                <el-option :label="$t('test_track.plan.smoke_test')" value="smoke"></el-option>
                <!--<el-option :label="$t('test_track.plan.functional_test')" value="functional"></el-option>-->
                <!--<el-option :label="$t('test_track.plan.integration_testing')" value="integration"></el-option>-->
                <el-option :label="$t('test_track.plan.system_test')" value="system"></el-option>
                <el-option :label="$t('test_track.plan.regression_test')" value="regression"></el-option>
                <!--<el-option :label="$t('test_track.plan.version_validation')" value="version"></el-option>-->
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row type="flex" justify="left" style="margin-top: 10px;">
          <el-col :span="23" :offset="1">
            <el-form-item :label="$t('commons.description')" :label-width="formLabelWidth" prop="description">
              <el-input v-model="form.description"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 4}"
                        :rows="2"
                        :placeholder="$t('commons.input_content')"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="operationType == 'edit'" type="flex" justify="left" style="margin-top: 10px;">
          <el-col :span="19" :offset="1">
            <el-form-item :label="$t('test_track.plan.plan_status')" :label-width="formLabelWidth" prop="status">
              <test-plan-status-button :status="form.status" @statusChange="statusChange"/>
            </el-form-item>
          </el-col>
        </el-row>

      </el-form>

      <template v-slot:footer>
        <div class="dialog-footer">
          <el-button
            @click="dialogFormVisible = false">
            {{ $t('test_track.cancel') }}
          </el-button>
          <el-button
            type="primary"
            @click="savePlan">
            {{ $t('test_track.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>


</template>

<script>

import {WORKSPACE_ID} from '../../../../../common/js/constants';
import TestPlanStatusButton from "../common/TestPlanStatusButton";
import {listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";
import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";

export default {
  name: "TestPlanEdit",
  components: {TestPlanStatusButton},
  data() {
    return {
      dialogFormVisible: false,
      form: {
        name: '',
        projectIds: [],
        principal: '',
        stage: '',
        description: ''
      },
      rules: {
        name: [
          {required: true, message: this.$t('test_track.plan.input_plan_name'), trigger: 'blur'},
          {max: 30, message: this.$t('test_track.length_less_than') + '30', trigger: 'blur'}
        ],
        projectIds: [{required: true, message: this.$t('test_track.plan.input_plan_project'), trigger: 'change'}],
        principal: [{required: true, message: this.$t('test_track.plan.input_plan_principal'), trigger: 'change'}],
        stage: [{required: true, message: this.$t('test_track.plan.input_plan_stage'), trigger: 'change'}],
        description: [{max: 200, message: this.$t('test_track.length_less_than') + '200', trigger: 'blur'}]
      },
      formLabelWidth: "120px",
      operationType: '',
      projects: [],
      principalOptions: []
    };
  },
  methods: {
    openTestPlanEditDialog(testPlan) {
      this.resetForm();
      this.getProjects();
      this.setPrincipalOptions();
      this.operationType = 'add';
      if (testPlan) {
        //修改
        this.operationType = 'edit';
        let tmp = {};
        Object.assign(tmp, testPlan);
        Object.assign(this.form, tmp);
      }
      listenGoBack(this.close);
      this.dialogFormVisible = true;
    },
    savePlan() {
      this.$refs['planFrom'].validate((valid) => {
        if (valid) {
          let param = {};
          Object.assign(param, this.form);
          param.name = param.name.trim();
          if (param.name == '') {
            this.$warning(this.$t('test_track.plan.input_plan_name'));
            return;
          }
          param.workspaceId = localStorage.getItem(WORKSPACE_ID);
          this.$post('/test/plan/' + this.operationType, param, () => {
            this.$success(this.$t('commons.save_success'));
            this.dialogFormVisible = false;
            this.$emit("refresh");
            // 发送广播，刷新 head 上的最新列表
            TrackEvent.$emit(LIST_CHANGE);
          });
        } else {
          return false;
        }
      });
    },
    getProjects() {
      this.$get("/project/listAll", (response) => {
        if (response.success) {
          this.projects = response.data;
        } else {
          this.$warning()(response.message);
        }
      });
    },
    setPrincipalOptions() {
      let workspaceId = localStorage.getItem(WORKSPACE_ID);
      this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
        this.principalOptions = response.data;
      });
    },
    statusChange(status) {
      this.form.status = status;
      this.$forceUpdate();
    },
    close() {
      removeGoBackListener(this.close);
      this.dialogFormVisible = false;
    },
    resetForm() {
      //防止点击修改后，点击新建触发校验
      if (this.$refs['planFrom']) {
        this.$refs['planFrom'].validate((valid) => {
          this.$refs['planFrom'].resetFields();
          this.form.name = '';
          this.form.projectIds = [];
          this.form.principal = '';
          this.form.stage = '';
          this.form.description = '';
          this.form.status = null;
          return true;
        });
      }
    }
  }
}
</script>

<style scoped>

</style>
