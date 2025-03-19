<template>
  <span>
    <el-row>
      <el-col :span="18">
        <el-upload
          action="#"
          class="api-body-upload"
          list-type="picture-card"
          :file-list="parameter.files"
          :beforeUpload="uploadValidate"
          :on-exceed="exceed"
          :limit="1"
          ref="upload">
          <div class="upload-default" @click.stop>
            <el-popover placement="right" trigger="hover">
              <div>
                <el-upload
                  action="#"
                  class="ms-body-upload"
                  :http-request="upload"
                  :limit="1"
                  :on-exceed="exceed"
                  :beforeUpload="uploadValidate"
                  ref="uploadLocal">
                  <el-button type="text" v-if="!disabled"> {{ $t('permission.project_file.local_upload') }}</el-button>
                  <span slot="file" />
                </el-upload>
              </div>
              <el-button type="text" @click="associationFile" v-if="!disabled">{{
                $t('permission.project_file.associated_files')
              }}</el-button>
              <i class="el-icon-plus" slot="reference" />
            </el-popover>
          </div>
          <div class="upload-item" slot="file" slot-scope="{ file }" v-loading="loading">
            <span>{{ file.file && file.file.name ? file.file.name : file.name }}</span>
            <span class="el-upload-list__item-actions" v-if="file.storage === 'FILE_REF'">
              <span v-if="!disabled" class="ms-list__item-delete" @click="handleUnlock(file)">
                <i class="el-icon-unlock" />
                <span style="font-size: 13px">
                  {{ file.isExist ? $t('permission.project_file.file_delete_tip') : '' }}
                </span>
              </span>
            </span>
            <span class="el-upload-list__item-actions" v-else>
              <span v-if="!disabled" class="ms-list__item-delete" @click="handleUpload(file)">
                <el-tooltip :content="$t('permission.project_file.save_to_file_manage')" placement="top">
                  <i class="el-icon-upload" style="font-size: 23px" />
                </el-tooltip>
              </span>
              <span v-if="!disabled" class="ms-list__item-delete" @click="handleRemove(file)">
                <i class="el-icon-delete" />
              </span>
            </span>
          </div>
        </el-upload>
      </el-col>
      <el-col :span="6">
        <el-button size="small" style="margin: 3px 5px" @click="download">下载</el-button>
      </el-col>
    </el-row>
    <ms-file-batch-move ref="module" @setModuleId="setModuleId" />
    <ms-file-metadata-list ref="metadataList" @checkRows="checkRows" />
  </span>
</template>

<script>
import { dumpFile, getFileMetadata } from 'metersphere-frontend/src/api/file-metadata';
import { downloadFile, getUUID } from 'metersphere-frontend/src/utils';
import MsFileBatchMove from '@/business/commons/FileBatchMove';
import MsFileMetadataList from '@/business/commons/QuoteFileList';
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';
import {getUploadSizeLimit} from "metersphere-frontend/src/utils/index";

