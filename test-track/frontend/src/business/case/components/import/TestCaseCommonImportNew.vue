<template>
  <el-dialog class="case-import" :title="name == 'excel' ? $t('test_track.case.import.import_by_excel') : $t('test_track.case.import.import_by_xmind')" :visible.sync="dialogVisible" @close="close" :width="'600px'">
    <div class="case-import-div">
      <el-row class="import-row">
        <div class="download-tips">
          <svg-icon icon-class="icon_info_colorful-2" style="float: left;position: relative;top: 11px;width: 1.3em;height: 1.3em;left: 20px;"/>
          <span class="download-tips-content">
            <label>{{$t('test_track.case.import.download_template_tips_please')}}</label>
            <el-link type="primary" class="download-link" @click="downloadXmindTemplate">
              {{ $t('test_track.case.import.download_template') }}
            </el-link>
            <label>{{$t('test_track.case.import.download_template_tips_fill_upload_tips')}}</label>
          </span>
        </div>
      </el-row>

      <el-row class="import-row">
        <el-upload
          v-loading="loading"
          :element-loading-text="$t('test_track.case.import.importing')"
          element-loading-spinner="el-icon-loading"
          class="case-import-upload"
          ref="caseUpload"
          action=""
          multiple :limit="1"
          drag :show-file-list="false"
          :beforeUpload="uploadValidateXmind"
          :on-change="handleChange"
          :on-exceed="handleExceed"
          :on-error="handleError"
          :http-request="addFile"
          :on-remove="handleRemove"
          :file-list="fileList">
          <svg-icon v-if="!lastFile" icon-class="icon_uploadfile2_colorful" style="width: 2.7em;height: 2.7em;position: relative;top: 26px;"></svg-icon>
          <svg-icon v-if="lastFile && name == 'excel'" icon-class="icon_file-excel_colorful" style="width: 2.7em;height: 2.7em;position: relative;top: 26px;"></svg-icon>
          <svg-icon v-if="lastFile && name == 'xmind'" icon-class="icon_file-xmind_colorful" style="width: 2.7em;height: 2.7em;position: relative;top: 26px;"></svg-icon>
          <div class="el-upload__text" v-if="progressFlag">
            <div class="upload-drag-tips">{{$t('test_track.case.import.import_uploading_tips')}}</div>
            <el-progress :percentage="loadProgress" class="case-upload-progress" :show-text="false"></el-progress>
          </div>
          <div class="el-upload__text" v-if="!progressFlag">
            <div class="upload-drag-tips">{{lastFile ? lastFile.name : $t('test_track.case.import.import_upload_drag_tips')}}</div>
            <div v-if="isExcel && !lastFile" class="upload-file-tips">{{$t('test_track.case.import.upload_limit')}}</div>
            <div v-if="isXmind && !lastFile" class="upload-file-tips">{{$t('test_track.case.import.upload_xmind')}}</div>
            <div v-if="lastFile" class="change-file-content">{{$t('test_track.case.import.import_change_file')}}</div>
          </div>
        </el-upload>
      </el-row>

      <el-row class="import-row">
        <span style="display: block; color: #1F2329; margin: 4px 0 10px;">{{$t('test_track.case.import.import_type')}}</span>
        <el-radio v-model="importType" label="Create" style="color: #1F2329; margin-bottom: 10px">{{ $t('test_track.case.import.import_create') }}</el-radio>
        <el-radio v-model="importType" label="Update" style="color: #1F2329; margin-bottom: 10px">{{ $t('test_track.case.import.import_update') }}</el-radio>
        <div v-if="importType === 'Create'" style="color: #646A73">
          {{ $t('test_track.case.import.import_tip1') }}
        </div>
        <div v-else style="color: #646A73">
          {{ $t('test_track.case.import.import_tip2') }}
        </div>
      </el-row>

      <el-row class="import-row">
        <span style="display: block; color: #1F2329; margin: 4px 0 10px;">{{$t('test_track.case.import.import_select_version')}}</span>
        <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion" style="width: 100%"/>
      </el-row>

      <el-row style="text-align: right; margin-top: 25px">
        <el-popover
          v-show="showContinueBtn"
          placement="right-end"
          width="400"
          class="error-popover"
          trigger="click">
          <el-scrollbar>
            <div style="max-height: 150px" class="import-error-list-div">
              <div v-for="errFile in errList" :key="errFile.num">
                {{ errFile.errMsg }}
              </div>
            </div>
          </el-scrollbar>
          <el-link slot="reference" type="primary">{{$t('test_track.case.import.click_preview_import_error_msg')}}</el-link>
        </el-popover>
        <el-button @click="close" size="small">{{ $t('commons.cancel') }}</el-button>
        <el-button v-if="showContinueBtn" type="primary" size="small" @click="upload(true)">
          {{ $t('test_track.case.import.continue_upload') }}
        </el-button>
        <el-button v-if="!showContinueBtn" type="primary" size="small" @click="upload(false)">
          {{ $t('commons.import') }}
        </el-button>
      </el-row>
    </div>
  </el-dialog>
