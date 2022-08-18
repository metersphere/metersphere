<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:16px 5px 16px 5px;"
           style="height: 350px;margin-top: 5px">
    <div slot="header">
      <span class="title">
        {{ $t('api_test.home_page.test_case_count_card.title') }}
      </span>
    </div>
    <!-- 数量统计 -->
    <el-container>
      <el-aside width="120px">
        <count-rectangle-chart :content="testCaseCountData.allApiDataCountNumber"/>
      </el-aside>
      <el-main style="padding-left: 0px;padding-right: 0px;display: block">
        <div style="float:right;margin:0 auto;overflow: auto">
          <el-row align="center" class="hidden-lg-and-down">
            <el-col :span="6"
                    style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="testCaseCountData.httpCountStr"></div>
            </el-col>
            <el-col :span="6"
                    style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="testCaseCountData.rpcCountStr"></div>
            </el-col>
            <el-col :span="6"
                    style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="testCaseCountData.tcpCountStr"></div>
            </el-col>
            <el-col :span="6" style="padding: 5px;">
              <div class="count-info-div" v-html="testCaseCountData.sqlCountStr"></div>
            </el-col>
          </el-row>
          <el-row align="right" class="hidden-xl-only">
            <el-col :span="6"
                    style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="testCaseCountData.httpCountStr"></div>
            </el-col>
            <el-col :span="6"
                    style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="testCaseCountData.rpcCountStr"></div>
            </el-col>
            <el-col :span="6"
                    style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="testCaseCountData.tcpCountStr"></div>
            </el-col>
            <el-col :span="6" style="padding: 5px;">
              <div class="count-info-div" v-html="testCaseCountData.sqlCountStr"></div>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>
    <!-- 本周新增 -->
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 0px;font-size: 14px">
        <el-row>
          <el-col>
            {{ $t('api_test.home_page.test_case_details_card.this_week_add') }}
            <el-link type="info" @click="redirectPage('thisWeekCount')" target="_blank" style="color: #000000">
              {{ testCaseCountData.thisWeekAddedCount }}
            </el-link>
            {{ $t('api_test.home_page.unit_of_measurement') }}
          </el-col>
        </el-row>
      </el-header>
      <el-main style="padding:0px">
        <el-row>
          <el-col :span="12">
            <span class="default-property">
              {{
                $t('api_test.home_page.test_case_details_card.this_week_execute', [testCaseCountData.thisWeekExecutedCount])
              }}
            </span>
          </el-col>
          <el-col :span="12">
              <span class="default-property" style="float: right">
                {{ $t('api_test.home_page.test_case_details_card.executed', [testCaseCountData.executedCount]) }}
              </span>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
    <!-- 用例通过率 -->
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 5px;font-size: 14px">
        <el-row>
          <span style="float: left">
            {{ $t('api_test.home_page.detail_card.rate.case_pase') + ":" }} &nbsp;&nbsp;
          </span>
          <span class="rows-count-number" style="font-size: 14px">
            <b>
            {{ testCaseCountData.passRage }}
            </b>
            <el-tooltip placement="top" class="info-tool-tip">
              <div slot="content">{{ $t('api_test.home_page.formula.api_case_pass') }}</div>
              <el-button icon="el-icon-info" style="padding:0px;border: 0px"></el-button>
            </el-tooltip>
          </span>
        </el-row>
      </el-header>
      <el-main style="padding:0px">
        <el-row v-if="isXpack">
          <el-col :span="6">
            <span class="default-property">
              {{ $t('api_test.home_page.detail_card.unexecute') }}
              <el-link class="rows-count-number" type="info" @click="redirectPage('unexecuteCount')" target="_blank">
                <b>{{ testCaseCountData.unexecuteCount }}</b>
              </el-link>
            </span>
          </el-col>
          <el-col :span="6" class="itemIsCenter">
            <span class="default-property">
              {{ $t('api_test.home_page.detail_card.execution_failed') }}
              <el-link class="rows-count-number" type="info" @click="redirectPage('executionFailedCount')"
                       target="_blank">
                <b>{{ testCaseCountData.executionFailedCount }}</b>
              </el-link>
            </span>
          </el-col>
          <el-col :span="6" class="itemIsCenter">
              <span class="default-property">
              {{ $t('error_report_library.option.name') }}
              <el-link class="rows-count-number" type="info" @click="redirectPage('fakeErrorCount')" target="_blank">
                <b>{{ testCaseCountData.fakeErrorCount }}</b>
              </el-link>
            </span>
          </el-col>
          <el-col :span="6">
              <span class="main-property" style="float: right">
                {{ $t('api_test.home_page.detail_card.execution_pass') }}
                <el-link class="rows-count-number" type="info" @click="redirectPage('executionPassCount')"
                         target="_blank">
                  <b>{{ testCaseCountData.executionPassCount }}</b>
                </el-link>
              </span>
          </el-col>
        </el-row>
        <el-row v-else>
          <el-col :span="8">
            <span class="default-property">
              {{ $t('api_test.home_page.detail_card.unexecute') }}
              <el-link class="rows-count-number" type="info" @click="redirectPage('unexecuteCount')" target="_blank">
                <b>{{ testCaseCountData.unexecuteCount }}</b>
              </el-link>
            </span>
          </el-col>
          <el-col :span="8" class="itemIsCenter">
            <span class="default-property">
              {{ $t('api_test.home_page.detail_card.execution_failed') }}
              <el-link class="rows-count-number" type="info" @click="redirectPage('notSuccessCount')" target="_blank">
                <b>{{ testCaseCountData.executionFailedCount + testCaseCountData.fakeErrorCount }}</b>
              </el-link>
            </span>
          </el-col>
          <el-col :span="8">
              <span class="main-property" style="float: right">
                {{ $t('api_test.home_page.detail_card.execution_pass') }}
                <el-link class="rows-count-number" type="info" @click="redirectPage('executionPassCount')"
                         target="_blank">
                  <b>{{ testCaseCountData.executionPassCount }}</b>
                </el-link>
              </span>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
    <!-- 接口覆盖率 -->
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 5px;font-size: 14px">
        <el-row>
          <span style="float: left">
            {{ $t('api_test.home_page.detail_card.rate.interface_coverage') + ":" }}&nbsp;&nbsp;
          </span>
          <span class="rows-count-number" style="font-size: 14px">
            <b>
            {{ testCaseCountData.coverageRage }}
            </b>
              <el-tooltip placement="top" class="info-tool-tip">
                <div slot="content">{{ $t('api_test.home_page.formula.coverage') }}</div>
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
               <el-link type="info" class="rows-count-number" @click="redirectPage('uncoverageCount')" target="_blank">
                 <b>{{ testCaseCountData.uncoverageCount }}</b>
                </el-link>
            </span>
          </el-col>
          <el-col :span="12">
              <span class="main-property" style="float: right">
                {{ $t('api_test.home_page.detail_card.coverage') }}
                <el-link class="rows-count-number" @click="redirectPage('coverageCount')" target="_blank">
                  <b>
                    {{ testCaseCountData.coverageCount }}
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
  name: "ApiCaseCountCard",

  components: {CountRectangleChart},

  data() {
    return {
      result: {},
      loading: false,
    }
  },
  props: {
    testCaseCountData: {},
    apiCaseCoverage: Object,
    isXpack: Boolean,
  },
  methods: {
    redirectPage(clickType) {
      if (clickType === 'uncoverageCount') {
        //这里业务逻辑应当跳转接口列表
        this.$emit("redirectPage", "api", "api", "unCoverageTestCase");
      } else if (clickType === 'coverageCount') {
        //这里业务逻辑应当跳转接口列表
        this.$emit("redirectPage", "api", "api", "coverageTestCase");
      } else {
        this.$emit("redirectPage", "api", "apiTestCase", clickType);
      }
    }
  },

  created() {

  },
  activated() {
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
