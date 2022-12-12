<template>
  <div
    class="comment-box"
    :class="state === 'READY' ? 'small-comment-box' : 'normal-comment-box'"
  >
    <div class="narrow-wrap">
      <div class="header-img-row">
        <div class="header-img">{{ getCurrentShortName() }}</div>
      </div>
      <div class="input-row">
        <case-comment-edit
          :read-only="readOnly"
          @toEdit="toEdit"
          :state="state"
          @cancel="cancel"
          @submit="submit"
          :formData="formData"
        ></case-comment-edit>
      </div>
    </div>
  </div>
</template>
<script>
import CaseCommentEdit from "./CaseCommentEdit";
import { testCaseCommentAdd } from "@/api/test-case-comment";
export default {
  name: "CaseCommentComponent",
  components: {
    CaseCommentEdit,
  },
  props: {
    caseId: String,
    readOnly: Boolean
  },
  data() {
    return {
      state: "READY",
      formData: {
        richText: "",
        prop: "richText",
      },
      result: {},
    };
  },
  watch: {
    state() {
      this.$emit('stateChange', this.state);
    }
  },
  methods: {
    getCurrentShortName() {
      let token = localStorage.getItem("Admin-Token");
      if (!token) {
        return "";
      }
      let userObj = JSON.parse(token);
      let name = userObj.name;
      return name.substring(0, 2).toUpperCase();
    },
    toEdit() {
      this.state = "EDIT";
    },
    cancel(data) {
      this.formData.richText = data;
      this.state = "READY";
    },
    submit(data) {
      //提交评论
      let comment = {};
      this.formData.richText = data;
      comment.caseId = this.caseId;
      comment.description = this.formData.richText;
      if (!comment.description) {
        this.$warning(this.$t("test_track.comment.description_is_null"), false);
        return;
      }
      this.result.loading = true;
      testCaseCommentAdd(comment).then(() => {
        this.result.loading = false;
        this.$success(this.$t("test_track.comment.send_success"), false);
        this.refresh(comment.caseId);
        this.formData.richText = "";
      });
    },
    refresh(id) {
      this.$emit("getComments");
    },
  },
};
</script>
<style scoped lang="scss">
.small-comment-box {
  height: 80px;
  background: #ffffff;
  box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
}
.normal-comment-box {
  height: 240px;
  background: #ffffff;
  box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
}
.comment-box {
  overflow: hidden;
  width: 100%;
  .narrow-wrap {
    display: flex;
    width: 100%;
    height: 100%;
    justify-content: flex-start;
    overflow: hidden;
    .header-img-row {
      margin-top: 20px;
      margin-left: 24px;
      margin-right: 12px;
      color: #fff;
      width: 40px;
      background-color: #783887;
      height: 40px;
      line-height: 40px;
      text-align: center;
      border-radius: 40px;
      background-blend-mode: normal;
      font-size: 14px;
      .header-img {
      }
    }

    .input-row {
      width: 100%;
      height: 100%;
      margin-top: 20px;
      margin-right: 24px;
    }
  }
}
</style>
