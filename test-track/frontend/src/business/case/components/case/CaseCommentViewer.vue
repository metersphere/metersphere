<template>
  <div class="comment-view-box">
    <div
      class="view-item"
      comment-view-box
      v-for="(comment, index) in comments"
      :key="index"
    >
      <case-comment-view-item
        :comment="comment"
        api-url="/test/case"
        @refresh="getComments"
        :readOnly="readOnly"
      ></case-comment-view-item>
    </div>
  </div>
</template>
<script>
import CaseCommentViewItem from "./CaseCommentViewItem";
export default {
  name: "CaseCommentViewer",
  props: {
    comments: Array,
    readOnly: {
      type: Boolean,
      default: false,
    },
  },
  components: {
    CaseCommentViewItem,
  },
  methods: {
    getComments(testCase) {
      this.$emit("getComments", testCase);
    },
  },
};
</script>
<style scoped lang="scss">
.comment-view-box {
  padding-bottom: 30px;
  .view-item {
    display: flex;
    flex-direction: column;
    margin-top: 24px;
  }
}
</style>
