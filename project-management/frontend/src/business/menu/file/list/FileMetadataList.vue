<template>
  <el-card v-loading="tableLoading" class="table-card">
    <template v-slot:header>
      <ms-table-header
        :condition.sync="condition"
        :show-create="false"
        @change="change"
        @search="getProjectFiles"
        title=""
      >
        <template v-slot:button>
          <ms-table-button
            icon="el-icon-upload2"
            v-if="moduleType === 'repository'"
            :content="$t('variables.add_file')"
            @click="addRepositoryFile"
            v-permission="['PROJECT_FILE:READ+UPLOAD+JAR']"
          />
          <el-upload
            v-else
            action=""
            multiple
            :show-file-list="false"
            :before-upload="beforeUploadFile"
            :http-request="handleUpload"
            :on-exceed="handleExceed"
          >
            <ms-table-button
              icon="el-icon-upload2"
              :content="$t('variables.add_file')"
              v-permission="['PROJECT_FILE:READ+UPLOAD+JAR']"
            />
          </el-upload>
        </template>
      </ms-table-header>
    </template>
    <div v-if="showView === 'list'">
      <ms-table
        v-loading="data.loading"
        class="basic-config"
        :total="total"
        :screen-height="height"
        :batch-operators="buttons"
        :data="metadataArr"
        :condition="condition"
        :hidePopover="true"
        @refresh="getProjectFiles"
        ref="table"
      >
        <ms-table-column
          prop="name"
          show-overflow-tooltip
          :min-width="120"
          :label="$t('load_test.file_name')"
        >
          <template v-slot="scope">
            <span v-if="scope.row.storage !== 'GIT'">
              {{ scope.row.name }}
            </span>
            <div v-else>
              <span>
                {{ scope.row.name }}
              </span>
              <ms-tag
                effect="plain"
                class="ms-tags"
                :content="parseGitBranch(scope.row.attachInfo)"
              />
            </div>
          </template>
        </ms-table-column>
        <ms-table-column
          sortable
          prop="type"
          :min-width="120"
          :filters="typeFilters"
          :label="$t('load_test.file_type')"
        >
        </ms-table-column>
        <ms-table-column
          :min-width="120"
          :label="$t('project.project_file.file.path')"
          prop="path"
        />
        <ms-table-column prop="description" :label="$t('group.description')" />

        <ms-table-column
          prop="tags"
          min-width="60px"
          :show-overflow-tooltip="false"
          :label="$t('commons.tag')"
        >
          <template v-slot:default="scope">
            <el-tooltip class="item" effect="dark" placement="top">
              <div v-html="getTagToolTips(scope.row.tags)" slot="content"></div>
              <div class="oneLine">
                <ms-tag
                  v-for="(itemName, index) in scope.row.tags"
                  :key="index"
                  :show-tooltip="
                    scope.row.tags.length === 1 && itemName.length * 12 <= 20
                  "
                  :content="itemName"
                  type="success"
                  effect="plain"
                  class="ms-tags"
                />
              </div>
            </el-tooltip>
            <span />
          </template>
        </ms-table-column>

        <ms-table-column
          sortable
          prop="createUser"
          :min-width="100"
          :label="$t('commons.create_user')"
        >
        </ms-table-column>
        <ms-table-column
          sortable
          prop="updateUser"
          :min-width="100"
          :label="$t('ui.update_user')"
        >
        </ms-table-column>
        <ms-table-column
          sortable
          :label="$t('commons.create_time')"
          :min-width="120"
          prop="createTime"
        >
          <template v-slot="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          sortable
          :min-width="120"
          :label="$t('commons.update_time')"
          prop="updateTime"
        >
          <template v-slot="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          :label="$t('commons.operating')"
          fixed="right"
          :width="130"
        >
          <template v-slot:default="scope">
            <ms-table-operator-button
              icon="el-icon-view"
              type="primary"
              :tip="$t('permission.project_report_analysis.read')"
              v-permission="['PROJECT_FILE:READ']"
              @exec="handleView(scope.row)"
            >
            </ms-table-operator-button>

            <ms-table-operator-button
              icon="el-icon-download"
              type="success"
              :tip="$t('project.file_download')"
              v-permission="['PROJECT_FILE:READ+DOWNLOAD+JAR']"
              @exec="handleDownload(scope.row)"
            >
            </ms-table-operator-button>
            <ms-table-operator-button
              icon="el-icon-delete"
              type="danger"
              :tip="$t('commons.delete')"
              v-permission="['PROJECT_FILE:READ+DELETE+JAR']"
              @exec="handleDelete(scope.row)"
            >
            </ms-table-operator-button>
          </template>
        </ms-table-column>
      </ms-table>
      <ms-table-pagination
        :change="getProjectFiles"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"
      />
    </div>
    <!-- 缩略图 -->
    <div v-else>
      <ms-file-thumbnail
        :data="metadataArr"
        :page="currentPage"
        :size="pageSize"
        :page-total="total"
        :condition="condition"
        :node-tree="nodeTree"
        @download="handleDownload"
        @delete="handleDelete"
        @refreshModule="refreshModule"
        @setCurrentPage="setCurrentPage"
        @change="changeList"
      />
    </div>
    <!-- 移动组建-->
    <ms-file-batch-move @refreshModule="refreshModule" ref="batchMove" />
    <!--编辑页面-->
    <ms-edit-file-metadata
      :metadata-array="metadataArr"
      :module-options="nodeTree"
      :condition="condition"
      @refreshModule="refreshModule"
      @reload="getProjectFiles"
      @download="handleDownload"
      @setCurrentPage="setCurrentPage"
      @delete="handleDelete"
      ref="editFileMetadata"
    />
    <file-metadata-dialog
      :module-id="moduleId"
      @refresh="refreshModuleAndList"
      ref="repositoryFileDialog"
    />
  </el-card>
