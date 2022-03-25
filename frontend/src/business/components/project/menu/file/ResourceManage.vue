<template>
  <el-card v-loading="result.loading" class="table-card">
    <template v-slot:header>
      <ms-table-header title="" :condition.sync="condition" @search="getProjectFiles"
                       :show-create="false">
        <template v-slot:button>
          <el-upload
            accept=".jmx,.jar,.csv,.json,.pdf,.jpg,.png,.jpeg,.doc,.docx,.xlsx,.txt"
            action=""
            :limit="fileNumLimit"
            multiple
            :show-file-list="false"
            :before-upload="beforeUploadFile"
            :http-request="handleUpload"
            :on-exceed="handleExceed"
          >
            <ms-table-button icon="el-icon-upload2"
                             :content="$t('load_test.upload_file')" v-permission="['PROJECT_FILE:READ+UPLOAD+FILE']"/>
          </el-upload>
        </template>
      </ms-table-header>
    </template>

    <el-table v-loading="projectLoadingResult.loading"
              class="basic-config"
              :height="height"
              :data="existFiles">

      <el-table-column type="selection"/>
      <el-table-column
        prop="name"
        :label="$t('load_test.file_name')">
      </el-table-column>
      <el-table-column
        prop="type"
        width="100"
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
            :before-upload="beforeUploadFile"
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
            v-permission="['PROJECT_FILE:READ+DELETE+FILE']"
            @exec="handleDelete(scope.row)">
          </ms-table-operator-button>
        </template>
      </el-table-column>
    </el-table>
    <ms-table-pagination :change="getProjectFiles" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
  </el-card>
</template>

<script>
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {getCurrentProjectID} from "@/common/js/utils";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import {Message} from "element-ui";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTableSearchBar from "@/business/components/common/components/MsTableSearchBar";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";

export default {
  name: "ResourceManage",
  components: {
    MsMainContainer,
    MsContainer,
    MsTableSearchBar,
    MsTableHeader,
    MsTableOperatorButton,
    MsDialogFooter,
    MsTableButton,
    MsTablePagination
  },
  data() {
    return {
      loadFileVisible: false,
      result: {},
      projectLoadingResult: {},
      currentPage: 1,
      pageSize: 5,
      total: 0,
      existFiles: [],
      fileList: [],
      uploadList: [],
      fileNumLimit: 10,
      condition: {},
      projectId: getCurrentProjectID(),
      currentRow: null,
      height: 'calc(50vh - 160px)'
    };
  },
  created() {
    this.getProjectFiles();
  },
  methods: {
    open(project) {
      this.projectId = project.id;
      this.loadFileVisible = true;
      this.getProjectFiles();
    },
    close() {
      this.loadFileVisible = false;
      this.selectIds.clear();
    },
    getProjectFiles() {
      this.projectLoadingResult = this.$post('/performance/project/all/' + this.projectId + "/" + this.currentPage + "/" + this.pageSize, this.condition, res => {
        let data = res.data;
        this.total = data.itemCount;
        this.existFiles = data.listObject;
      });
    },
    fileValidator(file) {
      /// todo: 是否需要对文件内容和大小做限制
      return file.size > 0;
    },
    beforeUploadFile(file) {
      if (!this.fileValidator(file)) {
        /// todo: 显示错误信息
        return false;
      }

      return true;
    },
    handleUpload(uploadResources) {
      let file = uploadResources.file;
      let formData = new FormData();
      let url = '/project/upload/files/' + this.projectId;
      formData.append("file", file);
      let options = {
        method: 'POST',
        url: url,
        data: formData,
        headers: {
          'Content-Type': undefined
        }
      };
      this.result = this.$request(options, () => {
        this.$success(this.$t('commons.save_success'));
        this.getProjectFiles();
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
      this.result = this.$request(options, () => {
        this.$success(this.$t('commons.save_success'));
        this.currentRow = null;
        this.getProjectFiles();
      });
    },
    handleExceed() {
      this.$error(this.$t('load_test.file_size_limit'));
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
    }
  }
};
</script>

<style scoped>

</style>
