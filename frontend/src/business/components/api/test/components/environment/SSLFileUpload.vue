<template>
  <el-dialog
    :title="$t('test_track.case.import.import_file')"
    :visible.sync="dialogVisible"
    append-to-body
    destroy-on-close
    width="500px"
    :before-close="handleClose">
    <el-form :model="currentConfig" label-width="100px" v-loading="result.loading" ref="form">
      <el-row>
        <el-form-item :label="$t('commons.password')" prop="password">
          <el-input size="small" v-model="currentConfig.password" clearable show-password/>
        </el-form-item>
        <el-form-item>
          <el-upload
            class="jar-upload"
            drag
            action="#"
            :http-request="upload"
            :limit="1"
            :beforeUpload="uploadValidate"
            :on-remove="handleRemove"
            :on-exceed="handleExceed"
            :file-list="fileList"
            ref="fileUpload">
            <i class="el-icon-upload"></i>
            <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
            <div class="el-upload__tip" slot="tip">{{$t('api_test.api_import.file_size_limit')}}，支持p12,jks,pfx格式</div>
          </el-upload>
        </el-form-item>
        <el-col>
          <div class="buttons">
            <el-button type="primary" size="small" @click="save('add')">{{$t('commons.save')}}</el-button>
          </div>
        </el-col>

      </el-row>
    </el-form>
  </el-dialog>
</template>

<script>
  import {getUUID} from "@/common/js/utils";

  export default {
    name: "SSLFileUpload",
    data() {
      return {
        visible: false,
        dialogVisible: false,
        result: {},
        currentConfig: {
          password: '',
          fileName: '',
        },
        rules: {
          name: [
            {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
            {max: 60, message: this.$t('commons.input_limit', [1, 60]), trigger: 'blur'}
          ],
          description: [
            {max: 250, message: this.$t('commons.input_limit', [1, 250]), trigger: 'blur'}
          ],
        },
        fileList: []
      }
    },
    props: {
      readOnly: {
        type: Boolean,
        default: false
      },
      config: {
        type: Object,
        default() {
          return {};
        }
      },
      sslConfig: {},
      callback: {
        type: Function
      },
    },
    watch: {
      config() {
        this.currentConfig = {
          id: '',
          name: '',
          fileName: ''
        };
        if (this.config.fileName) {
          this.fileList = [{name: this.config.fileName}];
        } else {
          this.fileList = [];
        }
        Object.assign(this.currentConfig, this.config);
      }
    },
    mounted() {
      Object.assign(this.currentConfig, this.config);
    },
    methods: {
      upload(file) {
        this.fileList.push(file.file)
      },
      handleExceed(files, fileList) {
        this.$warning(this.$t('test_track.case.import.upload_limit_count'));
      },
      handleRemove(file, fileList) {
        this.fileList = [];
      },
      uploadValidate(file, fileList) {
        let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
        if (suffix !== 'jks' && suffix !== 'p12' && suffix !== 'pfx') {
          this.$warning(this.$t('api_test.api_import.suffixFormatErr'));
          return false;
        }
        if (file.size / 1024 / 1024 > 30) {
          this.$warning(this.$t('jar_config.upload_limit_size'));
          return false;
        }
        if (this.sslConfig.files) {
          let isFlag = false;
          this.sslConfig.files.forEach(item => {
            if (item && item.name === file.name) {
              isFlag = true;
            }
          })
          if (isFlag) {
            this.$warning("文件已经存在！");
            return false;
          }
        }
        return true;
      },
      save(type) {
        this.$refs['form'].validate((valid) => {
          if (valid) {
            if (this.fileList <= 0) {
              this.$warning(this.$t('commons.please_upload'));
              return;
            }
            if (this.callback) {
              this.dialogVisible = false;
              this.callback(this.currentConfig, this.fileList[0]);
            }
          } else {
            return false;
          }
        });
      },
      clear() {
        this.currentConfig.password = "";
        this.currentConfig.id = "";
        this.fileList = [];
      },
      open(row) {
        this.clear();
        if (row) {
          this.currentConfig.password = row.password;
          this.currentConfig.id = row.id;
          this.fileList.push({name: row.name});
        }
        this.dialogVisible = true;
      },
      handleClose() {
        this.dialogVisible = false;
      }
    }
  }
</script>

<style scoped>

  .el-divider {

    height: 200px;
  }

  .jar-upload {
    text-align: center;
    margin: auto 0;
  }

  .jar-upload >>> .el-upload {
    width: 100%;
    max-width: 350px;
  }

  .jar-upload >>> .el-upload-dragger {
    width: 100%;
  }

  .el-form {
    border: solid #E1E1E1 1px;
    margin: 10px 0;
    padding: 30px 20px;
    border-radius: 3px;
  }

  .buttons {
    margin-top: 10px;
    margin-bottom: -10px;
    float: right;
  }

</style>
