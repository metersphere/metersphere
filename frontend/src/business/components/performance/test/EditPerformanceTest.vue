<template>
  <ms-container>
    <ms-main-container>
      <el-card v-loading="result.loading">
        <el-row>
          <el-col :span="12">
            <el-form :inline="true">
              <el-form-item :label="$t('load_test.name') ">
                <el-input :disabled="isReadOnly" :placeholder="$t('load_test.input_name')" v-model="test.name"
                          class="input-with-select"
                          size="small"
                          maxlength="255" show-word-limit/>
              </el-form-item>
            </el-form>
          </el-col>

          <el-col :span="12">
            <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow">
              <i class="el-icon-star-off"
                 style="color: #783987; font-size: 25px; margin-right: 15px;cursor: pointer;position: relative; top: 5px; "
                 @click="saveFollow"/>
            </el-tooltip>
            <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow">
              <i class="el-icon-star-on"
                 style="color: #783987; font-size: 28px;  margin-right: 15px;cursor: pointer;position: relative; top: 5px; "
                 @click="saveFollow"/>
            </el-tooltip>

            <el-link type="primary" size="small" style="margin-right: 5px" @click="openHis" v-if="test.id">
              {{ $t('operating_log.change_history') }}
            </el-link>
            <ms-version-history v-xpack
                                ref="versionHistory"
                                :version-data="versionData"
                                :current-id="testId"
                                @compare="compare" @checkout="checkout" @create="create" @del="del"/>
            <el-button :disabled="isReadOnly" type="primary" size="small" plain @click="save"
                       v-permission="['PROJECT_PERFORMANCE_TEST:READ+EDIT']"
            >{{ $t('commons.save') }}
            </el-button>
            <el-button :disabled="isReadOnly" size="small" type="primary" plain @click="saveAndRun"
                       v-permission="['PROJECT_PERFORMANCE_TEST:READ+RUN']">
              {{ $t('load_test.save_and_run') }}
            </el-button>
            <el-button :disabled="isReadOnly" size="small" type="warning" plain @click="cancel">
              {{ $t('commons.cancel') }}
            </el-button>

            <ms-schedule-config :schedule="test.schedule" :save="saveCronExpression" @scheduleChange="saveSchedule"
                                v-permission="['PROJECT_PERFORMANCE_TEST:READ+SCHEDULE']"
                                :check-open="checkScheduleEdit" :test-id="testId" :custom-validate="durationValidate"/>

            <ms-tip-button v-if="test.isNeedUpdate"
                           class="sync-btn" type="primary" size="small" circle
                           icon="el-icon-connection"
                           @click="syncScenario"
                           :plain="!test.isNeedUpdate"
                           :disabled="!test.isNeedUpdate"
                           :tip="'同步场景测试最新变更'"/>

          </el-col>
        </el-row>

        <el-tabs v-model="active" @tab-click="clickTab">
          <el-tab-pane :label="$t('load_test.basic_config')" class="advanced-config">
            <performance-basic-config :is-read-only="isReadOnly" :test="test" ref="basicConfig"
                                      @tgTypeChange="tgTypeChange"
                                      @fileChange="fileChange"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('load_test.pressure_config')" class="advanced-config">
            <performance-pressure-config :is-read-only="isReadOnly" :test="test" :test-id="testId"
                                         @fileChange="fileChange"
                                         ref="pressureConfig" @changeActive="changeTabActive"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('load_test.advanced_config')" class="advanced-config">
            <performance-advanced-config :read-only="isReadOnly" :test-id="testId" ref="advancedConfig"/>
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <ms-change-history ref="changeHistory"/>

      <el-dialog
        :fullscreen="true"
        :visible.sync="dialogVisible"
        :destroy-on-close="true"
        width="100%"
      >
        <diff-version v-if="dialogVisible" :old-data="oldData" :show-follow="showFollow" :new-data="newData"
                      :new-show-follow="newShowFollow"></diff-version>
      </el-dialog>

    </ms-main-container>
  </ms-container>
</template>

