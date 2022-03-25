<template>
  <div v-loading="result.loading">
    <div class="comment-list">
      <review-comment-item v-for="(comment) in comments" v-bind:key="comment.id"
                           ref="reviewComments"
                           :comment="comment"
                           @refresh="refresh"
                           :review-status="reviewStatus" api-url="/test/case"/>
      <div v-if="comments.length === 0" style="text-align: center">
        <i class="el-icon-chat-line-square" style="font-size: 15px;color: #8a8b8d;">
        <span style="font-size: 15px; color: #8a8b8d;">
          {{ $t('test_track.comment.no_comment') }}
        </span>
        </i>
      </div>
    </div>
    <div>
      <div class="editors_div_style">
        <div id="editorsDiv">
          <ms-mark-down-text prop="description" :data="form" :toolbars="toolbars" ref="md"/>
        </div>
      </div>

      <el-button type="primary" size="mini" class="send-btn"
                 v-permission="['PROJECT_TRACK_REVIEW:READ+COMMENT']"
                 @click="sendComment" :disabled="isReadOnly">
        {{ $t('test_track.comment.send') }}
      </el-button>
    </div>
  </div>
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
    comments: Array,
    reviewId: String,
    reviewStatus: String,
    oldReviewStatus: String,
  },
  data() {
    return {
      result: {},
      form: {
        description: ''
      },
      loadCommenItem: true,
      labelWidth: '120px',
      showEditor: true,
      isReadOnly: false,
      toolbars: {
        bold: false, // 粗体
        italic: false, // 斜体
        header: false, // 标题
        underline: false, // 下划线
        strikethrough: false, // 中划线
        mark: false, // 标记
        superscript: false, // 上角标
        subscript: false, // 下角标
        quote: false, // 引用
        ol: false, // 有序列表
        ul: false, // 无序列表
        link: false, // 链接
        imagelink: true, // 图片链接
        code: false, // code
        table: false, // 表格
        fullscreen: false, // 全屏编辑
        readmodel: false, // 沉浸式阅读
        htmlcode: false, // 展示html源码
        help: false, // 帮助
        /* 1.3.5 */
        undo: false, // 上一步
        redo: false, // 下一步
        trash: false, // 清空
        save: false, // 保存（触发events中的save事件）
        /* 1.4.2 */
        navigation: false, // 导航目录
        /* 2.1.8 */
        alignleft: false, // 左对齐
        aligncenter: false, // 居中
        alignright: false, // 右对齐
        /* 2.2.1 */
        subfield: false, // 单双栏模式
        preview: false, // 预览
      }
    };
  },
  created() {
    this.isReadOnly = false;
  },
  watch: {
    comments() {
    }
  },
  methods: {
    sendComment() {
      let comment = {};
      comment.caseId = this.caseId;
      comment.description = this.form.description;
      comment.reviewId = this.reviewId;
      comment.status = this.reviewStatus;
      if (!comment.description) {
        this.$warning(this.$t('test_track.comment.description_is_null'));
        return;
      }
      this.result = this.$post('/test/case/review/comment/save', comment, () => {
        this.$success(this.$t('test_track.comment.send_success'));
        this.form.description = "";
        this.refresh();
        if (this.$refs.md) {
          this.$refs.md.toolbar_left_click('trash');
        }
        if ((this.oldReviewStatus === 'Prepare' || this.oldReviewStatus === 'Pass') && this.reviewStatus === 'UnPass') {
          this.$emit('saveCaseReview');
        }
      });
    },
    inputLight() {
      let textAreaDom = this.$refs.md.getTextareaDom();
      let editorDivDom = document.getElementById("editorsDiv");
      if (editorDivDom) {
        editorDivDom.setAttribute("style", "-webkit-box-shadow: 0 0 8px rgb(205,51,43);");
      }
      textAreaDom.focus();
    },
    resetInputLight() {
      let editorDivDom = document.getElementById("editorsDiv");
      if (editorDivDom) {
        editorDivDom.setAttribute("style", "-webkit-box-shadow: 0 0 0px rgb(-1,0,0);");
      }
    },
    refresh() {
      this.resetInputLight();
      this.$emit('getComments');
    }
  }
};
</script>

<style scoped>
.send-btn {
  margin-top: 5px;
  width: 100%;
}

.comment-list {
  overflow-y: scroll;
  height: calc(100vh - 450px);
}

.editors-div {
  -webkit-box-shadow: 0 0 8px rgb(-1, 0, 0);
}

.editors_div_style {
  height: 300px;
  overflow: auto
}

.review-mavon-editor {
  min-height: 20px;
  height: 300px;
  overflow: auto
}

.review-mavon-editor {
  position: initial;
}
</style>
