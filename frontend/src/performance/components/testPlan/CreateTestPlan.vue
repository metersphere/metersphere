<template>
  <div class="testplan-container">
    <el-row>
      <el-col :span="10">
        <el-input placeholder="请输入名称" v-model="testplanName" class="input-with-select">
          <el-select v-model="project" slot="prepend" placeholder="请选择项目">
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
      <el-tab-pane
        v-for="item in tabs"
        :key="item.id"
        :label="item.title"
      >
        <component :is="active === item.id ? item.component : false"/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
  import BasicConfig from './components/BasicConfig';
  import PressureConfig from './components/PressureConfig';
  import AdvancedConfig from './components/AdvancedConfig';

  export default {
    name: "CreateTestPlan",
    components: {
      BasicConfig,
      PressureConfig,
      AdvancedConfig
    },
    data() {
      return {
        project: '',
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
        testplanName: '',
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
        window.console.log("save")

        /// todo: save
        this.$message({
          message: '保存成功！',
          type: 'success'
        });
      },
      saveAndRun() {
        window.console.log("saveAndRun")

        /// todo: saveAndRun
        this.$message({
          message: '保存成功，开始运行！',
          type: 'success'
        });
      },
      cancel() {
        this.$router.push({path: '/'})
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
