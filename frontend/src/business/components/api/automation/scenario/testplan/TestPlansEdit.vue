<template>

  <div>

    <el-dialog
      :title="$t('test_track.plan.create_plan')"
      :visible.sync="dialogFormVisible"
      @close="close"
      width="65%"
      append-to-body>

      <el-form :model="form" :rules="rules" ref="planFrom" v-if="isStepTableAlive">

        <el-row>
          <el-col :span="8" :offset="1">
            <el-form-item
              :label="$t('test_track.plan.plan_name')"
              :label-width="formLabelWidth"
              prop="name">
              <el-input v-model="form.name" :placeholder="$t('test_track.plan.input_plan_name')"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="10" :offset="1">
            <el-form-item :label="$t('commons.tag')" :label-width="formLabelWidth" prop="tag">
              <ms-input-tag :currentScenario="form" ref="tag"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="10" :offset="1">
            <el-form-item :label="$t('test_track.plan.plan_principal')" :label-width="formLabelWidth" prop="principal">
              <el-select v-model="form.principal" :placeholder="$t('test_track.plan.input_plan_principal')" filterable>
                <el-option
                  v-for="(item) in principalOptions"
                  :key="item.id"
                  :label="item.name + '(' + item.id + ')'"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item :label="$t('test_track.plan.plan_stage')" :label-width="formLabelWidth" prop="stage">
              <el-select v-model="form.stage" clearable :placeholder="$t('test_track.plan.input_plan_stage')">
                <el-option :label="$t('test_track.plan.smoke_test')" value="smoke"></el-option>
                <el-option :label="$t('test_track.plan.system_test')" value="system"></el-option>
                <el-option :label="$t('test_track.plan.regression_test')" value="regression"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!--start:xuxm增加自定义‘计划开始’，‘计划结束’时间字段-->
        <el-row>
          <el-col :span="8" :offset="1">
            <el-form-item
              :label="$t('test_track.plan.planned_start_time')"
              :label-width="formLabelWidth"
              prop="plannedStartTime">
              <el-date-picker :placeholder="$t('test_track.plan.planned_start_time')" v-model="form.plannedStartTime"
                              type="datetime" value-format="timestamp"></el-date-picker>
            </el-form-item>
          </el-col>

          <el-col :span="11" :offset="2">
            <el-form-item
              :label="$t('test_track.plan.planned_end_time')"
              :label-width="formLabelWidth"
              prop="plannedEndTime">
              <el-date-picker :placeholder="$t('test_track.plan.planned_end_time')" v-model="form.plannedEndTime"
                              type="datetime" value-format="timestamp"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <!--end:xuxm增加自定义‘计划开始’，‘计划结束’时间字段-->

        <el-row>
          <el-col :span="8" :offset="1">
            <el-form-item
              :label="$t('自动更新状态')"
              :label-width="formLabelWidth"
              prop="automaticStatusUpdate">
              <el-switch v-model="form.automaticStatusUpdate"/>
              <ms-instructions-icon :content="'当功能用例关联的接口或性能用例在测试计划执行后，自动更新功能用例的状态'"/>
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

        <el-row v-if="operationType === 'edit'" type="flex" justify="left" style="margin-top: 10px;">
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
import MsInstructionsIcon from "@/business/components/common/components/MsInstructionsIcon";
import TestPlanStatusButton from "@/business/components/track/plan/common/TestPlanStatusButton";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import {getCurrentProjectID, getCurrentWorkspaceId, listenGoBack, removeGoBackListener} from "@/common/js/utils";

export default {
  name: "TestPlansEdit",
  components: {MsInstructionsIcon, TestPlanStatusButton, MsInputTag},
  data() {
    return {
      isStepTableAlive: true,
      dialogFormVisible: false,
      form: {
        name: '',
        projectIds: [],
        principal: '',
        stage: '',
        description: '',
        plannedStartTime: '',
        plannedEndTime: '',
        automaticStatusUpdate: false
      },
      rules: {
        name: [
          {required: true, message: this.$t('test_track.plan.input_plan_name'), trigger: 'blur'},
          {max: 30, message: this.$t('test_track.length_less_than') + '30', trigger: 'blur'}
        ],
        principal: [{required: true, message: this.$t('test_track.plan.input_plan_principal'), trigger: 'change'}],
        stage: [{required: true, message: this.$t('test_track.plan.input_plan_stage'), trigger: 'change'}],
        description: [{max: 200, message: this.$t('test_track.length_less_than') + '200', trigger: 'blur'}]
      },
      formLabelWidth: "100px",
      operationType: '',
      principalOptions: []
    };
  },
  created() {
    //设置“测试阶段”和“负责人”的默认值
    this.form.stage = 'smoke';
    const adminToken = JSON.parse(localStorage.getItem("Admin-Token"));
    this.form.principal = adminToken.id;
  },
  methods: {
    reload() {
      this.isStepTableAlive = false;
      this.$nextTick(() => (this.isStepTableAlive = true));
    },
    openTestPlanEditDialog() {
      this.resetForm();
      this.setPrincipalOptions();
      this.form.tags = [];
      listenGoBack(this.close);
      this.dialogFormVisible = true;
      this.reload();
    },
    savePlan() {
      this.$refs['planFrom'].validate((valid) => {
        if (valid) {
          let param = {};
          Object.assign(param, this.form);
          param.name = param.name.trim();
          if (param.name === '') {
            this.$warning(this.$t('test_track.plan.input_plan_name'));
            return;
          }
          param.workspaceId = getCurrentWorkspaceId();
          if (this.form.tags instanceof Array) {
            this.form.tags = JSON.stringify(this.form.tags);
          }
          param.tags = this.form.tags;
          this.$post('/test/plan/add', param, () => {
            this.$success(this.$t('commons.save_success'));
            this.dialogFormVisible = false;
            this.$emit("refresh");
          });
        } else {
          return false;
        }
      });
    },
    validate(param) {
      if (param.name === '') {
        this.$warning(this.$t('test_track.plan.input_plan_name'));
        return false;
      }
      if (param.plannedStartTime > param.plannedEndTime) {
        this.$warning(this.$t('commons.date.data_time_error'));
        return false;
      }
      return true;
    },
    setPrincipalOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
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
        this.$refs['planFrom'].validate(() => {
          this.$refs['planFrom'].resetFields();
          this.form.name = '';
          this.form.projectIds = [];
          const adminToken = JSON.parse(localStorage.getItem("Admin-Token"));
          this.form.principal = adminToken.id;
          this.form.stage = 'smoke';
          this.form.description = '';
          this.form.status = null;
          this.form.plannedStartTime = null;
          this.form.plannedEndTime = null;
          return true;
        });
      }
    }
  }
};
</script>

<style scoped>
.instructions-icon {
  margin-left: 10px;
}
</style>
