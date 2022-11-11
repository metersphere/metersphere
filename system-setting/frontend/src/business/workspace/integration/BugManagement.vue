<template>
  <div class="header-title" v-loading="loading">
    <div>
      <div>{{ $t('organization.integration.select_defect_platform') }}</div>
      <el-radio-group v-model="platform" style="margin-top: 10px">
        <span v-for="config in platformConfigs" :key="config.key">
           <el-radio :label="config.label">
            <img class="platform" :src="'/platform/plugin/resource/' + config.id + '?fileName=' + config.image"
                 alt="Jira"/>
          </el-radio>
        </span>
        <el-radio label="Tapd">
          <img class="platform" src="/assets/tapd.png" alt="Tapd"/>
        </el-radio>
        <el-radio label="Zentao">
          <img class="zentao_platform" src="/assets/zentao.jpg" alt="Zentao"/>
        </el-radio>
        <el-radio label="AzureDevops" v-xpack>
          <img class="platform" src="/assets/AzureDevops.png" alt="AzureDevops"/>
        </el-radio>
      </el-radio-group>
    </div>

    <tapd-setting v-if="tapdEnable" ref="tapdSetting"/>
    <zentao-setting v-if="zentaoEnable" ref="zentaoSetting"/>
    <azuredevops-setting v-if="azuredevopsEnable" ref="azureDevopsSetting"/>

    <div v-for="config in platformConfigs" :key="config.key">
      <platform-config
        :config="config"
        v-if="config.key === platform"
      />
    </div>
  </div>
</template>

<script>
import TapdSetting from '@/business/workspace/integration/TapdSetting';
import JiraSetting from '@/business/workspace/integration/JiraSetting';
import ZentaoSetting from '@/business/workspace/integration/ZentaoSetting';
import AzuredevopsSetting from '@/business/workspace/integration/AzureDevopsSetting';
import {AZURE_DEVOPS, TAPD, ZEN_TAO} from "metersphere-frontend/src/utils/constants";
import PlatformConfig from "@/business/workspace/integration/PlatformConfig";
import {getIntegrationInfo} from "@/api/platform-plugin";

export default {
  name: "BugManagement",
  components: {PlatformConfig, TapdSetting, JiraSetting, ZentaoSetting, AzuredevopsSetting},
  data() {
    return {
      loading: false,
      platformConfigs: [],
      platform: TAPD,
    }
  },
  created() {
    this.platformConfigs = [];

    getIntegrationInfo()
      .then((r) => {
        this.platformConfigs = r.data;
      });

    this.platform = TAPD;
    this.platformConfigs[0].key;
  },
  computed: {
    tapdEnable() {
      return this.platform === TAPD;
    },
    zentaoEnable() {
      return this.platform === ZEN_TAO;
    },
    azuredevopsEnable() {
      return this.platform === AZURE_DEVOPS;
    },
  },
  methods: {}
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
