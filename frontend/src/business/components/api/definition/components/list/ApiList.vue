<template>
  <div>
    <div>
      <el-link type="primary" @click="open" style="float: right;margin-top: 5px">{{ $t('commons.adv_search.title') }}
      </el-link>
      <el-input :placeholder="$t('commons.search_by_id_name_tag_path')" @blur="search" class="search-input" size="small"
                @keyup.enter.native="enterSearch"
                v-model="condition.name" ref="inputVal"/>

      <ms-table :data="tableData" :select-node-ids="selectNodeIds" :condition="condition" :page-size="pageSize"
                :total="total" enableSelection
                :batch-operators="trashEnable ? trashButtons : buttons" :screen-height="screenHeight"
                :operators="tableOperatorButtons" operator-width="200px"
                @refresh="initTable"
                :fields.sync="fields"
                :table-is-loading="this.result.loading"
                :field-key="tableHeaderKey"
                @saveSortField="saveSortField"
                ref="table">
        <ms-table-column
          prop="deleteTime"
          sortable
          v-if="this.trashEnable"
          :fields-width="fieldsWidth"
          :label="$t('commons.delete_time')"
          min-width="150px">
          <template v-slot:default="scope">
            <span>{{ scope.row.deleteTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="deleteUser"
          :fields-width="fieldsWidth"
          v-if="this.trashEnable"
          :label="$t('commons.delete_user')"
          min-width="120"/>
        <span v-for="(item) in fields" :key="item.key">
          <ms-table-column
               prop="num"
               label="ID"
               :field="item"
               min-width="100px"
               :fields-width="fieldsWidth"
               sortable>

            <template slot-scope="scope">
              <el-tooltip content="编辑">
                <a style="cursor:pointer" @click="editApi(scope.row)"> {{ scope.row.num }} </a>
              </el-tooltip>
            </template>
          </ms-table-column>

        <ms-table-column
          prop="name"
          :label="$t('api_test.definition.api_name')"
          sortable="custom"
          :fields-width="fieldsWidth"
          min-width="120"
          :field="item"/>

        <ms-table-column
          prop="status"
          sortable="custom"
          :filters="statusFilters"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="120px"
          :label="$t('api_test.definition.api_status')">

          <template v-slot:default="scope">
          <span class="el-dropdown-link">
            <api-status :value="scope.row.status"/>
          </span>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="method"
          sortable="custom"
          :field="item"
          :filters="methodFilters"
          :fields-width="fieldsWidth"
          min-width="120px"
          :label="getApiRequestTypeName">
          <template v-slot:default="scope" class="request-method">
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
          :field="item"
          :fields-width="fieldsWidth"
          min-width="100px"
          :label="$t('api_test.definition.api_principal')"/>\
        <ms-table-column
          prop="path"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="100px"
          :label="$t('api_test.definition.api_path')"/>

        <ms-table-column
          prop="tags"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="100px"
          :label="$t('commons.tag')">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :show-tooltip="true" :content="itemName"
                    style="margin-left: 0px; margin-right: 2px"/>
          </template>
        </ms-table-column>

        <ms-table-column
          :label="$t('api_test.definition.api_last_time')"
          :field="item"
          :fields-width="fieldsWidth"
          sortable="custom"
          min-width="140px"
          prop="updateTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>
        <ms-table-column prop="createTime"
                         :field="item"
                         :fields-width="fieldsWidth"
                         :label="$t('commons.create_time')"
                         sortable
                         min-width="180px">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column >

        <ms-table-column
          prop="caseTotal"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="100px"
          sortable
          :label="$t('api_test.definition.api_case_number')"/>

        <ms-table-column
          :field="item"
          prop="caseStatus"
          :fields-width="fieldsWidth"
          min-width="100px"
          :label="$t('api_test.definition.api_case_status')"/>

        <ms-table-column
          prop="casePassingRate"
          :field="item"
          min-width="120px"
          sortable
          :fields-width="fieldsWidth"
          :label="$t('api_test.definition.api_case_passing_rate')"/>
        </span>
      </ms-table>
      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </div>
    <ms-api-case-list @refresh="initTable" @showExecResult="showExecResult" :currentApi="selectApi" ref="caseList"/>
    <!--批量编辑-->
    <ms-batch-edit ref="batchEdit" @batchEdit="batchEdit" :data-count="$refs.table ? $refs.table.selectDataCounts : 0" :typeArr="typeArr" :value-arr="valueArr"/>
    <!--高级搜索-->
    <ms-table-adv-search-bar :condition.sync="condition" :showLink="false" ref="searchBar" @search="search"/>
    <case-batch-move @refresh="initTable" @moveSave="moveSave" ref="testCaseBatchMove"></case-batch-move>
  </div>

</template>

<script>

import MsTableHeader from '../../../../common/components/MsTableHeader';
import MsTableOperator from "../../../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";
import MsTableButton from "../../../../common/components/MsTableButton";
import MsTablePagination from "../../../../common/pagination/TablePagination";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTag from "../../../../common/components/MsTag";
import MsApiCaseList from "../case/ApiCaseList";
import MsContainer from "../../../../common/components/MsContainer";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsBottomContainer from "../BottomContainer";
import MsBatchEdit from "../basis/BatchEdit";
import {API_METHOD_COLOUR, API_STATUS, DUBBO_METHOD, REQ_METHOD, SQL_METHOD, TCP_METHOD} from "../../model/JsonData";
import {downloadFile, getCurrentProjectID} from "@/common/js/utils";
import {API_LIST, PROJECT_NAME, WORKSPACE_ID} from '@/common/js/constants';
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import ApiStatus from "@/business/components/api/definition/components/list/ApiStatus";
import MsTableAdvSearchBar from "@/business/components/common/components/search/MsTableAdvSearchBar";
import {API_DEFINITION_CONFIGS} from "@/business/components/common/components/search/search-components";
import MsTipButton from "@/business/components/common/components/MsTipButton";
import CaseBatchMove from "@/business/components/api/definition/components/basis/BatchMove";
import {
  initCondition,
  getCustomTableHeader, getCustomTableWidth, buildBatchParam, checkTableRowIsSelected,
  saveLastTableSortField,getLastTableSortField
} from "@/common/js/tableUtils";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import {Body} from "@/business/components/api/definition/model/ApiTestModel";
import {buildNodePath} from "@/business/components/api/definition/model/NodeTree";


export default {
  name: "ApiList",
  components: {
    HeaderLabelOperate,
    CaseBatchMove,
    ApiStatus,
    MsTableHeaderSelectPopover,
    MsTableButton,
    MsTableOperatorButton,
    MsTableOperator,
    MsTableHeader,
    MsTablePagination,
    MsTag,
    MsApiCaseList,
    MsContainer,
    MsBottomContainer,
    MsBatchEdit,
    MsTipButton,
    MsTableAdvSearchBar,
    MsTable,
    MsTableColumn
  },
  data() {
    return {
      type: API_LIST,
      tableHeaderKey:"API_DEFINITION",
      fields: getCustomTableHeader('API_DEFINITION'),
      fieldsWidth: getCustomTableWidth('API_DEFINITION'),
      condition: {
        components: API_DEFINITION_CONFIGS
      },
      selectApi: {},
      result: {},
      moduleId: "",
      selectDataRange: "all",
      deletePath: "/test/case/delete",
      buttons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_API']
        },
        {
          name: this.$t('api_test.definition.request.batch_edit'),
          handleClick: this.handleEditBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        },
        {
          name: this.$t('api_test.definition.request.batch_move'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        }
      ],
      trashButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_API']
        },
        {
          name: "批量恢复", handleClick: this.handleBatchRestore
        },
      ],
      tableOperatorButtons: [],
      tableUsualOperatorButtons: [
        {
          tip: this.$t('api_test.automation.execute'),
          icon: "el-icon-video-play",
          exec: this.runApi,
          class: "run-button",
          permissions: ['PROJECT_API_DEFINITION:READ+RUN']
        },
        {
          tip: this.$t('commons.edit'),
          icon: "el-icon-edit",
          exec: this.editApi,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        },
        {
          tip: "CASE",
          exec: this.handleTestCase,
          isDivButton: true,
          type: "primary",
          permissions: ['PROJECT_API_DEFINITION:READ+CREATE_CASE']
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.handleDelete,
          icon: "el-icon-delete",
          type: "danger",
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_API']
        },
        {
          tip: this.$t('commons.copy'),
          exec: this.handleCopy,
          icon: "el-icon-document-copy",
          type: "primary",
          permissions: ['PROJECT_API_DEFINITION:READ+COPY_API']
        },
      ],
      tableTrashOperatorButtons: [
        {tip: this.$t('commons.reduction'), icon: "el-icon-refresh-left", exec: this.reductionApi},
        {
          tip: this.$t('commons.delete'),
          exec: this.handleDelete,
          icon: "el-icon-delete",
          type: "danger",
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_API']
        },
      ],
      typeArr: [
        {id: 'status', name: this.$t('api_test.definition.api_status')},
        {id: 'method', name: this.$t('api_test.definition.api_type')},
        {id: 'userId', name: this.$t('api_test.definition.api_principal')},
      ],
      statusFilters: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
        {text: this.$t('test_track.plan.plan_status_completed'), value: 'Completed'},
        {text: this.$t('test_track.plan.plan_status_trash'), value: 'Trash'},
      ],
      caseStatusFilters: [
        {text: this.$t('api_test.home_page.detail_card.unexecute'), value: '未执行'},
        {text: this.$t('test_track.review.pass'), value: '通过'},
        {text: this.$t('test_track.review.un_pass'), value: '未通过'},
      ],
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
      valueArr: {
        status: API_STATUS,
        method: REQ_METHOD,
        userId: [],
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: 'calc(100vh - 258px)',//屏幕高度,
      environmentId: undefined,
      selectDataCounts: 0,
      projectName: "",
    };
  },
  props: {
    currentProtocol: String,
    selectNodeIds: Array,
    isSelectThisWeek: String,
    activeDom: String,
    initApiTableOpretion: String,
    visible: {
      type: Boolean,
      default: false,
    },
    isCaseRelevance: {
      type: Boolean,
      default: false,
    },
    trashEnable: {
      type: Boolean,
      default: false,
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
    moduleTree: {
      type: Array,
      default() {
        return [];
      },
    },
    moduleOptions: {
      type: Array,
      default() {
        return [];
      },
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    getApiRequestTypeName(){
      if(this.currentProtocol === 'TCP'){
        return this.$t('api_test.definition.api_agreement');
      }else{
        return this.$t('api_test.definition.api_type');
      }
    }
  },
  created: function () {
    if(!this.projectName || this.projectName === ""){
      this.getProjectName();
    }
    if (this.trashEnable) {
      this.tableOperatorButtons = this.tableTrashOperatorButtons;
      this.condition.filters = {status: ["Trash"]};
    } else {
      this.tableOperatorButtons = this.tableUsualOperatorButtons;
      this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
    }
    let orderArr = this.getSortField();
    if(orderArr){
      this.condition.orders = orderArr;
    }
    this.initTable();
    this.getMaintainerOptions();
  },
  watch: {
    selectNodeIds() {
      if(!this.trashEnable){
        initCondition(this.condition, false);
        this.currentPage = 1;
        this.condition.moduleIds = [];
        this.condition.moduleIds.push(this.selectNodeIds);
        this.closeCaseModel();
        this.initTable();
      }
    },
    currentProtocol() {
      this.currentPage = 1;
      initCondition(this.condition, false);
      this.closeCaseModel();
      this.initTable();
    },
    trashEnable() {
      if (this.trashEnable) {
        this.tableOperatorButtons = this.tableTrashOperatorButtons;
        this.condition.filters = {status: ["Trash"]};
        this.condition.moduleIds = [];
      } else {
        this.tableOperatorButtons = this.tableUsualOperatorButtons;
        this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
      }
      initCondition(this.condition, false);
      this.closeCaseModel();
      this.initTable();
    }
  },
  methods: {
    getProjectName (){
      this.$get('project/get/' + this.projectId, response => {
        let project = response.data;
        if(project){
          this.projectName = project.name;
        }
      });
    },
    handleBatchMove() {
      this.$refs.testCaseBatchMove.open(this.moduleTree, [], this.moduleOptions);
    },
    closeCaseModel(){
      //关闭案例弹窗
      if(this.$refs.caseList){
        this.$refs.caseList.handleClose();
      }
    },
    initTable() {
      if (this.$refs.table) {
        this.$refs.table.clear();
      }

      initCondition(this.condition, this.condition.selectAll);
      this.selectDataCounts = 0;
      if(!this.trashEnable){
        this.condition.moduleIds = this.selectNodeIds;
      }
      this.condition.projectId = this.projectId;
      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }

      //检查是否只查询本周数据
      this.getSelectDataRange();
      this.condition.selectThisWeedData = false;
      this.condition.apiCaseCoverage = null;
      switch (this.selectDataRange) {
        case 'thisWeekCount':
          this.condition.selectThisWeedData = true;
          break;
        case 'uncoverage':
          this.condition.apiCaseCoverage = 'uncoverage';
          break;
        case 'coverage':
          this.condition.apiCaseCoverage = 'coverage';
          break;
        case 'Prepare':
          this.condition.filters.status = [this.selectDataRange];
          break;
        case 'Completed':
          this.condition.filters.status = [this.selectDataRange];
          break;
        case 'Underway':
          this.condition.filters.status = [this.selectDataRange];
          break;
      }
      if (this.condition.projectId) {
        this.result = this.$post("/api/definition/list/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.genProtocalFilter(this.condition.protocol);
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });

          checkTableRowIsSelected(this, this.$refs.table);
        });
      }
      if(this.needRefreshModule()){
        this.$emit("refreshTree");
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
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.valueArr.userId = response.data;
        this.userFilters = response.data.map(u => {
          return {text: u.name, value: u.id};
        });
      });
    },
    enterSearch(){
      this.$refs.inputVal.blur();
      this.search();
    },
    search() {
      this.changeSelectDataRangeAll();
      this.initTable();
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },

    editApi(row) {
      this.$emit('editApi', row);
    },
    handleCopy(row) {
      row.isCopy = true;
      this.$emit('editApi', row);
    },
    runApi(row) {

      let request = row ? JSON.parse(row.request) : {};
      if (row.tags instanceof Array) {
        row.tags = JSON.stringify(row.tags);
      }
      let response = "";
      if (row.response != null && row.response != 'null' && row.response != undefined) {
        if (Object.prototype.toString.call(row.response).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
          response = row.response;
        } else {
          response = JSON.parse(row.response);
        }
      } else {
        response = {headers: [], body: new Body(), statusCode: [], type: "HTTP"};
      }
      if (response.body) {
        let body = new Body();
        Object.assign(body, response.body);
        if (!body.binary) {
          body.binary = [];
        }
        if (!body.kvs) {
          body.kvs = [];
        }
        if (!body.binary) {
          body.binary = [];
        }
        response.body = body;
      }
      row.request = request;
      row.response = response;
      this.$emit('runTest', row);
    },
    reductionApi(row) {
      let tmp = JSON.parse(JSON.stringify(row));
      let rows = {ids: [tmp.id]};
      rows.projectId = getCurrentProjectID();
      rows.protocol = this.currentProtocol;
      this.$post('/api/definition/reduction/', rows, () => {
        this.$success(this.$t('commons.save_success'));
        // this.search();
        this.$emit('refreshTable');
      });
    },
    handleBatchRestore() {
      let batchParam = buildBatchParam(this, this.$refs.table.selectIds);
      batchParam.protocol = this.currentProtocol;
      this.$post('/api/definition/reduction/', batchParam, () => {
        this.$success(this.$t('commons.save_success'));
        // this.search();
        this.$emit('refreshTable');
      });
    },
    handleDeleteBatch() {
      if (this.trashEnable) {
        this.$alert(this.$t('api_test.definition.request.delete_confirm') + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              this.$post('/api/definition/deleteBatchByParams/', buildBatchParam(this, this.$refs.table.selectIds), () => {
                this.$refs.table.clear();
                // this.initTable();
                this.$emit("refreshTable");
                this.$success(this.$t('commons.delete_success'));
              });
            }
          }
        });
      } else {
        this.$alert(this.$t('api_test.definition.request.delete_confirm') + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              this.$post('/api/definition/removeToGcByParams/', buildBatchParam(this, this.$refs.table.selectIds), () => {
                this.$refs.table.clear();
                // this.initTable();
                this.$emit("refreshTable");
                this.$success(this.$t('commons.delete_success'));
                this.$refs.caseList.apiCaseClose();
              });
            }
          }
        });
      }
    },
    handleEditBatch() {
      if (this.currentProtocol == 'HTTP') {
        this.valueArr.method = REQ_METHOD;
      } else if (this.currentProtocol == 'TCP') {
        this.valueArr.method = TCP_METHOD;
      } else if (this.currentProtocol == 'SQL') {
        this.valueArr.method = SQL_METHOD;
      } else if (this.currentProtocol == 'DUBBO') {
        this.valueArr.method = DUBBO_METHOD;
      }
      this.$refs.batchEdit.open();
    },
    batchEdit(form) {
      let param = buildBatchParam(this, this.$refs.table.selectIds);
      param[form.type] = form.value;
      this.$post('/api/definition/batch/editByParams', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.initTable();
      });
    },
    moveSave(param) {
      let ids = this.$refs.table.selectIds;
      param.ids = ids;
      param.projectId = this.projectId;
      param.condition = this.condition;
      param.moduleId = param.nodeId;
      this.$post('/api/definition/batch/editByParams', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.testCaseBatchMove.close();
        this.initTable();
      });
    },
    handleTestCase(api) {
      this.selectApi = api;
      let request = {};
      if (Object.prototype.toString.call(api.request).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
        request = api.request;
      } else {
        request = JSON.parse(api.request);
      }
      if (!request.hashTree) {
        request.hashTree = [];
      }
      this.selectApi.url = request.path;
      this.$refs.caseList.open(this.selectApi);
    },
    handleDelete(api) {
      if (this.trashEnable) {
        this.$get('/api/definition/delete/' + api.id, () => {
          this.$success(this.$t('commons.delete_success'));
          // this.initTable();
          this.$emit("refreshTable");
        });
        return;
      }
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + api.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let ids = [api.id];
            this.$post('/api/definition/removeToGc/', ids, () => {
              this.$success(this.$t('commons.delete_success'));
              // this.initTable();
              this.$emit("refreshTable");
              this.$refs.caseList.apiCaseClose();
            });
          }
        }
      });
    },

    getColor(enable, method) {
      if (enable) {
        return this.methodColorMap.get(method);
      }
    },
    showExecResult(row) {
      this.$emit('showExecResult', row);
    },
    //判断是否只显示本周的数据。  从首页跳转过来的请求会带有相关参数
    getSelectDataRange() {
      let dataRange = this.$route.params.dataSelectRange;
      let dataType = this.$route.params.dataType;
      if (dataType === 'api') {
        this.selectDataRange = dataRange;
      } else {
        this.selectDataRange = 'all';
      }
    },
    changeSelectDataRangeAll() {
      this.$emit("changeSelectDataRangeAll", "api");
    },
    getIds(rowSets) {
      let rowArray = Array.from(rowSets);
      let ids = rowArray.map(s => s.id);
      return ids;
    },
    exportApi(type, nodeTree) {
      let param = buildBatchParam(this, this.$refs.table.selectIds);
      param.protocol = this.currentProtocol;
      if (param.ids === undefined || param.ids.length < 1) {
        this.$warning(this.$t("api_test.definition.check_select"));
        return;
      }
      this.result = this.$post("/api/definition/export/" + type, param, response => {
        let obj = response.data;
        if (type == 'MS') {
          obj.protocol = this.currentProtocol;
          obj.nodeTree = nodeTree;
          downloadFile("Metersphere_Api_" + this.projectName + ".json", JSON.stringify(obj));
        } else {
          downloadFile("Swagger_Api_" + this.projectName+ ".json", JSON.stringify(obj));
        }
      });
    },
    headerDragend(newWidth, oldWidth, column, event) {
      let finalWidth = newWidth;
      if (column.minWidth > finalWidth) {
        finalWidth = column.minWidth;
      }
      column.width = finalWidth;
      column.realWidth = finalWidth;
    },
    open() {
      this.$refs.searchBar.open();
    },
    needRefreshModule(){
      if(this.initApiTableOpretion === '0'){
        return true;
      }else {
        this.$emit('updateInitApiTableOpretion','0');
        return false;
      }
    },
    saveSortField(key,orders){
      saveLastTableSortField(key,JSON.stringify(orders));
    },
    getSortField(){
      let orderJsonStr = getLastTableSortField(this.tableHeaderKey);
      let returnObj = null;
      if(orderJsonStr){
        try {
          returnObj = JSON.parse(orderJsonStr);
        }catch (e){
          return null;
        }
      }
      return returnObj;
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
  width: 300px;
  margin-right: 10px;
}

.el-tag {
  margin-left: 10px;
}

.ms-select-all >>> th:first-child {
  margin-top: 20px;
}

.ms-select-all >>> th:nth-child(2) .el-icon-arrow-down {
  top: -2px;
}

/* /deep/ .el-table__fixed-body-wrapper {
  top: 60px !important;
} */

</style>
