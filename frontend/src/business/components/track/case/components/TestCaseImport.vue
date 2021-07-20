<template>
  <el-dialog class="testcase-import" :title="$t('test_track.case.import.case_import')" :visible.sync="dialogVisible"
             @close="close">

    <el-tabs v-model="activeName" @tab-click="clickTabs" simple>
      <el-tab-pane :label="$t('test_track.case.import.excel_title')" name="excelImport">
        <el-row class="import-row" style="margin-left: 34px">
          <el-radio v-model="importType" label="Create">导入新建</el-radio>
          <el-radio v-model="importType" label="Update">导入更新</el-radio>
        </el-row>
        <el-row class="import-row">
          <div class="el-step__icon is-text" style="background-color: #C9E6F8;border-color: #C9E6F8;margin-right: 10px">
            <div class="el-step__icon-inner">1</div>
          </div>
          <label class="ms-license-label">{{$t('test_track.case.import.import_desc')}}</label>
        </el-row>
        <el-row class="import-row">
          <div style="margin-left: 34px">
            <div v-if="importType === 'Create'">
              项目设置中“测试用例自定义ID” 开关开启时ID为必填项
            </div>
            <div v-else >
              导入更新时ID为必填项
            </div>

          </div>
        </el-row>
        <el-row class="import-row">
          <div class="el-step__icon is-text"
               style="background-color: #C9E6F8;border-color: #C9E6F8;margin-right: 10px ">
            <div class="el-step__icon-inner">2</div>
          </div>
          <label class="ms-license-label">{{$t('test_track.case.import.import_file')}}</label>
        </el-row>
        <el-row>
          <el-link type="primary" class="download-template" style="margin: 20px 34px;"
                   @click="downloadTemplate"
          >{{ $t('test_track.case.import.download_template') }}
          </el-link>
        </el-row>
        <el-row>
          <el-upload
            v-loading="result.loading"
            :element-loading-text="$t('test_track.case.import.importing')"
            element-loading-spinner="el-icon-loading"
            class="upload-demo"
            multiple
            :limit="1"
            action=""
            :on-exceed="handleExceed"
            :beforeUpload="uploadValidate"
            :on-error="handleError"
            :show-file-list="false"
            :http-request="upload"
            :file-list="fileList">
            <template v-slot:trigger>
              <el-button size="mini" type="success" plain>{{$t('test_track.case.import.click_upload')}}</el-button>
            </template>
            <template v-slot:tip>
              <div class="el-upload__tip">{{$t('test_track.case.import.upload_limit')}}</div>
            </template>
          </el-upload>
        </el-row>


        <el-row>
          <ul>
            <li v-for="errFile in errList" :key="errFile.rowNum">
              {{ errFile.errMsg }}
            </li>
          </ul>
        </el-row>

        <el-row style="text-align: right" v-if="showExcelImportContinueBtn">
          <div style="margin-right: 20px;margin-bottom: 10px;">
            <el-checkbox v-model="uploadIgnoreError">{{ $t('test_track.case.import.ignore_error') }}</el-checkbox>
          </div>
          <el-button type="primary" @click="uploadContinue(false)">{{ $t('test_track.case.import.continue_upload') }}
          </el-button>
          <el-button @click="close">{{ $t('commons.cancel') }}</el-button>
        </el-row>

      </el-tab-pane>
      <!-- Xmind 导入 -->
      <el-tab-pane :label="$t('test_track.case.import.xmind_title')" name="xmindImport" style="border: 0px">
        <el-row class="import-row" style="margin-left: 34px">
          <el-radio v-model="importType" label="Create">导入新建</el-radio>
          <el-radio v-model="importType" label="Update">导入更新</el-radio>
        </el-row>
        <el-row class="import-row">
          <div class="el-step__icon is-text" style="background-color: #C9E6F8;border-color: #C9E6F8;margin-right: 10px">
            <div class="el-step__icon-inner">1</div>
          </div>
          <label class="ms-license-label">{{$t('test_track.case.import.import_desc')}}</label>
        </el-row>
        <el-row class="import-row" style="margin-left: 34px">
          <div v-if="importType === 'Create'">
            项目设置中“测试用例自定义ID” 开关开启时ID为必填项
          </div>
          <div v-else >
            导入更新时ID为必填项
          </div>
        </el-row>
<!--        <el-row class="import-row">-->
<!--          <el-card :body-style="{ padding: '0px' }">-->
<!--            <img src="../../../../../assets/xmind.jpg"-->
<!--                 class="testcase-import-img">-->
<!--          </el-card>-->

