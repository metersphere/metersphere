<template>
  <el-card class="executeCard">
    <div class="status-bnt">
      <el-button type="success" size="mini"
                 :disabled="isReadOnly" :icon="testCase.reviewStatus === 'Pass' ? 'el-icon-check' : ''"
                 @click="changeStatus('Pass')">
        {{ $t('test_track.review.pass') }}
      </el-button>
      <el-button type="danger" size="mini"
                 :disabled="isReadOnly"
                 :icon="testCase.reviewStatus === 'UnPass' ? 'el-icon-check' : ''"
                 @click="changeStatus('UnPass')">
        {{ $t('test_track.review.un_pass') }}
      </el-button>
    </div>

    <el-button class="save-btn" type="primary" size="mini" :disabled="isReadOnly" @click="saveCase()">
      {{$t('test_track.save')}}
    </el-button>

    <test-plan-comment-input
      :data="testCase"
      ref="comment"/>
  </el-card>
</template>

<script>
import TestPlanTestCaseStatusButton from "@/business/plan/common/TestPlanTestCaseStatusButton";
import TestPlanCommentInput from "@/business/plan/view/comonents/functional/TestPlanCommentInput";
export default {
  name: "TestReviewTestCaseExecute",
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
  methods: {
    saveCase() {
      // 从其他状态改成失败才需要写评论
      if (this.testCase.reviewStatus === 'UnPass' && this.originStatus !== 'UnPass' && !this.testCase.comment) {
        this.$refs.comment.inputLight();
      } else {
        this.$emit('saveCase');
      }
    },
    changeStatus(status) {
      this.testCase.reviewStatus = status;
    }
  }
}
</script>

<style scoped>

.status-bnt {
  float: left;
}


.save-btn {
  float: right;
  margin-top: 10px;
}

</style>
