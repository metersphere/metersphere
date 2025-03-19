<template>
  <el-dialog :close-on-click-modal="false"
             :destroy-on-close="true"
             :title="$t('load_test.exist_jmx')" width="70%"
             z-index="1000"
             :visible.sync="loadFileVisible">
    <ms-table-header title="" :condition.sync="condition" @search="getProjectFiles"
                     :show-create="false">
      <template v-slot:button>
        <el-upload
          v-if="loadType === 'jmx'"
          style="margin-bottom: 10px"
          accept=".jmx"
          action=""
          multiple
          :show-file-list="false"
          :before-upload="beforeUploadFile"
          :http-request="handleUpload"
          :on-exceed="handleExceed"
          :disabled="isReadOnly"
          :file-list="fileList">
          <ms-table-button icon="el-icon-upload2"
                           :content="$t('load_test.upload_jmx')"/>
        </el-upload>
        <el-upload
          v-else
          style="margin-bottom: 10px"
          accept=".jar,.csv,.json,.pdf,.jpg,.png,.jpeg,.doc,.docx,.xlsx,.txt,.der,.cer,.pem,.crt,.pfx,.p12,.jks,.dcm"
          action=""
          multiple
          :show-file-list="false"
          :before-upload="beforeUploadFile"
          :http-request="handleUpload"
          :on-exceed="handleExceed"
          :disabled="isReadOnly"
          :file-list="fileList">
          <ms-table-button icon="el-icon-upload2"
                           :content="$t('load_test.upload_file')"/>
        </el-upload>
      </template>
    </ms-table-header>
    <el-table v-loading="loading"
              row-key="id"
              class="basic-config"
              :data="existFiles"
              @select-all="handleSelectAll"
              @select="handleSelectionChange">

      <el-table-column type="selection" reserve-selection/>
      <el-table-column
        prop="name"
        :label="$t('load_test.file_name')">
      </el-table-column>
      <el-table-column
        prop="type"
        :label="$t('load_test.file_type')">
      </el-table-column>
      <el-table-column
        :label="$t('load_test.last_modify_time')">
        <template v-slot:default="scope">
          <i class="el-icon-time"/>
          <span class="last-modified">{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('commons.operating')">
        <template v-slot:default="scope">
          <el-upload
            style="width: 38px; float: left;"
            accept=".jmx,.jar,.csv,.json,.pdf,.jpg,.png,.jpeg,.doc,.docx,.xlsx,.txt,.der,.cer,.pem,.crt,.pfx,.p12,.jks,.dcm"
            action=""
            :show-file-list="false"
            :before-upload="beforeUpdateUploadFile"
            :http-request="handleUpdateUpload"
            :on-exceed="handleExceed">
            <el-tooltip effect="dark" :content="$t('project.upload_file_again')" placement="bottom">
              <el-button circle
                         type="success"
                         icon="el-icon-upload"
                         @click="handleEdit(scope.row)"
                         size="mini"/>
            </el-tooltip>
          </el-upload>
          <ms-table-operator-button
            icon="el-icon-delete"
            type="danger"
            :tip="$t('commons.delete')"
            :disabled="tableData.filter(f => f.name === scope.row.name).length > 0"
            @exec="handleDelete(scope.row)">
          </ms-table-operator-button>
        </template>
      </el-table-column>
    </el-table>
    <ms-table-pagination :change="getProjectFiles" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="handleImport"/>
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {findThreadGroup} from "@/business/test/model/ThreadGroup";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import {
  checkFileIsRelated,
  deleteFile,
  getJmxContents,
  getProjectFileByName,
  getProjectFiles,
  updateFile,
  uploadFiles
} from "@/api/performance";
import {getUploadSizeLimit} from "metersphere-frontend/src/utils/index";

export default {
  name: "ExistFiles",
  components: {MsTableOperatorButton, MsTableHeader, MsTableButton, MsTablePagination, MsDialogFooter},
  props: {
    fileList: Array,
    tableData: Array,
    uploadList: Array,
    scenarios: Array,
    isReadOnly: Boolean,
    testId: String
  },
  inject: [
    'reload'
  ],
  data() {
    return {
      loadFileVisible: false,
      loading: false,
      currentPage: 1,
      pageSize: 5,
      total: 0,
      loadType: 'jmx',
      existFiles: [],
      selectIds: new Set,
      selectFiles: [],
      condition: {},
      projectId: getCurrentProjectID()
    };
  },
  computed: {
    uploadSize() {
      return getUploadSizeLimit();
    }
  },
  methods: {
    open(loadType) {
      this.loadFileVisible = true;
      this.loadType = loadType;
      this.getProjectFiles();
    },
    close() {
      this.loadFileVisible = false;
      this.selectIds.clear();
      this.selectFiles = [];
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        this.existFiles.forEach(item => {
          if (!this.selectIds.has(item.id)) {
            this.selectIds.add(item.id);
          }
          if (this.selectFiles.filter(f => f.id === item.id).length === 0) {
            this.selectFiles.push(item);
          }
        });
      } else {
        this.existFiles.forEach(item => {
          if (this.selectIds.has(item.id)) {
            this.selectIds.delete(item.id);
            for (let i = 0; i < this.selectFiles.length - 1; i++) {
              if (this.selectFiles[i].id === item.id) {
                this.selectFiles.splice(i, 1);
              }
            }
          }
        });
      }
    },
    handleSelectionChange(selection, clickRow) {
      this.selectIds = new Set;
      this.selectFiles = [];
      if (selection.length != 0) {
        selection.forEach(row => {
          this.selectIds.add(row.id);
          if (this.selectFiles.filter(f => f.id === row.id).length === 0) {
            this.selectFiles.push(row);
          }
        })
      }
    },
    getProjectFiles() {
      if (this.testId) {
        this.condition.ids = [];
        this.condition.ids.push(this.testId);
      }
      this.loading = getProjectFiles(this.loadType, this.projectId, this.currentPage, this.pageSize, this.condition)
        .then(res => {
          let data = res.data;
          this.total = data.itemCount;
          this.existFiles = data.listObject;
        });
    },
    handleImport(file) {
      if (file) { // 接口测试创建的性能测试
        if (!this.selectIds.has(file.id)) {
          this.selectIds.add(file.id);
        }
        if (this.selectFiles.filter(f => f.id === file.id).length === 0) {
          this.selectFiles.push(file)
        }
        this.getJmxContents();
        return;
      }
      if (this.selectIds.size === 0) {
        this.loadFileVisible = false;
        return;
      }
      let jmxIds = [];
      for (let i = 0; i < this.selectFiles.length; i++) {
        let row = this.selectFiles[i];
        if (this.tableData.filter(f => f.name === row.name).length > 0) {
          setTimeout(() => {
            this.$warning(this.$t('load_test.delete_file') + 'name: ' + row.name);
          }, 100);
          continue;
        }
        if (row.type.toUpperCase() === 'JMX') {
          jmxIds.push(row.id);
        }
        this.tableData.push({
          name: row.name,
          size: (row.size / 1024).toFixed(2) + ' KB',
          type: row.type.toUpperCase(),
          updateTime: row.lastModified,
        });
        this.fileList.push(row);
      }

      if (this.loadType === 'resource') {
        this.$success(this.$t('test_track.case.import.success'));
        this.close();
        return;
      }

      this.getJmxContents(jmxIds);
      this.selectFiles = [];
    },
    getJmxContents(jmxIds) {
      getJmxContents(jmxIds)
        .then(response => {
          let data = response.data;
          if (!data) {
            return;
          }
          data.forEach(d => {
            let threadGroups = findThreadGroup(d.jmx, d.name);
            threadGroups.forEach(tg => {
              tg.options = {};
              this.scenarios.push(tg);
            });
          });

          this.$emit('fileChange', this.scenarios);
          this.$success(this.$t('test_track.case.import.success'));
          this.close();
        })
    },
    beforeUploadFile(file) {
      if (!this.fileValidator(file)) {
        /// todo: 显示错误信息
        return false;
      }
      if (file.size / 1024 / 1024 > this.uploadSize) {
        this.$error(this.$t('performance_test.upload_limit_size_warn', [this.uploadSize]));
        this.close();
        return false;
      }
      if (this.tableData.filter(f => f.name === file.name).length > 0) {
        this.$error(this.$t('load_test.delete_file') + ', name: ' + file.name);
        return false;
      }
    },
    beforeUpdateUploadFile(file) {
      if (!this.fileValidator(file)) {
        /// todo: 显示错误信息
        return false;
      }

      return true;
    },
    handleUpload(uploadResources, apiImport) {
      let file = uploadResources.file;

      async function f(projectId) {
        return await getProjectFileByName(projectId, {name: file.name})
      }

      f(this.projectId)
        .then((res) => {
          let formData = new FormData();
          formData.append("file", file);
          uploadFiles(this.projectId, formData)
            .then((response) => {
              this.$success(this.$t('commons.save_success'));
              this.getProjectFiles();
              if (apiImport) {
                let row = response.data[0];
                this.handleImport(row);
              }
            });
        })
        .catch(err => {
          this.$error(this.$t('load_test.project_file_exist') + ', name: ' + file.name);
        })
    },
    handleUpdateUpload(uploadResources) {
      let file = uploadResources.file;
      let i1 = file.name.lastIndexOf(".");
      let i2 = this.currentRow.name.lastIndexOf(".");
      let suffix1 = file.name.substring(i1);
      let suffix2 = this.currentRow.name.substring(i2);
      if (suffix1 !== suffix2) {
        this.$error(this.$t('load_test.project_file_update_type_error'));
        return;
      }

      let formData = new FormData();
      formData.append("file", file);

      updateFile(this.currentRow.id, formData)
        .then((response) => {
          this.$success(this.$t('commons.save_success'));
          this.getProjectFiles();
          // 刷新页面上的线程组
          if (this.tableData.filter(f => f.id === this.currentRow.id).length > 0) {
            this.reload();
          }
          this.currentRow = null;
        });
    },
    handleEdit(row) {
      this.currentRow = row;
    },
    handleDelete(row) {
      this.$confirm(this.$t('project.file_delete_tip', [row.name]), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        checkFileIsRelated(row.id)
          .then(() => {
            deleteFile(row.id)
              .then(response => {
                this.$success(this.$t('commons.delete_success'));
                this.getProjectFiles();
              });
          });
      }).catch(() => {

      });
    },
    handleExceed() {
      this.$error(this.$t('load_test.file_size_limit'));
    },
    fileValidator(file) {
      /// todo: 是否需要对文件内容和大小做限制
      return file.size > 0;
    },
  }
};
</script>

<style scoped>

</style>
