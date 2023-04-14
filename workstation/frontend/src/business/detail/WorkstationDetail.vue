<template>
  <ms-container>
    <ms-aside-container>
      <project-menu
        ref="projectMenu"
        @setProject="setProject"
        @setCurrentProtocol="setCurrentProtocol"
        :current-todo="currentTodo"
      >
      </project-menu>
    </ms-aside-container>

    <ms-main-container>
      <el-card>
        <el-row>
          <table-header
            ref="tableHeader"
            :select-show=!isFocus
            @rushTableNode="rushTableNode"
          >
          </table-header>
        </el-row>


        <el-row>
          <template
            v-if="currentTodo === 'performance'||((currentTodo === 'api_automation'||currentTodo === 'track_case'||currentTodo === 'api_case')&& isUpcoming ===false)">
            <mx-version-select v-xpack :project-id="projectId" @changeVersion="changeVersion"/>
          </template>
          <ms-tab-button
            v-if="currentTodo ==='track_case' && isUpcoming ===true"
            :active-dom="activeDom"
            @update:activeDom="updateActiveDom"
            :left-tip="$t('test_track.case.list')"
            :left-content="$t('test_track.case.list')"
            :right-tip="$t('commons.dash_board')"
            :right-content="$t('commons.dash_board')"
            :middle-button-enable="false">
            <template v-slot:version>
              <mx-version-select v-xpack :project-id="projectId" @changeVersion="changeVersion"/>
            </template>
            <div v-if="activeDom === 'left'" style="padding-top: 20px">
              <case-table-list
                v-if="currentTodo ==='track_case'"
                @refresh="refresh"
                @setCondition="setCondition"
                :is-focus=isFocus
                :is-creation=isCreation
                :custom-num="custom_num"
                :current-version="currentVersion"
                ref="caseTableList">
              </case-table-list>
            </div>
          </ms-tab-button>
          <case-table-list
            v-if="currentTodo ==='track_case'&& isUpcoming===false"
            @refresh="refresh"
            @setCondition="setCondition"
            :is-focus=isFocus
            :is-creation=isCreation
            :current-version="currentVersion"
            :custom-num="custom_num"
            ref="caseTableList">
          </case-table-list>
          <status-tap-button
            v-if="(currentTodo ==='api_automation'||currentTodo ==='api_case'||currentTodo === 'api_definition') && isUpcoming===true"
            :active-status="activeStatus"
            @update:activeStatus="updateActiveStatus"
            :left-tip="$t('commons.to_be_completed')"
            :left-content="$t('commons.to_be_completed')"
            :right-tip="$t('commons.pending_upgrade')"
            :right-content="$t('commons.pending_upgrade')"
            :middle-button-enable="false">
            <template v-slot:version>
              <mx-version-select v-xpack :project-id="projectId" @changeVersion="changeVersion"/>
            </template>
            <div style="padding-top: 20px">
              <api-case-table-list
                v-if="currentTodo === 'api_case'"
                :is-focus=isFocus
                :is-creation=isCreation
                :is-upcoming=isUpcoming
                :relevanceProjectId=projectId
                :current-version="currentVersion"
                :current-protocol="currentProtocol"
                ref="apiCaseTableList"
              >
              </api-case-table-list>
              <automation-table-list
                v-if="currentTodo === 'api_automation'"
                :is-focus=isFocus
                :is-creation=isCreation
                :current-version="currentVersion"
                ref="automationTableList">
              </automation-table-list>
              <api-definition-table-list
                v-if="currentTodo === 'api_definition'"
                @runTest="runTest"
                :current-protocol="currentProtocol"
                :queryDataType="queryDataType"
                :current-version="currentVersion"
                :is-focus=isFocus
                :is-creation=isCreation
                :is-upcoming=isUpcoming
                ref="apiDefinitionTableList"
              >
              </api-definition-table-list>
            </div>
          </status-tap-button>
          <api-case-table-list
            v-if="currentTodo ==='api_case'&& isUpcoming===false"
            :is-focus=isFocus
            :is-creation=isCreation
            :is-upcoming=isUpcoming
            :relevanceProjectId=projectId
            :current-version="currentVersion"
            :current-protocol="currentProtocol"
            ref="apiCaseTableList"
          >
          </api-case-table-list>
          <automation-table-list
            v-if="currentTodo ==='api_automation'&& isUpcoming===false"
            :is-focus=isFocus
            :is-creation=isCreation
            :current-version="currentVersion"
            ref="automationTableList">
          </automation-table-list>
          <plan-table-list
            v-if="currentTodo === 'track_plan'"
            :is-focus=isFocus
            :is-creation=isCreation
            ref="testPlanList">
          </plan-table-list>
          <review-table-list
            v-if="currentTodo === 'track_review'"
            :is-focus=isFocus
            :is-creation=isCreation
            ref="testPlanList">
          </review-table-list>
          <issue-table-list
            v-if="currentTodo === 'track_issue'"
            @handlePageChange="getIssues"
            :is-focus=isFocus
            :is-creation=isCreation
            @refresh="getIssues"
            ref="issueTableList"
          >
          </issue-table-list>
          <api-definition-table-list
            v-if="currentTodo === 'api_definition' && isUpcoming===false"
            @runTest="runTest"
            :current-protocol="currentProtocol"
            :queryDataType="queryDataType"
            :current-version="currentVersion"
            :is-read-only="false"
            :is-focus=isFocus
            :is-creation=isCreation
            :is-upcoming=isUpcoming
            ref="apiDefinitionTableList"
          >
          </api-definition-table-list>
          <performance-table-list
            v-if="currentTodo === 'performance'"
            :is-focus=isFocus
            :is-creation=isCreation
            :current-version="currentVersion"
            ref="performanceTableList"
          >
          </performance-table-list>

        </el-row>
      </el-card>
    </ms-main-container>
  </ms-container>
