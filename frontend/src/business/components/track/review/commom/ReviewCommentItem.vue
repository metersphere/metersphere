<template>
  <div class="main">
    <div class="comment-left">
      <span>
        <i class="el-icon-user-solid review-comment-user"/>
      </span>
    </div>
    <div class="comment-right">
      <span style="font-size: 16px;">{{comment.author}}</span>
      <span style="color: #8a8b8d; margin-left: 8px; font-size: 12px">
        {{comment.createTime | timestampFormatDate}}
      </span>
      <br/>
      <div style="word-wrap:break-word; word-break:break-all;">{{comment.description}}</div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ReviewCommentItem",
  props: {
    comment: Object
  },
  data() {
    return {}
  }
}
</script>

<style scoped>
  .main {
    overflow-y: scroll
  }

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
    padding: 0;
    line-height: 25px;
  }
</style>
