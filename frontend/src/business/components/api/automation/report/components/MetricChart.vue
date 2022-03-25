<template>
  <div class="metric-container">
    <el-row type="flex" align="middle">
      <div style="width: 50%">
        <el-row type="flex" justify="center" align="middle">
          <el-row>
            <div class="metric-time">
              <div class="value" style="margin-right: 50px">{{ time }}</div>
            </div>
          </el-row>

          <div v-if="isExport">
             <span class="ms-req ms-req-error" v-if="content.error && content.error>0">
                <span class="ms-req-span"> {{ content.success + content.error }} 请求</span>
           </span>
            <span class="ms-req ms-req-success" v-else>
                <span class="ms-req-span">  {{ content.success ? content.success + content.error : 0 }} 请求</span>
           </span>
          </div>
          <ms-chart id="chart" ref="chart" :options="options" :height="220" style="margin-right: 10px"
                    :autoresize="true" v-else/>
          <el-row type="flex" justify="center" align="middle" style="width: 150px">
            <div>
              <div class="metric-icon-box" style="height: 26px">
                <span class="ms-point-success" style="margin: 7px;float: left;"/>
                <div class="metric-box">
                  <div class="value" style="font-size: 12px">{{ content.success }} {{ $t('api_report.success') }}</div>
                </div>
              </div>
              <el-divider></el-divider>
              <div class="metric-icon-box" style="height: 26px">
                <span class="ms-point-error" style="margin: 7px;float: left;"/>
                <div class="metric-box">
                  <div class="value" style="font-size: 12px">{{ content.error }} {{ $t('api_report.fail') }}</div>
                </div>
              </div>
              <el-divider v-if="content.errorCode > 0"></el-divider>
              <div class="metric-icon-box" v-if="content.errorCode > 0" style="height: 26px">
                <span class="ms-point-error-code" style="margin: 7px;float: left;"/>
                <div class="metric-box" v-if="content.errorCode > 0">
                  <div class="value" style="font-size: 12px">{{ content.errorCode }}
                    {{ $t('error_report_library.option.name') }}
                  </div>
                </div>
              </div>
              <el-divider ></el-divider>
              <div class="metric-icon-box" style="height: 26px">
                <span class="ms-point-unexecute" style="margin: 7px;float: left;"/>
                <div class="metric-box" >
                  <div class="value" style="font-size: 12px">{{ content.unExecute }}
                    {{ $t('api_test.home_page.detail_card.unexecute') }}
                  </div>
                </div>
              </div>
            </div>

          </el-row>
        </el-row>
      </div>
      <div class="split"></div>
      <!-- 场景统计 -->
      <div style="width: 50%">
        <el-row type="flex" justify="center" align="middle" v-if="report.reportType !== 'API_INTEGRATED'">
          <div class="metric-box">
            <div class="value">{{ content.scenarioTotal ? content.scenarioTotal : 0 }}</div>
            <div class="name">{{ $t('api_test.scenario.scenario') }}</div>
          </div>
          <span class="ms-point-success"/>
          <div class="metric-box">
            <div class="value">{{ content.scenarioSuccess ? content.scenarioSuccess : 0 }}</div>
            <div class="name">{{ $t('api_report.success') }}</div>
          </div>
          <span class="ms-point-error"/>
          <div class="metric-box">
            <div class="value">{{ content.scenarioError ? content.scenarioError : 0 }}</div>
            <div class="name">{{ $t('api_report.fail') }}</div>
          </div>
          <span class="ms-point-error-code"
                v-if="content.scenarioErrorReport > 0 || content.scenarioStepErrorReport > 0 "/>
          <div class="metric-box" v-if="content.scenarioErrorReport > 0 || content.scenarioStepErrorReport > 0 ">
            <div class="value">{{ content.scenarioErrorReport ? content.scenarioErrorReport : 0 }}</div>
            <div class="name">{{ $t('error_report_library.option.name') }}</div>
          </div>
          <span class="ms-point-unexecute"/>
          <div class="metric-box">
            <div class="value">{{ content.scenarioUnExecute ? content.scenarioUnExecute : 0 }}</div>
            <div class="name">{{ $t('api_test.home_page.detail_card.unexecute') }}</div>
          </div>
        </el-row>
        <el-divider v-if="report.reportType !== 'API_INTEGRATED'"/>
        <el-row type="flex" justify="center" align="middle">
          <el-row type="flex" justify="center" align="middle">
            <div class="metric-box">
              <div class="value">{{ content.scenarioStepTotal ? content.scenarioStepTotal : 0 }}</div>
              <div class="name" v-if="report.reportType === 'API_INTEGRATED'">{{$t('api_test.definition.request.case')}}</div>
              <div class="name" v-else>{{ $t('test_track.plan_view.step') }}</div>
            </div>
            <span class="ms-point-success"/>
            <div class="metric-box">
              <div class="value">{{ content.scenarioStepSuccess ? content.scenarioStepSuccess : 0 }}</div>
              <div class="name">{{ $t('api_report.success') }}</div>
            </div>
            <span class="ms-point-error"/>
            <div class="metric-box">
              <div class="value">{{ content.scenarioStepError ? content.scenarioStepError : 0 }}</div>
              <div class="name">{{ $t('api_report.fail') }}</div>
            </div>
            <span class="ms-point-error-code"
                  v-if="content.scenarioErrorReport > 0 || content.scenarioStepErrorReport > 0 "/>
            <div class="metric-box" v-if="content.scenarioErrorReport > 0 || content.scenarioStepErrorReport > 0 ">
              <div class="value">{{ content.scenarioStepErrorReport ? content.scenarioStepErrorReport : 0 }}</div>
              <div class="name">{{ $t('error_report_library.option.name') }}</div>
            </div>
            <span class="ms-point-unexecute" />
            <div class="metric-box">
              <div class="value">{{
                  content.scenarioStepUnExecuteReport ? content.scenarioStepUnExecuteReport : 0
                }}
              </div>
              <div class="name">{{ $t('api_test.home_page.detail_card.unexecute') }}</div>
            </div>
          </el-row>
        </el-row>
      </div>
      <div class="split"></div>

      <div style="width: 50%">
        <el-row type="flex" justify="space-around" align="middle">
          <div class="metric-icon-box">
            <i class="el-icon-warning-outline fail"></i>
            <div class="value">{{ fail }}</div>
            <div class="name">{{ $t('api_report.fail') }}</div>
          </div>
          <div class="metric-icon-box">
            <i class="el-icon-document-checked assertions"></i>
            <div class="value">{{ assertions }}</div>
            <div class="name">{{ $t('api_report.assertions_pass') }}</div>
          </div>
          <div class="metric-icon-box" v-if="content.errorCode > 0">
            <i class="el-icon-document-checked assertions"></i>
            <div class="value">{{ errorCodeAssertions }}</div>
            <div class="name">{{ $t('error_report_library.assertion') }}</div>
          </div>
          <div class="metric-icon-box">
            <i class="el-icon-document-copy total"></i>
            <div class="value">{{ this.content.total }}</div>
            <div class="name">{{ $t('api_report.request') }}</div>
          </div>
        </el-row>
      </div>
    </el-row>
  </div>
