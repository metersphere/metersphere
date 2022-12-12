<template>
  <el-dialog v-loading="loading"
             :visible.sync="visible"
             :title="title"
             :destroy-on-close="true"
             :close-on-click-modal="false"
             :show-close="false"
             append-to-body>

    <div>
      <comment-edit-input :placeholder="tip" :data="comment" ref="commentEditInput"/>

      <div class="operate-btn">
        <el-button size="mini" @click="close">
          {{ $t('commons.cancel') }}
        </el-button>
        <el-button type="primary" size="mini" @click="editComment">
          {{ $t('test_track.comment.send') }}
        </el-button>
      </div>
    </div>

  </el-dialog>
</template>

<script>

import {getCurrentUser} from "@/business/utils/sdk-utils";
import CommentEditInput from "@/business/review/view/components/commnet/CommentEditInput";

export default {
  name: "CommentEdit",
  components: {CommentEditInput},
  data() {
    return {
      visible: false,
      loading: false,
      comment: {},
      isEdit: true
    };
  },
  props: {
    title: String,
    tip: String,
    status: String,
    isCommentRequire: {
      type: Boolean,
      default() {
        return true;
      }
    }
  },
  computed: {},
  methods: {
    editComment() {
      // 必填校验
      if (this.isCommentRequire && !this.comment.description) {
        this.$refs.commentEditInput.inputLight();
        return;
      }
      this.loading = true;
      if (this.isEdit) {
        this.$emit('editComment', this.comment);
      } else {
        this.$emit('addComment', this.comment);
      }
    },
    close() {
      this.loading = false;
      this.visible = false;
    },
    open(comment) {
      this.loading = false;
      if (comment) {
        this.isEdit = true;
        let copyComment = {};
        Object.assign(copyComment, comment);
        this.comment = copyComment;
      } else {
        this.isEdit = false;
        this.comment = {description: ''};
      }
      if (this.isEdit && getCurrentUser().id !== this.comment.author) {
        this.$warning(this.$t('test_track.comment.cannot_edit'));
        return;
      }
      this.visible = true;
    }
  }
}
</script>

<style scoped>
.operate-btn {
  text-align: right;
}

:deep(.el-dialog__body) {
  padding: 24px 0px 6px 0px;
}

:deep(.el-dialog__title) {
  font-weight: bold;
}

.operate-btn .el-button {
   width: 80px;
   height: 32px;
}
</style>
