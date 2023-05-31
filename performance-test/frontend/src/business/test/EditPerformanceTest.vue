<template>
  <ms-container>
    <ms-main-container>
      <el-card v-loading="loading">
        <el-row>
          <el-col :span="12">
            <el-form :inline="true">
              <el-form-item :label="$t('load_test.name')">
                <el-input
                  :disabled="isReadOnly"
                  :placeholder="$t('load_test.input_name')"
                  v-model="test.name"
                  class="input-with-select"
                  size="small"
                  maxlength="255"
                  show-word-limit
                />
              </el-form-item>
            </el-form>
          </el-col>

          <el-col :span="12">
            <el-tooltip
              :content="$t('commons.follow')"
              placement="bottom"
              effect="dark"
              v-if="!showFollow"
            >
              <i
                class="el-icon-star-off"
                style="
                  color: #783987;
                  font-size: 25px;
                  margin-right: 15px;
                  cursor: pointer;
                  position: relative;
                  top: 5px;
                "
                @click="saveFollow"
              />
            </el-tooltip>
            <el-tooltip
              :content="$t('commons.cancel')"
              placement="bottom"
              effect="dark"
              v-if="showFollow"
            >
              <i
                class="el-icon-star-on"
                style="
                  color: #783987;
                  font-size: 28px;
                  margin-right: 15px;
                  cursor: pointer;
                  position: relative;
                  top: 5px;
                "
                @click="saveFollow"
              />
            </el-tooltip>

            <el-link
              type="primary"
              size="small"
              style="margin-right: 5px"
              @click="openHis"
              v-if="test.id"
            >
              {{ $t("operating_log.change_history") }}
            </el-link>
            <mx-version-history
              v-xpack
              ref="versionHistory"
              :version-data="versionData"
              :current-id="testId"
              :is-read="isReadOnly"
              :has-latest="hasLatest"
              @setLatest="setLatest"
              @compare="compare"
              @checkout="checkout"
              @create="create"
              @del="del"
            />
            <el-button
              :disabled="isReadOnly"
              type="primary"
              size="small"
              plain
              @click="save"
              v-permission="['PROJECT_PERFORMANCE_TEST:READ+EDIT']"
              >{{ $t("commons.save") }}
            </el-button>
            <el-button
              :disabled="isReadOnly"
              size="small"
              type="primary"
              plain
              @click="saveAndRun"
              v-permission="['PROJECT_PERFORMANCE_TEST:READ+RUN']"
            >
              {{ $t("load_test.save_and_run") }}
            </el-button>
            <el-button
              :disabled="isReadOnly"
              size="small"
              type="warning"
              plain
              @click="cancel"
            >
              {{ $t("commons.cancel") }}
            </el-button>

            <schedule-config
              :schedule="test.schedule"
              :save="saveCronExpression"
              @scheduleChange="saveSchedule"
              v-if="hasPermission('PROJECT_PERFORMANCE_TEST:READ+SCHEDULE')"
              :check-open="checkScheduleEdit"
              :test-id="testId"
              :custom-validate="durationValidate"
            />

            <ms-tip-button
              v-if="
                test.isNeedUpdate &&
                hasPermission('PROJECT_PERFORMANCE_TEST:READ+EDIT')
              "
              class="sync-btn"
              type="primary"
              size="small"
              circle
              icon="el-icon-connection"
              @click="syncScenario"
              :plain="!test.isNeedUpdate"
              :disabled="!test.isNeedUpdate"
              :tip="'同步场景测试最新变更'"
            />
          </el-col>
        </el-row>

        <el-tabs v-model="active" @tab-click="clickTab">
          <el-tab-pane
            :label="$t('load_test.basic_config')"
            class="advanced-config"
          >
            <performance-basic-config
              :is-read-only="isReadOnly"
              :test="test"
              ref="basicConfig"
              @tgTypeChange="tgTypeChange"
              @fileChange="fileChange"
            />
          </el-tab-pane>
          <el-tab-pane
            :label="$t('load_test.pressure_config')"
            class="advanced-config"
          >
            <performance-pressure-config
              :is-read-only="isReadOnly"
              :test="test"
              :test-id="testId"
              @fileChange="fileChange"
              ref="pressureConfig"
              @changeActive="changeTabActive"
            />
          </el-tab-pane>
          <el-tab-pane
            :label="$t('load_test.advanced_config')"
            class="advanced-config"
          >
            <performance-advanced-config
              :read-only="isReadOnly"
              :test-id="testId"
              ref="advancedConfig"
            />
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <ms-change-history ref="changeHistory" />

      <el-dialog
        :fullscreen="true"
        :visible.sync="dialogVisible"
        :destroy-on-close="true"
        width="100%"
      >
        <diff-version
          v-if="dialogVisible"
          :old-data="oldData"
          :show-follow="showFollow"
          :new-data="newData"
          :new-show-follow="newShowFollow"
        ></diff-version>
      </el-dialog>
    </ms-main-container>
  </ms-container>
