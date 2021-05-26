<template>
  <ms-container>
    <ms-main-container>
      <el-row :gutter="20">
        <el-col :span="24">
          <overview-compare-card ref="overviewCard"/>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="24">
          <load-compare-card ref="loadCard"/>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="24">
          <response-time-compare-card ref="responseTimeCard"/>
        </el-col>
      </el-row>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import OverviewCompareCard from "@/business/components/performance/report/components/OverviewCompareCard";
import MsChart from "@/business/components/common/chart/MsChart";
import LoadCompareCard from "@/business/components/performance/report/components/LoadCompareCard";
import ResponseTimeCompareCard from "@/business/components/performance/report/components/ResponseTimeCompareCard";

export default {
  name: "PerformanceReportCompare",
  components: {ResponseTimeCompareCard, LoadCompareCard, MsChart, OverviewCompareCard, MsMainContainer, MsContainer},
  mounted() {
    this.init();
  },
  computed: {
    isReadOnly() {
      return false;
    }
  },
  data() {
    return {}
  },
  methods: {
    init() {
      this.$refs.overviewCard.initTable();
      this.$refs.loadCard.initCard();
      this.$refs.responseTimeCard.initCard();
    }
  },
  watch: {
    '$route'(to) {
      if (to.name !== "ReportCompare") {
        return;
      }

      this.init();
    }
  }
}
</script>

<style scoped>
.el-row {
  padding-bottom: 20px;
}

</style>
