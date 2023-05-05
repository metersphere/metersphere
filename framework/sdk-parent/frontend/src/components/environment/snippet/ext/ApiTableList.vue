<template>
  <span>
    <slot name="header"></slot>
    <ms-search v-if="resetOver" :condition.sync="condition" @search="search">
    </ms-search>

    <ms-table
      :data="tableData"
      :select-node-ids="selectNodeIds"
      :condition="condition"
      :page-size="pageSize"
      :total="total"
      enableSelection
      @selectCountChange="selectCountChange"
      :screenHeight="screenHeight"
      row-key="id"
      :reserve-option="true"
      :page-refresh="pageRefresh"
      operator-width="170px"
      @order="initTable"
      @filter="search"
      ref="apitable"
    >
      <ms-table-column prop="num" label="ID" min-width="80px" sortable>
      </ms-table-column>
      <ms-table-column
        prop="name"
        :label="$t('api_test.definition.api_name')"
        sortable
        width="120px"
      />

      <ms-table-column
        prop="method"
        sortable="custom"
        column-key="method"
        :filters="methodFilters"
        :label="getApiRequestTypeName"
        width="120px"
      >
        <template v-slot:default="scope">
          <el-tag
            size="mini"
            :style="{
              'background-color': getColor(true, scope.row.method),
              border: getColor(true, scope.row.method),
            }"
            class="api-el-tag"
          >
            {{ scope.row.method }}
          </el-tag>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="userName"
        sortable="custom"
        :filters="userFilters"
        column-key="user_id"
        :label="$t('api_test.definition.api_principal')"
        width="100px"
      />

      <ms-table-column
        prop="path"
        width="120px"
        :label="$t('api_test.definition.api_path')"
      />

      <ms-table-column
        prop="tags"
        :label="$t('commons.tag')"
        width="120px"
        :show-overflow-tooltip="false"
      >
        <template v-slot:default="scope">
          <el-tooltip class="item" effect="dark" placement="top">
            <div v-html="getTagToolTips(scope.row.tags)" slot="content"></div>
            <div class="oneLine">
              <ms-tag
                v-for="(itemName, index) in scope.row.tags"
                :key="index"
                type="success"
                effect="plain"
                :show-tooltip="
                  scope.row.tags.length === 1 && itemName.length * 12 <= 100
                "
                :content="itemName"
                style="margin-left: 0px; margin-right: 2px"
              />
            </div>
          </el-tooltip>
        </template>
      </ms-table-column>

      <ms-table-column
        v-if="versionEnable"
        :label="$t('project.version.name')"
        :filters="versionFilters"
        min-width="100px"
        prop="versionId"
      >
        <template v-slot:default="scope">
          <span>{{ scope.row.versionName }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        sortable="createTime"
        width="160px"
        :label="$t('commons.create_time')"
        prop="createTime"
      >
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        width="160"
        :label="$t('api_test.definition.api_last_time')"
        sortable="custom"
        prop="updateTime"
      >
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="caseTotal"
        width="80px"
        :label="$t('api_test.definition.api_case_number')"
      />
    </ms-table>
    <ms-table-pagination
      :change="pageChange"
      :current-page.sync="currentPage"
      :page-size.sync="pageSize"
      :total="total"
    />
  </span>
</template>

<script>
import MsTable from "../../../table/MsTable";
import MsTableColumn from "../../../table/MsTableColumn";
import MsTableOperator from "../../../MsTableOperator";
import MsTableOperatorButton from "../../../MsTableOperatorButton";
import MsTablePagination from "../../../pagination/TablePagination";
import MsTag from "../../../MsTag";
import MsTableAdvSearchBar from "../../../search/MsTableAdvSearchBar";
import ShowMoreBtn from "../../../table/ShowMoreBtn";
import MsSearch from "../../../search/MsSearch";
import { API_METHOD_COLOUR } from "../../../../model/JsonData";
import PriorityTableItem from "../PriorityTableItem";
import MsEnvironmentSelect from "./MsEnvironmentSelect";
import { getProtocolFilter } from "../api-definition";
import { getProjectMemberById } from "../../../../api/user";
import TableSelectCountBar from "../ext/TableSelectCountBar";

import { hasLicense } from "../../../../utils/permission";
import { isProjectVersionEnable } from "../../../../api/version";

export default {
  name: "ApiTableList",
  components: {
    TableSelectCountBar,
    MsEnvironmentSelect,
    PriorityTableItem,
    MsTableOperatorButton,
    MsTableOperator,
    MsTablePagination,
    MsTag,
    ShowMoreBtn,
    MsTable,
    MsTableColumn,
    MsTableAdvSearchBar,
    MsSearch,
  },
  data() {
    return {
      moduleId: "",
      typeArr: [{ id: "priority", name: this.$t("test_track.case.priority") }],
      priorityFilters: [
        { text: "P0", value: "P0" },
        { text: "P1", value: "P1" },
        { text: "P2", value: "P2" },
        { text: "P3", value: "P3" },
      ],
      methodColorMap: new Map(API_METHOD_COLOUR),
      methodFilters: [],
      userFilters: [],
      currentPage: 1,
      pageSize: 10,
      versionEnable: false,
      pageRefresh: false,
      resetOver: true,
    };
  },
  props: {
    currentProtocol: String,
    projectId: String,
    selectNodeIds: Array,
    result: Object,
    tableData: Array,
    condition: Object,
    total: Number,
    versionFilters: Array,
    screenHeight: {
      type: [Number, String],
      default() {
        return "calc(100vh - 400px)";
      },
    },
  },
  created() {
    this.getUserFilter();
    this.getProtocolFilter();
    this.checkVersionEnable();
  },
  watch: {
    currentProtocol() {
      this.pageRefresh = false;
      this.getProtocolFilter();
    },
    projectId() {
      this.pageRefresh = false;
      this.checkVersionEnable();
      this.getUserFilter();
    },
    selectNodeIds() {
      this.pageRefresh = false;
    },
  },
  mounted() {
    if (this.$refs.apitable) {
      this.$emit("setSelectRow", this.$refs.apitable.getSelectRows());
    } else {
      this.$emit("setSelectRow", new Set());
    }

    if (this.$refs.apitable) {
      this.$refs.apitable.doLayout();
    }
  },
  computed: {
    getApiRequestTypeName() {
      if (this.currentProtocol === "TCP") {
        return this.$t("api_test.definition.api_agreement");
      } else {
        return this.$t("api_test.definition.api_type");
      }
    },
  },
  methods: {
    selectCountChange(value) {
      this.$emit("selectCountChange", value);
      if (this.$refs.apitable) {
        this.$emit("setSelectRow", this.$refs.apitable.getSelectRows());
      } else {
        this.$emit("setSelectRow", new Set());
      }
    },
    getColor(flag, method) {
      return this.methodColorMap.get(method);
    },
    getProtocolFilter() {
      this.methodFilters = getProtocolFilter(this.currentProtocol);
    },
    getSelectIds() {
      return this.$refs.apitable.selectIds;
    },
    search() {
      // 添加搜索条件时，当前页设置成第一页
      this.currentPage = 1;
      this.initTable();
    },
    pageChange() {
      this.initTable("page");
    },
    initTable(data) {
      this.pageRefresh = data === "page";
      this.$emit("refreshTable");
    },
    clear() {
      if (this.$refs.apitable) {
        this.$refs.apitable.clear();
      }
      this.resetOver = false;
      this.$nextTick(() => {
        this.resetOver = true;
      });
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        isProjectVersionEnable(this.projectId).then((res) => {
          this.versionEnable = res.data;
        });
      }
    },
    getUserFilter() {
      // todo project id is empty
      if (!this.projectId) {
        return;
      }
      getProjectMemberById(this.projectId).then((res) => {
        this.userFilters = res.data.map((u) => {
          return { text: u.name, value: u.id };
        });
      });
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
  },
};
</script>

<style scoped>
.request-method {
  padding: 0 5px;
  color: #1e90ff;
}

.api-el-tag {
  color: white;
}

.search-input {
  float: right;
  width: 200px;
}

.adv-search-bar {
  float: right;
  margin-top: 5px;
  margin-right: 10px;
}

.oneLine {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
