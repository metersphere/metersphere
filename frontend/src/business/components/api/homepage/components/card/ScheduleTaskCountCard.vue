<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:16px 5px 16px 5px;"
           style="height: 350px;margin-top: 5px">
    <div slot="header">
      <span class="title">
        {{ $t('api_test.home_page.schedule_task_count_card.title') }}
      </span>
    </div>

    <!-- 数量统计 -->
    <el-container>
      <el-aside width="120px">
        <count-rectangle-chart :content="scheduleTaskCountData.allApiDataCountNumber"/>
      </el-aside>
      <el-main style="padding-left: 0px;padding-right: 0px; margin-right: 5px;display: block">
        <div style="width: 185px; float:right;margin:0 auto;overflow: auto">
          <el-row align="right">
            <span style="text-align: right;display:block;margin-top: 4px">
            {{
                $t('api_test.home_page.test_scene_details_card.this_week_execute', [scheduleTaskCountData.thisWeekExecutedCount])
              }}
            </span>
          </el-row>
          <el-row align="right">
            &nbsp;
          </el-row>
          <el-row align="right">
            <span style="text-align: right;display:block;margin-bottom: 4px;">
            {{ $t('api_test.home_page.test_scene_details_card.executed', [scheduleTaskCountData.executedCount]) }}
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
              {{ scheduleTaskCountData.thisWeekAddedCount }}
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

    <!-- 任务成功率 -->
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 5px;font-size: 14px">
        <el-row>
          <span style="float: left">
            {{ $t('api_test.home_page.detail_card.rate.success') + ":" }}&nbsp;&nbsp;
          </span>
          <span class="rows-count-number" style="font-size: 14px">
            <b>
            {{ scheduleTaskCountData.successRage }}
            </b>
            <el-tooltip placement="top" class="info-tool-tip">
              <div slot="content">{{ $t('api_test.home_page.formula.success') }}</div>
              <el-button icon="el-icon-info" style="padding:0px;border: 0px"></el-button>
            </el-tooltip>
          </span>
        </el-row>
      </el-header>
      <el-main style="padding:0px">
        <el-row v-if="this.isXpack">
          <el-col :span="8">
            <span class="default-property">
              {{ $t('api_test.home_page.detail_card.failed') }}
               <el-link type="info" class="rows-count-number" @click="redirectPage('executionFailedCount')"
                        target="_blank">
                 <b>{{ scheduleTaskCountData.failedCount }}</b>
                </el-link>
            </span>
          </el-col>
          <el-col :span="8" class="itemIsCenter">
            <span class="default-property">
              {{ $t('error_report_library.option.name') }}
               <el-link type="info" class="rows-count-number" @click="redirectPage('fakeErrorCount')" target="_blank">
                 <b>{{ scheduleTaskCountData.fakeErrorCount }}</b>
                </el-link>
            </span>
          </el-col>
          <el-col :span="8">
              <span class="main-property" style="float: right">
                {{ $t('api_test.home_page.detail_card.success') }}
                <el-link class="rows-count-number" @click="redirectPage('executionPassCount')" target="_blank">
                  <b>
                    {{ scheduleTaskCountData.successCount }}
                  </b>
                </el-link>
              </span>
          </el-col>
        </el-row>
        <el-row v-else>
          <el-col :span="12">
            <span class="default-property">
              {{ $t('api_test.home_page.detail_card.failed') }}
               <el-link type="info" class="rows-count-number" @click="redirectPage('notSuccessCount')" target="_blank">
                 <b>{{ scheduleTaskCountData.failedCount + scheduleTaskCountData.fakeErrorCount }}</b>
                </el-link>
            </span>
          </el-col>
          <el-col :span="12">
              <span class="main-property" style="float: right">
                {{ $t('api_test.home_page.detail_card.success') }}
                <el-link class="rows-count-number" @click="redirectPage('executionPassCount')" target="_blank">
                  <b>
                    {{ scheduleTaskCountData.successCount }}
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
import 'element-ui/lib/theme-chalk/display.css';
import CountRectangleChart from "@/business/components/common/chart/CountRectangleChart";


export default {

  name: "MsScheduleTaskInfoCard",

  components: {CountRectangleChart},

  data() {
    return {
      result: {},
      apiCountData: {},
      loading: false
    }
  },
  props: {
    scheduleTaskCountData: {},
    isXpack: Boolean,
  },
  methods: {
    redirectPage(clickType) {
      this.$emit("redirectPage", "scenario", "schedule", clickType);
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