<!--        </el-row>-->
        <el-row class="import-row">
          <div class="el-step__icon is-text"
               style="background-color: #C9E6F8;border-color: #C9E6F8;margin-right: 10px ">
            <div class="el-step__icon-inner">2</div>
          </div>
          <label class="ms-license-label">{{$t('test_track.case.import.import_file')}}</label>
        </el-row>
        <el-row class="import-row">
          <el-link type="primary" class="download-template" style="margin: 0px 34px;"
                   @click="downloadXmindTemplate"
          >{{$t('test_track.case.import.download_template')}}
          </el-link>
        </el-row>
        <el-row class="import-row">
          <el-upload
            v-loading="result.loading"
            :element-loading-text="$t('test_track.case.import.importing')"
            element-loading-spinner="el-icon-loading"
            class="upload-demo"
            multiple
            :limit="1"
            action=""
            :on-exceed="handleExceed"
            :beforeUpload="uploadValidateXmind"
            :on-error="handleError"
            :show-file-list="false"
            :http-request="uploadXmind"
            :file-list="fileList">
            <template v-slot:trigger>
              <el-button size="mini" type="success" plain>{{$t('test_track.case.import.click_upload')}}</el-button>
            </template>
            <template v-slot:tip>
              <div class="el-upload__tip">{{$t('test_track.case.import.upload_xmind')}}</div>
            </template>
          </el-upload>
        </el-row>
        <el-row>
          <ul>
            <li v-for="errFile in xmindErrList" :key="errFile.rowNum">
              {{ errFile.errMsg }}
            </li>
          </ul>
        </el-row>
        <el-row style="text-align: right" v-if="showXmindImportContinueBtn">
          <div style="margin-right: 20px;margin-bottom: 10px;">
            <el-checkbox v-model="uploadXmindIgnoreError">{{ $t('test_track.case.import.ignore_error') }}</el-checkbox>
          </div>
          <el-button type="primary" @click="uploadContinue(true)">{{ $t('test_track.case.import.continue_upload') }}
          </el-button>
          <el-button @click="close">{{ $t('commons.cancel') }}</el-button>
        </el-row>
      </el-tab-pane>

    </el-tabs>
  </el-dialog>
</template>