</template>

<script>
import {getCurrentProjectID, getCurrentUserId} from "metersphere-frontend/src/utils/token";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";

export default {
  name: "TestCaseCommonImport",
  components: {'VersionSelect': MxVersionSelect},
  data() {
    return {
      loading: false,
      fileList: [],
      errList: [],
      importType: 'Create',
      showContinueBtn: false,
      uploadIgnoreError: false,
      lastFile: null,
      versionId: null,
      name: "excel",
      dialogVisible: false,
      progressFlag: false,
      loadProgress: 0
    }
  },
  created() {
    this.showContinueBtn = false;
  },
  activated() {
    this.showContinueBtn = false;
  },
  computed: {
    isExcel() {
      return this.name === 'excel';
    },
    isXmind() {
      return this.name === 'xmind';
    },
    projectId() {
      return getCurrentProjectID();
    }
  },
  methods: {
    open(name) {
      this.dialogVisible = true;
      this.name = name;
    },
    close() {
      this.dialogVisible = false;
      this.init();
    },
    init() {
      this.fileList = [];
      this.showContinueBtn = false;
      this.errList = [];
      this.uploadIgnoreError = false;
      this.lastFile = null;
    },
    uploadValidateXmind(file) {
      let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);

      if (this.isExcel && suffix != 'xls' && suffix != 'xlsx') {
        this.$error(this.$t('test_track.case.import.upload_limit_format'), false);
        return false;
      } else if (this.isXmind && suffix != 'xmind') {
        this.$error(this.$t('test_track.case.import.upload_xmind_format'), false);
        return false;
      }

      if (file.size / 1024 / 1024 > 100) {
        this.$error(this.$t('test_track.case.import.upload_limit_size'), false);
        return false;
      }
      this.isLoading = true;
      this.errList = [];
      return true;
    },
    handleChange(file, fileList) {
      this.$refs.caseUpload.clearFiles();
    },
    handleExceed(files, fileList) {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'), false);
    },
    handleProgress(event, file, fileList) {
      this.progressFlag = true;
      this.loadProgress = parseInt(event.percent);
      if (this.loadProgress >= 100) {
        this.loadProgress = 100
        setTimeout( () => {this.progressFlag = false}, 1000) // 一秒后关闭进度条
      }
    },
    handleError(err, file, fileList) {
      this.isLoading = false;
      this.$error(err.message, false);
    },
    downloadXmindTemplate() {
      let uri = '/test/case/export/template/';
      if (this.isXmind) {
        uri = '/test/case/export/xmind/template/';
      }
      this.$fileDownload(uri + getCurrentProjectID() + '/' + this.importType);
    },
    addFile(file) {
      this.lastFile = file.file;
      this.progressFlag = true;
      this.loadProgress = 0;
      let process = setInterval(() => {
        this.loadProgress = this.loadProgress + Math.floor(Math.random()* 10)
        if (this.loadProgress >= 100) {
          this.loadProgress = 100
          setTimeout(() => {
            this.progressFlag = false;
          }, 1000)
          clearInterval(process);
        }
      }, 100);
    },
    handleRemove(file, fileList) {
      this.lastFile = null;
      this.showContinueBtn = false;
    },
    upload(isIgnore) {
      this.isLoading = false;
      let param = {
        projectId: getCurrentProjectID(),
        userId: getCurrentUserId(),
        importType: this.importType,
        versionId: this.versionId,
        ignore: isIgnore
      };
      if (this.lastFile == null || this.lastFile == undefined) {
        this.$warning(this.$t('test_track.case.import.import_file_tips'), false);
        return;
      }
      this.loading = true;
      this.$fileUpload('/test/case/import', this.lastFile, param)
        .then(response => {
          this.loading = false;
          let res = response.data;
          if (isIgnore) {
            this.$success(this.$t('test_track.case.import.success'), false);
            this.close();
            this.$emit("refreshAll");
          } else {
            if (res.success) {
              this.$success(this.$t('test_track.case.import.success'), false);
              this.close();
              this.$emit("refreshAll");
            } else {
              this.$error(this.$t('test_track.case.import.error'), false);
              this.errList = res.errList;
              this.showContinueBtn = true;
            }
          }
        }).catch(() => {
          this.loading = false;
        });
    },
    changeVersion(data) {
      this.versionId = data;
    }
  }
}
</script>

