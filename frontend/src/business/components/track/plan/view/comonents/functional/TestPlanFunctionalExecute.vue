<template>
  <el-card class="executeCard">
    <test-plan-test-case-status-button class="status-button"
                                       @statusChange="statusChange"
                                       :is-read-only="statusReadOnly"
                                       :status="testCase.status"/>
    <el-button class="save-btn" type="primary" size="mini" :disabled="isReadOnly" @click="saveCase()">
      {{$t('test_track.save')}}
    </el-button>

    <test-plan-comment-input
      :data="testCase"
      ref="comment"/>
  </el-card>
</template>

<script>
import TestPlanTestCaseStatusButton from "@/business/components/track/plan/common/TestPlanTestCaseStatusButton";
import {hasPermission} from "@/common/js/utils";
import TestPlanCommentInput from "@/business/components/track/plan/view/comonents/functional/TestPlanCommentInput";
export default {
  name: "TestPlanFunctionalExecute",
  components: {TestPlanCommentInput, TestPlanTestCaseStatusButton},
  props: {
    isReadOnly: Boolean,
    originStatus: String,
    testCase: {
      type: Object,
      default() {
        return {}
      }
    }
  },
  computed: {
    statusReadOnly() {
      return !hasPermission('PROJECT_TRACK_PLAN:READ+RUN');
    },
  },
  methods: {
    statusChange(status) {
      this.testCase.status = status;
    },
    saveCase() {
      this.$emit('saveCase');
    }
  }
}
</script>

<style scoped>
.status-button >>> .el-col {
  padding-right: 0px !important;
}

.status-button {
  float: left;
}

.save-btn {
  float: right;
  margin-top: 10px;
}

</style>
