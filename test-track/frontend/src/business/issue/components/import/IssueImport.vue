<template>
  <el-dialog
    v-loading="loading"
    width="35%"
    :visible="visible"
    :title="$t('test_track.issue.import_bugs')"
    class="issue-import-dialog"
    @close="cancel">
    <div>
      <el-row>
        <span style="color: red">*</span> {{ $t('test_track.issue.import_type') }}
        <el-select v-model="importType" :placeholder="$t('commons.please_select')" size="mini" class="issue-import-type" clearable>
          <el-option
            v-for="item in importOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
        <el-tooltip effect="dark" style="margin-left: 10px" placement="right">
          <div slot="content" v-html="$t('test_track.issue.import_type_tips')"></div>
          <i class="el-icon-info"></i>
        </el-tooltip>
      </el-row>

      <el-row>
        <el-upload
          class="issue-upload" drag action="alert"
          :file-list="uploadFiles"
          :http-request="handleUpload"
          :on-remove="handleRemove" accept=".xls, .xlsx">
          <i class="el-icon-upload"></i>
          <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
          <div class="el-upload__tip" slot="tip">
            {{ $t('test_track.issue.import_file_limit_tips') }}
            <div>
              <el-link type="primary" class="download-template" @click="downloadIssueImportTemplate">
                {{ $t('test_track.case.import.download_template') }}
              </el-link>
            </div>
          </div>
        </el-upload>
      </el-row>

      <el-row style="margin: 10px 0">
        <el-scrollbar>
          <ul style="max-height: 60px">
            <li v-for="errFile in errList" :key="errFile.rowNum">
              {{ errFile.errMsg }}
            </li>
          </ul>
        </el-scrollbar>
      </el-row>

      <el-row style="text-align: right; margin-top: 30px">
        <el-button size="mini" @click="cancel">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="save">
          {{ $t('commons.save') }}
        </el-button>
      </el-row>
    </div>
  </el-dialog>
</template>

<script>
import {getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";

export default {
  name: "IssueImport",
  props: ['tabName', 'name'],
  data() {
    return {
      visible:false,
      loading: false,
      importType: "",
      importOptions: [{value: "Update", label: this.$t('commons.cover')}, {value: "Create", label: this.$t('commons.not_cover')}],
      uploadFiles: [],
      errList: [],
    }
  },
  created() {
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    }
  },
  methods: {
    open() {
      this.visible = true;
    },
    cancel() {
      this.visible = false;
      this.importType = "";
      this.uploadFiles = [];
      this.errList = [];
    },
    handleUpload(file) {
      this.uploadFiles = [];
      this.uploadFiles.push(file.file);
    },
    handleRemove(file) {
      let fileName = file.name ? file.name : file.file.name
      for (let i = 0; i < this.uploadFiles.length; i++) {
        let uploadFileName = this.uploadFiles[i].name ? this.uploadFiles[i].name : this.uploadFiles[i].file.name;
        if (fileName === uploadFileName) {
          this.uploadFiles.splice(i, 1);
          break;
        }
      }
    },
    downloadIssueImportTemplate() {
      let uri = '/issues/import/template/download/';
      this.$fileDownload(uri + getCurrentProjectID());
    },
    save() {
      if (this.importType === '') {
        this.$warning(this.$t('test_track.case.import.import_type_require_tips'))
        return;
      }
      if (this.uploadFiles.length === 0) {
        this.$warning(this.$t('test_track.case.import.import_file_tips'));
        return;
      }
      let uploadFile = this.uploadFiles[0]

      this.uploadFiles[0].slice(0, 1).arrayBuffer()
        .then(() => {
          let suffix = uploadFile.name.substring(uploadFile.name.lastIndexOf('.') + 1);
          if (suffix !== 'xls' && suffix !== 'xlsx') {
            this.$warning(this.$t('test_track.case.import.upload_limit_format'));
            return;
          }
          if (uploadFile.size / 1024 / 1024 > 100) {
            this.$warning(this.$t('test_track.case.import.upload_limit_size'));
            return;
          }
          let param = {
            workspaceId: getCurrentWorkspaceId(),
            projectId: getCurrentProjectID(),
            userId: getCurrentUserId(),
            importType: this.importType
          };
          this.loading = true;
          this.$fileUpload('/issues/import', uploadFile, param)
            .then(response => {
              this.loading = false;
              let res = response.data;
              if (res.success) {
                this.$success(this.$t('test_track.case.import.success'));
                this.cancel();
                this.$emit("refresh");
              } else {
                this.errList = res.errList;
              }
            }).catch((err) => {
            this.loading = false;
          });
        })
        .catch((err) => {
          this.$warning(this.$t('test_track.case.import.upload_refresh_tips'));
          return;
        });
    }
  }
}
</script>

<style scoped>
.issue-import-type {
  margin-left: 6px;
}

.issue-upload {
  margin-left: 75px;
  margin-top: 20px;
}

.download-template {
  margin-left: 220px;
  top: -15px;
  font-size: 12px;
  font-weight: 600;
}

.issue-import-dialog :deep(.el-dialog) {
  min-width: 500px;
}
</style>
