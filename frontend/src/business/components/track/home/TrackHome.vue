<template>
  <ms-container>
    <el-header height="0">
      <div style="float: right">
        <div v-if="tipsType==='1'">
          🤔️ 天凉了，保温杯买了吗？
        </div>
        <div v-else-if="tipsType==='2'">
          😔 觉得MeterSphere不好用就来
          <el-link href="https://github.com/metersphere/metersphere/issues" target="_blank" style="color: black"
                   type="primary">https://github.com/metersphere/metersphere/issues
          </el-link>
          吐个槽吧！
        </div>
        <div v-else-if="tipsType==='3'">
          😄 觉得MeterSphere好用就来
          <el-link href="https://github.com/metersphere/metersphere" target="_blank" style="color: black"
                   type="primary">https://github.com/metersphere/metersphere
          </el-link>
          点个star吧！
        </div>
        <div v-else>
          😊 MeterSphere温馨提醒 —— 多喝热水哟！
        </div>
      </div>
    </el-header>
    <ms-main-container v-loading="result.loading">
      <el-row :gutter="0"></el-row>
      <el-row :gutter="10">
        <el-col :span="6">
          <div class="square">
            <case-count-card :track-count-data="trackCountData" class="track-card"/>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="square">
            <relevance-case-card class="track-card"/>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="square">3</div>
        </el-col>
      </el-row>
    </ms-main-container>
  </ms-container>
</template>

<script>

import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsContainer from "@/business/components/common/components/MsContainer";
import CaseCountCard from "@/business/components/track/home/components/CaseCountCard";
import RelevanceCaseCard from "@/business/components/track/home/components/RelevanceCaseCard";
import {getCurrentProjectID} from "@/common/js/utils";

export default {
  name: "TrackHome",
  components: {
    RelevanceCaseCard,
    CaseCountCard,
    MsMainContainer,
    MsContainer
  },
  data() {
    return {
      tipsType: "1",
      result: {},
      trackCountData: {}
    }
  },
  activated() {
    this.checkTipsType();
    this.init();
  },
  methods: {
    checkTipsType() {
      let random = Math.floor(Math.random() * (4 - 1 + 1)) + 1;
      this.tipsType = random + "";
    },
    init() {
      let selectProjectId = getCurrentProjectID();
      this.$get("/track/count/" + selectProjectId, response => {
        this.trackCountData = response.data;
      });
    }

  }
}
</script>

<style scoped>
.square {
  background-color: #ecf0f3;
  width: 100%;
  height: 400px;
}

.rectangle {
  background-color: #e7e5e5;
  width: 100%;
  height: 400px;
}

.el-row {
  margin-bottom: 20px;
  margin-left: 20px;
  margin-right: 20px;
}

.track-card {
  height: 100%;
}
</style>
