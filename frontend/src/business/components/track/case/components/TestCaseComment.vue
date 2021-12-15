<template>
  <el-dialog :visible.sync="dialogTableVisible">

    <div v-loading="result.loading">
      <div class="editors_div_style">
        <div id="editorsDiv">
          <ms-mark-down-text prop="description" :data="from" :toolbars="toolbars"/>
        </div>
      </div>
      <div>
        <el-button type="primary" size="mini" class="send-btn" @click="sendComment" :disabled="isReadOnly">
          {{ $t('test_track.comment.send') }}
        </el-button>
      </div>
    </div>

  </el-dialog>
</template>

<script>
import ReviewCommentItem from "@/business/components/track/review/commom/ReviewCommentItem";
import {uploadMarkDownImg} from "@/network/image";
import MsMarkDownText from "@/business/components/track/case/components/MsMarkDownText";

export default {
  name: "TestCaseComment",
  components: {MsMarkDownText, ReviewCommentItem},
  props: {
    caseId: String,
    reviewId: String,
  },
  data() {
    return {
      result: {},
      from: {
        description: ''
      },
      isReadOnly: false,
      dialogTableVisible: false,
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
  methods: {
    open() {
      this.dialogTableVisible = true;
    },
    sendComment() {
      let comment = {};
      comment.caseId = this.caseId;
      comment.description = this.from.description;
      if (!comment.description) {
        this.$warning(this.$t('test_track.comment.description_is_null'));
        return;
      }
      this.result = this.$post('/test/case/comment/save', comment, () => {
        this.$success(this.$t('test_track.comment.send_success'));
        this.refresh(comment.caseId);
        this.from.description = '';
        this.dialogTableVisible = false;
      });
    },
    refresh(id) {
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
  height: calc(100vh - 250px);
}

.editors_div_style {
  height: 300px;
  overflow: auto
}
</style>
