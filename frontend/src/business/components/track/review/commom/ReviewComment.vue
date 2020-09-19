<template>
  <div v-loading="result.loading">
    <div style="height: 60vh;overflow-y: scroll">
      <review-comment-item v-for="(comment,index) in comments" :key="index" :comment="comment"/>
      <div v-if="comments.length === 0" style="text-align: center">
        <i class="el-icon-chat-line-square" style="font-size: 20px;color: #8a8b8d;">
        <span style="font-size: 20px; color: #8a8b8d;">
          暂无评论
        </span>
        </i>
      </div>
    </div>
    <div>
      <el-input
        type="textarea"
        placeholder="发表评论（Ctrl+Enter发送）"
        v-model="textarea"
        maxlength="60"
        show-word-limit
        resize="none"
        :autosize="{ minRows: 4, maxRows: 4}"
        @keyup.ctrl.enter.native="sendComment"
      >
      </el-input>
      <el-button type="primary" size="mini" class="send-btn" @click="sendComment">发送</el-button>
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
    comments: Array
  },
  data() {
    return {
      result: {},
      textarea: '',
    }
  },
  methods: {
    sendComment() {
      let comment = {};
      comment.caseId = this.caseId;
      comment.description = this.textarea;
      if (!this.textarea) {
        this.$warning("评论内容不能为空！");
        return;
      }
      this.$post('/test/case/comment/save', comment, () => {
        this.$success("评论成功！");
        this.$emit('getComments');
        this.textarea = '';
      });
    },

  }
}
</script>

<style scoped>
  .send-btn {
    float: right;
    margin-top: 5px;
  }
</style>
