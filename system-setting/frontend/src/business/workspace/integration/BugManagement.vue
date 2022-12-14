<template>
  <div class="header-title" v-loading="loading">
    <div>
      <div>{{ $t('organization.integration.select_defect_platform') }}</div>
      <el-radio-group v-model="platform" style="margin-top: 10px">
        <span v-for="config in platformConfigs" :key="config.key">
           <el-radio :label="config.label" class="platform-radio">
            <img class="platform" :src="getPlatformImageUrl(config)" :alt="config.label"/>
          </el-radio>
        </span>
        <el-radio label="Tapd" class="platform-radio">
          <img class="platform" src="/assets/tapd.png" alt="Tapd"/>
        </el-radio>
        <el-radio label="AzureDevops" class="platform-radio" v-xpack>
          <img class="platform" src="/assets/AzureDevops.png" alt="AzureDevops"/>
        </el-radio>
      </el-radio-group>
    </div>

    <tapd-setting v-if="tapdEnable" ref="tapdSetting"/>
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
import AzuredevopsSetting from '@/business/workspace/integration/AzureDevopsSetting';
import {AZURE_DEVOPS, TAPD} from "metersphere-frontend/src/utils/constants";
import PlatformConfig from "@/business/workspace/integration/PlatformConfig";
import {generatePlatformResourceUrl, getIntegrationInfo} from "@/api/platform-plugin";

export default {
  name: "BugManagement",
  components: {PlatformConfig, TapdSetting, AzuredevopsSetting},
  data() {
    return {
      loading: false,
      platformConfigs: [],
      platform: TAPD,
    }
  },
  activated() {
    this.platformConfigs = [];

    getIntegrationInfo()
      .then((r) => {
        this.platformConfigs = r.data;
      });

    this.platform = TAPD;
  },
  computed: {
    tapdEnable() {
      return this.platform === TAPD;
    },
    azuredevopsEnable() {
      return this.platform === AZURE_DEVOPS;
    }
  },
  methods: {
    getPlatformImageUrl(config) {
      return generatePlatformResourceUrl(config.id, config.image);
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

.platform-radio {
  margin-left: 20px;
}
</style>
