<template>
  <el-dialog :visible="dialogVisible" :title="dialogTitle"
             @close="close" :close-on-click-modal="false"
              width="50%">
    <el-form>
      <el-row>
        <el-col :span="6">
          <div class="project-item">
            <div style="margin-bottom: 10px">
              {{$t('project.select')}}
            </div>
            <el-select v-model="currentProjectId"  filterable clearable >
              <el-option v-for="item in projectList" :key="item.id"
                         :label="item.name" :value="item.id">
              </el-option>
            </el-select>
          </div>
        </el-col>
        <el-col :span="16" :offset="1">
          <el-upload
            class="api-upload" drag action="alert"
            :on-change="handleFileChange"
            :limit="1" :file-list="uploadFiles"
            :on-remove="handleRemove"
            :on-exceed="handleExceed"
            :auto-upload="false" accept=".json">
            <i class="el-icon-upload"></i>
            <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
            <div class="el-upload__tip" slot="tip">
              {{ $t('api_test.api_import.file_size_limit') }} {{'，' + $t('api_test.api_import.ms_env_import_file_limit') }}
            </div>
          </el-upload>
        </el-col>
      </el-row>
    </el-form>
    <template v-slot:footer>
      <el-button type="primary" @click="save">
        {{ $t('commons.confirm') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script>

export default {
  name: "EnvironmentImport",
  props: {
    projectList: {
      type: Array,
      default() {
        return [];
      }
    },
  },
  data() {
    return {
      currentProjectId: '',   //所选中环境的id
      uploadFiles: [],
      dialogTitle: this.$t('api_test.environment.import'),
      dialogVisible: false
    }
  },
  watch: {
    //导入框激活时重置选中的项目和文件
    dialogVisible(val, oldVal) {
      if (oldVal === false) {
        this.currentProjectId = '';
        this.uploadFiles = [];
      }
    }
  },


  methods: {
    handleFileChange(file, uploadFiles) {
      this.uploadFiles = uploadFiles;
    },
    save() {
      if (this.uploadFiles.length > 0) {
        for (let i = 0; i < this.uploadFiles.length; i++) {
          this.uploadValidate(this.uploadFiles[i]);
          let file = this.uploadFiles[i];
          if (!file) {
            continue;
          }
          let reader = new FileReader();

          reader.readAsText(file.raw)
            reader.onload = (e) => {
              let fileString = e.target.result;
              try {
                JSON.parse(fileString).map(env => {
                  //projectId为空字符串要转换为null，空字符串会被认为有projectId
                  env.projectId = this.currentProjectId === '' ? null : this.currentProjectId;
                  this.$fileUpload('/api/environment/add', null,[],  env, response => {
                    this.$emit('refresh');
                    this.$success(this.$t('commons.save_success'));
                  })
                })
              } catch (exception) {
                this.$warning(this.$t('api_test.api_import.ms_env_import_file_limit'));
              }
            }

        }
      }
    },
    handleExceed() {
      this.$warning(this.$t('api_test.api_import.file_exceed_limit'));
    },
    handleRemove() {

    },
    uploadValidate(file) {    //判断文件扩展名是不是.json，以及文件大小是否超过20M
      const extension = file.name.substring(file.name.lastIndexOf('.') + 1);
      if (!(extension === 'json')) {
        this.$warning(this.$t('api_test.api_import.ms_env_import_file_limit'));
      }
      if (file.size / 1024 / 1024 > 20) {
        this.$warning(this.$t('api_test.api_import.file_size_limit'));
      }
    },
    open() {
      this.dialogVisible = true;
    },
    close() {
      this.dialogVisible = false;
    }
  },

}
</script>

<style scoped>
  .project-item {
    padding-left: 20px;
    padding-right: 20px;
  }
</style>
