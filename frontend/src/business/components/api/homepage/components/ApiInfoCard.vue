<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:16px 5px 16px 16px;" style="height: 350px;margin-top: 5px">
    <div slot="header">
      <span class="title">
        {{ $t('api_test.home_page.api_count_card.title') }}
      </span>
    </div>
    <el-container>
      <el-aside width="120px">
        <ms-count-ring-chart :content="apiCountData.allApiDataCountNumber"/>
      </el-aside>

      <el-main style="padding-left: 0px;padding-right: 0px;">
        <div style="width: 185px; float:right;margin:0 auto">

          <el-row align="center" class="hidden-lg-and-down">
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
          <el-row align="right" class="hidden-xl-only">
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
      <el-header style="height:20px;padding: 0px;margin-bottom: 10px;">
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
      <el-main style="padding: 5px 5px 0px 0px;margin-top: 10px">
        <el-container style="height: 96px">
          <el-aside width="60%" class="count-number-show" style="margin-bottom: 0px;margin-top: 0px">
            <el-container style="height: 40px;margin-top: 10px">
              <el-aside width="80px" style="line-height: 40px;">
                <span style="float: left">
                  {{ $t('api_test.home_page.detail_card.rate.completion') + ":" }}
                </span>
              </el-aside>
              <el-main style="padding: 0px 0px 0px 0px; line-height: 40px; text-align: center;">
                <span class="rows-count-number">
                {{ apiCountData.completionRage }}
                  <el-tooltip placement="top" class="info-tool-tip">
                    <div slot="content">{{ $t('api_test.home_page.formula.completion') }}</div>
                    <el-button icon="el-icon-info" style="padding:0px;border: 0px"></el-button>
                  </el-tooltip>
              </span>
              </el-main>
            </el-container>
            <el-container style="height: 40px;margin-top: 1px">
              <el-aside width="80px" style="line-height: 40px;">
                <span style="float: left">
                    <span>{{ $t('api_test.home_page.detail_card.rate.interface_coverage') + ":" }}</span>
                </span>
              </el-aside>
              <el-main style="padding: 0px 0px 0px 0px; line-height: 40px; text-align: center;">
                <span v-if="interfaceCoverage === 'waitting...'">
                  <i class="el-icon-loading lading-icon"></i>
                </span>
                <span v-else class="rows-count-number">
                  {{ interfaceCoverage }}
                  <el-tooltip placement="top" class="info-tool-tip">
                    <div slot="content">{{ $t('api_test.home_page.formula.api_coverage') }}</div>
                    <el-button icon="el-icon-info" style="padding:0px;border: 0px"></el-button>
                  </el-tooltip>
                </span>
              </el-main>
            </el-container>
          </el-aside>
          <el-main style="padding: 0px; height: 80px; float:right;">
            <el-card class="no-shadow-card" body-style="padding-left:5px;padding-right:5px">
              <main>
                <el-row>
                  <el-col>
                    <span class="default-property">
                      {{ $t('api_test.home_page.detail_card.running') }}
                      {{ "\xa0\xa0" }}
                      <el-link type="info" @click="redirectPage('Underway')" target="_blank" style="color: #000000">
                        {{ apiCountData.runningCount }}
                      </el-link>
                    </span>
                  </el-col>
                  <el-col style="margin-top: 5px;">
                    <span class="default-property">
                      {{ $t('api_test.home_page.detail_card.not_started') }}
                      {{ "\xa0\xa0" }}
                      <el-link type="info" @click="redirectPage('Prepare')" target="_blank" style="color: #000000">
                        {{ apiCountData.notStartedCount }}
                      </el-link>
                    </span>
                  </el-col>
                  <el-col style="margin-top: 5px;">
                    <span class="main-property">
                      {{ $t('api_test.home_page.detail_card.finished') }}
                      {{ "\xa0\xa0" }}
                      <el-link type="info" @click="redirectPage('Completed')" target="_blank" style="color: #000000">
                        {{ apiCountData.finishedCount }}
                      </el-link>
                    </span>
                  </el-col>
                </el-row>
              </main>
            </el-card>
          </el-main>
        </el-container>
      </el-main>
    </el-container>
  </el-card>
</template>

<script>

import MsCountRingChart from "@/business/components/common/chart/MsCountRingChart";

export default {
  name: "MsApiInfoCard",

  components: {MsCountRingChart},

  data() {
    return {
      result: {},
      loading: false
    }
  },
  props: {
    apiCountData: {},
    interfaceCoverage: String,
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

.count-number {
  font-family: 'ArialMT', 'Arial', sans-serif;
  font-size: 33px;
  color: var(--count_number);
  position: relative;
}

.rows-count-number {
  font-family: 'ArialMT', 'Arial', sans-serif;
  font-size: 19px;
  color: var(--count_number);
  position: relative;
  margin-left: 10px;
  float: left;
}

.count-number-show {
  margin: 20px auto;
}

.detail-container {
  margin-top: 30px
}

.no-shadow-card {
  width: 115px;
  float: right;
  -webkit-box-shadow: 0 0px 0px 0 rgba(0, 0, 0, .1);
  box-shadow: 0 0px 0px 0 rgba(0, 0, 0, .1);
}

.default-property {
  font-size: 12px
}

.main-property {
  color: #F39021;
  font-size: 12px
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
</style>
