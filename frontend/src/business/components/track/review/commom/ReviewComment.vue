<template>
  <div v-loading="result.loading">
    <div class="comment-list">
      <review-comment-item v-for="(comment,index) in comments"
                           :key="index"
                           :comment="comment"
                           @refresh="refresh"
                           :review-status="reviewStatus"/>
      <div v-if="comments.length === 0" style="text-align: center">
        <i class="el-icon-chat-line-square" style="font-size: 15px;color: #8a8b8d;">
        <span style="font-size: 15px; color: #8a8b8d;">
          {{ $t('test_track.comment.no_comment') }}
        </span>
        </i>
      </div>
    </div>
    <div>
      <el-input
        v-permission="['PROJECT_TRACK_REVIEW:READ+COMMENT']"
        ref="test"
        type="textarea"
        :placeholder="$t('test_track.comment.send_comment')"
        v-model.trim="textarea"
        maxlength="180"
        show-word-limt
        resize="none"
        :autosize="{ minRows: 4, maxRows: 4}"
        @keyup.ctrl.enter.native="sendComment"
        :disabled="isReadOnly"
      >
      </el-input>
      <el-button type="primary" size="mini" class="send-btn"
                 v-permission="['PROJECT_TRACK_REVIEW:READ+COMMENT']"
                 @click="sendComment" :disabled="isReadOnly">
        {{ $t('test_track.comment.send') }}
      </el-button>
    </div>
  </div>
</template>

<script>
import ReviewCommentItem from "./ReviewCommentItem";

export default {
  name: "ReviewComment",
  components: {ReviewCommentItem},
  props: {
    caseId: String,
    comments: Array,
    reviewId: String,
    reviewStatus: String,
  },
  data() {
    return {
      result: {},
      textarea: '',
      isReadOnly: false
    };
  },
  created() {
    this.isReadOnly = false;
  },
  methods: {
    sendComment() {
      let comment = {};
      comment.caseId = this.caseId;
      comment.description = this.textarea;
      comment.reviewId = this.reviewId;
      if (!this.textarea) {
        this.$warning(this.$t('test_track.comment.description_is_null'));
        return;
      }
      this.result = this.$post('/test/case/comment/save', comment, () => {
        this.$success(this.$t('test_track.comment.send_success'));
        this.refresh();
        this.textarea = '';
      });
    },
    inputLight() {
      this.$refs.test.focus();
    },
    refresh() {
      this.$emit('getComments');
    },
  }
};
</script>

<style scoped>
.send-btn {
  margin-top: 5px;
  width: 100%;
}

.comment-list {
  overflow-y: scroll;
  height: calc(100vh - 250px);
}
</style>
