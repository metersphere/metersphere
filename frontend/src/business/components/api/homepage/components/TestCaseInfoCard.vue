<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:10px;">
    <div slot="header" >
      <span class="title">
        {{$t('api_test.home_page.test_case_count_card.title')}}
      </span>
    </div>
    <el-container>
      <el-aside width="120px">
        <div class="main-number-show">
          <span class="count-number">
            {{testCaseCountData.allApiDataCountNumber}}
          </span>
          <span style="color: #6C317C;">
            {{$t('api_test.home_page.unit_of_measurement')}}
          </span>
        </div>
      </el-aside>
      <el-main style="padding-left: 0px;padding-right: 0px">
        <div style="width: 200px;margin:0 auto">
          <el-row align="center" class="hidden-lg-and-down">
            <el-col :span="6" style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="testCaseCountData.httpCountStr"></div>
            </el-col>
            <el-col :span="6" style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="testCaseCountData.rpcCountStr"></div>
            </el-col>
            <el-col :span="6" style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="testCaseCountData.tcpCountStr"></div>
            </el-col>
            <el-col :span="6" style="padding: 5px;">
              <div class="count-info-div" v-html="testCaseCountData.sqlCountStr"></div>
            </el-col>
          </el-row>
          <el-row align="right" style="margin-left: 20px" class="hidden-xl-only">
            <el-col :span="6" style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="testCaseCountData.httpCountStr"></div>
            </el-col>
            <el-col :span="6" style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="testCaseCountData.rpcCountStr"></div>
            </el-col>
            <el-col :span="6" style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="testCaseCountData.tcpCountStr"></div>
            </el-col>
            <el-col :span="6" style="padding: 5px;">
              <div class="count-info-div" v-html="testCaseCountData.sqlCountStr"></div>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>
    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 10px;">
        <el-row :gutter="20" class="hidden-lg-and-down ">
          <el-col  :span="8">
            {{$t('api_test.home_page.test_case_details_card.this_week_add',[testCaseCountData.thisWeekAddedCount])}}
          </el-col>
          <el-col :span="8" >
            {{$t('api_test.home_page.test_case_details_card.this_week_execute',[testCaseCountData.thisWeekExecutedCount])}}
          </el-col>
          <el-col :span="8" >
            {{$t('api_test.home_page.test_case_details_card.executed',[testCaseCountData.executedCount])}}
          </el-col>
        </el-row>
        <el-row :gutter="20" class="hidden-xl-only">
          <el-col  :span="8">
            <div class="count-info-div" v-html="$t('api_test.home_page.test_case_details_card.this_week_add_sm',[testCaseCountData.thisWeekAddedCount])"></div>
          </el-col>
          <el-col :span="8" >
            <div class="count-info-div" v-html="$t('api_test.home_page.test_case_details_card.this_week_execute_sm',[testCaseCountData.thisWeekExecutedCount])"></div>
          </el-col>
          <el-col :span="8" >
            <div class="count-info-div" v-html="$t('api_test.home_page.test_case_details_card.executed_sm',[testCaseCountData.executedCount])"></div>
          </el-col>
        </el-row>
      </el-header>
      <el-main style="padding: 5px;margin-top: 10px">
        <el-container>
          <el-aside width="60%" class="count-number-show" style="margin-bottom: 0px;margin-top: 0px">
            <el-container>
              <el-aside width="30%">
                {{$t('api_test.home_page.detail_card.rate.coverage')+":"}}
              </el-aside>
              <el-main style="padding: 0px 0px 0px 0px; line-height: 100px; text-align: center;">
                <span class="count-number">
                {{testCaseCountData.coverageRage}}
              </span>
              </el-main>
            </el-container>
          </el-aside>
          <el-main style="padding: 5px">
            <el-card class="no-shadow-card" body-style="padding-left:5px;padding-right:5px">
              <main>
                <el-row>
                  <el-col>
                    <span class="default-property">
                      {{$t('api_test.home_page.detail_card.uncoverage')}}
                      {{"\xa0\xa0"}}
                      {{testCaseCountData.uncoverageCount}}
                    </span>
                  </el-col>
                  <el-col style=" height: 20px">
                  </el-col>
                  <el-col>
                    <span class="main-property">
                      {{$t('api_test.home_page.detail_card.coverage')}}
                      {{"\xa0\xa0"}}
                      {{testCaseCountData.coverageCount}}
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

export default {
  name: "MsTestCaseInfoCard",

  components: {},

  data() {
    return {
      result: {},
      loading: false,
    }
  },
  props:{
    testCaseCountData:{},
  },
  methods: {
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
.count-number{
  font-family:'ArialMT', 'Arial', sans-serif;
  font-size:33px;
  color: #6C317C;
}

.main-number-show {
  width: 100px;
  height: 100px;
  border-style: solid;
  border-width: 7px;
  border-color: #CDB9D2;
  border-radius:50%;

}

.count-number-show{
  margin:20px auto;
}
.detail-container{
  margin-top: 30px
}
.no-shadow-card{
  -webkit-box-shadow: 0 0px 0px 0 rgba(0,0,0,.1);
  box-shadow: 0 0px 0px 0 rgba(0,0,0,.1);
}
.default-property{

}
.main-property{
  color: #F39021;
}

.el-card /deep/ .el-card__header {
  border-bottom: 0px solid #EBEEF5;
}

.count-info-div{
  margin: 3px;
}
.count-info-div >>>p{
  font-size: 10px;
}
</style>
