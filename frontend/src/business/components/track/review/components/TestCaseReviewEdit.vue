<template>

  <div>

    <el-dialog :close-on-click-modal="false"
               :title="operationType === 'edit' ? $t('test_track.review.edit_review') : $t('test_track.review.create_review')"
               :visible.sync="dialogFormVisible"
               @close="close"
               v-loading="result.loading"
               width="60%">

      <el-form :model="form" :rules="rules" ref="reviewForm">

        <el-row>
          <el-col :span="10">
            <el-form-item
              :placeholder="$t('test_track.review.input_review_name')"
              :label="$t('test_track.review.review_name')"
              :label-width="formLabelWidth"
              prop="name">
              <el-input v-model="form.name"/>
            </el-form-item>
          </el-col>
          <el-col :span="12" :offset="1">
            <el-form-item :label="$t('commons.tag')" :label-width="formLabelWidth" prop="tag">
              <ms-input-tag :currentScenario="form" ref="tag" size="-" v-if="isStepTableAlive"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="10">
            <el-form-item :label="$t('test_track.review.reviewer')" :label-width="formLabelWidth" prop="userIds">
              <el-select
                v-model="form.userIds"
                :placeholder="$t('test_track.review.input_reviewer')"
                filterable multiple
                style="width: 100%"
              >
                <el-option
                  v-for="item in reviewerOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12" :offset="1">
            <el-form-item :label="$t('test_track.review.end_time')" :label-width="formLabelWidth" prop="endTime">
              <el-date-picker @change="endTimeChange" type="datetime" :placeholder="$t('commons.select_date')"
                              v-model="form.endTime" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>


        <el-row type="flex" justify="left" style="margin-top: 10px;">
          <el-col :span="23">
            <el-form-item :label="$t('commons.description')" :label-width="formLabelWidth" prop="description">
              <el-input v-model="form.description"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 4}"
                        :rows="2"
                        :placeholder="$t('commons.input_content')"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="operationType === 'edit'" type="flex" justify="left" style="margin-top: 10px;">
          <el-col :span="19">
            <el-form-item :label="$t('test_track.review.review_status')" :label-width="formLabelWidth" prop="status">
              <test-plan-status-button :status="form.status" @statusChange="statusChange"/>
            </el-form-item>
          </el-col>
        </el-row>

      </el-form>

      <template v-slot:footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">
            {{ $t('test_track.cancel') }}
          </el-button>
          <el-button type="primary" @click="saveReview">
            {{ $t('test_track.confirm') }}
          </el-button>
          <el-button type="primary" @click="reviewInfo">
            {{ $t('test_track.planning_execution') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>


</template>

<script>

import TestPlanStatusButton from "../../plan/common/TestPlanStatusButton";
import {WORKSPACE_ID} from "@/common/js/constants";
import {getCurrentProjectID, listenGoBack, removeGoBackListener} from "@/common/js/utils";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";

export default {
  name: "TestCaseReviewEdit",
  components: {MsInputTag, TestPlanStatusButton},
  data() {
    return {
      isStepTableAlive: true,
      dialogFormVisible: false,
      result: {},
      form: {
        name: '',
        projectIds: [],
        userIds: [],
        stage: '',
        description: '',
        endTime: ''
      },
      dbProjectIds: [],
      rules: {
        name: [
          {required: true, message: this.$t('test_track.review.input_review_name'), trigger: 'blur'},
          {max: 30, message: this.$t('test_track.length_less_than') + '30', trigger: 'blur'}
        ],
        // projectIds: [{required: true, message: this.$t('test_track.plan.input_plan_project'), trigger: 'change'}],
        userIds: [{required: true, message: this.$t('test_track.review.input_reviewer'), trigger: 'change'}],
        stage: [{required: true, message: this.$t('test_track.plan.input_plan_stage'), trigger: 'change'}],
        description: [{max: 200, message: this.$t('test_track.length_less_than') + '200', trigger: 'blur'}],
        endTime: [{required: true, message: '请选择截止时间', trigger: 'blur'}]
      },
      formLabelWidth: "100px",
      operationType: '',
      reviewerOptions: []
    };
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    }
  },
  methods: {
    reload() {
      this.isStepTableAlive = false;
      this.$nextTick(() => (this.isStepTableAlive = true));
    },
    openCaseReviewEditDialog(caseReview) {
      this.resetForm();
      this.setReviewerOptions();
      this.operationType = 'save';
      if (caseReview) {
        //修改
        this.operationType = 'edit';
        let tmp = {};
        Object.assign(tmp, caseReview);
        Object.assign(this.form, tmp);
        this.dbProjectIds = JSON.parse(JSON.stringify(this.form.projectIds));
      } else {
        this.form.tags = [];
      }
      listenGoBack(this.close);
      this.dialogFormVisible = true;
      this.reload();
    },
    reviewInfo() {

      this.$refs['reviewForm'].validate((valid) => {
        if (valid) {
          let param = {};
          Object.assign(param, this.form);
          param.name = param.name.trim();
          if (this.form.tags instanceof Array) {
            this.form.tags = JSON.stringify(this.form.tags);
          }
          param.tags = this.form.tags;
          if (param.name === '') {
            this.$warning(this.$t('test_track.plan.input_plan_name'));
            return;
          }

          if (!this.compareTime(new Date().getTime(), this.form.endTime)) {
            return false;
          }
          param.projectId = this.projectId;
          if (this.projectId) {
            this.result = this.$post('/test/case/review/' + this.operationType, param, response => {
              this.dialogFormVisible = false;
              this.$router.push('/track/review/view/' + response.data);
            });
          }
        } else {
          return false;
        }
      });
    },

    saveReview() {
      this.$refs['reviewForm'].validate((valid) => {
        if (valid) {
          let param = {};
          Object.assign(param, this.form);
          param.name = param.name.trim();
          if (this.form.tags instanceof Array) {
            this.form.tags = JSON.stringify(this.form.tags);
          }
          param.tags = this.form.tags;
          if (param.name === '') {
            this.$warning(this.$t('test_track.plan.input_plan_name'));
            return;
          }

          if (!this.compareTime(new Date().getTime(), this.form.endTime)) {
            return false;
          }

          param.projectId = this.projectId;
          if (this.projectId) {
            this.result = this.$post('/test/case/review/' + this.operationType, param, () => {
              this.$success(this.$t('commons.save_success'));
              this.dialogFormVisible = false;
              this.$emit("refresh");
            });
          }

        } else {
          return false;
        }
      });
    },
    setReviewerOptions() {
      this.result = this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()},response => {
        this.reviewerOptions = response.data;
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
      if (this.$refs['reviewForm']) {
        this.$refs['reviewForm'].validate(() => {
          this.$refs['reviewForm'].resetFields();
          this.form.name = '';
          this.form.stage = '';
          this.form.endTime = '';
          this.form.description = '';
          this.form.status = null;
          this.form.projectIds = [];
          this.form.userIds = [];
          return true;
        });
      }
    },
    endTimeChange(value) {
      if (value) {
        this.form.endTime = this.form.endTime.getTime();
        this.compareTime(new Date().getTime(), value.getTime());
      }
    },
    compareTime(ts1, ts2) {
      if (ts1 > ts2) {
        this.$warning("截止时间不能早于当前时间！");
        return false;
      }
      return true;
    }
  }
}
</script>

<style scoped>

</style>

