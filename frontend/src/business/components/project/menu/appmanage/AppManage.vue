<template>
  <ms-container>
    <ms-main-container>
      <div v-loading="result.loading">
        <el-card class="card">
          <el-col :span="24" justify="space-around">
            <el-tabs v-model="activeName" @tab-click="handleClick" style="height: 600px">
              <el-tab-pane :label="$t('commons.my_workstation')" name="my_workstation" :disabled="true">
                {{ this.$t('commons.my_workstation') }}
              </el-tab-pane>
              <el-tab-pane :label="$t('test_track.test_track')" name="test_track">
                <div>
                  <el-col :span="8">
                    <el-row style="margin-top: 10px">
                      <span style="font-weight:bold">{{ this.$t('commons.enable_settings') }}</span>
                    </el-row>
                    <el-row style="margin-top: 15px">
                      <div style="width: 550px" class="divBorder" :model="form">
                        <div v-if="isXpack">
                          <span style="margin-left: 10px; margin-top: 5px; display: block">{{
                              this.$t('project.public')
                            }}</span>
                          <span class="spanCss">{{ this.$t('project.public_info') }}</span>
                          <el-switch v-model="form.casePublic"
                                     style="margin-left: 500px ; margin-top: -60px"></el-switch>
                          <el-divider></el-divider>
                        </div>
                        <span style="margin-left: 10px; margin-top: 5px; display: block">{{
                            this.$t('project.test_case_custom_id')
                          }}</span>
                        <span class="spanCss">{{ this.$t('project.test_case_custom_id_info') }}</span>
                        <el-switch v-model="form.customNum" style="margin-left: 500px ; margin-top: -60px"></el-switch>
                      </div>
                    </el-row>
                  </el-col>
                </div>
              </el-tab-pane>
              <el-tab-pane :label="$t('commons.api')" name="api">
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-row style="margin-top: 10px">
                      <span style="font-weight:bold">{{ this.$t('commons.enable_settings') }}</span>
                    </el-row>
                    <el-row style="margin-top: 15px">
                      <div style="width: 550px" class="divBorder" :model="form">
                        <span style="margin-left: 10px ; margin-top: 10px; display: block;">{{
                            this.$t('project.repeatable')
                          }}</span>
                        <span class="spanCss">{{ this.$t('project.repeatable_info') }}</span>
                        <el-switch v-model="form.repeatable" style="margin-left: 500px ; margin-top: -60px"></el-switch>
                        <el-divider></el-divider>
                        <span style="margin-left: 10px">{{ this.$t('project.scenario_custom_id') }}</span>
                        <span class="spanCss">{{ this.$t('project.scenario_custom_id_info') }}</span>
                        <el-switch v-model="form.scenarioCustomNum"
                                   style="margin-left: 500px ; margin-top: -60px"></el-switch>
                        <el-divider></el-divider>
                        <span style="margin-left: 10px">{{ 'TCP Mock Port' }}</span>
                        <el-input-number v-model="form.mockTcpPort" :controls="false"
                                         style="width: 37%;margin-left: 100px; margin-top: 5px"></el-input-number>
                        <el-switch v-model="form.isMockTcpOpen" @change="chengeMockTcpSwitch"
                                   style="margin-left: 500px;margin-top: -60px "></el-switch>
                      </div>
                    </el-row>
                  </el-col>
                  <el-col :span="8" :offset="4">
                    <el-row style="margin-top: 10px">
                      <span style="font-weight:bold">{{ this.$t('commons.view_settings') }}</span>
                    </el-row>
                    <el-row style="margin-top: 15px">
                      <div style="width: 550px" class="divBorder" :model="form">
                        <span style="margin-left: 10px;margin-top: 10px; display: block;">{{
                            this.$t('api_test.definition.api_quick_button')
                          }}</span>
                        <el-radio-group v-model="form.apiQuick" style="margin-left: 300px; margin-top: -40px">
                          <el-radio label="debug" value="debug">
                            {{ this.$t('api_test.definition.request.fast_debug') }}
                          </el-radio>
                          <el-radio label="api" value="api">
                            {{ this.$t('api_test.definition.request.title') }}
                          </el-radio>
                        </el-radio-group>
                      </div>
                    </el-row>
                  </el-col>
                </el-row>
              </el-tab-pane>

              <el-tab-pane :label="$t('commons.performance')" name="performance" :disabled="true">{{
                  this.$t('commons.performance')
                }}
              </el-tab-pane>
            </el-tabs>
          </el-col>
        </el-card>
      </div>
    </ms-main-container>
  </ms-container>
</template>


<script>

import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";

import {
  getCurrentProjectID,
  getCurrentUser,
  getCurrentUserId,
  getCurrentWorkspaceId,
  getUUID, hasLicense,
  hasPermission
} from "@/common/js/utils";

export default {
  name: "appManage",
  components: {
    MsMainContainer,
    MsContainer
  },
  data() {
    return {
      activeName: 'test_track',
      form: {},
      labelWidth: '400px',
      count: 0,
      isXpack: false,
      result: {}
    };
  },
  created() {
    this.result = this.$get('/project/get/' + this.projectId, res => {
      this.form = res.data;
      this.count = 0
    })
    if (hasLicense()) {
      this.isXpack = true;
    } else {
      this.isXpack = false;
    }

  },
  watch: {
    form: {
      handler(val, oldVal) {
        this.count++;
        if (this.count > 1) {
          this.submitForm();
        }
      },
      deep: true
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    handleClick() {

    },
    submitForm() {
      this.form.workspaceId = getCurrentWorkspaceId();
      this.form.createUser = getCurrentUserId();
      this.form.id = getCurrentProjectID();
      this.result = this.$post("/project/update", this.form, () => {
        this.$success(this.$t('commons.save_success'));
      });
    },
    chengeMockTcpSwitch(value) {
      if (value && this.form.mockTcpPort === 0) {
        this.result = this.$get('/project/genTcpMockPort/' + this.form.id, res => {
          let port = res.data;
          this.form.mockTcpPort = port;
        })
      }
    }

  }
};
</script>

<style scoped>

/deep/ .el-form-item__label {
  white-space: pre-line;
}

.divBorder {
  border: 1px solid #DCDFE6;
}

.spanCss {
  display: block;
  color: #929295;
  margin-left: 10px;
  font-size: 13px
}

.el-divider--horizontal {
  margin: 0 0 5px 0;
}
</style>
