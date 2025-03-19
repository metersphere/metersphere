<template>
  <span>
    <el-upload
      action="#"
      class="api-body-upload"
      list-type="picture-card"
      :http-request="upload"
      :beforeUpload="uploadValidate"
      :file-list="plugin.files"
      :on-exceed="exceed"
      ref="upload">
      <div class="upload-default">
        <i class="el-icon-plus" />
      </div>
      <div class="upload-item" slot="file" slot-scope="{ file }">
        <span>{{ file.file ? file.file.name : file.name }}</span>
        <span class="el-upload-list__item-actions">
          <span v-if="!disabled" class="el-upload-list__item-delete" @click="handleRemove(file)">
            <i class="el-icon-delete" />
          </span>
        </span>
      </div>
    </el-upload>
  </span>
</template>

<script>
import { useApiStore } from '@/store';
import {getUploadSizeLimit} from "metersphere-frontend/src/utils/index";

const store = useApiStore();
export default {
  name: 'MsPluginUpload',
  data() {
    return {
      disabled: false,
      plugin: { files: [] },
    };
  },
  props: {
    value: String,
  },
  mounted() {
    if (this.value) {
      this.plugin.files = JSON.parse(this.value);
    }
  },
  computed: {
    uploadSize() {
      return getUploadSizeLimit();
    }
  },
  methods: {
    handleRemove(file) {
      this.$refs.upload.handleRemove(file);
      for (let i = 0; i < this.plugin.files.length; i++) {
        let fileName = file.file ? file.file.name : file.name;
        let paramFileName = this.plugin.files[i].file ? this.plugin.files[i].file.name : this.plugin.files[i].name;
        if (fileName === paramFileName) {
          this.plugin.files.splice(i, 1);
          this.$refs.upload.handleRemove(file);
          break;
        }
      }
    },
    exceed() {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'));
    },
    upload(file) {
      this.plugin.files.push(file);
      let files = [];
      if (!(store.pluginFiles instanceof Array)) {
        store.pluginFiles = [];
      }
      this.plugin.files.forEach((item) => {
        if (item.file) {
          files.push({
            uid: item.file.uid,
            name: item.file.name,
            size: item.file.size,
          });
          store.pluginFiles.push({ name: item.file.name, file: item.file });
        } else {
          files.push(item);
        }
      });
      this.$emit('input', JSON.stringify(files));
    },
    uploadValidate(file) {
      if (file.size / 1024 / 1024 > this.uploadSize) {
        this.$warning(this.$t('api_test.api_import.file_size_limit', {size: this.uploadSize}));
        return false;
      }
      return true;
    },
  },
  created() {
    if (this.plugin && !this.plugin.files) {
      this.plugin.files = [];
    }
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
  padding: 6px;
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

.upload-item {
}
</style>
