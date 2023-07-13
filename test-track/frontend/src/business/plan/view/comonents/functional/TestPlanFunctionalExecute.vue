<template>
  <el-card class="executeCard">
    <test-plan-test-case-status-button class="status-button"
                                       @statusChange="statusChange"
                                       :is-read-only="statusReadOnly"
                                       :status="testCase.status"/>
    <test-plan-comment-input
      :data="testCase"
      ref="comment"/>

    <el-dropdown
      split-button
      type="primary"
      class="save-btn"
      @command="handleCommand"
      @click="saveCase"
      size="medium">
      {{ save }}
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item :command="command">{{ saveAndNext }}</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </el-card>
</template>

<script>
import TestPlanTestCaseStatusButton from "@/business/plan/common/TestPlanTestCaseStatusButton";
import {hasPermission} from "metersphere-frontend/src/utils/permission";
import TestPlanCommentInput from "@/business/plan/view/comonents/functional/TestPlanCommentInput";
export default {
  name: "TestPlanFunctionalExecute",
  components: {TestPlanCommentInput, TestPlanTestCaseStatusButton},
  data() {
    return {
      save: this.$t('test_track.save'),
      saveAndNext: this.$t('test_track.save_and_next'),
      command: 'save_and_next',
    }
  },
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
      this.$emit('saveCase',this.command);
    },
    handleCommand(e) {
      switch (e) {
        case 'save_and_next':
          this.save = this.$t('test_track.save_and_next');
          this.saveAndNext = this.$t('test_track.save');
          this.command = 'save';
          break;
        default:
          this.save = this.$t('test_track.save');
          this.saveAndNext = this.$t('test_track.save_and_next');
          this.command = 'save_and_next';
          break;
      }
    }
  }
}
</script>

<style scoped>
.status-button :deep(.el-col) {
  padding-right: 0px !important;
}

.status-button {
  float: left;
}

.save-btn {
  float: right;
  margin-top: 10px;
  margin-block: 10px;
  margin-right: 10px;
}

</style>
