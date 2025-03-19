<template>
  <span v-if="showHide">
    <el-upload
      action="#"
      class="ms-upload-header"
      list-type="picture-card"
      :disabled="disabled"
      :file-list="parameter.files"
      ref="upload">
      <div class="upload-default" @click.stop>
        <i class="el-icon-plus" v-show="disabled" slot="reference" />
        <el-popover placement="right" v-show="!disabled" trigger="hover">
          <div>
            <el-upload
              action="#"
              class="ms-body-upload"
              :http-request="upload"
              :beforeUpload="uploadValidate"
              ref="uploadLocal">
              <el-button type="text" :disabled="isReadOnly" v-if="!isReadOnly">
                {{ $t('permission.project_file.local_upload') }}</el-button
              >
              <span slot="file" />
            </el-upload>
          </div>
          <el-button type="text" @click="associationFile" v-if="!isReadOnly" :disabled="isReadOnly">{{
            $t('permission.project_file.associated_files')
          }}</el-button>
          <i class="el-icon-plus" slot="reference" />
        </el-popover>
      </div>
      <div class="upload-item" slot="file" slot-scope="{ file }">
        <span>{{ file.file && file.file.name ? file.file.name : file.name }}</span>
        <span v-if="file.storage === 'FILE_REF'" class="el-upload-list__item-actions">
          <span v-if="!disabled" class="ms-list__item-delete" @click="handleRemove(file)">
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
    <ms-file-batch-move ref="module" @setModuleId="setModuleId" />
    <ms-file-metadata-list ref="metadataList" @checkRows="checkRows" />
  </span>
</template>

<script>
import { dumpFile, fileExists } from 'metersphere-frontend/src/api/file-metadata';
import MsFileBatchMove from '@/business/commons/FileBatchMove';
import MsFileMetadataList from '@/business/commons/QuoteFileList';
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';
import { getUUID } from 'metersphere-frontend/src/utils';
import {getUploadSizeLimit} from "metersphere-frontend/src/utils/index";

export default {
  name: 'MsApiBodyFileUpload',
  components: {
    MsFileBatchMove,
    MsFileMetadataList,
  },
  data() {
    return {
      file: {},
      showHide: true,
    };
  },
  props: {
    parameter: Object,
    id: String,
    default() {
      return {};
    },
    isReadOnly: {
      type: Boolean,
      default: false,
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
      let fileIds = [];
      this.parameter.files.forEach((file) => {
        if (file.storage === 'FILE_REF' && file.fileId) {
          fileIds.push(file.fileId);
        }
      });
      if (fileIds.length > 0) {
        this.result = fileExists(fileIds).then((response) => {
          let resultIds = response.data;
          this.parameter.files.forEach((file) => {
            if (file.storage === 'FILE_REF' && resultIds.indexOf(file.fileId) === -1) {
              file.isExist = true;
            }
          });
          this.reload();
        });
      }
    },
    reload() {
      this.showHide = false;
      this.$nextTick(() => {
        this.showHide = true;
      });
    },
    setModuleId(moduleId) {
      let files = [];
      if (this.file && this.file.file) {
        files.push(this.file.file);
      }
      let request = {
        id: getUUID(),
        resourceId: this.id,
        moduleId: moduleId,
        projectId: getCurrentProjectID(),
        fileName: this.file.name,
      };
      dumpFile(null, files, request).then((response) => {
        this.$success(this.$t('organization.integration.successful_operation'));
      });
    },
    checkRows(rows) {
      rows.forEach((item) => {
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
    handleRemove(file) {
      if (file && this.parameter.files) {
        for (let i = 0; i < this.parameter.files.length; i++) {
          let fileName = file.file ? file.file.name : file.name;
          let paramFileName = this.parameter.files[i].file
            ? this.parameter.files[i].file.name
            : this.parameter.files[i].name;
          if (fileName === paramFileName) {
            this.parameter.files.splice(i, 1);
            break;
          }
        }
      }
    },
    upload(file) {
      file.name = file.file.name;
      this.parameter.files.push(file);
    },
    uploadValidate(file) {
      if (file.size / 1024 / 1024 > this.uploadSize) {
        this.$warning(this.$t('api_test.api_import.file_size_limit', {size: this.uploadSize}));
        return false;
      }
      return true;
    },
    handleUpload(file) {
      this.$refs.module.init();
      this.file = file;
    },
    associationFile() {
      this.$refs.metadataList.open();
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
<style>
.el-popover {
  min-width: 60px;
}
</style>
<style scoped>
.el-upload {
  background-color: black;
}

.ms-upload-header :deep(.el-upload) {
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

.ms-upload-header :deep(.el-upload-list__item) {
  height: 30px;
  width: auto;
  padding: 2px 5px;
  margin-bottom: 0px;
}

.ms-upload-header :deep(.el-upload-list--picture-card) {
}

.ms-upload-header {
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

.ms-upload-header :deep(.el-upload-list--picture-card) {
  display: inline;
  white-space: normal;
}

.ms-upload-header :deep(.el-upload-list--picture-card .el-upload-list__item) {
  display: inline-block;
}

.ms-list__item-delete {
  margin-top: -12px;
  text-align: center;
  vertical-align: middle;
}
</style>
