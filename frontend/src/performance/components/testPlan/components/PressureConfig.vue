<template>
  <div class="pressure-config-container">
    <el-row>
      <el-col :span="10">
        <el-form :inline="true">
          <el-col :span="12">
            <el-form-item>
              <div class="config-form-label">{{$t('load_test.thread_num')}}</div>
            </el-form-item>
            <el-form-item>
              <el-input
                type="number"
                :placeholder="$t('load_test.input_thread_num')"
                v-model="threadNumber"
                @click="convertProperty"
                show-word-limit
              >
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item>
              <div class="config-form-label">{{$t('load_test.duration')}}</div>
            </el-form-item>
            <el-form-item>
              <el-input
                type="number"
                :placeholder="$t('load_test.duration')"
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
                  <div class="config-form-label">{{$t('load_test.rps_limit')}}</div>
                </el-form-item>
                <el-form-item>
                  <el-input
                    type="number"
                    :placeholder="$t('load_test.input_rps_limit')"
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
            <div>{{$t('load_test.ramp_up_time_within')}}</div>
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
            <div>{{$t('load_test.ramp_up_time_minutes')}}</div>
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
            <div>{{$t('load_test.ramp_up_time_times')}}</div>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  export default {
    name: "MsTestPlanPressureConfig",
    data() {
      return {
        testPlan: {},
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
    watch: {
      testPlan() {
        this.convertProperty();
      }
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
