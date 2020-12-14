<template>
  <div>
    <el-card class="card-content">
      <el-input placeholder="搜索" @blur="search" class="search-input" size="small" v-model="condition.name"/>

      <el-table border :data="tableData" row-key="id" class="test-content adjust-table"
                @select-all="handleSelectAll"
                @select="handleSelect" :height="screenHeight">
        <el-table-column type="selection"/>
        <el-table-column width="40" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectRows.size"/>
          </template>
        </el-table-column>

        <el-table-column prop="name" :label="$t('api_test.definition.api_name')" show-overflow-tooltip/>
        <el-table-column
          prop="status"
          column-key="api_status"
          :label="$t('api_test.definition.api_status')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <ms-tag v-if="scope.row.status == 'Prepare'" type="info" effect="plain" :content="$t('test_track.plan.plan_status_prepare')"/>
            <ms-tag v-if="scope.row.status == 'Underway'" type="warning" effect="plain" :content="$t('test_track.plan.plan_status_running')"/>
            <ms-tag v-if="scope.row.status == 'Completed'" type="success" effect="plain" :content="$t('test_track.plan.plan_status_completed')"/>
            <ms-tag v-if="scope.row.status == 'Trash'" type="danger" effect="plain" content="废弃"/>
          </template>
        </el-table-column>

        <el-table-column
          prop="method"
          :label="$t('api_test.definition.api_type')"
          show-overflow-tooltip>
          <template v-slot:default="scope" class="request-method">
            <el-tag size="mini" :style="{'background-color': getColor(true, scope.row.method), border: getColor(true, scope.row.method)}" class="api-el-tag">
              {{ scope.row.method}}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="path"
          :label="$t('api_test.definition.api_path')"
          show-overflow-tooltip/>

        <el-table-column
          prop="userName"
          :label="$t('api_test.definition.api_principal')"
          show-overflow-tooltip/>

        <el-table-column width="160" :label="$t('api_test.definition.api_last_time')" prop="updateTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <el-table-column
          prop="caseTotal"
          :label="$t('api_test.definition.api_case_number')"
          show-overflow-tooltip/>

        <el-table-column
          prop="caseStatus"
          :label="$t('api_test.definition.api_case_status')"
          show-overflow-tooltip/>

        <el-table-column
          prop="casePassingRate"
          :label="$t('api_test.definition.api_case_passing_rate')"
          show-overflow-tooltip/>

        <el-table-column :label="$t('commons.operating')" min-width="130" align="center">
          <template v-slot:default="scope">
            <el-button type="text" @click="reductionApi(scope.row)" v-if="currentModule!=undefined && currentModule.id === 'gc'">恢复</el-button>
            <el-button type="text" @click="editApi(scope.row)" v-else>{{$t('commons.edit')}}</el-button>
            <el-button type="text" @click="handleTestCase(scope.row)">{{$t('api_test.definition.request.case')}}</el-button>
            <el-button type="text" @click="handleDelete(scope.row)" style="color: #F56C6C">{{$t('commons.delete')}}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initApiTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>
    <ms-api-case-list @refresh="initApiTable" @showExecResult="showExecResult" :currentApi="selectApi" ref="caseList"/>
    <!--批量编辑-->
    <ms-batch-edit ref="batchEdit" @batchEdit="batchEdit" :typeArr="typeArr" :value-arr="valueArr"/>
  </div>

</template>

