<template>
  <div>
    <api-list-container
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange">

      <ms-environment-select :project-id="projectId" v-if="isTestPlan" :is-read-only="isReadOnly"
                             @setEnvironment="setEnvironment"/>

      <el-input :placeholder="$t('commons.search_by_name_or_id')" @blur="initTable"
                @keyup.enter.native="initTable" class="search-input" size="small" v-model="condition.name"/>

      <ms-table :data="tableData" :select-node-ids="selectNodeIds" :condition="condition" :page-size="pageSize"
                :total="total" enableSelection
                :screenHeight="screenHeight"
                @refresh="initTable"
                operator-width="170px"
                ref="table"
      >

        <ms-table-column
          prop="num"
          label="ID"
          width="80px"
          sortable=true>
          <!--          <template slot-scope="scope">-->
          <!--            &lt;!&ndash; 判断为只读用户的话不可点击ID进行编辑操作 &ndash;&gt;-->
          <!--            <span style="cursor:pointer" v-if="isReadOnly"> {{ scope.row.num }} </span>-->
          <!--            <el-tooltip v-else content="编辑">-->
          <!--              <a style="cursor:pointer" @click="editApi(scope.row)"> {{ scope.row.num }} </a>-->
          <!--            </el-tooltip>-->
          <!--          </template>-->
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

        <ms-table-column
          sortable="custom"
          prop="casePath"
          width="180px"
          :label="$t('api_test.definition.request.case')+ $t('api_test.definition.api_path')"/>

        <ms-table-column prop="tags" width="120px" :label="$t('commons.tag')">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="createUser"
          :label="'创建人'"/>

        <ms-table-column
          sortable="updateTime"
          width="160px"
          :label="$t('api_test.definition.api_last_time')"
          prop="updateTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>
      </ms-table>
      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </api-list-container>

    <table-select-count-bar :count="selectRows.size"/>

  </div>

</template>

<script>

import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTableOperator from "../../../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";
import MsTablePagination from "../../../../common/pagination/TablePagination";
import MsTag from "../../../../common/components/MsTag";
import MsBottomContainer from "../../../definition/components/BottomContainer";
import ShowMoreBtn from "../../../../track/case/components/ShowMoreBtn";
import MsBatchEdit from "../../../definition/components/basis/BatchEdit";
import {API_METHOD_COLOUR, CASE_PRIORITY} from "../../../definition/model/JsonData";
import ApiListContainer from "../../../definition/components/list/ApiListContainer";
import PriorityTableItem from "../../../../track/common/tableItems/planview/PriorityTableItem";
import MsEnvironmentSelect from "../../../definition/components/case/MsEnvironmentSelect";
import TableSelectCountBar from "./TableSelectCountBar";
import {_filter, _sort, buildBatchParam} from "@/common/js/tableUtils";
import {API_CASE_LIST} from "@/common/js/constants";

export default {
  name: "RelevanceCaseList",
  components: {
    TableSelectCountBar,
    MsEnvironmentSelect,
    PriorityTableItem,
    ApiListContainer,
    MsTableOperatorButton,
    MsTableOperator,
    MsTablePagination,
    MsTag,
    MsBottomContainer,
    ShowMoreBtn,
    MsBatchEdit,
    MsTable,
    MsTableColumn
  },
  data() {
    return {
      condition: {},
      selectCase: {},
      result: {},
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
      valueArr: {
        priority: CASE_PRIORITY,
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      screenHeight: 'calc(100vh - 400px)',//屏幕高度
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
    planId: String,
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
        url = '/test/plan/api/case/relevance/list/';
        this.condition.planId = this.planId;
      } else {
        this.condition.ids = [];
      }

      this.result = this.$post(url + this.currentPage + "/" + this.pageSize, this.condition, response => {
        this.total = response.data.itemCount;
        this.tableData = response.data.listObject;
        this.tableData.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
        this.$nextTick(function () {
          if (this.$refs.table) {
            this.$refs.table.doLayout();
            this.$refs.table.checkTableRowIsSelect();
          }
        });
      });
    },
    clear() {
      if (this.$refs.table) {
        this.$refs.table.clear();
      }
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
      if (this.$refs.table) {
        this.$refs.table.clearSelectRows();
        this.$refs.table.clearSelection();
      }
    },
    getConditions() {
      let sampleSelectRows = this.$refs.table.getSelectRows();
      let batchParam = buildBatchParam(this);
      let param = {};
      if (batchParam.condition) {
        param = batchParam.condition;
        param.projectId = batchParam.projectId;
      } else {
        param = batchParam;
      }
      param.ids = Array.from(sampleSelectRows).map(row => row.id);
      return param;
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
