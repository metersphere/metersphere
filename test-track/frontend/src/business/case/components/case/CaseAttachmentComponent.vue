<template>
  <div class="upload-default" @click.stop>
    <el-popover
      ref="popover"
      placement="right"
      trigger="hover"
      popper-class="attachment-popover"
      :visible-arrow="false"
    >
      <div
        class="upload-wrap"
        style="
          display: flex;
          flex-direction: column;
          width: 120px;
          align-items: center;
        "
      >
        <!-- 本地上传 -->
        <div class="local-row opt-row"
          style="
            height: 32px;
            margin-top: 8px;
            line-height: 32px;
            text-align: center;
            margin-left: 1px;
            margin-right: 1px;
          "
        >
          <div class="title" style="letter-spacing: -0.1px; color: #1f2329">
            <el-upload
              multiple
              action=""
              :auto-upload="true"
              :file-list="fileList"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="handleUpload"
              :on-exceed="handleExceed"
              :on-success="handleSuccess"
              :on-error="handleError"
              :disabled="readOnly"
            >
              <div class="icon" style="display: inline-flex; line-height: 34px" @click="uploadLocalFile">
                <div style="margin-right: 10px">
                  <i class="el-icon-upload2" style="color: #646a73"></i>
                </div>
                <el-button
                  size="small"
                  :disabled="readOnly"
                  type="text"
                  >{{ $t("permission.project_file.local_upload") }}</el-button
                >
              </div>
            </el-upload>
          </div>
        </div>
        <!-- 关联文件 -->
        <div class="ref-row opt-row"
          @click="associationFile"
          style="
            display: flex;
            justify-content: center;
            height: 32px;
            margin-bottom: 8px;
            line-height: 32px;
            text-align: center;
            magin-left: 1px;
            magin-right: 1px;
          "
        >
          <div class="icon" @click="associationFile" style="line-height: 34px">
            <i class="el-icon-connection" style="color: #646a73"></i>
          </div>
          <div
            class="title"
            style="letter-spacing: -0.1px; color: #1f2329; margin-left: 10px"
          >
            <el-button
              type="text"
              :disabled="readOnly"
              size="small"
              @click="associationFile"
              >{{ $t("permission.project_file.associated_files") }}</el-button
            >
          </div>
        </div>
      </div>
    </el-popover>
    <el-button v-popover:popover icon="el-icon-plus" size="small" class="add-attachment">{{
      $t("case.add_attachment")
    }}</el-button>
    <div class="opt-tip">{{ $t("case.file_size_limit") }}</div>
    <div class="attachment-preview">
      <case-attachment-viewer
        :tableData="tableData"
        :isCopy="isCopy"
        :readOnly="readOnly"
        :is-delete="isDelete"
        @handleRetry="handleUpload"
        @handleUnRelate="handleUnRelate"
        @handleDelete="handleDelete"
        @handleDump="handleDump"
      ></case-attachment-viewer>
    </div>

    <ms-file-metadata-list ref="metadataList" @checkRows="checkRows" />
    <ms-file-batch-move ref="module" @setModuleId="setModuleId" />
  </div>
</template>
<script>
import CaseAttachmentViewer from "@/business/case/components/case/CaseAttachmentViewer";
import MsFileMetadataList from "../common/MsFileMetadataList";
import { TokenKey } from "metersphere-frontend/src/utils/constants";
import {
  byteToSize,
  getCurrentUser,
  getTypeByFileName,
} from "@/business/utils/sdk-utils";
import axios from "axios";
import { getUUID } from "metersphere-frontend/src/utils";
import { getCurrentProjectID } from "metersphere-frontend/src/utils/token";

import {
  attachmentList,
  deleteTestCaseAttachment,
  dumpAttachment,
  relatedTestCaseAttachment,
  unrelatedTestCaseAttachment,
  uploadTestCaseAttachment,
} from "@/api/attachment";
import MsFileBatchMove from "metersphere-frontend/src/components/environment/commons/variable/FileBatchMove";

