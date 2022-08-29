<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="visible" width="1000px"
             @close="close" destroy-on-close ref="editFile" append-to-body>
    <el-card v-loading="result.loading" class="table-card">
      <template v-slot:header>
        <ms-table-header title="" :condition.sync="condition" @search="getProjectFiles"
                         :show-create="false" :show-thumbnail="false" @change="change">
        </ms-table-header>
      </template>
      <ms-table v-loading="data.loading"
                class="basic-config"
                :screen-height="height"
                :data="metadataArr"
                :condition="condition"
                :hidePopover="true"
                @refresh="getProjectFiles" ref="table">
        <ms-table-column
          prop="name"
          show-overflow-tooltip
          :width="100"
          :label="$t('load_test.file_name')">
        </ms-table-column>
        <ms-table-column
          sortable
          prop="type"
          :filters="typeFilters"
          :label="$t('load_test.file_type')">
        </ms-table-column>

        <ms-table-column
          prop="description"
          :label="$t('group.description')">
        </ms-table-column>

        <ms-table-column
          prop="tags"
          min-width="60px"
          :show-overflow-tooltip=false
          :label="$t('commons.tag')">
          <template v-slot:default="scope">
            <el-tooltip class="item" effect="dark" placement="top">
              <div v-html="getTagToolTips(scope.row.tags)" slot="content"></div>
              <div class="oneLine">
                <ms-tag v-for="(itemName,index)  in scope.row.tags"
                        :key="index"
                        :show-tooltip="scope.row.tags.length === 1 && itemName.length * 12 <= 20"
                        :content="itemName"
                        type="success" effect="plain"
                        class="ms-tags"/>
              </div>
            </el-tooltip>
            <span/>
          </template>
        </ms-table-column>

        <ms-table-column
          sortable
          prop="createUser"
          :label="$t('commons.create_user')">
        </ms-table-column>
        <ms-table-column
          sortable
          prop="updateUser"
          :label="$t('ui.update_user')">
        </ms-table-column>

        <ms-table-column
          sortable
          :label="$t('commons.update_time')"
          prop="updateTime">
          <template v-slot="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>
      </ms-table>
      <ms-table-pagination :change="getProjectFiles" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>
    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="visible = false"
        @confirm="submit"/>
    </template>
  </el-dialog>
</template>

<script>
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {getCurrentProjectID, operationConfirm, getCurrentUserId, hasPermission} from "@/common/js/utils";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import MsTableHeader from "../header/FileHeader";
import MsTableSearchBar from "@/business/components/common/components/MsTableSearchBar";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsFileBatchMove from "../module/FileBatchMove";
import MsFileThumbnail from "../list/FileThumbnail";
import MsEditFileMetadata from "../edit/EditFileMetadata";
import MsTag from "@/business/components/common/components/MsTag";

export default {
  name: "MsFileMetadataList",
  components: {
    MsMainContainer,
    MsContainer,
    MsTableSearchBar,
    MsTableHeader,
    MsTableOperatorButton,
    MsDialogFooter,
    MsTableButton,
    MsTablePagination,
    MsTable,
    MsTableColumn,
    MsFileBatchMove,
    MsFileThumbnail,
    MsEditFileMetadata,
    MsTag
  },
  props: {
    moduleId: String,
    nodeTree: Array,
  },
  data() {
    return {
      loadFileVisible: false,
      result: {},
      data: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      metadataArr: [],
      fileNumLimit: 10,
      condition: {},
      projectId: getCurrentProjectID(),
      height: '500px',
      typeFilters: [],
      showView: "list",
      visible: false
    };
  },
  created() {
    this.getTypes();
    this.getProjectFiles();
  },
  methods: {
    submit() {
      if (this.$refs.table.selectRows && this.$refs.table.selectRows.size > 0) {
        this.$emit("checkRows", this.$refs.table.selectRows);
        this.visible = false;
      } else {
        this.$warning("请选择一条数据");
      }
    },
    changeList(pageSize, currentPage) {
      this.currentPage = currentPage;
      this.pageSize = pageSize;
      this.getProjectFiles();
    },
    change(value) {
      this.showView = value;
    },
    refreshModule() {
      this.$emit('refreshModule');
    },
    close() {
      this.visible = false;
    },
    open() {
      this.visible = true;
    },
    myFile() {
      if (!this.condition.filters) {
        this.condition.filters = {createUser: [getCurrentUserId()]};
      } else {
        this.condition.filters.createUser = [getCurrentUserId()];
      }
      this.getProjectFiles();
    },
    getProjectFiles() {
      this.data = this.$post('/file/metadata/project/' + this.projectId + "/" + this.currentPage + "/" + this.pageSize, this.condition, res => {
        let data = res.data;
        this.total = data.itemCount;
        this.metadataArr = data.listObject;
        this.metadataArr.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
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
      let url = '/file/metadata/create';
      let request = {createUser: getCurrentUserId(), updateUser: getCurrentUserId(), projectId: this.projectId, moduleId: this.moduleId};
      formData.append("request", new Blob([JSON.stringify(request)], {type: "application/json"}));
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
    handleExceed() {
      this.$error(this.$t('load_test.file_size_limit'));
    },
    handleView(row) {
      this.$refs.editFileMetadata.open(row, this.pageSize, this.currentPage, this.total);
    },
    getTagToolTips(tags) {
      try {
        let showTips = "";
        tags.forEach(item => {
          showTips += item + ",";
        })
        return showTips.substr(0, showTips.length - 1);
      } catch (e) {
        return "";
      }
    },
    handleBatchMove() {
      this.$refs.batchMove.open(this.nodeTree, this.$refs.table.selectIds);
    },
    handleDownload(row) {
      let type = row.type ? row.type.toLowerCase() : "";
      let name = row.name;
      if (type !== "" && !name.endsWith(type)) {
        name = name + "." + type;
      }
      this.$fileDownload("/file/metadata/download/" + row.id, name);
    },
    exportZip() {
      let array = [];
      this.$refs.table.selectRows.forEach(item => {
        array.push(item);
      })
      let request = {projectId: getCurrentProjectID, requests: array};
      this.$fileDownloadPost("/file/metadata/download/zip", request, "文件集.zip");
    },

    handleDeleteBatch() {
      operationConfirm(this.$t('project.file_delete_tip', [this.$refs.table.selectIds.length + " 条 "]), () => {
        this.$post('/file/metadata/delete/batch', this.$refs.table.selectIds, () => {
          this.$refs.table.clear();
          this.$success(this.$t('commons.delete_success'));
          this.getProjectFiles();
        });
      });
    },
    handleDelete(row) {
      operationConfirm(this.$t('project.file_delete_tip', [row.name]), () => {
        this.$get('/file/metadata/delete/' + row.id, response => {
          this.$success(this.$t('commons.delete_success'));
          this.getProjectFiles();
        });
      });
    },
    getTypes() {
      this.typeFilters = [];
      this.$get('/file/metadata/get/type/all', response => {
        response.data.forEach(item => {
          this.typeFilters.push({text: item, value: item});
        })
      });
    },
    moduleChange(ids) {
      if (!this.condition.filters) {
        this.condition.filters = {moduleIds: ids};
      } else {
        this.condition.filters.moduleIds = ids;
      }
      this.getProjectFiles();
    },
    moveSave(param) {
      this.buildBatchParam(param);
    }
  }
};
</script>

<style scoped>
.oneLine {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.ms-tags {
  margin-left: 0px;
  margin-right: 2px;
}
</style>
