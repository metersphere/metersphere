<template>
  <div>
    <api-list-container
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange">

      <ms-environment-select :project-id="projectId" v-if="isTestPlan" :is-read-only="isReadOnly"
                             @setEnvironment="setEnvironment"/>

      <el-input :placeholder="$t('commons.search_by_name_or_id')" @blur="initTable" class="search-input" size="small"
                @keyup.enter.native="initTable" v-model="condition.name"/>

      <ms-table :data="tableData" :select-node-ids="selectNodeIds" :condition="condition" :page-size="pageSize"
                :total="total" enableSelection
                :screenHeight="screenHeight"
                operator-width="170px"
                @refresh="initTable"
                ref="apitable">

        <ms-table-column
          prop="num"
          label="ID"
          min-width="80px"
          sortable>
<!--          <template slot-scope="scope">-->
<!--            &lt;!&ndash; 判断为只读用户的话不可点击ID进行编辑操作 &ndash;&gt;-->
<!--            <span style="cursor:pointer" v-if="isReadOnly"> {{ scope.row.num }} </span>-->
<!--            <el-tooltip v-else content="编辑">-->
<!--              <a style="cursor:pointer" @click="editApi(scope.row)"> {{ scope.row.num }} </a>-->
<!--            </el-tooltip>-->
<!--          </template>-->
        </ms-table-column>
        <ms-table-column
          prop="name"
          :label="$t('api_test.definition.api_name')"
          sortable
          width="120px"/>

        <ms-table-column
          prop="method"
          sortable="custom"
          column-key="method"
          :filters="methodFilters"
          :label="getApiRequestTypeName"
          width="120px">
          <template v-slot:default="scope">
            <el-tag size="mini"
                    :style="{'background-color': getColor(true, scope.row.method), border: getColor(true, scope.row.method)}"
                    class="api-el-tag">
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
          width="100px"/>

        <ms-table-column
          prop="path"
          width="120px"
          :label="$t('api_test.definition.api_path')"/>

        <ms-table-column
          prop="tags"
          :label="$t('commons.tag')"
          width="120px">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :show-tooltip="true" :content="itemName"
                    style="margin-left: 0px; margin-right: 2px"/>
          </template>
        </ms-table-column>

        <ms-table-column
          width="160"
          :label="$t('api_test.definition.api_last_time')"
          sortable="custom"
          prop="updateTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="caseTotal"
          width="80px"
          :label="$t('api_test.definition.api_case_number')"/>

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
  import {_filter, _sort, buildBatchParam, getLabel,} from "@/common/js/tableUtils";
  import {WORKSPACE_ID} from "@/common/js/constants";
  import {getCurrentProjectID} from "@/common/js/utils";

  export default {
    name: "RelevanceApiList",
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
        deletePath: "/test/case/delete",
        screenHeight: 'calc(100vh - 400px)',//屏幕高度,
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
        environmentId: "",
        methodFilters: [
          {text: 'GET', value: 'GET'},
          {text: 'POST', value: 'POST'},
          {text: 'PUT', value: 'PUT'},
          {text: 'PATCH', value: 'PATCH'},
          {text: 'DELETE', value: 'DELETE'},
          {text: 'OPTIONS', value: 'OPTIONS'},
          {text: 'HEAD', value: 'HEAD'},
          {text: 'CONNECT', value: 'CONNECT'},
          {text: 'DUBBO', value: 'DUBBO'},
          {text: 'dubbo://', value: 'dubbo://'},
          {text: 'SQL', value: 'SQL'},
          {text: 'TCP', value: 'TCP'},
        ],
        userFilters: [],
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
      if (this.$refs.apitable) {
        this.$refs.apitable.clearSelectRows();
      }
      this.initTable();
      this.getMaintainerOptions();
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
        if (this.$refs.apitable) {
          return this.$refs.apitable.getSelectRows();
        } else {
          return new Set();
        }
      },
      getApiRequestTypeName(){
        if(this.currentProtocol === 'TCP'){
          return this.$t('api_test.definition.api_agreement');
        }else{
          return this.$t('api_test.definition.api_type');
        }
      }
    },
    methods: {
      isApiListEnableChange(data) {
        this.$emit('isApiListEnableChange', data);
      },
      initTable(projectId) {
        this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
        this.condition.moduleIds = this.selectNodeIds;
        if (this.trashEnable) {
          this.condition.filters = {status: ["Trash"]};
          this.condition.moduleIds = [];
        }
        if (projectId != null && typeof projectId === 'string') {
          this.condition.projectId = projectId;
        } else if (this.projectId != null) {
          this.condition.projectId = this.projectId;
        }

        if (this.currentProtocol != null) {
          this.condition.protocol = this.currentProtocol;
        } else {
          this.condition.protocol = "HTTP";
        }

        let url = '/api/definition/list/';
        if (this.isTestPlan) {
          url = '/api/definition/list/relevance/';
          this.condition.planId = this.planId;
        }
        this.result = this.$post(url + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
          this.genProtocalFilter(this.condition.protocol);
          this.$nextTick(function () {
            if (this.$refs.apitable) {
              this.$refs.apitable.doLayout();
              this.$refs.apitable.checkTableRowIsSelect();
            }
          });
        });
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
      getColor(flag, method) {
        return this.methodColorMap.get(method);
      },
      setEnvironment(data) {
        this.environmentId = data.id;
      },
      clearSelection() {
        if (this.$refs.apitable) {
          this.$refs.apitable.clearSelectRows();
          this.$refs.apitable.clearSelection();
        }
      },
      genProtocalFilter(protocalType) {
        if (protocalType === "HTTP") {
          this.methodFilters = [
            {text: 'GET', value: 'GET'},
            {text: 'POST', value: 'POST'},
            {text: 'PUT', value: 'PUT'},
            {text: 'PATCH', value: 'PATCH'},
            {text: 'DELETE', value: 'DELETE'},
            {text: 'OPTIONS', value: 'OPTIONS'},
            {text: 'HEAD', value: 'HEAD'},
            {text: 'CONNECT', value: 'CONNECT'},
          ];
        } else if (protocalType === "TCP") {
          this.methodFilters = [
            {text: 'TCP', value: 'TCP'},
          ];
        } else if (protocalType === "SQL") {
          this.methodFilters = [
            {text: 'SQL', value: 'SQL'},
          ];
        } else if (protocalType === "DUBBO") {
          this.methodFilters = [
            {text: 'DUBBO', value: 'DUBBO'},
            {text: 'dubbo://', value: 'dubbo://'},
          ];
        } else {
          this.methodFilters = [
            {text: 'GET', value: 'GET'},
            {text: 'POST', value: 'POST'},
            {text: 'PUT', value: 'PUT'},
            {text: 'PATCH', value: 'PATCH'},
            {text: 'DELETE', value: 'DELETE'},
            {text: 'OPTIONS', value: 'OPTIONS'},
            {text: 'HEAD', value: 'HEAD'},
            {text: 'CONNECT', value: 'CONNECT'},
            {text: 'DUBBO', value: 'DUBBO'},
            {text: 'dubbo://', value: 'dubbo://'},
            {text: 'SQL', value: 'SQL'},
            {text: 'TCP', value: 'TCP'},
          ];
        }
      },
      getMaintainerOptions() {
        this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()},response => {
          this.valueArr.userId = response.data;
          this.userFilters = response.data.map(u => {
            return {text: u.name, value: u.id};
          });
        });
      },
      getConditions() {
        let sampleSelectRows = this.$refs.apitable.getSelectRows();
        let param = buildBatchParam(this);
        param.ids = Array.from(sampleSelectRows).map(row => row.id);
        return param;
      },
      clear() {
        if (this.$refs.apitable) {
          this.$refs.apitable.clear();
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
    width: 30%;
    margin-bottom: 20px;
    margin-right: 20px;
  }

</style>
