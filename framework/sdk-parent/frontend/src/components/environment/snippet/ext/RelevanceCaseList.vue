<template>
  <div v-loading="loading">
    <api-list-container
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange">

      <template>
        <slot name="version"></slot>
      </template>

      <ms-environment-select
        :project-id="projectId"
        :is-read-only="isReadOnly"
        @setEnvironment="setEnvironment"
        ref="msEnvironmentSelect"/>

      <ms-search
        :condition.sync="condition"
        @search="initTable">
      </ms-search>

      <ms-table
        :data="tableData"
        :select-node-ids="selectNodeIds"
        :condition="condition"
        :page-size="pageSize"
        :total="total" enableSelection
        :screenHeight="screenHeight"
        @refresh="initTable"
        @selectCountChange="selectCountChange"
        operator-width="170px"
        ref="table">

        <ms-table-column
          prop="num"
          label="ID"
          width="80px"
          sortable=true>
        </ms-table-column>

        <ms-table-column prop="name" width="160px" :label="$t('test_track.case.name')"/>

        <ms-table-column
          prop="priority"
          :filters="priorityFilters"
          column-key="priority"
          width="120px"
          :label="$t('test_track.case.priority')">
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority"/>
          </template>
        </ms-table-column>

        <ms-table-column
          sortable="custom"
          prop="path"
          width="180px"
          :label="'API'+ $t('api_test.definition.api_path')"/>

        <ms-table-column prop="tags" width="120px" :label="$t('commons.tag')">
          <template v-slot:default="scope">
            <ms-tag
              v-for="(itemName,index) in scope.row.tags"
              :key="index"
              type="success"
              effect="plain"
              :content="itemName"
              style="margin-left: 0; margin-right: 2px"/>
          </template>
        </ms-table-column>

        <ms-table-column
          v-if="versionEnable"
          :label="$t('project.version.name')"
          :filters="versionFilters"
          min-width="100px"
          prop="versionId">
          <template v-slot:default="scope">
            <span>{{ scope.row.versionName }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="createUser"
          :label="$t('commons.create_user')"/>

        <ms-table-column
          sortable="createTime"
          width="160px"
          :label="$t('commons.create_time')"
          prop="createTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          sortable="updateTime"
          width="160px"
          :label="$t('api_test.definition.api_last_time')"
          prop="updateTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>
      </ms-table>
      <ms-table-pagination
        :change="initTable"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"/>
    </api-list-container>

  </div>

</template>

<script>

import MsTable from "../../../table/MsTable";
import MsTableColumn from "../../../table/MsTableColumn";
import MsTableOperator from "../../../../components/MsTableOperator";
import MsTableOperatorButton from "../../../../components/MsTableOperatorButton";
import MsTablePagination from "../../../../components/pagination/TablePagination";
import ShowMoreBtn from "../../../table/ShowMoreBtn";
import MsTag from "../../../../components/MsTag";
import MsTableAdvSearchBar from "../../../../components/search/MsTableAdvSearchBar";
import {_filter, _sort, buildBatchParam} from "../../../../utils/tableUtils";
import {TEST_PLAN_RELEVANCE_API_CASE_CONFIGS} from "../../../../components/search/search-components";
import {hasLicense} from "../../../../utils/permission";
import MsSearch from "../../../../components/search/MsSearch";

import ApiListContainer from "./ApiListContainer";
import PriorityTableItem from "../PriorityTableItem";
import MsEnvironmentSelect from "./MsEnvironmentSelect";
import {isProjectVersionEnable} from "../../../../api/version";
import {getApiDefinitionById, getApiTestCasePages} from "../../../../api/environment";

export default {
  name: "RelevanceCaseList",
  components: {
    MsEnvironmentSelect,
    PriorityTableItem,
    ApiListContainer,
    MsTableOperatorButton,
    MsTableOperator,
    MsTablePagination,
    MsTag,
    ShowMoreBtn,
    MsTable,
    MsTableColumn,
    MsSearch,
    MsTableAdvSearchBar
  },
  data() {
    return {
      condition: {
        components: TEST_PLAN_RELEVANCE_API_CASE_CONFIGS
      },
      selectCase: {},
      result: {},
      loading: false,
      moduleId: "",
      typeArr: [
        {id: 'priority', name: this.$t('test_track.case.priority')},
      ],
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      screenHeight: 'calc(100vh - 400px)',//屏幕高度
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      environmentId: "",
      versionEnable: false,
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
    isScript: {
      type: Boolean,
      default() {
        return false;
      }
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
    isCaseRelevance: {
      type: Boolean,
      default: false,
    },
    projectId: String,
    planId: String,
    isTestPlan: Boolean,
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
        components: TEST_PLAN_RELEVANCE_API_CASE_CONFIGS
      };
      this.selectNodeIds.length = 0;
      this.initTable();
      this.checkVersionEnable();
    },
    currentVersion() {
      this.condition.versionId = this.currentVersion;
      this.initTable();
    }
  },
  computed: {
    selectRows() {
      if (this.$refs.table) {
        return this.$refs.table.getSelectRows();
      } else {
        return new Set();
      }
    }
  },
  methods: {
    isApiListEnableChange(data) {
      this.$emit('isApiListEnableChange', data);
    },
    selectCountChange(data) {
      this.$emit('selectCountChange', data);
    },
    initTable(projectId) {
      this.condition.status = "";
      this.condition.moduleIds = this.selectNodeIds;
      if (projectId != null && typeof projectId === 'string') {
        this.condition.projectId = projectId;
      } else if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }
      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }

      this.loading = getApiTestCasePages(this.currentPage, this.pageSize, this.condition)
        .then(response => {
          let {itemCount, listObject} = response.data;
          this.total = itemCount;
          this.tableData = listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
        })
    },
    clear() {
      if (this.$refs.table) {
        this.$refs.table.clear();
      }
    },
    clearEnvAndSelect() {
      this.environmentId = "";
      if (this.$refs.msEnvironmentSelect) {
        this.$refs.msEnvironmentSelect.environmentId = "";
      }
      this.clear();
    },
    showExecResult(row) {
      this.visible = false;
      this.$emit('showExecResult', row);
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTable();
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.initTable();
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleTestCase(testCase) {
      getApiDefinitionById(testCase.apiDefinitionId).then(response => {
        let api = response.data;
        let selectApi = api;
        let request = {};
        if (Object.prototype.toString.call(api.request).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
          request = api.request;
        } else {
          request = JSON.parse(api.request);
        }
        if (!request.hashTree) {
          request.hashTree = [];
        }
        selectApi.url = request.path;
        this.$refs.caseList.open(selectApi, testCase.id);
      })
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
      param.ids = Array.from(sampleSelectRows).map(row => row.id);
      let tableDataIds = Array.from(this.tableData).map(row => row.id);
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
        isProjectVersionEnable(this.projectId).then(response => {
          this.versionEnable = response.data;
        })
      }
    }
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
  color: #1E90FF;
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

</style>
