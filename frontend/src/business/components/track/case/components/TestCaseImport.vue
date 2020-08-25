<template>
    <el-dialog class="testcase-import" :title="$t('test_track.case.import.case_import')" :visible.sync="dialogVisible"
               @close="close">

      <el-row>
        <el-link type="primary" class="download-template"
                 @click="downloadTemplate"
                 >{{$t('test_track.case.import.download_template')}}</el-link></el-row>
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
            {{errFile.errMsg}}
          </li>
        </ul>
      </el-row>

    </el-dialog>
</template>

<script>
    import ElUploadList from "element-ui/packages/upload/src/upload-list";
    import MsTableButton from '../../../../components/common/components/MsTableButton';
    import {listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";

    export default {
      name: "TestCaseImport",
      components: {ElUploadList, MsTableButton},
      data() {
        return {
          result: {},
          dialogVisible: false,
          fileList: [],
          errList: [],
          isLoading: false
        }
      },
      props: {
        projectId: {
          type: String
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
          this.dialogVisible = true;
        },
        close() {
          removeGoBackListener(this.close);
          this.dialogVisible = false;
          this.fileList = [];
          this.errList = [];
        },
        downloadTemplate() {
          this.$fileDownload('/test/case/export/template');
        },
        upload(file) {
          this.isLoading = false;
          this.fileList.push(file.file);
          this.result = this.$fileUpload('/test/case/import/' + this.projectId, file.file, null, {}, response => {
            let res = response.data;
            if (res.success) {
              this.$success(this.$t('test_track.case.import.success'));
              this.dialogVisible = false;
              this.$emit("refresh");
            } else {
              this.errList = res.errList;
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

  .testcase-import >>> .el-dialog {
    width: 400px;
  }


</style>
