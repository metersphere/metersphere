<template>
  <div class="header-title" v-loading="loading">
    <div>
      <div>
        {{ $t('organization.integration.select_defect_platform') }}
      </div>

      <el-form class="platform-radio">
        <el-radio-group v-model="platform" style="margin-top: 10px;width: 100%">
          <el-row>
            <el-col :gutter="20" :span="4">
              <el-radio class="platform-radio" label="Tapd">
                <img alt="Tapd" class="platform" src="/assets/tapd.png"/>
              </el-radio>
            </el-col>
            <el-col :gutter="20" :span="4">
              <el-radio class="platform-radio" label="AzureDevops">
                <img alt="AzureDevops" class="platform" src="/assets/AzureDevops.png"/>
              </el-radio>
            </el-col>
            <el-col :gutter="20" :span="16">
            </el-col>
          </el-row>
          <el-row v-for="(row, index) in platformConfigs" :key="index">
            <el-col v-for="config in row" :key="config.key" :gutter="20" :span="4">
            <span>
             <el-radio :label="config.label" class="platform-radio">
              <img :alt="config.label" :src="getPlatformImageUrl(config)" class="platform"/>
            </el-radio>
          </span>
            </el-col>
          </el-row>
        </el-radio-group>
      </el-form>

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
        for (let i = 0; i < r.data.length; i++) {
          let pageNum = Math.floor(i / 5);
          if (!this.platformConfigs[pageNum]) {
            this.platformConfigs.push([]);
          }
          this.platformConfigs[pageNum].push(r.data[i]);
        }
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
  height: 60px;
  vertical-align: middle
}

.platform-radio {
  margin-left: 20px;
}
</style>
