<template>
  <el-dialog width="50%" :close-on-click-modal="false" :title="$t('api_test.jar_config.title')" :visible.sync="visible" class="jar-import" @close="close">
    <div v-loading="result.loading">
      <el-form :model="currentConfig" :rules="rules" label-width="100px" v-loading="result.loading" ref="form">
        <el-row>
          <el-col :span="12">
            <el-form-item :label="$t('commons.name')" prop="name">
              <el-input size="small" v-model="currentConfig.name" clearable show-word-limit/>
            </el-form-item>
            <el-form-item :label="$t('commons.project')" prop="description">
              <el-input :disabled="readOnly" v-model="currentConfig.description"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 4}"
                        :rows="2"
                        :placeholder="$t('commons.input_content')"/>
            </el-form-item>

          </el-col>

          <el-col :span="1">
            <el-divider direction="vertical"/>
          </el-col>

          <el-col :span="11">
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
              <div class="el-upload__tip" slot="tip">{{$t('api_test.api_import.file_size_limit')}}</div>
            </el-upload>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <template v-slot:footer>
      <div class="dialog-footer">
        <ms-dialog-footer @cancel="close" @confirm="save"/>
      </div>
    </template>

  </el-dialog>
</template>

<script>
  import {listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";
  import MsDialogFooter from "../../../common/components/MsDialogFooter";
  // todo
  export default {
    name: "MsJarConfigEdit",
    components: {MsDialogFooter},
    data() {
      return {
        visible: false,
        result: {},
        currentConfig: {
          name: '',
          description: '',
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
      }
    },

    methods: {
      open(config) {
        this.currentConfig = {
          name: '',
          description: '',
          fileName: '',
        };
        if (config) {
          if (config.fileName) {
            this.fileList = [{name: config.fileName}];
          } else {
            this.fileList = [];
          }
          Object.assign(this.currentConfig, config);
        }
        this.visible = true;
        listenGoBack(this.close);
      },
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
        if (suffix != 'jar') {
          this.$warning(this.$t('api_test.api_import.suffixFormatErr'));
          return false;
        }
        if (file.size / 1024 / 1024 > 30) {
          this.$warning(this.$t('jar_config.upload_limit_size'));
          return false;
        }
        return true;
      },
      save() {
        this.$refs['form'].validate((valid) => {
          if (valid) {
            if (this.fileList <= 0) {
              this.$warning(this.$t('commons.please_upload'));
              return false;
            }
            this._save();
          } else {
            return false;
          }
        });
      },
      _save() {
        let url = this.currentConfig.id ? "/jar/update" : "/jar/add";
        this.result = this.$fileUpload(url, this.fileList[0], null, this.currentConfig, () => {
          this.$success(this.$t('commons.save_success'));
          this.$emit("refresh");
          this.close();
        });
      },
      clear() {
        this.$refs['form'].clearValidate();
        this.fileList = [];
      },
      close() {
        this.clear();
        removeGoBackListener(this.close);
        this.visible = false;
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

  .jar-upload  >>> .el-upload-dragger {
    width: 100%;
  }

  .el-form {
    border: solid #E1E1E1 1px;
    margin: 10px 0;
    padding: 30px 10px;
    border-radius: 3px;
  }

  .buttons {
    margin-top: 10px;
    margin-bottom: -10px;
    float: right;
  }

</style>
