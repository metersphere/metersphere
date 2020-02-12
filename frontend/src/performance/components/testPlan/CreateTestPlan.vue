<template>
  <div class="testplan-container">
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
    data () {
      return {
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
    }
  }
</script>

<style>
  .testplan-container .el-tabs__nav {
    float: none;
    text-align: center;
  }
</style>