</template>

<script>
import PerformanceBasicConfig from "./components/PerformanceBasicConfig";
import PerformancePressureConfig from "./components/PerformancePressureConfig";
import PerformanceAdvancedConfig from "./components/PerformanceAdvancedConfig";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {getCurrentProjectID, getCurrentUser, getCurrentWorkspaceId,} from "metersphere-frontend/src/utils/token";
import {hasLicense, hasPermission,} from "metersphere-frontend/src/utils/permission";
import ScheduleConfig from "./components/ScheduleConfig";
import MsChangeHistory from "metersphere-frontend/src/components/history/ChangeHistory";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTipButton from "metersphere-frontend/src/components/MsTipButton";
import MxVersionHistory from "metersphere-frontend/src/components/version/MxVersionHistory";
import DiffVersion from "./DiffVersion";
import {PROJECT_ID} from "metersphere-frontend/src/utils/constants";
import {getProjectUsers} from "metersphere-frontend/src/api/user";
import {
  deleteCurrentVersionTest,
  getFollows,
  getTest,
  getTestByVersion,
  getTestVersionHistory,
  runTest,
  saveFollows,
  saveSchedule,
  saveTest,
  syncScenario,
} from "@/api/performance";
import {getDefaultVersion, setLatestVersionById,} from "metersphere-frontend/src/api/version";

