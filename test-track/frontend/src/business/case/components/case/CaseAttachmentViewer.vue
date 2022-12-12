<template>
  <div>
    <div class="viewer-box">
      <div
        class="viewer-wrap"
        v-for="(item, index) in tableData"
        :key="item.id"
      >
        <case-attachment-item
          :fileItem="item"
          :isDelete="isDelete"
          :isCopy="isCopy"
          :readOnly="readOnly"
          :index="index"
          @handleDownload="handleDownload"
          @handleRetry="handleUpload"
          @handlePreview="preview"
          @handleUnLink="handleUnRelate"
          @handleDelete="handleDelete"
        ></case-attachment-item>
      </div>
    </div>
    <test-case-file ref="testCaseFile" />
  </div>
</template>

<script>
import TestCaseFile from "@/business/case/components/TestCaseFile";
import DownloadNotice from "metersphere-frontend/src/components/DownloadNotice";
import { downloadAttachment } from "@/api/attachment";
import CaseAttachmentItem from "./CaseAttachmentItem";

export default {
  name: "CaseAttachmentViewer",
  components: { TestCaseFile, CaseAttachmentItem },
  props: {
    tableData: Array,
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
  mixins: [DownloadNotice],
  data() {
    return {
      uploadProgressColor: "#d4f6d4",
      uploadSuccessColor: "#FFFFFF",
      that: null,
    };
  },
  methods: {
    preview(row) {
      this.$refs.testCaseFile.open(row);
    },
    handleDownload(file) {
      downloadAttachment(file.id, file.isLocal, file.name);
    },
    handleUpload(file) {
      this.$emit("handleDump", file);
    },
    handleDelete(file, index) {
      this.$emit("handleDelete", file, index);
    },
    handleUnRelate(file, index) {
      this.$emit("handleUnRelate", file, index);
    },
    handleCancel(file, index) {
      this.$emit("handleCancel", file, index);
    },
  },
};
</script>

<style scoped lang="scss">
@import "@/business/style/index.scss";
.viewer-box {
  width: 100%;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  margin-top: px2rem(8);
  .viewer-wrap {
    margin-bottom: px2rem(12);
  }
}
</style>