</template>

<script>
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {
  getCurrentProjectID,
  getCurrentUserId,
} from "metersphere-frontend/src/utils/token";
import { operationConfirm } from "metersphere-frontend/src/utils";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTableHeader from "../header/FileHeader";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsFileBatchMove from "../module/FileBatchMove";
import MsFileThumbnail from "./FileThumbnail";
import MsEditFileMetadata from "../edit/EditFileMetadata";
import MsTag from "metersphere-frontend/src/components/MsTag";
import FileMetadataDialog from "@/business/menu/file/dialog/FileMetadataDialog";
import {
  batchDeleteMetaData,
  createFileMeta,
  deleteFileMetaById,
  downloadFileZip,
  downloadMetaData,
  getAllTypeFileMeta,
  getFileMetaPages,
} from "../../../../api/file";

export default {
  name: "MsFileMetadataList",
  components: {
    MsMainContainer,
    MsContainer,
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
    MsTag,
    FileMetadataDialog,
  },
  props: {
    moduleId: String,
    nodeTree: Array,
    moduleType: {
      type: String,
      default: "module",
    },
  },
  data() {
    return {
      loadFileVisible: false,
      result: {},
      data: {},
      tableLoading: false,
      cardLoading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      metadataArr: [],
      condition: {},
      projectId: getCurrentProjectID(),
      height: "calc(100vh - 160px)",
      typeFilters: [],
      buttons: [
        {
          name: this.$t("file_manage.batch_delete"),
          handleClick: this.handleDeleteBatch,
          permissions: ["PROJECT_FILE:READ+BATCH+DELETE"],
        },
        {
          name: this.$t("file_manage.batch_download"),
          handleClick: this.exportZip,
          permissions: ["PROJECT_FILE:READ+BATCH+DOWNLOAD"],
        },
        {
          name: this.$t("file_manage.batch_move"),
          handleClick: this.handleBatchMove,
          permissions: ["PROJECT_FILE:READ+BATCH+MOVE"],
        },
      ],
      showView: "list",
    };
  },
  created() {
    this.getTypes();
    this.getProjectFiles();
  },
  methods: {
    changeList(pageSize, currentPage) {
      this.currentPage = currentPage;
      this.pageSize = pageSize;
      this.getProjectFiles();
    },
    change(value) {
      this.showView = value;
    },
    refreshModule() {
      this.$emit("refreshModule");
    },
    refreshList() {
      this.$emit("refreshList");
    },
    open(project) {
      this.projectId = project.id;
      this.loadFileVisible = true;
      this.getProjectFiles();
    },
    close() {
      this.loadFileVisible = false;
      this.selectIds.clear();
    },
    myFile() {
      if (!this.condition.filters) {
        this.condition.filters = { createUser: [getCurrentUserId()] };
      } else {
        this.condition.filters.createUser = [getCurrentUserId()];
      }
      this.condition.filters.moduleIds = [];
      this.getProjectFiles();
    },
    getProjectFiles() {
      this.tableLoading = getFileMetaPages(
        this.projectId,
        this.currentPage,
        this.pageSize,
        this.condition
      ).then((res) => {
        let data = res.data;
        this.total = data.itemCount;
        this.metadataArr = data.listObject;
        this.metadataArr.forEach((item) => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
      });
    },
    refreshModuleAndList() {
      this.getProjectFiles();
      this.refreshModule();
    },
    fileValidator(file) {
      /// todo: 是否需要对文件内容和大小做限制
      return file.size > 0;
    },
    beforeUploadFile(file) {
      if (!this.fileValidator(file)) {
        return false;
      }
      if (file.size / 1024 / 1024 > 500) {
        this.$warning(this.$t("api_test.request.body_upload_limit_size"));
        return false;
      }
      return true;
    },
    handleUpload(uploadResources) {
      let file = uploadResources.file;
      let request = {
        createUser: getCurrentUserId(),
        updateUser: getCurrentUserId(),
        projectId: this.projectId,
        moduleId: this.moduleId,
      };
      this.cardLoading = createFileMeta(file, request).then(() => {
        this.$success(this.$t("commons.save_success"));
        this.getProjectFiles();
        this.refreshModule();
      });
    },
    handleExceed() {
      this.$error(this.$t("load_test.file_size_limit"));
    },
    handleView(row) {
      this.$refs.editFileMetadata.open(
        row,
        this.pageSize,
        this.currentPage,
        this.total
      );
    },
    getTagToolTips(tags) {
      try {
        let showTips = "";
        tags.forEach((item) => {
          showTips += item + ",";
        });
        return showTips.substr(0, showTips.length - 1);
      } catch (e) {
        return "";
      }
    },
    handleBatchMove() {
      this.$refs.batchMove.open(this.nodeTree, this.$refs.table.selectIds);
    },
    setCurrentPage(page) {
      if (this.currentPage !== page) {
        this.currentPage = page;
        this.getProjectFiles();
      }
    },
    handleDownload(row) {
      let type = row.type ? row.type.toLowerCase() : "";
      let name = row.name;
      if (type !== "" && !name.endsWith(type)) {
        name = name + "." + type;
      }
      downloadMetaData(row.id, name);
      // this.$fileDownload("/file/metadata/download/" + row.id, name);
    },
    parseGitBranch(attachInfo) {
      let branch = "master";
      branch = JSON.parse(attachInfo)["branch"];
      return branch;
    },
    exportZip() {
      let array = [];
      this.$refs.table.selectRows.forEach((item) => {
        let request = JSON.parse(JSON.stringify(item));
        request.tags = "";
        array.push(request);
      });
      let request = { projectId: getCurrentProjectID, requests: array };
      downloadFileZip(request);
      // this.$fileDownloadPost("/file/metadata/download/zip", request, "文件集.zip");
    },

    handleDeleteBatch() {
      operationConfirm(
        this,
        this.$t("project.file_delete_tip", [
          this.$refs.table.selectIds.length + " 条 ",
        ]),
        () => {
          batchDeleteMetaData(this.$refs.table.selectIds).then(() => {
            this.$refs.table.clear();
            this.$success(this.$t("commons.delete_success"));
            this.getProjectFiles();
            this.refreshModule();
          });
        }
      );
    },
    handleDelete(row) {
      if (row && row.confirm) {
        deleteFileMetaById(row.id).then(() => {
          this.$success(this.$t("commons.delete_success"));
          this.getProjectFiles();
          this.refreshModule();
        });
        return;
      }
      operationConfirm(
        this,
        this.$t("project.file_delete_tip", [row.name]),
        () => {
          deleteFileMetaById(row.id).then(() => {
            this.$success(this.$t("commons.delete_success"));
            this.getProjectFiles();
            this.refreshModule();
          });
        }
      );
    },
    getTypes() {
      this.typeFilters = [];
      getAllTypeFileMeta().then((res) => {
        res.data.forEach((item) => {
          this.typeFilters.push({ text: item, value: item });
        });
      });
    },
    moduleChange(ids) {
      if (!this.condition.filters) {
        this.condition.filters = { moduleIds: ids };
      } else {
        this.condition.filters.moduleIds = ids;
      }
      this.condition.filters.createUser = [];
      this.getProjectFiles();
    },
    addRepositoryFile() {
      this.$refs.repositoryFileDialog.open("create");
    },
    moveSave(param) {
      // this.buildBatchParam(param);
    },
  },
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
