<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.api_import.title')" width="30%"
             :visible.sync="visible" class="api-import" v-loading="result.loading" @close="close"
             :destroy-on-close="true">

    <div class="header-bar">
      <div>{{ $t('api_test.api_import.data_format') }}</div>
      <el-radio-group v-model="selectedPlatformValue">
        <span v-for="(item, index) in platforms" :key="index">
          <el-radio v-if="!isScenarioModel || item.name != 'Swagger'" :label="item.value">{{ item.name }}</el-radio>
        </span>
      </el-radio-group>

      <div class="operate-button">
        <el-button class="save-button" type="primary" plain @click="save">
          {{ $t('commons.save') }}
        </el-button>
        <el-button class="cancel-button" type="warning" plain @click="visible = false">
          {{ $t('commons.cancel') }}
        </el-button>
      </div>
    </div>

    <el-form :model="formData" :rules="rules" label-width="100px" v-loading="result.loading" ref="form">
      <el-row>
        <el-col :span="11">
          <el-form-item :label="$t('commons.import_module')" prop="moduleId">
            <ms-select-tree size="small" :data="moduleOptions" :defaultKey="formData.moduleId" @getValue="setModule" :obj="moduleObj" clearable checkStrictly/>
          </el-form-item>
          <el-form-item v-if="!isScenarioModel&&showImportModel" :label="$t('commons.import_mode')" prop="modeId">
            <el-select size="small" v-model="formData.modeId" clearable style="width: 100%">
              <el-option v-for="item in modeOptions" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
          <el-form-item v-if="showTemplate">
            <el-link type="primary" class="download-template"
                     @click="downloadTemplate"
            >{{$t('test_track.case.import.download_template')}}
            </el-link>
          </el-form-item>
          <el-form-item v-if="isSwagger2">
            <el-switch
              v-model="swaggerUrlEnable"
              :active-text="$t('api_test.api_import.swagger_url_import')">
            </el-switch>
          </el-form-item>

        </el-col>
        <el-col :span="1">
          <el-divider direction="vertical"/>
        </el-col>
        <el-col :span="12" v-show="isSwagger2 && swaggerUrlEnable" style="margin-top: 40px">
          <el-form-item :label="'Swagger URL'" prop="swaggerUrl" class="swagger-url">
            <el-input size="small" v-model="formData.swaggerUrl" clearable show-word-limit/>
          </el-form-item>
        </el-col>
        <el-col :span="12"
                v-if="selectedPlatformValue != 'Swagger2' || (selectedPlatformValue == 'Swagger2' && !swaggerUrlEnable)">
          <el-upload
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
            <div class="el-upload__tip" slot="tip">{{ $t('api_test.api_import.file_size_limit') }}</div>
          </el-upload>
        </el-col>
      </el-row>
    </el-form>

    <div class="format-tip">
      <div>
        <span>{{ $t('api_test.api_import.tip') }}：{{ selectedPlatform.tip }}</span>
      </div>
      <div>
        <span>{{ $t('api_test.api_import.export_tip') }}：{{ selectedPlatform.exportTip }}</span>
      </div>
    </div>
  </el-dialog>
</template>

