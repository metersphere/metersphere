<template>
  <div>
    <div class="main">
      <review-comment-item/>
    </div>
    <div>
      <el-input
        type="textarea"
        placeholder="发表评论"
        v-model="textarea"
        maxlength="60"
        show-word-limit
        resize="none"
        :autosize="{ minRows: 4, maxRows: 4}"
      >
      </el-input>
      <el-button type="primary" size="mini" class="send-btn">发送</el-button>
    </div>
  </div>
</template>

<script>
import ReviewCommentItem from "./ReviewCommentItem";

export default {
  name: "ReviewComment",
  components: {ReviewCommentItem},
  data() {
    return {
      commentData: [],
      textarea: ''
    }
  }
}
</script>

<style scoped>
  .main {
    height: 60vh;
  }
  .send-btn {
    float: right;
    margin-top: 5px;
  }
</style>
