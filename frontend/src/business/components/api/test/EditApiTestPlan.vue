<template>
  <div class="edit-testplan-container" >
    <div class="main-content">
      <el-card>
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
          <el-button type="primary" plain @click="save">{{$t('commons.save')}}</el-button>
          <el-button type="primary" plain @click="saveAndRun">{{$t('load_test.save_and_run')}}</el-button>
          <el-button type="warning" plain @click="cancel">{{$t('commons.cancel')}}</el-button>
        </el-row>

        <el-tabs class="testplan-config" v-model="active" type="border-card" :stretch="true">
          <el-tab-pane :label="$t('load_test.basic_config')">
            <api-test-scene-config :test-plan="testPlan" />
          </el-tab-pane>
          <el-tab-pane :label="$t('load_test.runtime_config')">
            <api-test-runtime-config :test-plan="testPlan" ref="runtimeConfig"/>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script>
  import ApiTestSceneConfig from './components/ApiTestSceneConfig';
  import ApiTestRuntimeConfig from './components/ApiTestRuntimeConfig';

  export default {
    name: "EditApiTestPlan",
    components: {
      ApiTestSceneConfig,
      ApiTestRuntimeConfig,
    },
    data() {
      return {
        result: {},
        testPlan: {},
        listProjectPath: "/project/listAll",
        savePath: "/api/save",
        editPath: "/api/edit",
        runPath: "/api/run",
        projects: [],
        active: '0',
        tabs: [{
          title: this.$t('load_test.basic_config'),
          id: '0',
          component: 'ApiTestSceneConfig'
        }, {
          title: this.$t('load_test.runtime_config'),
          id: '1',
          component: 'ApiTestRuntimeConfig'
        }]
      }
    },
    watch: {
      '$route'(to) {
        // 如果是创建测试
        if (to.name === 'createFucTest') {
          window.location.reload();
          return;
        }
        let testId = to.path.split('/')[4]; // find testId
        if (testId) {
          this.$get('/api/get/' + testId, response => {
            this.testPlan = response.data;
          });
        }
      }

    },
    created() {
      let testId = this.$route.path.split('/')[4];
      if (testId) {
        this.$get('/api/get/' + testId, response => {
          this.testPlan = response.data;
        });
      }

      this.listProjects();
    },
    methods: {
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
          this.$message({
            message: this.$t('commons.save_success'),
            type: 'success'
          });
          this.$refs.runtimeConfig.cancelAllEdit();
          this.$router.push({path: '/api/test/all'})
        });
      },
      saveAndRun() {
        if (!this.validTestPlan()) {
          return;
        }

        let options = this.getSaveOption();

        this.result = this.$request(options, (response) => {
          this.testPlan.id = response.data;
          this.$message({
            message: this.$t('commons.save_success'),
            type: 'success'
          });

          this.result = this.$post(this.runPath, {id: this.testPlan.id}, () => {
            this.$message({
              message: this.$t('load_test.is_running'),
              type: 'success'
            });
          })
        });
      },
      getSaveOption() {
        let formData = new FormData();
        let url = this.testPlan.id ? this.editPath : this.savePath;

        if (!this.testPlan.file.id) {
          formData.append("file", this.testPlan.file);
        }

        this.testPlan.runtimeConfiguration = JSON.stringify(this.$refs.runtimeConfig.configurations());

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
        this.$router.push({path: '/api/test/all'})
      },
      validTestPlan() {
        if (!this.testPlan.name) {
          this.$message({
            message: this.$t('load_test.test_name_is_null'),
            type: 'error'
          });

          return false;
        }

        if (!this.testPlan.projectId) {
          this.$message({
            message: this.$t('load_test.project_is_null'),
            type: 'error'
          });

          return false;
        }

        if (!this.testPlan.file) {
          this.$message({
            message: this.$t('load_test.jmx_is_null'),
            type: 'error'
          });

          return false;
        }

        if (!this.$refs.runtimeConfig.validConfig()) {
          return false;
        }

        /// todo: 其他校验

        return true;
      }
    }
  }
</script>

<style scoped>
  .edit-testplan-container {
    float: none;
    text-align: center;
    padding: 15px;
    width: 100%;
    height: 100%;
    box-sizing: border-box;
  }

  .edit-testplan-container .main-content {
    margin: 0 auto;
    width: 100%;
    max-width: 1200px;
  }

  .edit-testplan-container .testplan-config {
    margin-top: 15px;
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
