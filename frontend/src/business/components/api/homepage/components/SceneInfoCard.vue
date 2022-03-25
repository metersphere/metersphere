<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:16px 5px 16px 16px;"
           style="height: 350px;margin-top: 5px">
    <div slot="header">
      <span class="title">
        {{ $t('api_test.home_page.test_scene_count_card.title') }}
      </span>
    </div>
    <el-container>
      <el-main style="padding: 0px 0px 0px 0px; line-height: 100px; text-align: center;">
        <ms-count-ring-chart :content="sceneCountData.allApiDataCountNumber"/>
      </el-main>
    </el-container>
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 10px;">
        <el-row :gutter="20" class="hidden-lg-and-down ">
          <el-col :span="8">
            {{ $t('api_test.home_page.test_scene_details_card.this_week_add') }}
            <el-link type="info" @click="redirectPage('thisWeekCount')" target="_blank" style="color: #000000">
              {{ sceneCountData.thisWeekAddedCount }}
            </el-link>
            {{ $t('api_test.home_page.unit_of_measurement') }}
          </el-col>
          <el-col :span="8">
            {{
              $t('api_test.home_page.test_scene_details_card.this_week_execute', [sceneCountData.thisWeekExecutedCount])
            }}
          </el-col>
          <el-col :span="8">
            {{ $t('api_test.home_page.test_scene_details_card.executed', [sceneCountData.executedCount]) }}
          </el-col>
        </el-row>
        <el-row :gutter="20" class="hidden-xl-only">
          <el-col :span="8">
            <div class="count-info-div"
                 v-html="$t('api_test.home_page.schedule_task_details_card.this_week_add_sm',[sceneCountData.thisWeekAddedCount])"></div>
          </el-col>
          <el-col :span="8">
            <div class="count-info-div"
                 v-html="$t('api_test.home_page.test_scene_details_card.this_week_execute_sm',[sceneCountData.thisWeekExecutedCount])"></div>
          </el-col>
          <el-col :span="8">
            <div class="count-info-div"
                 v-html="$t('api_test.home_page.test_scene_details_card.executed_sm',[sceneCountData.executedCount])"></div>
          </el-col>
        </el-row>
      </el-header>
      <el-main style="padding: 5px 5px 0px 0px;margin-top: 10px">
        <el-container style="height: 96px">
          <el-aside width="60%" class="count-number-show" style="margin-bottom: 0px;margin-top: 0px">
            <el-container style="height: 40px;margin-top: 10px">
              <el-aside width="80px" style="line-height: 40px;">
                <span style="float: left">
                  {{ $t('api_test.home_page.detail_card.rate.pass') + ":" }}
                </span>
              </el-aside>
              <el-main style="padding: 0px 0px 0px 0px; line-height: 40px; text-align: center;">
                <span class="rows-count-number">
                {{ sceneCountData.passRage }}
                  <el-tooltip placement="top" class="info-tool-tip">
                    <div slot="content">{{ $t('api_test.home_page.formula.pass') }}</div>
                    <el-button icon="el-icon-info" style="padding:0px;border: 0px"></el-button>
                  </el-tooltip>
              </span>
              </el-main>
            </el-container>
            <el-container style="height: 40px;margin-top: 1px">
              <el-aside width="80px" style="line-height: 40px;">
                <span style="float: left">
                  {{ $t('api_test.home_page.detail_card.rate.interface_coverage') + ":" }}
                </span>
              </el-aside>
              <el-main style="padding: 0px 0px 0px 0px; line-height: 40px; text-align: center;">
                <span v-if="interfaceCoverage === 'waitting...'">
                  <i class="el-icon-loading lading-icon"></i>
                </span>
                <span v-else class="rows-count-number">
                  {{ interfaceCoverage }}
                  <el-tooltip placement="top" class="info-tool-tip">
                    <div slot="content">{{ $t('api_test.home_page.formula.interface_coverage') }}</div>
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
                    <span class="defaultProperty">
                      {{ $t('api_test.home_page.detail_card.unexecute') }}
                      {{ "\xa0\xa0" }}
                      <el-link type="info" @click="redirectPage('unExecute')" target="_blank" style="color: #000000">
                        {{ sceneCountData.unexecuteCount }}
                      </el-link>
                    </span>
                  </el-col>
                  <el-col style="margin-top: 5px;">
                    <span class="defaultProperty">
                      {{ $t('api_test.home_page.detail_card.execution_failed') }}
                      {{ "\xa0\xa0" }}
                      <el-link type="info" @click="redirectPage('executeFailed')" target="_blank"
                               style="color: #000000">
                        {{ sceneCountData.executionFailedCount }}
                      </el-link>
                    </span>
                  </el-col>
                  <el-col style="margin-top: 5px;">
                    <span class="main-property">
                      {{ $t('api_test.home_page.detail_card.execution_pass') }}
                      {{ "\xa0\xa0" }}
                      <el-link type="info" @click="redirectPage('executePass')" target="_blank" style="color: #000000">
                        {{ sceneCountData.executionPassCount }}
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
  name: "MsSceneInfoCard",

  components: {MsCountRingChart},

  data() {
    return {
      result: {},
      loading: false
    }
  },

  props: {
    sceneCountData: {},
    interfaceCoverage: String,
  },

  methods: {
    redirectPage(clickType) {
      this.$emit("redirectPage", "scenario", "scenario", clickType);
    }
  },
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
  margin: 20px auto;
}

.rows-count-number {
  font-family: 'ArialMT', 'Arial', sans-serif;
  font-size: 19px;
  color: var(--count_number);
  position: relative;
  margin-left: 10px;
  float: left;
}

.lading-icon {
  font-size: 25px;
  color: var(--count_number);
  font-weight: bold;
}

.main-number-show {
  margin: 0 auto;
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

.defaultProperty {
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
