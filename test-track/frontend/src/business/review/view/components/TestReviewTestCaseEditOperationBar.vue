<template>
  <div class="bar-container">

    <!--  暂时注释  -->
    <!--        <div class="bar-item">-->
    <!--          <img-->
    <!--            src="/assets/module/figma/icon_visible_outlined.svg"/>-->
    <!--          <span>-->
    <!--              流程预览-->
    <!--            </span>-->
    <!--        </div>-->

    <div class="bar-item button-item" @click="passCommentOpen">
      <el-button type="success" size="mini">{{ $t('test_track.review.pass') }}</el-button>
    </div>

    <div class="bar-item button-item un-pass-btn" @click="uhPassCommentOpen">
      <el-button type="danger" plain size="mini">{{ $t('test_track.review.un_pass') }}</el-button>
    </div>

    <div class="bar-item click-item"
         @click="commentOpen">
      <img class="icon-item"
           src="/assets/module/figma/icon_add-comment_outlined.svg"/>
      <span>
        {{ $t('case.comment') }}
      </span>
    </div>

    <div class="bar-item">
      <el-divider direction="vertical"/>
    </div>

    <div class="bar-item click-item"
         :disabled="countNum !== total || countNum >= total"
         @click="handleNext">
      <span>
        {{ $t('commons.next_page') }}
      </span>
      <i class="el-icon-arrow-right"/>
    </div>

    <div class="bar-item click-item"
         :disabled="countNum === total || countNum <= 1"
         @click="handlePre">
      <i class="el-icon-arrow-left"/>
      <span>
        {{ $t('commons.pre_page') }}
      </span>
    </div>

    <comment-edit
      :title="commentTitle"
      :tip="commentTip"
      :is-comment-require="isCommentRequire"
      @addComment="addComment"
      ref="commentEdit"/>

  </div>

</template>

<script>

import CommentEdit from "@/business/review/view/components/commnet/CommentEdit";
import {testCaseCommentAdd} from "@/api/test-case-comment";
import {editTestCaseReviewStatus, editTestReviewTestCase} from "@/api/test-review";
import {getCurrentUser} from "@/business/utils/sdk-utils";
export default {
  name: "TestReviewTestCaseEditOperationBar",
  components: {CommentEdit},
  data() {
    return {
      status: null,
      commentTitle: '',
      commentTip: '',
      isCommentRequire: true
    };
  },
  props: {
    list: {
      type: Array,
      default() {
        return []
      }
    },
    index: {
      type: Number,
      default() {
        return 0
      }
    },
    pageTotal: {
      type: Number,
      default() {
        return 0
      }
    },
    total: {
      type: Number,
      default() {
        return 0
      }
    },
    pageNum: {
      type: Number,
      default() {
        return 0
      }
    },
    pageSize: {
      type: Number,
      default() {
        return 0
      }
    },
    nextPageData: Object,
    prePageData: Object,
    testCase: Object
  },

  computed: {
    countNum() {
      return this.pageSize * (this.pageNum - 1) + this.index + 1;
    }
  },
  methods: {
    handlePre() {
      this.$emit('pre');
    },
    handleNext() {
      this.$emit('next');
    },
    addComment(comment) {
      if (this.status) {
        let param = {};
        param.id = this.testCase.id;
        param.caseId = this.testCase.caseId;
        param.reviewId = this.testCase.reviewId;
        param.comment = comment.description;
        param.status = this.status;
        editTestReviewTestCase(param)
          .then((r) => {
            this.$success(this.$t('commons.save_success'));
            this.$refs.commentEdit.close();
            editTestCaseReviewStatus(this.testCase.reviewId);

            // 修改当前用例在整个用例列表的状态
            this.$emit('refreshTestCaseStatus', r.data);
          })
          .catch(() => {
            this.$refs.commentEdit.close();
          });
      } else {
        comment.caseId = this.testCase.caseId;
        comment.type = 'REVIEW';
        comment.belongId = this.testCase.reviewId;
        comment.author = getCurrentUser().id;
        testCaseCommentAdd(comment)
          .then(() => {
            this.$success(this.$t('test_track.comment.send_success'));
            this.$emit('refreshComment');
            this.$refs.commentEdit.close();
          });
      }
    },
    addCommentOpen(status) {
      this.status = status;
      this.$refs.commentEdit.open();
    },
    uhPassCommentOpen() {
      this.isCommentRequire = true;
      this.commentTitle = this.$t('review.un_pass_review_confirm');
      this.commentTip = this.$t('review.please_input_review_comment');
      this.addCommentOpen('UnPass');
    },
    passCommentOpen() {
      this.isCommentRequire = false;
      this.commentTitle = this.$t('review.pass_review_confirm');
      this.commentTip = this.$t('review.please_input_review_comment');
      this.addCommentOpen('Pass');
    },
    commentOpen() {
      this.isCommentRequire = true;
      this.commentTitle = this.$t('case.comment');
      this.commentTip = this.$t('commons.input_content');
      this.addCommentOpen();
    },
  }
}
</script>

<style scoped>

.bar-container {
  height: 56px;
  width: 100%;
  box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
  background: #FFFFFF;
}

.bar-item {
  float: right;
  display: inline-block;
  line-height: 56px;
  margin-right: 24px;
}

.button-item .el-button {
  width: 80px;
  height: 32px;
}

.icon-item {
  width: 14.67px;
  height: 13px;
}

.un-pass-btn {
  margin-right: 15px;
}

.el-divider {
  width: 1px;
  height: 20px;

  background: #BBBFC4;

  flex: none;
  order: 1;
  flex-grow: 0;
}

.click-item {
  cursor: pointer;
}
</style>