export default {
  name: "EditPerformanceTest",
  components: {
    MsTipButton,
    MsTableOperatorButton,
    ScheduleConfig,
    PerformancePressureConfig,
    PerformanceBasicConfig,
    PerformanceAdvancedConfig,
    MsContainer,
    MsMainContainer,
    MsChangeHistory,
    DiffVersion,
    MxVersionHistory,
  },
  inject: ["reload"],
  data() {
    return {
      dialogVisible: false,
      loading: false,
      test: { schedule: {}, follows: [] },
      oldData: { schedule: {}, follows: [] },
      newData: { schedule: {}, follows: [] },
      newShowFollow: false,
      savePath: "/performance/save",
      editPath: "/performance/edit",
      projects: [],
      active: "0",
      testId: "",
      isReadOnly: false,
      showFollow: false,
      tabs: [
        {
          title: this.$t("load_test.basic_config"),
          id: "0",
          component: "PerformanceBasicConfig",
        },
        {
          title: this.$t("load_test.pressure_config"),
          id: "1",
          component: "PerformancePressureConfig",
        },
        {
          title: this.$t("load_test.advanced_config"),
          id: "2",
          component: "PerformanceAdvancedConfig",
        },
      ],
      maintainerOptions: [],
      versionData: [],
      projectEnvMap: {},
      latestVersionId: "",
      hasLatest: false,
    };
  },
  watch: {
    $route(to) {
      // 如果是创建测试
      if (to.path.startsWith("/performance/test/create")) {
        this.reload();
      }

      if (!to.path.startsWith("/performance/test/edit")) {
        return;
      }
      this.reload();
      this.isReadOnly = false;
      this.getTest(to.params.testId);
    },
  },
  created() {
    let projectId = this.$route.query.projectId;
    if (projectId && projectId !== getCurrentProjectID()) {
      sessionStorage.setItem(PROJECT_ID, projectId);
    }
    this.isReadOnly = !hasPermission("PROJECT_PERFORMANCE_TEST:READ+EDIT");
    this.getTest(this.$route.params.testId);
    if (hasLicense()) {
      this.getDefaultVersion();
    }
    this.$EventBus.$on("projectChange", this.handleProjectChange);
  },
  destroyed() {
    this.$EventBus.$off("projectChange", this.handleProjectChange);
  },
  mounted() {
    // todo
    this.importAPITest();
    this.getMaintainerOptions();
  },
  methods: {
    hasPermission,
    currentUser: () => {
      return getCurrentUser();
    },
    getMaintainerOptions() {
      getProjectUsers().then((res) => {
        this.maintainerOptions = res.data;
      });
    },
    openHis() {
      this.$refs.changeHistory.open(this.test.id, [
        "性能测试",
        "性能測試",
        "Performance test",
        "PERFORMANCE_TEST",
      ]);
    },
    importAPITest() {
      // 先用 sessionStorage
      let apiTest = JSON.parse(sessionStorage.getItem("LOAD_TEST"))?.test;
      if (apiTest && apiTest.name) {
        this.$set(this.test, "name", apiTest.name);
        if (apiTest.jmx.projectEnvMap) {
          this.projectEnvMap = apiTest.jmx.projectEnvMap;
        }
        if (apiTest.jmx.scenarioId) {
          this.$refs.basicConfig.importScenario(apiTest.jmx.scenarioId);
          this.$refs.basicConfig.handleUpload();
          let relateApiList = [];
          relateApiList.push({
            apiId: apiTest.jmx.scenarioId,
            apiVersion: apiTest.jmx.version,
            type: "SCENARIO",
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
            type: "API_CASE",
          });
          this.$set(this.test, "apiList", relateApiList);
        }
        if (JSON.stringify(apiTest.jmx.attachFiles) !== "{}") {
          let attachFiles = [];
          for (let fileID in apiTest.jmx.attachFiles) {
            attachFiles.push(fileID);
          }
          if (attachFiles.length > 0) {
            this.$refs.basicConfig.selectAttachFileById(attachFiles);
          }
        }
        this.active = "1";
        sessionStorage.removeItem("LOAD_TEST");
      } else {
        // 先用 sessionStorage
        let scenarioJmxs = JSON.parse(
          sessionStorage.getItem("LOAD_TEST")
        )?.scenarioJmxs;
        if (scenarioJmxs && scenarioJmxs.name) {
          this.$set(this.test, "name", scenarioJmxs.name);
          let relateApiList = [];
          if (scenarioJmxs.projectEnvMap) {
            this.projectEnvMap = scenarioJmxs.projectEnvMap;
          }
          if (scenarioJmxs.jmxs) {
            scenarioJmxs.jmxs.forEach((item) => {
              if (item.scenarioId) {
                this.$refs.basicConfig.importScenario(item.scenarioId);
                relateApiList.push({
                  apiId: item.scenarioId,
                  apiVersion: item.version,
                  type: "SCENARIO",
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
              }
              this.$set(this.test, "apiList", relateApiList);
            });
            this.$refs.basicConfig.handleUpload();
            this.active = "1";
            sessionStorage.removeItem("LOAD_TEST");
          }
        }
      }
    },
    getTest(testId) {
      if (testId) {
        this.test.follows = [];
        this.showFollow = false;
        this.testId = testId;
        this.loading = getTest(testId).then((response) => {
          if (response.data) {
            this.test = response.data;
            if (!this.test.schedule) {
              this.test.schedule = {};
            }
            if (this.test.envInfo) {
              try {
                this.projectEnvMap = JSON.parse(this.test.envInfo);
              } catch (e) {
                this.projectEnvMap = null;
              }
            }
            this.$refs.pressureConfig.getJmxContent();
            this.getDefaultFollow(testId);
          }
        });
      }
    },
    getDefaultFollow(testId) {
      getFollows(testId).then((response) => {
        this.$set(this.test, "follows", response.data);
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
        if (this.$refs.versionHistory) {
          this.$refs.versionHistory.loading = false;
        }
        return;
      }
      if (!this.test.versionId) {
        if (
          this.$refs.versionHistory &&
          this.$refs.versionHistory.currentVersion
        ) {
          this.test.versionId = this.$refs.versionHistory.currentVersion.id;
        }
      }
      let formData = this.getSaveOption();

      this.loading = saveTest(this.test, formData)
        .then(({ data }) => {
          this.$success(this.$t("commons.save_success"));
          this.test.id = data.data.id;
          this.$refs.advancedConfig.cancelAllEdit();
          if (this.$route.path.indexOf("/performance/test/edit/") < 0) {
            this.$router.push({
              path: "/performance/test/edit/" + data.data.id,
            });
          } else {
            this.$refs.basicConfig.uploadList = [];
            this.getTest(this.test.id);
          }
          this.getVersionHistory();
        })
        .catch((error) => {
          if (this.$refs.versionHistory) {
            this.$refs.versionHistory.loading = false;
          }
        });
    },
    saveAndRun() {
      if (!this.validTest()) {
        return;
      }

      let formData = this.getSaveOption();
      this.loading = saveTest(this.test, formData).then(({ data }) => {
        this.test.id = data.data.id;
        if (this.$route.path.indexOf("/performance/test/edit/") < 0) {
          this.$router.push({
            path: "/performance/test/edit/" + data.data.id,
          });
        } else {
          this.$refs.basicConfig.uploadList = [];
          this.getTest(this.test.id);
        }
        this.$success(this.$t("commons.save_success"));
        runTest(this.test).then((response) => {
          let reportId = response.data;
          this.$router.push({path: "/performance/report/view/" + reportId});
        });
      });
    },
    getSaveOption() {
      let formData = new FormData();

      if (this.$refs.basicConfig.uploadList.length > 0) {
        this.$refs.basicConfig.uploadList.forEach((f) => {
          formData.append("file", f);
        });
      }
      // 基本配置
      this.test.updatedFileList = this.$refs.basicConfig.updatedFileList();
      this.test.fileSorts = this.$refs.basicConfig.fileSorts();
      this.test.conversionFileIdList =
        this.$refs.basicConfig.conversionMetadataIdList();
      // 压力配置
      this.test.loadConfiguration = JSON.stringify(
        this.$refs.pressureConfig.convertProperty()
      );
      this.test.testResourcePoolId = this.$refs.pressureConfig.resourcePool;
      // 高级配置
      this.test.advancedConfiguration = JSON.stringify(
        this.$refs.advancedConfig.configurations()
      );
      this.test.projectEnvMap = this.projectEnvMap;
      // file属性不需要json化
      let requestJson = JSON.stringify(this.test, function (key, value) {
        return key === "file" ? undefined : value;
      });
      formData.append(
        "request",
        new Blob([requestJson], {
          type: "application/json",
        })
      );
      return formData;
    },
    syncScenario() {
      if (!hasPermission("PROJECT_API_SCENARIO:READ+CREATE")) {
        this.$warning(
          this.$t("performance_test.sync_scenario_no_permission_tips")
        );
        return;
      }
      let param = {
        id: this.test.id,
        scenarioId: this.test.scenarioId,
      };
      syncScenario(param).then(() => {
        this.getTest(this.$route.params.testId);
        this.$success("commons.save_success");
      });
    },
    cancel() {
      this.$router.push({ path: "/performance/test/all" });
    },
    validTest() {
      let currentProjectId = getCurrentProjectID();
      this.test.projectId = currentProjectId;

      if (!this.test.name) {
        this.$error(this.$t("load_test.test_name_is_null"));
        return false;
      }

      if (!this.test.projectId) {
        this.$error(this.$t("load_test.project_is_null"));
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
      if (!param.id) {
        // 创建定时任务，初始化是开启状态
        param.enable = true;
      }

      saveSchedule(param).then((response) => {
        this.$success(this.$t("commons.save_success"));
        this.getTest(this.test.id);
      });
    },
    checkScheduleEdit() {
      if (!this.test.id) {
        this.$message(this.$t("api_test.environment.please_save_test"));
        return false;
      }
      return true;
    },
    durationValidate(intervalTime) {
      let duration = 0;
      this.$refs.pressureConfig.threadGroups.forEach((tg) => {
        let d = this.$refs.pressureConfig.getDuration(tg);
        if (duration < d) {
          duration = d;
        }
      });
      if (intervalTime < duration * 1000) {
        return {
          pass: false,
          info: this.$t("load_test.schedule_tip"),
        };
      }
      return {
        pass: true,
      };
    },
    fileChange(threadGroups) {
      let handler = this.$refs.pressureConfig;
      let csvSet = new Set();
      threadGroups.forEach((tg) => {
        tg.threadNumber = tg.threadNumber || 10;
        tg.duration = tg.duration || 10;
        tg.durationHours = Math.floor(tg.duration / 3600);
        tg.durationMinutes = Math.floor((tg.duration / 60) % 60);
        tg.durationSeconds = Math.floor(tg.duration % 60);
        tg.rampUpTime = tg.rampUpTime || 5;
        tg.step = tg.step || 5;
        tg.rpsLimit = tg.rpsLimit || 10;
        tg.threadType = tg.threadType || "DURATION";
        tg.iterateNum = tg.iterateNum || 1;
        tg.iterateRampUp = tg.iterateRampUp || 10;

        if (tg.csvFiles) {
          tg.csvFiles.map((item) => csvSet.add(item));
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
      if (tab.index === "1") {
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
          saveFollows(this.testId, this.test.follows).then(() => {
            this.$success(this.$t("commons.cancel_follow_success"));
          });
        }
      } else {
        this.showFollow = true;
        if (!this.test.follows) {
          this.test.follows = [];
        }
        this.test.follows.push(this.currentUser().id);
        if (this.testId) {
          saveFollows(this.testId, this.test.follows).then(() => {
            this.$success(this.$t("commons.follow_success"));
          });
        }
      }
    },
    getDefaultVersion() {
      getDefaultVersion(getCurrentProjectID()).then((response) => {
        this.latestVersionId = response.data;
        this.getVersionHistory();
      });
    },
    getVersionHistory() {
      let testId = undefined;
      if (this.testId) {
        testId = this.testId;
      }
      getTestVersionHistory(testId).then((response) => {
        this.versionData = response.data;
        let latestVersionData = response.data.filter(
          (v) => v.versionId === this.latestVersionId
        );
        if (latestVersionData.length > 0) {
          this.hasLatest = false;
        } else {
          this.hasLatest = true;
        }
      });
    },
    compare(row) {
      this.oldData = this.test;
      this.oldData.createTime = this.$refs.versionHistory.versionOptions.filter(
        (v) => v.id === this.test.versionId
      )[0].createTime;
      getTestByVersion(row.id, this.test.refId).then((response) => {
        getTest(response.data.id).then((res) => {
          if (res.data) {
            this.newData = res.data;
            this.newData.createTime = row.createTime;
            getFollows(response.data.id).then((resp) => {
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
      this.loading = getTestByVersion(
        this.test.versionId,
        this.test.refId
      ).then((response) => {
        this.testId = response.data.id;
        this.$router.push({
          path: "/performance/test/edit/" + this.testId,
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
      this.$alert(
        this.$t("load_test.delete_confirm") + " " + row.name + " ？",
        "",
        {
          confirmButtonText: this.$t("commons.confirm"),
          callback: (action) => {
            if (action === "confirm") {
              deleteCurrentVersionTest({
                versionId: row.id,
                refId: this.test.refId,
              }).then((response) => {
                this.$success(this.$t("load_test.delete_success"));
                this.getVersionHistory();
              });
            }
          },
        }
      );
    },
    setLatest(row) {
      let param = {
        projectId: getCurrentProjectID(),
        type: "PERFORMANCE",
        versionId: row.id,
        resourceId: this.test.id,
      };
      setLatestVersionById(param).then(() => {
        this.$success(this.$t("commons.modify_success"));
        this.checkout(row);
      });
    },
    handleProjectChange() {
      if (this.$route.path.startsWith("/performance/test/edit")) {
        this.$nextTick(() => {
          this.$router.push("/performance/test/all");
        });
      }
    },
  },
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
  height: calc(100vh - 175px);
  overflow: auto;
}

.sync-btn {
  float: right;
  margin-right: 25px;
  margin-top: 5px;
}
</style>
