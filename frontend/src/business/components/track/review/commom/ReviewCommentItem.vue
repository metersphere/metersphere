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
        {{ comment.createTime | timestampFormatDate }}
      </span>
      <span>
        <el-button v-if="comment.status === 'UnPass'" type="danger" size="mini" round>
         {{ $t('test_track.review.un_pass') }}
         </el-button>
         <el-button v-if="comment.status === 'Pass'" type="success" size="mini">
                        {{ $t('test_track.review.pass') }}
         </el-button>
      </span>
      <span class="comment-delete">
        <el-link icon="el-icon-edit" style="font-size: 9px;margin-right: 6px;" @click="openEdit" :disabled="readOnly"/>
        <el-link icon="el-icon-close" @click="deleteComment" :disabled="readOnly"/>
      </span>
      <br/>

      <div v-if="!isImage" class="comment-desc" style="font-size: 10px;color: #303133">
        <pre>{{ comment.description }}</pre>
      </div>
      <div v-if="isImage" class="demo-image__preview">
        <pre>{{ imgDescription }}</pre>
        <el-image
          :z-index="imageIndex"
          style="width: 100px; height: 100px;"
          fit="contain"
          :src="src"
          :preview-src-list="srcList">
        </el-image>
      </div>
    </div>

    <el-dialog :visible.sync="visible"
               :title="$t('commons.edit')"
               :destroy-on-close="true"
               :close-on-click-modal="false"
               append-to-body>

      <div>
        <div class="editors_div_style">
          <div id="editorsDiv">
            <ms-mark-down-text prop="description" :data="comment" :toolbars="toolbars"/>
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
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {getCurrentUser} from "@/common/js/utils";
import MsMarkDownText from "@/business/components/track/case/components/MsMarkDownText";

export default {
  name: "ReviewCommentItem",
  components: {MsDialogFooter, MsMarkDownText},
  props: {
    comment: Object,
    readOnly: {
      type: Boolean,
      default: false
    },
    reviewStatus: String,
    apiUrl: String
  },
  data() {
    return {
      visible: false,
      imgDescription: "",
      imageIndex: 99999,
      src: "",
      srcList: [],
      imgNameList: [],
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
  computed: {
    isImage() {
      return this.checkImage(this.comment.description);
    }
  },
  methods: {
    deleteComment() {
      if (getCurrentUser().id !== this.comment.author) {
        this.$warning(this.$t('test_track.comment.cannot_delete'));
        return;
      }
      if (this.imgNameList.length > 0) {
        this.imgNameList.forEach(imgName => {
          this.$get('/resource/md/delete/' + imgName);
        });
      }
      this.$get(this.apiUrl + "/comment/delete/" + this.comment.id, () => {
        this.$success(this.$t('commons.delete_success'));
        this.$emit("refresh");
      });
    },
    openEdit() {
      if (getCurrentUser().id !== this.comment.author) {
        this.$warning(this.$t('test_track.comment.cannot_edit'));
        return;
      }
      this.description = this.comment.description;
      this.visible = true;
    },
    editComment() {
      this.$post(this.apiUrl + "/comment/edit", {id: this.comment.id, description: this.comment.description}, () => {
        this.visible = false;
        this.$success(this.$t('commons.modify_success'));
        this.$emit("refresh");
      });
    },
    checkImage() {
      this.srcList = [];
      let param = this.comment.description;
      let returnFlag = false;
      if (param) {
        let message = param + "";
        let matchIndex = message.indexOf("](/resource/md/get");
        if (matchIndex > 0) {
          let messageSplitArr = message.split("](/resource/md/get");
          for (let itemIndex = 0; itemIndex < messageSplitArr.length; itemIndex++) {
            let itemStr = messageSplitArr[itemIndex];
            let picNameIndex = itemStr.indexOf("![");
            if (picNameIndex < 0) {
              let endUrlIndex = itemStr.indexOf(")");
              if (endUrlIndex > 0) {
                let itemStrArr = itemStr.substr(0, endUrlIndex);
                //if(imgNameList.)
                if (this.imgNameList.indexOf(itemStrArr) < 0) {
                  this.imgNameList.push(itemStrArr);
                }

                let imgUrl = "/resource/md/get" + itemStrArr;
                this.src = imgUrl;
                if (this.srcList.indexOf(itemStrArr) < 0) {
                  this.srcList.push(imgUrl);
                }
              }
            } else {
              let inputStr = itemStr.substr(0, picNameIndex);
              if (this.imgDescription === "") {
                this.imgDescription = inputStr;
              } else {
                this.imgDescription = "\n" + inputStr;
              }
            }
          }
        } else {
          let imgUrlIndex = message.indexOf("](http");
          if (imgUrlIndex > 0) {
            let imgUrlSplitArr = message.split("](http");
            for (let itemIndex = 0; itemIndex < imgUrlSplitArr.length; itemIndex++) {
              let itemStr = imgUrlSplitArr[itemIndex];
              let picNameIndex = itemStr.indexOf("![");
              if (picNameIndex < 0) {
                let endUrlIndex = itemStr.indexOf(")");
                if (endUrlIndex > 0) {
                  let itemStrArr = itemStr.substr(0, endUrlIndex);
                  //if(imgNameList.)
                  if (this.imgNameList.indexOf(itemStrArr) < 0) {
                    this.imgNameList.push(itemStrArr);
                  }
                  let imgUrl = "http" + itemStrArr;
                  this.src = imgUrl;
                  if (this.srcList.indexOf(itemStrArr) < 0) {
                    this.srcList.push(imgUrl);
                  }
                }
              } else {
                let inputStr = itemStr.substr(0, picNameIndex);
                if (this.imgDescription === "") {
                  this.imgDescription = inputStr;
                } else {
                  this.imgDescription = "\n" + inputStr;
                }
              }
            }
          }
        }
        if (this.srcList.length > 0) {
          returnFlag = true;
        }
      }
      return returnFlag;
    },
    checkByUrls(url) {
      let checkResultFlag = false;
      if (this.imgNameList.length > 0) {
        this.imgNameList.forEach(imgName => {
          if (imgName === url) {
            checkResultFlag = true;
          }
        });
      }
      return checkResultFlag;
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


.comment-desc {
  overflow-wrap: break-word;
  word-break: break-all;
  border-bottom: 1px solid #ced3de;
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

/deep/ .el-button--mini, .el-button--mini.is-round {
  padding: 7px 15px;
}

.send-btn {
  margin-top: 5px;
  width: 100%;
}
</style>
