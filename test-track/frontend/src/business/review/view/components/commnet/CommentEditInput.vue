<template>
  <el-form class="comment-form clearfix">
    <el-form-item>
      <ms-mark-down-text
        default-open="edit"
        custom-min-height="168"
        :prop="prop"
        :class="{inputWarning: inputWarning}"
        :data="data"
        :placeholder="placeholder"
        :toolbars="toolbars"
        ref="md"/>
    </el-form-item>
  </el-form>
</template>

<script>
import MsMarkDownText from "metersphere-frontend/src/components/MsMarkDownText";
export default {
  name: "CommentEditInput",
  components: {MsMarkDownText},
  props: {
    data: {
      type: Object,
      default() {
        return {
          description: ''
        }
      }
    },
    placeholder: String,
    prop: {
      type: String,
      default() {
        return 'description';
      }
    }
  },
  data() {
    return {
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
      },
      inputWarning: false
    };
  },
  methods: {
    inputLight() {
      this.inputWarning = true;
      this.$refs.md.getTextareaDom().focus();
    },
    clear() {
      this.inputWarning = false;
    }
  }
}
</script>

<style scoped>

.inputWarning :deep(.v-note-panel) {
  -webkit-box-shadow: 1px 1px 5px rgb(205,51,43) !important;
}

.comment-form :deep(.v-note-wrapper .v-note-panel) {
  border: #EBEEF5 solid 2px !important;
  order: 1;
}

.comment-form :deep(.v-note-wrapper .v-note-op) {
  order: 2;
  border-bottom: 0px;
}

.comment-form :deep(.op-icon-divider) {
  border-left: 0px !important;
}

.comment-form :deep(.v-note-wrapper) {
  position: initial;
  box-shadow: 0px 0px 0px 0px !important;
  display: flex !important;
  flex-direction: column;
}

.comment-form :deep(.v-note-op .op-icon) {
  box-sizing: border-box !important;
  width: 30px !important;
  height: 30px !important;
  background: #FFFFFF !important;
  border: 1px solid #BBBFC4 !important;
  border-radius: 4px !important;
  padding: 5px 4px 4px 3px !important;
}

.icon-title {
  color: #fff;
  width: 30px;
  background-color: #72dc91;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-radius: 30px;
  font-size: 14px;
  margin-top: 5px;
}
</style>
