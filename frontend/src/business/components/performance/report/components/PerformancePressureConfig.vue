<template>
  <div v-loading="result.loading" class="pressure-config-container">
    <el-row>
      <el-col :span="12">

        <el-collapse v-model="activeNames" accordion>
          <el-collapse-item :name="index"
                            v-for="(threadGroup, index) in threadGroups.filter(th=>th.enabled === 'true' && th.deleted=='false')"
                            :key="index">
            <template slot="title">
              <el-row>
                <el-col :span="14">
                  <el-tooltip :content="threadGroup.attributes.testname" placement="top">
                    <div style="padding-right:20px; font-size: 16px;" class="wordwrap">
                      {{ threadGroup.attributes.testname }}
                    </div>
                  </el-tooltip>
                </el-col>
                <el-col :span="10">
                  <el-tag type="primary" size="mini" v-if="threadGroup.threadType === 'DURATION'">
                    {{ $t('load_test.thread_num') }}{{ threadGroup.threadNumber }},
                    {{ $t('load_test.duration') }}:
                    <span v-if="threadGroup.durationHours">
                      {{ threadGroup.durationHours }}{{ $t('schedule.cron.hours') }}
                    </span>
                    <span v-if="threadGroup.durationMinutes">
                      {{ threadGroup.durationMinutes }}{{ $t('schedule.cron.minutes') }}
                    </span>
                    <span v-if="threadGroup.durationSeconds">
                      {{ threadGroup.durationSeconds }}{{ $t('schedule.cron.seconds') }}
                    </span>
                  </el-tag>
                  <el-tag type="primary" size="mini" v-if="threadGroup.threadType === 'ITERATION'">
                    {{ $t('load_test.thread_num') }} {{ threadGroup.threadNumber }},
                    {{ $t('load_test.iterate_num') }} {{ threadGroup.iterateNum }}
                  </el-tag>
                </el-col>
              </el-row>
            </template>
            <el-form :inline="true" label-width="100px">
              <el-form-item :label="$t('load_test.thread_num')">
                <el-input-number
                  :disabled="true"
                  :placeholder="$t('load_test.input_thread_num')"
                  v-model="threadGroup.threadNumber"
                  :min="1"
                  size="mini"/>
              </el-form-item>
              <br>
              <el-form-item label="执行方式">
                <el-radio-group v-model="threadGroup.threadType" :disabled="true">
                  <el-radio-button label="DURATION">{{ $t('load_test.by_duration') }}</el-radio-button>
                  <el-radio-button label="ITERATION">{{ $t('load_test.by_iteration') }}</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <br>
              <div v-if="threadGroup.threadType === 'DURATION'">
                <el-form-item :label="$t('load_test.duration')">
                  <el-input-number controls-position="right"
                                   :disabled="true"
                                   v-model="threadGroup.durationHours"
                                   :min="0"
                                   :max="9999"
                                   @change="calculateTotalChart()"
                                   size="mini"/>
                </el-form-item>
                <el-form-item :label="$t('schedule.cron.hours')" label-width="40px"/>
                <el-form-item>
                  <el-input-number controls-position="right"
                                   :disabled="true"
                                   v-model="threadGroup.durationMinutes"
                                   :min="0"
                                   :max="59"
                                   @change="calculateTotalChart()"
                                   size="mini"/>
                </el-form-item>
                <el-form-item :label="$t('schedule.cron.minutes')" label-width="40px"/>
                <el-form-item>
                  <el-input-number controls-position="right"
                                   :disabled="true"
                                   v-model="threadGroup.durationSeconds"
                                   :min="0"
                                   :max="59"
                                   @change="calculateTotalChart()"
                                   size="mini"/>
                </el-form-item>
                <el-form-item :label="$t('schedule.cron.seconds')" label-width="20px"/>
                <br>
                <el-form-item :label="$t('load_test.rps_limit_enable')">
                  <el-switch v-model="threadGroup.rpsLimitEnable" @change="calculateTotalChart()"/>
                </el-form-item>
                <el-form-item :label="$t('load_test.rps_limit')">
                  <el-input-number controls-position="right"
                                   :disabled="true || !threadGroup.rpsLimitEnable"
                                   v-model="threadGroup.rpsLimit"
                                   @change="calculateTotalChart()"
                                   :min="1"
                                   :max="99999"
                                   size="mini"/>
                </el-form-item>
                <div v-if="threadGroup.tgType === 'com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup'">
                  <el-form-item label="Ramp-Up">
                    <el-input-number controls-position="right"
                                     :disabled="true"
                                     :min="1"

                                     :max="getDuration(threadGroup)"
                                     v-model="threadGroup.rampUpTime"
                                     @change="calculateTotalChart()"
                                     size="mini"/>
                  </el-form-item>
                  <el-form-item label="Step" label-width="50px">
                    <el-input-number controls-position="right"
                                     :disabled="true"
                                     :min="1"
                                     :max="Math.min(threadGroup.threadNumber, threadGroup.rampUpTime)"
                                     v-model="threadGroup.step"
                                     @change="calculateTotalChart()"
                                     size="mini"/>
                  </el-form-item>
                </div>

                <div v-if="threadGroup.tgType === 'ThreadGroup'">
                  <el-form-item label="Ramp-Up">
                    <el-input-number controls-position="right"
                                     :disabled="true"

                                     :min="1"
                                     :max="getDuration(threadGroup)"
                                     v-model="threadGroup.rampUpTime"
                                     @change="calculateTotalChart()"
                                     size="mini"/>
                  </el-form-item>
                </div>
              </div>
              <div v-if="threadGroup.threadType === 'ITERATION'">
                <el-form-item :label="$t('load_test.iterate_num')">
                  <el-input-number controls-position="right"
                                   :disabled="true"
                                   v-model="threadGroup.iterateNum"
                                   :min="1"
                                   :max="9999999"
                                   @change="calculateTotalChart()"
                                   size="mini"/>
                </el-form-item>
                <br>
                <el-form-item :label="$t('load_test.rps_limit_enable')">
                  <el-switch v-model="threadGroup.rpsLimitEnable" @change="calculateTotalChart()"/>
                </el-form-item>
                <el-form-item :label="$t('load_test.rps_limit')">
                  <el-input-number controls-position="right"
                                   :disabled="true || !threadGroup.rpsLimitEnable"
                                   v-model="threadGroup.rpsLimit"
                                   :min="1"
                                   :max="99999"
                                   size="mini"/>
                </el-form-item>
                <br>
                <el-form-item label="Ramp-Up">
                  <el-input-number controls-position="right"
                                   :disabled="true"
                                   :min="1"
                                   v-model="threadGroup.iterateRampUp"
                                   size="mini"/>
                </el-form-item>
              </div>
              <div v-if="resourcePoolType === 'NODE'">
                <el-form-item :label="$t('load_test.resource_strategy')">
                  <el-radio-group v-model="threadGroup.strategy" :disabled="true" size="mini">
                    <el-radio-button label="auto">{{ $t('load_test.auto_ratio') }}</el-radio-button>
                    <el-radio-button label="specify">{{ $t('load_test.specify_resource') }}</el-radio-button>
                    <el-radio-button label="custom">{{ $t('load_test.custom_ratio') }}</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <div v-if="threadGroup.strategy === 'auto'"></div>
                <div v-else-if="threadGroup.strategy === 'specify'">
                  <el-form-item :label="$t('load_test.specify_resource')">
                    <el-select v-model="threadGroup.resourceNodeIndex" :disabled="true" size="mini">
                      <el-option
                        v-for="(node, index) in resourceNodes"
                        :key="node.ip"
                        :label="node.ip"
                        :value="index">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </div>
                <div v-else>
                  <el-table :data="threadGroup.resourceNodes" :max-height="200">
                    <el-table-column type="index" width="50"/>
                    <el-table-column prop="ip" label="IP"/>
                    <el-table-column prop="maxConcurrency" :label="$t('test_resource_pool.max_threads')"/>
                    <el-table-column prop="ratio" :label="$t('test_track.home.percentage')">
                      <template v-slot:default="{row}">
                        <el-input-number size="small" v-model="row.ratio" :min="0" :step=".1"
                                         :max="1"></el-input-number>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </el-form>
          </el-collapse-item>
        </el-collapse>
      </el-col>
      <el-col :span="12">
        <ms-chart class="chart-container" ref="chart1" :options="options" :autoresize="true"></ms-chart>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import MsChart from "@/business/components/common/chart/MsChart";
