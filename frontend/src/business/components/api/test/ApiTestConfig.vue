<template>
  <div class="container">
    <div class="main-content">
      <el-card>
        <el-container class="test-container" v-loading="result.loading">
          <el-header>
            <el-row type="flex" align="middle">
              <el-input class="test-name" v-model="test.name" maxlength="60" :placeholder="$t('api_test.input_name')"
                        show-word-limit>
                <el-select class="test-project" v-model="test.projectId" slot="prepend"
                           :placeholder="$t('api_test.select_project')">
                  <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id"/>
                </el-select>
              </el-input>

              <el-button type="primary" plain :disabled="isDisabled" @click="saveTest">
                {{$t('commons.save')}}
              </el-button>

              <el-button type="primary" plain v-if="!isShowRun" :disabled="isDisabled" @click="saveRunTest">
                {{$t('load_test.save_and_run')}}
              </el-button>

              <el-button type="primary" plain v-if="isShowRun" @click="runTest">
                {{$t('api_test.run')}}
              </el-button>

              <el-button type="warning" plain @click="cancel">{{$t('commons.cancel')}}</el-button>

              <el-dropdown trigger="click" @command="handleCommand">
                <el-button class="el-dropdown-link more" icon="el-icon-more" plain/>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="report" :disabled="test.status !== 'Completed'">
                    {{$t('api_report.title')}}
                  </el-dropdown-item>
                  <el-dropdown-item command="performance" :disabled="create">
                    {{$t('api_test.create_performance_test')}}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>

              <ms-api-report-dialog :test-id="id" ref="reportDialog"/>
            </el-row>
          </el-header>
          <ms-api-scenario-config :scenarios="test.scenarioDefinition" ref="config"/>
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

  export default {
    name: "MsApiTestConfig",

    components: {MsApiReportDialog, MsApiReportStatus, MsApiScenarioConfig},

    props: ["id"],

    data() {
      return {
        reportVisible: false,
        create: false,
        result: {},
        projects: [],
        change: false,
        test: new Test()
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
        this.result = this.$post("/api/run", {id: this.test.id}, (response) => {
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
        let request = {
          id: this.test.id,
          projectId: this.test.projectId,
          name: this.test.name,
          scenarioDefinition: JSON.stringify(this.test.scenarioDefinition)
        }
        let requestJson = JSON.stringify(request);

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
        }
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
