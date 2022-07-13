<template>
  <el-card>
    <template slot="header">
      <span style="font-size: 15px; color: #1E90FF">{{ $t('test_track.review.comment') }}</span>
      <i class="el-icon-refresh" @click="getComments()"/>
      <el-select
        class="type-select"
        v-model="type"
        size="small">
        <el-option
          v-for="item in commentTypeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value">
        </el-option>
      </el-select>
    </template>
    <div v-loading="result.loading">
      <div class="comment-list">
        <review-comment-item v-for="(comment) in comments"
                             api-url="/test/case"
                             :key="comment.id"
                             :comment="comment"
                             @refresh="refresh"
                             ref="reviewComments"/>
        <div v-if="comments.length === 0" style="text-align: center">
          <i class="el-icon-chat-line-square" style="font-size: 15px;color: #8a8b8d;">
        <span style="font-size: 15px; color: #8a8b8d;">
          {{ $t('test_track.comment.no_comment') }}
        </span>
          </i>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script>
import ReviewCommentItem from "./ReviewCommentItem";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import MsMarkDownText from "@/business/components/track/case/components/MsMarkDownText";

export default {
  name: "ReviewComment",
  components: {MsMarkDownText, ReviewCommentItem, FormRichTextItem},
  props: {
    caseId: String,
    defaultType: String
  },
  data() {
    return {
      result: {},
      loadCommenItem: true,
      labelWidth: '120px',
      showEditor: true,
      isReadOnly: false,
      comments: [],
      type: '',
      commentTypeOptions: [
        {
          value: 'CASE',
          label: '用例评论'
        },
        {
          value: 'PLAN',
          label: '执行评论'
        },
        {
          value: 'REVIEW',
          label: '评审评论'
        },
        {
          value: null,
          label: '全部评论'
        }
      ],
    };
  },
  created() {
    this.isReadOnly = false;
    this.type = this.defaultType;
  },
  watch: {
    caseId() {
      this.type = this.defaultType;
      this.getComments();
    },
    type() {
      this.getComments();
    }
  },
  methods: {
    getComments() {
      if (this.caseId) {
        let type = this.type ? this.type : '';
        this.result = this.$get('/test/case/comment/list/' + this.caseId + '/' + type , res => {
          this.comments = res.data;
        });
      }
    },
    refresh() {
      this.getComments();
    }
  }
};
</script>

<style scoped>

.comment-list {
  overflow-y: scroll;
  height: calc(100vh - 340px);
}

/deep/ .v-note-wrapper {
  position: initial;
}

.el-icon-refresh {
  margin-left: 10px;
  font-size: 14px;
  cursor: pointer
}

.type-select {
  float: right;
  width: 120px;
}
</style>
