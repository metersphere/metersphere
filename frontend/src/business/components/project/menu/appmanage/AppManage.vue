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
                      <span>{{ this.$t('commons.enable_settings') }}</span>
                    </el-row>
                    <el-row style="margin-top: 10px">
                      <fieldset>
                        <el-form :model="form" ref="form" label-position="left" label-width="200px" size="small">
                          <el-form-item :label-width="labelWidth" :label="$t('project.public_info')" prop="casePublic"
                                        v-if="this.isXpack">
                            <el-switch v-model="form.casePublic" style="margin-top:20px"></el-switch>
                          </el-form-item>
                          <el-divider v-if="this.isXpack"></el-divider>
                          <el-form-item :label-width="labelWidth" :label="$t('project.test_case_custom_id_info')"
                                        prop="repeatable">
                            <el-switch v-model="form.repeatable" style="margin-top:20px"></el-switch>
                          </el-form-item>
                        </el-form>
                      </fieldset>
                    </el-row>
                  </el-col>
                </div>
              </el-tab-pane>
              <el-tab-pane :label="$t('commons.api')" name="api">
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-row style="margin-top: 10px">
                      <span>{{ this.$t('commons.enable_settings') }}</span>
                    </el-row>
                    <el-row style="margin-top: 10px">
                      <fieldset>
                        <el-form :model="form" ref="form" label-position="left" label-width="200px" size="small">
                          <el-form-item :label-width="labelWidth" :label="$t('project.repeatable_info')"
                                        prop="customNum">
                            <el-switch v-model="form.customNum" style="margin-top:20px"></el-switch>
                          </el-form-item>
                          <el-divider></el-divider>
                          <el-form-item :label-width="labelWidth" :label="$t('project.scenario_custom_id_info')"
                                        prop="scenarioCustomNum">
                            <el-switch v-model="form.scenarioCustomNum" style="margin-top:20px"></el-switch>
                          </el-form-item>
                        </el-form>
                      </fieldset>
                    </el-row>
                  </el-col>
                  <el-col :span="8" :offset="4">
                    <el-row style="margin-top: 10px">
                      <span>{{ this.$t('commons.view_settings') }}</span>
                    </el-row>
                    <el-row style="margin-top: 10px">
                      <fieldset>
                        <el-form :model="form" ref="form" label-position="left" label-width="200px" size="small">
                          <el-form-item label-width="200px" :label="$t('api_test.definition.api_quick_button')"
                                        prop="apiQuick">
                            <el-radio-group v-model="form.apiQuick">
                              <el-radio label="debug" value="debug">
                                {{ this.$t('api_test.definition.request.fast_debug') }}
                              </el-radio>
                              <el-radio label="api" value="api">
                                {{ this.$t('api_test.definition.request.title') }}
                              </el-radio>
                            </el-radio-group>
                          </el-form-item>
                        </el-form>
                      </fieldset>
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

  }
};
</script>

<style scoped>

/deep/ .el-form-item__label {
  white-space: pre-line;
}


</style>
