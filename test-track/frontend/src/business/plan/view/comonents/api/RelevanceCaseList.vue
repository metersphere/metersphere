<template>
  <div v-loading="loading">
    <api-list-container
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange"
    >
      <template>
        <slot name="version"></slot>
      </template>
      <ms-search
        :condition.sync="condition"
        v-if="clearOver"
        @search="initTable"
      >
      </ms-search>
      <ms-table
        :data="tableData"
        :select-node-ids="selectNodeIds"
        :condition="condition"
        :page-size="pageSize"
        :total="total"
        enableSelection
        :screenHeight="screenHeight"
        row-key="id"
        :reserve-option="true"
        :page-refresh="pageRefresh"
        @refresh="initTable"
        @selectCountChange="selectCountChange"
        operator-width="170px"
        ref="table"
      >
        <ms-table-column prop="num" label="ID" width="80px" sortable="true">
        </ms-table-column>

        <ms-table-column
          prop="name"
          width="160px"
          :label="$t('test_track.case.name')"
        />

        <ms-table-column
          prop="priority"
          :filters="priorityFilters"
          column-key="priority"
          width="120px"
          :label="$t('test_track.case.priority')"
        >
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority" />
          </template>
        </ms-table-column>

        <ms-table-column
          sortable="custom"
          prop="path"
          width="180px"
          :label="'API' + $t('api_test.definition.api_path')"
        />

        <ms-table-column
          prop="tags"
          width="120px"
          :label="$t('commons.tag')"
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

        <ms-table-column prop="createUser" :label="$t('commons.create_user')" />

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
          sortable="updateTime"
          width="160px"
          :label="$t('api_test.definition.api_last_time')"
          prop="updateTime"
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>
      </ms-table>
      <ms-table-pagination
        :change="pageChange"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"
      />
      <div>
        <el-radio-group v-model="envType" style="float: left;margin-top: 8px;">
          <el-radio label="default">{{ $t("api_test.environment.default_environment") }}</el-radio>
          <el-radio label="newEnv">{{ $t("api_test.environment.choose_new_environment") }}</el-radio>
        </el-radio-group>
        <ms-environment-select
            :project-id="projectId"
            :is-read-only="isReadOnly"
            @setEnvironment="setEnvironment"
            ref="msEnvironmentSelect"
            v-if="envType==='newEnv'"
            style="float: left;margin-left: 16px"
        />
      </div>
    </api-list-container>
  </div>
</template>

