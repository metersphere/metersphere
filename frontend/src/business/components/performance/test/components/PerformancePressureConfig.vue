<template>
  <div v-loading="result.loading" class="pressure-config-container">
    <el-row>
      <el-col :span="10">
        <el-form :inline="true">
          <el-form-item>
            <div class="config-form-label">{{ $t('load_test.thread_num') }}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number
              :disabled="isReadOnly"
              :placeholder="$t('load_test.input_thread_num')"
              v-model="threadNumber"
              @change="calculateChart"
              :min="1"
              size="mini"/>
          </el-form-item>
        </el-form>
        <el-form :inline="true">
          <el-form-item>
            <div class="config-form-label">{{ $t('load_test.duration') }}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number
              :disabled="isReadOnly"
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
              <div class="config-form-label">{{ $t('load_test.rps_limit') }}</div>
            </el-form-item>
            <el-form-item>
              <el-switch v-model="rpsLimitEnable"/>
            </el-form-item>
            <el-form-item>
              <el-input-number
                :disabled="isReadOnly || !rpsLimitEnable"
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
            <div>{{ $t('load_test.ramp_up_time_within') }}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number
              :disabled="isReadOnly"
              placeholder=""
              :min="1"
              :max="duration"
              v-model="rampUpTime"
              @change="calculateChart"
              size="mini"/>
          </el-form-item>
          <el-form-item>
            <div>{{ $t('load_test.ramp_up_time_minutes') }}</div>
          </el-form-item>
          <el-form-item>
            <el-input-number
              :disabled="isReadOnly"
              placeholder=""
              :min="1"
              :max="Math.min(threadNumber, rampUpTime)"
              v-model="step"
              @change="calculateChart"
              size="mini"/>
          </el-form-item>
          <el-form-item>
            <div>{{ $t('load_test.ramp_up_time_times') }}</div>
          </el-form-item>
        </el-form>
        <el-form :inline="true" class="input-bottom-border">
          <el-form-item>
            <div>{{ $t('load_test.select_resource_pool') }}</div>
          </el-form-item>
          <el-form-item>
            <el-select v-model="resourcePool" :disabled="isReadOnly" size="mini">
              <el-option
                v-for="item in resourcePools"
                :key="item.id"
                :label="item.name"
                :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="14">
        <div class="title">{{ $t('load_test.pressure_prediction_chart') }}</div>
        <ms-chart class="chart-container" ref="chart1" :options="orgOptions" :autoresize="true"></ms-chart>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import echarts from "echarts";
import MsChart from "@/business/components/common/chart/MsChart";

const TARGET_LEVEL = "TargetLevel";
const RAMP_UP = "RampUp";
const STEPS = "Steps";
const DURATION = "duration";
const RPS_LIMIT = "rpsLimit";
const RPS_LIMIT_ENABLE = "rpsLimitEnable";

