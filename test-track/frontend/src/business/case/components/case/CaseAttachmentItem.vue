<template>
  <div class="atta-box">
    <div class="atta-container" :class="isError ? 'error-border' : ''">
      <div class="icon">
        <img :src="iconSrc" alt="" />
      </div>
      <div class="detail">
        <div class="filename">{{ fileItem.name }}</div>
        <div class="file-info-row" v-if="isSuccess">
          <div class="size">{{ fileItem.size }}</div>
          <div class="split">|</div>
          <div class="username">{{ fileItem.creator }}</div>
          <div class="fiexd">{{enableUnLink ? $t("case.relate_at") : $t("case.upload_at")}}</div>
          <div class="upload-time">
            {{ fileItem.updateTime | datetimeFormat }}
          </div>
        </div>
        <div class="file-info-row" v-if="isUploading">
          <div class="size">{{ uploadSize }}</div>
          <div class="split">/</div>
          <div class="size">{{ fileItem.size }}</div>
        </div>
        <div class="error-msg" v-else-if="isError">上传失败</div>
        <div class="error-msg" v-else-if="isExpired">
          该文件不存在
        </div>
        <div class="wait-upload" v-else-if="isToUpload">等待上传</div>
      </div>
      <div class="options">
        <!-- 预览 -->
        <div class="into opt-item" v-if="enablePreview" @click="handlePreview">
          <img src="/assets/module/figma/icon_visible_outlined.svg" alt=""/>
        </div>
        <!-- 下载 -->
        <div
          class="download opt-item"
          v-if="enableDownload"
          @click="handleDownload"
        >
          <img src="/assets/module/figma/icon_bottom-align_outlined.svg" alt="" />
        </div>
        <!-- 转储 -->
        <div class="retry opt-item" v-if="enableRetry" @click="handleRetry">
          <!-- <img src="/assets/figma/icon_refresh_outlined" alt="" /> -->
          <img src="/assets/module/figma/icon_into-item_outlined.svg" alt="" />
        </div>
        <!-- 取消关联 -->
        <div class="unLink opt-item" v-if="enableUnLink" @click="handleUnLink">
          <img src="/assets/module/figma/icon_unlink_outlined.svg" alt="" />
        </div>
        <!-- 删除 -->
        <div class="delete opt-item" v-if="enableDelete" @click="handleDelete">
          <img src="/assets/module/figma/icon_delete-trash_outlined.svg" alt="" />
        </div>
      </div>
    </div>
    <!-- 进度 -->
    <div class="process">
      <el-progress
        class="row-delete-name"
        type="line"
        v-if="!fileItem.isLocal && fileItem.isRelatedDeleted"
        :text-inside="true"
        :show-text="false"
        :stroke-width="4"
      >
      </el-progress>
      <el-progress
        v-else-if="!isError && !isExpired && !isToUpload && !isComplete"
        :color="fileItem.progress >= 100 ? '' : uploadProgressColor"
        type="line"
        :show-text="false"
        :text-inside="true"
        :percentage="fileItem.progress >= 100 ? 100 : fileItem.progress"
        :stroke-width="4"
      ></el-progress>
    </div>
  </div>
</template>
<script>
import {byteToSize, sizeToByte} from "@/business/utils/sdk-utils";