<script>
  import ElUploadList from "element-ui/packages/upload/src/upload-list";
  import MsTableButton from '../../../../components/common/components/MsTableButton';
  import {getCurrentProjectID, listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";
  import {TokenKey} from '../../../../../common/js/constants';
  import axios from "axios";

  export default {
    name: "TestCaseImport",
    components: {ElUploadList, MsTableButton},
    data() {
      return {
        result: {},
        activeName: 'excelImport',
        dialogVisible: false,
        fileList: [],
        lastXmindFile: null,
        lastExcelFile: null,
        importType:"Create",
        errList: [],
        xmindErrList: [],
        isLoading: false,
        isUpdated: false,
        clickTabsName: "",
        showExcelImportContinueBtn: false,
        showXmindImportContinueBtn: false,
        uploadIgnoreError: false,
        uploadXmindIgnoreError: false,
      }
    },
    created() {
      this.showExcelImportContinueBtn = false;
      this.showXmindImportContinueBtn = false;
    },
    activated() {
      this.showExcelImportContinueBtn = false;
      this.showXmindImportContinueBtn = false;
    },
    methods: {
      handleExceed(files, fileList) {
        this.$warning(this.$t('test_track.case.import.upload_limit_count'));
      },
      clickTabs(tab, event) {
        this.clickTabsName = tab.name;
      },
      uploadValidate(file) {
        let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
        if (suffix != 'xls' && suffix != 'xlsx') {
          this.$warning(this.$t('test_track.case.import.upload_limit_format'));
          return false;
        }

        if (file.size / 1024 / 1024 > 50) {
          this.$warning(this.$t('test_track.case.import.upload_limit_size'));
          return false;
        }
        this.isLoading = true;
        this.errList = [];
        this.xmindErrList = [];
        return true;
      },
      uploadValidateXmind(file) {
        let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
        if (suffix != 'xmind') {
          this.$warning(this.$t('test_track.case.import.upload_xmind_format'));
          return false;
        }

        if (file.size / 1024 / 1024 > 50) {
          this.$warning(this.$t('test_track.case.import.upload_limit_size'));
          return false;
        }
        this.isLoading = true;
        this.errList = [];
        this.xmindErrList = [];
        return true;
      },
      handleError(err, file, fileList) {
        this.isLoading = false;
        this.$error(err.message);
      },
      open() {
        listenGoBack(this.close);
        this.projectId = getCurrentProjectID();
        this.dialogVisible = true;
      },
      close() {
        removeGoBackListener(this.close);
        this.dialogVisible = false;
        this.fileList = [];
        this.showExcelImportContinueBtn = false;
        this.showXmindImportContinueBtn = false;
        this.errList = [];
        this.xmindErrList = [];

        //通过excel导入更新过数据的话就刷新页面
        if (this.isUpdated === true) {
          this.$emit("refreshAll");
          this.isUpdated = false;
        }
      },
      downloadTemplate() {

        this.$fileDownload('/test/case/export/template/'+this.projectId+"/"+this.importType);
      },
      downloadXmindTemplate() {
        axios.get('/test/case/export/xmindTemplate/'+this.projectId+"/"+this.importType, {responseType: 'blob'})
          .then(response => {
            let fileName = window.decodeURI(response.headers['content-disposition'].split('=')[1]);
            let link = document.createElement("a");
            link.href = window.URL.createObjectURL(new Blob([response.data]));
            link.download = fileName;
            link.click();
          });
      },
      upload(file) {
        this.lastExcelFile = file.file;
        this.fileList.push(file.file);
        this.isLoading = false;

        let user = JSON.parse(localStorage.getItem(TokenKey));

        this.result = this.$fileUpload('/test/case/import/' + this.projectId + '/' + user.id+'/'+this.importType, file.file, null, {}, response => {
          let res = response.data;
          if (res.success) {
            this.$success(this.$t('test_track.case.import.success'));
            this.dialogVisible = false;
            this.$emit("refreshAll");
            this.lastXmindFile = null;
            this.lastExcelFile = null;
            this.showExcelImportContinueBtn = false;
            this.showXmindImportContinueBtn = false;
          } else {
            this.errList = res.errList;
            this.isUpdated = res.isUpdated;
            this.showExcelImportContinueBtn = true;
          }
          this.fileList = [];
        }, erro => {
          this.fileList = [];
          this.lastXmindFile = null;
          this.lastExcelFile = null;
        });
      },
      uploadContinue(isImportXmind) {
        this.isLoading = false;
        let user = JSON.parse(localStorage.getItem(TokenKey));
        let file = null;
        if (isImportXmind) {
          this.uploadXmindIgnoreError = true;
          file = this.lastXmindFile;
        } else {
          this.uploadIgnoreError = true;
          file = this.lastExcelFile;
        }
        this.result = this.$fileUpload('/test/case/importIgnoreError/' + this.projectId + '/' + user.id+'/'+this.importType, file, null, {}, response => {
          let res = response.data;
          this.$success(this.$t('test_track.case.import.success'));
          this.dialogVisible = false;
          this.$emit("refreshAll");
          this.fileList = [];
          this.lastXmindFile = null;
          this.lastExcelFile = null;
          this.showExcelImportContinueBtn = false;
          this.showXmindImportContinueBtn = false;
          this.uploadIgnoreError = false;
          this.uploadXmindIgnoreError = false;
        }, erro => {
          this.fileList = [];
          this.lastXmindFile = null;
          this.lastExcelFile = null;
          this.uploadIgnoreError = false;
          this.uploadXmindIgnoreError = false;
        });
      },
      uploadXmind(file) {
        this.isLoading = false;
        this.fileList.push(file.file);
        this.lastXmindFile = file.file;
        let user = JSON.parse(localStorage.getItem(TokenKey));

        this.result = this.$fileUpload('/test/case/import/' + this.projectId + '/' + user.id+'/'+this.importType, file.file, null, {}, response => {
          let res = response.data;
          if (res.success) {
            this.$success(this.$t('test_track.case.import.success'));
            this.dialogVisible = false;
            this.$emit("refreshAll");
            this.lastXmindFile = null;
            this.lastExcelFile = null;
            this.showExcelImportContinueBtn = false;
            this.showXmindImportContinueBtn = false;
          } else {
            this.xmindErrList = res.errList;
            this.showXmindImportContinueBtn = true;
          }
          this.fileList = [];
        }, erro => {
          this.fileList = [];
        });
      }
    }
  }
</script>

<style>
</style>

<style scoped>

  .download-template {
    padding-top: 0px;
    padding-bottom: 10px;
  }

  .import-row {
    padding-top: 20px;
  }

  .testcase-import >>> .el-dialog {
    width: 650px;
  }

  .testcase-import-img {
    width: 614px;
    height: 312px;
    size: 200px;
  }


</style>