export default {
  name: "PerformancePressureConfig",
  components: {MsChart},
  props: {
    testPlan: {
      type: Object
    },
    testId: {
      type: String
    },
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      result: {},
      threadNumber: 10,
      duration: 10,
      rampUpTime: 10,
      step: 10,
      rpsLimit: 10,
      rpsLimitEnable: false,
      orgOptions: {},
      resourcePool: null,
      resourcePools: [],
    }
  },
  mounted() {
    if (this.testId) {
      this.getLoadConfig();
    } else {
      this.calculateChart();
    }
    this.resourcePool = this.testPlan.testResourcePoolId;
    this.getResourcePools();
  },
  watch: {
    testPlan(n) {
      this.resourcePool = n.testResourcePoolId;
    },
    testId() {
      if (this.testId) {
        this.getLoadConfig();
      } else {
        this.calculateChart();
      }
      this.getResourcePools();
    },
  },
  methods: {
    getResourcePools() {
      this.result = this.$get('/testresourcepool/list/quota/valid', response => {
        this.resourcePools = response.data;
        // 如果当前的资源池无效 设置 null
        if (response.data.filter(p => p.id === this.resourcePool).length === 0) {
          this.resourcePool = null;
        }
      })
    },
    getLoadConfig() {
      if (this.testId) {

        this.$get('/performance/get-load-config/' + this.testId, (response) => {
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
      }
    },
    calculateChart() {
      if (this.duration < this.rampUpTime) {
        this.rampUpTime = this.duration;
      }
      if (this.rampUpTime < this.step) {
        this.step = this.rampUpTime;
      }
      this.orgOptions = {
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: []
        },
        yAxis: {
          type: 'value'
        },
        tooltip: {
          trigger: 'axis',
          formatter: '{a}: {c0}',
          axisPointer: {
            lineStyle: {
              color: '#57617B'
            }
          }
        },
        series: [{
          name: 'User',
          data: [],
          type: 'line',
          step: 'start',
          smooth: false,
          symbolSize: 5,
          showSymbol: false,
          lineStyle: {
            normal: {
              width: 1
            }
          },
          areaStyle: {
            normal: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                offset: 0,
                color: 'rgba(137, 189, 27, 0.3)'
              }, {
                offset: 0.8,
                color: 'rgba(137, 189, 27, 0)'
              }], false),
              shadowColor: 'rgba(0, 0, 0, 0.1)',
              shadowBlur: 10
            }
          },
          itemStyle: {
            normal: {
              color: 'rgb(137,189,27)',
              borderColor: 'rgba(137,189,2,0.27)',
              borderWidth: 12
            }
          },
        }]
      };
      let timePeriod = Math.floor(this.rampUpTime / this.step);
      let timeInc = timePeriod;

      let threadPeriod = Math.floor(this.threadNumber / this.step);
      let threadInc1 = Math.floor(this.threadNumber / this.step);
      let threadInc2 = Math.ceil(this.threadNumber / this.step);
      let inc2count = this.threadNumber - this.step * threadInc1;
      for (let i = 0; i <= this.duration; i++) {
        // x 轴
        this.orgOptions.xAxis.data.push(i);
        if (i > timePeriod) {
          timePeriod += timeInc;
          if (inc2count > 0) {
            threadPeriod = threadPeriod + threadInc2;
            inc2count--;
          } else {
            threadPeriod = threadPeriod + threadInc1;
          }
          if (threadPeriod > this.threadNumber) {
            threadPeriod = this.threadNumber;
          }
          this.orgOptions.series[0].data.push(threadPeriod);
        } else {
          this.orgOptions.series[0].data.push(threadPeriod);
        }
      }
    },
    validConfig() {
      if (!this.resourcePool) {
        this.$warning(this.$t('load_test.resource_pool_is_null'));
        // 资源池为空，设置参数为资源池所在Tab的name
        this.$emit('changeActive', '1');
        return false;
      }

      if (!this.threadNumber || !this.duration || !this.rampUpTime || !this.step || !this.rpsLimit) {
        this.$warning(this.$t('load_test.pressure_config_params_is_empty'));
        this.$emit('changeActive', '1');
        return false;
      }

      return true;
    },
    convertProperty() {
      /// todo：下面4个属性是jmeter ConcurrencyThreadGroup plugin的属性，这种硬编码不太好吧，在哪能转换这种属性？
      return [
        {key: TARGET_LEVEL, value: this.threadNumber},
        {key: RAMP_UP, value: this.rampUpTime},
        {key: STEPS, value: this.step},
        {key: DURATION, value: this.duration},
        {key: RPS_LIMIT, value: this.rpsLimit},
        {key: RPS_LIMIT_ENABLE, value: this.rpsLimitEnable},
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

.el-col .el-form {
  margin-top: 15px;
  text-align: left;
}

.el-col {
  margin-top: 15px;
  text-align: left;
}

.title {
  margin-left: 60px;
}
</style>