<style scoped>
.case-import :deep(.el-dialog){
  height: 570px;
}

.download-template {
  padding-top: 0px;
  padding-bottom: 10px;
}

.import-row {
  padding-top: 20px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  flex: none;
  order: 0;
  flex-grow: 0;
}

.testcase-import-img {
  width: 614px;
  height: 312px;
  size: 200px;
}

.el-dialog__wrapper.case-import :deep(.el-dialog__body) {
  padding: 0px 0px 6px 0px!important;
}

.download-tips {
  height: 40px;
  background: linear-gradient(0deg, rgba(120, 56, 135, 0.15), rgba(120, 56, 135, 0.15)), #FFFFFF;
}

.download-tips {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
  flex: none;
  order: 0;
  flex-grow: 0;
}

.download-tips-content {
  position: relative;
  left: 30px;
  top: 8px;
}

.download-tips-content a {
  padding-bottom: 4px;
}

.version-select :deep(.el-select.el-select--small) {
  width: 100%!important;
}

.case-import-upload :deep(.el-upload.el-upload--text){
  height: 156px;
  width: 100%;
}

.case-import-upload :deep(.el-upload-dragger) {
  width: 100%;
  height: 156px;
}

.case-import-upload :deep(.el-upload__text) {
  display: block;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  line-height: 22px;
  text-align: center;
  flex: none;
  order: 0;
  flex-grow: 0;
  margin-top: 30px;
}

.upload-drag-tips {
  color: #1F2329;
  font-size: 14px;
}

.upload-file-tips {
  color: #8F959E;
  font-size: 12px;
  margin-top: 7px;
}

.change-file-content {
  color: #783887;
  font-size: 14px;
  margin-top: 5px;
}

.el-button--small {
  min-width: 80px;
  height: 32px;
  border-radius: 4px;
  font-size: 14px;
}

.case-upload-progress :deep(.el-progress-bar__outer){
  height: 4px!important;
  width: 240px;
  margin-left: 150px;
  margin-top: 10px;
}

.case-upload-progress :deep(.el-progress-bar__inner){
  background-color: #AA4FBF;;
}
</style>

<style>
.error-popover {
  float: left;
  margin-top: 5px;
}
</style>
