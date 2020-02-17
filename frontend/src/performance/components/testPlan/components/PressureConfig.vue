<template>
  <div class="pressure-config-container">
    <el-row type="flex">
      <div class="small-input">
        <span>线程数：</span>
        <el-input
          type="number"
          placeholder="请输入线程数"
          v-model="threadNumber"
          show-word-limit
        >
        </el-input>
      </div>
      <div class="small-input">
        <span>压测时长（分钟）：</span>
        <el-input
          type="number"
          placeholder="请输入时长"
          v-model="duration"
          show-word-limit
        >
        </el-input>
      </div>
      <div class="small-input">
        <span>Ramp-Up时间（秒）</span>
        <el-input
          type="number"
          placeholder="请输入时间"
          v-model="rampUpTime"
          show-word-limit
        >
        </el-input>
      </div>
    </el-row>
  </div>
</template>

<script>
  export default {
    name: "TestPlanPressureConfig",
    data() {
      return {
        threadNumber: 10,
        duration: 10,
        rampUpTime: 5,
        chartData: {
          columns: ['压测时长（分钟）', '并发量'],
          rows: [
            {'压测时长（分钟）': '1月1日', '并发量': 123},
            {'压测时长（分钟）': '1月2日', '并发量': 1223},
            {'压测时长（分钟）': '1月3日', '并发量': 2123},
            {'压测时长（分钟）': '1月4日', '并发量': 4123},
            {'压测时长（分钟）': '1月5日', '并发量': 3123},
            {'压测时长（分钟）': '1月6日', '并发量': 7123}
          ]
        },
      }
    },
    beforeCreate() {

    },
    methods: {
    }
  }
</script>

<style>
  .pressure-config-container .small-input {
    width: 150px;
  }
</style>
