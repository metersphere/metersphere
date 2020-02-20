<template>
  <div class="pressure-config-container">
    <el-row type="flex">
      <div class="small-input">
        <span>并发用户数：</span>
        <el-input
          type="number"
          placeholder="请输入线程数"
          v-model="threadNumber"
          @click="convertProperty"
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
          @click="convertProperty"
          show-word-limit
        >
        </el-input>
      </div>
      <div class="small-input">
        <span>在</span>
        <el-input
          type="number"
          placeholder=""
          v-model="rampUpTime"
          @click="convertProperty"
          show-word-limit
        >
        </el-input>
        <span>分钟内，分</span>
        <el-input
          type="number"
          placeholder=""
          v-model="step"
          @click="convertProperty"
          show-word-limit
        >
        </el-input>
        <span>次增加并发用户</span>
      </div>
      <div class="small-input">
        <span>RPS上限：</span>
        <el-input
          type="number"
          placeholder="请输入限制"
          v-model="rpsLimit"
          @click="convertProperty"
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
    props: ["testPlan"],
    data() {
      return {
        threadNumber: 2,
        duration: 3,
        rampUpTime: 12,
        step: 2,
        rpsLimit: 10,
      }
    },
    created() {
      this.testPlan.loadConfigurationObj = [];
      this.convertProperty();
    },
    methods: {
      convertProperty() {
        /// todo：下面4个属性是jmeter ConcurrencyThreadGroup plugin的属性，这种硬编码不太好吧，在哪能转换这种属性？
        this.testPlan.loadConfigurationObj = [
          {key: "TargetLevel", value: this.threadNumber},
          {key: "RampUp", value: this.rampUpTime},
          {key: "Steps", value: this.step},
          {key: "duration", value: this.duration},
          {key: "rpsLimit", value: this.rpsLimit}
        ];
      }
    }
  }
</script>

<style>
  .pressure-config-container .small-input {
    width: 150px;
  }
</style>
