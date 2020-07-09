<template>
  <div class="container">
    <div class="main-content">
      <el-card>
        <el-container class="test-container" v-loading="result.loading">
          <el-header>
            <el-row>
              <el-input :disabled="isReadOnly" class="test-name" v-model="test.name" maxlength="60"
                        :placeholder="$t('api_test.input_name')"
                        show-word-limit>
                <el-select :disabled="isReadOnly" class="test-project" v-model="test.projectId" slot="prepend"
                           :placeholder="$t('api_test.select_project')">
                  <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id"/>
                </el-select>
              </el-input>

              <el-button type="primary" plain :disabled="isDisabled || isReadOnly" @click="saveTest">
                {{$t('commons.save')}}
              </el-button>

              <el-button type="primary" plain v-if="!isShowRun" :disabled="isDisabled || isReadOnly"
                         @click="saveRunTest">
                {{$t('load_test.save_and_run')}}
              </el-button>

              <el-button :disabled="isReadOnly" type="primary" plain v-if="isShowRun" @click="runTest">
                {{$t('api_test.run')}}
              </el-button>

              <el-button :disabled="isReadOnly" type="warning" plain @click="cancel">{{$t('commons.cancel')}}
              </el-button>

              <el-dropdown trigger="click" @command="handleCommand">
                <el-button class="el-dropdown-link more" icon="el-icon-more" plain/>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="report" :disabled="test.status !== 'Completed'">
                    {{$t('api_report.title')}}
                  </el-dropdown-item>
                  <el-dropdown-item command="performance" :disabled="create || isReadOnly">
                    {{$t('api_test.create_performance_test')}}
                  </el-dropdown-item>
                  <el-dropdown-item command="export" :disabled="isReadOnly || create">
                    {{$t('api_test.export_config')}}
                  </el-dropdown-item>
                  <el-dropdown-item command="import" :disabled="isReadOnly">
                    {{$t('api_test.api_import.label')}}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>

              <api-import ref="apiImport"/>

              <ms-api-report-dialog :test-id="id" ref="reportDialog"/>

              <ms-schedule-config :schedule="test.schedule" :save="saveCronExpression" @scheduleChange="saveSchedule" :check-open="checkScheduleEdit"/>
            </el-row>
          </el-header>
          <ms-api-scenario-config :is-read-only="isReadOnly" :scenarios="test.scenarioDefinition" :project-id="test.projectId" ref="config"/>
        </el-container>
      </el-card>
    </div>
  </div>
</template>

<script>
  import MsApiScenarioConfig from "./components/ApiScenarioConfig";
  import {Test} from "./model/ScenarioModel"
  import MsApiReportStatus from "../report/ApiReportStatus";
  import MsApiReportDialog from "./ApiReportDialog";
  import {checkoutTestManagerOrTestUser, downloadFile} from "../../../../common/js/utils";
  import MsScheduleConfig from "../../common/components/MsScheduleConfig";
  import ApiImport from "./components/import/ApiImport";

  export default {
    name: "MsApiTestConfig",

    components: {ApiImport, MsScheduleConfig, MsApiReportDialog, MsApiReportStatus, MsApiScenarioConfig},

    props: ["id"],

    data() {
      return {
        reportVisible: false,
        create: false,
        result: {},
        projects: [],
        change: false,
        test: new Test(),
        isReadOnly: false
      }
    },

    watch: {
      '$route': 'init',
      test: {
        handler: function () {
          this.change = true;
        },
        deep: true
      }
    },

    methods: {
      init() {
        let projectId;

        this.isReadOnly = !checkoutTestManagerOrTestUser();

        if (this.id) {
          this.create = false;
          this.getTest(this.id);
        } else {
          this.create = true;
          this.test = new Test();
          if (this.$refs.config) {
            this.$refs.config.reset();
          }
          // 仅创建时获取选择的项目
          projectId = this.$store.state.common.projectId;
        }
        this.result = this.$get("/project/listAll", response => {
          this.projects = response.data;
          // 等待项目列表加载完
          if (projectId) this.test.projectId = projectId;
        })
      },
      getTest(id) {
        this.result = this.$get("/api/get/" + id, response => {
          if (response.data) {
            let item = response.data;

            this.test = new Test({
              id: item.id,
              projectId: item.projectId,
              name: item.name,
              status: item.status,
              scenarioDefinition: JSON.parse(item.scenarioDefinition),
              schedule: item.schedule ? item.schedule : {},
            });
            this.$refs.config.reset();
          }
        });
      },
      save(callback) {
        this.change = false;
        let url = this.create ? "/api/create" : "/api/update";
        this.result = this.$request(this.getOptions(url), () => {
          this.create = false;
          if (callback) callback();
        });
      },
      saveTest() {
        this.save(() => {
          this.$success(this.$t('commons.save_success'));
          if (this.create) {
            this.$router.push({
              path: '/api/test/edit?id=' + this.test.id
            })
          }
        })
      },
      runTest() {
        this.result = this.$post("/api/run", {id: this.test.id, triggerMode: 'MANUAL'}, (response) => {
          this.$success(this.$t('api_test.running'));
          this.$router.push({
            path: '/api/report/view/' + response.data
          })
        });
      },
      saveRunTest() {
        this.change = false;

        this.save(() => {
          this.$success(this.$t('commons.save_success'));
          this.runTest();
        })
      },
      cancel() {
        // console.log(this.test.toJMX().xml)
        this.$router.push('/api/test/list/all');
      },
      getOptions(url) {
        let formData = new FormData();
        let requestJson = JSON.stringify(this.test);

        formData.append('request', new Blob([requestJson], {
          type: "application/json"
        }));
        let jmx = this.test.toJMX();
        let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
        formData.append("files", new File([blob], jmx.name));

        return {
          method: 'POST',
          url: url,
          data: formData,
          headers: {
            'Content-Type': undefined
          }
        };
      },
      handleCommand(command) {
        switch (command) {
          case "report":
            this.$refs.reportDialog.open();
            break;
          case "performance":
            this.$store.commit('setTest', {
              projectId: this.test.projectId,
              name: this.test.name,
              jmx: this.test.toJMX()
            })
            this.$router.push({
              path: "/performance/test/create"
            })
            break;
          case "export":
            downloadFile(this.test.name + ".json", this.test.export());
            break;
          case "import":
            this.$refs.apiImport.open();
            break;
        }
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
        let url = '/api/schedule/create';
        if (param.id) {
          url = '/api/schedule/update';
        }
        this.$post(url, param, response => {
          this.$success(this.$t('commons.save_success'));
          this.getTest(this.test.id);
        });
      },
      checkScheduleEdit() {
        if (this.create) {
          this.$message(this.$t('api_test.environment.please_save_test'));
          return false;
        }
        return true;
      }
    },

    computed: {
      isShowRun() {
        return this.test.isValid() && !this.change;
      },
      isDisabled() {
        return !(this.test.isValid() && this.change)
      }
    },

    created() {
      this.init();
    }
  }
</script>

<style scoped>
  .test-container {
    height: calc(100vh - 150px);
    min-height: 600px;
  }

  .test-name {
    width: 600px;
    margin-left: -20px;
    margin-right: 20px;
  }

  .test-project {
    min-width: 150px;
  }

  .test-container .more {
    margin-left: 10px;
  }
</style>
