<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:16px 5px 16px 5px;"
           style="height: 350px;margin-top: 5px">
    <div slot="header">
      <span class="title">
        {{ $t('api_test.home_page.api_count_card.title') }}
      </span>
    </div>
    <!--  数值统计  -->
    <el-container>
      <el-aside width="120px">
        <count-rectangle-chart :content="apiCountData.allApiDataCountNumber"/>
      </el-aside>
      <el-main style="padding-left: 0px;padding-right: 0px;display: block">
        <div style="float:right;margin:0 auto;overflow: auto">
          <el-row align="right">
            <el-col :span="6"
                    style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="apiCountData.httpCountStr"></div>
            </el-col>
            <el-col :span="6"
                    style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="apiCountData.rpcCountStr"></div>
            </el-col>
            <el-col :span="6"
                    style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="apiCountData.tcpCountStr"></div>
            </el-col>
            <el-col :span="6" style="padding: 5px;">
              <div class="count-info-div" v-html="apiCountData.sqlCountStr"></div>
            </el-col>
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
              {{ apiCountData.thisWeekAddedCount }}
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

    <!--   接口完成率   -->
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 5px;font-size: 14px">
        <el-row>
          <span style="float: left;">
            {{ $t('api_test.home_page.detail_card.rate.api_completion') + ":" }}&nbsp;&nbsp;
          </span>
          <span class="rows-count-number" style="font-size:14px">
            <b>
            {{ apiCountData.completionRage }}
            </b>
              <el-tooltip placement="top" class="info-tool-tip">
                <div slot="content">{{ $t('api_test.home_page.formula.completion') }}</div>
                <el-button icon="el-icon-info" style="padding:0px;border: 0px"></el-button>
              </el-tooltip>
          </span>
        </el-row>
      </el-header>
      <el-main style="padding:0px">
        <el-row>
          <el-col :span="8">
            <span class="default-property">
              {{ $t('api_test.home_page.detail_card.not_started') }}
              <el-link class="rows-count-number" type="info" @click="redirectPage('Prepare')" target="_blank">
                <b>{{ apiCountData.notStartedCount }}</b>
              </el-link>
            </span>
          </el-col>
          <el-col :span="8" class="itemIsCenter">
            <span class="default-property">
              {{ $t('api_test.home_page.detail_card.running') }}
              <el-link class="rows-count-number" type="info" @click="redirectPage('Underway')" target="_blank">
                <b>{{ apiCountData.runningCount }}</b>
              </el-link>
            </span>
          </el-col>
          <el-col :span="8">
              <span class="main-property" style="float: right">
                {{ $t('api_test.home_page.detail_card.finished') }}
                <el-link class="rows-count-number" type="info" @click="redirectPage('Completed')" target="_blank">
                  <b>{{ apiCountData.finishedCount }}</b>
                </el-link>
              </span>
          </el-col>
        </el-row>
      </el-main>
    </el-container>

    <!--   接口覆蓋率   -->
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 5px;font-size: 14px">
        <el-row>
          <span style="float: left">
            {{ $t('api_test.home_page.detail_card.rate.interface_coverage') + ":" }}&nbsp;&nbsp;
          </span>
          <span v-if="apiCoverage.rateOfCoverage === 'waitting...'">
            <i class="el-icon-loading lading-icon"></i>
          </span>
          <span v-else class="rows-count-number" style="font-size: 14px">
            <b>{{ apiCoverage.rateOfCoverage }}</b>
            <el-tooltip placement="top" class="info-tool-tip">
              <div slot="content">{{ $t('api_test.home_page.formula.api_coverage') }}</div>
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
              <el-link class="rows-count-number" type="info" @click="redirectPage('notCoverate')" target="_blank">
                <b>
                {{ apiCoverage.notCoverate }}
                </b>
              </el-link>
            </span>
          </el-col>
          <el-col :span="12">
              <span class="main-property" style="float: right">
                {{ $t('api_test.home_page.detail_card.coverage') }}
                <el-link class="rows-count-number" @click="redirectPage('coverate')" target="_blank">
                  <b>
                  {{ apiCoverage.coverate }}
                  </b>
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
  name: "ApiCountCard",

  components: {CountRectangleChart},

  data() {
    return {
      result: {},
      loading: false
    }
  },
  props: {
    apiCountData: {},
    apiCoverage: Object,
  },
  methods: {
    redirectPage(clickType) {
      this.$emit("redirectPage", "api", "api", clickType);
    }
  }
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
