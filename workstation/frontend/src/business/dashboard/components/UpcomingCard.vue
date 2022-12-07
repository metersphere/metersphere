<template>
  <el-card class="table-card" body-style="padding:10px;">
    <el-row :gutter="10">
      <el-col :span="12">
        <span class="top-left-css">{{ $t('workstation.upcoming') }}</span>
      </el-col>
      <el-col :span="12" class="top-right-css">
        <i class="el-icon-refresh" @click="refresh()"></i>
        <el-select class="select-todo"
                   v-model="currentTodo"
                   filterable
                   default-first-option
                   @change="updateUpcoming"
                   placeholder="请选择待办">
          <el-option
            v-for="item in realTodoList"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <ms-main-container>
        <div v-if="toReLoad">
          <case-table-list
            v-if="currentTodo === 'TRACK_CASE'"
            @refresh="refresh"
            @setCondition="setCondition"
            :custom-num="custom_num"
            :is-show-all-column=false
            :is-select-all=true
            screen-height="40vh"
            ref="caseTableList">
          </case-table-list>
          <plan-table-list
            v-if="currentTodo === 'TRACK_PLAN'"
            :is-show-all-column=false
            :is-select-all=true
            :is-dashboard=true
            screen-height="40vh"
            ref="testPlanList">
          </plan-table-list>
          <review-table-list
            v-if="currentTodo === 'TRACK_REVIEW'"
            :is-show-all-column=false
            :is-select-all=true
            screen-height="40vh"
            ref="testPlanList">
          </review-table-list>
          <issue-table-list
            v-if="currentTodo === 'TRACK_ISSUE'"
            @handlePageChange="getIssues"
            @refresh="getIssues"
            :is-show-all-column=false
            :is-select-all=true
            :is-dashboard=true
            screen-height="40vh"
            ref="issueTableList"
          >
          </issue-table-list>
          <api-definition-table-list
            v-if="currentTodo === 'API_DEFINITION'"
            @runTest="runTest"
            :current-protocol="currentProtocol"
            :queryDataType="queryDataType"
            :is-read-only="false"
            :is-show-all-column=false
            :is-select-all=true
            @refreshTable="refresh"
            screen-height="40vh"
            ref="apiDefinitionTableList"
          >
          </api-definition-table-list>
          <performance-table-list
            v-if="currentTodo === 'PERFORMANCE'"
            :is-show-all-column=false
            :is-select-all=true
            screen-height="40vh"
            ref="performanceTableList"
          >
          </performance-table-list>
          <automation-table-list
            v-if="currentTodo === 'API_AUTOMATION'"
            :is-show-all-column=false
            :is-select-all=true
            :show-tap=true
            screen-height="40vh"
            ref="automationTableList">
          </automation-table-list>
          <api-case-table-list
            v-if="currentTodo === 'API_CASE'"
            :is-show-all-column=false
            :is-select-all=true
            :show-tap=true
            screen-height="40vh"
            ref="apiCaseTableList"
          >
          </api-case-table-list>
        </div>
      </ms-main-container>
    </el-row>
  </el-card>
</template>
<script>
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import CaseTableList from "@/business/component/CaseTableList";
import {getUUID} from "metersphere-frontend/src/utils";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {hasPermissions} from "metersphere-frontend/src/utils/permission";
import {getIssues, syncIssues} from "@/api/issue";
import PlanTableList from "@/business/component/PlanTableList";
import ReviewTableList from "@/business/component/ReviewTableList";
import IssueTableList from "@/business/component/IssueTableList";
import {getLastTableSortField} from "metersphere-frontend/src/utils/tableUtils";
import ApiDefinitionTableList from "@/business/component/ApiDefinitionTableList";
import PerformanceTableList from "@/business/component/PerformanceTableList";
import AutomationTableList from "@/business/component/AutomationTableList";
import ApiCaseTableList from "@/business/component/ApiCaseTableList";
import {WORKSTATION} from "metersphere-frontend/src/utils/constants";

