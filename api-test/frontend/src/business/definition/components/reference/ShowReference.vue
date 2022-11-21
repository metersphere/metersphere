<template>
  <el-dialog :visible.sync="isVisible" class="advanced-item-value" width="50%">
    <el-tabs tab-position="top" style="width: 100%" v-model="activeName" @tab-click="handleClick">
      <el-tab-pane :label="$t('api_test.automation.scenario_ref')" name="scenario">
        <ms-table
          :data="scenarioData"
          style="width: 100%"
          :screen-height="screenHeight"
          :total="total"
          :page-size="pageSize"
          :enable-selection="false"
          @refresh="search"
          :condition="condition">
          <ms-table-column prop="num" label="ID" sortable width="80" />
          <ms-table-column prop="name" :label="$t('api_report.scenario_name')" width="200">
            <template v-slot:default="{ row }">
              <el-link @click="openScenario(row)" style="cursor: pointer">{{ row.name }} </el-link>
            </template>
          </ms-table-column>
          <ms-table-column
            prop="workspaceName"
            :label="$t('group.belong_workspace')"
            width="200"
            column-key="workspaceId"
            :filters="workspaceFilters">
          </ms-table-column>
          <ms-table-column
            prop="projectName"
            :label="$t('group.belong_project')"
            :filters="projectFilters"
            column-key="projectId"
            width="200">
          </ms-table-column>
        </ms-table>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_test.automation.plan_ref')" name="testPlan">
        <ms-table
          :data="planData"
          style="width: 100%"
          :screen-height="screenHeight"
          :total="total"
          :page-size="pageSize"
          :enable-selection="false"
          @refresh="search"
          :condition="condition">
          <ms-table-column prop="name" :label="$t('test_track.home.test_plan_name')" width="200" sortable>
            <template v-slot:default="{ row }">
              <el-link @click="openTestPlan(row)" style="cursor: pointer">{{ row.name }} </el-link>
            </template>
          </ms-table-column>
          <ms-table-column
            prop="workspaceName"
            :label="$t('group.belong_workspace')"
            width="200"
            column-key="workspaceId">
          </ms-table-column>
          <ms-table-column
            prop="projectName"
            :label="$t('group.belong_project')"
            :filters="projectPlanFilters"
            column-key="projectId"
            width="200">
          </ms-table-column>
        </ms-table>
      </el-tab-pane>
    </el-tabs>
    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total" />
  </el-dialog>
</template>
<script>
import MsTablePagination from 'metersphere-frontend/src/components/pagination/TablePagination';
import { apiProjectRelated, getOwnerProjectIds, getProject, getUserWorkspace, projectRelated } from '@/api/project';
import { getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId } from 'metersphere-frontend/src/utils/token';
import { getUUID } from 'metersphere-frontend/src/utils';
import { getDefinitionReference, getPlanReference } from '@/api/definition';
import MsTable from 'metersphere-frontend/src/components/table/MsTable';
import MsTableColumn from 'metersphere-frontend/src/components/table/MsTableColumn';

