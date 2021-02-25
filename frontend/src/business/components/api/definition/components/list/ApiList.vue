<template>
  <div>
    <api-list-container-with-doc
      :is-api-list-enable="isApiListEnable"
      :active-dom="activeDom"
      @activeDomChange="activeDomChange"
      @isApiListEnableChange="isApiListEnableChange">

      <el-link type="primary" @click="open" style="float: right;margin-top: 5px">{{ $t('commons.adv_search.title') }}
      </el-link>
      <el-input :placeholder="$t('commons.search_by_id_name_tag')" @blur="search" class="search-input" size="small"
                @keyup.enter.native="search"
                v-model="condition.name"/>

      <el-table v-loading="result.loading"
                ref="apiDefinitionTable"
                border
                @sort-change="sort"
                @filter-change="filter"
                :data="tableData" row-key="id" class="test-content adjust-table ms-select-all-fixed"
                @select-all="handleSelectAll"
                @header-dragend="headerDragend"
                @select="handleSelect" :height="screenHeight">
        <el-table-column width="50" type="selection"/>

        <ms-table-header-select-popover v-show="total>0"
                                        :page-size="pageSize>total?total:pageSize"
                                        :total="total"
                                        @selectPageAll="isSelectDataAll(false)"
                                        @selectAll="isSelectDataAll(true)"/>

        <el-table-column width="30" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectDataCounts"/>
          </template>
        </el-table-column>
        <template v-for="(item, index) in tableLabel">
          <el-table-column
            v-if="item.id == 'num'"
            prop="num"
            label="ID"
            show-overflow-tooltip
            min-width="80px"
            sortable="custom"
            :key="index">
            <template slot-scope="scope">
              <el-tooltip content="编辑">
                <a style="cursor:pointer" @click="editApi(scope.row)"> {{ scope.row.num }} </a>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column
            v-if="item.id == 'name'"
            prop="name"
            :label="$t('api_test.definition.api_name')"
            show-overflow-tooltip
            sortable="custom"
            min-width="120px"
            :key="index"/>
          <el-table-column
            v-if="item.id == 'status'"
            prop="status"
            column-key="status"
            sortable="custom"
            :filters="statusFilters"
            :label="$t('api_test.definition.api_status')"
            min-width="120px"
            :key="index">
            <template v-slot:default="scope">
            <span class="el-dropdown-link">
              <api-status :value="scope.row.status"/>
            </span>
            </template>
          </el-table-column>

          <el-table-column
            v-if="item.id == 'method'"
            prop="method"
            sortable="custom"
            column-key="method"
            :filters="methodFilters"
            :label="$t('api_test.definition.api_type')"
            show-overflow-tooltip min-width="120px"
            :key="index">
            <template v-slot:default="scope" class="request-method">
              <el-tag size="mini"
                      :style="{'background-color': getColor(true, scope.row.method), border: getColor(true, scope.row.method)}"
                      class="api-el-tag">
                {{ scope.row.method }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column
            v-if="item.id == 'userName'"
            prop="userName"
            sortable="custom"
            :filters="userFilters"
            column-key="user_id"
            :label="$t('api_test.definition.api_principal')"
            show-overflow-tooltip
            min-width="100px"
            :key="index"/>

          <el-table-column
            v-if="item.id == 'path'"
            prop="path"
            min-width="120px"
            :label="$t('api_test.definition.api_path')"
            show-overflow-tooltip
            :key="index"/>

          <el-table-column
            v-if="item.id == 'tags'"
            prop="tags"
            :label="$t('commons.tag')"
            min-width="120px"
            :key="index">
            <template v-slot:default="scope">
              <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain" :content="itemName" style="margin-left: 5px"/>
            </template>
          </el-table-column>

          <el-table-column
            v-if="item.id == 'updateTime'"
            width="160"
            :label="$t('api_test.definition.api_last_time')"
            sortable="custom"
            min-width="160px"
            prop="updateTime"
            :key="index">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>

          <el-table-column
            v-if="item.id == 'caseTotal'"
            prop="caseTotal"
            min-width="80px"
            :label="$t('api_test.definition.api_case_number')"
            show-overflow-tooltip
            :key="index"/>

          <el-table-column
            v-if="item.id == 'caseStatus'"
            prop="caseStatus"
            min-width="80px"
            :label="$t('api_test.definition.api_case_status')"
            show-overflow-tooltip
            :key="index"/>

          <el-table-column
            v-if="item.id == 'casePassingRate'"
            prop="casePassingRate"
            :width="100"
            min-width="100px"
            :label="$t('api_test.definition.api_case_passing_rate')"
            show-overflow-tooltip
            :key="index"/>
        </template>

        <el-table-column fixed="right" v-if="!isReadOnly" min-width="180"
                         align="center">

          <template slot="header">
            <header-label-operate @exec="customHeader"/>
          </template>

          <template v-slot:default="scope">
            <ms-table-operator-button class="run-button" :is-tester-permission="true"
                                      :tip="$t('api_test.automation.execute')"
                                      icon="el-icon-video-play"
                                      @exec="runApi(scope.row)"/>
            <ms-table-operator-button :tip="$t('commons.reduction')" icon="el-icon-refresh-left"
                                      @exec="reductionApi(scope.row)" v-if="trashEnable" v-tester/>
            <ms-table-operator-button :tip="$t('commons.edit')" icon="el-icon-edit" @exec="editApi(scope.row)" v-else
                                      v-tester/>
            <el-tooltip :content="$t('test_track.case.case_list')"
                        placement="bottom"
                        :enterable="false"
                        effect="dark">
              <el-button @click="handleTestCase(scope.row)"
                         @keydown.enter.native.prevent
                         type="primary"
                         circle
                         style="color:white;padding: 0px 0.1px;font-size: 11px;width: 28px;height: 28px;"
                         size="mini">case
              </el-button>
            </el-tooltip>
            <ms-table-operator-button :tip="$t('commons.delete')" icon="el-icon-delete" @exec="handleDelete(scope.row)"
                                      type="danger" v-tester/>
          </template>
        </el-table-column>
        <header-custom ref="headerCustom" :initTableData="initTable" :optionalFields=headerItems
                       :type=type></header-custom>
      </el-table>
      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </api-list-container-with-doc>
    <ms-api-case-list @refresh="initTable" @showExecResult="showExecResult" :currentApi="selectApi" ref="caseList"/>
    <!--批量编辑-->
    <ms-batch-edit ref="batchEdit" @batchEdit="batchEdit" :typeArr="typeArr" :value-arr="valueArr"/>
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
import MsTag from "../../../../common/components/MsTag";
import MsApiCaseList from "../case/ApiCaseList";
import MsContainer from "../../../../common/components/MsContainer";
import MsBottomContainer from "../BottomContainer";
import ShowMoreBtn from "../../../../track/case/components/ShowMoreBtn";
import MsBatchEdit from "../basis/BatchEdit";
import {API_METHOD_COLOUR, API_STATUS, DUBBO_METHOD, REQ_METHOD, SQL_METHOD, TCP_METHOD} from "../../model/JsonData";
import {downloadFile} from "@/common/js/utils";
import {PROJECT_NAME} from '@/common/js/constants';
import {getCurrentProjectID, getCurrentUser} from "@/common/js/utils";
import {API_LIST, TEST_CASE_LIST, WORKSPACE_ID} from '@/common/js/constants';
import ApiListContainer from "./ApiListContainer";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import ApiStatus from "@/business/components/api/definition/components/list/ApiStatus";
import MsTableAdvSearchBar from "@/business/components/common/components/search/MsTableAdvSearchBar";
import {API_DEFINITION_CONFIGS} from "@/business/components/common/components/search/search-components";
import MsTipButton from "@/business/components/common/components/MsTipButton";
import CaseBatchMove from "@/business/components/api/definition/components/basis/BatchMove";
import ApiListContainerWithDoc from "@/business/components/api/definition/components/list/ApiListContainerWithDoc";
import {
  _handleSelect,
  _handleSelectAll, buildBatchParam, getLabel,
  getSelectDataCounts, initCondition,
  setUnSelectIds, toggleAllSelection
} from "@/common/js/tableUtils";
import {_filter, _sort} from "@/common/js/tableUtils";
import {Api_List, Track_Test_Case} from "@/business/components/common/model/JsonData";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";


export default {
  name: "ApiList",
  components: {
    HeaderLabelOperate,
    HeaderCustom,
    CaseBatchMove,
    ApiStatus,
    MsTableHeaderSelectPopover,
    ApiListContainerWithDoc,
    MsTableButton,
    MsTableOperatorButton,
    MsTableOperator,
    MsTableHeader,
    MsTablePagination,
    MsTag,
    MsApiCaseList,
    MsContainer,
    MsBottomContainer,
    ShowMoreBtn,
    MsBatchEdit,
    MsTipButton,
    MsTableAdvSearchBar
  },
  data() {
    return {
      type: API_LIST,
      headerItems: Api_List,
      tableLabel: Api_List,
      condition: {
        components: API_DEFINITION_CONFIGS
      },
      selectApi: {},
      result: {},
      moduleId: "",
      selectDataRange: "all",
      deletePath: "/test/case/delete",
      selectRows: new Set(),
      buttons: [
        {name: this.$t('api_test.definition.request.batch_delete'), handleClick: this.handleDeleteBatch},
        {name: this.$t('api_test.definition.request.batch_edit'), handleClick: this.handleEditBatch},
        {
          name: this.$t('api_test.definition.request.batch_move'), handleClick: this.handleBatchMove
        }
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
      screenHeight: document.documentElement.clientHeight - 270,//屏幕高度,
      environmentId: undefined,
      selectDataCounts: 0,
    }
  },
  props: {
    currentProtocol: String,
    selectNodeIds: Array,
    isSelectThisWeek: String,
    activeDom:String,
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
    isApiListEnable: Boolean,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    moduleTree: {
      type: Array,
      default() {
        return []
      },
    },
    moduleOptions: {
      type: Array,
      default() {
        return []
      },
    }
  },
  created: function () {
    if (this.trashEnable) {
      this.condition.filters = {status: ["Trash"]};
    } else {
      this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
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
    trashEnable() {
      if (this.trashEnable) {
        this.condition.filters = {status: ["Trash"]};
        this.condition.moduleIds = [];
      } else {
        this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
      }
      this.initTable();
    }
  },
  methods: {
    customHeader() {
      this.$refs.headerCustom.open(this.tableLabel)
    },
    handleBatchMove() {
      this.$refs.testCaseBatchMove.open(this.moduleTree, [], this.moduleOptions);
    },
    isApiListEnableChange(data) {
      this.$emit('isApiListEnableChange', data);
    },
    activeDomChange(tabType){
      this.$emit("activeDomChange",tabType);
    },
    initTable() {
      getLabel(this, API_LIST);
      this.selectRows = new Set();
      initCondition(this.condition);
      this.selectDataCounts = 0;
      this.condition.moduleIds = this.selectNodeIds;
      this.condition.projectId = getCurrentProjectID();
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
          })
        });
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
      let workspaceId = localStorage.getItem(WORKSPACE_ID);
      this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
        this.valueArr.userId = response.data;
        this.userFilters = response.data.map(u => {
          return {text: u.name, value: u.id}
        });
      });
    },
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.tableData, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    handleSelect(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    isSelectDataAll(data) {
      this.condition.selectAll = data;
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      toggleAllSelection(this.$refs.apiDefinitionTable, this.tableData, this.selectRows);
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
    runApi(row) {
      let request = JSON.parse(row.request);
      row.request = request
      this.$emit('runTest', row);
    },
    reductionApi(row) {
      let tmp = JSON.parse(JSON.stringify(row));
      tmp.request = null;
      tmp.response = null;
      if (tmp.tags instanceof Array) {
        tmp.tags = JSON.stringify(tmp.tags);
      }
      let rows = [tmp];
      this.$post('/api/definition/reduction/', rows, () => {
        this.$success(this.$t('commons.save_success'));
        this.search();
      });
    },
    handleDeleteBatch() {
      if (this.trashEnable) {
        this.$alert(this.$t('api_test.definition.request.delete_confirm') + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              this.$post('/api/definition/deleteBatchByParams/', buildBatchParam(this), () => {
                this.selectRows.clear();
                this.initTable();
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
              this.$post('/api/definition/removeToGcByParams/', buildBatchParam(this), () => {
                this.selectRows.clear();
                this.initTable();
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
      let param = buildBatchParam(this);
      param[form.type] = form.value;
      this.$post('/api/definition/batch/editByParams', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.initTable();
      });
    },
    moveSave(param) {
      let arr = Array.from(this.selectRows);
      let ids = arr.map(row => row.id);
      param.ids = ids;
      param.projectId = getCurrentProjectID();
      param.moduleId=param.nodeId;
      param.condition = this.condition;
      param.selectAllDate = this.isSelectAllDate;
      param.unSelectIds = this.unSelection;
      param = Object.assign(param, this.condition);
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
          this.initTable();
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
              this.initTable();
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
      let rowArray = Array.from(rowSets)
      let ids = rowArray.map(s => s.id);
      return ids;
    },
    exportApi() {
      let param = buildBatchParam(this);
      param.protocol = this.currentProtocol;
      if (param.ids === undefined || param.ids.length < 1) {
        this.$warning(this.$t("api_test.definition.check_select"));
        return;
      }
      this.result = this.$post("/api/definition/export", param, response => {
        let obj = response.data;
        obj.protocol = this.currentProtocol;
        this.buildApiPath(obj.data);
        downloadFile("Metersphere_Api_" + localStorage.getItem(PROJECT_NAME) + ".json", JSON.stringify(obj));
      });
    },
    buildApiPath(apis) {
      apis.forEach((api) => {
        this.moduleOptions.forEach(item => {
          if (api.moduleId === item.id) {
            api.modulePath = item.path;
          }
        });
      });
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.initTable();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTable();
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

</style>