</template>

<script>
import MsChart from "@/business/components/common/chart/MsChart";

export default {
  name: "MsMetricChart",
  components: {MsChart},
  props: {
    report: Object,
    content: Object,
    totalTime: Number,
    isExport: {
      type: Boolean,
      default: false,
    }
  },
  data() {
    return {
      hour: 0,
      minutes: 0,
      seconds: 0,
      time: 0,
      scenarioTotal: 0,
      scenarioSuccess: 0,
      scenarioError: 0,
      reqTotal: 0,
    }
  },
  created() {
    this.initTime();
  },
  methods: {
    initTime() {
      this.time = this.totalTime;
      this.seconds = (this.time) / 1000;
      if (this.seconds >= 1) {
        if (this.seconds < 60) {
          this.seconds = Math.round(this.seconds * 100 / 1) / 100;
          this.time = this.seconds + "s"
        }
        if (this.seconds > 60) {
          this.minutes = Math.round(this.seconds / 60)
          this.seconds = Math.round(this.seconds * 100 % 60) / 100;
          this.time = this.minutes + "min" + this.seconds + "s"
        }
        if (this.minutes > 60) {
          this.hour = Math.round(this.minutes / 60)
          this.minutes = Math.round(this.minutes % 60)
          this.time = this.hour + "hour" + this.minutes + "min" + this.seconds + "s"
        }

      } else {
        this.time = this.totalTime + "ms"
      }
    },
  },
  computed: {
    options() {
      return {
        color: ['#67C23A', '#F56C6C', '#F6972A', '#9C9B9A'],
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        title: [{
          text: (this.content.success + this.content.error  + this.content.errorCode + this.content.unExecute),
          subtext: this.$t('api_report.request'),
          top: 'center',
          left: 'center',
          textStyle: {
            rich: {
              align: 'center',
              value: {
                fontSize: 32,
                fontWeight: 'bold',
                padding: [10, 0]
              },
              name: {
                fontSize: 14,
                fontWeight: 'normal',
                color: '#7F7F7F',
              }
            }
          }
        }],
        series: [
          {
            type: 'pie',
            radius: ['80%', '90%'],
            avoidLabelOverlap: false,
            hoverAnimation: false,
            itemStyle: {
              borderColor: "#FFF",
              shadowColor: '#E1E1E1',
              shadowBlur: 10
            },
            labelLine: {
              show: false
            },
            data: [
              {value: this.content.success},
              {value: this.content.error},
              {value: this.content.errorCode},
              {value: this.content.unExecute},
            ]
          }
        ]
      };
    },

    fail() {
      return (this.content.error / this.content.total * 100).toFixed(0) + "%";
    },
    assertions() {
      return this.content.passAssertions + " / " + this.content.totalAssertions;
    },
    errorCodeAssertions() {
      return this.content.errorCode + " / " + this.content.totalAssertions;
    }
  },
}
</script>

