<template>
  <el-dialog class="user-import" :title="$t('commons.import_user')" :visible.sync="dialogVisible"
             @close="close">
        <el-row>
          <el-alert
            title="用户组只支持系统默认用户组"
            type="info"
            show-icon
            :closable="false"
          >
          </el-alert>
        </el-row>
        <el-row>
          <el-link type="primary" class="download-template"
                   @click="downloadTemplate"
          >{{$t('test_track.case.import.download_template')}}
          </el-link>
        </el-row>
        <el-row>
          <el-upload
            style="width: 100%"
            v-loading="result.loading"
            :element-loading-text="$t('test_track.case.import.importing')"
            element-loading-spinner="el-icon-loading"
            class="upload-demo"
            multiple
            drag
            :limit="1"
            action=""
            :on-exceed="handleExceed"
            :beforeUpload="uploadValidate"
            :on-error="handleError"
            :show-file-list="false"
            :http-request="upload"
            :file-list="fileList">
            <i class="el-icon-upload"></i>
            <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
            <div class="el-upload__tip" slot="tip">{{ $t('api_test.api_import.file_size_limit') }}</div>
          </el-upload>
        </el-row>
        <el-row>
          <ul>
            <li v-for="errFile in errList" :key="errFile.rowNum">
              {{errFile.errMsg}}
            </li>
          </ul>
        </el-row>
  </el-dialog>
</template>

<script>
import ElUploadList from "element-ui/packages/upload/src/upload-list";
import MsTableButton from '../../../../components/common/components/MsTableButton';
import {getCurrentProjectID, listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";
import {TokenKey} from '../../../../../common/js/constants';
import axios from "axios";

export default {
  name: "UserImport",
  components: {ElUploadList, MsTableButton},
  data() {
    return {
      result: {},
      activeName: 'excelImport',
      dialogVisible: false,
      fileList: [],
      errList: [],
      isLoading: false
    }
  },
  methods: {
    handleExceed(files, fileList) {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'));
    },
    uploadValidate(file) {
      let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
      if (suffix != 'xls' && suffix != 'xlsx') {
        this.$warning(this.$t('test_track.case.import.upload_limit_format'));
        return false;
      }
      if (file.size / 1024 / 1024 > 20) {
        this.$warning(this.$t('test_track.case.import.upload_limit_size'));
        return false;
      }
      this.isLoading = true;
      this.errList = [];
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
      this.errList = [];
    },
    downloadTemplate() {
      this.$fileDownload('/user/export/template');
    },
    upload(file) {
      this.isLoading = false;
      this.fileList.push(file.file);
      let user = JSON.parse(localStorage.getItem(TokenKey));

      this.result = this.$fileUpload('/user/import', file.file, null, {}, response => {
        let res = response.data;
        if (res.success) {
          this.$success(this.$t('commons.import_success'));
          this.dialogVisible = false;
          this.$emit("refreshAll");
        } else {
          this.errList = res.errList;
        }
        this.fileList = [];
      }, erro => {
        this.fileList = [];
      });
    },
  }
}
</script>

<style>
</style>

<style scoped>

.download-template {
  padding-top: 10px;
  padding-bottom: 5px;
}

.user-import >>> .el-dialog {
  width: 400px;
}

.user-import >>> .el-dialog__body {
  padding: 5px 20px;
}


</style>
