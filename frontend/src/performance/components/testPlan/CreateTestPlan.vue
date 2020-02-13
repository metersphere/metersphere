<template>
  <div class="testplan-container">
    <el-row>
      <el-col :span="10">
        <el-input placeholder="请输入名称" v-model="testPlan.name" class="input-with-select">
          <el-select v-model="testPlan.project" slot="prepend" placeholder="请选择项目">
            <el-option
              v-for="item in projects"
              :key="item.id"
              :label="item.name"
              :value="item.name">
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
        <test-plan-basic-config v-on:change-test-plan="changeTestPlan"/>
      </el-tab-pane>
      <el-tab-pane label="压力配置">
        <test-plan-pressure-config v-on:change-test-plan="changeTestPlan"/>
      </el-tab-pane>
      <el-tab-pane label="高级配置">
        <test-plan-advanced-config v-on:change-test-plan="changeTestPlan"/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
  import TestPlanBasicConfig from './components/BasicConfig';
  import TestPlanPressureConfig from './components/PressureConfig';
  import TestPlanAdvancedConfig from './components/AdvancedConfig';

  export default {
    name: "CreateTestPlan",
    components: {
      TestPlanBasicConfig,
      TestPlanPressureConfig,
      TestPlanAdvancedConfig,
    },
    data() {
      return {
        savePath: "/testplan/save",
        testPlan: {},
        projects: [{
          id: '选项1',
          name: '黄金糕'
        }, {
          id: '选项2',
          name: '双皮奶'
        }, {
          id: '选项3',
          name: '蚵仔煎'
        }, {
          id: '选项4',
          name: '龙须面'
        }, {
          id: '选项5',
          name: '北京烤鸭'
        }],
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
    methods: {
      save() {
        if (!this.validTestPlan()) {
          return;
        }

        this.$post(this.savePath, this.testPlan).then(response => {
          if (response) {
            this.$message({
              message: '保存成功！',
              type: 'success'
            });
          }
        }).catch((response) => {
          this.$message.error(response.message);
        });
      },
      saveAndRun() {
        if (!this.validTestPlan()) {
          return;
        }

        /// todo: saveAndRun
        this.$message({
          message: '保存成功，开始运行！',
          type: 'success'
        });
      },
      cancel() {
        this.$router.push({path: '/'})
      },
      changeTestPlan(updateFunc) {
        updateFunc(this.testPlan);
        window.console.log(this.testPlan);
      },
      validTestPlan() {
        if (!this.testPlan.name) {
          this.$message({
            message: '测试名称不能为空！',
            type: 'error'
          });

          return false;
        }

        if (!this.testPlan.project) {
          this.$message({
            message: '项目不能为空！',
            type: 'error'
          });

          return false;
        }

        if (!this.testPlan.fileId) {
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
  .testplan-container .el-tabs__nav {
    float: none;
    text-align: center;
  }

  .testplan-container .el-select .el-input {
    width: 130px;
  }

  .testplan-container .input-with-select .el-input-group__prepend {
    background-color: #fff;
  }
</style>
