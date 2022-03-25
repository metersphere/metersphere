<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:10px;">
    <div slot="header" >
      <span class="title">
        {{ $t('test_track.home.case_count') }}
      </span>
    </div>

    <el-container>
      <el-aside width="120px">
        <div class="main-number-show">
          <span class="count-number">
            {{trackCountData.allCaseCountNumber}}
          </span>
          <span style="color: #6C317C;">
            {{$t('api_test.home_page.unit_of_measurement')}}
          </span>
        </div>
      </el-aside>
      <el-main style="padding-left: 0px;padding-right: 0px;">
        <div style="width: 200px;margin:0 auto">

          <el-row align="center">
            <el-col :span="6" style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="trackCountData.p0CountStr"></div>
            </el-col>
            <el-col :span="6" style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="trackCountData.p1CountStr"></div>
            </el-col>
            <el-col :span="6" style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">
              <div class="count-info-div" v-html="trackCountData.p2CountStr"></div>
            </el-col>
            <el-col :span="6" style="padding: 5px;">
              <div class="count-info-div" v-html="trackCountData.p3CountStr"></div>
            </el-col>
          </el-row>
<!--          <el-row align="right" style="margin-left: 20px" class="hidden-xl-only">-->
<!--            <el-col :span="6" style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">-->
<!--              <div class="count-info-div" v-html="trackCountData.p0CountStr"></div>-->
<!--            </el-col>-->
<!--            <el-col :span="6" style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">-->
<!--              <div class="count-info-div" v-html="trackCountData.p1CountStr"></div>-->
<!--            </el-col>-->
<!--            <el-col :span="6" style="padding: 5px;border-right-style: solid;border-right-width: 1px;border-right-color: #ECEEF4;">-->
<!--              <div class="count-info-div" v-html="trackCountData.p2CountStr"></div>-->
<!--            </el-col>-->
<!--            <el-col :span="6" style="padding: 5px;">-->
<!--              <div class="count-info-div" v-html="trackCountData.p3CountStr"></div>-->
<!--            </el-col>-->
<!--          </el-row>-->
        </div>
      </el-main>
    </el-container>

    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 10px;">
        <el-row>
          <el-col>
            {{$t('api_test.home_page.api_details_card.this_week_add')}}
            <el-link type="info" @click="redirectPage('thisWeekCount')" target="_blank" style="color: #000000">{{trackCountData.thisWeekAddedCount}}
            </el-link>
            {{$t('api_test.home_page.unit_of_measurement')}}
          </el-col>
        </el-row>
      </el-header>
      <el-main style="padding: 5px;margin-top: 10px">
        <el-container>
          <el-aside width="60%" class="count-number-show" style="margin-bottom: 0px;margin-top: 0px">
            <el-container>
              <el-aside width="30%">
                {{ $t('test_track.home.review_rate') }}:
              </el-aside>
              <el-main style="padding: 0px 0px 0px 0px; line-height: 100px; text-align: center;">
                <span class="count-number">
                {{trackCountData.reviewRage}}
                  <el-tooltip placement="top" class="info-tool-tip">
                    <div slot="content">{{ $t('api_test.home_page.formula.review')}}</div>
                    <el-button icon="el-icon-info" style="padding:0px;border: 0px"></el-button>
                  </el-tooltip>
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
                      {{ $t('test_track.review.prepare') }}
                      {{"\xa0\xa0"}}
                      <el-link type="info" @click="redirectPage('Prepare')" target="_blank" style="color: #000000">
                        {{trackCountData.prepareCount}}
                      </el-link>
                    </span>
                  </el-col>
                  <el-col style="margin-top: 5px;">
                    <span class="default-property">
                      {{ $t('test_track.review.un_pass') }}
                      {{"\xa0\xa0"}}
                      <el-link type="info" @click="redirectPage('UnPass')" target="_blank" style="color: #000000">
                        {{trackCountData.unPassCount}}
                      </el-link>
                    </span>
                  </el-col>
                  <el-col style="margin-top: 5px;">
                    <span class="main-property">
                      {{ $t('test_track.review.pass') }}
                      {{"\xa0\xa0"}}
                      <el-link type="info" @click="redirectPage('Pass')" target="_blank" style="color: #000000">
                        {{trackCountData.passCount}}
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
export default {
  name: "CaseCountCard",
  props:{
    trackCountData: {},
  },
  data() {
    return {
      result: {

      }
    }
  },
  methods: {
    redirectPage(clickType){
      this.$emit("redirectPage","case", "case",clickType);
    }
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
  color: var(--count_number);
  position: relative;
}

.main-number-show {
  width: 100px;
  height: 100px;
  border-style: solid;
  border-width: 7px;
  border-color: var(--count_number_shallow);
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
  font-size: 12px
}
.main-property{
  color: #F39021;
  font-size: 12px
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
.info-tool-tip{
  position: absolute;
  top: 0;
}
</style>
