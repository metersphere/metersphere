<template>
    <span>
      <el-row>
        <el-col :span="18">
           <el-upload
             action="#"
             class="api-body-upload"
             list-type="picture-card"
             :http-request="upload"
             :beforeUpload="uploadValidate"
             :file-list="parameter.files"
             :limit="1"
             :on-exceed="exceed"
             ref="upload">
         <div class="upload-default">
           <i class="el-icon-plus"/>
         </div>
         <div class="upload-item" slot="file" slot-scope="{file}">
           <span>{{ file.file ? file.file.name : file.name }}</span>
           <span class="el-upload-list__item-actions">
            <span v-if="!disabled" class="el-upload-list__item-delete" @click="handleRemove(file)">
              <i class="el-icon-delete"/>
            </span>
           </span>
          </div>
      </el-upload>
        </el-col>
        <el-col :span="6">
          <el-button size="small" style="margin: 3px 5px" @click="download">下载</el-button>
        </el-col>
      </el-row>
    </span>
</template>

<script>
import {downloadFile} from "@/common/js/utils";

export default {
  name: "MsApiBodyFileUpload",
  data() {
    return {
      disabled: false,
    };
  },
  props: {
    parameter: Object,
    default() {
      return {}
    }
  },
  methods: {
    download() {
      // 本地文件
      if (this.parameter.files && this.parameter.files.length > 0 && this.parameter.files[0].file) {
        downloadFile(this.parameter.files[0].file.name, this.parameter.files[0].file);
      }
      // 远程下载文件
      if (this.parameter.files && this.parameter.files.length > 0 && !this.parameter.files[0].file) {
        let file = this.parameter.files[0];
        let conf = {
          url: "/api/automation/file/download",
          method: 'post',
          data: file,
          responseType: 'blob',
        };
        this.result = this.$request(conf).then(response => {
          const content = response.data;
          if (content && this.parameter.files[0]) {
            downloadFile(this.parameter.files[0].name, content);
          }
        });
      }
    },
    handleRemove(file) {
      let fileName = file.file ? file.file.name : file.name
      this.$alert('是否确认删除CSV文件：【 ' + fileName + " 】？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$refs.upload.handleRemove(file);
            for (let i = 0; i < this.parameter.files.length; i++) {
              let paramFileName = this.parameter.files[i].file ?
                this.parameter.files[i].file.name : this.parameter.files[i].name;
              if (fileName === paramFileName) {
                this.parameter.files.splice(i, 1);
                this.$refs.upload.handleRemove(file);
                break;
              }
            }
          }
        }
      });
    },
    exceed() {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'));
    },
    upload(file) {
      this.parameter.files.push(file);
    },
    uploadValidate(file) {
      if (file.size / 1024 / 1024 > 500) {
        this.$warning(this.$t('api_test.request.body_upload_limit_size'));
        return false;
      }
      if (!file.name.endsWith(".csv")) {
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
  }
}
</script>

<style scoped>

.el-upload {
  background-color: black;
}

.api-body-upload >>> .el-upload {
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

.api-body-upload >>> .el-upload-list__item {
  height: 30px;
  width: auto;
  padding: 6px;
  margin-bottom: 0px;
}

.api-body-upload >>> .el-upload-list--picture-card {
}

.api-body-upload {
  min-height: 30px;
  border: 1px solid #EBEEF5;
  padding: 2px;
  border-radius: 4px;
}

.upload-item {
}

</style>
