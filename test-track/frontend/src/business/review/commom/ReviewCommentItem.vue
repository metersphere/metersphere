<template>
  <div class="main">
    <div class="comment-left">
      <div class="icon-title">
        {{ comment.authorName.substring(0, 1) }}
      </div>
    </div>
    <div class="comment-right">
      <span style="font-size: 14px;color: #909399;font-weight: bold">{{ comment.authorName }}</span>
      <span style="color: #8a8b8d; margin-left: 8px; font-size: 12px">
        {{ comment.createTime | datetimeFormat }}
      </span>
      <span v-if="showStatus">
         <status-table-item v-if="comment.status" :value="comment.status"/>
      </span>
      <span class="comment-delete">
        <el-link icon="el-icon-edit" style="font-size: 9px;margin-right: 6px;" @click="openEdit" :disabled="readOnly"/>
        <el-link icon="el-icon-close" v-prevent-link-re-click="1300" @click="deleteComment" :disabled="readOnly"/>
      </span>
      <br/>
      <ms-mark-down-text
        class="rich-text"
        prop="description"
        :disabled="true"
        :data="comment"/>
    </div>

    <el-dialog :visible.sync="visible"
               :title="$t('commons.edit')"
               :destroy-on-close="true"
               :close-on-click-modal="false"
               @close="handleClose"
               append-to-body>

      <div>
        <div class="editors_div_style">
          <div id="editorsDiv">
            <ms-mark-down-text default-open="edit" prop="description" :data="comment" :toolbars="toolbars" :custom-min-height="200"/>
          </div>
        </div>
        <div>
          <el-button type="primary" size="mini" class="send-btn" @click="editComment">
            {{ $t('test_track.comment.send') }}
          </el-button>
        </div>
      </div>

    </el-dialog>
  </div>
</template>

<script>
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {getCurrentUser} from "metersphere-frontend/src/utils/token";
import MsMarkDownText from "metersphere-frontend/src/components/MsMarkDownText";
import {deleteMarkDownImgByName} from "@/business/utils/sdk-utils";
import StatusTableItem from "@/business/common/tableItems/planview/StatusTableItem";

export default {
  name: "ReviewCommentItem",
  components: {StatusTableItem, MsDialogFooter, MsMarkDownText},
  props: {
    comment: Object,
    readOnly: {
      type: Boolean,
      default: false
    },
    apiUrl: String,
    showStatus: {
      type: Boolean,
      default: true
    },
  },
  data() {
    return {
      visible: false,
      imgDescription: '',
      originDesc: '',
      src: "",
      srcList: [],
      description: "",
      imageMatchPattern: "(\\!\\[)\\S+]\\(\\S+\\)",
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
    }
  },
  methods: {
    deleteComment() {
      if (getCurrentUser().id !== this.comment.author) {
        this.$warning(this.$t('test_track.comment.cannot_delete'));
        return;
      }
      this.$get(this.apiUrl + "/comment/delete/" + this.comment.id)
        .then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.$emit("refresh");
        });
    },
    openEdit() {
      this.originDesc = this.comment.description;
      if (getCurrentUser().id !== this.comment.author) {
        this.$warning(this.$t('test_track.comment.cannot_edit'));
        return;
      }
      this.description = this.comment.description;
      this.visible = true;
    },
    editComment() {
      this.$post(this.apiUrl + "/comment/edit", {id: this.comment.id, description: this.comment.description})
        .then(() => {
          this.originDesc = this.comment.description;
          this.visible = false;
          this.$success(this.$t('commons.modify_success'));
          this.$emit("refresh");
        });
    },
    handleClose() {
      this.comment.description = this.originDesc;
    }
  }
}
</script>

<style scoped>
.main {
  overflow-y: scroll;
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.comment-left {
  float: left;
  width: 50px;
  height: 100%;
}

.comment-right {
  float: left;
  width: 90%;
  padding: 0;
  line-height: 25px;
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
}

pre {
  margin: 0 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  width: 100%;
  line-height: 20px;
}

.comment-delete {
  float: right;
  margin-right: 5px;
  cursor: pointer;
}

:deep(.el-button--mini, .el-button--mini.is-round) {
  padding: 7px 15px;
}

.send-btn {
  margin-top: 5px;
  width: 100%;
}
</style>