</template>
<script>
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import ProjectMenu from "@/business/component/ProjectMenu";
import TableHeader from "@/business/head/TableHeader";
import CaseTableList from "@/business/component/CaseTableList";
import PlanTableList from "@/business/component/PlanTableList";
import ReviewTableList from "@/business/component/ReviewTableList";
import IssueTableList from "@/business/component/IssueTableList";
import ApiDefinitionTableList from "@/business/component/ApiDefinitionTableList";
import PerformanceTableList from "@/business/component/PerformanceTableList";
import AutomationTableList from "@/business/component/AutomationTableList";
import ApiCaseTableList from "@/business/component/ApiCaseTableList";
import {getIssues} from "@/api/issue";
import {
  fullScreenLoading,
  getUUID,
  stopFullScreenLoading
} from "metersphere-frontend/src/utils";
import {
  getCurrentProjectID,
  getCurrentUser,
  getCurrentUserId,
  getCurrentWorkspaceId
} from "metersphere-frontend/src/utils/token";
import {switchProject} from "metersphere-frontend/src/api/project";
import MsTabButton from "metersphere-frontend/src/components/MsTabButton";
import StatusTapButton from "@/business/component/StatusTapButton";
import {useUserStore} from "@/store";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import {hasPermissions} from "metersphere-frontend/src/utils/permission";
import {getDefaultSecondLevelMenu} from "metersphere-frontend/src/router";
import {getUserProjectList} from "@/api/project";
import {PROJECT_NAME} from "metersphere-frontend/src/utils/constants";



