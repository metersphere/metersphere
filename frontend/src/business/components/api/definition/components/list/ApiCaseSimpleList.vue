<template>
  <div>
    <api-list-container
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange">
      <el-input placeholder="搜索" @blur="search" class="search-input" size="small" v-model="condition.name"/>
      <el-table v-loading="result.loading"
                border
                :data="tableData" row-key="id" class="test-content adjust-table"
                @select-all="handleSelectAll"
                @filter-change="filter"
                @sort-change="sort"
                @select="handleSelect" :height="screenHeight">
        <el-table-column type="selection"/>
        <el-table-column width="40" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectRows.size"/>
          </template>
        </el-table-column>

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


        <el-table-column :label="$t('commons.operating')" min-width="130" align="center">
          <template v-slot:default="scope">
            <!--<el-button type="text" @click="reductionApi(scope.row)" v-if="trashEnable">恢复</el-button>-->
            <el-button type="text" @click="handleTestCase(scope.row)" v-if="!trashEnable">{{$t('commons.edit')}}</el-button>
            <el-button type="text" @click="handleDelete(scope.row)" style="color: #F56C6C">{{$t('commons.delete')}}</el-button>
          </template>
        </el-table-column>

      </el-table>
      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </api-list-container>

    <api-case-list @refresh="initTable" :currentApi="selectCase" ref="caseList"/>
    <!--批量编辑-->
    <ms-batch-edit ref="batchEdit" @batchEdit="batchEdit" :typeArr="typeArr" :value-arr="valueArr"/>
  </div>

</template>

<script>

  import MsTableHeader from '../../../../common/components/MsTableHeader';
  import MsTableOperator from "../../../../common/components/MsTableOperator";
  import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";
  import MsTableButton from "../../../../common/components/MsTableButton";
  import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";
  import MsTablePagination from "../../../../common/pagination/TablePagination";
  import MsTag from "../../../../common/components/MsTag";
  import MsApiCaseList from "../case/ApiCaseList";
  import MsContainer from "../../../../common/components/MsContainer";
  import MsBottomContainer from "../BottomContainer";
  import ShowMoreBtn from "../../../../track/case/components/ShowMoreBtn";
  import MsBatchEdit from "../basis/BatchEdit";
  import {API_METHOD_COLOUR, REQ_METHOD, API_STATUS, CASE_PRIORITY} from "../../model/JsonData";
  import {getCurrentProjectID} from "@/common/js/utils";
  import ApiListContainer from "./ApiListContainer";
  import PriorityTableItem from "../../../../track/common/tableItems/planview/PriorityTableItem";
  import ApiCaseList from "../case/ApiCaseList";
  import {_filter, _sort} from "../../../../../../common/js/utils";

  export default {
    name: "ApiCaseSimpleList",
    components: {
      ApiCaseList,
      PriorityTableItem,
      ApiListContainer,
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
      MsBatchEdit
    },
    data() {
      return {
        condition: {},
        selectCase: {},
        result: {},
        moduleId: "",
        deletePath: "/test/case/delete",
        selectRows: new Set(),
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
        projectId: "",
        screenHeight: document.documentElement.clientHeight - 330,//屏幕高度
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
      isApiListEnable: Boolean
    },
    created: function () {
      this.projectId = getCurrentProjectID();
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
    },
    methods: {
      isApiListEnableChange(data) {
        this.$emit('isApiListEnableChange', data);
      },
      initTable() {
        this.selectRows = new Set();
        // this.condition.filters = ["Prepare", "Underway", "Completed"];
        this.condition.status = "";
        this.condition.moduleIds = this.selectNodeIds;
        if (this.trashEnable) {
          this.condition.status = "Trash";
          this.condition.moduleIds = [];
        }
        if (this.projectId != null) {
          this.condition.projectId = this.projectId;
        }
        if (this.currentProtocol != null) {
          this.condition.protocol = this.currentProtocol;
        }
        this.result = this.$post("/api/testcase/list/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
        });
      },
      // getMaintainerOptions() {
      //   let workspaceId = localStorage.getItem(WORKSPACE_ID);
      //   this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
      //     this.valueArr.userId = response.data;
      //   });
      // },
      handleSelect(selection, row) {
        row.hashTree = [];
        if (this.selectRows.has(row)) {
          this.$set(row, "showMore", false);
          this.selectRows.delete(row);
        } else {
          this.$set(row, "showMore", true);
          this.selectRows.add(row);
        }
        let arr = Array.from(this.selectRows);
        // 选中1个以上的用例时显示更多操作
        if (this.selectRows.size === 1) {
          this.$set(arr[0], "showMore", false);
        } else if (this.selectRows.size === 2) {
          arr.forEach(row => {
            this.$set(row, "showMore", true);
          })
        }
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
        if (selection.length > 0) {
          if (selection.length === 1) {
            selection.hashTree = [];
            this.selectRows.add(selection[0]);
          } else {
            this.tableData.forEach(item => {
              item.hashTree = [];
              this.$set(item, "showMore", true);
              this.selectRows.add(item);
            });
          }
        } else {
          this.selectRows.clear();
          this.tableData.forEach(row => {
            this.$set(row, "showMore", false);
          })
        }
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
                let ids = Array.from(this.selectRows).map(row => row.id);
                this.$post('/api/testcase/deleteBatch/', ids, () => {
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
        this.$post('/api/testcase/batch/edit', param, () => {
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