export default {
  name: "UpcomingCard",
  components: {
    MsMainContainer,
    CaseTableList,
    PlanTableList,
    ReviewTableList,
    IssueTableList,
    ApiDefinitionTableList,
    PerformanceTableList,
    AutomationTableList,
    ApiCaseTableList
  },
  watch: {
    currentProtocol() {
      if (this.activeDom === 'right') {
        this.activeDom = 'left';
      }
    },

  },
  data() {
    return {
      toReLoad: true,
      currentTodo: '',
      todoList: [
        {
          value: 'TRACK_CASE',
          label: this.$t('workstation.table_name.track_case'),
          permission: ['PROJECT_TRACK_CASE:READ']
        },
        {
          value: 'TRACK_PLAN',
          label: this.$t('workstation.table_name.track_plan'),
          permission: ['PROJECT_TRACK_PLAN:READ']
        }, {
          value: 'TRACK_REVIEW',
          label: this.$t('workstation.table_name.track_review'),
          permission: ['PROJECT_TRACK_REVIEW:READ']
        }, {
          value: 'TRACK_ISSUE',
          label: this.$t('workstation.table_name.track_issue'),
          permission: ['PROJECT_TRACK_ISSUE:READ']
        }, {
          value: 'API_DEFINITION',
          label: this.$t('workstation.table_name.api_definition'),
          permission: ['PROJECT_API_DEFINITION:READ']
        }, {
          value: 'API_CASE',
          label: this.$t('workstation.table_name.api_case'),
          permission: ['PROJECT_API_SCENARIO:READ']
        }, {
          value: 'API_AUTOMATION',
          label: this.$t('workstation.table_name.api_automation'),
          permission: ['PROJECT_API_SCENARIO:READ']
        }, {
          value: 'PERFORMANCE',
          label: this.$t('workstation.table_name.performance'),
          permission: ['PROJECT_PERFORMANCE_TEST:READ']
        }
      ],
      realTodoList: [],
      condition: {},
      custom_num: false,
      currentProtocol: 'HTTP',
      activeTab: "api",
      apiTabs: [{
        title: this.$t('api_test.definition.api_title'),
        name: 'default',
        type: "list",
        closable: false
      }],
      apiDefaultTab: 'default',
      selectCase: {},
    };
  },
  methods: {
    updateUpcoming(value) {
      sessionStorage.setItem(WORKSTATION.UPCOMING, value);
    },
    refresh() {
      this.toReLoad = false;
      this.$nextTick(function () {
        this.toReLoad = true
      })
    },
    setCondition(data) {
      this.condition = data;
    },
    changeRedirectParam(redirectIDParam) {
      this.redirectID = redirectIDParam;
      if (redirectIDParam != null) {
        if (this.redirectFlag === "none") {
          this.activeName = "default";
          this.addListener();
          this.redirectFlag = "redirected";
        }
      } else {
        this.redirectFlag = "none";
      }
    },
    syncIssues() {
      this.page.result = syncIssues(() => {
        this.getIssues();
      });
    },
    getIssues() {
      this.page.condition.projectId = this.projectId;
      this.page.condition.orders = getLastTableSortField(this.tableHeaderKey);

      this.page.result = getIssues(this.page);
    },
    runTest(data) {
      this.activeTab = "test";
      this.handleTabsEdit(this.$t("commons.api"), "TEST", data);
      this.setTabTitle(data);
    },
    setTabTitle(data) {
      for (let index in this.apiTabs) {
        let tab = this.apiTabs[index];
        if (tab.name === this.apiDefaultTab) {
          tab.title = this.$t('api_test.definition.request.edit_api') + "-" + data.name;
          break;
        }
      }
    },
    handleTabsEdit(targetName, action, api) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      if (targetName === undefined || targetName === null) {
        targetName = this.$t('api_test.definition.request.title');
      }
      let newTabName = getUUID();
      this.apiTabs.push({
        title: targetName,
        name: newTabName,
        closable: true,
        type: action,
        api: api,
      });
      if (action === "ADD") {
        this.activeTab = "api";
      }
      this.apiDefaultTab = newTabName;
    },
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    selectNode() {
      return this.$store.state.testCaseSelectNode;
    },
    queryDataType: function () {
      let routeParam = this.$route.params.dataType;
      let redirectIDParam = this.$route.params.redirectID;
      this.changeRedirectParam(redirectIDParam);
      return routeParam;
    },
  },
  created() {
    for (let i = 0; i < this.todoList.length; i++) {
      let todo = this.todoList[i];
      if (hasPermissions(...todo.permission)) {
        this.realTodoList.push(todo);
      }
    }
    if (sessionStorage.getItem(WORKSTATION.UPCOMING)) {
      this.currentTodo = sessionStorage.getItem(WORKSTATION.UPCOMING);
    } else {
      if (this.realTodoList.length > 0) {
        this.currentTodo = this.realTodoList[0].value
      }
    }
  }
}
</script>
<style scoped>
.top-left-css {
  margin-left: 49px;
  font-weight: 650;
  font-style: normal;
  font-size: 17px;
}

.top-right-css {
  text-align: right;
}

.select-todo {
  width: 33%;
  padding-left: 10px;
}

.el-icon-refresh {
  cursor: pointer;
}

.ms-main-container {
  height: calc(100vh - 200px);
}
</style>