<script>
import PerformanceBasicConfig from "./components/PerformanceBasicConfig";
import PerformancePressureConfig from "./components/PerformancePressureConfig";
import PerformanceAdvancedConfig from "./components/PerformanceAdvancedConfig";
import MsContainer from "../../common/components/MsContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import {getCurrentProjectID, getCurrentUser, getCurrentWorkspaceId, hasLicense, hasPermission} from "@/common/js/utils";
import MsScheduleConfig from "../../common/components/MsScheduleConfig";
import MsChangeHistory from "../../history/ChangeHistory";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import MsTipButton from "@/business/components/common/components/MsTipButton";
import DiffVersion from "@/business/components/performance/test/DiffVersion";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const versionHistory = requireComponent.keys().length > 0 ? requireComponent("./version/VersionHistory.vue") : {};

export default {
  name: "EditPerformanceTest",
  components: {
    MsTipButton,
    MsTableOperatorButton,
    MsScheduleConfig,
    PerformancePressureConfig,
    PerformanceBasicConfig,
    PerformanceAdvancedConfig,
    MsContainer,
    MsMainContainer,
    MsChangeHistory,
    'MsVersionHistory': versionHistory.default,
    DiffVersion
  },
  inject: [
    'reload'
  ],
  data() {
    return {
      dialogVisible: false,
      result: {},
      test: {schedule: {}, follows: []},
      oldData: {schedule: {}, follows: []},
      newData: {schedule: {}, follows: []},
      newShowFollow: false,
      savePath: "/performance/save",
      editPath: "/performance/edit",
      runPath: "/performance/run",
      projects: [],
      active: '0',
      testId: '',
      isReadOnly: false,
      showFollow: false,
      tabs: [{
        title: this.$t('load_test.basic_config'),
        id: '0',
        component: 'PerformanceBasicConfig'
      }, {
        title: this.$t('load_test.pressure_config'),
        id: '1',
        component: 'PerformancePressureConfig'
      }, {
        title: this.$t('load_test.advanced_config'),
        id: '2',
        component: 'PerformanceAdvancedConfig'
      }],
      maintainerOptions: [],
      versionData: [],
    };
  },
  watch: {
    '$route'(to) {
      // 如果是创建测试
      if (to.name === 'createPerTest') {
        this.reload();
        return;
      }

      if (to.name !== 'editPerTest') {
        return;
      }

      this.isReadOnly = false;
      this.getTest(to.params.testId);
    }

  },
  created() {
    this.isReadOnly = !hasPermission('PROJECT_PERFORMANCE_TEST:READ+EDIT');
    this.getTest(this.$route.params.testId);
    if (hasLicense()) {
      this.getVersionHistory();
    }
  },
  mounted() {
    this.importAPITest();
    this.getMaintainerOptions();
  },
  methods: {
    currentUser: () => {
      return getCurrentUser();
    },
    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.maintainerOptions = response.data;
      });
    },
    openHis() {
      this.$refs.changeHistory.open(this.test.id, ["性能测试", "性能測試", "Performance test", "PERFORMANCE_TEST"]);
    },
    importAPITest() {
      let apiTest = this.$store.state.test;
      console.log("输出vuex的test");
      console.log(apiTest);
      if (apiTest && apiTest.name) {
        console.log("set test name");
        this.$set(this.test, "name", apiTest.name);
        if (apiTest.jmx.scenarioId) {
          this.$refs.basicConfig.importScenario(apiTest.jmx.scenarioId);
          this.$refs.basicConfig.handleUpload();
          let relateApiList = [];
          relateApiList.push({
            apiId: apiTest.jmx.scenarioId,
            apiVersion: apiTest.jmx.version,
            type: 'SCENARIO'
          });
          this.$set(this.test, "apiList", relateApiList);
        }
        if (apiTest.jmx.caseId) {
          this.$refs.basicConfig.importCase(apiTest.jmx);
          let relateApiList = [];
          relateApiList.push({
            apiId: apiTest.jmx.caseId,
            apiVersion: apiTest.jmx.version,
            envId: apiTest.jmx.envId,
            type: 'API_CASE'
          });
          this.$set(this.test, "apiList", relateApiList);
        }
        if (JSON.stringify(apiTest.jmx.attachFiles) !== "{}") {
          let attachFiles = [];
          for (let fileID in apiTest.jmx.attachFiles) {
            attachFiles.push(fileID);
          }
          // if (attachFiles.length > 0) {
          // this.$refs.basicConfig.selectAttachFileById(attachFiles);
          // }
        }
        this.active = '1';
        this.$store.commit("clearTest");
      } else {
        let scenarioJmxs = this.$store.state.scenarioJmxs;
        console.log("输出vuex的scenarioJmxs");
        console.log(scenarioJmxs);
        if (scenarioJmxs && scenarioJmxs.name) {
          console.log("set scenarioJmxs name");
          this.$set(this.test, "name", scenarioJmxs.name);
          let relateApiList = [];
          if (scenarioJmxs.jmxs) {
            scenarioJmxs.jmxs.forEach(item => {
              if (item.scenarioId) {
                this.$refs.basicConfig.importScenario(item.scenarioId);
                this.$refs.basicConfig.handleUpload();
                relateApiList.push({
                  apiId: item.scenarioId,
                  apiVersion: item.version,
                  type: 'SCENARIO'
                });
              }
              if (item.caseId) {
                this.$refs.basicConfig.importCase(item);
              }
              if (JSON.stringify(item.attachFiles) !== "{}") {
                let attachFiles = [];
                for (let fileID in item.attachFiles) {
                  attachFiles.push(fileID);
                }
                // if (attachFiles.length > 0) {
                //   this.$refs.basicConfig.selectAttachFileById(attachFiles);
                // }
              }
              this.$set(this.test, "apiList", relateApiList);
            });
            this.active = '1';
            this.$store.commit("clearScenarioJmxs");
          }
        }
      }
    },
    getTest(testId) {
      if (testId) {
        this.test.follows = [];
        this.showFollow = false;
        this.testId = testId;
        this.result = this.$get('/performance/get/' + testId, response => {
          if (response.data) {
            this.test = response.data;
            if (!this.test.schedule) {
              this.test.schedule = {};
            }
            this.getDefaultFollow(testId);
          }
        });
      }
    },
    getDefaultFollow(testId) {
      this.$get('/performance/test/follow/' + testId, response => {
        this.$set(this.test, 'follows', response.data);
        for (let i = 0; i < this.test.follows.length; i++) {
          if (this.test.follows[i] === this.currentUser().id) {
            this.showFollow = true;
            break;
          }
        }
      });
    },
    save(newVersion) {
      if (!this.validTest()) {
        return;
      }
      if (!this.test.versionId) {
        if (this.$refs.versionHistory && this.$refs.versionHistory.currentVersion) {
          this.test.versionId = this.$refs.versionHistory.currentVersion.id;
        }
      }
      let options = this.getSaveOption();

      this.result = this.$request(options, (response) => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.advancedConfig.cancelAllEdit();
        if (newVersion) {
          this.$router.push({
            path: '/performance/test/edit/' + response.data.id,
          });
          this.getVersionHistory();
        }
      });
    },
    saveAndRun() {
      if (!this.validTest()) {
        return;
      }

      let options = this.getSaveOption();

      this.result = this.$request(options, (response) => {
        this.test.id = response.data.id;
        this.$success(this.$t('commons.save_success'));
        this.result = this.$post(this.runPath, {id: this.test.id, triggerMode: 'MANUAL'}, (response) => {
          let reportId = response.data;
          this.$router.push({path: '/performance/report/view/' + reportId});
        });
      });
    },
    getSaveOption() {
      let formData = new FormData();
      let url = this.test.id ? this.editPath : this.savePath;

      if (this.$refs.basicConfig.uploadList.length > 0) {
        this.$refs.basicConfig.uploadList.forEach(f => {
          formData.append("file", f);
        });
      }
      // 基本配置
      this.test.updatedFileList = this.$refs.basicConfig.updatedFileList();
      this.test.fileSorts = this.$refs.basicConfig.fileSorts();
      this.test.conversionFileIdList = this.$refs.basicConfig.conversionMetadataIdList();
      // 压力配置
      this.test.loadConfiguration = JSON.stringify(this.$refs.pressureConfig.convertProperty());
      this.test.testResourcePoolId = this.$refs.pressureConfig.resourcePool;
      // 高级配置
      this.test.advancedConfiguration = JSON.stringify(this.$refs.advancedConfig.configurations());

      // file属性不需要json化
      let requestJson = JSON.stringify(this.test, function (key, value) {
        return key === "file" ? undefined : value;
      });

      formData.append('request', new Blob([requestJson], {
        type: "application/json"
      }));
      return {
        method: 'POST',
        url: url,
        data: formData,
        headers: {
          'Content-Type': undefined
        }
      };
    },
    syncScenario() {
      let param = {
        id: this.test.id,
        scenarioId: this.test.scenarioId
      };
      this.result = this.$post('/performance/sync/scenario', param, () => {
        this.getTest(this.$route.params.testId);
        this.$success('更新成功');
      });
    },
    cancel() {
      this.$router.push({path: '/performance/test/all'});
    },
    validTest() {
      let currentProjectId = getCurrentProjectID();
      this.test.projectId = currentProjectId;

      if (!this.test.name) {
        this.$error(this.$t('load_test.test_name_is_null'));
        return false;
      }

      if (!this.test.projectId) {
        this.$error(this.$t('load_test.project_is_null'));
        return false;
      }

      if (!this.$refs.basicConfig.validConfig()) {
        return false;
      }

      if (!this.$refs.pressureConfig.validConfig()) {
        return false;
      }

      if (!this.$refs.advancedConfig.validConfig()) {
        return false;
      }

      /// todo: 其他校验

      return true;
    },
    changeTabActive(activeName) {
      this.$nextTick(() => {
        this.active = activeName;
      });
    },
    saveCronExpression(cronExpression) {
      this.test.schedule.enable = true;
      this.test.schedule.value = cronExpression;
      this.saveSchedule();
    },
    saveSchedule() {
      this.checkScheduleEdit();
      let param = {};
      param = this.test.schedule;
      param.resourceId = this.test.id;
      // 兼容问题，数据库里有的projectId为空
      if (!param.projectId) {
        param.projectId = getCurrentProjectID();
      }
      if (!param.workspaceId) {
        param.workspaceId = getCurrentWorkspaceId();
      }
      let url = '/performance/schedule/create';
      if (param.id) {
        url = '/performance/schedule/update';
      }
      this.$post(url, param, response => {
        this.$success(this.$t('commons.save_success'));
        this.getTest(this.test.id);
      });
    },
    checkScheduleEdit() {
      if (!this.test.id) {
        this.$message(this.$t('api_test.environment.please_save_test'));
        return false;
      }
      return true;
    },
    durationValidate(intervalTime) {
      let duration = 0;
      this.$refs.pressureConfig.threadGroups.forEach(tg => {
        let d = this.$refs.pressureConfig.getDuration(tg);
        if (duration < d) {
          duration = d;
        }
      });
      if (intervalTime < duration * 1000) {
        return {
          pass: false,
          info: this.$t('load_test.schedule_tip')
        };
      }
      return {
        pass: true
      };
    },
    fileChange(threadGroups) {
      let handler = this.$refs.pressureConfig;
      let csvSet = new Set;
      threadGroups.forEach(tg => {
        tg.threadNumber = tg.threadNumber || 10;
        tg.duration = tg.duration || 10;
        tg.durationHours = Math.floor(tg.duration / 3600);
        tg.durationMinutes = Math.floor((tg.duration / 60 % 60));
        tg.durationSeconds = Math.floor((tg.duration % 60));
        tg.rampUpTime = tg.rampUpTime || 5;
        tg.step = tg.step || 5;
        tg.rpsLimit = tg.rpsLimit || 10;
        tg.threadType = tg.threadType || 'DURATION';
        tg.iterateNum = tg.iterateNum || 1;
        tg.iterateRampUp = tg.iterateRampUp || 10;

        if (tg.csvFiles) {
          tg.csvFiles.map(item => csvSet.add(item));
        }
      });
      let csvFiles = [];
      for (const f of csvSet) {
        csvFiles.push({name: f, csvSplit: false, csvHasHeader: true});
      }

      this.$set(handler, "threadGroups", threadGroups);

      this.$refs.basicConfig.threadGroups = threadGroups;
      this.$refs.pressureConfig.threadGroups = threadGroups;
      this.$refs.advancedConfig.csvFiles = csvFiles;

      this.$refs.pressureConfig.resourcePoolChange();
      handler.calculateTotalChart();
    },
    tgTypeChange(threadGroup) {
      let handler = this.$refs.pressureConfig;
      handler.calculateTotalChart();
    },
    clickTab(tab) {
      if (tab.index === '1') {
        this.$refs.pressureConfig.calculateTotalChart();
      }
    },
    saveFollow() {
      if (this.showFollow) {
        this.showFollow = false;
        for (let i = 0; i < this.test.follows.length; i++) {
          if (this.test.follows[i] === this.currentUser().id) {
            this.test.follows.splice(i, 1);
            break;
          }
        }
        if (this.testId) {
          this.$post("/performance/test/update/follows/" + this.testId, this.test.follows, () => {
            this.$success(this.$t('commons.cancel_follow_success'));
          });
        }
      } else {
        this.showFollow = true;
        if (!this.test.follows) {
          this.test.follows = [];
        }
        this.test.follows.push(this.currentUser().id);
        if (this.testId) {
          this.$post("/performance/test/update/follows/" + this.testId, this.test.follows, () => {
            this.$success(this.$t('commons.follow_success'));
          });
        }
      }
    },
    getVersionHistory() {
      let testId = undefined;
      if (this.testId) {
        testId = this.testId;
      }
      this.$get('/performance/versions/' + testId, response => {
        this.versionData = response.data;
      });
    },
    compare(row) {
      this.oldData = this.test;
      this.$get('/performance/get/' + row.id + "/" + this.test.refId, response => {
        this.$get('/performance/get/' + response.data.id, res => {
          if (res.data) {
            this.newData = res.data;
            this.$get('/performance/test/follow/' + response.data.id, resp => {
              if (resp.data && resp.data.follows) {
                for (let i = 0; i < resp.data.follows.length; i++) {
                  if (resp.data.follows[i] === this.currentUser().id) {
                    this.newShowFollow = true;
                    break;
                  }
                }
              }
            });
          }
        });
      });
      if (this.newData) {
        this.dialogVisible = true;
      }
    },
    checkout(row) {
      //let test = this.versionData.filter(v => v.versionId === row.id)[0];
      this.test.versionId = row.id;
      this.result = this.$get('/performance/get/' + this.test.versionId + "/" + this.test.refId, response => {
        this.testId = response.data.id;
        this.$router.push({
          path: '/performance/test/edit/' + this.testId,
        });
        this.getVersionHistory();
      });
    },
    create(row) {
      // 创建新版本
      this.test.versionId = row.id;
      this.save(true);
    },
    del(row) {
      this.$alert(this.$t('load_test.delete_confirm') + ' ' + row.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$get('performance/delete/' + row.id + '/' + this.test.refId, () => {
              this.$success(this.$t('commons.delete_success'));
              this.getVersionHistory();
            });
          }
        }
      });
    }
  }
};
</script>

<style scoped>


.el-select {
  min-width: 130px;
}

.edit-testplan-container .input-with-select .el-input-group__prepend {
  background-color: #fff;
}

.advanced-config {
  height: calc(100vh - 210px);
  overflow: auto;
}

.sync-btn {
  float: right;
  margin-right: 25px;
  margin-top: 5px;
}

</style>
