<template>
  <div class="pressure-config-container">
    <el-row>
      <el-col :span="12">
        <el-form :inline="true">
          <el-form-item>
            <div class="config-form-label">{{$t('load_test.thread_num')}}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number
              :placeholder="$t('load_test.input_thread_num')"
              v-model="threadNumber"
              @change="calculateChart"
              :min="1"
              size="mini"/>
          </el-form-item>
        </el-form>
        <el-form :inline="true">
          <el-form-item>
            <div class="config-form-label">{{$t('load_test.duration')}}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number
              :placeholder="$t('load_test.duration')"
              v-model="duration"
              :min="1"
              @change="calculateChart"
              size="mini"/>
          </el-form-item>
        </el-form>
        <el-form :inline="true">
          <el-form-item>
            <el-form-item>
              <div class="config-form-label">{{$t('load_test.rps_limit')}}</div>
            </el-form-item>
            <el-form-item>
              <el-input-number
                :placeholder="$t('load_test.input_rps_limit')"
                v-model="rpsLimit"
                @change="calculateChart"
                :min="1"
                size="mini"/>
            </el-form-item>
          </el-form-item>
        </el-form>

        <el-form :inline="true" class="input-bottom-border">
          <el-form-item>
            <div>{{$t('load_test.ramp_up_time_within')}}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number
              placeholder=""
              :min="1"
              v-model="rampUpTime"
              @change="calculateChart"
              size="mini"/>
          </el-form-item>
          <el-form-item>
            <div>{{$t('load_test.ramp_up_time_minutes')}}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number
              placeholder=""
              :min="1"
              v-model="step"
              @change="calculateChart"
              size="mini"/>
          </el-form-item>
          <el-form-item>
            <div>{{$t('load_test.ramp_up_time_times')}}</div>
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="12">
        压力预估图
        <chart ref="chart1" :options="orgOptions" :auto-resize="true"></chart>
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
        threadNumber: 10,
        duration: 30,
        rampUpTime: 12,
        step: 4,
        rpsLimit: 10,
        orgOptions: {},
      }
    },
    mounted() {
      this.testPlan.loadConfigurationObj = [];
      this.calculateChart();
      this.convertProperty();
    },
    watch: {
      testPlan() {
        this.convertProperty();
      }
    },
    methods: {
      calculateChart() {
        this.orgOptions = {
          xAxis: {
            type: 'category',
            data: []
          },
          yAxis: {
            type: 'value'
          },
          series: [{
            data: [],
            type: 'line',
          }]
        };
        let timePeriod = Math.ceil(this.rampUpTime / this.step);
        let threadPeriod = Math.ceil(this.threadNumber / this.step);
        let inc = threadPeriod;

        for (let i = 0; i < this.duration; i++) {
          // x 轴
          this.orgOptions.xAxis.data.push(i);
          if (i > timePeriod) {
            threadPeriod = threadPeriod + inc;
            timePeriod += timePeriod;
            if (threadPeriod > this.threadNumber) {
              threadPeriod = this.threadNumber;
            }
            this.orgOptions.series[0].data.push(threadPeriod);
          } else {
            this.orgOptions.series[0].data.push(threadPeriod);
          }
        }
      },
      convertProperty() {
        /// todo：下面4个属性是jmeter ConcurrencyThreadGroup plugin的属性，这种硬编码不太好吧，在哪能转换这种属性？
        return [
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

<style scoped>
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

  .chart-container {
    width: 100%;
  }
</style>
