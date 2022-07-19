<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:16px 5px 16px 5px;"
           style="height: 350px;margin-top: 5px">
    <div slot="header">
      <span class="title">
        {{ $t('api_test.home_page.test_scene_count_card.title') }}
      </span>
    </div>

    <!-- 数量统计 -->
    <el-container>
      <el-aside width="120px">
        <count-rectangle-chart :content="sceneCountData.allApiDataCountNumber"/>
      </el-aside>
      <el-main style="padding-left: 0px;padding-right: 0px; margin-right: 5px;display: block">
        <div style="width: 185px; float:right;margin:0 auto;overflow: auto">
          <el-row align="right">
            <span style="text-align: right;display:block;margin-top: 4px">
            {{
                $t('api_test.home_page.test_scene_details_card.this_week_execute', [sceneCountData.thisWeekExecutedCount])
              }}
            </span>
          </el-row>
          <el-row align="right">
            &nbsp;
          </el-row>
          <el-row align="right">
            <span style="text-align: right;display:block;margin-bottom: 4px;">
            {{ $t('api_test.home_page.test_scene_details_card.executed', [sceneCountData.executedCount]) }}
            </span>
          </el-row>
        </div>
      </el-main>
    </el-container>

    <el-container class="detail-container">
      <!-- 本周新增 -->
      <el-header style="height:20px;padding: 0px;margin-bottom: 0px;font-size: 14px">
        <el-row>
          <el-col>
            {{ $t('api_test.home_page.api_details_card.this_week_add') }}
            <el-link type="info" @click="redirectPage('thisWeekCount')" target="_blank" style="color: #000000">
              {{ sceneCountData.thisWeekAddedCount }}
            </el-link>
            {{ $t('api_test.home_page.unit_of_measurement') }}
          </el-col>
        </el-row>
      </el-header>
      <el-main style="padding:0px">
        <el-row>
          <el-col :span="8">&nbsp;</el-col>
        </el-row>
      </el-main>
    </el-container>

    <!-- 场景通过率 -->
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 5px;font-size: 14px">
        <el-row>
          <span style="float: left">
            {{ $t('api_test.home_page.detail_card.rate.scenario_pase') + ":" }}&nbsp;&nbsp;
          </span>
          <span class="rows-count-number" style="font-size: 14px">
            <b>
            {{ sceneCountData.passRage }}
            </b>
              <el-tooltip placement="top" class="info-tool-tip">
                <div slot="content">{{ $t('api_test.home_page.formula.pass') }}</div>
                <el-button icon="el-icon-info" style="padding:0px;border: 0px"></el-button>
              </el-tooltip>
          </span>
        </el-row>
      </el-header>
      <el-main style="padding:0px">
        <el-row v-if="this.isXpack">
          <el-col :span="6">
            <span class="default-property">
              {{ $t('api_test.home_page.detail_card.unexecute') }}
               <el-link type="info" class="rows-count-number" @click="redirectPage('unexecuteCount')" target="_blank">
                 <b>{{ sceneCountData.unexecuteCount }}</b>
                </el-link>
            </span>
          </el-col>
          <el-col :span="6" class="itemIsCenter">
            <span class="default-property" style="float: left">
                {{ $t('api_test.home_page.detail_card.execution_failed') }}
                <el-link class="rows-count-number" @click="redirectPage('executionFailedCount')" target="_blank">
                  <b>
                    {{ sceneCountData.executionFailedCount }}
                  </b>
                </el-link>
              </span>
          </el-col>
          <el-col :span="6" class="itemIsCenter">
            <span class="default-property" style="float: left">
                {{ $t('error_report_library.option.name') }}
                <el-link class="rows-count-number" @click="redirectPage('fakeErrorCount')" target="_blank">
                  <b>
                    {{ sceneCountData.fakeErrorCount }}
                  </b>
                </el-link>
              </span>
          </el-col>
          <el-col :span="6">
              <span class="main-property" style="float: right">
                {{ $t('api_test.home_page.detail_card.execution_pass') }}
                <el-link class="rows-count-number" @click="redirectPage('executionPassCount')" target="_blank">
                  <b>
                    {{ sceneCountData.executionPassCount }}
                  </b>
                </el-link>
              </span>
          </el-col>
        </el-row>
        <el-row v-else>
          <el-col :span="8">
            <span class="default-property">
              {{ $t('api_test.home_page.detail_card.unexecute') }}
               <el-link type="info" class="rows-count-number" @click="redirectPage('unexecuteCount')" target="_blank">
                 <b>{{ sceneCountData.unexecuteCount }}</b>
                </el-link>
            </span>
          </el-col>
          <el-col :span="8" class="itemIsCenter">
            <span class="default-property" style="float: left">
                {{ $t('api_test.home_page.detail_card.execution_failed') }}
                <el-link class="rows-count-number" @click="redirectPage('notSuccessCount')" target="_blank">
                  <b>
                    {{ sceneCountData.executionFailedCount + sceneCountData.fakeErrorCount }}
                  </b>
                </el-link>
              </span>
          </el-col>
          <el-col :span="8">
              <span class="main-property" style="float: right">
                {{ $t('api_test.home_page.detail_card.execution_pass') }}
                <el-link class="rows-count-number" @click="redirectPage('executionPassCount')" target="_blank">
                  <b>
                    {{ sceneCountData.executionPassCount }}
                  </b>
                </el-link>
              </span>
          </el-col>
        </el-row>
      </el-main>
    </el-container>

    <!-- 场景覆盖率 -->
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 5px;font-size: 14px">
        <el-row>
          <span style="float: left">
            {{ $t('api_test.home_page.detail_card.rate.interface_coverage') + ":" }}&nbsp;&nbsp;
          </span>
          <span v-if="scenarioCoverage.rateOfCoverage === 'waitting...'">
            <i class="el-icon-loading lading-icon"></i>
          </span>
          <span v-else class="rows-count-number" style="font-size: 14px">
            <b>
            {{ scenarioCoverage.rateOfCoverage }}
            </b>
              <el-tooltip placement="top" class="info-tool-tip">
                <div slot="content">{{ $t('api_test.home_page.formula.interface_coverage') }}</div>
                <el-button icon="el-icon-info" style="padding:0px;border: 0px"></el-button>
              </el-tooltip>
          </span>
        </el-row>
      </el-header>
      <el-main style="padding:0px">
        <el-row>
          <el-col :span="12">
            <span class="default-property">
              {{ $t('api_test.home_page.detail_card.uncoverage') }}
               <el-link type="info" class="rows-count-number" @click="redirectPage('notCoverate')" target="_blank">
                 <b>{{ scenarioCoverage.notCoverate }}</b>
                </el-link>
            </span>
          </el-col>
          <el-col :span="12">
              <span class="main-property" style="float: right">
                {{ $t('api_test.home_page.detail_card.coverage') }}
                <el-link class="rows-count-number" @click="redirectPage('coverate')" target="_blank">
                  <b>{{ scenarioCoverage.coverate }}</b>
                </el-link>
              </span>
          </el-col>
        </el-row>
      </el-main>
    </el-container>

  </el-card>
