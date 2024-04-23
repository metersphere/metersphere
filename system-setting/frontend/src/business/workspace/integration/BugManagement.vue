<template>
  <div class="header-title" v-loading="loading">
    <div>
      <div>
        {{ $t('organization.integration.select_defect_platform') }}
      </div>

      <el-form style=" margin-left:-20px;">
        <el-radio-group v-model="platform" style="margin-top: 10px;width: 100%">
          <table>
            <tr>
              <td>
                <el-radio class="platform-radio" label="Tapd">
                  <img alt="Tapd" class="platform" src="/assets/tapd.png"/>
                </el-radio>
              </td>
              <td>
                <el-radio class="platform-radio" label="AzureDevops">
                  <img alt="AzureDevops" class="platform" src="/assets/AzureDevops.png"/>
                </el-radio>
              </td>
              <td v-for="config in firstRowPlatformConfigImages" :key="config.key">
                  <span>
                     <el-radio :label="config.label" class="platform-radio">
                      <img :alt="config.label" :src="getPlatformImageUrl(config)" class="platform"
                           style="width: 150px;height: auto"/>
                    </el-radio>
                  </span>
              </td>
            </tr>
            <tr v-for="(row, index) in otherPlatformConfigImages" :key="index">
              <td v-for="config in row" :key="config.key">
                  <span>
                     <el-radio :label="config.label" class="platform-radio">
                      <img :alt="config.label" :src="getPlatformImageUrl(config)" class="platform"
                           style="width: 150px;height: auto"/>
                    </el-radio>
                  </span>
              </td>
            </tr>
          </table>
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
      firstRowPlatformConfigImages: [],
      otherPlatformConfigImages: [],
      platform: TAPD,
    }
  },
  activated() {
    this.platformConfigs = [];
    this.firstRowPlatformConfigImages = [];
    this.otherPlatformConfigImages = [];
    getIntegrationInfo()
      .then((r) => {
        for (let i = 0; i < r.data.length; i++) {
          this.platformConfigs.push(r.data[i]);
          if (i < 2) {
            this.firstRowPlatformConfigImages.push(r.data[i]);
          } else {
            let pageNum = Math.floor((i - 2) / 4);
            if (!this.otherPlatformConfigImages[pageNum]) {
              this.otherPlatformConfigImages.push([]);
            }
            this.otherPlatformConfigImages[pageNum].push(r.data[i]);
          }
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
