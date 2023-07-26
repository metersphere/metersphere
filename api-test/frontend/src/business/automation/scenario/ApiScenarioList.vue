<template>
  <el-card class="scenario-div" v-loading="result">
    <slot name="version"></slot>
    <ms-search :condition.sync="condition" :base-search-tip="$t('commons.search_by_id_name_tag')" @search="search">
    </ms-search>
    <ms-table
      :data="tableData"
      :screen-height="isRelate ? 'calc(100vh - 400px)' : screenHeight"
      :condition="condition"
      :page-size="pageSize"
      :operators="isRelate ? [] : operators"
      :batch-operators="buttons"
      :total="total"
      :fields.sync="fields"
      :field-key="tableHeaderKey"
      :remember-order="true"
      operator-width="200"
      :enable-order-drag="enableOrderDrag"
      row-key="id"
      :row-order-group-id="condition.projectId"
      :row-order-func="editApiScenarioCaseOrder"
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
        min-width="120" />

      <span v-for="item in fields" :key="item.key">
        <ms-table-column
          :fields-width="fieldsWidth"
          prop="num"
          label="ID"
          sortable
          min-width="120px"
          v-if="item.id == 'num' && !customNum">
          <template slot-scope="scope" v-if="!trashEnable">
            <el-tooltip :content="$t('commons.edit')">
              <a style="cursor: pointer" @click="edit(scope.row)">
                {{ scope.row.num }}
              </a>
            </el-tooltip>
          </template>
        </ms-table-column>

        <ms-table-column
          label="ID"
          sortable
          :fields-width="fieldsWidth"
          min-width="120px"
          prop="customNum"
          v-if="item.id == 'num' && customNum">
          <template slot-scope="scope">
            <el-tooltip :content="$t('commons.edit')">
              <a style="cursor: pointer" @click="edit(scope.row)">
                {{ scope.row.customNum }}
              </a>
            </el-tooltip>
          </template>
        </ms-table-column>

        <ms-table-column
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('api_test.automation.scenario_name')"
          min-width="150px"
          prop="name"
          sortable />

        <ms-table-column
          :field="item"
          :fields-width="fieldsWidth"
          :filters="scenarioFilters.LEVEL_FILTERS"
          :label="$t('api_test.automation.case_level')"
          min-width="130px"
          prop="level"
          sortable>
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.level" />
          </template>
        </ms-table-column>

        <ms-table-column
          :label="$t('test_track.plan.plan_status')"
          :sortable="trashEnable ? false : true"
          :field="item"
          :fields-width="fieldsWidth"
          :filters="!trashEnable ? scenarioFilters.STATUS_FILTERS : null"
          prop="status"
          min-width="120px">
          <template v-slot:default="scope">
            <plan-status-table-item :value="scope.row.status" />
          </template>
        </ms-table-column>

        <ms-table-column
          :field="item"
          :fields-width="fieldsWidth"
          :showOverflowTooltip="false"
          :label="$t('api_test.automation.tag')"
          min-width="120px"
          prop="tags">
          <template v-slot:default="scope">
            <el-tooltip class="item" effect="dark" placement="top">
              <div v-html="getTagToolTips(scope.row.tags)" slot="content"></div>
              <div class="oneLine">
                <ms-tag
                  v-for="(itemName, index) in scope.row.tags"
                  :key="index"
                  type="success"
                  effect="plain"
                  :show-tooltip="scope.row.tags.length === 1 && itemName.length * 12 <= 100"
                  :content="itemName"
                  style="margin-left: 0px; margin-right: 2px" />
              </div>
            </el-tooltip>
          </template>
        </ms-table-column>

        <ms-table-column
          :label="$t('project.version.name')"
          :field="item"
          :fields-width="fieldsWidth"
          :filters="versionFilters"
          min-width="100px"
          prop="versionId">
          <template v-slot:default="scope">
            <span>{{ scope.row.versionName }}</span>
          </template>
        </ms-table-column>
        <ms-table-column
          :label="$t('api_test.definition.request.responsible')"
          :filters="userFilters"
          :field="item"
          :fields-width="fieldsWidth"
          prop="principalName"
          min-width="120px"
          sortable />
        <ms-table-column
          :label="$t('api_test.automation.creator')"
          :filters="userFilters"
          :field="item"
          :fields-width="fieldsWidth"
          prop="creatorName"
          min-width="120px"
          sortable="custom" />
        <ms-table-column
          :field="item"
          :fields-width="fieldsWidth"
          :filters="environmentsFilters"
          prop="environmentMap"
          :label="$t('commons.environment')"
          min-width="180">
          <template v-slot:default="{ row }">
            <div v-if="row.environmentMap">
              <span v-for="(k, v, index) in row.environmentMap" :key="index">
                <span v-if="index === 0">
                  <span class="project-name" :title="v">{{ v }}</span
                  >:
                  <el-tag type="success" size="mini" effect="plain">
                    <span class="project-env" :title="k">{{ k }}</span>
                  </el-tag>
                  <br />
                </span>
                <el-popover placement="top" width="350" trigger="click">
                  <div v-for="(k, v, index) in row.environmentMap" :key="index">
                    <span class="plan-case-env" :title="v">{{ v }}</span
                    >:
                    <el-tag type="success" size="mini" effect="plain">
                      <span class="project-env" style="margin: 0 0 0 5px" :title="k">{{ k }} </span>
                    </el-tag>
                    <br />
                  </div>
                  <el-link v-if="index === 1" slot="reference" type="info" :underline="false" icon="el-icon-more" />
                </el-popover>
              </span>
            </div>
          </template>
        </ms-table-column>
        <ms-table-column
          :label="$t('commons.trigger_mode.schedule')"
          :field="item"
          :fields-width="fieldsWidth"
          min-width="100px"
          :filters="!trashEnable ? scheduleFilters : null"
          prop="schedule">
          <template v-slot:default="scope">
            <schedule-info-in-table
              v-if="scope.row.scheduleObj"
              @openSchedule="openSchedule(scope.row)"
              @scheduleChange="scheduleStatusChange"
              @refreshTable="nodeChange"
              :scenario="scope.row"
              :has-permission="trashEnable"
              :request="runRequest"
              :schedule="scope.row.scheduleObj" />
            <i v-else class="el-icon-loading" />
          </template>
        </ms-table-column>
        <ms-table-column
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.update_time')"
          sortable
          prop="updateTime"
          min-width="180px">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>
        <ms-table-column
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.create_time')"
          sortable
          prop="createTime"
          min-width="180px">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('api_test.automation.step')"
          prop="stepTotal"
          min-width="80px" />
        <ms-table-column
          :label="$t('api_test.automation.last_result')"
          :filters="resultFilters"
          :field="item"
          :fields-width="fieldsWidth"
          sortable
          prop="lastResult"
          min-width="130px">
          <template v-slot:default="{ row }">
            <el-link @click="showReport(row)" :disabled="!row.lastResult || row.lastResult === 'PENDING'">
              <ms-api-report-status :status="row.lastResult" />
            </el-link>
          </template>
        </ms-table-column>

        <ms-table-column
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('api_test.automation.passing_rate')"
          prop="passRate"
          min-width="120px" />
      </span>

      <template v-slot:opt-before="scope">
        <ms-table-operator-button
          :tip="$t('api_test.automation.execute')"
          @exec="run(scope.row)"
          v-if="!scope.row.isStop && !trashEnable"
          icon="el-icon-video-play"
          class="run-button"
          style="margin-right: 10px"
          v-permission="['PROJECT_API_SCENARIO:READ+RUN']" />
        <el-tooltip :content="$t('report.stop_btn')" placement="top" :enterable="false" v-else>
          <el-button
            v-if="!trashEnable"
            @click.once="stop(scope.row)"
            size="mini"
            style="color: white; padding: 0; width: 28px; height: 28px; margin-right: 10px"
            class="stop-btn"
            circle>
            <div style="transform: scale(0.72)">
              <span style="margin-left: -3.5px; font-weight: bold">STOP</span>
            </div>
          </el-button>
        </el-tooltip>
      </template>

      <template v-slot:opt-behind="scope">
        <ms-scenario-extend-buttons
          :request="runRequest"
          :row="scope.row"
          @openSchedule="openSchedule(scope.row)"
          @openScenario="openScenario"
          @showCaseRef="showScenarioRef"
          v-if="!trashEnable"
          style="display: contents" />
      </template>
    </ms-table>

    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total" />

    <div>
      <!-- 执行结果 -->
      <el-drawer
        :visible.sync="runVisible"
        :destroy-on-close="true"
        direction="rtl"
        :withHeader="true"
        :modal="false"
        size="90%">
        <sysn-api-report-detail
          @refresh="search"
          :debug="true"
          :scenario="currentScenario"
          :scenarioId="scenarioId"
          :infoDb="infoDb"
          :report-id="reportId"
          :currentProjectId="projectId" />
      </el-drawer>
      <!-- 执行结果 -->
      <el-drawer
        :visible.sync="showReportVisible"
        :destroy-on-close="true"
        direction="rtl"
        :withHeader="true"
        :modal="false"
        size="90%">
        <ms-api-report-detail
          @invisible="showReportVisible = false"
          @refresh="search"
          :infoDb="infoDb"
          :show-cancel-button="false"
          :report-id="showReportId"
          :currentProjectId="projectId" />
      </el-drawer>
      <!--测试计划-->
      <el-drawer
        :visible.sync="planVisible"
        :destroy-on-close="true"
        direction="rtl"
        :withHeader="false"
        :title="$t('test_track.plan_view.test_result')"
        :modal="false"
        size="90%">
        <ms-test-plan-list
          @addTestPlan="addTestPlan(arguments)"
          @cancel="cancel"
          ref="testPlanList"
          :scenario-condition="condition"
          :row="selectRows" />
      </el-drawer>
    </div>

    <batch-edit
      ref="batchEdit"
      @batchEdit="batchEdit"
      :typeArr="typeArr"
      :value-arr="valueArr"
      :dialog-title="$t('test_track.case.batch_edit_case')" />
    <batch-move @refresh="search" @moveSave="moveSave" ref="testBatchMove" />
    <ms-api-run-mode
      :request="runRequest"
      :project-id="projectId"
      @close="search"
      @handleRunBatch="handleRunBatch"
      ref="apiBatchRun" />
    <ms-run
      :debug="true"
      :environment="projectEnvMap"
      :reportId="reportId"
      :saved="true"
      :executeType="'Saved'"
      :environment-type="environmentType"
      :environment-group-id="envGroupId"
      :run-data="debugData"
      @runRefresh="runRefresh"
      @errorRefresh="errorRefresh"
      ref="runTest" />
    <ms-task-center ref="taskCenter" :show-menu="false" />
    <relationship-graph-drawer :graph-data="graphData" ref="relationshipGraph" />
    <!--  删除接口提示  -->
    <scenario-delete-confirm ref="apiDeleteConfirmVersion" @handleDelete="_handleDelete" />
    <!--  删除场景弹窗  -->
    <api-delete-confirm
      :has-ref="hasRef"
      :show-scenario="showScenario"
      @showCaseRef="showScenarioRef"
      @handleDeleteCase="handleDeleteScenario"
      ref="apiDeleteConfirm" />
    <!--  引用场景弹窗  -->
    <ms-show-reference ref="viewRef" @showCaseRef="showScenarioRef" @openScenario="openScenario" />
  </el-card>