<script>

  import MsTableHeader from '../../../../components/common/components/MsTableHeader';
  import MsTableOperator from "../../../common/components/MsTableOperator";
  import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
  import MsTableButton from "../../../common/components/MsTableButton";
  import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";
  import MsTablePagination from "../../../common/pagination/TablePagination";
  import MsTag from "../../../common/components/MsTag";
  import MsApiCaseList from "./ApiCaseList";
  import MsContainer from "../../../common/components/MsContainer";
  import MsBottomContainer from "./BottomContainer";
  import ShowMoreBtn from "../../../../components/track/case/components/ShowMoreBtn";
  import MsBatchEdit from "./basis/BatchEdit";
  import {API_METHOD_COLOUR, REQ_METHOD, API_STATUS} from "../model/JsonData";
  import {getCurrentProjectID} from "@/common/js/utils";
  import {WORKSPACE_ID} from '../../../../../common/js/constants';

  export default {
    name: "ApiList",
    components: {
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
        selectApi: {},
        moduleId: "",
        deletePath: "/test/case/delete",
        selectRows: new Set(),
        buttons: [
          {name: this.$t('api_test.definition.request.batch_delete'), handleClick: this.handleDeleteBatch},
          {name: this.$t('api_test.definition.request.batch_edit'), handleClick: this.handleEditBatch}
        ],
        typeArr: [
          {id: 'status', name: this.$t('api_test.definition.api_case_status')},
          {id: 'method', name: this.$t('api_test.definition.api_type')},
          {id: 'userId', name: this.$t('api_test.definition.api_principal')},
        ],
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
        projectId: "",
        screenHeight: document.documentElement.clientHeight - 330,//屏幕高度
      }
    },
    props: {
      currentProtocol: String,
      currentModule: Object,
      visible: {
        type: Boolean,
        default: false,
      }
    },
    created: function () {
      this.projectId = getCurrentProjectID();
      this.initApiTable();
      this.getMaintainerOptions();
    },
    watch: {
      currentModule() {
        this.initApiTable();
      },
      currentProtocol() {
        this.initApiTable();
      },
    },
    methods: {
      initApiTable() {
        this.selectRows = new Set();
        this.condition.filters = ["Prepare", "Underway", "Completed"];
        if (this.currentModule != null) {
          if (this.currentModule.id == "root") {
            this.condition.moduleIds = [];
          } else if (this.currentModule.id == "gc") {
            this.condition.moduleIds = [];
            this.condition.filters = ["Trash"];
          }
          else {
            this.condition.moduleIds = this.currentModule.ids;
          }
        }
        if (this.projectId != null) {
          this.condition.projectId = this.projectId;
        }
        if (this.currentProtocol != null) {
          this.condition.protocol = this.currentProtocol;
        }
        this.result = this.$post("/api/definition/list/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
        });
      },
      getMaintainerOptions() {
        let workspaceId = localStorage.getItem(WORKSPACE_ID);
        this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
          this.valueArr.userId = response.data;
        });
      },
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
        this.initApiTable();
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },

      editApi(row) {
        this.$emit('editApi', row);
      },
      reductionApi(row) {
        let ids = [row.id];
        this.$post('/api/definition/reduction/', ids, () => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      },
      handleDeleteBatch() {
        if (this.currentModule != undefined && this.currentModule.id == "gc") {
          this.$alert(this.$t('api_test.definition.request.delete_confirm') + "？", '', {
            confirmButtonText: this.$t('commons.confirm'),
            callback: (action) => {
              if (action === 'confirm') {
                let ids = Array.from(this.selectRows).map(row => row.id);
                this.$post('/api/definition/deleteBatch/', ids, () => {
                  this.selectRows.clear();
                  this.initApiTable();
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
                let ids = Array.from(this.selectRows).map(row => row.id);
                this.$post('/api/definition/removeToGc/', ids, () => {
                  this.selectRows.clear();
                  this.initApiTable();
                  this.$success(this.$t('commons.delete_success'));
                });
              }
            }
          });
        }
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
        this.$post('/api/definition/batch/edit', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.initApiTable();
        });
      },
      handleTestCase(api) {
        this.selectApi = api;
        let request = JSON.parse(api.request);
        if (!request.hashTree) {
          request.hashTree = [];
        }
        this.selectApi.url = request.path;
        this.$refs.caseList.open(this.selectApi);
      },
      handleDelete(api) {
        if (this.currentModule != undefined && this.currentModule.id == "gc") {
          this.$get('/api/definition/delete/' + api.id, () => {
            this.$success(this.$t('commons.delete_success'));
            this.initApiTable();
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
                this.initApiTable();
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
