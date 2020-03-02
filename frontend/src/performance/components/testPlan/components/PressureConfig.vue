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
  const TARGET_LEVEL = "TargetLevel";
  const RAMP_UP = "RampUp";
  const STEPS = "Steps";
  const DURATION = "duration";
  const RPS_LIMIT = "rpsLimit";

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
      let testId = this.$route.path.split('/')[2];
      if (testId) {
        this.getLoadConfig(testId);
      }
    },
    watch: {
      '$route'(to) {
        let testId = to.path.split('/')[2];
        if (testId) {
          this.getLoadConfig(testId);
        }
      }
    },
    methods: {
      getLoadConfig(testId) {
        this.$get('/testplan/get-load-config/' + testId, (response) => {
          if (response.data) {
            let data = JSON.parse(response.data);

            data.forEach(d => {
              switch (d.key) {
                case TARGET_LEVEL:
                  this.threadNumber = d.value;
                  break;
                case RAMP_UP:
                  this.rampUpTime = d.value;
                  break;
                case DURATION:
                  this.duration = d.value;
                  break;
                case STEPS:
                  this.step = d.value;
                  break;
                case RPS_LIMIT:
                  this.rpsLimit = d.value;
                  break;
                default:
                  break;
              }
            });

            this.threadNumber = this.threadNumber || 10;
            this.duration = this.duration || 30;
            this.rampUpTime = this.rampUpTime || 12;
            this.step = this.step || 3;
            this.rpsLimit = this.rpsLimit || 10;

            this.calculateChart();
          }
        });
      },
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
          {key: TARGET_LEVEL, value: this.threadNumber},
          {key: RAMP_UP, value: this.rampUpTime},
          {key: STEPS, value: this.step},
          {key: DURATION, value: this.duration},
          {key: RPS_LIMIT, value: this.rpsLimit}
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