</template>

<script>
import {
  batchCopyScenario,
  batchEditScenario,
  batchGenPerformanceTestJmx,
  checkBeforeDelete,
  delByScenarioId,
  delByScenarioIdAndRefId,
  deleteBatchByCondition,
  editApiScenarioCaseOrder,
  execStop,
  exportScenario,
  getScenarioById,
  getScenarioList,
  getScenarioVersions,
  getScenarioWithBLOBsById,
  getScheduleDetail,
  listWithIds,
  removeScenarioToGcByBatch,
  runBatch,
  scenarioAllIds,
  scenarioPlan,
  scenarioReduction,
  scenarioRun,
  updateScenarioEnv,
} from '@/api/scenario';
import { getMaintainer, getProject } from '@/api/project';
import { getProjectVersions, versionEnableByProjectId } from '@/api/xpack';
import { getCurrentProjectID, getCurrentUserId } from 'metersphere-frontend/src/utils/token';
import { downloadFile, getUUID, objToStrMap, strMapToObj } from 'metersphere-frontend/src/utils';
import { hasLicense, hasPermission } from 'metersphere-frontend/src/utils/permission';
import { API_SCENARIO_CONFIGS } from 'metersphere-frontend/src/components/search/search-components';
import { API_SCENARIO_LIST } from 'metersphere-frontend/src/utils/constants';
import {
  buildBatchParam,
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField,
  getSelectDataCounts,
} from 'metersphere-frontend/src/utils/tableUtils';
import { API_SCENARIO_FILTERS } from 'metersphere-frontend/src/utils/table-constants';
import MsTable from 'metersphere-frontend/src/components/table/MsTable';
import MsTableColumn from 'metersphere-frontend/src/components/table/MsTableColumn';
import HeaderLabelOperate from 'metersphere-frontend/src/components/head/HeaderLabelOperate';
import { getGraphByCondition } from '@/api/graph';
import { API_SCENARIO_CONFIGS_TRASH, TYPE_TO_C } from '@/business/automation/scenario/Setting';
import MsTableSearchBar from 'metersphere-frontend/src/components/MsTableSearchBar';
import MsTableAdvSearchBar from 'metersphere-frontend/src/components/search/MsTableAdvSearchBar';
import ListItemDeleteConfirm from 'metersphere-frontend/src/components/ListItemDeleteConfirm';
import ScenarioDeleteConfirm from '@/business/automation/scenario/ScenarioDeleteConfirm';
import { $error } from 'metersphere-frontend/src/plugins/message';
import MsSearch from 'metersphere-frontend/src/components/search/MsSearch';
import { buildNodePath } from 'metersphere-frontend/src/model/NodeTree';
import { getEnvironmentByProjectId } from 'metersphere-frontend/src/api/environment';
import { REPORT_STATUS } from '@/business/commons/js/commons';
import { usePerformanceStore } from '@/store';
import { request } from 'metersphere-frontend/src/plugins/request';
import { parseEnvironment } from '@/business/environment/model/EnvironmentModel';
import MsApiRunMode from '@/business/automation/scenario/common/ApiRunMode';
import ApiDeleteConfirm from '@/business/definition/components/list/ApiDeleteConfirm';
import MsShowReference from '@/business/definition/components/reference/ShowReference';
import scheduleInfoInTable from '@/business/automation/schedule/ScheduleInfoInTable.vue';
import { scheduleUpdate } from '@/api/schedule';

