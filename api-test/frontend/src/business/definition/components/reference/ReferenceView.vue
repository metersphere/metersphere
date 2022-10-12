<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.automation.case_ref')" :visible.sync="visible"
             :modal="false" width="45%" :destroy-on-close="true">
    <span>{{ $t('api_test.automation.scenario_ref') }}：</span>
    <div class="refs" v-loading="scenarioLoading">
      <div v-for="(item, index) in scenarioRefs" :key="index" class="el-button--text">
        <el-link @click="openScenario(item)">
          {{ item.name }}
        </el-link>
      </div>
    </div>

    <span>{{ $t('api_test.automation.plan_ref') }}：</span>
    <div class="refs">
      <div v-for="(item, index) in planRefs" :key="index" class="el-button--text">
        <el-link @click="openTestPlan(item)">
          {{ item.name }}
        </el-link>
      </div>
    </div>

    <template v-slot:footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="visible = false" @keydown.enter.native.prevent>
          {{ $t('commons.confirm') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>

import {getDefinitionReference} from "@/api/definition";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {getUUID} from "metersphere-frontend/src/utils";
import {getOwnerProjectIds, getProject} from "@/api/project";

export default {
  name: "MsReferenceView",
  components: {},
  data() {
    return {
      visible: false,
      scenarioLoading: false,
      scenarioRefs: [],
      planRefs: []
    }
  },
  methods: {
    getReferenceData(row) {
      if (row.id === undefined) {
        return;
      }
      this.scenarioLoading = true;
      this.scenarioRefs = [];
      getDefinitionReference(row).then(response => {
        this.scenarioRefs = response.data.scenarioList;
        this.planRefs = response.data.testPlanList;
        this.scenarioLoading = false;
      })
    },
    open(row) {
      this.getReferenceData(row);
      this.visible = true
    },
    openScenario(resource) {
      let workspaceId = getCurrentWorkspaceId();
      let isTurnSpace = true
      if (resource.projectId !== getCurrentProjectID()) {
        isTurnSpace = false;
        getProject(resource.projectId).then(response => {
          if (response.data) {
            workspaceId = response.data.workspaceId;
            isTurnSpace = true;
            this.checkPermission(resource, workspaceId, isTurnSpace);
          }
        });
      } else {
        this.checkPermission(resource, workspaceId, isTurnSpace);
      }

    },
    gotoTurn(resource, workspaceId, isTurnSpace) {
      let automationData = this.$router.resolve({
        name: 'ApiAutomationWithQuery',
        params: {
          redirectID: getUUID(),
          dataType: "scenario",
          dataSelectRange: 'edit:' + resource.id,
          projectId: resource.projectId,
          workspaceId: workspaceId
        }
      });
      if (isTurnSpace) {
        window.open(automationData.href, '_blank');
      }
    },
    checkPermission(resource, workspaceId, isTurnSpace) {
      getOwnerProjectIds().then(res => {
        const project = res.data.find(p => p === resource.projectId);
        if (!project) {
          this.$warning(this.$t('commons.no_permission'));
        } else {
          this.gotoTurn(resource, workspaceId, isTurnSpace)
        }
      })
    },
    openTestPlan(item) {
      let automationData = this.$router.resolve({
        path: '/track/plan/view/' + item.id,
        query: {workspaceId: item.workspaceId, projectId: item.projectId, charType: 'api'}
      });
      window.open(automationData.href, '_blank');
    }
  }
}
</script>

<style scoped>
.refs {
  min-height: 50px;
  max-height: 200px;
  overflow-y: auto;
  font-size: 12px;
  padding-bottom: 10px;
}
</style>
