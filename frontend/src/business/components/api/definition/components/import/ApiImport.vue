<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.api_import.title')" width="30%"
             :visible.sync="visible" class="api-import" v-loading="result.loading" @close="close">

    <div class="header-bar">
      <div>{{$t('api_test.api_import.data_format')}}</div>
      <el-radio-group v-model="selectedPlatformValue">
        <el-radio v-for="(item, index) in platforms" :key="index" :label="item.value">{{item.name}}</el-radio>
      </el-radio-group>

      <div class="operate-button">
        <el-button class="save-button" type="primary" plain @click="save">
          {{$t('commons.save')}}
        </el-button>
        <el-button class="cancel-button" type="warning" plain @click="visible = false">
          {{$t('commons.cancel')}}
        </el-button>
      </div>
    </div>

    <el-form :model="formData" :rules="rules" label-width="100px" v-loading="result.loading" ref="form">

      <el-upload
        v-if="selectedPlatformValue != 'Swagger2' || (selectedPlatformValue == 'Swagger2' && !swaggerUrlEable)"
        class="api-upload"
        drag
        action=""
        :http-request="upload"
        :limit="1"
        :beforeUpload="uploadValidate"
        :on-remove="handleRemove"
        :file-list="fileList"
        :on-exceed="handleExceed"
        multiple>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
        <div class="el-upload__tip" slot="tip">{{$t('api_test.api_import.file_size_limit')}}</div>
      </el-upload>

      <el-form-item :label="'Swagger URL'" prop="wgerUrl" v-if="isSwagger2 && swaggerUrlEable" class="swagger-url">
        <el-input size="small" v-model="formData.swaggerUrl" clearable show-word-limit/>
      </el-form-item>

      <el-form-item v-if="isSwagger2" class="swagger-enable" :class="{'swagger-url-disable': !swaggerUrlEable}">
        <el-switch
          v-model="swaggerUrlEable"
          :active-text="$t('api_test.api_import.swagger_url_import')">
        </el-switch>
      </el-form-item>

    </el-form>

    <div class="format-tip">
      <div>
        <span>{{$t('api_test.api_import.tip')}}：{{selectedPlatform.tip}}</span>
      </div>
      <div>
        <span>{{$t('api_test.api_import.export_tip')}}：{{selectedPlatform.exportTip}}</span>
      </div>
    </div>

  </el-dialog>
</template>

<script>
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import {listenGoBack, removeGoBackListener} from "@/common/js/utils";
  import {getCurrentProjectID} from "../../../../../../common/js/utils";

  export default {
    name: "ApiImport",
    components: {MsDialogFooter},
    props: {
      saved: {
        type: Boolean,
        default: true,
      }
    },
    data() {
      return {
        visible: false,
        swaggerUrlEable: false,
        showEnvironmentSelect: true,
        platforms: [
          {
            name: 'Metersphere',
            value: 'Metersphere',
            tip: this.$t('api_test.api_import.ms_tip'),
            exportTip: this.$t('api_test.api_import.ms_export_tip'),
            suffixes: new Set(['json'])
          },
          {
            name: 'Postman',
            value: 'Postman',
            tip: this.$t('api_test.api_import.postman_tip'),
            exportTip: this.$t('api_test.api_import.post_export_tip'),
            suffixes: new Set(['json'])
          },
          {
            name: 'Swagger',
            value: 'Swagger2',
            tip: this.$t('api_test.api_import.swagger_tip'),
            exportTip: this.$t('api_test.api_import.swagger_export_tip'),
            suffixes: new Set(['json'])
          }
        ],
        selectedPlatform: {},
        selectedPlatformValue: 'Metersphere',
        result: {},
        projects: [],
        environments: [],
        useEnvironment: false,
        formData: {
          file: undefined,
          swaggerUrl: ''
        },
        rules: {},
        currentModule: {},
        fileList: []
      }
    },
    activated() {
      this.selectedPlatform = this.platforms[0];
    },
    watch: {
      selectedPlatformValue() {
        for (let i in this.platforms) {
          if (this.platforms[i].value === this.selectedPlatformValue) {
            this.selectedPlatform = this.platforms[i];
            break;
          }
        }
      },
    },
    computed: {
      isSwagger2() {
        return this.selectedPlatformValue === 'Swagger2';
      }
    },
    methods: {
      open(module) {
        this.currentModule = module;
        this.visible = true;
        listenGoBack(this.close);
      },
      upload(file) {
        this.formData.file = file.file;
      },
      handleExceed(files, fileList) {
        this.$warning(this.$t('test_track.case.import.upload_limit_count'));
      },
      handleRemove(file, fileList) {
        this.formData.file = undefined;
      },
      uploadValidate(file, fileList) {
        let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
        if (this.selectedPlatform.suffixes && !this.selectedPlatform.suffixes.has(suffix)) {
          this.$warning(this.$t('api_test.api_import.suffixFormatErr'));
          return false;
        }
        if (file.size / 1024 / 1024 > 20) {
          this.$warning(this.$t('test_track.case.import.upload_limit_size'));
          return false;
        }
        return true;
      },
      save() {
        this.$refs.form.validate(valid => {
          if (valid) {
            if ((this.selectedPlatformValue != 'Swagger2' || (this.selectedPlatformValue == 'Swagger2' && !this.swaggerUrlEable)) && !this.formData.file) {
              this.$warning(this.$t('commons.please_upload'));
              return;
            }
            let param = this.buildParam();
            this.result = this.$fileUpload('/api/definition/import', param.file, null, this.buildParam(), response => {
              let res = response.data;
              this.$success(this.$t('test_track.case.import.success'));
              this.visible = false;
              this.$emit('refresh', res);
            });
          } else {
            return false;
          }
        });
      },
      buildParam() {
        let param = {};
        Object.assign(param, this.formData);
        param.platform = this.selectedPlatformValue;
        param.saved = this.saved;
        if (this.currentModule) {
          param.moduleId = this.currentModule.id;
          param.modulePath = this.currentModule.path;
        }
        param.projectId = getCurrentProjectID();
        if (!this.swaggerUrlEable) {
          param.swaggerUrl = undefined;
        }
        return param;
      },
      close() {
        this.formData = {
          file: undefined,
          swaggerUrl: ''
        };
        this.fileList = [];
        removeGoBackListener(this.close);
        this.visible = false;
      }
    }
  }
</script>

<style scoped>

  .api-import >>> .el-dialog {
    min-width: 700px;
  }

  .format-tip {
    background: #EDEDED;
  }

  .api-upload {
    text-align: center;
    margin: auto 0;
  }

  .api-upload >>> .el-upload {
    width: 100%;
    max-width: 350px;
  }

  .api-upload >>> .el-upload-dragger {
    width: 100%;
  }

  .el-radio-group {
    margin: 10px 0;
  }

  .header-bar, .format-tip, .el-form {
    border: solid #E1E1E1 1px;
    margin: 10px 0;
    padding: 10px;
    border-radius: 3px;
  }

  .header-bar {
    padding: 10px 30px;
  }

  .api-import >>> .el-dialog__body {
    padding: 15px 25px;
  }

  .operate-button {
    float: right;
  }

  .save-button {
    margin-left: 10px;
  }

  .el-form {
    padding: 30px 10px;
  }

  .dialog-footer {
    float: right;
  }

  .swagger-url-disable {
    margin-top: 10px;

    margin-left: 80px;
  }

</style>
