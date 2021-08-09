<template>
  <div v-loading="result.loading">
    <div class="comment-list">
      <review-comment-item v-for="(comment) in comments" v-bind:key="comment.id"
                           ref="reviewComments"
                           :comment="comment"
                           @refresh="refresh"
                           :review-status="reviewStatus"/>
      <div v-if="comments.length === 0" style="text-align: center" >
        <i class="el-icon-chat-line-square" style="font-size: 15px;color: #8a8b8d;">
        <span style="font-size: 15px; color: #8a8b8d;">
          {{ $t('test_track.comment.no_comment') }}
        </span>
        </i>
      </div>
    </div>
    <div>
      <div class="editors_div_style">
        <div id="editorsDiv" >
          <mavon-editor v-if="showEditor" @imgAdd="imgAdd" :default-open="'edit'" class="review-mavon-editor" :imageFilter="imageFilter"
                        :toolbars="richDataToolbars"  @imgDel="imgDel" v-model="textarea"  ref="md"/>
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
import {getUUID} from "@/common/js/utils";

export default {
  name: "ReviewComment",
  components: {ReviewCommentItem,FormRichTextItem},
  props: {
    caseId: String,
    comments: Array,
    reviewId: String,
    reviewStatus: String,
  },
  data() {
    return {
      result: {},
      textarea: '',
      loadCommenItem:true,
      labelWidth: '120px',
      showEditor:true,
      isReadOnly: false,
      richDataToolbars: {
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
  watch:{
    comments(){
    }
  },
  methods: {
    sendComment() {
      let comment = {};
      comment.caseId = this.caseId;
      comment.description = this.textarea;
      comment.reviewId = this.reviewId;
      comment.status = this.reviewStatus;
      if (!this.textarea) {
        this.$warning(this.$t('test_track.comment.description_is_null'));
        return;
      }
      this.result = this.$post('/test/case/comment/save', comment, () => {
        this.$success(this.$t('test_track.comment.send_success'));
        this.refresh();
        if(this.$refs.md){
          this.$refs.md.toolbar_left_click('trash');
        }
      });
    },
    inputLight() {
      let textAreaDom = this.$refs.md.getTextareaDom();
      let editorDivDom = document.getElementById("editorsDiv");
      if(editorDivDom){
        editorDivDom.setAttribute("style","-webkit-box-shadow: 0 0 8px rgb(205,51,43);");
      }
      textAreaDom.focus();
     },
    resetInputLight(){
      let editorDivDom = document.getElementById("editorsDiv");
      if(editorDivDom){
        editorDivDom.setAttribute("style","-webkit-box-shadow: 0 0 0px rgb(-1,0,0);");
      }
    },
    refresh() {
      this.resetInputLight();
      this.$emit('getComments');
    },
    //富文本框
    imgAdd(pos, file) {
      let param = {
        id: getUUID().substring(0, 8)
      };

      file.prefix = param.id;
      this.result = this.$fileUpload('/resource/md/upload', file, null, param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.md.$img2Url(pos, '/resource/md/get/'  + param.id+"_"+file.name);
        this.sendComment();
      });
      this.$emit('imgAdd', file);
    },
    imageFilter(file){
      let isImg = false;
      if(file){
        if(file.name){
          if (file.name.indexOf("[")> 0 || file.name.indexOf("]") > 0||file.name.indexOf("([)")> 0 || file.name.indexOf(")") > 0){
            this.$error("图片名称不能含有特殊字符");
            isImg = false;
          }else {
            isImg = true;
          }
        }
      }
      return isImg;
    },
    imgDel(file) {
      let fileUrl = file[1].prefix + "_" + file[1].name;
      let comments = this.$refs.reviewComments;
      comments.forEach(item => {
        let imgCheckResult = item.checkByUrls(fileUrl);
        if(imgCheckResult){
          item.deleteComment();
        }
      });
    },
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
.editors-div{
  -webkit-box-shadow: 0 0 8px rgb(-1,0,0);
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
</style>