<script>
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import {listenGoBack, removeGoBackListener, hasLicense, getCurrentProjectID} from "@/common/js/utils";
  import MsSelectTree from "../../../../common/select-tree/SelectTree";

  export default {
    name: "ApiImport",
    components: {MsDialogFooter, MsSelectTree},
    props: {
      saved: {
        type: Boolean,
        default: true,
      },
      moduleOptions: Array,
      propotal: String,
      model: {
        type: String,
        default: 'definition'
      }
    },
    data() {
      return {
        visible: false,
        swaggerUrlEnable: false,
        showEnvironmentSelect: true,
        showXpackCompnent:false,
        moduleObj: {
          id: 'id',
          label: 'name',
        },
        modeOptions: [
          {
            id: 'fullCoverage',
            name: this.$t('commons.cover')
          },
          {
            id: 'incrementalMerge',
            name: this.$t('commons.not_cover')
          }
        ],
        protocol: "",
        platforms: [
          {
            name: 'MeterSphere',
            value: 'Metersphere',
            tip: this.$t('api_test.api_import.ms_tip'),
            exportTip: this.$t('api_test.api_import.ms_export_tip'),
            suffixes: new Set(['json'])
          },
        ],
        postmanPlanform: {
          name: 'Postman',
          value: 'Postman',
          tip: this.$t('api_test.api_import.postman_tip'),
          exportTip: this.$t('api_test.api_import.post_export_tip'),
          suffixes: new Set(['json'])
        },
        swaggerPlanform: {
          name: 'Swagger',
          value: 'Swagger2',
          tip: this.$t('api_test.api_import.swagger_tip'),
          exportTip: this.$t('api_test.api_import.swagger_export_tip'),
          suffixes: new Set(['json'])
        },
        harPlanform: {
          name: 'HAR',
          value: 'Har',
          tip: this.$t('api_test.api_import.har_tip'),
          exportTip: this.$t('api_test.api_import.har_export_tip'),
          suffixes: new Set(['har'])
        },
        esbPlanform: {
          name: 'ESB',
          value: 'ESB',
          tip: this.$t('api_test.api_import.esb_tip'),
          exportTip: this.$t('api_test.api_import.esb_export_tip'),
          suffixes: new Set(['xlsx', 'xls'])
        },
        selectedPlatform: {},
        selectedPlatformValue: 'Metersphere',
        result: {},
        projects: [],
        environments: [],
        useEnvironment: false,
        formData: {
          file: undefined,
          swaggerUrl: '',
          modeId: this.$t('commons.not_cover'),
          moduleId: '',
        },
        rules: {
          modeId: [
            {required: true, message: this.$t('commons.please_select_import_mode'), trigger: 'change'},
          ],
        },
        currentModule: {},
        fileList: []
      }
    },
    created() {
      this.platforms.push(this.postmanPlanform);
      this.platforms.push(this.swaggerPlanform);
      this.platforms.push(this.harPlanform);
      this.selectedPlatform = this.platforms[0];
    },
    watch: {
      moduleOptions() {
        if (this.moduleOptions.length > 0) {
          this.formData.moduleId = this.moduleOptions[0].id;
        }
      },
      selectedPlatformValue() {
        for (let i in this.platforms) {
          if (this.platforms[i].value === this.selectedPlatformValue) {
            this.selectedPlatform = this.platforms[i];
            break;
          }
        }
      },
      propotal() {
        let postmanIndex = this.platforms.indexOf(this.postmanPlanform);
        let swaggerPlanformIndex = this.platforms.indexOf(this.swaggerPlanform);
        let harPlanformIndex = this.platforms.indexOf(this.harPlanform);
        let esbPlanformIndex = this.platforms.indexOf(this.esbPlanform);
        if (postmanIndex >= 0) {
          this.platforms.splice(this.platforms.indexOf(this.postmanPlanform), 1);
        }
        if (swaggerPlanformIndex >= 0) {
          this.platforms.splice(this.platforms.indexOf(this.swaggerPlanform), 1);
        }
        if (harPlanformIndex >= 0) {
          this.platforms.splice(this.platforms.indexOf(this.harPlanform), 1);
        }
        if (esbPlanformIndex >= 0) {
          this.platforms.splice(this.platforms.indexOf(this.esbPlanform), 1);
        }
        if (this.propotal === 'TCP') {
          if(hasLicense()){
            this.platforms.push(this.esbPlanform);
          }
          return true;
        } else if (this.propotal === 'HTTP') {
          this.platforms.push(this.postmanPlanform);
          this.platforms.push(this.swaggerPlanform);
          this.platforms.push(this.harPlanform);
          return false;
        }
      }
    },
    computed: {
      isSwagger2() {
        return this.selectedPlatformValue === 'Swagger2';
      },
      showImportModel() {
        return this.selectedPlatformValue != 'Har' && this.selectedPlatformValue != 'ESB';
      },
      showTemplate() {
        return this.selectedPlatformValue === 'ESB';
      },
      isScenarioModel() {
        return this.model === 'scenario';
      },
      projectId() {
        return getCurrentProjectID();
      },
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
      downloadTemplate() {
        if (this.selectedPlatformValue == "ESB") {
          this.$fileDownload('/api/definition/export/esbExcelTemplate');
        }
      },
      uploadValidate(file, fileList) {
        let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
        if (this.selectedPlatform.suffixes && !this.selectedPlatform.suffixes.has(suffix)) {
          this.$warning(this.$t('api_test.api_import.suffixFormatErr'));
          return false;
        }
        if (file.size / 1024 / 1024 > 50) {
          this.$warning(this.$t('test_track.case.import.upload_limit_size'));
          return false;
        }
        return true;
      },
      save() {
        this.$refs.form.validate(valid => {
          if (valid) {
            if ((this.selectedPlatformValue != 'Swagger2' || (this.selectedPlatformValue == 'Swagger2' && !this.swaggerUrlEnable)) && !this.formData.file) {
              this.$warning(this.$t('commons.please_upload'));
              return;
            }
            let url = '/api/definition/import';
            if (this.isScenarioModel) {
              url = '/api/automation/import';
            }
            let param = this.buildParam();
            this.result = this.$fileUpload(url, param.file, null, this.buildParam(), response => {
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
      setModule(id, data) {
        this.formData.moduleId = id;
        this.formData.modulePath = data.path;
      },
      buildParam() {
        let param = {};
        Object.assign(param, this.formData);
        param.platform = this.selectedPlatformValue;
        param.saved = this.saved;
        param.model = this.model;
        if (this.currentModule) {
          param.moduleId = this.formData.moduleId
          param.modeId = this.formData.modeId
        }
        param.projectId = this.projectId;
        if (!this.swaggerUrlEnable) {
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
    min-width: 750px;
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

  .el-radio {
    margin-right: 20px;
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

  .el-divider {
    height: 200px;
  }

</style>
