<template>
  <div>
    <api-list-container
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange">

      <ms-environment-select :project-id="relevanceProjectId" v-if="isRelevance" :is-read-only="isReadOnly" @setEnvironment="setEnvironment"/>

      <el-input placeholder="搜索" @blur="search" class="search-input" size="small" @keyup.enter.native="search" v-model="condition.name"/>

      <el-table v-loading="result.loading"
                border
                :data="tableData" row-key="id" class="test-content adjust-table"
                @select-all="handleSelectAll"
                @select="handleSelect" :height="screenHeight">
        <el-table-column type="selection"/>
        <el-table-column width="40" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore && !isReadOnly" :buttons="buttons" :size="selectRows.size"/>
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

        <el-table-column v-if="!isReadOnly && !isRelevance" :label="$t('commons.operating')" min-width="130" align="center">
          <template v-slot:default="scope">
            <el-button type="text" @click="reductionApi(scope.row)" v-if="trashEnable">恢复</el-button>
            <el-button type="text" @click="editApi(scope.row)" v-else>{{$t('commons.edit')}}</el-button>
            <el-button type="text" @click="handleTestCase(scope.row)">{{$t('api_test.definition.request.case')}}</el-button>
            <el-button type="text" @click="handleDelete(scope.row)" style="color: #F56C6C">{{$t('commons.delete')}}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </api-list-container>
    <ms-api-case-list @refresh="initTable" @showExecResult="showExecResult" :currentApi="selectApi" ref="caseList"/>
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
  import {API_METHOD_COLOUR, REQ_METHOD, API_STATUS} from "../../model/JsonData";
  import {getCurrentProjectID} from "@/common/js/utils";
  import {WORKSPACE_ID} from '../../../../../../common/js/constants';
  import ApiListContainer from "./ApiListContainer";
  import MsEnvironmentSelect from "../case/MsEnvironmentSelect";

  export default {
    name: "ApiList",
    components: {
      MsEnvironmentSelect,
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
        selectApi: {},
        result: {},
        moduleId: "",
        deletePath: "/test/case/delete",
        selectRows: new Set(),
        buttons: [
          {name: this.$t('api_test.definition.request.batch_delete'), handleClick: this.handleDeleteBatch},
          {name: this.$t('api_test.definition.request.batch_edit'), handleClick: this.handleEditBatch}
        ],
        typeArr: [
          {id: 'status', name: this.$t('api_test.definition.api_status')},
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
        screenHeight: document.documentElement.clientHeight - 330,//屏幕高度,
        environmentId: undefined
      }
    },
    props: {
      currentProtocol: String,
      selectNodeIds: Array,
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
      relevanceProjectId: String,
      isRelevance: Boolean
    },
    created: function () {
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
          this.initTable();
        }
      },
      relevanceProjectId() {
        this.initTable();
      }
    },
    methods: {
      isApiListEnableChange(data) {
        this.$emit('isApiListEnableChange', data);
      },
      initTable() {
        this.selectRows = new Set();
        this.condition.filters = ["Prepare", "Underway", "Completed"];

        this.condition.moduleIds = this.selectNodeIds;
        if (this.trashEnable) {
          this.condition.filters = ["Trash"];
          this.condition.moduleIds = [];
        }

        this.condition.projectId = this.getProjectId();

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
        this.initTable();
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },

      editApi(row) {
        this.$emit('editApi', row);
      },
      reductionApi(row) {
        row.request = null;
        row.response = null;
        let rows = [row];
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
                let ids = Array.from(this.selectRows).map(row => row.id);
                this.$post('/api/definition/deleteBatch/', ids, () => {
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
                let ids = Array.from(this.selectRows).map(row => row.id);
                this.$post('/api/definition/removeToGc/', ids, () => {
                  this.selectRows.clear();
                  this.initTable();
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
      getProjectId() {
        if (!this.isCaseRelevance) {
          return getCurrentProjectID();
        } else {
          return this.relevanceProjectId;
        }
      },
      setEnvironment(data) {
        this.environmentId = data.id;
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
