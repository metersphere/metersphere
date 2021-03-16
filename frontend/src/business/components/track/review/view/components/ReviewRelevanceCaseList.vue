<template>
  <div>
    <api-list-container
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange">
      <ms-environment-select :project-id="projectId" v-if="isTestPlan" :is-read-only="isReadOnly"
                             @setEnvironment="setEnvironment"/>
      <el-input :placeholder="$t('api_test.definition.request.select_case')" @blur="initTable"
                @keyup.enter.native="initTable" class="search-input" size="small" v-model="condition.name"/>

      <el-table v-loading="result.loading"
                border
                :data="tableData"
                row-key="id"
                class="test-content adjust-table"
                @select-all="handleSelectAll"
                @filter-change="filter"
                @sort-change="sort"
                @select="handleSelect" ref="table">
        <el-table-column reserve-selection type="selection"/>

        <el-table-column prop="name" :label="$t('api_test.definition.api_name')" show-overflow-tooltip/>

        <el-table-column
          prop="priority"
          :filters="priorityFilters"
          column-key="priority"
          :label="$t('test_track.case.priority')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority"/>
          </template>
        </el-table-column>

        <el-table-column
          prop="path"
          :label="$t('api_test.definition.api_path')"
          show-overflow-tooltip/>

        <el-table-column
          prop="createUser"
          :label="'创建人'"
          show-overflow-tooltip/>

        <el-table-column
          sortable="custom"
          width="160"
          :label="$t('api_test.definition.api_last_time')"
          prop="updateTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

      </el-table>
      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </api-list-container>
    <table-select-count-bar :count="selectRows.size"/>
  </div>
</template>

<script>
import ApiListContainer from "@/business/components/api/definition/components/list/ApiListContainer";
import MsEnvironmentSelect from "@/business/components/api/definition/components/case/MsEnvironmentSelect";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import TableSelectCountBar from "@/business/components/api/automation/scenario/api/TableSelectCountBar";
import {API_METHOD_COLOUR, CASE_PRIORITY} from "@/business/components/api/definition/model/JsonData";
import {_filter, _handleSelect, _handleSelectAll, _sort} from "@/common/js/tableUtils";
import PriorityTableItem from "@/business/components/track/common/tableItems/planview/PriorityTableItem";

export default {
  name: "ReviewRelevanceCaseList",
  components: {PriorityTableItem, TableSelectCountBar, MsTablePagination, MsEnvironmentSelect, ApiListContainer},
  data() {
    return {
      condition: {},
      selectCase: {},
      result: {},
      moduleId: "",
      selectRows: new Set(),
      typeArr: [
        {id: 'priority', name: this.$t('test_track.case.priority')},
      ],
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      valueArr: {
        priority: CASE_PRIORITY,
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      environmentId: ""
    }
  },
  props: {
    currentProtocol: String,
    selectNodeIds: Array,
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
      default: false
    },
    isCaseRelevance: {
      type: Boolean,
      default: false,
    },
    projectId: String,
    reviewId: String,
    isTestPlan: Boolean
  },
  created: function () {
    this.initTable();
  },
  watch: {
    selectNodeIds() {
      this.initTable();
    },
    currentProtocol() {
      this.initTable();
    },
    projectId() {
      this.initTable();
    }
  },
  methods: {
    isApiListEnableChange(data) {
      this.$emit('isApiListEnableChange', data);
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
      let url = '/api/testcase/list/';
      if (this.isTestPlan) {
        url = '/test/case/review/api/case/relevance/list/';
        this.condition.reviewId = this.reviewId;
      }

      this.result = this.$post(url + this.currentPage + "/" + this.pageSize, this.condition, response => {
        this.total = response.data.itemCount;
        this.tableData = response.data.listObject;
      });
    },

    handleSelect(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
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
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.tableData, this.selectRows);
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleTestCase(testCase) {
      this.$get('/api/definition/get/' + testCase.apiDefinitionId, (response) => {
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
      });
    },
    setEnvironment(data) {
      this.environmentId = data.id;
    },
    clearSelection() {
      this.selectRows = new Set();
      if (this.$refs.table) {
        this.$refs.table.clearSelection();
      }
    }
  },
}
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
  width: 300px;
  /*margin-bottom: 20px;*/
  margin-right: 20px;
}

</style>
