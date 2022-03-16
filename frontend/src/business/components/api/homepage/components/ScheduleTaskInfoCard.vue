<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:16px 5px 16px 16px;"
           style="height: 350px;margin-top: 5px">
    <div slot="header">
      <span class="title">
        {{ $t('api_test.home_page.schedule_task_count_card.title') }}
      </span>
    </div>
    <el-container>
      <el-main style="padding: 0px 0px 0px 0px; line-height: 100px; text-align: center;">
        <ms-count-ring-chart :content="scheduleTaskCountData.allApiDataCountNumber"/>
      </el-main>
    </el-container>
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 10px;">
        <el-row :gutter="20" class="hidden-lg-and-down ">
          <el-col :span="8">
            {{
              $t('api_test.home_page.schedule_task_details_card.this_week_add', [scheduleTaskCountData.thisWeekAddedCount])
            }}
          </el-col>
          <el-col :span="8">
            {{
              $t('api_test.home_page.schedule_task_details_card.this_week_execute', [scheduleTaskCountData.thisWeekExecutedCount])
            }}
          </el-col>
          <el-col :span="8">
            {{ $t('api_test.home_page.schedule_task_details_card.executed', [scheduleTaskCountData.executedCount]) }}
          </el-col>
        </el-row>
        <el-row :gutter="20" class="hidden-xl-only">
          <el-col :span="8">
            <div class="count-info-div"
                 v-html="$t('api_test.home_page.schedule_task_details_card.this_week_add_sm',[scheduleTaskCountData.thisWeekAddedCount])"></div>
          </el-col>
          <el-col :span="8">
            <div class="count-info-div"
                 v-html="$t('api_test.home_page.schedule_task_details_card.this_week_execute_sm',[scheduleTaskCountData.thisWeekExecutedCount])"></div>
          </el-col>
          <el-col :span="8">
            <div class="count-info-div"
                 v-html="$t('api_test.home_page.schedule_task_details_card.executed_sm',[scheduleTaskCountData.executedCount])"></div>
          </el-col>
        </el-row>
      </el-header>
      <el-main style="padding: 5px 5px 0px 0px;margin-top: 10px">
        <el-container>
          <el-aside width="60%" class="count-number-show" style="margin-bottom: 0px;margin-top: 0px">
            <el-container>
              <el-aside width="50px">
                <span style="float: left">
                  {{ $t('api_test.home_page.detail_card.rate.success') + ":" }}
                </span>
              </el-aside>
              <el-main style="padding: 0px 0px 0px 0px; line-height: 100px; text-align: center;">
                <span class="count-number">
                {{ scheduleTaskCountData.successRage }}
                  <el-tooltip placement="top" class="info-tool-tip">
                    <div slot="content">{{ $t('api_test.home_page.formula.success') }}</div>
                    <el-button icon="el-icon-info" style="padding:0px;border: 0px"></el-button>
                  </el-tooltip>
              </span>
              </el-main>
            </el-container>
          </el-aside>
          <el-main style="padding: 0px; height: 80px; float:right;">
            <el-card class="no-shadow-card" body-style="padding-left:5px;padding-right:5px;">
              <main>
                <el-row>
                  <el-col>
                    <span class="default-property">
                      {{ $t('api_test.home_page.detail_card.failed') }}
                      {{ "\xa0\xa0" }}
                      {{ scheduleTaskCountData.failedCount }}
                    </span>
                  </el-col>
                  <el-col style=" height: 20px;margin-top: 3px;">
                  </el-col>
                  <el-col style="margin-top: 5px;">
                    <span class="main-property">
                      {{ $t('api_test.home_page.detail_card.success') }}
                      {{ "\xa0\xa0" }}
                      {{ scheduleTaskCountData.successCount }}
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
import 'element-ui/lib/theme-chalk/display.css';
import MsCountRingChart from "@/business/components/common/chart/MsCountRingChart";


export default {

  name: "MsScheduleTaskInfoCard",

  components: {MsCountRingChart},

  data() {
    return {
      result: {},
      apiCountData: {},
      loading: false
    }
  },
  props: {
    scheduleTaskCountData: {},
  },
  methods: {},

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

.count-number {
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
  font-size: 12px;
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
  top: 30px;
}
</style>
