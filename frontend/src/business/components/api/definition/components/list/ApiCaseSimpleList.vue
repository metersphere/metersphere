<template>
  <div>
    <api-list-container
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange">

      <el-input placeholder="搜索" @blur="search" @keyup.enter.native="search" class="search-input" size="small"
                v-model="condition.name"/>

      <el-table v-loading="result.loading"
                ref="caseTable"
                border
                :data="tableData" row-key="id" class="test-content adjust-table"
                @select-all="handleSelectAll"
                @filter-change="filter"
                @sort-change="sort"
                @select="handleSelect" :height="screenHeight">
        <el-table-column type="selection"/>
        <el-table-column width="40" :resizable="false" align="center">
          <el-dropdown slot="header" style="width: 14px">
            <span class="el-dropdown-link" style="width: 14px">
              <i class="el-icon-arrow-down el-icon--right" style="margin-left: 0px"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native.stop="isSelectDataAll(true)">
                {{ $t('api_test.batch_menus.select_all_data', [total]) }}
              </el-dropdown-item>
              <el-dropdown-item @click.native.stop="isSelectDataAll(false)">
                {{ $t('api_test.batch_menus.select_show_data', [tableData.length]) }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore && !isReadOnly" :buttons="buttons" :size="selectDataCounts"/>
          </template>
        </el-table-column>

        <el-table-column prop="num" label="ID" show-overflow-tooltip/>
        <el-table-column prop="name" :label="$t('test_track.case.name')" show-overflow-tooltip/>

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
          sortable="custom"
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

        <el-table-column v-if="!isReadOnly" :label="$t('commons.operating')" min-width="130" align="center">
          <template v-slot:default="scope">
            <!--<el-button type="text" @click="reductionApi(scope.row)" v-if="trashEnable">{{$t('commons.reduction')}}</el-button>-->
            <el-button type="text" @click="handleTestCase(scope.row)" v-if="!trashEnable">{{ $t('commons.edit') }}
            </el-button>
            <el-button type="text" @click="handleDelete(scope.row)" style="color: #F56C6C">{{ $t('commons.delete') }}
            </el-button>
            <ms-api-case-table-extend-btns @showCaseRef="showCaseRef" @showEnvironment="showEnvironment" @createPerformance="createPerformance" :row="scope.row" v-tester/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </api-list-container>

    <api-case-list @showExecResult="showExecResult" @refresh="initTable" :currentApi="selectCase" ref="caseList"/>
    <!--批量编辑-->
    <ms-batch-edit ref="batchEdit" @batchEdit="batchEdit" :typeArr="typeArr" :value-arr="valueArr"/>
    <!--选择环境(当创建性能测试的时候)-->
    <ms-set-environment ref="setEnvironment" :testCase="clickRow" @createPerformance="createPerformance"/>
    <!--查看引用-->
    <ms-reference-view ref="viewRef"/>
  </div>

</template>

<script>

import MsTableOperator from "../../../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";
import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";
import MsTablePagination from "../../../../common/pagination/TablePagination";
import MsTag from "../../../../common/components/MsTag";
import MsApiCaseList from "../case/ApiCaseList";
import MsContainer from "../../../../common/components/MsContainer";
import MsBottomContainer from "../BottomContainer";
import ShowMoreBtn from "../../../../track/case/components/ShowMoreBtn";
import MsBatchEdit from "../basis/BatchEdit";
import {API_METHOD_COLOUR, CASE_PRIORITY} from "../../model/JsonData";
import {getBodyUploadFiles, getCurrentProjectID} from "@/common/js/utils";
import ApiListContainer from "./ApiListContainer";
import PriorityTableItem from "../../../../track/common/tableItems/planview/PriorityTableItem";
import ApiCaseList from "../case/ApiCaseList";
import {_filter, _sort} from "../../../../../../common/js/utils";
import {_handleSelect, _handleSelectAll} from "../../../../../../common/js/tableUtils";
import MsApiCaseTableExtendBtns from "../reference/ApiCaseTableExtendBtns";
import MsReferenceView from "../reference/ReferenceView";
import MsSetEnvironment from "@/business/components/api/definition/components/basis/SetEnvironment";
import TestPlan from "@/business/components/api/definition/components/jmeter/components/test-plan";
import ThreadGroup from "@/business/components/api/definition/components/jmeter/components/thread-group";
import {parseEnvironment} from "@/business/components/api/test/model/EnvironmentModel";


export default {
  name: "ApiCaseSimpleList",
  components: {
    MsSetEnvironment,
    ApiCaseList,
    PriorityTableItem,
    ApiListContainer,
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
  },
  data() {
    return {
      condition: {},
      selectCase: {},
      result: {},
      moduleId: "",
      deletePath: "/test/case/delete",
      selectRows: new Set(),
      clickRow: {},
      buttons: [
        {name: this.$t('api_test.definition.request.batch_delete'), handleClick: this.handleDeleteBatch},
        {name: this.$t('api_test.definition.request.batch_edit'), handleClick: this.handleEditBatch}
      ],
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
      screenHeight: document.documentElement.clientHeight - 330,//屏幕高度
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
    visible: {
      type: Boolean,
      default: false,
    },
    trashEnable: {
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
    this.initTable();
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
      return this.model === 'api'
    },
  },
  methods: {
    isApiListEnableChange(data) {
      this.$emit('isApiListEnableChange', data);
    },
    initTable() {
      this.selectRows = new Set();
      this.condition.status = "";
      this.condition.moduleIds = this.selectNodeIds;
      if (this.trashEnable) {
        this.condition.status = "Trash";
        this.condition.moduleIds = [];
      }
      this.selectAll = false;
      this.unSelection = [];
      this.selectDataCounts = 0;
      this.condition.projectId = getCurrentProjectID();

      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }
      if (this.condition.projectId) {
        this.result = this.$post('/api/testcase/list/' + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
          this.unSelection = response.data.listObject.map(s => s.id);
        });
      }
    },
    // getMaintainerOptions() {
    //   let workspaceId = localStorage.getItem(WORKSPACE_ID);
    //   this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
    //     this.valueArr.userId = response.data;
    //   });
    // },
    handleSelect(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      this.selectRowsCount(this.selectRows)
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
      this.selectRowsCount(this.selectRows)
    },
    search() {
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
    reductionApi(row) {
      let ids = [row.id];
      this.$post('/api/testcase/reduction/', ids, () => {
        this.$success(this.$t('commons.save_success'));
        this.search();
      });
    },
    handleDeleteBatch() {
      // if (this.trashEnable) {
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let obj = {};
            obj.projectId = getCurrentProjectID();
            obj.selectAllDate = this.isSelectAllDate;
            obj.unSelectIds = this.unSelection;
            obj.ids = Array.from(this.selectRows).map(row => row.id);
            obj = Object.assign(obj, this.condition);
            this.$post('/api/testcase/deleteBatchByParam/', obj, () => {
              this.selectRows.clear();
              this.initTable();
              this.$success(this.$t('commons.delete_success'));
            });
          }
        }
      });
      // } else {
      //   this.$alert(this.$t('api_test.definition.request.delete_confirm') + "？", '', {
      //     confirmButtonText: this.$t('commons.confirm'),
      //     callback: (action) => {
      //       if (action === 'confirm') {
      //         let ids = Array.from(this.selectRows).map(row => row.id);
      //         this.$post('/api/testcase/removeToGc/', ids, () => {
      //           this.selectRows.clear();
      //           this.initTable();
      //           this.$success(this.$t('commons.delete_success'));
      //         });
      //       }
      //     }
      //   });
      // }
    },
    handleEditBatch() {
      this.$refs.batchEdit.open();
    },
    batchEdit(form) {
      let arr = Array.from(this.selectRows);
      let ids = arr.map(row => row.id);
      let param = {};
      param[form.type] = form.value;
      param.ids = ids;

      param.projectId = getCurrentProjectID();
      param.selectAllDate = this.isSelectAllDate;
      param.unSelectIds = this.unSelection;
      param = Object.assign(param, this.condition);
      this.$post('/api/testcase/batch/editByParam', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.initTable();
      });
    },
    handleDelete(apiCase) {
      // if (this.trashEnable) {
      this.$get('/api/testcase/delete/' + apiCase.id, () => {
        this.$success(this.$t('commons.delete_success'));
        this.initTable();
      });
      return;
      // }
      // this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + apiCase.name + " ？", '', {
      //   confirmButtonText: this.$t('commons.confirm'),
      //   callback: (action) => {
      //     if (action === 'confirm') {
      //       let ids = [apiCase.id];
      //       this.$post('/api/testcase/removeToGc/', ids, () => {
      //         this.$success(this.$t('commons.delete_success'));
      //         this.initTable();
      //       });
      //     }
      //   }
      // });
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
      if (this.isSelectAllDate) {
        this.selectDataCounts = this.total - this.unSelection.length;
      } else {
        this.selectDataCounts = selection.size;
      }
    },
    isSelectDataAll(dataType) {
      this.isSelectAllDate = dataType;
      this.selectRowsCount(this.selectRows)
      //如果已经全选，不需要再操作了
      if (this.selectRows.size != this.tableData.length) {
        this.$refs.caseTable.toggleAllSelection(true);
      }
    },
    getIds(rowSets) {
      let rowArray = Array.from(rowSets)
      let ids = rowArray.map(s => s.id);
      return ids;
    },
    showCaseRef(row) {
      this.$refs.viewRef.open(row);
    },
    showEnvironment(row) {

      let projectID = getCurrentProjectID();
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
    createPerformance(row,environment){
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
      row.request = JSON.parse( row.request );
      row.request.name = row.id;
      row.request.useEnvironment = environment.id;
      runData.push(row.request);
      /*触发执行操作*/
      let testPlan = new TestPlan();
      let threadGroup = new ThreadGroup();
      threadGroup.hashTree = [];
      testPlan.hashTree = [threadGroup];
      runData.forEach(item => {
        threadGroup.hashTree.push(item);
      })
      let reqObj = {id: row.id,
        testElement: testPlan,
        name:row.name,
        projectId:getCurrentProjectID(),
      };
      let bodyFiles = getBodyUploadFiles(reqObj, runData);
      reqObj.reportId = "run";

      let url = "/api/genPerformanceTestXml";

      this.$fileUpload(url, null, bodyFiles, reqObj, response => {
        let jmxObj = {};
        jmxObj.name = response.data.name;
        jmxObj.xml = response.data.xml;
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
  margin-right: 20px;
}

</style>
