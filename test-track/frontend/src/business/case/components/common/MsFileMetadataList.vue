<template>
  <el-drawer
    :close-on-click-modal="false"
    :visible.sync="visible"
    :size="1000"
    @close="close"
    destroy-on-close
    ref="editFile"
    custom-class="file-drawer"
    append-to-body
  >
    <template slot="title">
      <div style="color: #1f2329; font-size: 16px; font-weight: 500">
        {{ $t("case.associated_files") }}
      </div>
    </template>
    <div class="file-box" v-if="metadataArr.length > 0">
      <div class="header both-padding">
        <ms-table-header
          title=""
          :condition.sync="condition"
          @search="getProjectFiles"
          :show-create="false"
          :show-thumbnail="false"
          @change="change"
        >
        </ms-table-header>
      </div>
      <div class="table-data both-padding">
        <ms-table
          v-loading="data.loading"
          class="basic-config"
          :screen-height="height"
          :data="metadataArr"
          :condition="condition"
          :hidePopover="true"
          @refresh="getProjectFiles"
          ref="table"
        >
          <ms-table-column
            prop="name"
            show-overflow-tooltip
            :width="100"
            :label="$t('load_test.file_name')"
          >
          </ms-table-column>
          <ms-table-column
            sortable
            prop="type"
            :filters="typeFilters"
            :label="$t('load_test.file_type')"
          >
          </ms-table-column>

          <ms-table-column prop="description" :label="$t('group.description')">
          </ms-table-column>

          <ms-table-column
            prop="tags"
            min-width="60px"
            :show-overflow-tooltip="false"
            :label="$t('commons.tag')"
          >
            <template v-slot:default="scope">
              <el-tooltip class="item" effect="dark" placement="top">
                <div
                  v-html="getTagToolTips(scope.row.tags)"
                  slot="content"
                ></div>
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
            :label="$t('commons.create_user')"
          >
          </ms-table-column>
          <ms-table-column
            sortable
            prop="updateUser"
            :label="$t('ui.update_user')"
          >
          </ms-table-column>

          <ms-table-column
            sortable
            :label="$t('commons.update_time')"
            prop="updateTime"
          >
            <template v-slot="scope">
              <span>{{ scope.row.updateTime | datetimeFormat }}</span>
            </template>
          </ms-table-column>
        </ms-table>
      </div>
      <div class="footer">
        <div class="pagination">
          <home-pagination
            :change="getProjectFiles"
            :current-page.sync="currentPage"
            :page-size.sync="pageSize"
            :total="total"
            layout="total, prev, pager, next, sizes, jumper"
          />
        </div>
        <div class="options">
          <div class="options-btn">
            <div class="check-row" v-if="selectRows">
              <div class="label">{{$t('case.selected')}} {{ selectRowsCount }} {{$t('case.strip')}}</div>
              <div class="clear" @click="clearSelect">{{$t('case.clear')}}</div>
            </div>
            <div class="cancel">
              <el-button size="small" @click="visible = false">{{
                $t("commons.cancel")
              }}</el-button>
            </div>
            <div class="submit">
              <el-button
                size="small"
                v-prevent-re-click
                :type="selectRows ? 'primary' : 'info'"
                @click="submit"
                @keydown.enter.native.prevent
              >
                {{ $t("commons.confirm") }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <!-- <el-card v-loading="result" class="table-card">
        <template v-slot:header>
          <ms-table-header
            title=""
            :condition.sync="condition"
            @search="getProjectFiles"
            :show-create="false"
            :show-thumbnail="false"
            @change="change"
          >
          </ms-table-header>
        </template>
        <ms-table
          v-loading="data.loading"
          class="basic-config"
          :screen-height="height"
          :data="metadataArr"
          :condition="condition"
          :hidePopover="true"
          @refresh="getProjectFiles"
          ref="table"
        >
          <ms-table-column
            prop="name"
            show-overflow-tooltip
            :width="100"
            :label="$t('load_test.file_name')"
          >
          </ms-table-column>
          <ms-table-column
            sortable
            prop="type"
            :filters="typeFilters"
            :label="$t('load_test.file_type')"
          >
          </ms-table-column>

          <ms-table-column prop="description" :label="$t('group.description')">
          </ms-table-column>

          <ms-table-column
            prop="tags"
            min-width="60px"
            :show-overflow-tooltip="false"
            :label="$t('commons.tag')"
          >
            <template v-slot:default="scope">
              <el-tooltip class="item" effect="dark" placement="top">
                <div
                  v-html="getTagToolTips(scope.row.tags)"
                  slot="content"
                ></div>
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
            :label="$t('commons.create_user')"
          >
          </ms-table-column>
          <ms-table-column
            sortable
            prop="updateUser"
            :label="$t('ui.update_user')"
          >
          </ms-table-column>

          <ms-table-column
            sortable
            :label="$t('commons.update_time')"
            prop="updateTime"
          >
            <template v-slot="scope">
              <span>{{ scope.row.updateTime | datetimeFormat }}</span>
            </template>
          </ms-table-column>
        </ms-table>

        <home-pagination
          :change="getProjectFiles"
          :current-page.sync="currentPage"
          :page-size.sync="pageSize"
          :total="total"
          layout="total, prev, pager, next, sizes, jumper"
          style="margin-top: 19px"
        />
      </el-card>

      <ms-dialog-footer @cancel="visible = false" @confirm="submit" /> -->
    </div>
    <div class="empty-file">
      <div class="info-wrap" style="text-align: center; margin-top: 226px">
        <div class="image">
          <img
            style="height: 100px; width: 100px; margin-bottom: 8px"
            src="/assets/module/figma/icon_none.svg"
          />
          <div class="label" style="color: #646a73">
            {{ $t("case.empty_file") }}
          </div>
        </div>
        <div class="upload-file" style="margin-top: 16px">
          <el-button type="primary" size="small" icon="el-icon-upload2">{{
            $t("case.upload_file")
          }}</el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script>
import HomePagination from "@/business/home/components/pagination/HomePagination";
import {
  getFileMetadataList,
  getMetadataTypes,
} from "metersphere-frontend/src/api/file-metadata";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {
  getCurrentProjectID,
  getCurrentUserId,
} from "metersphere-frontend/src/utils/token";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTableHeader from "metersphere-frontend/src/components/environment/commons/variable/FileHeader";
import MsTableSearchBar from "metersphere-frontend/src/components/MsTableSearchBar";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsFileBatchMove from "metersphere-frontend/src/components/environment/commons/variable/FileBatchMove";
import MsTag from "metersphere-frontend/src/components/MsTag";

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
    MsTag,
    HomePagination,
  },
  props: {
    moduleId: String,
    nodeTree: Array,
  },
  data() {
    return {
      loadFileVisible: false,
      result: false,
      data: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      metadataArr: [],
      fileNumLimit: 10,
      condition: {},
      projectId: getCurrentProjectID(),
      height: "500px",
      typeFilters: [],
      showView: "list",
      visible: false,
    };
  },
  created() {
    this.getTypes();
    this.getProjectFiles();
  },
  computed: {
    selectRows() {
      return (
        this.$refs.table &&
        this.$refs.table.selectRows &&
        this.$refs.table.selectRows.size > 0
      );
    },
    selectRowsCount() {
      if (
        this.$refs.table &&
        this.$refs.table.selectRows &&
        this.$refs.table.selectRows.size > 0
      ) {
        return this.$refs.table.selectRows.size;
      }
      return 0;
    },
  },
  methods: {
    clearSelect() {
      this.$refs.table.clearSelection();
    },
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
      this.$emit("refreshModule");
    },
    close() {
      this.visible = false;
    },
    open() {
      this.visible = true;
    },
    myFile() {
      if (!this.condition.filters) {
        this.condition.filters = { createUser: [getCurrentUserId()] };
      } else {
        this.condition.filters.createUser = [getCurrentUserId()];
      }
      this.getProjectFiles();
    },
    getProjectFiles() {
      this.data.loading = getFileMetadataList(
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
    getTypes() {
      this.typeFilters = [];
      getMetadataTypes().then((response) => {
        response.data.forEach((item) => {
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
      this.getProjectFiles();
    },
    moveSave(param) {
      this.buildBatchParam(param);
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

<style lang="scss">
@import "@/business/style/index.scss";
.file-drawer .el-drawer__header {
  height: 40px !important;
  margin: 0 !important;
  padding: 16px 24px 0 24px !important;
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
}
.file-drawer .header {
  margin-top: 24px;
  margin-bottom: 16px;
}
.file-drawer .input-row {
  width: 100%;
}
.file-drawer .file-box .both-padding {
  padding: 0 24px;
}
.file-drawer .file-box .footer {
  position: absolute;
  bottom: 0;
  width: 100%;
}
.file-drawer .file-box .footer .pagination {
  height: 68px;
  //border-top: 1px solid rgba(31, 35, 41, 0.15);
  margin: 0 24px;
  padding-top: 6px;
}
.file-drawer .file-box .footer .options {
  height: 80px;
  background: #ffffff;
  box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
  overflow: hidden;
}
.file-drawer .file-box .footer .options-btn {
  display: flex;
  margin-top: 24px;
  height: 32px;
  margin-right: 24px;
  float: right;
}
.file-drawer .file-box .footer .options-btn .submit {
  margin-left: 12px;
}
.file-drawer .file-box .check-row {
  display: flex;
  line-height: 32px;
}
.file-drawer .file-box .check-row .label {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  text-align: center;
  color: #646a73;
}
.file-drawer .file-box .check-row .clear {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  text-align: center;
  color: #783887;
  cursor: pointer;
  margin-left: 16px;
  margin-right: 16px;
}

.file-drawer .table-data .ms-table {
  height: px2rem(519) !important;
  max-height: px2rem(519) !important;
}
.empty-file {
  height: 100%;
  width: 100%;
}
</style>
