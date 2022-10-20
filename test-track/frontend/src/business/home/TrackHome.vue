<template>
  <ms-container>
    <ms-main-container v-loading="result.loading">
      <el-row :gutter="5">
        <el-col :span="6">
          <div class="square">
            <case-count-card :track-count-data="trackCountData" class="track-card" @redirectPage="redirectPage"/>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="square">
            <relevance-case-card :relevance-count-data="relevanceCountData" class="track-card"
                                 @redirectPage="redirectPage"/>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="square">
            <case-maintenance :case-option="caseOption" class="track-card"/>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="5">
        <el-col :span="12">
          <bug-count-card class="track-card"/>
        </el-col>
        <el-col :span="12">
          <ms-failure-test-case-list class="track-card" :select-function-case="true" @redirectPage="redirectPage"/>
        </el-col>
      </el-row>

      <el-row :gutter="5">
        <el-col :span="12">
          <review-list class="track-card"/>
        </el-col>
        <el-col :span="12">
          <ms-running-task-list :call-from="'track_home'" class="track-card" @redirectPage="redirectPage"/>
        </el-col>
      </el-row>

    </ms-main-container>
  </ms-container>
</template>

<script>

import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import CaseCountCard from "./components/CaseCountCard";
import RelevanceCaseCard from "./components/RelevanceCaseCard";
import CaseMaintenance from "./components/CaseMaintenance";
import {COUNT_NUMBER, COUNT_NUMBER_SHALLOW} from "metersphere-frontend/src/utils/constants";
import BugCountCard from "./components/BugCountCard";
import ReviewList from "./components/ReviewList";
import MsRunningTaskList from "./components/RunningTaskList";
import {getUUID} from "metersphere-frontend/src/utils";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import MsFailureTestCaseList from "@/business/home/components/FailureTestCaseList";
import {getTrackCaseBar, getTrackCount, getTrackRelevanceCount} from "@/api/track";
import {useStore} from "@/store";

require('echarts/lib/component/legend');
export default {
  name: "TrackHome",
  components: {
    MsFailureTestCaseList,
    ReviewList,
    BugCountCard,
    RelevanceCaseCard,
    CaseCountCard,
    MsMainContainer,
    MsContainer,
    CaseMaintenance,
    MsRunningTaskList,
  },
  data() {
    return {
      result: {},
      trackCountData: {},
      relevanceCountData: {},
      caseOption: {},
    }
  },
  activated() {
    this.init();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    init() {
      let selectProjectId = this.projectId;
      if (!selectProjectId) {
        return;
      }
      getTrackCount(selectProjectId)
        .then(r => {
          this.trackCountData = r.data;
        });

      getTrackRelevanceCount(selectProjectId)
        .then(r => {
          this.relevanceCountData = r.data;
        });

      getTrackCaseBar(selectProjectId)
        .then(r => {
          let data = r.data;
          this.setBarOption(data);
        });
    },
    setBarOption(data) {
      let xAxis = [];
      data.map(d => {
        if (!xAxis.includes(d.xAxis)) {
          xAxis.push(d.xAxis);
        }
      });
      let yAxis1 = data.filter(d => d.groupName === 'FUNCTIONCASE').map(d => [d.xAxis, d.yAxis]);
      let yAxis2 = data.filter(d => d.groupName === 'RELEVANCECASE').map(d => [d.xAxis, d.yAxis]);
      let store = useStore();
      let option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: xAxis
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        },
        legend: {
          data: [this.$t('test_track.home.function_case_count'), this.$t('test_track.home.relevance_case_count')],
          orient: 'vertical',
          right: '80',
        },
        series: [
          {
            name: this.$t('test_track.home.function_case_count'),
            data: yAxis1,
            type: 'bar',
            barWidth: 50,
            itemStyle: {
              color: store.theme ? store.theme : COUNT_NUMBER
            }
          },
          {
            name: this.$t('test_track.home.relevance_case_count'),
            data: yAxis2,
            type: 'bar',
            barWidth: 50,
            color: store.theme ? store.theme : COUNT_NUMBER_SHALLOW
          }]
      };
      this.caseOption = option;
    },
    redirectPage(page, dataType, selectType, title) {
      //api页面跳转
      //传入UUID是为了进行页面重新加载判断
      let uuid = getUUID();
      let home;
      switch (page) {
        case "testCase":
          this.$router.push({
            path: '/track/case/all/' + uuid + '/' + dataType + '/' + selectType
          })
          break;
        case "testPlanEdit":
          this.$router.push('/track/plan/view/' + selectType)
          break;
        case "scenarioWithQuery":
          home = this.$router.resolve('/api/automation/' + uuid + "/" + dataType + "/" + selectType);
          break;
        case "api":
          home = this.$router.resolve('/api/definition/' + uuid + "/" + dataType + "/" + selectType);
          break;
      }
      if (home) {
        window.open(home.href, '_blank');
      }
    }
  }
}
</script>

<style scoped>
.square {
  width: 100%;
  height: 400px;
}

.rectangle {
  width: 100%;
  height: 400px;
}

.el-row {
  margin-bottom: 5px;
}

.track-card {
  height: 100%;
}
</style>