export default {
  name: 'MsCsvFileUpload',
  data() {
    return {
      loading: false,
    };
  },
  components: {
    MsFileBatchMove,
    MsFileMetadataList,
  },
  props: {
    parameter: Object,
    default() {
      return {};
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    uploadSize() {
      return getUploadSizeLimit();
    }
  },
  methods: {
    exist() {
      this.parameter.files.forEach((file) => {
        this.loading = true;
        getFileMetadata(file.fileId).then((response) => {
          file.isExist = !response.data;
          this.loading = false;
        });
      });
    },
    setModuleId(moduleId) {
      let files = [];
      if (this.file && this.file.file) {
        files.push(this.file.file);
      }
      let request = {
        id: getUUID(),
        csv: true,
        resourceId: this.file.id,
        moduleId: moduleId,
        projectId: getCurrentProjectID(),
        fileName: this.file.name,
      };
      dumpFile(null, files, request).then((response) => {
        this.$success(this.$t('organization.integration.successful_operation'));
      });
    },
    handleUpload(file) {
      this.$refs.module.init();
      this.file = file;
    },
    associationFile() {
      this.$refs.metadataList.open();
    },
    checkRows(rows) {
      if ((rows && rows.size !== 1) || this.parameter.files.length > 0) {
        this.$warning(this.$t('test_track.case.import.upload_limit_count'));
        return;
      }
      rows.forEach((item) => {
        if (!item.type || item.type.toLowerCase() !== 'csv') {
          this.$warning(this.$t('variables.cvs_info'));
          return;
        }
        let file = {
          name: item.name,
          id: getUUID(),
          fileId: item.id,
          storage: 'FILE_REF',
          projectId: item.projectId,
          fileType: item.type,
        };
        this.parameter.files.push(file);
      });
    },
    handleUnlock(file) {
      for (let i = 0; i < this.parameter.files.length; i++) {
        let fileName = file.file ? file.file.name : file.name;
        let paramFileName = this.parameter.files[i].file
          ? this.parameter.files[i].file.name
          : this.parameter.files[i].name;
        if (fileName === paramFileName) {
          this.parameter.files.splice(i, 1);
          this.$refs.upload.handleRemove(file);
          break;
        }
      }
    },
    download() {
      // 本地文件
      if (
        this.parameter.files &&
        this.parameter.files.length > 0 &&
        this.parameter.files[0].file &&
        this.parameter.files[0].file.name
      ) {
        downloadFile(this.parameter.files[0].file.name, this.parameter.files[0].file);
      }
      // 远程下载文件
      if (
        this.parameter.files &&
        this.parameter.files.length > 0 &&
        (!this.parameter.files[0].file || !this.parameter.files[0].file.name)
      ) {
        let file = this.parameter.files[0];
        let conf = {
          url: '/api/automation/file/download',
          method: 'post',
          data: file,
          responseType: 'blob',
        };
        if (file.storage === 'FILE_REF') {
          conf = {
            url: '/file/metadata/download/' + file.fileId,
            method: 'get',
            responseType: 'blob',
          };
        }
        this.result = this.$request(conf).then((response) => {
          const content = response.data;
          if (content && this.parameter.files[0]) {
            downloadFile(this.parameter.files[0].name, content);
          }
        });
      }
    },
    handleRemove(file) {
      let fileName = file.name ? file.name : file.file.name;
      this.$alert(this.$t('api_test.environment.csv_delete') + '：【 ' + fileName + ' 】？', '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$refs.upload.handleRemove(file);
            this.$refs.uploadLocal.handleRemove(file);
            for (let i = 0; i < this.parameter.files.length; i++) {
              let paramFileName = this.parameter.files[i].name
                ? this.parameter.files[i].name
                : this.parameter.files[i].file.name;
              if (fileName === paramFileName) {
                this.parameter.files.splice(i, 1);
                this.$refs.upload.handleRemove(file);
                this.$refs.uploadLocal.handleRemove(file);
                break;
              }
            }
          }
        },
      });
    },
    exceed() {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'));
    },
    upload(file) {
      this.parameter.files = [];
      this.parameter.files.push(file);
    },
    uploadValidate(file) {
      if (this.parameter.files.length > 0) {
        this.$warning(this.$t('test_track.case.import.upload_limit_count'));
        return false;
      }
      if (file.size / 1024 / 1024 > this.uploadSize) {
        this.$warning(this.$t('api_test.api_import.file_size_limit', {size: this.uploadSize}));
        return false;
      }
      if (!file.name.endsWith('.csv')) {
        this.$warning(this.$t('variables.cvs_info'));
        return false;
      }
      return true;
    },
  },
  created() {
    if (!this.parameter.files) {
      this.parameter.files = [];
    }
    this.exist();
  },
};
</script>

<style scoped>
.el-upload {
  background-color: black;
}

.api-body-upload :deep(.el-upload) {
  height: 30px;
  width: 32px;
}

.upload-default {
  min-height: 30px;
  width: 32px;
  line-height: 32px;
}

.el-icon-plus {
  font-size: 16px;
}

.api-body-upload :deep(.el-upload-list__item) {
  height: 30px;
  width: auto;
  padding: 2px 5px;
  margin-bottom: 0px;
}

.api-body-upload :deep(.el-upload-list--picture-card) {
}

.api-body-upload {
  min-height: 30px;
  border: 1px solid #ebeef5;
  padding: 2px;
  border-radius: 4px;
}

.ms-body-upload {
  min-height: 0px;
  height: 30px;
  border: 0px;
  padding: 0px;
  border-radius: 0px;
}

.upload-item {
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  width: 180px;
}

.ms-list__item-delete {
  margin-top: -15px;
  padding-top: -15px;
  text-align: center;
  vertical-align: middle;
}
</style>
