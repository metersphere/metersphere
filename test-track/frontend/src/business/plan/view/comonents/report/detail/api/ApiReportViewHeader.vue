<template>
  <header class="report-header">
    <el-row>
      <el-col>
        <span v-if="!debug">
          <span>
            <span v-if="isTemplate">
              {{ report.name }}
            </span>
             <el-link v-else-if="isSingleScenario"
                      type="primary"
                      class="report-name"
                      @click="redirect">
              {{ report.name }}
            </el-link>
            <span v-else>
              {{ report.name }}
            </span>
            <i v-if="showCancelButton" class="el-icon-edit" style="cursor:pointer" @click="nameIsEdit = true"
               @click.stop/>
          </span>
        </span>
        <span v-if="report.endTime || report.createTime">
          <span style="margin-left: 10px">{{ $t('report.test_start_time') }}：</span>
          <span class="time"> {{ report.createTime | datetimeFormat }}</span>
          <span style="margin-left: 10px">{{ $t('report.test_end_time') }}：</span>
          <span class="time"> {{ report.endTime | datetimeFormat }}</span>
        </span>
      </el-col>
    </el-row>
    <el-row type="flex" style="margin-top: 5px">
      <el-col v-if="this.poolName">
        <div style="float: left">
          <span> {{ $t('load_test.select_resource_pool') + ':' }} </span>
        </div>
        <div style="color: #61c550; margin-left: 10px; float: left">
          {{ this.poolName }}
        </div>
      </el-col>
      <el-col></el-col>
    </el-row>
    <el-row v-if="showProjectEnv" type="flex" style="margin-top: 5px">
      <span> {{ $t('commons.environment') + ':' }} </span>
      <div v-for="(values,key) in projectEnvMap" :key="key" style="margin-right: 10px">
        {{ key + ":" }}
        <ms-tag v-for="(item,index) in values" :key="index" type="success" :content="item"
                style="margin-left: 2px"/>
      </div>
    </el-row>
  </header>
</template>

<script>

import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {getUUID} from "metersphere-frontend/src/utils";

export default {
  name: "MsApiReportViewHeader",
  components: {MsTag},
  props: {
    report: {},
    projectEnvMap: {},
    debug: Boolean,
    showCancelButton: {
      type: Boolean,
      default: true,
    },
    showRerunButton: {
      type: Boolean,
      default: false,
    },
    isTemplate: Boolean,
    exportFlag: {
      type: Boolean,
      default: false,
    },
    poolName: String,
    isPlan: Boolean
  },
  computed: {
    showProjectEnv() {
      return this.projectEnvMap && JSON.stringify(this.projectEnvMap) !== '{}';
    },
    path() {
      return "/api/test/edit?id=" + this.report.testId;
    },
    scenarioId() {
      if (typeof this.report.scenarioId === 'string') {
        return this.report.scenarioId;
      } else {
        return "";
      }
    },
    isSingleScenario() {
      try {
        JSON.parse(this.report.scenarioId);
        return false;
      } catch (e) {
        return true;
      }
    },
  },
  data() {
    return {
      isReadOnly: false,
      nameIsEdit: false,
      shareUrl: "",
      application: {}
    }
  },
  methods: {
    handleSaveKeyUp($event) {
      $event.target.blur();
    },
    redirect() {
      let uuid = getUUID().substring(1, 5);
      let projectId = getCurrentProjectID();
      let workspaceId = getCurrentWorkspaceId();
      let path = `/api/automation/?redirectID=${uuid}&dataType=scenario&projectId=${projectId}&workspaceId=${workspaceId}&resourceId=${this.scenarioId}`;
      let data = this.$router.resolve({
        path: path
      });
      window.open(data.href, '_blank');
    },
    returnView() {
      this.$router.push('/api/automation/report');
    },
  }
}
</script>

<style scoped>

.export-button {
  float: right;
  margin-right: 10px;
}

.rerun-button {
  float: right;
  margin-right: 10px;
  background-color: #F2F9EF;
  color: #87C45D;
}

.report-name {
  border-bottom: 1px solid var(--primary_color);
}

.report-header {
  min-width: 1000px;
}
</style>
