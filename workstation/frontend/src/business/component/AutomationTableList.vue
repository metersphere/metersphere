<template>
  <div>
    <div class="changeTap" v-if="showTap">
      <span :class="isFinish ? showTextColor: unShowTextColor"
            @click="changeTabState('finish')">{{ $t('commons.to_be_completed') }}</span><span>｜</span><span
      @click="changeTabState('update')"
      :class="isFinish ? unShowTextColor: showTextColor">{{ $t('commons.pending_upgrade') }}</span>
    </div>

    <ms-table
      class="table-card-nopadding"
      :table-is-loading="this.result"
      :data="tableData"
      :screen-height="isRelate ? 'calc(100vh - 400px)' :  screenHeight"
      :condition="condition"
      :page-size="pageSize"
      :enableSelection="false"
      :total="total"
      :fields.sync="fields"
      :field-key=tableHeaderKey
      :remember-order="true"
      row-key="id"
      :row-order-group-id="condition.projectId"
      @refresh="search(projectId)"
      @callBackSelectAll="callBackSelectAll"
      @callBackSelect="callBackSelect"
      ref="scenarioTable">

      <ms-table-column
        prop="deleteTime"
        sortable
        v-if="this.trashEnable"
        :fields-width="fieldsWidth"
        :label="$t('commons.delete_time')"
        min-width="150px">
        <template v-slot:default="scope">
          <span>{{ scope.row.deleteTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="deleteUser"
        :fields-width="fieldsWidth"
        v-if="this.trashEnable"
        :label="$t('commons.delete_user')"
        min-width="120"/>

      <span v-for="(item) in fields" :key="item.key">

        <ms-table-column v-if="item.id === 'num' && !customNum"
                         prop="num"
                         label="ID"
                         sortable
                         :fields-width="fieldsWidth"
                         min-width="120px">
          <template slot-scope="scope">
            <!--<span style="cursor:pointer" v-if="isReadOnly"> {{ scope.row.num }} </span>-->
            <el-tooltip :content="$t('commons.edit')">
              <a style="cursor:pointer" @click="edit(scope.row)"> {{ scope.row.num }} </a>
            </el-tooltip>
          </template>
        </ms-table-column>

        <ms-table-column
          v-if="item.id === 'num' && customNum" prop="customNum"
          label="ID"
          sortable
          :fields-width="fieldsWidth"
          min-width="120px">
          <template slot-scope="scope">
            <!--<span style="cursor:pointer" v-if="isReadOnly"> {{ scope.row.customNum }} </span>-->
            <el-tooltip :content="$t('commons.edit')">
              <a style="cursor:pointer" @click="edit(scope.row)"> {{ scope.row.customNum }} </a>
            </el-tooltip>
          </template>
        </ms-table-column>

        <ms-table-column prop="name"
                         sortable
                         :field="item"
                         :fields-width="fieldsWidth"
                         :label="$t('api_test.automation.scenario_name')"
                         min-width="150px"/>

        <ms-table-column
          prop="level"
          sortable
          :field="item"
          :fields-width="fieldsWidth"
          :filters="apiscenariofilters.LEVEL_FILTERS"
          min-width="130px"
          :label="$t('api_test.automation.case_level')">
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.level"/>
          </template>
        </ms-table-column>

        <ms-table-column
          :label="$t('project.version.name')"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="100px"
          prop="versionId">
          <template v-slot:default="scope">
            <span>{{ scope.row.versionName }}</span>
          </template>
        </ms-table-column>

        <ms-table-column prop="status"
                         :label="$t('test_track.plan.plan_status')"
                         sortable
                         :field="item"
                         :fields-width="fieldsWidth"
                         :filters="apiscenariofilters.STATUS_FILTERS"
                         min-width="120px">
          <template v-slot:default="scope">
            <plan-status-table-item :value="scope.row.status"/>
          </template>
        </ms-table-column>

        <ms-table-column prop="principalName"
                         min-width="150px"
                         :label="$t('api_test.definition.api_principal')"
                         :filters="userFilters"
                         :field="item"
                         :fields-width="fieldsWidth"
                         sortable/>

        <ms-table-column prop="tags"
                         :field="item"
                         v-if="isShowAllColumn"
                         :fields-width="fieldsWidth"
                         min-width="120px"
                         :showOverflowTooltip="false"
                         :label="$t('api_test.automation.tag')">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :content="itemName" :show-tooltip="scope.row.tags.length===1&&itemName.length*12<=120"
                    tooltip style="margin-left: 0px; margin-right: 2px"/>
            <span/>
          </template>
        </ms-table-column>


        <ms-table-column prop="userName" min-width="120px"
                         :label="$t('api_test.automation.creator')"
                         :filters="userFilters"
                         v-if="isShowAllColumn"
                         :field="item"
                         :fields-width="fieldsWidth"
                         sortable="custom"/>

        <ms-table-column prop="createTime"
                         :field="item"
                         v-if="isFinish"
                         :fields-width="fieldsWidth"
                         :label="$t('commons.create_time')"
                         sortable
                         min-width="180px">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column prop="lastResult"
                         :label="$t('api_test.automation.last_result')"
                         :filters="apiscenariofilters.RESULT_FILTERS"
                         v-if="!isFinish"
                         :field="item"
                         :fields-width="fieldsWidth"
                         sortable
                         min-width="130px">
          <template v-slot:default="{row}">
            <el-link type="success" @click="showReport(row)" v-if="row.lastResult === 'Success'">
              {{ $t('api_test.automation.success') }}
            </el-link>
            <el-link type="danger" @click="showReport(row)" v-else-if="row.lastResult === 'Error'">
              {{ $t('api_test.automation.fail') }}
            </el-link>
          </template>
        </ms-table-column>

        <ms-table-column prop="updateTime"
                         v-if="!isFinish"
                         :field="item"
                         :fields-width="fieldsWidth"
                         :label="$t('api_test.automation.update_time')"
                         sortable
                         min-width="180px">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column prop="projectName"
                         :field="item"
                         :fields-width="fieldsWidth"
                         min-width="120px"
                         :label="$t('report.project_name')"
        />

      </span>
    </ms-table>

    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

  </div>
</template>

<script>
import {getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {getUUID} from "metersphere-frontend/src/utils";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {API_SCENARIO_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import {API_SCENARIO_LIST} from "metersphere-frontend/src/utils/constants";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {getCustomTableWidth, getLastTableSortField,} from "metersphere-frontend/src/utils/tableUtils";
import {API_SCENARIO_FILTERS} from "metersphere-frontend/src/utils/table-constants";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import HeaderLabelOperate from "metersphere-frontend/src/components/head/HeaderLabelOperate";
import {TYPE_TO_C} from "@/business/module/api/scenario/Setting";
import {getCustomTableHeaderByXpack} from "@/business/component/js/table-head-util";
import {getProjectVersions} from "metersphere-frontend/src/api/version";
import {useApiStore} from "@/store";
import {getScenarioById, getScenarioList} from "@/api/scenario";
import {getProject} from "@/api/project";
import {getProjectMember} from "@/api/user";


export default {
  name: "AutomationTableList",
  components: {
    MsTable,
    MsTag,
    MsTableColumn,
    HeaderLabelOperate,
    HeaderCustom: () => import("metersphere-frontend/src/components/head/HeaderCustom"),
    BatchMove: () => import("@/business/module/api/BatchMove"),
    EnvironmentSelect: () => import("@/business/module/environment/EnvSelect"),
    BatchEdit: () => import("@/business/module/api/BatchEdit"),
    PlanStatusTableItem: () => import("@/business/module/plan/PlanStatusTableItem"),
    PriorityTableItem: () => import("@/business/module/track/PriorityTableItem"),
    MsTablePagination: () => import("metersphere-frontend/src/components/pagination/TablePagination"),
  },
  props: {
    referenced: {
      type: Boolean,
      default: false,
    },
    isReferenceTable: {
      type: Boolean,
      default: false,
    },
    selectNodeIds: Array,
    selectProjectId: {
      type: String,
      default: ""
    },
    trashEnable: {
      type: Boolean,
      default: false,
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
    },
    //用于判断是否是只读用户
    isReadOnly: {
      type: Boolean,
      default: false,
    },
    initApiTableOpretion: String,
    isRelate: Boolean,
    isFocus: {
      type: Boolean,
      default: false,
    },
    isCreation: {
      type: Boolean,
      default: false,
    },
    isShowAllColumn: {
      type: Boolean,
      default: true,
    },
    isSelectAll: {
      type: Boolean,
      default: false,
    },
    showTap: {
      type: Boolean,
      default: false,
    },
    currentVersion: String,
    screenHeight: {
      type: [Number, String],
      default() {
        return 'calc(100vh - 218px)';
      }
    }, //屏幕高度
  },
  data() {
    return {
      showTextColor: "showTextColor",
      unShowTextColor: "unShowTextColor",
      isFinish: true,
      projectName: "",
      result: false,
      tableHeaderKey: "API_SCENARIO",
      type: API_SCENARIO_LIST,
      fields: getCustomTableHeaderByXpack('API_SCENARIO_HEAD'),
      fieldsWidth: getCustomTableWidth('API_SCENARIO'),
      condition: {
        components: API_SCENARIO_CONFIGS,
      },
      scenarioId: "",
      currentScenario: {},
      schedule: {},
      tableData: [],
      selectDataRange: 'all',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reportId: "",
      showReportId: "",
      projectEnvMap: new Map(),
      batchReportId: "",
      content: {},
      infoDb: false,
      runVisible: false,
      showReportVisible: false,
      planVisible: false,
      runData: [],
      report: {},
      selectDataSize: 0,
      selectAll: false,
      userFilters: [],
      operators: [],
      selectRows: new Set(),
      isStop: false,
      enableOrderDrag: true,
      debugData: {},
      buttons: [],
      typeArr: [
        {id: 'level', name: this.$t('test_track.case.priority')},
        {id: 'status', name: this.$t('test_track.plan.plan_status')},
        {
          id: 'principal',
          name: this.$t('api_test.definition.request.responsible'),
          optionMethod: this.getPrincipalOptions
        },
        {id: 'projectEnv', name: this.$t('api_test.definition.request.run_env')},
      ],
      valueArr: {
        level: [
          {name: 'P0', id: 'P0'},
          {name: 'P1', id: 'P1'},
          {name: 'P2', id: 'P2'},
          {name: 'P3', id: 'P3'}
        ],
        status: [
          {name: this.$t('test_track.plan.plan_status_prepare'), id: 'Prepare'},
          {name: this.$t('test_track.plan.plan_status_running'), id: 'Underway'},
          {name: this.$t('test_track.plan.plan_status_completed'), id: 'Completed'}
        ],
        principal: [],
        environmentId: [],
        projectEnv: [],
        projectId: ''
      },
      apiscenariofilters: {},
      versionFilters: [],
      store:{}
    };
  },
  created() {
    this.store = useApiStore();
    this.apiscenariofilters = API_SCENARIO_FILTERS();
    this.$EventBus.$on('hide', id => {
      this.hideStopBtn(id);
    });
    this.projectId = getCurrentProjectID();
    if (!this.projectName || this.projectName === "") {
      this.getProjectName();
    }
    if (this.isFocus) {
      if (this.condition.filters) {
        delete this.condition.filters['user_id']
      }
      this.condition.combine = {followPeople: {operator: "current user", value: "current user",}}
    } else if (this.isCreation) {
      if (this.condition.filters) {
        delete this.condition.filters['user_id']
      }
      this.condition.combine = {creator: {operator: "current user", value: "current user",}}
    } else {
      if (this.isFinish) {
        if (this.condition.combine) {
          delete this.condition.combine
        }
        if (this.condition.filters) {
          this.condition.filters.status = ["Prepare", "Underway"];
          this.condition.filters.principal = [getCurrentUserId()];
        } else {
          this.condition.filters = {status: ["Prepare", "Underway"], principal: [getCurrentUserId()]}
        }
      } else {

        this.condition.combine = {lastResult: {operator: "in", value: ["error"]}}
        if (this.condition.filters) {
          this.condition.filters.principal = [getCurrentUserId()];
        } else {
          this.condition.filters = {principal: [getCurrentUserId()]};
        }
      }
    }

    if (this.trashEnable) {
      if (this.condition.filters) {
        this.condition.filters.status = ["Trash"];
      } else {
        this.condition.filters = {status: ["Trash"]};
      }
      this.condition.moduleIds = [];
    }

    if (this.trashEnable) {
      this.condition.orders = [{"name": "delete_time", "type": "desc"}];
    } else {
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);
    }
    this.condition.versionId = this.currentVersion;
    this.search();
    this.getPrincipalOptions([]);

    // 通知过来的数据跳转到编辑
    if (this.$route.query.resourceId) {
      getScenarioById(this.$route.query.resourceId).then((response) => {
        this.edit(response.data);
      });
    }
    this.getVersionOptions();
  },
  beforeDestroy() {
    this.$EventBus.$off("hide");
  },
  watch: {
    selectNodeIds() {
      this.currentPage = 1;
      this.$refs.scenarioTable.clear();
      this.selectProjectId ? this.search(this.selectProjectId) : this.search();
    },
    trashEnable() {
      if (this.trashEnable) {
        if (this.condition.filters) {
          this.condition.filters.status = ["Trash"];
        } else {
          this.condition.filters = {status: ["Trash"]};
        }
        this.condition.moduleIds = [];

      } else {
        if (this.condition.filters) {
          this.condition.filters.status = ["Prepare", "Underway", "Completed"];
        } else {
          this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
        }
      }
      this.$refs.scenarioTable.clear();
      this.search();
    },
    isFinish() {
      if (!this.isFocus) {
        if (this.isFinish) {
          delete this.condition.combine

          if (this.condition.filters) {
            this.condition.filters.status = ["Prepare", "Underway"];
            this.condition.filters.principal = [getCurrentUserId()];
          } else {
            this.condition.filters = {status: ["Prepare", "Underway"], principal: [getCurrentUserId()]}
          }

        } else {
          this.condition.combine = {lastResult: {operator: "in", value: ["Error"]}}
          if (this.condition.filters) {
            this.condition.filters.principal = [getCurrentUserId()];
          } else {
            this.condition.filters = {principal: [getCurrentUserId()]};
          }
        }
      }
      this.search();
    },
    currentVersion() {
      this.condition.versionId = this.currentVersion;
      // 选择了版本过滤，版本列上的checkbox也进行过滤
      this.search();
      this.getVersionOptions(this.currentVersion);
    },
  },
  computed: {
    isNotRunning() {
      return "Running" !== this.report.status;
    },
    customNum() {
      return this.store.currentProjectIsCustomNum;
    },
  },
  methods: {
    getProjectName() {
      getProject(this.projectId).then(response => {
        let project = response.data;
        if (project) {
          this.projectName = project.name;
        }
      });
    },
    search(projectId) {
      if (this.needRefreshModule()) {
        this.$emit('refreshTree');
      }
      if (this.selectProjectId) {
        projectId = this.selectProjectId;
      }
      this.selectRows = new Set();
      this.condition.moduleIds = this.selectNodeIds;
      if (this.trashEnable) {
        if (this.condition.filters) {
          this.condition.filters.status = ["Trash"];
        } else {
          this.condition.filters = {status: ["Trash"]};
        }
        this.condition.moduleIds = [];
      }

      // todo
      if (this.isSelectAll === false) {
        if (projectId != null && typeof projectId === 'string') {
          this.condition.projectId = projectId;
        } else if (this.projectId != null) {
          this.condition.projectId = this.projectId;
        }
      }

      this.enableOrderDrag = this.condition.orders.length <= 0;

      //检查是否只查询本周数据
      this.condition.selectThisWeedData = false;
      this.condition.executeStatus = null;
      this.isSelectThissWeekData();
      switch (this.selectDataRange) {
        case 'thisWeekCount':
          this.condition.selectThisWeedData = true;
          break;
        case 'unExecute':
          this.condition.executeStatus = 'unExecute';
          break;
        case 'executeFailed':
          this.condition.executeStatus = 'executeFailed';
          break;
        case 'executePass':
          this.condition.executeStatus = 'executePass';
          break;
      }
      this.condition.workspaceId = getCurrentWorkspaceId();
      this.result =  getScenarioList(this.currentPage,this.pageSize,this.condition).then(response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        this.tableData.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
        this.$emit('getTrashCase');
      });
    },
    getPrincipalOptions(option) {
      getProjectMember(response => {
        option.push(...response.data);
        if (this.isCreation) {
          response.data.map(u => {
            if (u.id === getCurrentUserId()) {
              let a = {text: u.name, value: u.id};
              this.userFilters.push(a);
            }
          });
        } else {
          this.userFilters = response.data.map(u => {
            return {text: u.name, value: u.id};
          });
        }
      });
    },

    edit(row) {
      let uuid = getUUID();
      let apiResolve = this.$router.resolve({
        path: '/api/automation/default/' + uuid + '/scenario/edit:' + row.id + '/' + row.projectId + '/' + getCurrentWorkspaceId(),
      });
      window.open(apiResolve.href, '_blank');
    },

    sort(stepArray) {
      for (let i in stepArray) {
        stepArray[i].index = Number(i) + 1;
        if (!stepArray[i].resourceId) {
          stepArray[i].resourceId = getUUID();
        }
        if (!stepArray[i].clazzName) {
          stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
        }
        if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
          stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
        }
        if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
          this.sort(stepArray[i].hashTree);
        }
      }
    },


    showReport(row) {
      this.showReportVisible = true;
      this.infoDb = true;
      this.showReportId = row.reportId;
    },
    //判断是否只显示本周的数据。  从首页跳转过来的请求会带有相关参数
    isSelectThissWeekData() {
      let dataRange = this.$route.params.dataSelectRange;
      this.selectDataRange = dataRange;
    },
    changeSelectDataRangeAll() {
      this.$emit("changeSelectDataRangeAll");
    },
    openScenario(item) {
      this.$emit('openScenario', item);
    },

    needRefreshModule() {
      if (this.initApiTableOpretion === '0') {
        return true;
      } else {
        this.$emit('updateInitApiTableOpretion', '0');
        return false;
      }
    },
    callBackSelectAll(selection) {
      this.$emit('selection', selection);
    },
    callBackSelect(selection) {
      this.$emit('selection', selection);
    },


    hideStopBtn(scenarioId) {
      for (let data of this.tableData) {
        if (scenarioId && scenarioId === data.id) {
          this.$set(data, "isStop", false);
        }
      }
    },
    changeTabState(name) {
      if (name === 'update') {
        this.isFinish = false;
      } else {
        this.isFinish = true;
      }
    },
    getVersionOptions(currentVersion) {
      if (hasLicense()) {
        getProjectVersions(getCurrentProjectID()).then(response => {
          if (currentVersion) {
            this.versionFilters = response.data.filter(u => u.id === currentVersion).map(u => {
              return {text: u.name, value: u.id};
            });
          } else {
            this.versionFilters = response.data.map(u => {
              return {text: u.name, value: u.id};
            });
          }
        });
      }
    },
  },

};
</script>

<style type="text/css" scoped>
.showTextColor {
  color: #783987;
  cursor: pointer;
}

.unShowTextColor {
  cursor: pointer;
}

.changeTap {
  margin: auto;
  width: 50%;
  text-align: center;
}
</style>
<style scoped>

:deep(.el-drawer__header) {
  margin-bottom: 0px;
}

:deep(.el-table__fixed-body-wrapper)  {
  z-index: auto !important;
}

:deep(.el-table__fixed-right)  {
  height: 100% !important;
}

:deep(.el-card__header) {
  padding: 10px;
}


</style>
