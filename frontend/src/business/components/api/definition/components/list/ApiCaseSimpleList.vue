<template>
  <div>
    <div>
      <el-link type="primary" style="float:right;margin-top: 5px" @click="open">{{ $t('commons.adv_search.title') }}
      </el-link>

      <el-input :placeholder="$t('commons.search_by_id_name_tag')" @blur="search" @keyup.enter.native="search"
                class="search-input" size="small"
                v-model="condition.name"/>

      <ms-table :data="tableData" :select-node-ids="selectNodeIds" :condition="condition" :page-size="pageSize"
                :total="total"
                :operators="operators"
                :batch-operators="buttons"
                :screenHeight="screenHeight"
                :fields.sync="fields"
                :field-key="tableHeaderKey"
                @saveSortField="saveSortField"
                operator-width="170px"
                @refresh="initTable"
                ref="caseTable"
      >
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
            :fields-width="fieldsWidth"
            min-width="80px"
            sortable>
            <template slot-scope="scope">
              <!-- 判断为只读用户的话不可点击ID进行编辑操作 -->
              <span style="cursor:pointer" v-if="isReadOnly"> {{ scope.row.num }} </span>
              <el-tooltip v-else content="编辑">
                <a style="cursor:pointer" @click="handleTestCase(scope.row)"> {{ scope.row.num }} </a>
              </el-tooltip>
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="name"
            min-width="160px"
            :label="$t('test_track.case.name')"/>

          <ms-table-column
            prop="priority"
            :filters="priorityFilters"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="120px"
            :label="$t('test_track.case.priority')">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.priority"/>
            </template>
          </ms-table-column>

          <ms-table-column
            sortable="custom"
            prop="path"
            min-width="180px"
            :field="item"
            :fields-width="fieldsWidth"
            :label="'API'+ $t('api_test.definition.api_path')"/>

          <ms-table-column
            sortable
            prop="casePath"
            min-width="180px"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('api_test.definition.request.case')+ $t('api_test.definition.api_path')"/>

          <ms-table-column v-if="item.id=='tags'" prop="tags" width="120px" :label="$t('commons.tag')">
            <template v-slot:default="scope">
              <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                      :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
            </template>
          </ms-table-column>

          <ms-table-column
            prop="createUser"
            :field="item"
            :fields-width="fieldsWidth"
            :label="'创建人'"/>

          <ms-table-column
            sortable="updateTime"
            min-width="160px"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('api_test.definition.api_last_time')"
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
          </ms-table-column>
        </span>

        <template v-if="!trashEnable" v-slot:opt-behind="scope">
          <ms-api-case-table-extend-btns @showCaseRef="showCaseRef"
                                         @showEnvironment="showEnvironment"
                                         @createPerformance="createPerformance" :row="scope.row"/>
        </template>

      </ms-table>

      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </div>

    <api-case-list @showExecResult="showExecResult" @refresh="initTable" :currentApi="selectCase" ref="caseList"/>
    <!--批量编辑-->
    <ms-batch-edit ref="batchEdit" :data-count="$refs.caseTable ? $refs.caseTable.selectDataCounts : 0" @batchEdit="batchEdit" :typeArr="typeArr" :value-arr="valueArr"/>
    <!--选择环境(当创建性能测试的时候)-->
    <ms-set-environment ref="setEnvironment" :testCase="clickRow" @createPerformance="createPerformance"/>
    <!--查看引用-->
    <ms-reference-view ref="viewRef"/>
    <!--高级搜索-->
    <ms-table-adv-search-bar :condition.sync="condition" :showLink="false" ref="searchBar" @search="initTable"/>

  </div>

</template>

<script>

import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTableOperator from "../../../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";
import MsTablePagination from "../../../../common/pagination/TablePagination";
import MsTag from "../../../../common/components/MsTag";
import MsApiCaseList from "../case/ApiCaseList";
import ApiCaseList from "../case/ApiCaseList";
import MsContainer from "../../../../common/components/MsContainer";
import MsBottomContainer from "../BottomContainer";
import ShowMoreBtn from "../../../../track/case/components/ShowMoreBtn";
import MsBatchEdit from "../basis/BatchEdit";
import {API_METHOD_COLOUR, CASE_PRIORITY, DUBBO_METHOD, REQ_METHOD, SQL_METHOD, TCP_METHOD} from "../../model/JsonData";

