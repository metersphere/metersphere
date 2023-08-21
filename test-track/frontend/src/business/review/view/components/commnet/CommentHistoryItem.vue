<template>
  <div class="main" v-loading="loading">
    <div class="comment-left">
      <ms-user-icon :user-name="comment.authorName"/>
    </div>
    <div class="comment-right">
      <div>
        <span style="font-size: 14px;color: #909399;font-weight: bold">{{ comment.authorName }}</span>
        <span style="color: #8a8b8d; margin-left: 8px; font-size: 12px">
          {{ comment.createTime | datetimeFormat }}
        </span>
        <el-divider direction="vertical"/>
          <span class="operate-item operate-edit-item"
                @click="openEdit"
                :disabled="readOnly">
            <i class="el-icon-edit"/>
            <span>
              {{ $t('commons.edit') }}
            </span>
          </span>
         <span v-if="!comment.status"
               class="operate-item"
               @click="deleteComment"
               :disabled="readOnly">
          <i class="el-icon-delete"/>
          <span>
            {{ $t('commons.delete') }}
          </span>
        </span>
      </div>

      <div>
        <span v-if="comment.status">
          <comment-status-text :value="comment.status"/>
        </span>
        <span v-else>
          {{ $t('review.added_comment') }}
        </span>
      </div>

      <div v-if="comment.description">
        <ms-mark-down-text
          class="comment-text"
          prop="description"
          :disabled="true"
          :data="comment"/>
      </div>

      <comment-edit :title="$t('case.comment')"
                    :tip="$t('commons.input_content')"
                    :is-comment-require="isCommentRequire"
                    @editComment="editComment"
                    ref="commentEdit"/>
    </div>

  </div>
</template>

<script>
import MsUserIcon from "metersphere-frontend/src/components/MsUserIcon";
import {getCurrentProjectID, getCurrentUser} from "metersphere-frontend/src/utils/token";
import MsMarkDownText from "metersphere-frontend/src/components/MsMarkDownText";
import {deleteMarkDownImgByName, parseMdImage, saveMarkDownImg} from "@/business/utils/sdk-utils";
import StatusTableItem from "@/business/common/tableItems/planview/StatusTableItem";
import CommentStatusText from "@/business/review/view/components/commnet/CommentStatusText";
import CommentEdit from "@/business/review/view/components/commnet/CommentEdit";

export default {
  name: "CommentHistoryItem",
  components: {CommentEdit, MsUserIcon, CommentStatusText, StatusTableItem, MsMarkDownText},
  props: {
    comment: Object,
    readOnly: {
      type: Boolean,
      default: false
    },
    apiUrl: String
  },
  data() {
    return {
      loading: false
    }
  },
  provide() {
    return {
      enableTempUpload: true
    }
  },
  computed: {
    isCommentRequire() {
      return !this.comment.status || this.comment.status === 'UnPass';
    }
  },
  methods: {
    deleteComment() {
      if (getCurrentUser().id !== this.comment.author) {
        this.$warning(this.$t('test_track.comment.cannot_delete'));
        return;
      }
      this.loading = true;

      this.getImgFormDesc().forEach(imgName => {
        deleteMarkDownImgByName(imgName);
      });

      this.$get(this.apiUrl + "/comment/delete/" + this.comment.id)
        .then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.loading = false;
          this.$emit("refresh");
        });
    },
    editComment(comment) {
      this.$post(this.apiUrl + "/comment/edit", comment)
        .then(() => {
          this.originDesc = this.comment.description;
          this.$refs.commentEdit.close();
          this.$success(this.$t('commons.modify_success'));
          this.comment.description = comment.description;
          this.handleMdImages(comment);
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
    openEdit() {
      this.$refs.commentEdit.open(this.comment);
    },
    getImgFormDesc() {
      let imgNames = [];
      let description = this.comment.description;
      if (!description) {
        return imgNames;
      }
      this.addImgNames(description, /resource\/md\/get\?fileName=(.*?)\)/g, "resource/md/get?fileName=", imgNames);
      this.addImgNames(description, /resource\/md\/get\/(.*?)\)/g, "resource/md/get/", imgNames);
      return imgNames;
    },
    addImgNames(str, pattern, prefix, imgNames) {
      let names = str.match(pattern);
      if (names) {
        let regStr = prefix;
        names.forEach(item => {
          imgNames.push(item.substring(regStr.length, item.length - 1));
        });
      }
    }
  }
}
</script>

<style scoped>
.main {
  overflow-y: scroll;
  display: flex;
  align-items: flex-start;
  margin-top: 10px;
}

.comment-left {
  float: left;
  width: 40px;
  height: 100%;
}

.comment-right {
  float: left;
  width: 90%;
  padding: 0;
  line-height: 25px;
}

.comment-desc {
  background: #F5F6F7;
  border-radius: 4px;
  padding: 8px 12px;
}

pre {
  margin: 0 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  width: 100%;
  line-height: 20px;
}

.operate-item {
  cursor: pointer;
  margin-left: 20px;
}

.operate-edit-item {
  margin-left: 10px;
}

:deep(.el-button--mini, .el-button--mini.is-round) {
  padding: 7px 15px;
}

.send-btn {
  margin-top: 5px;
  width: 100%;
}

.comment-text :deep(.v-note-wrapper) {
  padding: 8px 25px;
}

.comment-text :deep(.markdown-body p) {
  margin-bottom: 0px;
}

.el-divider {
  background: #BBBFC4;
}
</style>
