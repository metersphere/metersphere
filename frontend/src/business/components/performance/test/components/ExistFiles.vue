<template>
  <el-dialog :close-on-click-modal="false"
             :destroy-on-close="true"
             :title="$t('load_test.exist_jmx')" width="70%"
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
          :limit="fileNumLimit"
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
          accept=".jar,.csv,.json,.pdf,.jpg,.png,.jpeg,.doc,.docx,.xlsx,.txt"
          action=""
          :limit="fileNumLimit"
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
    <el-table v-loading="projectLoadingResult.loading"
              class="basic-config"
              :data="existFiles"
              @select-all="handleSelectAll"
              @select="handleSelectionChange">

      <el-table-column type="selection"/>
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
          <span class="last-modified">{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('commons.operating')">
        <template v-slot:default="scope">
          <el-upload
            style="width: 38px; float: left;"
            accept=".jmx,.jar,.csv,.json,.pdf,.jpg,.png,.jpeg,.doc,.docx,.xlsx,.txt"
            action=""
            :limit="fileNumLimit"
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
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import {getCurrentProjectID} from "@/common/js/utils";
import {findThreadGroup} from "@/business/components/performance/test/model/ThreadGroup";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import axios from "axios";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import {Message} from "element-ui";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";

export default {
  name: "ExistFiles",
  components: {MsTableOperatorButton, MsTableHeader, MsTableButton, MsTablePagination, MsDialogFooter},
  props: {
    fileList: Array,
    tableData: Array,
    uploadList: Array,
    scenarios: Array,
    isReadOnly: Boolean,
  },
  inject: [
    'reload'
  ],
  data() {
    return {
      loadFileVisible: false,
      projectLoadingResult: {},
      currentPage: 1,
      pageSize: 5,
      total: 0,
      loadType: 'jmx',
      existFiles: [],
      selectIds: new Set,
      fileNumLimit: 10,
      condition: {}
    };
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
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        this.existFiles.forEach(item => {
          this.selectIds.add(item.id);
        });
      } else {
        this.existFiles.forEach(item => {
          if (this.selectIds.has(item.id)) {
            this.selectIds.delete(item.id);
          }
        });
      }
    },
    handleSelectionChange(selection, row) {
      if (this.selectIds.has(row.id)) {
        this.selectIds.delete(row.id);
      } else {
        this.selectIds.add(row.id);
      }
    },
    getProjectFiles() {
      this.projectLoadingResult = this.$post('/performance/project/' + this.loadType + '/' + getCurrentProjectID() + "/" + this.currentPage + "/" + this.pageSize, this.condition, res => {
        let data = res.data;
        this.total = data.itemCount;
        this.existFiles = data.listObject;
      });
    },
    handleImport(file) {
      if (file) { // 接口测试创建的性能测试
        this.selectIds.add(file.id);
        this.getJmxContents();
        return;
      }
      if (this.selectIds.size === 0) {
        this.loadFileVisible = false;
        return;
      }

      let rows = this.existFiles.filter(f => this.selectIds.has(f.id));
      let jmxIds = [];
      for (let i = 0; i < rows.length; i++) {
        let row = rows[i];
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
      }
      //
      rows.forEach(row => {
        this.fileList.push(row);
      });

      if (this.loadType === 'resource') {
        this.$success(this.$t('test_track.case.import.success'));
        this.close();
        return;
      }

      this.getJmxContents(jmxIds);
    },
    getJmxContents(jmxIds) {
      this.projectLoadingResult = this.$post('/performance/export/jmx', jmxIds, (response) => {
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
      });
    },
    beforeUploadFile(file) {
      if (!this.fileValidator(file)) {
        /// todo: 显示错误信息
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
    checkFileExist(file, callback) {
      // 检查数据库是否存在同名文件
      async function f() {
        return await axios.post('/performance/file/' + getCurrentProjectID() + '/getMetadataByName', {name: file.name});
      }

      f().then(res => {
        let response = res.data;
        if (response.data.length === 0) {
          callback();
        } else {
          this.$error(this.$t('load_test.project_file_exist') + ', name: ' + file.name);
        }
      });
    },
    handleUpload(uploadResources, apiImport) {
      let self = this;

      let file = uploadResources.file;
      this.checkFileExist(file, () => {
        let formData = new FormData();
        let url = '/project/upload/files/' + getCurrentProjectID();
        formData.append("file", file);
        let options = {
          method: 'POST',
          url: url,
          data: formData,
          headers: {
            'Content-Type': undefined
          }
        };
        self.$request(options, (response) => {
          self.$success(this.$t('commons.save_success'));
          self.getProjectFiles();
          if (apiImport) {
            let row = response.data[0];
            self.handleImport(row);
          }
        });
      });
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
      let url = '/project/update/file/' + this.currentRow.id;
      formData.append("file", file);
      let options = {
        method: 'POST',
        url: url,
        data: formData,
        headers: {
          'Content-Type': undefined
        }
      };
      this.$request(options, (response) => {
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
        this.$get('/project/delete/file/' + row.id, response => {
          Message.success(this.$t('commons.delete_success'));
          this.getProjectFiles();
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
