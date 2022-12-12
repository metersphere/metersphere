<template>
  <div class="bar-container" :class="{'empty-container': isCommentEmpty}">
    <div v-if="isCommentEmpty" class="padding-container">
      {{ $t('review.no_review_history') }}
    </div>
    <div v-else class="padding-container" v-loading="loading">
        <el-scrollbar>
        <span class="comment-title">
          {{ $t('review.review_history') }}
        </span>

          <div class="comment-list">
            <comment-history-item v-for="(comment) in comments"
                                  api-url="/test/case"
                                  :key="comment.id"
                                  :comment="comment"
                                  @refresh="getComments"
                                  ref="reviewComments"/>
            <div v-if="comments.length === 0" style="text-align: center">
              <i class="el-icon-chat-line-square" style="font-size: 15px;color: #8a8b8d;">
          <span style="font-size: 15px; color: #8a8b8d;">
            {{ $t('test_track.comment.no_comment') }}
          </span>
              </i>
            </div>
          </div>

        </el-scrollbar>
      </div>
    </div>
</template>

<script>

import {testCaseCommentListByTypeAndBelongId} from "@/api/test-case-comment";
import CommentHistoryItem from "@/business/review/view/components/commnet/CommentHistoryItem";

export default {
  name: "CommentHistory",
  components: {CommentHistoryItem},
  data() {
    return {
      comments: [],
      type: '',
      loading: false,
    };
  },
  props: {
    caseId: String,
    reviewId: String,
    defaultType: String
  },
  computed: {
    isCommentEmpty() {
      return !this.comments || this.comments.length == 0
    }
  },
  watch: {
    caseId() {
      this.type = this.defaultType;
      this.getComments();
    },
    type() {
      this.getComments();
    },
    comments() {
      this.$emit('emptyChange', this.isCommentEmpty);
    }
  },
  methods: {
    getComments() {
      if (this.caseId) {
        this.loading = true;
        let type = this.type || '';
        testCaseCommentListByTypeAndBelongId(this.caseId, type, this.reviewId)
          .then(res => {
            this.comments = res.data;
            this.loading = false;
          });
      }
    },
    clearComments() {
      this.comments = [];
    }
  }
}
</script>

<style scoped>

.bar-container {
  width: 100%;
  box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
  background: #FFFFFF;
  height: 241px;
  box-sizing: border-box;
}

.empty-container {
  height: 46px;
}

.empty-container .padding-container {
  color: #646A73;
}

.el-scrollbar {
  height: 100%;
}

.padding-container {
  padding: 12px 24px;
  height: 100%;
  box-sizing: border-box;
}

.comment-title {
  line-height: 22px;
  color: #1F2329;
  font-weight: bold;
}
</style>