</template>

<script>

import CountRectangleChart from "@/business/components/common/chart/CountRectangleChart";

export default {
  name: "ScenarioCountCard",

  components: {CountRectangleChart},

  data() {
    return {
      result: {},
      loading: false
    }
  },

  props: {
    sceneCountData: {},
    scenarioCoverage: Object,
    isXpack: Boolean,
  },
  methods: {
    redirectPage(clickType) {
      if (clickType === "coverate") {
        this.$emit("redirectPage", "api", "api", "coverageScenario");
      } else if (clickType === "notCoverate") {
        this.$emit("redirectPage", "api", "api", "unCoverageScenario");
      } else {
        this.$emit("redirectPage", "scenario", "scenario", clickType);
      }
    }
  },
}
</script>
<style scoped>
.el-aside {
  line-height: 100px;
  text-align: center;
}

.rows-count-number {
  font-family: 'ArialMT', 'Arial', sans-serif;
  font-size: 14px;
  color: var(--count_number) !important;
}

.detail-container {
  margin-top: 20px
}

.default-property {
  font-size: 14px;
}

.main-property {
  color: #F39021;
  font-size: 14px
}

.el-card /deep/ .el-card__header {
  border-bottom: 0px solid #EBEEF5;
}

.count-info-div {
  margin: 3px;
}

.count-info-div >>> p {
  font-size: 10px;
}

.info-tool-tip {
  position: absolute;
  top: 0;
}

.itemIsCenter {
  display: flex;
  justify-content: center; /*主轴上居中*/
  align-items: center; /*侧轴上居中*/
}
</style>
