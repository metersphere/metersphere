<template>
  <el-dialog
    width="50%"
    :visible.sync="visible"
    @confirm="save"
    :title="title"
    append-to-body>

    <el-form :model="currentConfig" :rules="rules" label-width="100px" v-loading="result.loading" ref="form">
      <el-row>
        <el-col :span="12">
          <el-form-item :label="$t('project.select')" prop="projectName">
            <el-autocomplete
              size="small"
              v-model="currentConfig.projectName"
              :fetch-suggestions="querySearch"
              :placeholder="$t('commons.input_content')"
              @select="handleSelect"
              clearable show-word-limit
            ></el-autocomplete>
          </el-form-item>
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input size="small" v-model="currentConfig.name" clearable show-word-limit/>
          </el-form-item>
          <el-form-item :label="$t('commons.description')" prop="description">
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
            action=""
            :accept="acceptFileString"
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

        <el-col>
          <div class="buttons">
            <el-button type="primary" v-show="currentConfig.id" size="small" @click="save('update')">{{ $t('commons.update') }}</el-button>
            <el-button type="primary" v-show="!currentConfig.id" size="small" @click="save('add')">{{$t('commons.add')}}</el-button>
          </div>
        </el-col>

      </el-row>
    </el-form>
  </el-dialog>
</template>

<script>
  import {hasRoles} from "@/common/js/utils";
  import {ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "@/common/js/constants";

    export default {
      name: "filesConfigForm",
      data() {
        return {
          projectArray: [],
          visible: false,
          result: {},
          currentConfig: {
            projectName: '',
            projectId: '',
            name: '',
            description: '',
            fileName: '',
          },
          title: '',
          rules: {
            projectName: [
              {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
              {max: 60, message: this.$t('commons.input_limit', [1, 60]), trigger: 'blur'}
            ],
            name: [
              {required: false, message: this.$t('commons.input_name'), trigger: 'blur'},
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
        callback: {
          type: Function
        },
        acceptFileType: {
          type: Array,
          default: () => []
        },
        acceptFileString: {
          type: String
        }
      },
      watch: {
        config() {
          this.currentConfig = {
            name: '',
            description: '',
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
        if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
          this.result = this.$get("/project/listAll", response => {
            this.projectArray = response.data;
          });
        }
      },
      methods: {
        open(data) {
          this.visible = true;
          this.currentConfig = data;

          if (data) {
            this.url = '/file/update/';
            this.title = this.$t('project.edit_file');
            this.projectArray.forEach(item => {
              if(item.id === this.currentConfig.projectId) {
                this.currentConfig.projectName = item.name;
                this.currentConfig.projectId = item.id;
              }
            });
          } else {
            this.url = '/file/upload/';
            this.title = this.$t('load_test.upload_file');
            this.fileList = [];
            this.currentConfig = {};
          }
        },
        querySearch(queryString, cb) {
          let projects = [];
          this.projectArray.forEach(item => {
            projects.push({value: item.name, id: item.id});
          });
          let results = queryString ? projects.filter(this.createFilter(queryString)) : projects;
          cb(results);
        },
        createFilter(queryString) {
          return (project) => {
            return (project.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
          };
        },
        handleSelect(item) {
          this.currentConfig.projectName = item.value;
          this.currentConfig.projectId = item.id;
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
          let flag = false;
          this.acceptFileType.forEach(type => {
            if(suffix === type) {
              flag = true;
            }
          });
          if(!flag) {
            this.$warning(this.$t('api_test.api_import.suffixFormatErr'));
            return false;
          }
          if (file.size / 1024 / 1024 > 30) {
            this.$warning(this.$t('jar_config.upload_limit_size'));
            return false;
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
                if (type === 'add') {
                  // this.currentConfig.id = undefined;
                  this.callback(type, '/file/upload', this.currentConfig.projectId, null, this.fileList[0]);
                } else {  //  type = 'upload'
                  this.callback(type, '/file/update', this.currentConfig.projectId, this.currentConfig, this.fileList[0]);
                }
              }
            } else {
              return false;
            }
          });
        },
        clear() {
          this.$refs['form'].clearValidate();
          this.fileList = [];
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