export default {
  name: "CaseAttachmentItem",
  props: {
    fileItem: Object,
    index: Number,
    readOnly: {
      type: Boolean,
      default: false,
    },
    isDelete: {
      type: Boolean,
      default: false,
    },
    isCopy: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      uploadProgressColor: "#AA4FBF",
    };
  },
  methods: {
    handleDownload() {
      if (this.readOnly) {
        return;
      }
      this.$emit("handleDownload", this.fileItem);
    },
    handleRetry() {
      if (this.readOnly) {
        return;
      }
      // 转储
      this.$emit("handleRetry", this.fileItem, this.index);
    },
    handlePreview() {
      if (this.readOnly) {
        return;
      }
      this.$emit("handlePreview", this.fileItem);
    },
    handleUnLink() {
      if (this.readOnly) {
        return;
      }
      this.$emit("handleUnLink", this.fileItem, this.index);
    },
    handleDelete() {
      if (this.readOnly) {
        return;
      }
      this.$emit("handleDelete", this.fileItem, this.index);
    },
    clearPercentage(data) {
      return () => {
        return data.name;
      };
    },
  },
  computed: {
    uploadSize() {
      return byteToSize(sizeToByte(this.fileItem.size) *  this.fileItem.progress / 100)
    },
    enableUnLink() {
      return (
        !(
          this.readOnly ||
          !this.isDelete ||
          this.isCopy ||
          (!this.fileItem.id && !this.isToRelate)
        ) &&
        this.isComplete &&
        !this.fileItem.isLocal
      );
    },
    enableDelete() {
      if (this.isToUpload || this.isError || this.isUploading) {
        return true;
      }
      return (
        !(
          this.readOnly ||
          // !this.isDelete ||
          this.isCopy ||
          (!this.fileItem.id && !this.isToUpload)
        ) &&
        this.isComplete &&
        this.fileItem.isLocal
      );
    },
    // 转储
    enableRetry() {
      return (
        !(this.isCopy || !this.fileItem.id) &&
        this.isComplete &&
        this.fileItem.isLocal
      );
    },
    enablePreview() {
      let fileType = this.fileItem.type || "";
      fileType = fileType.toUpperCase();
      return (
        this.isComplete &&
        !(
          !this.fileItem.id ||
          this.isToRelate ||
          this.fileItem.isRelatedDeleted
        ) &&
        (fileType === "JPG" ||
          fileType === "JPEG" ||
          fileType === "PDF" ||
          fileType === "PNG")
      );
    },
    enableDownload() {
      return (
        this.fileItem.id &&
        !this.isToRelate &&
        !this.fileItem.isRelatedDeleted &&
        this.isComplete
      );
    },
    isComplete() {
      return isNaN(this.fileItem.progress)
        ? true
        : parseInt(this.fileItem.progress) >= 100;
    },
    isSuccess() {
      return this.fileItem.status === "success";
    },
    isToUpload() {
      return this.fileItem.status === "toUpload";
    },
    isToRelate() {
      return this.fileItem.status === "toRelate";
    },
    isExpired() {
      return this.fileItem.status === "expired";
    },
    isUploading() {
      return !isNaN(this.fileItem.status)
    },
    isError() {
      return (
        !this.isSuccess &&
        !this.isToUpload &&
        !this.isToRelate &&
        !this.isExpired &&
        !this.isUploading
      );
    },
    fileStatus() {
      return this.fileItem.status;
    },
    iconSrc() {
      let type = this.fileItem.type || "None";
      type = type.toLowerCase();
      let prefix = "/assets/module/figma/";
      let suffix = ".svg";
      let src = "";
      switch (type) {
        case "txt":
          src = "icon_file-text_colorful";
          break;
        case "xlsx":
          src = "icon_file-excel_colorful";
          break;
        case "word":
          src = "icon_file-word_colorful";
          break;
        case "pdf":
          src = "icon_file-pdf_colorful";
          break;
        case "xmind":
          src = "icon_file-xmind_colorful";
          break;
        case "ppt":
          src = "icon_file-ppt_colorful";
          break;
        case "sketch":
          src = "icon_file-sketch_colorful";
          break;
        case "csv":
          src = "icon_file-CSV_colorful";
          break;
        case "gif":
        case "png":
        case "jpg":
        case "jpge":
          src = "icon_file-image_colorful";
          break;
        case "sql":
          src = "icon_file-sql_colorful";
          break;

        // 视频资源
        case "mp4":
        case "flv":
        case "mp3":
          src = "icon_file-vedio_colorful";
          break;
        case "zip":
        case "jar":
        case "jtl":
        case "gz":
        case "tar":
        case "jmx":
          src = "icon_file-all-zip_colorful";
          break;
        default:
          // 未知
          src = "icon_file-unknow_colorful";
          break;
      }

      return prefix + src + suffix;
    },
  },
};
</script>
<style scoped lang="scss">
@import "@/business/style/index.scss";
.atta-box {
  width: px2rem(480);
  height: px2rem(58);
  .atta-container:hover {
    border-color: #783887;
  }
  .error-border {
    border-color: #f54a45 !important;
  }
  .atta-container {
    width: px2rem(480);
    height: px2rem(58);
    display: flex;
    justify-content: flex-start;
    background: #ffffff;
    border: 1px solid #dee0e3;
    border-radius: 4px;
    .icon {
      margin-top: px2rem(11.5);
      margin-left: px2rem(18.67);
      width: px2rem(37);
      margin-right: px2rem(14.67);
      img {
        width: 100%;
        /* height: 100%; */
      }
    }

    .detail {
      margin-top: px2rem(8);
      margin-right: px2rem(17.33);
      width: px2rem(310);
      display: flex;
      flex-direction: column;
      .filename {
        width: 100%;
        color: #1f2329;
        height: px2rem(22);
        line-height: px2rem(22);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .wait-upload {
        height: px2rem(20);
        font-size: 12px;
        line-height: px2rem(20);
        color: #8f959e;
      }
      .error-msg {
        height: px2rem(20);
        font-size: 12px;
        line-height: px2rem(20);
        color: #f54a45;
      }
      .file-info-row {
        display: flex;
        font-size: 12px;
        line-height: px2rem(20);
        height: px2rem(20);
        color: #8f959e;
        .size {
        }

        .split {
          margin: 0 px2rem(8);
        }

        .username {
        }

        .fiexd {
          margin: 0 px2rem(8);
        }

        .upload-time {
        }
      }
    }

    .options {
      width: px2rem(112);
      display: flex;
      justify-content: flex-end;
      align-items: center;
      img {
        width: 13.33px;
        height: 13.33px;
      }

      .opt-item {
        margin-right: 20px;
        cursor: pointer;
      }
      .download {
        img {
        }
      }

      .retry {
        img {
        }
      }

      .into {
        img {
        }
      }

      .unLink {
        img {
        }
      }

      .delete {
        img {
        }
      }
    }
  }
  .process {
    width: px2rem(480);
    position: relative;
    bottom: px2rem(4);
    height: px2rem(4);
    :deep(.el-progress.row-delete-name .el-progress-bar__innerText) {
      color: lightgrey !important;
    }
  }
}
</style>
