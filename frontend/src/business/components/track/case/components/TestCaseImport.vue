<template>

  <div>

    <el-dialog width="30%" :title="$t('test_track.case.import.case_import')" :visible.sync="dialogVisible"
               @close="init">

      <el-row>
        <el-link type="primary" class="download-template"
                 href="/test/case/export/template">{{$t('test_track.case.import.download_template')}}</el-link></el-row>
      <el-row>
        <el-upload
          v-loading="isLoading"
          element-loading-text="导入中"
          element-loading-spinner="el-icon-loading"
          class="upload-demo"
          :action="'/test/case/import/' + projectId"
          multiple
          :limit="1"
          :on-exceed="handleExceed"
          :beforeUpload="UploadValidate"
          :on-success="handleSuccess"
          :on-error="handleError"
          :show-file-list="false"
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

  </div>
</template>

<script>
    import ElUploadList from "element-ui/packages/upload/src/upload-list";
    import MsTableButton from '../../../../components/common/components/MsTableButton';

    export default {
      name: "TestCaseImport",
      components: {ElUploadList, MsTableButton},
      data() {
        return {
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
        UploadValidate(file) {
          var suffix =file.name.substring(file.name.lastIndexOf('.') + 1);
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
        handleSuccess(response) {
          this.isLoading = false;
          let res = response.data;
          if (res.success) {
            this.$success(this.$t('test_track.case.import.success'));
            this.dialogVisible = false;
            this.$emit("refresh");
          } else {
            this.errList = res.errList;
          }
          this.fileList = [];
        },
        handleError(err, file, fileList) {
          this.isLoading = false;
          this.$error(err.message);
        },
        init() {
          this.fileList = [];
          this.errList = [];
        },
        open() {
          this.dialogVisible = true;
        },
      }
    }
</script>

<style>

  .el-dialog__body {
    padding-top: 10px;
    padding-bottom: 10px;
  }

  .download-template {
    padding-top: 0px;
    padding-bottom: 10px;
  }

</style>

<style scoped>


</style>
