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
         @click="handleNext">
      <span>
       {{ $t('case.next_public_case') }}
      </span>
      <i class="el-icon-arrow-right"/>
    </div>

    <div class="bar-item click-item"
         @click="handlePre">
      <i class="el-icon-arrow-left"/>
      <span>
        {{ $t("case.previous_public_case") }}
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
import {getCurrentProjectID, getCurrentUser, parseMdImage, saveMarkDownImg} from "@/business/utils/sdk-utils";
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
  provide() {
    return {
      enableTempUpload: true
    }
  },
  computed: {
    countNum() {
      return this.pageSize * (this.pageNum - 1) + this.index + 1;
    }
  },
  methods: {
    handlePre() {
      if (this.countNum === 1) {
        this.$error(this.$t('commons.already_pre_page'));
        return;
      }
      this.$emit('pre');
    },
    handleNext() {
      if (this.countNum >= this.total) {
        this.$error(this.$t('commons.already_next_page'));
        return;
      }
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
            this.$emit('refreshTestCaseStatus', r.data.status);

            comment.id = r.data.commentId;
            this.handleMdImages(comment);
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
          .then((response) => {
            this.$success(this.$t('test_track.comment.send_success'));
            this.$emit('refreshComment');
            this.$refs.commentEdit.close();
            comment.id = response.data.id;
            this.handleMdImages(comment);
          });
      }
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
    addCommentOpen(status) {
      this.status = status;
      this.$refs.commentEdit.open();
    },
    uhPassCommentOpen() {
      this.isCommentRequire = true;
      this.commentTitle = this.$t('review.un_pass_review_confirm');
      this.commentTip = this.$t('review.please_input_review_comment') + this.$t('review.comment_require');
      this.addCommentOpen('UnPass');
    },
    passCommentOpen() {
      this.isCommentRequire = false;
      this.commentTitle = this.$t('review.pass_review_confirm');
      this.commentTip = this.$t('review.please_input_review_comment') + this.$t('review.comment_not_require');
      this.addCommentOpen('Pass');
    },
    commentOpen() {
      this.isCommentRequire = true;
      this.commentTitle = this.$t('case.comment');
      this.commentTip = this.$t('commons.input_content') + this.$t('review.comment_not_require');
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
