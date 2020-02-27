<template>
  <div class="edit-testplan-container" v-loading="result.loading">
    <div class="main-content">
      <el-card>
        <el-row>
          <el-col :span="10">
            <el-input placeholder="请输入名称" v-model="testPlan.name" class="input-with-select">
              <el-select v-model="testPlan.projectId" slot="prepend" placeholder="请选择项目">
                <el-option
                  v-for="item in projects"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-input>
          </el-col>
          <el-button type="primary" plain @click="save">保存</el-button>
          <el-button type="primary" plain @click="saveAndRun">保存并执行</el-button>
          <el-button type="warning" plain @click="cancel">取消</el-button>
        </el-row>

        <el-tabs v-model="active" type="border-card" :stretch="true">
          <el-tab-pane label="基础配置">
            <ms-test-plan-basic-config :test-plan="testPlan"/>
          </el-tab-pane>
          <el-tab-pane label="压力配置">
            <ms-test-plan-pressure-config :test-plan="testPlan"/>
          </el-tab-pane>
          <el-tab-pane label="高级配置">
            <ms-test-plan-advanced-config/>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script>
  import MsTestPlanBasicConfig from './components/BasicConfig';
  import MsTestPlanPressureConfig from './components/PressureConfig';
  import MsTestPlanAdvancedConfig from './components/AdvancedConfig';

  export default {
    name: "MsEditTestPlan",
    components: {
      MsTestPlanBasicConfig,
      MsTestPlanPressureConfig,
      MsTestPlanAdvancedConfig,
    },
    props: ['testPlanObj'],
    data() {
      return {
        result: {},
        testPlan: {},
        listProjectPath: "/project/listAll",
        savePath: "/testplan/save",
        editPath: "/testplan/edit",
        runPath: "/testplan/run",
        projects: [],
        active: '0',
        tabs: [{
          title: '场景配置',
          id: '0',
          component: 'BasicConfig'
        }, {
          title: '压力配置',
          id: '1',
          component: 'PressureConfig'
        }, {
          title: '高级配置',
          id: '2',
          component: 'AdvancedConfig'
        }]
      }
    },
    watch: {
      '$route'(to) {
        window.console.log(to);
        let testId = to.path.split('/')[2]; // find testId
        this.$get('/testplan/get/' + testId, response => {
          this.testPlan = response.data;
        });
      }

    },
    created() {
      let testId = this.$route.path.split('/')[2];
      window.console.log(testId);
      if (testId) {
        this.$get('/testplan/get/' + testId, response => {
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
            message: '保存成功！',
            type: 'success'
          });
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
            message: '保存成功！',
            type: 'success'
          });

          this.result = this.$post(this.runPath, {id: this.testPlan.id}).then(() => {
            this.$message({
              message: '正在运行！',
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
        if (this.testPlan.loadConfigurationObj) {
          this.testPlan.loadConfiguration = JSON.stringify(this.testPlan.loadConfigurationObj);
        }
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
        this.$router.push({path: '/'})
      },
      validTestPlan() {
        if (!this.testPlan.name) {
          this.$message({
            message: '测试名称不能为空！',
            type: 'error'
          });

          return false;
        }

        if (!this.testPlan.projectId) {
          this.$message({
            message: '项目不能为空！',
            type: 'error'
          });

          return false;
        }

        if (!this.testPlan.file) {
          this.$message({
            message: 'jmx文件不能为空！',
            type: 'error'
          });

          return false;
        }

        /// todo: 其他校验
        return true;
      }
    }
  }
</script>

<style>
  .edit-testplan-container {
    float: none;
    text-align: center;
    padding: 15px;
    width: 100%;
    height: 100%;
    box-sizing: border-box;
  }

  .main-content {
    margin: 0 auto;
    width: 100%;
    max-width: 1200px;
  }

  .edit-testplan-container .el-select .el-input {
    width: 130px;
  }

  .edit-testplan-container .input-with-select .el-input-group__prepend {
    background-color: #fff;
  }
</style>
