<template>
  <div class="view-item">
    <div class="comment-detail-row">
      <div class="header-img-row">{{ getShortName(comment.authorName) }}</div>
      <div class="info">
        <div class="username">{{ comment.authorName }}</div>
        <div class="fiexed">{{ $t("case.commented") }}</div>
        <div class="time">{{ comment.createTime | datetimeFormat }}</div>

        <template v-if="!readOnly">
          <div class="split">|</div>
          <div class="edit opt-row" @click="openEdit">
            <div class="icon">
              <i class="el-icon-edit"></i>
            </div>
            <div class="label">{{ $t("commons.edit") }}</div>
          </div>
          <div class="remove opt-row" @click="deleteComment">
            <div class="icon">
              <i class="el-icon-delete"></i>
            </div>
            <div class="label">{{ $t("commons.delete") }}</div>
          </div>
        </template>
        <div
          class="status"
          v-if="comment.diffStatus > 0"
          style="margin-left: 5px"
        >
          <case-diff-status :diffStatus="comment.diffStatus"></case-diff-status>
        </div>
      </div>
    </div>
    <div class="viewer">
      <case-comment-edit
        :state="state"
        :formData="formData"
        @cancel="cancel"
        @submit="submit"
      ></case-comment-edit>
    </div>
  </div>
</template>
<script>
import CaseCommentEdit from "./CaseCommentEdit";
import {getCurrentProjectID, getCurrentUser} from "metersphere-frontend/src/utils/token";
import CaseDiffStatus from "./diff/CaseDiffStatus";
import {parseMdImage, saveMarkDownImg} from "@/business/utils/sdk-utils";
export default {
  name: "CaseCommentViewItem",
  components: {
    CaseCommentEdit,
    CaseDiffStatus,
  },
  props: {
    comment: Object,
    readOnly: {
      type: Boolean,
      default: false,
    },
    apiUrl: String,
    isPublicShow: Boolean
  },
  watch: {
    "comment.description": {
      immediate: true,
      handler(v) {
        this.formData.richText = v || "";
      },
    },
  },
  data() {
    return {
      formData: { prop: "richText", richText: "" },
      state: "PREVIEW",
    };
  },
  methods: {
    getComments(testCase) {
      this.$emit("getComments", testCase);
    },
    getShortName(name) {
      name = name || "";
      return name.substring(0, 1).toUpperCase();
    },
    openEdit() {
      if (this.isPublicShow) {
        return;
      }
      if (getCurrentUser().id !== this.comment.author) {
        this.$warning(this.$t("test_track.comment.cannot_edit"), false);
        return;
      }
      this.state = "EDIT";
    },
    cancel(data) {
      this.formData.richText = data;
      this.state = "PREVIEW";
    },
    submit(description) {
      this.editComment(description);
    },
    deleteComment() {
      if (this.isPublicShow) {
        return;
      }
      if (getCurrentUser().id !== this.comment.author) {
        this.$warning(this.$t("test_track.comment.cannot_delete"), false);
        return;
      }
      //   if (this.imgNameList.length > 0) {
      //     this.imgNameList.forEach((imgName) => {
      //       deleteMarkDownImgByName(imgName);
      //     });
      //   }
      this.$get(this.apiUrl + "/comment/delete/" + this.comment.id).then(() => {
        this.$success(this.$t("commons.delete_success"), false);
        this.$emit("refresh");
      });
    },
    editComment(description) {
      let param = {
        id: this.comment.id,
        description,
      };
      this.$post(this.apiUrl + "/comment/edit", param).then(() => {
        this.originDesc = this.comment.description;
        this.visible = false;
        this.$success(this.$t("commons.modify_success"), false);
        this.state = "PREVIEW";
        this.$emit("refresh");
        this.handleMdImages(param);
      });
    },
    handleMdImages(param) {
      // 解析富文本框中的图片
      let mdImages = [];
      mdImages.push(...parseMdImage(param.description));
      // 将图片从临时目录移入正式目录
      saveMarkDownImg({
        projectId: getCurrentProjectID(),
        resourceId: param.id,
        fileNames: mdImages
      });
    },
  },
};
</script>
<style scoped lang="scss">
.view-item {
  display: flex;
  flex-direction: column;
  margin-top: 24px;
}
.comment-detail-row {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  .header-img-row {
    width: 32px;
    height: 32px;
    line-height: 32px;
    font-size: 12px;
    background: #783887;
    border-radius: 50%;
    text-align: center;
    color: #ffffff;
  }
  .info {
    display: flex;
    .username {
      margin-left: 8px;
      color: #1f2329;
    }

    .fiexed {
      margin-left: 8px;
      color: #1f2329;
    }

    .time {
      margin-left: 8px;
      color: #646a73;
    }

    .split {
      margin-left: 20px;
      color: #bbbfc4;
    }

    .edit:hover .icon{
      color: #783887;
    }

    .edit:hover .label{
      color: #783887;
    }

    .remove:hover .icon{
      color: #783887;
    }

    .remove:hover .label{
      color: #783887;
    }

    .opt-row {
      margin-left: 21.33px;
      display: flex;
      cursor: pointer;
      .icon {
        color: #646a73;
        width: 13.53px;
        height: 13.53px;
      }
      .label {
        letter-spacing: -0.1px;
        color: #646a73;
        margin-left: 5.33px;
      }
    }
  }
}
.viewer {
  margin-top: 9px;
  margin-left: 40px;
  //   background: #f5f6f7;
  border-radius: 4px;
}
</style>
