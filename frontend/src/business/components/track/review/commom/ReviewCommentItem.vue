<template>
  <div style="height: 60px;">
    <div class="comment-left">
      <span>
        <i class="el-icon-user-solid review-comment-user"/>
      </span>
    </div>
    <div class="comment-right">
      张三 <span style="color: #8a8b8d">9月8日</span><br/>
      <span>优先级为2</span>
    </div>
  </div>
</template>

<script>
export default {
  name: "ReviewCommentItem"
}
</script>

<style scoped>
  .review-comment-user {
    font-size: 19px;
  }
  .comment-left {
    float: left;
    width: 10%;
    height: 100%;
  }
  .comment-right {
    float: left;
    width: 90%;
    height: 100%;
    padding: 0;
    line-height: 25px;
  }
</style>
