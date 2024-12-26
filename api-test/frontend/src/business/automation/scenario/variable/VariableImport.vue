<template>
  <el-dialog
    :visible="dialogVisible"
    :title="dialogTitle"
    @close="close"
    :close-on-click-modal="false"
    append-to-body
    width="35%">
    <el-form :rules="rules" label-width="80px" v-model="modeId">
      <el-form-item prop="modeId" :label="$t('commons.import_mode')">
        <el-select size="small" v-model="modeId">
          <el-option v-for="item in modeOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-upload
          class="api-upload"
          drag
          action="alert"
          :on-change="handleFileChange"
          :limit="1"
          :file-list="uploadFiles"
          :on-remove="handleRemove"
          :on-exceed="handleExceed"
          :auto-upload="false"
          accept=".json">
          <i class="el-icon-upload"></i>
          <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
          <div class="el-upload__tip" slot="tip">
            {{ $t('api_test.api_import.file_size_limit', {size: this.uploadSize}) }}
            {{ '，' + $t('api_test.api_import.ms_env_import_file_limit') }}
          </div>
        </el-upload>
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <el-button type="primary" @click="save">
        {{ $t('commons.confirm') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script>
import {getUploadSizeLimit} from "metersphere-frontend/src/utils";
export default {
  name: 'VariableImport',
  props: {
    projectList: {
      type: Array,
      default() {
        return [];
      },
    },
    toImportProjectId: {
      type: String,
      default() {
        return '';
      },
    },
  },
  data() {
    return {
      currentProjectId: '', //所选中环境的id
      uploadFiles: [],
      dialogTitle: this.$t('commons.import_variable'),
      dialogVisible: false,
      modeOptions: [
        {
          id: 'fullCoverage',
          name: this.$t('commons.cover'),
        },
        {
          id: 'incrementalMerge',
          name: this.$t('commons.not_cover'),
        },
      ],
      modeId: 'fullCoverage',
      rules: {
        modeId: [{ required: true, message: '', trigger: 'blur' }],
      },
    };
  },
  watch: {
    //导入框激活时重置选中的项目和文件
    dialogVisible(val, oldVal) {
      if (oldVal === false) {
        this.currentProjectId = '';
        this.uploadFiles = [];
      }
    },
  },
  computed: {
    uploadSize() {
      return getUploadSizeLimit();
    }
  },

  methods: {
    handleFileChange(file, uploadFiles) {
      this.uploadFiles = uploadFiles;
    },
    save() {
      if (this.uploadFiles.length > 0) {
        for (let i = 0; i < this.uploadFiles.length; i++) {
          if (this.uploadValidate(this.uploadFiles[i])) {
            return;
          }
          let file = this.uploadFiles[i];
          if (!file) {
            continue;
          }
          let reader = new FileReader();
          reader.readAsText(file.raw);
          reader.onload = (e) => {
            let fileString = e.target.result;
            let messages = '';
            try {
              JSON.parse(fileString).map((env) => {
                if (!env.name) {
                  messages = this.$t('api_test.automation.variable_warning');
                }
              });
              if (messages !== '') {
                this.$warning(messages);
                return;
              }
              this.$emit('mergeData', fileString, this.modeId);
              this.dialogVisible = false;
              this.$success(this.$t('commons.save_success'));
            } catch (exception) {
              this.$warning(this.$t('api_test.api_import.ms_env_import_file_limit'));
            }
          };
        }
      } else {
        this.$warning(this.$t('test_track.case.import.import_file_tips'));
      }
    },
    handleExceed() {
      this.$warning(this.$t('api_test.api_import.file_exceed_limit'));
    },
    handleRemove() {},
    uploadValidate(file) {
      //判断文件扩展名是不是.json，以及文件大小是否超过20M
      const extension = file.name.substring(file.name.lastIndexOf('.') + 1);
      if (!(extension === 'json')) {
        this.$warning(this.$t('api_test.api_import.ms_env_import_file_limit'));
        return true;
      }
      if (file.size / 1024 / 1024 > 20) {
        this.$warning(this.$t('api_test.api_import.file_size_limit'));
        return true;
      }
      return false;
    },
    open() {
      this.dialogVisible = true;
    },
    close() {
      this.dialogVisible = false;
    },
  },
};
</script>

<style scoped>
.project-item {
  padding-left: 20px;
  padding-right: 20px;
}
</style>
