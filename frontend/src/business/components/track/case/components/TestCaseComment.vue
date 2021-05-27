<template>
  <el-dialog :visible.sync="dialogTableVisible">

    <div v-loading="result.loading">

      <div>
        <el-input
          type="textarea"
          :placeholder="$t('test_track.comment.send_comment')"
          v-model="textarea"
          maxlength="60"
          show-word-limit
          resize="none"
          :autosize="{ minRows: 4, maxRows: 4}"
          @keyup.ctrl.enter.native="sendComment"
          :disabled="isReadOnly"
        >
        </el-input>
        <el-button type="primary" size="mini" class="send-btn" @click="sendComment" :disabled="isReadOnly">
          {{ $t('test_track.comment.send') }}
        </el-button>
      </div>
    </div>

  </el-dialog>
</template>

<script>
import ReviewCommentItem from "@/business/components/track/review/commom/ReviewCommentItem";
import {getUUID} from "@/common/js/utils";

export default {
  name: "TestCaseComment",
  components: {ReviewCommentItem},
  props: {
    caseId: String,
    reviewId: String,
  },
  data() {
    return {
      result: {},
      textarea: '',
      isReadOnly: false,
      dialogTableVisible: false
    }
  },
  created() {

  },
  methods: {
    open() {
      this.dialogTableVisible = true
    },
    sendComment() {
      let comment = {};
      comment.caseId = this.caseId;
      comment.description = this.textarea;
      if (!this.textarea) {
        this.$warning(this.$t('test_track.comment.description_is_null'));
        return;
      }
      this.result = this.$post('/test/case/comment/save', comment, () => {
        this.$success(this.$t('test_track.comment.send_success'));
        this.refresh(comment.caseId);
        this.textarea = '';
        this.dialogTableVisible = false
      });
    },
    refresh(id) {
      this.$emit('getComments');
    },
  }
}
</script>

<style scoped>
.send-btn {
  margin-top: 5px;
  width: 100%;
}

.comment-list {
  overflow-y: scroll;
  height: calc(100vh - 250px);
}
</style>
