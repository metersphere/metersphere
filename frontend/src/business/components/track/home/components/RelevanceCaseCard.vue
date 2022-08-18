<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:16px 5px 16px 5px;"
           style="margin-top: 5px">
    <div slot="header">
      <span class="title">
        {{ $t('test_track.home.relevance_case') }}
      </span>
    </div>
    <!--数值统计-->
    <el-container>
      <el-aside width="120px">
        <count-rectangle-chart :content="relevanceCountData.allRelevanceCaseCount"/>
      </el-aside>
      <el-main style="padding-left: 0px;padding-right: 0px;display: block">
        <div style="float:right;margin:0 auto;overflow: auto">
          <el-row align="right">
            <el-col :span="8"
                    style="border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="relevanceCountData.apiCaseCountStr"></div>
            </el-col>
            <el-col :span="8"
                    style="border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="relevanceCountData.scenarioCaseStr"></div>
            </el-col>
            <el-col :span="8"
                    style="border-right-style: solid;border-right-width: 1px;border-right-color: rgba(220, 38, 38, 0);">
              <div class="count-info-div" v-html="relevanceCountData.performanceCaseCountStr"></div>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>

    <!-- 本周新增-->
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 0px;font-size: 14px">
        <el-row>
          <el-col>
            {{ $t('api_test.home_page.api_details_card.this_week_add') }}
            <el-link type="info" @click="redirectPage('thisWeekRelevanceCount')" target="_blank" style="color: #000000">
              {{ relevanceCountData.thisWeekAddedCount }}
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

    <!--   用例覆盖率率   -->
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 5px;font-size: 14px">
        <el-row>
          <span style="float: left">
            {{ $t('test_track.home.coverage') + ":" }}&nbsp;&nbsp;
          </span>
          <span style="font-size: 14px">
            <b>{{ relevanceCountData.coverageRage }}</b>
            <el-tooltip placement="top" class="info-tool-tip">
              <div slot="content">{{ $t('api_test.home_page.formula.testplan_coverage') }}</div>
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
              <el-link class="rows-count-number" @click="redirectPage('uncoverage')" target="_blank">
                <b>
                {{ relevanceCountData.uncoverageCount }}
                </b>
              </el-link>
            </span>
          </el-col>
          <el-col :span="12">
              <span class="main-property" style="float: right">
                {{ $t('api_test.home_page.detail_card.coverage') }}
                <el-link class="rows-count-number" @click="redirectPage('coverage')" target="_blank">
                  <b>
                  {{ relevanceCountData.coverageCount }}
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
  name: "RelevanceCaseCard",
  components: {CountRectangleChart},
  props: {
    relevanceCountData: {},
  },
  data() {
    return {
      result: {}
    }
  },
  methods: {
    redirectPage(clickType) {
      this.$emit("redirectPage", "testCase", "relationCase", clickType);
    }
  }
}
</script>

<style scoped>

.detail-container {
  margin-top: 30px;
}

.default-property {
  font-size: 14px
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

.rows-count-number {
  font-family: 'ArialMT', 'Arial', sans-serif;
  font-size: 14px;
  color: var(--count_number) !important;
}
</style>
