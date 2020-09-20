<template>
  <div class="main">
    <div class="comment-left">
      <div class="icon-title">
        {{comment.author.substring(0,1)}}
      </div>
    </div>
    <div class="comment-right">
      <span style="font-size: 16px;">{{comment.author}}</span>
      <span style="color: #8a8b8d; margin-left: 8px; font-size: 12px">
        {{comment.createTime | timestampFormatDate}}
      </span>
      <br/>
      <div class="comment-desc">{{comment.description}}</div>
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
    overflow-y: scroll;
    display: flex;
    align-items: center;
    margin-top: 10px;
  }

  .comment-left {
    float: left;
    width: 50px;
    height: 100%;
  }

  .comment-right {
    float: left;
    width: 90%;
    padding: 0;
    line-height: 25px;
  }

  .icon-title {
    color: #fff;
    width: 30px;
    background-color: #72dc91;
    height: 30px;
    line-height: 30px;
    text-align: center;
    border-radius: 30px;
  }

  .comment-desc {
    overflow-wrap: break-word;
    word-break: break-all;
    border-bottom: 1px solid #ced3de;
  }
</style>
