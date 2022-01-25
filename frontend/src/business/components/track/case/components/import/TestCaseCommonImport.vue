<template>
  <div>

    <el-row class="import-row" style="margin-left: 34px">
      <el-radio v-model="importType" label="Create">{{$t('test_track.case.import.import_create')}}</el-radio>
      <el-radio v-model="importType" label="Update">{{$t('test_track.case.import.import_update')}}</el-radio>
    </el-row>

    <el-row class="import-row">
      <div class="el-step__icon is-text" style="background-color: #C9E6F8;border-color: #C9E6F8;margin-right: 10px">
        <div class="el-step__icon-inner">1</div>
      </div>
      <label class="ms-license-label">{{$t('test_track.case.import.import_desc')}}</label>
    </el-row>

    <el-row class="import-row" style="margin-left: 34px">
      <div v-if="importType === 'Create'">
        {{$t('test_track.case.import.import_tip1')}}
      </div>
      <div v-else >
        {{$t('test_track.case.import.import_tip2')}}
      </div>
    </el-row>

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
        :http-request="addFile"
        :on-remove="handleRemove"
        :file-list="fileList">
        <template v-slot:trigger>
          <el-button size="mini" type="success" plain>{{$t('commons.please_select')}}</el-button>
          <version-select style="margin-left: 25px" v-xpack :project-id="projectId" @changeVersion="changeVersion"/>
        </template>
        <template v-slot:tip>
          <div v-if="isExcel" class="el-upload__tip">{{$t('test_track.case.import.upload_limit')}}</div>
          <div v-if="isXmind" class="el-upload__tip">{{$t('test_track.case.import.upload_xmind')}}</div>
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

    <el-row style="text-align: right" >
<!--      <div v-if="showContinueBtn" style="margin-right: 20px;margin-bottom: 10px;">-->
<!--        <el-checkbox :value="true" :disabled="true">{{ $t('test_track.case.import.ignore_error') }}</el-checkbox>-->
<!--      </div>-->
      <el-button v-if="showContinueBtn" type="primary" @click="upload(true)">
        {{ $t('test_track.case.import.continue_upload') }}
      </el-button>
      <el-button v-if="!showContinueBtn" type="primary" @click="upload(false)">
        {{ $t('test_track.case.import.click_upload') }}
      </el-button>
      <el-button @click="$emit('close')">{{ $t('commons.cancel') }}</el-button>
    </el-row>
  </div>

</template>

<script>
import {getCurrentProjectID, getCurrentUserId} from "@/common/js/utils";
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};

export default {
  name: "TestCaseCommonImport",
  components: {'VersionSelect': VersionSelect.default},
  props: ['tabName', 'name'],
  data() {
    return {
      result: {},
      fileList: [],
      errList: [],
      importType: 'Create',
      showContinueBtn: false,
      uploadIgnoreError: false,
      lastFile: null,
      versionId: null
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
        this.$warning(this.$t('test_track.case.import.upload_limit_format'));
        return false;
      } else if (this.isXmind && suffix != 'xmind') {
        this.$warning(this.$t('test_track.case.import.upload_xmind_format'));
        return false;
      }

      if (file.size / 1024 / 1024 > 100) {
        this.$warning(this.$t('test_track.case.import.upload_limit_size'));
        return false;
      }
      this.isLoading = true;
      this.errList = [];
      return true;
    },
    handleExceed(files, fileList) {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'));
    },
    handleError(err, file, fileList) {
      this.isLoading = false;
      this.$error(err.message);
    },
    downloadXmindTemplate() {
      let uri = '/test/case/export/template/';
      if (this.isXmind) {
        uri = '/test/case/export/xmindTemplate/';
      }
      this.$fileDownload(uri + getCurrentProjectID() + '/' + this.importType);
    },
    addFile(file) {
      this.lastFile = file.file;
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
        this.$warning(this.$t('test_track.case.import.import_file_tips'));
        return;
      }
      this.result = this.$fileUpload('/test/case/import',
        this.lastFile, null, param, response => {
        let res = response.data;
        if (isIgnore) {
          this.$success(this.$t('test_track.case.import.success'));
          this.$emit("close", res.isUpdated);
        } else {
          if (res.success) {
            this.$success(this.$t('test_track.case.import.success'));
            this.$emit("close", res.isUpdated);
          } else {
            this.errList = res.errList;
            this.showContinueBtn = true;
          }
        }
      });
    },
    changeVersion(data) {
      this.versionId = data;
    }
  }
}
</script>

<style scoped>

.download-template {
  padding-top: 0px;
  padding-bottom: 10px;
}

.import-row {
  padding-top: 20px;
}

.testcase-import-img {
  width: 614px;
  height: 312px;
  size: 200px;
}
</style>
