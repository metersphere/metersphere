<template>
  <div class="header-title" v-loading="result.loading">
    <div>
      <div>{{ $t('organization.integration.select_defect_platform') }}</div>
      <el-radio-group v-model="platform" style="margin-top: 10px" @change="change">
        <el-radio label="Tapd">
          <img class="platform" src="../../../../assets/tapd.png" alt="Tapd"/>
        </el-radio>
        <el-radio label="Jira">
          <img class="platform" src="../../../../assets/jira.png" alt="Jira"/>
        </el-radio>
        <el-radio label="Zentao">
          <img class="zentao_platform" src="../../../../assets/zentao.jpg" alt="Zentao"/>
        </el-radio>
        <el-radio label="AzureDevops" v-xpack>
          <img class="platform" src="../../../../assets/AzureDevops.png" alt="AzureDevops"/>
        </el-radio>
      </el-radio-group>
    </div>

    <tapd-setting v-if="tapdEnable" ref="tapdSetting"/>
    <jira-setting v-if="jiraEnable" ref="jiraSetting"/>
    <zentao-setting v-if="zentaoEnable" ref="zentaoSetting"/>
    <azuredevops-setting v-if="azuredevopsEnable" ref="azureDevopsSetting"/>
  </div>
</template>

<script>
import TapdSetting from "@/business/components/settings/workspace/components/TapdSetting";
import JiraSetting from "@/business/components/settings/workspace/components/JiraSetting";
import ZentaoSetting from "@/business/components/settings/workspace/components/ZentaoSetting";
import AzuredevopsSetting from "@/business/components/settings/workspace/components/AzureDevopsSetting";
import {JIRA, TAPD, ZEN_TAO, AZURE_DEVOPS} from "@/common/js/constants";

export default {
  name: "BugManagement",
  components: {TapdSetting, JiraSetting, ZentaoSetting, AzuredevopsSetting},
  data() {
    return {
      tapdEnable: true,
      jiraEnable: false,
      zentaoEnable: false,
      azuredevopsEnable:false,
      result: {},
      platform: TAPD
    }
  },
  methods: {
    change(platform) {
      if (platform === TAPD) {
        this.tapdEnable = true;
        this.jiraEnable = false;
        this.zentaoEnable = false;
        this.azuredevopsEnable = false;
      } else if (platform === JIRA) {
        this.tapdEnable = false;
        this.jiraEnable = true;
        this.zentaoEnable = false;
        this.azuredevopsEnable = false;
      } else if (platform === ZEN_TAO) {
        this.tapdEnable = false;
        this.jiraEnable = false;
        this.zentaoEnable = true;
        this.azuredevopsEnable = false;
      } else if (platform === AZURE_DEVOPS) {
        this.tapdEnable = false;
        this.jiraEnable = false;
        this.zentaoEnable = false;
        this.azuredevopsEnable = true;
      }
    }
  }
}
</script>

<style scoped>
.header-title {
  padding: 10px 30px;
}

.platform {
  height: 80px;
  vertical-align: middle
}

.zentao_platform {
  height: 100px;
  vertical-align: middle
}
</style>