const performanceStore = usePerformanceStore();
export default {
  name: 'MsApiScenarioList',
  components: {
    ListItemDeleteConfirm,
    MsTableAdvSearchBar,
    MsTableSearchBar,
    MsTable,
    MsTableColumn,
    HeaderLabelOperate,
    MsSearch,
    MsApiRunMode,
    ApiDeleteConfirm,
    MsShowReference,
    ScenarioDeleteConfirm,
    scheduleInfoInTable,
    MsApiReportStatus: () => import('../report/ApiReportStatus'),
    HeaderCustom: () => import('metersphere-frontend/src/components/head/HeaderCustom'),
    BatchMove: () => import('@/business/commons/BatchMove'),
    EnvironmentSelect: () => import('@/business/environment/components/EnvironmentSelect'),
    BatchEdit: () => import('@/business/commons/BatchEdit'),
    PlanStatusTableItem: () => import('@/business/commons/PlanStatusTableItem'),
    PriorityTableItem: () => import('@/business/commons/PriorityTableItem'),
    MsTableHeaderSelectPopover: () => import('metersphere-frontend/src/components/table/MsTableHeaderSelectPopover'),
    MsTablePagination: () => import('metersphere-frontend/src/components/pagination/TablePagination'),
    MsTableMoreBtn: () => import('./TableMoreBtn'),
    ShowMoreBtn: () => import('@/business/commons/ShowMoreBtn'),
    MsTableHeader: () => import('metersphere-frontend/src/components/MsTableHeader'),
    MsTag: () => import('metersphere-frontend/src/components/MsTag'),
    MsApiReportDetail: () => import('../report/ApiReportDetail'),
    SysnApiReportDetail: () => import('../report/SyncApiReportDetail'),
    MsScenarioExtendButtons: () => import('@/business/automation/scenario/ScenarioExtendBtns'),
    MsTestPlanList: () => import('./testplan/TestPlanList'),
    MsTableOperatorButton: () => import('metersphere-frontend/src/components/MsTableOperatorButton'),
    MsTaskCenter: () => import('metersphere-frontend/src/components/task/TaskCenter'),
    MsRun: () => import('./DebugRun'),
    RelationshipGraphDrawer: () => import('metersphere-frontend/src/components/graph/RelationshipGraphDrawer'),
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
      default: '',
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
    customNum: {
      type: Boolean,
      default: false,
    },
    initApiTableOpretion: String,
    isRelate: Boolean,
  },
  data() {
    return {
      environmentsFilters: [],
      projectName: '',
      result: false,
      tableHeaderKey: 'API_SCENARIO',
      type: API_SCENARIO_LIST,
      fields: getCustomTableHeader('API_SCENARIO', undefined),
      fieldsWidth: getCustomTableWidth('API_SCENARIO'),
      screenHeight: 'calc(100vh - 200px)', //屏幕高度,
      condition: {
        components: this.trashEnable ? API_SCENARIO_CONFIGS_TRASH : API_SCENARIO_CONFIGS,
      },
      scheduleFilters: [
        { text: this.$t('filters.schedule.open'), value: 'open' },
        { text: this.$t('filters.schedule.close'), value: 'close' },
        { text: this.$t('filters.schedule.unset'), value: 'unset' },
      ],
      projectId: '',
      scenarioId: '',
      isMoveBatch: true,
      currentScenario: {},
      schedule: {},
      tableData: [],
      selectDataRange: 'all',
      selectDataType: 'all',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reportId: '',
      showReportId: '',
      projectEnvMap: new Map(),
      batchReportId: '',
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
      versionFilters: [],
      operators: [],
      selectRows: new Set(),
      isStop: false,
      enableOrderDrag: true,
      debugData: {},
      trashOperators: [
        {
          tip: this.$t('commons.reduction'),
          icon: 'el-icon-refresh-left',
          exec: this.reductionApi,
          permissions: ['PROJECT_API_SCENARIO:READ+EDIT'],
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.remove,
          icon: 'el-icon-delete',
          type: 'danger',
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE'],
        },
      ],
      unTrashOperators: [
        {
          tip: this.$t('commons.edit'),
          icon: 'el-icon-edit',
          exec: this.edit,
          permissions: ['PROJECT_API_SCENARIO:READ+EDIT'],
        },
        {
          tip: this.$t('api_test.automation.copy'),
          icon: 'el-icon-document-copy',
          exec: this.copy,
          permissions: ['PROJECT_API_SCENARIO:READ+COPY'],
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.remove,
          icon: 'el-icon-delete',
          type: 'danger',
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE'],
        },
      ],
      buttons: [],
      trashButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE'],
        },
        {
          name: this.$t('commons.batch_restore'),
          handleClick: this.handleBatchRestore,
        },
      ],
      unTrashButtons: [
        {
          name: this.$t('api_test.automation.batch_execute'),
          handleClick: this.handleBatchExecute,
          permissions: ['PROJECT_API_SCENARIO:READ+RUN'],
        },
        {
          name: this.$t('test_track.case.batch_edit_case'),
          handleClick: this.handleBatchEdit,
          permissions: ['PROJECT_API_SCENARIO:READ+EDIT'],
        },
        {
          name: this.$t('test_track.case.batch_move_case'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_API_SCENARIO:READ+MOVE_BATCH'],
        },
        {
          name: this.$t('api_test.batch_copy'),
          handleClick: this.handleBatchCopy,
          permissions: ['PROJECT_API_SCENARIO:READ+BATCH_COPY'],
        },
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE'],
        },
        {
          name: this.$t('test_track.case.generate_dependencies'),
          handleClick: this.generateGraph,
          permissions: ['PROJECT_API_SCENARIO:READ+EDIT'],
        },
        {
          name: this.$t('api_test.automation.batch_add_plan'),
          handleClick: this.handleBatchAddCase,
          permissions: ['PROJECT_API_SCENARIO:READ+MOVE_BATCH'],
        },
        {
          name: this.$t('api_test.create_performance_test_batch'),
          handleClick: this.batchCreatePerformance,
          permissions: ['PROJECT_API_SCENARIO:READ+CREATE_PERFORMANCE_BATCH'],
        },
      ],
      typeArr: [
        { id: 'level', name: this.$t('test_track.case.priority') },
        { id: 'status', name: this.$t('test_track.plan.plan_status') },
        {
          id: 'principal',
          name: this.$t('api_test.definition.request.responsible'),
          optionMethod: this.getPrincipalOptions,
        },
        // {id: 'environmentId', name: this.$t('api_test.definition.request.run_env'), optionMethod: this.getEnvsOptions},
        {
          id: 'projectEnv',
          name: this.$t('api_test.definition.request.run_env'),
        },
        { id: 'tags', name: this.$t('commons.tag') },
      ],
      valueArr: {
        level: [
          { name: 'P0', id: 'P0' },
          { name: 'P1', id: 'P1' },
          { name: 'P2', id: 'P2' },
          { name: 'P3', id: 'P3' },
        ],
        status: [
          {
            name: this.$t('test_track.plan.plan_status_prepare'),
            id: 'Prepare',
          },
          {
            name: this.$t('test_track.plan.plan_status_running'),
            id: 'Underway',
          },
          {
            name: this.$t('test_track.plan.plan_status_completed'),
            id: 'Completed',
          },
        ],
        principal: [],
        environmentId: [],
        projectEnv: [],
        projectId: '',
      },
      graphData: {},
      environmentType: '',
      envGroupId: '',
      scenarioFilters: {},
      resultFilters: REPORT_STATUS,
      runRequest: {},
      versionEnable: false,
      hasRef: false,
      showScenario: false,
    };
  },
  created() {
    this.scenarioFilters = API_SCENARIO_FILTERS();
    this.$EventBus.$on('hide', (id) => {
      this.hideStopBtn(id);
    });
    this.projectId = getCurrentProjectID();
    if (!this.projectName || this.projectName === '') {
      this.getProjectName();
    }
    this.condition.filters = { status: ['Prepare', 'Underway', 'Completed'] };
    this.initEnvironment();
    if (this.trashEnable) {
      this.condition.filters = { status: ['Trash'] };
      this.condition.moduleIds = [];
      this.operators = this.trashOperators;
      this.buttons = this.trashButtons;
    } else {
      if (!this.isReferenceTable) {
        this.operators = this.unTrashOperators;
        this.buttons = this.unTrashButtons;
      } else {
        this.operators = this.unTrashOperators;
        this.buttons = this.unTrashButtons;
      }
    }

    if (this.trashEnable) {
      this.condition.orders = [{ name: 'delete_time', type: 'desc' }];
    } else {
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);
    }

    this.search();
    this.getPrincipalOptions([]);
    this.getVersionOptions();

    if (this.isRelate) {
      this.checkVersionEnable(this.selectProjectId);
    } else {
      this.checkVersionEnable(this.projectId);
    }

    // 通知过来的数据跳转到编辑
    if (this.$route.query.resourceId) {
      getScenarioById(this.$route.query.resourceId).then((response) => {
        this.edit(response.data);
      });
    }
  },
  beforeDestroy() {
    this.$EventBus.$off('hide');
  },
  watch: {
    selectNodeIds() {
      this.currentPage = 1;
      this.$refs.scenarioTable.clear();
      this.selectProjectId ? this.nodeChange(this.selectProjectId) : this.nodeChange();
    },
    trashEnable() {
      if (this.trashEnable) {
        this.condition.filters = { status: ['Trash'] };
        this.condition.moduleIds = [];
        this.operators = this.trashOperators;
        this.buttons = this.trashButtons;
      } else {
        this.condition.filters = {
          status: ['Prepare', 'Underway', 'Completed'],
        };
        this.operators = this.unTrashOperators;
        this.buttons = this.unTrashButtons;
      }
      this.$refs.scenarioTable.clear();
      this.search();
    },
    batchReportId() {
      this.result = true;
      this.getReport();
    },
  },
  computed: {
    isNotRunning() {
      return 'Running' !== this.report.status;
    },
    editApiScenarioCaseOrder() {
      return editApiScenarioCaseOrder;
    },
    moduleOptionsNew() {
      let moduleOptions = [];
      this.moduleOptions.forEach((node) => {
        buildNodePath(node, { path: '' }, moduleOptions);
      });
      return moduleOptions;
    },
  },
  methods: {
    generateGraph() {
      if (getSelectDataCounts(this.condition, this.total, this.selectRows) > 100) {
        this.$warning(this.$t('test_track.case.generate_dependencies_warning'));
        return;
      }
      getGraphByCondition('API_SCENARIO', buildBatchParam(this, this.$refs.scenarioTable.selectIds)).then((data) => {
        this.graphData = data.data;
        this.$refs.relationshipGraph.open();
      });
    },
    getProjectName() {
      getProject(this.projectId).then((response) => {
        let project = response.data;
        if (project) {
          this.projectName = project.name;
        }
      });
    },
    initEnvironment() {
      if (this.projectId) {
        getEnvironmentByProjectId(this.projectId).then((response) => {
          this.environments = response.data;
          this.environments.forEach((environment) => {
            parseEnvironment(environment);
          });
          this.environmentsFilters = response.data.map((u) => {
            return { text: u.name, value: u.id };
          });
        });
      }
    },
    search(projectId) {
      this.nodeChange(projectId);
    },
    nodeChange(projectId) {
      if (this.needRefreshModule()) {
        this.$emit('refreshTree');
      }
      if (this.selectProjectId) {
        projectId = this.selectProjectId;
      }
      this.selectRows = new Set();
      this.condition.moduleIds = this.selectNodeIds;

      if (this.trashEnable) {
        this.condition.filters = {
          ...this.condition.filters,
          status: ['Trash'],
        };
      }

      if (!this.condition.filters || !this.condition.filters.status) {
        this.condition.filters = {
          status: ['Prepare', 'Underway', 'Completed'],
        };
      }

      if (projectId != null && typeof projectId === 'string') {
        this.condition.projectId = projectId;
      } else if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }

      this.enableOrderDrag = this.condition.orders && this.condition.orders.length <= 0;

      //检查是否只查询本周数据
      this.condition.selectThisWeedData = false;
      this.condition.executeStatus = null;
      this.condition.selectDataRange = null;
      this.isRedirectFilter();
      this.condition.selectDataType = this.selectDataType;
      switch (this.selectDataRange) {
        case 'createdInWeek':
          this.condition.selectThisWeedData = true;
          break;
        case 'unExecuteCount':
          this.condition.executeStatus = 'PENDING';
          break;
        case 'executionFailedCount':
          this.condition.executeStatus = 'executeFailed';
          break;
        case 'fakeErrorCount':
          this.condition.executeStatus = 'fakeError';
          break;
        case 'executionPassCount':
          this.condition.executeStatus = 'executePass';
          break;
        case 'executedCount':
          this.condition.executeStatus = 'executedCount';
          break;
        case 'running':
          this.condition.selectDataRange = 'scheduleRunning';
          break;
        case 'notRun':
          this.condition.selectDataRange = 'scheduleNotRun';
          break;
      }
      if (this.selectDataRange != null) {
        let selectParamArr = this.selectDataRange.split(':');
        if (selectParamArr.length === 2) {
          if (selectParamArr[0] === 'list') {
            let ids = selectParamArr[1].split(',');
            this.condition.ids = ids;
          }
        }
      }
      this.$EventBus.$emit('scenarioConditionBus', this.condition);
      if (this.condition.projectId) {
        this.result = getScenarioList(this.currentPage, this.pageSize, this.condition).then((response) => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          let ids = [];
          this.tableData.forEach((item) => {
            ids.push(item.id);
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
          this.$emit('getTrashCase');
          if (this.$refs.scenarioTable) {
            this.$refs.scenarioTable.clearSelection();
          }
          this.selectSchedule(ids);
        });
      }
    },
    selectSchedule(ids) {
      if (ids.length > 0) {
        getScheduleDetail(ids).then((response) => {
          if (response.data) {
            let scheduleData = response.data;
            this.tableData.forEach((scenario) => {
              let scheduleInfo = this.getScheduleObject(scheduleData[scenario.id], scenario.id);
              this.$set(scenario, 'scheduleObj', scheduleInfo);
            });
          }
        });
      }
    },
    scheduleStatusChange(schedule) {
      let scheduleRequest = {
        taskID: schedule.id,
        enable: schedule.enable,
      };
      this.result = scheduleUpdate(scheduleRequest)
        .then(() => {
          schedule.scheduleStatus = schedule.enable ? 'OPEN' : 'SHUT';
          this.$success(this.$t('commons.save_success'));
        })
        .catch(() => {
          this.$success(this.$t('commons.save_failed'));
          schedule.enable = !schedule.enable;
        });
    },
    getScheduleObject(schedule, resourceId) {
      if (schedule) {
        return {
          scheduleStatus: schedule.enable ? 'OPEN' : 'SHUT',
          scheduleCorn: schedule.value,
          scheduleExecuteTime: schedule.scheduleExecuteTime,
          enable: schedule.enable,
          id: schedule.id,
          resourceId: schedule.resourceId,
        };
      } else {
        return {
          scheduleStatus: '',
          scheduleCorn: '',
          scheduleExecuteTime: '',
          enable: false,
          id: '',
          resourceId: resourceId,
        };
      }
    },
    handleCommand(cmd) {
      let table = this.$refs.scenarioTable;
      switch (cmd) {
        case 'table':
          this.condition.selectAll = false;
          table.toggleAllSelection();
          break;
        case 'all':
          this.condition.selectAll = true;
          break;
      }
    },
    handleBatchAddCase() {
      this.selectRows = this.$refs.scenarioTable.selectRows;
      this.planVisible = true;
    },
    handleBatchEdit() {
      this.$refs.batchEdit.setScenarioSelectRows(this.$refs.scenarioTable.selectRows, 'scenario');
      if (this.condition.selectAll) {
        this.condition.ids = [];
        let param = {};
        this.buildBatchParam(param);
        listWithIds(param).then((response) => {
          let dataRows = response.data;
          this.$refs.batchEdit.open(dataRows.size);
          this.$refs.batchEdit.setAllDataRows(dataRows);
          this.$refs.batchEdit.open(this.$refs.scenarioTable.selectDataCounts);
        });
      } else {
        this.$refs.batchEdit.setAllDataRows(new Set());
        this.$refs.batchEdit.open(this.$refs.scenarioTable.selectDataCounts);
      }
    },
    handleBatchMove() {
      this.isMoveBatch = true;
      this.$refs.testBatchMove.open(this.moduleTree, [], this.moduleOptionsNew);
    },
    handleBatchCopy() {
      this.isMoveBatch = false;
      this.$refs.testBatchMove.open(this.moduleTree, this.$refs.scenarioTable.selectIds, this.moduleOptionsNew);
    },
    moveSave(param) {
      this.buildBatchParam(param);
      param.apiScenarioModuleId = param.nodeId;
      param.modulePath = param.nodePath;
      if (!this.isMoveBatch) {
        batchCopyScenario(param).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.$refs.testBatchMove.close();
          this.search();
        });
      } else {
        batchEditScenario(param).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.$refs.testBatchMove.close();
          this.search();
        });
      }
    },
    batchEdit(form) {
      // 批量修改环境
      if (form.type === 'projectEnv') {
        let param = {};
        param.mapping = strMapToObj(form.map);
        param.envMap = strMapToObj(form.projectEnvMap);
        param.environmentType = form.environmentType;
        param.environmentGroupId = form.envGroupId;
        updateScenarioEnv(param).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      } else {
        // 批量修改其它
        let param = {};
        if (form.type === 'tags') {
          param.type = form.type;
          param.appendTag = form.appendTag;
          param.tagList = form.tags;
        } else {
          param[form.type] = form.value;
        }
        this.buildBatchParam(param);
        batchEditScenario(param).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      }
    },
    getPrincipalOptions(option) {
      getMaintainer().then((response) => {
        option.push(...response.data);
        this.userFilters = response.data.map((u) => {
          return { text: u.name, value: u.id };
        });
      });
    },
    getVersionOptions(currentVersion) {
      if (hasLicense()) {
        getProjectVersions(getCurrentProjectID()).then((response) => {
          if (currentVersion) {
            this.versionFilters = response.data
              .filter((u) => u.id === currentVersion)
              .map((u) => {
                return { text: u.name, value: u.id };
              });
          } else {
            this.versionFilters = response.data.map((u) => {
              return { text: u.name, value: u.id };
            });
          }
        });
      }
    },
    checkVersionEnable(projectId) {
      if (!projectId) {
        return;
      }
      if (hasLicense()) {
        versionEnableByProjectId(projectId).then((response) => {
          this.versionEnable = response.data;
          if (!response.data) {
            this.fields = this.fields.filter((f) => f.id !== 'versionId');
          }
        });
      }
    },
    cancel() {
      this.planVisible = false;
    },
    addTestPlan(params) {
      let obj = {
        planIds: params[0],
        scenarioIds: this.$refs.scenarioTable.selectIds,
      };

      this.planVisible = false;

      obj.mapping = strMapToObj(params[2]);
      obj.envMap = strMapToObj(params[1]);
      obj.environmentType = params[3];
      obj.envGroupId = params[4];

      scenarioPlan(obj).then((response) => {
        this.$success(this.$t('commons.save_success'));
        this.search();
      });
    },
    getReport() {
      if (this.batchReportId) {
        this.result = false;
        this.$success(this.$t('commons.run_message'));
        this.$refs.taskCenter.open('SCENARIO');
      }
    },
    buildBatchParam(param) {
      param.ids = this.$refs.scenarioTable.selectIds;
      param.projectId = this.projectId;
      param.condition = this.condition;
    },
    handleBatchExecute() {
      let run = {};
      run.id = getUUID();
      //按照列表排序
      let ids = this.orderBySelectRows();
      run.ids = ids;
      run.projectId = this.projectId;
      run.condition = this.condition;
      this.runRequest = run;
      this.$nextTick(() => {
        this.$refs.apiBatchRun.open();
      });
    },
    openSchedule(row) {
      let run = {};
      run.id = getUUID();
      run.ids = [row.id];
      run.projectId = this.projectId;
      run.condition = this.condition;
      this.runRequest = run;
    },
    orderBySelectRows() {
      let selectIds = this.$refs.scenarioTable.selectIds;
      let array = [];
      for (let i in this.tableData) {
        if (selectIds.indexOf(this.tableData[i].id) !== -1) {
          array.push(this.tableData[i].id);
        }
      }
      return array;
    },

    handleRunBatch(config) {
      this.infoDb = false;
      let run = { config: config };
      run.id = getUUID();
      //按照列表排序
      let ids = this.orderBySelectRows();
      run.ids = ids;
      run.projectId = this.projectId;
      run.condition = this.condition;
      runBatch(run).then(() => {
        this.runVisible = false;
        this.batchReportId = run.id;
      });
    },
    edit(row) {
      let data = JSON.parse(JSON.stringify(row));
      this.$emit('edit', data);
    },
    reductionApi(row) {
      scenarioReduction([row.id]).then((response) => {
        this.$success(this.$t('commons.save_success'));
        this.search();
      });
    },
    handleBatchRestore() {
      let ids = this.$refs.scenarioTable.selectIds;
      let params = {};
      this.buildBatchParam(params);
      params.ids = ids;
      scenarioAllIds(params).then((response) => {
        let idParams = response.data;
        scenarioReduction(idParams).then((response) => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      });
    },
    handleDeleteBatch(row) {
      if (this.trashEnable) {
        let param = {};
        this.buildBatchParam(param);
        this.result = true;
        deleteBatchByCondition(param).then(
          () => {
            this.$success(this.$t('commons.delete_success'));
            this.search();
          },
          (error) => {
            this.search();
          }
        );
        return;
      } else {
        let param = {};
        this.buildBatchParam(param);
        param.type = 'batch';
        this.showScenario = false;
        this.hasRef = false;
        checkBeforeDelete(param).then((response) => {
          let checkResult = response.data;
          let alertMsg = this.$t('load_test.delete_threadgroup_confirm') + ' ？';
          if (checkResult.deleteFlag) {
            alertMsg =
              this.$t('api_definition.scenario_is_referenced', [checkResult.refCount]) +
              ', ' +
              this.$t('api_test.is_continue') +
              ' ？';
            this.showScenario = true;
          }
          this.$refs.apiDeleteConfirm.open(
            alertMsg,
            this.$t('permission.project_api_scenario.delete'),
            param,
            checkResult.checkMsg
          );
        });
      }
    },
    getApiScenario(scenarioId) {
      return new Promise((resolve) => {
        this.result = getScenarioWithBLOBsById(scenarioId).then((response) => {
          if (response.data) {
            this.currentScenario = response.data;
            this.currentScenario.clazzName = TYPE_TO_C.get('scenario');
            if (response.data.scenarioDefinition != null) {
              let obj = JSON.parse(response.data.scenarioDefinition);
              this.currentScenario.scenarioDefinition = obj;
              this.currentScenario.name = response.data.name;
              if (this.currentScenario.scenarioDefinition && this.currentScenario.scenarioDefinition.hashTree) {
                this.sort(this.currentScenario.scenarioDefinition.hashTree);
              }
              resolve();
            }
          }
        });
      });
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
        if (stepArray[i].type === 'Assertions' && !stepArray[i].document) {
          stepArray[i].document = {
            type: 'JSON',
            data: {
              xmlFollowAPI: false,
              jsonFollowAPI: false,
              json: [],
              xml: [],
            },
          };
        }
        if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
          stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
        }
        if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
          this.sort(stepArray[i].hashTree);
        }
      }
    },
    execute(row) {
      this.infoDb = false;
      this.scenarioId = row.id;
      let run = {};
      let scenarioIds = [];
      scenarioIds.push(row.id);
      run.id = getUUID();
      run.projectId = this.projectId;
      run.ids = scenarioIds;
      run.executeType = 'Saved';
      scenarioRun(run).then(
        (response) => {
          this.runVisible = true;
          this.$set(row, 'isStop', true);
          this.reportId = run.id;
        },
        () => {
          this.$set(row, 'isStop', false);
        }
      );
    },
    runRefresh(row) {
      this.$set(row, 'isStop', false);
    },
    errorRefresh() {
      this.search();
    },
    run(row) {
      this.scenarioId = row.id;
      this.getApiScenario(row.id).then(() => {
        let scenarioStep = this.currentScenario.scenarioDefinition;
        if (scenarioStep) {
          this.debugData = {
            id: this.currentScenario.id,
            name: this.currentScenario.name,
            type: 'scenario',
            variables: scenarioStep.variables,
            referenced: 'Created',
            onSampleError: scenarioStep.onSampleError,
            enableCookieShare: scenarioStep.enableCookieShare,
            headers: scenarioStep.headers,
            environmentMap: this.currentScenario.environmentJson
              ? objToStrMap(JSON.parse(this.currentScenario.environmentJson))
              : new Map(),
            hashTree: scenarioStep.hashTree,
          };
          if (this.currentScenario.environmentJson) {
            this.projectEnvMap = objToStrMap(JSON.parse(this.currentScenario.environmentJson));
          }
          this.environmentType = this.currentScenario.environmentType;
          this.envGroupId = this.currentScenario.environmentGroupId;
          this.reportId = getUUID().substring(0, 8);
          this.runVisible = true;
          this.$set(row, 'isStop', true);
        }
      });
    },

    copy(row) {
      let rowParam = JSON.parse(JSON.stringify(row));
      rowParam.copy = true;
      rowParam.name = 'copy_' + rowParam.name;
      rowParam.customNum = '';
      rowParam.principal = getCurrentUserId();
      rowParam.createUser = getCurrentUserId();
      rowParam.userId = getCurrentUserId();
      this.$emit('edit', rowParam);
    },
    showReport(row) {
      this.showReportVisible = true;
      this.infoDb = true;
      this.showReportId = row.reportId;
    },
    //判断是否只显示本周的数据。  从首页跳转过来的请求会带有相关参数
    isRedirectFilter() {
      this.selectDataRange = 'all';
      this.selectDataType = 'all';
      let routeParamObj = this.$route.params;
      let redirectVersionId = this.$route.params.versionId;
      if (redirectVersionId && redirectVersionId !== 'default') {
        this.condition.versionId = redirectVersionId;
      }
      if (routeParamObj) {
        let dataRange = routeParamObj.dataSelectRange;
        let dataType = routeParamObj.dataType;
        this.selectDataRange = dataRange;
        this.selectDataType = dataType;
      }
      if (this.$route.query && this.$route.params.dataSelectRange === 'ref') {
        if (this.$route.query.ids) {
          if (typeof this.$route.query.ids === 'string') {
            this.condition.ids = [this.$route.query.ids];
          } else {
            this.condition.ids = this.$route.query.ids;
          }
        }
      }
    },
    changeSelectDataRangeAll() {
      this.$emit('changeSelectDataRangeAll');
    },
    remove(row) {
      if (this.trashEnable) {
        delByScenarioId(row.id).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.search();
        });
        return;
      } else {
        let param = {};
        this.buildBatchParam(param);
        param.ids = [row.id];
        this.showScenario = false;
        this.hasRef = false;
        checkBeforeDelete(param).then((response) => {
          let checkResult = response.data;
          let alertMsg = this.$t('load_test.delete_threadgroup_confirm') + '[' + row.name + ']' + '?';
          if (checkResult.deleteFlag) {
            alertMsg =
              '[' +
              row.name +
              '] ' +
              this.$t('api_definition.scenario_is') +
              (checkResult.scenarioCount > 0
                ? this.$t('api_definition.scenario_count', [checkResult.scenarioCount])
                : '') +
              (checkResult.planCount > 0 && checkResult.scenarioCount > 0 ? '、 ' : '') +
              (checkResult.planCount > 0 ? this.$t('api_definition.plan_count', [checkResult.planCount]) : '') +
              this.$t('api_test.scenario.reference') +
              ', ' +
              this.$t('api_test.is_continue') +
              ' ？';
            this.hasRef = true;
          }
          //
          getScenarioVersions(row.id).then((response) => {
            if (hasLicense() && this.versionEnable && response.data.length > 1) {
              // 删除提供列表删除和全部版本删除
              this.$refs.apiDeleteConfirmVersion.open(row, alertMsg);
            } else {
              this.$refs.apiDeleteConfirm.open(alertMsg, this.$t('permission.project_api_scenario.delete'), row, null);
            }
          });
        });
      }
    },
    handleDeleteScenario(row) {
      this.$refs.apiDeleteConfirm.close();
      if (row.type === 'batch') {
        removeScenarioToGcByBatch(row).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.search();
        });
      } else {
        this._handleDelete(row, false);
      }
    },
    _handleDelete(api, deleteCurrentVersion) {
      // 删除指定版本
      if (deleteCurrentVersion) {
        delByScenarioIdAndRefId(api.versionId, api.refId).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.$refs.apiDeleteConfirmVersion.close();
          this.search();
        });
      } else {
        let param = { ids: [api.id] };
        removeScenarioToGcByBatch(param).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.$refs.apiDeleteConfirmVersion.close();
          this.search();
        });
      }
    },
    openScenario(item) {
      this.$emit('openScenario', item);
    },
    exportApi(nodeTree) {
      let param = {};
      this.projectId = getCurrentProjectID();
      getProject(this.projectId).then((response) => {
        let project = response.data;
        if (project) {
          this.projectName = project.name;
          this.buildBatchParam(param);
          if (param.ids === undefined || param.ids.length < 1) {
            this.$warning(this.$t('api_test.automation.scenario.check_case'));
            return;
          }
          this.result = true;
          exportScenario(param).then((response) => {
            this.result = false;
            let obj = response.data;
            obj.nodeTree = nodeTree;
            downloadFile('Metersphere_Scenario_' + this.projectName + '.json', JSON.stringify(obj));
          });
        }
      });
    },
    fileDownload(url, param) {
      let config = {
        url: url,
        method: 'post',
        data: param,
        responseType: 'blob',
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
      };
      request(config).then(
        (response) => {
          let link = document.createElement('a');
          link.href = window.URL.createObjectURL(
            new Blob([response.data], {
              type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8',
            })
          );
          link.download = '场景JMX文件集.zip';
          this.result = false;
          link.click();
        },
        (error) => {
          this.result = false;
          if (error.response && error.response.status === 509) {
            let reader = new FileReader();
            reader.onload = function (event) {
              let content = reader.result;
              $error(content);
            };
            reader.readAsText(error.response.data);
          } else {
            $error('导出JMX文件失败，请检查是否选择环境');
          }
        }
      );
    },

    exportJmx() {
      let param = {};
      this.buildBatchParam(param);
      if (param.ids === undefined || param.ids.length < 1) {
        this.$warning(this.$t('api_test.automation.scenario.check_case'));
        return;
      }
      this.result = true;
      this.fileDownload('/api/automation/export/zip', param);
    },
    getConditions() {
      return this.condition;
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
    batchCreatePerformance() {
      if (!hasPermission('PROJECT_PERFORMANCE_TEST:READ+CREATE')) {
        this.$warning(this.$t('api_test.create_performance_test_tips'));
        return;
      }
      this.$alert(this.$t('api_test.definition.request.batch_to_performance_confirm') + ' ？', '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.infoDb = false;
            let param = {};
            this.buildBatchParam(param);
            batchGenPerformanceTestJmx(param).then((response) => {
              let returnDTO = response.data;
              let projectEnvMap = returnDTO.projectEnvMap;
              let returnDataList = returnDTO.jmxInfoDTOList;
              let jmxObjList = [];
              returnDataList.forEach((item) => {
                let jmxObj = {};
                jmxObj.name = item.name;
                jmxObj.xml = item.xml;
                jmxObj.attachFiles = item.attachFiles;
                jmxObj.attachByteFiles = item.attachByteFiles;
                jmxObj.scenarioId = item.id;
                jmxObj.version = item.version;
                jmxObjList.push(jmxObj);
              });
              performanceStore.$patch({
                test: null,
                scenarioJmxs: {
                  name: 'Scenarios',
                  jmxs: jmxObjList,
                  projectEnvMap: projectEnvMap,
                },
              });
              this.$router.push({
                path: '/performance/test/create',
              });
            });
          }
        },
      });
    },
    stop(row) {
      execStop(this.reportId).then(() => {
        this.$set(row, 'isStop', false);
      });
    },
    hideStopBtn(scenarioId) {
      for (let data of this.tableData) {
        if (scenarioId && scenarioId === data.id) {
          this.$set(data, 'isStop', false);
        }
      }
    },
    showScenarioRef(row) {
      this.$refs.viewRef.open(row, 'SCENARIO');
    },
    getTagToolTips(tags) {
      try {
        let showTips = '';
        tags.forEach((item) => {
          showTips += item + ',';
        });
        return showTips.substr(0, showTips.length - 1);
      } catch (e) {
        return '';
      }
    },
  },
};
</script>

<style scoped>
:deep(.el-drawer__header) {
  margin-bottom: 0px;
}

:deep(.el-table__fixed-body-wrapper) {
  z-index: auto !important;
}

:deep(.el-table__fixed-right) {
  height: 100% !important;
}

:deep(.el-card__header) {
  padding: 10px;
}

.stop-btn {
  background-color: #e62424;
  border-color: #dd3636;
  color: white;
}

.plan-case-env {
  display: inline-block;
  max-width: 250px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-left: 5px;
  vertical-align: bottom;
}

.project-name {
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 80px;
  vertical-align: middle;
}

.project-env {
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  width: 50px;
  text-overflow: ellipsis;
  vertical-align: middle;
}

.scenario-div {
  padding-left: 10px;
  padding-bottom: 8px;
  border-radius: 5px;
  border: #dcdfe6 solid 1px;
}

.oneLine {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
