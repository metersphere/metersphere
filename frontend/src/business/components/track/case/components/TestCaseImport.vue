<template>

  <div>
    <el-tooltip class="item" effect="dark" content="导入用例" placement="right">
      <el-button type="info" icon="el-icon-upload2" size="mini" circle
        @click="dialogVisible = true"></el-button>
    </el-tooltip>

    <el-dialog width="30%" title="导入测试用例" :visible.sync="dialogVisible"
               @close="init">

      <el-row>
        <el-link type="primary" class="download-template"
                 href="/test/case/export/template">下载模版</el-link>
      </el-row>

      <el-row>
        <el-upload
          class="upload-demo"
          :action="'/test/case/import/' + projectId"
          :on-preview="handlePreview"
          multiple
          :limit="1"
          :on-exceed="handleExceed"
          :beforeUpload="UploadValidate"
          :on-success="handleSuccess"
          :on-error="handleError"
          :file-list="fileList">
          <template v-slot:trigger>
            <el-button size="mini" type="success" plain>点击上传</el-button>
          </template>
          <template v-slot:tip>
            <div class="el-upload__tip">只能上传xls/xlsx文件，且不超过20M</div>
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
    export default {
      name: "TestCaseImport",
      components: {ElUploadList},
      data() {
        return {
          dialogVisible: false,
          fileList: [],
          errList: []
        }
      },
      props: {
        projectId: {
          type: String
        }
      },
      methods: {
        handlePreview(file) {
          console.log("init");
          this.init();
        },
        handleExceed(files, fileList) {
          this.$message.warning(`当前限制选择 1 个文件，本次选择了 ${files.length} 个文件`);
        },
        UploadValidate(file) {
          var suffix =file.name.substring(file.name.lastIndexOf('.') + 1);
          if (suffix != 'xls' && suffix != 'xlsx') {
            this.$message({
              message: '上传文件只能是 xls、xlsx格式!',
              type: 'warning'
            });
            return false;
          }

          if (file.size / 1024 / 1024 > 20) {
            this.$message({
              message: '上传文件大小不能超过 20MB!',
              type: 'warning'
            });
            return false;
          }
          return true;
        },
        handleSuccess(response) {
          let res = response.data;
          if (res.success) {
            this.$message.success("导入成功！");
            this.dialogVisible = false;
            this.$emit("refresh");
          } else {
            this.errList = res.errList;
          }
          this.fileList = [];
        },
        handleError(err, file, fileList) {
          this.$message.error(err.message);
        },
        init() {
          this.fileList = [];
          this.errList = [];
        }
      }
    }
</script>

<style>

  .el-dialog__body {
    padding-top: 10px;
  }

  .download-template {
    padding-top: 0px;
    padding-bottom: 10px;
  }

</style>

<style scoped>


</style>