export default {
  name: 'ShowReference',
  data() {
    return {
      isVisible: false,
      scenarioData: [],
      planData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      activeName: 'scenario',
      scenarioId: '',
      workspaceList: [],
      workspaceFilters: [],
      projectFilters: [],
      projectList: [],
      screenHeight: 'calc(100vh - 400px)',
      condition: {},
      type: '',
      projectPlanFilters: [],
    };
  },
  components: {
    MsTablePagination,
    MsTable,
    MsTableColumn,
  },
  watch: {
    activeName(o) {
      if (o) {
        this.init();
        this.search();
      }
    },
  },
  methods: {
    getWorkSpaceList() {
      getUserWorkspace().then((response) => {
        this.workspaceList = response.data;
      });
    },
    getUserProjectList() {
      projectRelated({
        userId: getCurrentUserId(),
        workspaceId: getCurrentWorkspaceId(),
      }).then((res) => {
        this.projectList = res.data ? res.data : [];
      });
    },
    /**
     * 操作方法
     */
    init() {
      this.currentPage = 1;
      this.pageSize = 10;
      this.total = 0;
      this.condition = {};
      this.scenarioData = [];
      this.planData = [];
    },
    open(row, type) {
      this.activeName = 'scenario';
      this.init();
      this.getUserProjectList();
      this.getWorkSpaceList();
      this.isVisible = true;
      this.scenarioId = row.id;
      this.type = type;
      this.search(row);
    },
    close() {
      this.isVisible = false;
    },
    search(row) {
      this.condition.id = this.scenarioId;
      if (row) {
        this.condition.id = row.id;
        this.condition.projectId = row.projectId;
        this.condition.workspaceId = row.workspaceId;
      }
      this.condition.workspaceId = getCurrentWorkspaceId();
      this.condition.scenarioType = this.type;
      if (this.activeName === 'scenario') {
        getDefinitionReference(this.currentPage, this.pageSize, this.condition).then((res) => {
          let data = res.data || [];
          this.total = data.itemCount || 0;
          if (this.workspaceList) {
            this.workspaceFilters = this.workspaceList
              .filter((workspace) => {
                return data.listObject.find((i) => i.workspaceId === workspace.id);
              })
              .map((e) => {
                return { text: e.name, value: e.id };
              });
            let workspaceIds = [];
            this.workspaceFilters.map((item) => {
              workspaceIds.push(item.value);
            });
            apiProjectRelated({
              userId: getCurrentUserId(),
              workspaceIds: workspaceIds,
            }).then((res) => {
              this.projectFilters = res.data
                .filter((project) => {
                  return data.listObject.find((i) => i.projectId === project.id);
                })
                .map((e) => {
                  return { text: e.name, value: e.id };
                });
            });
          }

          this.scenarioData = data.listObject || [];
        });
      } else {
        getPlanReference(this.currentPage, this.pageSize, this.condition).then((res) => {
          let data = res.data || [];
          this.total = data.itemCount || 0;
          this.projectPlanFilters = this.projectList
            .filter((project) => {
              return data.listObject.find((i) => i.projectId === project.id);
            })
            .map((e) => {
              return { text: e.name, value: e.id };
            });
          this.planData = data.listObject || [];
        });
      }
    },
    handleClick(tab, event) {
      //
    },
    openScenario(resource) {
      if (this.type === 'API') {
        let workspaceId = resource.workspaceId;
        let isTurnSpace = true;
        if (resource.projectId !== getCurrentProjectID()) {
          isTurnSpace = false;
          getProject(resource.projectId).then((response) => {
            if (response.data) {
              workspaceId = response.data.workspaceId;
              isTurnSpace = true;
              this.checkPermission(resource, workspaceId, isTurnSpace);
            }
          });
        } else {
          this.checkPermission(resource, workspaceId, isTurnSpace);
        }
      } else {
        this.$emit('openScenario', resource);
        this.isVisible = false;
      }
    },
    gotoTurn(resource, workspaceId, isTurnSpace) {
      let automationData = this.$router.resolve({
        name: 'ApiAutomationWithQuery',
        params: {
          redirectID: getUUID(),
          dataType: 'scenario',
          dataSelectRange: 'edit:' + resource.id,
          projectId: resource.projectId,
          workspaceId: workspaceId,
        },
      });
      if (isTurnSpace) {
        window.open(automationData.href, '_blank');
      }
    },
    checkPermission(resource, workspaceId, isTurnSpace) {
      getOwnerProjectIds().then((res) => {
        const project = res.data.find((p) => p === resource.projectId);
        if (!project) {
          this.$warning(this.$t('commons.no_permission'));
        } else {
          this.gotoTurn(resource, workspaceId, isTurnSpace);
        }
      });
    },
    openTestPlan(item) {
      let automationData = this.$router.resolve({
        path: '/track/plan/view/' + item.id,
        query: {
          workspaceId: item.workspaceId,
          projectId: item.projectId,
          charType: this.type === 'API' ? 'api' : 'scenario',
        },
      });
      window.open(automationData.href, '_blank');
    },
  },
};
</script>
<style scoped>
:deep(.el-table__empty-block) {
  padding-right: 0 !important;
}

:deep(.el-dialog__body) {
  padding: 0 20px 30px 20px;
}
</style>
