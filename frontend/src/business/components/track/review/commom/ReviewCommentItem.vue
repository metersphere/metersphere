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
        <el-link icon="el-icon-edit" v-if="!isImage" style="font-size: 9px;margin-right: 6px;" @click="openEdit" :disabled="readOnly"/>
        <el-link icon="el-icon-close" @click="deleteComment" :disabled="readOnly"/>
      </span>
      <br/>
<!--      <div  class="comment-desc" style="font-size: 10px;color: #303133">-->
<!--        <pre>{{ comment.description }}</pre>-->
<!--      </div>-->
      <div  v-if="!isImage" class="comment-desc" style="font-size: 10px;color: #303133">
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

    <el-dialog
      :title="$t('commons.edit')"
      :visible.sync="visible"
      width="30%"
      :destroy-on-close="true"
      :append-to-body="true"
      :close-on-click-modal="false"
      show-close>
      <el-input
        type="textarea"
        :rows="5"
        v-model="description">
      </el-input>
      <span slot="footer" class="dialog-footer">
        <ms-dialog-footer
          @cancel="visible = false"
          @confirm="editComment"/>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {getCurrentUser} from "@/common/js/utils";

export default {
  name: "ReviewCommentItem",
  components: {MsDialogFooter},
  props: {
    comment: Object,
    readOnly: {
      type: Boolean,
      default: false
    },
    reviewStatus: String,
  },
  data() {
    return {
      visible: false,
      imgDescription: "",
      imageIndex:99999,
      src:"",
      srcList:[],
      imgNameList:[],
      description: "",
      imageMatchPattern:"(\\!\\[)\\S+]\\(\\S+\\)",
    }
  },
  computed:{
    isImage(){
      return this.checkImage(this.comment.description);
    }
  },
  methods: {
    deleteComment() {
      if (getCurrentUser().id !== this.comment.author) {
        this.$warning(this.$t('test_track.comment.cannot_delete'));
        return;
      }
      this.$get("/test/case/comment/delete/" + this.comment.id, () => {
        this.$success(this.$t('commons.delete_success'));
        this.$emit("refresh");
      });
      if(this.imgNameList.length > 0){
        this.imgNameList.forEach(imgName => {
          this.$get('/resource/md/delete/' + imgName);
        });
      }
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
      this.$post("/test/case/comment/edit", {id: this.comment.id, description: this.description}, () => {
        this.visible = false;
        this.$success(this.$t('commons.modify_success'));
        this.$emit("refresh");
      });
    },
    checkImage(){
      let param = this.comment.description;
      let returnFlag = false;
      if(param){
        let message = param+"";
        let messageSplitArr = message.split("](/resource/md/get/");
        let matchIndex = message.indexOf("](/resource/md/get/");
        if(matchIndex > 0){
          for(let itemIndex = 0;itemIndex < messageSplitArr.length; itemIndex ++){
            let itemStr = messageSplitArr[itemIndex];
            let picNameIndex = itemStr.indexOf("![");
            if( picNameIndex < 0){
              let endUrlIndex = itemStr.indexOf(")");
              if( endUrlIndex > 0){
                let itemStrArr = itemStr.substr(0,endUrlIndex);
                //if(imgNameList.)
                if(this.imgNameList.indexOf(itemStrArr) < 0){
                  this.imgNameList.push(itemStrArr);
                }

                let imgUrl = "/resource/md/get/"+itemStrArr;
                this.src = imgUrl;

                if(this.srcList.indexOf(itemStrArr) < 0){
                  this.srcList.push(imgUrl);
                }
              }
            }else{
              let inputStr = itemStr.substr(0,picNameIndex);
              if(this.imgDescription === ""){
                this.imgDescription = inputStr;
              }else {
                this.imgDescription = "\n" + inputStr;
              }
            }
          }
        }
        if(this.srcList.length > 0){
          returnFlag = true;
        }
      }
      return returnFlag;
    },
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
  padding: 4px 9px;
}
</style>