<script>
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {API_METHOD_COLOUR, CASE_PRIORITY,} from "metersphere-frontend/src/model/JsonData";
import MsEnvironmentSelect from "metersphere-frontend/src/components/environment/snippet/ext/MsEnvironmentSelect";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import {TEST_PLAN_RELEVANCE_API_CASE_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import MsSearch from "metersphere-frontend/src/components/search/MsSearch";
import ApiListContainer from "@/business/plan/view/comonents/api/ApiListContainer";
import {buildBatchParam, hasLicense, isProjectVersionEnable,} from "@/business/utils/sdk-utils";
import PriorityTableItem from "@/business/common/tableItems/planview/PriorityTableItem";
import {apiDefinitionGet} from "@/api/remote/api/api-definition";
import {testPlanApiCaseRelevanceList} from "@/api/remote/plan/test-plan-api-case";

export default {
  name: "RelevanceCaseList",
  components: {
    PriorityTableItem,
    ApiListContainer,
    MsEnvironmentSelect,
    MsTablePagination,
    MsTag,
    MsTable,
    MsTableColumn,
    MsSearch,
    MsTableAdvSearchBar,
  },
  data() {
    return {
      condition: {
        components: TEST_PLAN_RELEVANCE_API_CASE_CONFIGS,
      },
      envType: 'default',
      selectCase: {},
      loading: false,
      moduleId: "",
      typeArr: [{ id: "priority", name: this.$t("test_track.case.priority") }],
      priorityFilters: [
        { text: "P0", value: "P0" },
        { text: "P1", value: "P1" },
        { text: "P2", value: "P2" },
        { text: "P3", value: "P3" },
      ],
      valueArr: {
        priority: CASE_PRIORITY,
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      screenHeight: "calc(100vh - 400px)", //屏幕高度
      tableData: [],
      currentPage: 1,
      clearOver: true,
      pageSize: 10,
      total: 0,
      environmentId: "",
      versionEnable: false,
      pageRefresh: false,
    };
  },
  props: {
    currentProtocol: String,
    selectNodeIds: Array,
    versionFilters: Array,
    currentVersion: String,
    visible: {
      type: Boolean,
      default: false,
    },
    isApiListEnable: {
      type: Boolean,
      default: false,
    },
    isReadOnly: {
      type: Boolean,
      default: false,
    },
    projectId: String,
    planId: String,
  },
  created() {
    this.condition.versionId = this.currentVersion;
    this.initTable();
    this.checkVersionEnable();
  },
  watch: {
    selectNodeIds() {
      this.initTable();
    },
    currentProtocol() {
      this.initTable();
    },
    projectId() {
      this.condition = {
        components: TEST_PLAN_RELEVANCE_API_CASE_CONFIGS,
      };
      this.selectNodeIds.length = 0;
      this.initTable();
      this.checkVersionEnable();
    },
    currentVersion() {
      this.condition.versionId = this.currentVersion;
      this.initTable();
    },
  },
  computed: {
    selectRows() {
      if (this.$refs.table) {
        return this.$refs.table.getSelectRows();
      } else {
        return new Set();
      }
    },
  },
  methods: {
    isApiListEnableChange(data) {
      this.$emit("isApiListEnableChange", data);
    },
    selectCountChange(data) {
      this.$emit("selectCountChange", data);
    },
    pageChange() {
      this.initTable(null, "page");
    },
    initTable(projectId, data) {
      this.pageRefresh = data === "page";
      this.condition.status = "";
      this.condition.moduleIds = this.selectNodeIds;
      if (projectId != null && typeof projectId === "string") {
        this.condition.projectId = projectId;
      } else if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }
      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }
      this.condition.planId = this.planId;
      this.loading = true;
      testPlanApiCaseRelevanceList(
        { pageNum: this.currentPage, pageSize: this.pageSize },
        this.condition
      ).then((response) => {
        this.loading = false;
        this.total = response.data.itemCount;
        this.tableData = response.data.listObject;
        this.tableData.forEach((item) => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
      });
    },
    clear() {
      if (this.$refs.table) {
        this.$refs.table.clear();
      }
      this.clearOver = false;
      this.$nextTick(() => {
        this.clearOver = true;
      });
    },
    clearEnvAndSelect() {
      this.environmentId = "";
      if (this.$refs.msEnvironmentSelect) {
        this.$refs.msEnvironmentSelect.environmentId = "";
      }
      this.condition.combine = undefined;
      this.clear();
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
    showExecResult(row) {
      this.visible = false;
      this.$emit("showExecResult", row);
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleTestCase(testCase) {
      apiDefinitionGet(testCase.apiDefinitionId).then((response) => {
        let api = response.data;
        let selectApi = api;
        let request = {};
        if (
          Object.prototype.toString
            .call(api.request)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() === "object"
        ) {
          request = api.request;
        } else {
          request = JSON.parse(api.request);
        }
        if (!request.hashTree) {
          request.hashTree = [];
        }
        selectApi.url = request.path;
        this.$refs.caseList.open(selectApi, testCase.id);
      });
    },
    setEnvironment(data) {
      this.environmentId = data.id;
    },
    clearSelection() {
      if (this.$refs.table) {
        this.$refs.table.clearSelectRows();
        this.$refs.table.clearSelection();
      }
    },
    getConditions() {
      let sampleSelectRows = this.$refs.table.getSelectRows();
      let batchParam = buildBatchParam(this, undefined, this.projectId);
      let param = {};
      if (batchParam.condition) {
        param = batchParam.condition;
        param.projectId = batchParam.projectId;
      } else {
        param = batchParam;
      }
      param.ids = Array.from(sampleSelectRows).map((row) => row.id);
      let tableDataIds = Array.from(this.tableData).map((row) => row.id);
      param.ids.sort((a, b) => {
        return tableDataIds.indexOf(a) - tableDataIds.indexOf(b);
      });
      return param;
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        isProjectVersionEnable(this.projectId).then((response) => {
          this.versionEnable = response.data;
        });
      }
    },
  },
};
</script>

<style scoped>
.operate-button > div {
  display: inline-block;
  margin-left: 10px;
}

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