export default {
  name: 'WorkstationDetail',
  components: {
    MsMainContainer,
    MsContainer,
    MsAsideContainer,
    ProjectMenu,
    TableHeader,
    CaseTableList,
    PlanTableList,
    ReviewTableList,
    IssueTableList,
    ApiDefinitionTableList,
    PerformanceTableList,
    AutomationTableList,
    ApiCaseTableList,
    MsTabButton,
    StatusTapButton,
    MxVersionSelect
  },
  watch: {
    tableKey() {
      this.getTableData();
    },
    activeStatus() {
      this.changeTableByState();
    },
    currentTodo() {
      this.activeStatus = 'left'
    },
    currentTodoName() {
      this.$nextTick(function () {
        this.$refs.tableHeader.setActiveIndex(this.currentTodoName);
        this.rushTableNode(this.currentTodoName)
      })
    }
  },
  data() {
    return {
      projectId: '',
      tableKey: '',
      currentTodo: 'track_case',
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
      userId: getCurrentUser().id,
      currentProjectId: '',
      activeDom: 'left',
      activeStatus: '',
      currentVersion: null,
      searchArray:[],
      userStore: {}
    }
  },
  inject: [
    'reloadTopMenus'
  ],
  computed: {
    queryDataType: function () {
      let routeParam = this.$route.params.dataType;
      let redirectIDParam = this.$route.params.redirectID;
      this.changeRedirectParam(redirectIDParam);
      return routeParam;
    },
  },
  methods: {
    reloadPage: function () {
      // todo refactor permission check
      let redirectUrl = sessionStorage.getItem('redirectUrl');
      let copyRedirectUrl = redirectUrl;
      if (!copyRedirectUrl) {
        this.$router.push("/");
        this.reload();
        return;
      }
      if (copyRedirectUrl.startsWith("/track") || copyRedirectUrl.startsWith("/performance")
        || copyRedirectUrl.startsWith("/api") || copyRedirectUrl.startsWith("/ui") || copyRedirectUrl.startsWith("/workstation")) {
        // 获取有权限的跳转路径
        copyRedirectUrl = getDefaultSecondLevelMenu(copyRedirectUrl);
        if (copyRedirectUrl !== '/') {
          this.$router.push(copyRedirectUrl);
          this.reloadTopMenus();
          this.reload();
          return;
        }
      }
      // 跳转至下一个有权限的菜单
      let projectPermission = hasPermissions('PROJECT_USER:READ', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE', 'PROJECT_CUSTOM_CODE:READ');
      let uiPermission = hasPermissions('PROJECT_UI_ELEMENT:READ', 'PROJECT_UI_SCENARIO:READ', 'PROJECT_UI_REPORT:READ');
      let redirectMap = {
        project: projectPermission,
        ui: uiPermission,
      };
      let locations = redirectUrl.split('/');
      if (locations.length > 2 && !redirectMap[locations[1]]) {
        let v = true;
        for (const k in redirectMap) {
          if (redirectMap[k]) {
            this.$router.push("/" + k);
            v = false;
            break;
          }
        }
        if (v) {
          this.$router.push("/");
        }
      }
      this.reloadTopMenus();
      this.reload();
    },
    setProject(projectId) {
      this.projectId = projectId;
      let currentProjectId = getCurrentProjectID();
      if (projectId === currentProjectId) {
        return;
      }
      const loading = fullScreenLoading(this);
      switchProject({id: this.userId, lastProjectId: projectId})
        .then(response => {
          this.userStore.switchProject(response);
          this.$EventBus.$emit('projectChange');
          this.changeProjectName(projectId);
          // 刷新路由
          this.reloadPage();
          stopFullScreenLoading(loading);
        })
        .catch(() => {
          stopFullScreenLoading(loading);
        });
    },
    changeProjectName(projectId) {
      if (projectId) {
        let project = this.searchArray.filter(p => p.id === projectId);
        if (project.length > 0) {
          sessionStorage.setItem(PROJECT_NAME, project[0].name);
        }
      }
    },
    rushTableNode(tableKey) {
      this.tableKey = tableKey;
    },
    changeTableByState() {
      if (this.currentTodo === 'api_automation' && this.$refs.automationTableList) {
        if (this.activeStatus === 'left') {
          this.$refs.automationTableList.changeTabState('finish')
        } else {
          this.$refs.automationTableList.changeTabState('update')
        }
      }
      if (this.currentTodo === 'api_case' && this.$refs.apiCaseTableList) {
        if (this.activeStatus === 'left') {
          this.$refs.apiCaseTableList.changeTabState('finish')
        } else {
          this.$refs.apiCaseTableList.changeTabState('update')
        }
      }
      if (this.currentTodo === 'api_definition' && this.$refs.apiDefinitionTableList) {
        if (this.activeStatus === 'left') {
          this.$refs.apiDefinitionTableList.changeTabState("finish")
        } else {
          this.$refs.apiDefinitionTableList.changeTabState("update")
        }
      }
    },
    getTableData() {
      this.currentTodo = this.tableKey
    },
    refresh() {

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

    getIssues() {
      this.page.condition.projectId = this.projectId;
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
    updateActiveDom(activeDom) {
      if (activeDom === 'right') {
        this.$alert(this.$t('commons.function_planning'), this.$t('commons.dash_board'), {
          confirmButtonText: '确定',
          callback: action => {
            /*this.$message({
              type: 'info',
              message: `action: ${ action }`
            });*/
          }
        });
      }
      this.activeDom = activeDom;
    },
    updateActiveStatus(activeStatus) {
      this.activeStatus = activeStatus;
    },
    setCurrentProtocol(protocol) {
      this.currentProtocol = protocol
    },
    changeVersion(currentVersion) {
      this.currentVersion = currentVersion || null;
    },
    init() {
      let data = {
        userId: getCurrentUserId(),
        workspaceId: getCurrentWorkspaceId()
      };
      this.loading = true;
      getUserProjectList(data)
        .then(response => {
          this.searchArray = response.data;
          let projectId = getCurrentProjectID();
          if (projectId) {
            // 保存的 projectId 在当前项目列表是否存在; 切换工作空间后
            if (this.searchArray.length > 0 && this.searchArray.map(p => p.id).indexOf(projectId) === -1) {
              this.change(this.items[0].id);
            }
          } else {
            if (this.items.length > 0) {
              this.change(this.items[0].id);
            }
          }
          this.changeProjectName(projectId);
        });
    },
    setDefaultCurrentTodo() {
      // 设置当前默认TAB页为下一个有权限的菜单TAB
      if (hasPermissions('PROJECT_TRACK_CASE:READ')) {
        this.currentTodo = 'track_case';
      } else if (hasPermissions('PROJECT_TRACK_PLAN:READ')) {
        this.currentTodo = 'track_plan';
      } else if (hasPermissions('PROJECT_TRACK_REVIEW:READ')) {
        this.currentTodo = 'track_review';
      } else if (hasPermissions('PROJECT_TRACK_ISSUE:READ')) {
        this.currentTodo = 'track_issue';
      } else if (hasPermissions('PROJECT_API_DEFINITION:READ')) {
        this.currentTodo = 'api_definition';
      } else if (hasPermissions('PROJECT_API_SCENARIO:READ')) {
        this.currentTodo = 'api_automation';
      } else if (hasPermissions('PROJECT_PERFORMANCE_TEST:READ')) {
        this.currentTodo = 'performance';
      }
    }
  },
  props: {
    isFocus: {
      type: Boolean,
      default: false,
    },
    isCreation: {
      type: Boolean,
      default: false,
    },
    isUpcoming: {
      type: Boolean,
      default: false,
    },
    currentTodoName: {
      type: String,
      default: 'track_case',
    }
  },
  mounted() {
    this.$nextTick(function () {
      this.$refs.tableHeader.setActiveIndex(this.currentTodoName);
      this.rushTableNode(this.currentTodoName)
    })
    this.activeStatus = 'left'
    if (this.isUpcoming === true) {
      if (this.currentTodo === 'api_automation') {
        this.$refs.automationTableList.changeTabState('finish')
      }
      if (this.currentTodo === 'api_case') {
        this.$refs.apiCaseTableList.changeTabState('finish')
      }
      if (this.currentTodo === 'api_definition') {
        this.$refs.apiDefinitionTableList.changeTabState('finish')
      }
    }
  },
  created() {
    this.setDefaultCurrentTodo();
    this.init();
    this.userStore = useUserStore();
  },
}
</script>
<style scoped>
.workstation-card {
  height: 100%;
}

.version-select {
  padding-left: 10px;
}
</style>