import {getBodyUploadFiles, getCurrentProjectID} from "@/common/js/utils";
import PriorityTableItem from "../../../../track/common/tableItems/planview/PriorityTableItem";
import MsApiCaseTableExtendBtns from "../reference/ApiCaseTableExtendBtns";
import MsReferenceView from "../reference/ReferenceView";
import MsSetEnvironment from "@/business/components/api/definition/components/basis/SetEnvironment";
import TestPlan from "@/business/components/api/definition/components/jmeter/components/test-plan";
import ThreadGroup from "@/business/components/api/definition/components/jmeter/components/thread-group";
import {parseEnvironment} from "@/business/components/api/test/model/EnvironmentModel";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import MsTableAdvSearchBar from "@/business/components/common/components/search/MsTableAdvSearchBar";
import {API_CASE_CONFIGS} from "@/business/components/common/components/search/search-components";
import {
  _filter,
  _sort,
  getCustomTableHeader,
  getCustomTableWidth, saveLastTableSortField, getLastTableSortField
} from "@/common/js/tableUtils";
import {API_CASE_LIST} from "@/common/js/constants";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";

export default {
  name: "ApiCaseSimpleList",
  components: {
    HeaderLabelOperate,
    MsTableHeaderSelectPopover,
    MsSetEnvironment,
    ApiCaseList,
    PriorityTableItem,
    MsTableOperatorButton,
    MsTableOperator,
    MsTablePagination,
    MsTag,
    MsApiCaseList,
    MsContainer,
    MsBottomContainer,
    ShowMoreBtn,
    MsBatchEdit,
    MsApiCaseTableExtendBtns,
    MsReferenceView,
    MsTableAdvSearchBar,
    MsTable,
    MsTableColumn
  },
  data() {
    return {
      type: API_CASE_LIST,
      tableHeaderKey: "API_CASE",
      fields: getCustomTableHeader('API_CASE'),
      fieldsWidth: getCustomTableWidth('API_CASE'),
      condition: {
        components: API_CASE_CONFIGS
      },
      selectCase: {},
      result: {},
      moduleId: "",
      selectDataRange: "all",
      clickRow: {},
      buttons: [],
      simpleButtons: [
        {name: this.$t('api_test.definition.request.batch_delete'), handleClick: this.handleDeleteToGcBatch},
        {name: this.$t('api_test.definition.request.batch_edit'), handleClick: this.handleEditBatch}
      ],
      trashButtons: [
        {name: this.$t('commons.reduction'), handleClick: this.handleBatchRestore},
        {name: this.$t('api_test.definition.request.batch_delete'), handleClick: this.handleDeleteBatch},
      ],
      operators: [],
      simpleOperators: [
        {
          tip: this.$t('api_test.automation.execute'),
          icon: "el-icon-video-play",
          exec: this.runTestCase,
          class: "run-button",
          permissions: ['PROJECT_API_DEFINITION:READ+RUN']
        },
        {
          tip: this.$t('commons.edit'),
          icon: "el-icon-edit",
          exec: this.handleTestCase,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_CASE']
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.deleteToGc,
          icon: "el-icon-delete",
          type: "danger",
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_CASE']
        },
      ],
      trashOperators: [
        {tip: this.$t('commons.reduction'), icon: "el-icon-refresh-left", exec: this.reduction},
        {
          tip: this.$t('commons.delete'),
          exec: this.handleDelete,
          icon: "el-icon-delete",
          type: "danger",
          permissions: ['PROJECT_API_DEFINITION:READ+DELETE_CASE']
        },
      ],
      typeArr: [
        {id: 'priority', name: this.$t('test_track.case.priority')},
        {id: 'method', name: this.$t('api_test.definition.api_type')},
        {id: 'path', name: this.$t('api_test.request.path')},
      ],
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      valueArr: {
        priority: CASE_PRIORITY,
        method: REQ_METHOD,
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: 'calc(100vh - 250px)',//屏幕高度
      environmentId: undefined,
      selectAll: false,
      unSelection: [],
      selectDataCounts: 0,
      environments: [],
    }
  },
  props: {
    currentProtocol: String,
    selectNodeIds: Array,
    activeDom: String,
    visible: {
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
    isCaseRelevance: {
      type: Boolean,
      default: false,
    },
    relevanceProjectId: String,
    model: {
      type: String,
      default() {
        'api'
      }
    },
    planId: String
  },
  created: function () {
    let orderArr = this.getSortField();
    if (orderArr) {
      this.condition.orders = orderArr;
    }

    if (this.trashEnable) {
      this.operators = this.trashOperators;
      this.buttons = this.trashButtons;
    } else {
      this.operators = this.simpleOperators;
      this.buttons = this.simpleButtons;
    }

    this.initTable();
  },
  watch: {
    selectNodeIds() {
      this.selectAll = false;
      this.unSelection = [];
      this.selectDataCounts = 0;
      this.initTable();
    },
    currentProtocol() {
      this.selectAll = false;
      this.unSelection = [];
      this.selectDataCounts = 0;
      this.initTable();
    },
    trashEnable() {
      if (this.trashEnable) {
        this.operators = this.trashOperators;
        this.buttons = this.trashButtons;
      } else {
        this.operators = this.simpleOperators;
        this.buttons = this.simpleButtons;
      }
      if (this.trashEnable) {
        this.selectAll = false;
        this.unSelection = [];
        this.selectDataCounts = 0;
        this.initTable();
      }
    },
    relevanceProjectId() {
      this.initTable();
    }
  },
  computed: {
    // 接口定义用例列表
    isApiModel() {
      return this.model === 'api';
    },
    projectId() {
      return getCurrentProjectID();
    },
    selectRows() {
      return this.$refs.caseTable.getSelectRows();
    }
  },
  methods: {
    customHeader() {
      this.$refs.caseTable.openCustomHeader();
    },
    initTable() {
      if (this.$refs.caseTable) {
        this.$refs.caseTable.clearSelectRows();
      }
      if (this.condition.orders) {
        const index = this.condition.orders.findIndex(d => d.name !== undefined && d.name === 'case_path');
        if (index != -1) {
          this.condition.orders.splice(index, 1);
        }
      }
      this.condition.status = "";
      this.condition.moduleIds = this.selectNodeIds;
      if (this.trashEnable) {
        // this.condition.status = "Trash";
        this.condition.moduleIds = [];
        if (this.condition.filters) {
          if (this.condition.filters.status) {
            this.condition.filters.status = ["Trash"];
          } else {
            this.condition.filters = {status: ["Trash"]};
          }
        } else {
          this.condition.filters = {};
          this.condition.filters = {status: ["Trash"]};
        }
      } else {
        if (this.condition.filters) {
          if (this.condition.filters.status) {
            this.condition.filters.status = [];
          }
        }
      }
      if (!this.selectAll) {
        this.selectAll = false;
        this.unSelection = [];
        this.selectDataCounts = 0;
      }
      this.condition.projectId = this.projectId;

      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }

      //检查是否只查询本周数据
      this.isSelectThissWeekData();
      this.condition.selectThisWeedData = false;
      this.condition.id = null;
      if (this.selectDataRange == 'thisWeekCount') {
        this.condition.selectThisWeedData = true;
      } else if (this.selectDataRange != null) {
        let selectParamArr = this.selectDataRange.split("single:");

        if (selectParamArr.length === 2) {
          this.condition.id = selectParamArr[1];
        }
      }
      if (this.condition.projectId) {
        this.result = this.$post('/api/testcase/list/' + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;

          if (!this.selectAll) {
            this.unSelection = response.data.listObject.map(s => s.id);
          }

          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          })

          this.$nextTick(function () {
            if (this.$refs.caseTable) {
              this.$refs.caseTable.doLayout();
              this.$refs.caseTable.checkTableRowIsSelect();
            }
          })
        });
      }
    },

    open() {
      this.$refs.searchBar.open();
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
    search() {
      this.changeSelectDataRangeAll();
      this.initTable();
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    runTestCase(testCase) {
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
        this.$refs.caseList.runTestCase(selectApi, testCase.id);
      });
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
    handleDeleteBatch() {
      this.$alert(this.$t('api_test.definition.request.delete_case_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let obj = {};
            obj.projectId = this.projectId;
            obj.selectAllDate = this.selectAll;
            obj.unSelectIds = this.unSelection;
            obj.ids = Array.from(this.selectRows).map(row => row.id);
            obj = Object.assign(obj, this.condition);
            this.$post('/api/testcase/deleteBatchByParam/', obj, () => {
              this.$refs.caseTable.clearSelectRows();
              // this.initTable();
              this.$emit('refreshTable');
              this.$success(this.$t('commons.delete_success'));
            });
          }
        }
      });
    },
    handleDeleteToGcBatch() {
      let obj = {};
      obj.projectId = this.projectId;
      obj.selectAllDate = this.selectAll;
      obj.unSelectIds = this.unSelection;
      obj.ids = Array.from(this.selectRows).map(row => row.id);
      obj = Object.assign(obj, this.condition);
      this.$post('/api/testcase/checkDeleteDatas/', obj, response => {
        let checkResult = response.data;
        let alertMsg = this.$t('api_test.definition.request.delete_confirm') + " ？";
        if (!checkResult.deleteFlag) {
          alertMsg = "";
          checkResult.checkMsg.forEach(item => {
            alertMsg += item + ";";
          });
          if (alertMsg === "") {
            alertMsg = this.$t('api_test.definition.request.delete_confirm') + " ？";
          } else {
            alertMsg += this.$t('api_test.is_continue') + " ？";
          }
        }

        this.$alert(alertMsg, '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {

              this.$post('/api/testcase/deleteToGcByParam/', obj, () => {
                this.$refs.caseTable.clearSelectRows();
                this.initTable();
                this.$success(this.$t('commons.delete_success'));
              });
            }
          }
        });
      });


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
      let arr = Array.from(this.selectRows);
      let ids = arr.map(row => row.id);
      let param = {};
      param[form.type] = form.value;
      param.ids = ids;
      param.projectId = this.projectId;
      param.selectAllDate = this.selectAll;
      param.unSelectIds = this.unSelection;
      param = Object.assign(param, this.condition);
      this.$post('/api/testcase/batch/editByParam', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.initTable();
      });
    },
    handleDelete(apiCase) {
      this.$alert(this.$t('api_test.definition.request.delete_case_confirm') + ' ' + apiCase.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$get('/api/testcase/delete/' + apiCase.id, () => {
              this.$success(this.$t('commons.delete_success'));
              // this.initTable();
              this.$emit('refreshTable');
            });
          }
        }
      });
      return;
    },
    deleteToGc(apiCase) {

      let obj = {};
      obj.projectId = this.projectId;
      obj.selectAllDate = false;
      obj.ids = [apiCase.id]
      obj = Object.assign(obj, this.condition);
      this.$post('/api/testcase/checkDeleteDatas/', obj, response => {
        let checkResult = response.data;
        let alertMsg = this.$t('api_test.definition.request.delete_case_confirm') + ' ' + apiCase.name + " ？";
        if (!checkResult.deleteFlag) {
          alertMsg = "";
          checkResult.checkMsg.forEach(item => {
            alertMsg += item + ";";
          });
          if (alertMsg === "") {
            alertMsg = this.$t('api_test.definition.request.delete_case_confirm') + ' ' + apiCase.name + " ？";
          } else {
            alertMsg += this.$t('api_test.is_continue') + " ？";
          }
        }

        this.$alert(alertMsg, '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              this.$get('/api/testcase/deleteToGc/' + apiCase.id, () => {
                this.$success(this.$t('commons.delete_success'));
                this.initTable();

              });
            }
          }
        });
      });
    },
    reduction(row) {
      let tmp = JSON.parse(JSON.stringify(row));
      let rows = {ids: [tmp.id]};
      this.$post('/api/testcase/reduction/', rows, response => {
        let cannotReductionApiNameArr = response.data;
        if (cannotReductionApiNameArr.length > 0) {
          let apiNames = "";
          cannotReductionApiNameArr.forEach(item => {
            if (apiNames === "") {
              apiNames += item
            } else {
              apiNames += ";" + item;
            }
          });
          this.$error("请先恢复[" + apiNames + "]接口");
        } else {
          this.$success(this.$t('commons.save_success'));
        }
        // this.search();
        this.$emit('refreshTable');
      });
    },
    handleBatchRestore() {
      let obj = {};
      obj.projectId = this.projectId;
      obj.selectAllDate = this.selectAll;
      obj.unSelectIds = this.unSelection;
      obj.ids = Array.from(this.selectRows).map(row => row.id);
      obj = Object.assign(obj, this.condition);
      this.$post('/api/testcase/reduction/', obj, response => {
        let cannotReductionApiNameArr = response.data;
        if (cannotReductionApiNameArr.length > 0) {
          let apiNames = "";
          cannotReductionApiNameArr.forEach(item => {
            if (apiNames === "") {
              apiNames += item
            } else {
              apiNames += ";" + item;
            }
          });
          this.$error("请先恢复[" + apiNames + "]接口");
        } else {
          this.$success(this.$t('commons.save_success'));
        }
        // this.search();
        this.$emit('refreshTable');
      });
    },
    setEnvironment(data) {
      this.environmentId = data.id;
    },
    selectRowsCount(selection) {
      let selectedIDs = this.getIds(selection);
      let allIDs = this.tableData.map(s => s.id);
      this.unSelection = allIDs.filter(function (val) {
        return selectedIDs.indexOf(val) === -1
      });
      if (this.selectAll) {
        this.selectDataCounts = this.total - this.unSelection.length;
      } else {
        this.selectDataCounts = selection.size;
      }
    },
    //判断是否只显示本周的数据。  从首页跳转过来的请求会带有相关参数
    isSelectThissWeekData() {
      this.selectDataRange = "all";
      let routeParam = this.$route.params.dataSelectRange;
      let dataType = this.$route.params.dataType;
      if (dataType === 'apiTestCase') {
        this.selectDataRange = routeParam;
      }
    },
    changeSelectDataRangeAll() {
      this.$emit("changeSelectDataRangeAll", "testCase");
    },
    getIds(rowSets) {
      let rowArray = Array.from(rowSets)
      let ids = rowArray.map(s => s.id);
      return ids;
    },
    showCaseRef(row) {
      let param = {};
      Object.assign(param, row);
      param.moduleId = undefined;
      this.$refs.viewRef.open(param);
    },
    showEnvironment(row) {

      let projectID = this.projectId;
      if (this.projectId) {
        this.$get('/api/environment/list/' + this.projectId, response => {
          this.environments = response.data;
          this.environments.forEach(environment => {
            parseEnvironment(environment);
          });
        });
      } else {
        this.environment = undefined;
      }
      this.clickRow = row;
      this.$refs.setEnvironment.open(row);
    },
    headerDragend(newWidth, oldWidth, column, event) {
      let finalWidth = newWidth;
      if (column.minWidth > finalWidth) {
        finalWidth = column.minWidth;
      }
      column.width = finalWidth;
      column.realWidth = finalWidth;
    },
    saveSortField(key, orders) {
      saveLastTableSortField(key, JSON.stringify(orders));
    },
    getSortField() {
      let orderJsonStr = getLastTableSortField(this.tableHeaderKey);
      let returnObj = null;
      if (orderJsonStr) {
        try {
          returnObj = JSON.parse(orderJsonStr);
        } catch (e) {
          return null;
        }
      }
      return returnObj;
    },
    createPerformance(row, environment) {
      /**
       * 思路：调用后台创建性能测试的方法，把当前案例的hashTree在后台转化为jmx并文件创建性能测试。
       * 然后跳转到修改性能测试的页面
       *
       * 性能测试保存地址： performance/save
       *
       */
      if (!environment) {
        this.$warning(this.$t('api_test.environment.select_environment'));
        return;
      }
      let runData = [];
      let singleLoading = true;
      row.request = JSON.parse(row.request);
      row.request.name = row.id;
      row.request.useEnvironment = environment.id;
      let map = new Map;
      map.set(row.projectId, environment.id);
      row.environmentMap = map;
      runData.push(row.request);
      /*触发执行操作*/
      let testPlan = new TestPlan();
      let threadGroup = new ThreadGroup();
      threadGroup.hashTree = [];
      testPlan.hashTree = [threadGroup];
      runData.forEach(item => {
        threadGroup.hashTree.push(item);
      })
      let reqObj = {
        id: row.id,
        testElement: testPlan,
        name: row.name,
        projectId: this.projectId,
      };
      let bodyFiles = getBodyUploadFiles(reqObj, runData);
      reqObj.reportId = "run";

      let url = "/api/genPerformanceTestXml";

      this.$fileUpload(url, null, bodyFiles, reqObj, response => {
        let jmxObj = {};
        jmxObj.name = response.data.name;
        jmxObj.xml = response.data.xml;
        jmxObj.attachFiles = response.data.attachFiles;
        jmxObj.attachByteFiles = response.data.attachByteFiles;
        jmxObj.caseId = reqObj.id;
        this.$store.commit('setTest', {
          name: row.name,
          jmx: jmxObj
        })
        this.$router.push({
          path: "/performance/test/create"
        })
        // let performanceId = response.data;
        // if(performanceId!=null){
        //   this.$router.push({
        //     path: "/performance/test/edit/"+performanceId,
        //   })
        // }
      }, erro => {
        this.$emit('runRefresh', {});
      });
    },
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
  margin-right: 10px;
}

.ms-select-all >>> th:first-child {
  margin-top: 20px;
}

.ms-select-all >>> th:nth-child(2) .el-icon-arrow-down {
  top: -2px;
}

</style>
