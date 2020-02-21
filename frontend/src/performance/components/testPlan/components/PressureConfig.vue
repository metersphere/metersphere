<template>
  <div class="pressure-config-container">
    <el-row>
      <el-col :span="10">
        <el-form :inline="true">
          <el-col :span="12">
            <el-form-item>
              <div class="config-form-label">并发用户数：</div>
            </el-form-item>
            <el-form-item>
              <el-input
                type="number"
                placeholder="请输入线程数"
                v-model="threadNumber"
                @click="convertProperty"
                show-word-limit
              >
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item>
              <div class="config-form-label">压测时长（分钟）：</div>
            </el-form-item>
            <el-form-item>
              <el-input
                type="number"
                placeholder="请输入时长"
                v-model="duration"
                @click="convertProperty"
                show-word-limit
              >
              </el-input>
            </el-form-item>
          </el-col>
          <el-row>
            <el-form-item>
              <el-col>
                <el-form-item>
                  <div class="config-form-label">RPS上限：</div>
                </el-form-item>
                <el-form-item>
                  <el-input
                    type="number"
                    placeholder="请输入限制"
                    v-model="rpsLimit"
                    @click="convertProperty"
                    show-word-limit
                  >
                  </el-input>
                </el-form-item>
              </el-col>
            </el-form-item>
          </el-row>
        </el-form>

        <el-form :inline="true" class="input-bottom-border">
          <el-form-item>
            <div>在</div>
          </el-form-item>
          <el-form-item>
            <el-input
              type="number"
              placeholder=""
              v-model="rampUpTime"
              @click="convertProperty"
              show-word-limit
            >
            </el-input>
          </el-form-item>
          <el-form-item>
            <div>分钟内，分</div>
          </el-form-item>
          <el-form-item>
            <el-input
              type="number"
              placeholder=""
              v-model="step"
              @click="convertProperty"
              show-word-limit
            >
            </el-input>
          </el-form-item>
          <el-form-item>
            <div>次增加并发用户</div>
          </el-form-item>
        </el-form>
      </el-col>
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
  .pressure-config-container .el-input {
    width: 130px;
  }

  .pressure-config-container .config-form-label {
    width: 130px;
  }

  .pressure-config-container .input-bottom-border input {
    border: 0;
    border-bottom: 1px solid #DCDFE6;
  }
</style>