import {findThreadGroup} from "@/business/components/performance/test/model/ThreadGroup";
import {
  getOldPerformanceJmxContent,
  getPerformanceJmxContent,
  getPerformanceLoadConfig,
  getPerformanceReport,
  getShareOldPerformanceJmxContent,
  getSharePerformanceJmxContent,
  getSharePerformanceLoadConfig,
  getSharePerformanceReport
} from "@/network/load-test";

const HANDLER = "handler";
const THREAD_GROUP_TYPE = "tgType";
const TARGET_LEVEL = "TargetLevel";
const RAMP_UP = "RampUp";
const STEPS = "Steps";
const DURATION = "duration";
const DURATION_HOURS = "durationHours";
const DURATION_MINUTES = "durationMinutes";
const DURATION_SECONDS = "durationSeconds";
const UNIT = "unit";
const RPS_LIMIT = "rpsLimit";
const RPS_LIMIT_ENABLE = "rpsLimitEnable";
const THREAD_TYPE = "threadType";
const ITERATE_NUM = "iterateNum";
const ITERATE_RAMP_UP = "iterateRampUpTime";
const ENABLED = "enabled";
const DELETED = "deleted";
const STRATEGY = "strategy";
const RESOURCE_NODE_INDEX = "resourceNodeIndex";
const RATIOS = "ratios";


