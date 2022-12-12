<template>
  <div class="view-item">
    <div class="comment-detail-row">
      <div class="header-img-row">{{ getShortName(comment.authorName) }}</div>
      <div class="info">
        <div class="username">{{ comment.authorName }}</div>
        <div class="fiexed">{{$t('case.commented')}}</div>
        <div class="time">{{ comment.createTime | datetimeFormat }}</div>

        <template v-if="!readOnly">
          <div class="split">|</div>
          <div class="edit opt-row" @click="openEdit">
            <div class="icon">
              <i class="el-icon-edit"></i>
            </div>
            <div class="label">{{$t('commons.edit')}}</div>
          </div>
          <div class="remove opt-row" @click="deleteComment">
            <div class="icon">
              <i class="el-icon-delete"></i>
            </div>
            <div class="label">{{$t('commons.delete')}}</div>
          </div>
        </template>
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
import { getCurrentUser } from "metersphere-frontend/src/utils/token";
import { deleteMarkDownImgByName } from "@/business/utils/sdk-utils";
export default {
  name: "CaseCommentViewItem",
  components: {
    CaseCommentEdit,
  },
  props: {
    comment: Object,
    readOnly: {
      type: Boolean,
      default: false,
    },
    apiUrl: String,
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
      return name.substring(0, 2).toUpperCase();
    },
    openEdit() {
      this.state = "EDIT";
    },
    cancel(data) {
      this.formData.richText = data;
      this.state = "PREVIEW";
    },
    submit(data) {
      this.editComment();
    },
    deleteComment() {
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
    editComment() {
      this.$post(this.apiUrl + "/comment/edit", {
        id: this.comment.id,
        description: this.comment.description,
      }).then(() => {
        this.originDesc = this.comment.description;
        this.visible = false;
        this.$success(this.$t("commons.modify_success"), false);
        this.$emit("refresh");
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

    .edit {
    }

    .remove {
    }
    .opt-row {
      margin-left: 21.33px;
      display: flex;
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