<style scoped>
.metric-container {
  padding: 5px 10px;
}

.metric-container #chart {
  width: 140px;
  height: 140px;
  margin-right: 40px;
}

.metric-container .split {
  margin: 20px;
  height: 100px;
  border-left: 1px solid #D8DBE1;
}

.metric-container .circle {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  box-shadow: 0 0 20px 1px rgba(200, 216, 226, .42);
  display: inline-block;
  margin-right: 10px;
  vertical-align: middle;
}

.metric-container .circle.success {
  background-color: #67C23A;
}

.metric-container .circle.fail {
  background-color: #F56C6C;
}

.metric-box {
  display: inline-block;
  text-align: center;
}

.metric-box .value {
  font-size: 32px;
  font-weight: 600;
  letter-spacing: -.5px;
}

.metric-time .value {
  font-size: 25px;
  font-weight: 400;
  letter-spacing: -.5px;
}

.metric-box .name {
  font-size: 16px;
  letter-spacing: -.2px;
  color: #404040;
}

.metric-icon-box {
  text-align: center;
  margin: 0 10px;
}

.metric-icon-box .value {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: -.4px;
  line-height: 28px;
  vertical-align: middle;
}

.metric-icon-box .name {
  font-size: 13px;
  letter-spacing: 1px;
  color: #BFBFBF;
  line-height: 18px;
}

.metric-icon-box .fail {
  color: #F56C6C;
  font-size: 40px;
}

.metric-icon-box .assertions {
  font-size: 40px;
}

.metric-icon-box .total {
  font-size: 40px;
}

.ms-req {
  border-radius: 50%;
  height: 110px;
  width: 110px;
  display: inline-block;
  vertical-align: top;
  margin-right: 30px;
}

.ms-req-error {
  border: 5px #F56C6C solid;
}

.ms-req-success {
  border: 5px #67C23A solid;
}

.ms-req-span {
  display: block;
  color: black;
  height: 110px;
  line-height: 110px;
  text-align: center;
}

.ms-point-success {
  border-radius: 50%;
  height: 12px;
  width: 12px;
  display: inline-block;
  vertical-align: top;
  margin-left: 20px;
  margin-right: 20px;
  background-color: #67C23A;
}

.ms-point-error {
  border-radius: 50%;
  height: 12px;
  width: 12px;
  display: inline-block;
  vertical-align: top;
  margin-left: 20px;
  margin-right: 20px;
  background-color: #F56C6C;
}

.ms-point-error-code {
  border-radius: 50%;
  height: 12px;
  width: 12px;
  display: inline-block;
  vertical-align: top;
  margin-left: 20px;
  margin-right: 20px;
  background-color: #F6972A;
}

.ms-point-unexecute {
  border-radius: 50%;
  height: 12px;
  width: 12px;
  display: inline-block;
  vertical-align: top;
  margin-left: 20px;
  margin-right: 20px;
  background-color: #9C9B9A;
}
</style>
