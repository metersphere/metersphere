<template>
  <ms-container>
    <ms-main-container>
      <el-card v-loading="result.loading">
        <el-row>
          <el-col :span="10">
            <el-input :placeholder="$t('load_test.input_name')" v-model="testPlan.name" class="input-with-select">
              <template v-slot:prepend>
                <el-select v-model="testPlan.projectId" :placeholder="$t('load_test.select_project')">
                  <el-option
                    v-for="item in projects"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id">
                  </el-option>
                </el-select>
              </template>
            </el-input>
          </el-col>
          <el-col :span="12" :offset="2">
            <el-button type="primary" plain @click="save">{{$t('commons.save')}}</el-button>
            <el-button type="primary" plain @click="saveAndRun">{{$t('load_test.save_and_run')}}</el-button>
            <el-button type="warning" plain @click="cancel">{{$t('commons.cancel')}}</el-button>
          </el-col>
        </el-row>


        <el-tabs class="testplan-config" v-model="active" type="border-card" :stretch="true">
          <el-tab-pane :label="$t('load_test.basic_config')">
            <performance-basic-config :test-plan="testPlan" ref="basicConfig"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('load_test.pressure_config')">
            <performance-pressure-config :test-plan="testPlan" ref="pressureConfig"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('load_test.advanced_config')" class="advanced-config">
            <performance-advanced-config ref="advancedConfig"/>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </ms-main-container>
  </ms-container>
</template>

<script>
  import PerformanceBasicConfig from "./components/PerformanceBasicConfig";
  import PerformancePressureConfig from "./components/PerformancePressureConfig";
  import PerformanceAdvancedConfig from "./components/PerformanceAdvancedConfig";
  import MsContainer from "../../common/components/MsContainer";
  import MsMainContainer from "../../common/components/MsMainContainer";

  export default {
    name: "EditPerformanceTestPlan",
    components: {
      PerformancePressureConfig,
      PerformanceBasicConfig,
      PerformanceAdvancedConfig,
      MsContainer,
      MsMainContainer
    },
    data() {
      return {
        result: {},
        testPlan: {},
        listProjectPath: "/project/listAll",
        savePath: "/performance/save",
        editPath: "/performance/edit",
        runPath: "/performance/run",
        projects: [],
        active: '0',
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
        }]
      }
    },
    watch: {
      '$route'(to) {
        // 如果是创建测试
        if (to.name === 'createPerTest') {
          window.location.reload();
          return;
        }

        if (to.name !== 'editPerTest') {
          return;
        }

        let testId = to.path.split('/')[4]; // find testId
        if (testId) {
          this.result = this.$get('/performance/get/' + testId, response => {
            if (response.data) {
              this.testPlan = response.data;
            }
          });
        }
      }

    },
    created() {
      let testId = this.$route.path.split('/')[4];
      if (testId) {
        this.result = this.$get('/performance/get/' + testId, response => {
          this.testPlan = response.data;
        });
      }

      this.listProjects();
    },
    mounted() {
      this.importAPITest();
    },
    methods: {
      importAPITest() {
        let apiTest = this.$store.state.api.test;
        if (apiTest && apiTest.name) {
          this.testPlan.projectId = apiTest.projectId;
          this.testPlan.name = apiTest.name;
          let blob = new Blob([apiTest.jmx.xml], {type: "application/octet-stream"});
          let file = new File([blob], apiTest.jmx.name);
          this.$refs.basicConfig.beforeUpload(file);
          this.$refs.basicConfig.handleUpload({file: file});
          this.active = '1';
          this.$store.commit("clearTest");
        }
      },
      listProjects() {
        this.result = this.$get(this.listProjectPath, response => {
          this.projects = response.data;
        })
      },
      save() {
        if (!this.validTestPlan()) {
          return;
        }

        let options = this.getSaveOption();

        this.result = this.$request(options, () => {
          this.$success(this.$t('commons.save_success'));
          this.$refs.advancedConfig.cancelAllEdit();
          this.$router.push({path: '/performance/test/all'})
        });
      },
      saveAndRun() {
        // if (!this.validTestPlan()) {
        //   return;
        // }

        let options = this.getSaveOption();

        this.result = this.$request(options, (response) => {
          this.testPlan.id = response.data;
          this.$success(this.$t('commons.save_success'));
          this.result = this.$post(this.runPath, {id: this.testPlan.id}, () => {
            this.$success(this.$t('load_test.is_running'))
            this.$router.push({path: '/performance/report/all'})
          })
        });
      },
      getSaveOption() {
        let formData = new FormData();
        let url = this.testPlan.id ? this.editPath : this.savePath;

        if (this.$refs.basicConfig.uploadList.length > 0) {
          this.$refs.basicConfig.uploadList.forEach(f => {
            formData.append("file", f);
          });
        }
        // 基本配置
        this.testPlan.updatedFileList = this.$refs.basicConfig.updatedFileList();
        // 压力配置
        this.testPlan.loadConfiguration = JSON.stringify(this.$refs.pressureConfig.convertProperty());
        this.testPlan.testResourcePoolId = this.$refs.pressureConfig.resourcePool;
        // 高级配置
        this.testPlan.advancedConfiguration = JSON.stringify(this.$refs.advancedConfig.configurations());

        // file属性不需要json化
        let requestJson = JSON.stringify(this.testPlan, function (key, value) {
          return key === "file" ? undefined : value
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
      cancel() {
        this.$router.push({path: '/performance/test/all'})
      },
      validTestPlan() {
        if (!this.testPlan.name) {
          this.$error(this.$t('load_test.test_name_is_null'));
          return false;
        }

        if (!this.testPlan.projectId) {
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
      }
    }
  }
</script>

<style scoped>

  .testplan-config {
    margin-top: 15px;
    text-align: center;
  }

  .el-select {
    min-width: 130px;
  }

  .edit-testplan-container .input-with-select .el-input-group__prepend {
    background-color: #fff;
  }

  .advanced-config {
    height: calc(100vh - 280px);
    overflow: auto;
  }
</style>