export default {
  name: "CaseAttachmentComponent",
  components: {
    CaseAttachmentViewer,
    MsFileMetadataList,
    MsFileBatchMove,
  },
  props: {
    caseId: String,
    readOnly: Boolean,
    projectId: String,
    isCopy: Boolean,
    copyCaseId: String,
    type: String,
    isClickAttachmentTab: Boolean,
    isDelete: Boolean,
    belongType: {
      type: String,
      default: "testcase",
    },
    issueId: String,
    editable: Boolean
  },
  data() {
    return {
      tableData: [],
      fileList: [],
      intervalMap: new Map(),
      cancelFileToken: [],
      uploadFiles: [],
      relateFiles: [],
      unRelateFiles: [],
      filterCopyFiles: [],
      dumpFile: {},
      result: {},
    };
  },
  computed: {
    targetId() {
      return this.belongType === "issue" ? this.issueId : this.caseId;
    },
  },
  watch: {
    caseId() {
      if (this.caseId) {
        this.getFileMetaData();
      }
    }
  },
  created() {
    this.getFileMetaData();
  },
  methods: {
    uploadLocalFile() {
      this.$refs['popover'].doClose();
    },
    associationFile() {
      this.$refs['popover'].doClose();
      if (this.readOnly) {
        return;
      }
      //唤起关联文件
      this.$refs.metadataList.open();
    },

    /**
     *  upload file methods
     */
    fileValidator(file) {
      return file.size < 500 * 1024 * 1024;
    },
    beforeUpload(file) {
      if (!this.fileValidator(file)) {
        this.$error(this.$t("case.file_size_out_of_bounds"), false);
        return false;
      }

      if (this.tableData.filter((f) => f.name === file.name).length > 0) {
        this.$error(this.$t("load_test.delete_file") + ", name: " + file.name, false);
        return false;
      }
    },
    handleUpload(e) {
      // 表格生成上传文件数据
      let file = e.file;
      let user = JSON.parse(localStorage.getItem(TokenKey));
      this.tableData.push({
        name: file.name,
        size: byteToSize(file.size),
        updateTime: new Date().getTime(),
        progress: this.editable ? 100 : 0,
        status: this.editable ? "toUpload" : 0,
        creator: user.name,
        type: getTypeByFileName(file.name),
        isLocal: true,
      });
      if (this.editable) {
        // 新增上传
        this.uploadFiles.push(file);
        return false;
      }
      // 上传文件
      this.uploadFile(e, (param) => {
        this.showProgress(e.file, param);
      });
    },
    async uploadFile(param, progressCallback) {
      let progress = 0;
      let file = param.file;
      let data = { belongId: this.targetId, belongType: this.belongType };
      let CancelToken = axios.CancelToken;
      let self = this;

      uploadTestCaseAttachment(
        file,
        data,
        CancelToken,
        self.cancelFileToken,
        progressCallback
      )
      .then((response) => {
        // 成功回调
        progress = 100;
        param.onSuccess(response);
        progressCallback({ progress, status: "success" });
        this.$success(this.$t('attachment.upload_success'), false)
        self.cancelFileToken.forEach((token, index, array) => {
          if (token.name === file.name) {
            array.splice(token, 1);
          }
        });
      })
      .catch(({ error }) => {
        // 失败回调
        progress = 100;
        progressCallback({ progress, status: "error" });
        this.$success(this.$t('attachment.upload_error'), false)
        self.cancelFileToken.forEach((token, index, array) => {
          if (token.name === file.name) {
            array.splice(token, 1);
          }
        });
      });
    },
    showProgress(file, params) {
      const { progress, status } = params;
      const arr = [...this.tableData].map((item) => {
        if (item.name === file.name) {
          item.progress = progress;
          item.status = status;
        }
        return item;
      });
      this.tableData = [...arr];
    },
    handleExceed(files, fileList) {
      this.$error(this.$t("load_test.file_size_limit"), false);
    },
    handleSuccess(response, file, fileList) {
      let readyFiles = fileList.filter((item) => item.status === "ready");
      if (readyFiles.length === 0) {
        this.getFileMetaData();
      }
    },
    handleError(err, file, fileList) {
      let readyFiles = fileList.filter((item) => item.status === "ready");
      if (readyFiles.length === 0) {
        this.getFileMetaData();
      }
    },
    handleDelete(file, index) {
      this.$alert(
        (this.cancelFileToken.length > 0 ? this.$t("load_test.delete_file_when_uploading") + "<br/>" : "") +
                    this.$t("load_test.delete_file_confirm") + file.name + "?",
        this.$t("attachment.delete_confirm_tips"),
        {
          confirmButtonText: this.$t("commons.confirm"),
          dangerouslyUseHTMLString: true,
          callback: (action) => {
            if (action === "confirm") {
              this._handleDelete(file, index);
            }
          },
        }
      );
    },
    _handleDelete(file, index) {
      // 中断所有正在上传的文件
      if (this.cancelFileToken && this.cancelFileToken.length >= 1) {
        this.cancelFileToken.forEach((cancelToken) => {
          cancelToken.cancelFunc();
        });
      }
      this.fileList.splice(index, 1);
      this.tableData.splice(index, 1);
      if (file.status === 'toUpload') {
        // 带上传的附件直接移除
        let uploadIndex = this.findUploadFileIndex(file);
        this.uploadFiles.splice(uploadIndex, 1);
      } else {
        if (this.isCopy) {
          //复制过来的附件需在后台过滤
          this.filterCopyFiles.push(file.id);
        } else {
          // 编辑, 直接删除
          deleteTestCaseAttachment(file.id).then(() => {
            this.$success(this.$t("commons.delete_success"), false);
            this.getFileMetaData();
          });
        }
      }
    },
    findUploadFileIndex(targetFile) {
      for (let i = 0; i < this.uploadFiles.length; i++) {
        if (this.uploadFiles[i].name === targetFile.name) {
          return i;
        }
      }
      return -1;
    },
    handleUnRelate(file, index) {
      // 取消关联
      this.$alert(
        this.$t("load_test.unrelated_file_confirm") + file.name + "?",
        "",
        {
          confirmButtonText: this.$t("commons.confirm"),
          dangerouslyUseHTMLString: true,
          callback: (action) => {
            if (action === "confirm") {
              this.result.loading = true;
              let unRelateFileIndex = this.tableData.findIndex((f) => f.name === file.name);
              this.tableData.splice(unRelateFileIndex, 1);
              if (file.status === "toRelate") {
                // 待关联的记录, 直接移除
                let unRelateId = this.relateFiles.findIndex((f) => f === file.id);
                this.relateFiles.splice(unRelateId, 1);
                this.result.loading = false;
              } else {
                if (this.isCopy) {
                  // 复制过来的记录, 后台过滤
                  this.filterCopyFiles.push(file.id)
                } else {
                  // 已经关联的记录
                  this.unRelateFiles.push(file.id);
                  let data = {
                    belongType: this.belongType,
                    belongId: this.targetId,
                    metadataRefIds: this.unRelateFiles,
                  };
                  unrelatedTestCaseAttachment(data).then(() => {
                    this.$success(this.$t("commons.unrelated_success"), false);
                    this.result.loading = false;
                    this.getFileMetaData(this.issueId);
                  });
                }
              }
            }
          },
        }
      );
    },
    handleDump(file) {
      this.$refs.module.init();
      this.dumpFile = file;
    },
    handleCancel(file, index) {
      this.fileList.splice(index, 1);
      let cancelToken = this.cancelFileToken.filter(
        (f) => f.name === file.name
      )[0];
      cancelToken.cancelFunc();
      let cancelFile = this.tableData.filter((f) => f.name === file.name)[0];
      cancelFile.progress = 100;
      cancelFile.status = "error";
    },
    getFileMetaData(id) {
      //this.$emit("update:isClickAttachmentTab", true);
      // 保存用例后传入用例id，刷新文件列表，可以预览和下载
      this.fileList = [];
      this.tableData = [];
      let testCaseId;
      if (this.isCopy && !this.belongType === "issue") {
        testCaseId = this.copyCaseId;
      } else {
        testCaseId = id ? id : this.targetId;
      }
      if (testCaseId) {
        let data = { belongType: this.belongType, belongId: testCaseId };
        this.result.loading = true;
        attachmentList(data).then((response) => {
          this.result.loading = false;
          let files = response.data;
          if (!files) {
            return;
          }
          // deep copy
          this.fileList = JSON.parse(JSON.stringify(files));
          this.tableData = JSON.parse(JSON.stringify(files));
          this.tableData.map((f) => {
            f.size = byteToSize(f.size);
            f.status = "success";
            f.progress = 100;
          });
        });
      }
    },

    checkRows(rows) {
      let repeatRecord = false;
      for (let row of rows) {
        let rowIndex = this.tableData.findIndex(
          (item) => item.name === row.name
        );
        if (rowIndex >= 0) {
          this.$error(
            this.$t("load_test.exist_related_file") + ": " + row.name, false
          );
          repeatRecord = true;
          break;
        }
      }
      if (!repeatRecord) {
        if (this.editable) {
          // 新增
          rows.forEach((row) => {
            this.relateFiles.push(row.id);
            this.tableData.push({
              id: row.id,
              name: row.name,
              size: byteToSize(row.size),
              updateTime: row.createTime,
              progress: 100,
              status: "toRelate",
              creator: getCurrentUser().id,
              type: row.type,
              isLocal: false,
            });
          });
        } else {
          // 编辑
          let metadataRefIds = [];
          rows.forEach((row) => metadataRefIds.push(row.id));
          let data = {
            belongType: this.belongType,
            belongId: this.targetId,
            metadataRefIds: metadataRefIds,
          };
          this.result.loading = true;
          relatedTestCaseAttachment(data).then(() => {
            this.$success(this.$t("commons.relate_success"), false);
            this.result.loading = false;
            this.getFileMetaData();
          });
        }
      }
    },
    setModuleId(moduleId) {
      let data = {
        id: getUUID(),
        resourceId: getCurrentProjectID(),
        moduleId: moduleId,
        projectId: getCurrentProjectID(),
        fileName: this.dumpFile.name,
        attachmentId: this.dumpFile.id,
      };
      dumpAttachment(data).then(() => {
        this.$success(this.$t("organization.integration.successful_operation"), false);
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.upload-default {
  width: 100%;
}
.opt-tip {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  /* identical to box height, or 157% */
  width: 100%;
  color: #8f959e;
  margin-top: 2px;
}
.el-button--small {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  letter-spacing: -0.1px;
  color: #1f2329;
}

.add-attachment:focus {
  border-color: #DCDFE6;
  background-color: whitesmoke;
}

.attachment-popover .opt-row{
  width: 100%!important;
}

.attachment-popover .opt-row:hover {
  background-color: rgba(31, 35, 41, 0.1);;
}
</style>