const hexToRgb = function (hex) {
  return 'rgb(' + parseInt('0x' + hex.slice(1, 3)) + ',' + parseInt('0x' + hex.slice(3, 5))
    + ',' + parseInt('0x' + hex.slice(5, 7)) + ')';
};

export default {
  name: "MsPerformancePressureConfig",
  components: {MsChart},
  props: ['report', 'isShare', 'shareId', 'planReportTemplate'],
  data() {
    return {
      result: {},
      threadNumber: 0,
      duration: 0,
      rampUpTime: 0,
      step: 0,
      rpsLimit: 0,
      rpsLimitEnable: false,
      options: {},
      resourcePool: null,
      resourcePools: [],
      activeNames: ["0"],
      threadGroups: [],
      resourcePoolType: null,
      resourceNodes: [],
    };
  },
  activated() {
    this.getJmxContent();
  },
  created() {
    this.getResourcePools();
  },
  methods: {
    getResourcePools() {
      this.result = this.$get('/testresourcepool/list/quota/valid', response => {
        this.resourcePools = response.data;

        this.resourcePoolChange();
      });
    },
    resourcePoolChange() {
      let result = this.resourcePools.filter(p => p.id === this.report.testResourcePoolId);
      if (result.length === 1) {
        let threadNumber = 0;
        this.resourceNodes = [];
        this.resourcePoolType = result[0].type;
        result[0].resources.forEach(resource => {
          let config = JSON.parse(resource.configuration);
          threadNumber += config.maxConcurrency;
          this.resourceNodes.push(config);
        });
        this.threadGroups.forEach(tg => {
          let tgRatios = tg.ratios;
          let resourceNodes = JSON.parse(JSON.stringify(this.resourceNodes));
          let ratios = resourceNodes.map(n => n.maxConcurrency).reduce((total, curr) => {
            total += curr;
            return total;
          }, 0);
          let preSum = 0;
          for (let i = 0; i < resourceNodes.length; i++) {
            let n = resourceNodes[i];
            if (resourceNodes.length === tgRatios.length) {
              n.ratio = tgRatios[i];
              continue;
            }

            if (i === resourceNodes.length - 1) {
              n.ratio = (1 - preSum).toFixed(2);
            } else {
              n.ratio = (n.maxConcurrency / ratios).toFixed(2);
              preSum += Number.parseFloat(n.ratio);
            }
          }
          this.$set(tg, "resourceNodes", resourceNodes);
          if (tg.resourceNodeIndex > resourceNodes.length - 1) {
            this.$set(tg, "resourceNodeIndex", 0);
          }
        });
        this.calculateTotalChart();
      }
    },
    calculateLoadConfiguration: function (data) {
      for (let i = 0; i < this.threadGroups.length; i++) {
        let d = data[i];
        if (!d) {
          return;
        }
        d.forEach(item => {
          switch (item.key) {
            case TARGET_LEVEL:
              this.threadGroups[i].threadNumber = item.value;
              break;
            case RAMP_UP:
              this.threadGroups[i].rampUpTime = item.value;
              break;
            case ITERATE_RAMP_UP:
              this.threadGroups[i].iterateRampUp = item.value;
              break;
            case DURATION:
              this.threadGroups[i].duration = item.value;
              break;
            case DURATION_HOURS:
              this.threadGroups[i].durationHours = item.value;
              break;
            case DURATION_MINUTES:
              this.threadGroups[i].durationMinutes = item.value;
              break;
            case DURATION_SECONDS:
              this.threadGroups[i].durationSeconds = item.value;
              break;
            case UNIT:
              this.threadGroups[i].unit = item.value;
              break;
            case STEPS:
              this.threadGroups[i].step = item.value;
              break;
            case RPS_LIMIT:
              this.threadGroups[i].rpsLimit = item.value;
              break;
            case RPS_LIMIT_ENABLE:
              this.threadGroups[i].rpsLimitEnable = item.value;
              break;
            case THREAD_TYPE:
              this.threadGroups[i].threadType = item.value;
              break;
            case ITERATE_NUM:
              this.threadGroups[i].iterateNum = item.value;
              break;
            case ENABLED:
              this.threadGroups[i].enabled = item.value;
              break;
            case DELETED:
              this.threadGroups[i].deleted = item.value;
              break;
            case HANDLER:
              this.threadGroups[i].handler = item.value;
              break;
            case THREAD_GROUP_TYPE:
              this.threadGroups[i].tgType = item.value;
              break;
            case STRATEGY:
              this.threadGroups[i].strategy = item.value;
              break;
            case RESOURCE_NODE_INDEX:
              this.threadGroups[i].resourceNodeIndex = item.value;
              break;
            case RATIOS:
              this.threadGroups[i].ratios = item.value;
              break;
            default:
              break;
          }
        });
        for (let i = 0; i < this.threadGroups.length; i++) {
          let tg = this.threadGroups[i];
          tg.durationHours = Math.floor(tg.duration / 3600);
          tg.durationMinutes = Math.floor((tg.duration / 60 % 60));
          tg.durationSeconds = Math.floor((tg.duration % 60));
        }

        this.resourcePoolChange();
        this.calculateTotalChart();
      }
    },
    getLoadConfig() {
      if (!this.report.id && !this.planReportTemplate) {
        return;
      }
      if (this.planReportTemplate) {
        this.handleGetLoadConfig(this.planReportTemplate);
      } else if (this.isShare) {
        this.result = getSharePerformanceReport(this.shareId, this.report.id, data => {
          this.handleGetLoadConfig(data);
        });
      } else {
        this.result = getPerformanceReport(this.report.id, data => {
          this.handleGetLoadConfig(data);
        });
      }
    },
    handleGetLoadConfig(data) {
      if (data) {
        if (data.loadConfiguration) {
          let d = JSON.parse(data.loadConfiguration);
          this.calculateLoadConfiguration(d);
        } else {
          if (this.planReportTemplate) {
            if (this.planReportTemplate.loadConfig) {
              let data = JSON.parse(this.planReportTemplate.fixLoadConfiguration);
              this.calculateLoadConfiguration(data);
            }
          } else if (this.isShare) {
            this.result = getSharePerformanceLoadConfig(this.shareId, this.report.id, data => {
              if (data) {
                data = JSON.parse(data);
                this.calculateLoadConfiguration(data);
              }
            });
          } else {
            this.result = getPerformanceLoadConfig(this.report.id, data => {
              if (data) {
                data = JSON.parse(data);
                this.calculateLoadConfiguration(data);
              }
            });
          }
        }
      } else {
        this.$error(this.$t('report.not_exist'));
      }
    },
    getJmxContent() {
      // console.log(this.report.testId);
      if (!this.report.testId && !this.planReportTemplate) {
        return;
      }
      if (this.planReportTemplate) {
        if (this.planReportTemplate.jmxContent) {
          this.handleGetJmxContent(JSON.parse(this.planReportTemplate.jmxContent));
        }
      } else if (this.isShare) {
        this.result = getSharePerformanceJmxContent(this.shareId, this.report.id, data => {
          this.handleGetJmxContent(data);
        });
      } else {
        this.result = getPerformanceJmxContent(this.report.id, data => {
          this.handleGetJmxContent(data);
        });
      }
    },
    handleGetJmxContent(d) {
      if (!d) {
        return;
      }
      let threadGroups = [];
      threadGroups = threadGroups.concat(findThreadGroup(d.jmx, d.name));
      threadGroups.forEach(tg => {
        tg.options = {};
      });
      this.threadGroups = threadGroups;
      this.getLoadConfig();

      // 兼容数据
      if (!threadGroups || threadGroups.length === 0) {
        if (this.planReportTemplate) {
          this.planReportTemplate.fixJmxContent.forEach(d => this.handleGetOldJmxContent(d, threadGroups));
        } else if (this.isShare) {
          this.result = getShareOldPerformanceJmxContent(this.shareId, this.report.testId, data => {
            data.forEach(d => this.handleGetOldJmxContent(d, threadGroups));
          });
        } else {
          this.result = getOldPerformanceJmxContent(this.report.testId, data => {
            data.forEach(d => this.handleGetOldJmxContent(d, threadGroups));
          });
        }
      }
    },
    handleGetOldJmxContent(d, threadGroups) {
      threadGroups = threadGroups.concat(findThreadGroup(d.jmx, d.name));
      threadGroups.forEach(tg => {
        tg.options = {};
      });
      this.threadGroups = threadGroups;
      this.getLoadConfig();
    },
    calculateTotalChart() {
      let handler = this;
      if (handler.duration < handler.rampUpTime) {
        handler.rampUpTime = handler.duration;
      }
      if (handler.rampUpTime < handler.step) {
        handler.step = handler.rampUpTime;
      }
      let color = ['#60acfc', '#32d3eb', '#5bc49f', '#feb64d', '#ff7c7c', '#9287e7', '#ca8622', '#bda29a', '#6e7074', '#546570', '#c4ccd3'];
      handler.options = {
        color: color,
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
        },
        series: []
      };


      for (let i = 0; i < handler.threadGroups.length; i++) {
        if (handler.threadGroups[i].enabled === 'false' ||
          handler.threadGroups[i].deleted === 'true' ||
          handler.threadGroups[i].threadType === 'ITERATION') {
          continue;
        }
        let seriesData = {
          name: handler.threadGroups[i].attributes.testname,
          data: [],
          type: 'line',
          step: 'start',
          smooth: false,
          symbolSize: 5,
          showSymbol: false,
          lineStyle: {
            width: 1
          },
          itemStyle: {
            color: hexToRgb(color[i % color.length]),
            borderColor: 'rgba(137,189,2,0.27)',
            borderWidth: 12
          },
        };

        let tg = handler.threadGroups[i];

        let timePeriod = Math.floor(tg.rampUpTime / tg.step);
        let timeInc = timePeriod;

        let threadPeriod = Math.floor(tg.threadNumber / tg.step);
        let threadInc1 = Math.floor(tg.threadNumber / tg.step);
        let threadInc2 = Math.ceil(tg.threadNumber / tg.step);
        let inc2count = tg.threadNumber - tg.step * threadInc1;

        let times = 1;
        switch (tg.unit) {
          case 'M':
            times *= 60;
            break;
          case 'H':
            times *= 3600;
            break;
          default:
            break;
        }
        let duration = tg.duration * times;
        for (let j = 0; j <= duration; j++) {
          // x 轴
          let xAxis = handler.options.xAxis.data;
          if (xAxis.indexOf(j) < 0) {
            xAxis.push(j);
          }

          if (tg.tgType === 'ThreadGroup') {
            seriesData.step = undefined;

            if (j === 0) {
              seriesData.data.push([0, 0]);
            }
            if (j >= tg.rampUpTime) {
              xAxis.push(duration);

              seriesData.data.push([j, tg.threadNumber]);
              seriesData.data.push([duration, tg.threadNumber]);
              break;
            }
          } else {
            seriesData.step = 'start';
            if (j > timePeriod) {
              timePeriod += timeInc;
              if (inc2count > 0) {
                threadPeriod = threadPeriod + threadInc2;
                inc2count--;
              } else {
                threadPeriod = threadPeriod + threadInc1;
              }
              if (threadPeriod > tg.threadNumber) {
                threadPeriod = tg.threadNumber;
                // 预热结束
                xAxis.push(duration);
                seriesData.data.push([duration, threadPeriod]);
                break;
              }
            }
            seriesData.data.push([j, threadPeriod]);
          }
        }
        handler.options.series.push(seriesData);
      }
    },
    getDuration(tg) {
      tg.duration = tg.durationHours * 60 * 60 + tg.durationMinutes * 60 + tg.durationSeconds;
      return tg.duration;
    },
  },
  watch: {
    report: {
      handler() {
        this.getJmxContent();
      },
      deep: true
    },
    planReportTemplate: {
      handler() {
        if (this.planReportTemplate) {
          this.getJmxContent();
        }
      },
      deep: true
    }
  }
};
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
  height: 300px;
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

.wordwrap {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
