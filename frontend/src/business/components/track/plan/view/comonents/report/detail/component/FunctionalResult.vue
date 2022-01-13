<template>
 <div>
   <el-row >
     <el-col :span="12">
       <ms-doughnut-pie-chart :name="$t('test_track.plan.test_plan_test_case_count')" :data="caseCharData" ref="functionChar"/>
     </el-col>
     <el-col :span="12">
       <ms-doughnut-pie-chart :name="$t('test_track.plan_view.issues_count')" :data="issueCharData"/>
     </el-col>
   </el-row>
 </div>
</template>

<script>

import MsDoughnutPieChart from "@/business/components/common/components/MsDoughnutPieChart";
export default {
  name: "FunctionalResult",
  components: {MsDoughnutPieChart},
  data() {
    return {
      caseDataMap: new Map([
        ["Pass", {name: this.$t('test_track.plan_view.pass'), itemStyle: {color: '#67C23A'}}],
        ["Failure", {name: this.$t('test_track.plan_view.failure'), itemStyle: {color: '#F56C6C'}}],
        ["Blocking", {name: this.$t('test_track.plan_view.blocking'), itemStyle: {color: '#E6A23C'}}],
        ["Skip", {name: this.$t('test_track.plan_view.skip'), itemStyle: {color: '#909399'}}],
        ["Underway", {name: this.$t('test_track.plan.plan_status_running'), itemStyle: {color: 'lightskyblue'}}],
        ["Prepare", {name: this.$t('test_track.plan.plan_status_prepare'), itemStyle: {color: '#DEDE10'}}]
      ]),
      issueDataMap: new Map([
        ["resolved", {name: this.$t('test_track.issue.status_resolved'), itemStyle: {color: '#67C23A'}}],
        ["new", {name: this.$t('test_track.issue.status_new'), itemStyle: {color: '#F56C6C'}}],
        ["closed", {name: this.$t('test_track.issue.status_closed'), itemStyle: {color: '#909399'}}],
      ]),
      caseCharData: [],
      issueCharData: [],
      isShow: true
    }
  },
  props: {
    functionResult: {
      type: Object,
      default() {
        return {
          caseData: [],
          issueData: []
        }
      }
    }
  },
  watch: {
    functionResult() {
      this.getCaseCharData();
    }
  },
  created() {
    this.getCaseCharData();
  },
  methods: {
    getCaseCharData() {
      let caseCharData = [];
      this.functionResult.caseData.forEach(item => {
        let data = this.caseDataMap.get(item.status);
        data.value = item.count;
        caseCharData.push(data);
      });
      this.caseCharData = caseCharData;

      let issueCharData = [];
      let colors = ['#67C23A', '#E6A23C','#DEDE10',
        '#F56C6C','#909399'];
      let usedSet = new Set();

      this.functionResult.issueData.forEach(item => {
        let status = item.status;
        let data = this.issueDataMap.get(status);
        if (!data) {
          data = {name: status, itemStyle: {color: null}};
          if (status === 'new' || status === '新' || status === '待办' || status === 'active' || status === 'created') {
            data.itemStyle.color = '#F56C6C';
            usedSet.add(data.itemStyle.color);
          }
          if (status === '已拒绝' || status === 'reject') {
            data.itemStyle.color = '#909399';
            usedSet.add(data.itemStyle.color);
          }
          if (status === '已关闭' || status === 'close') {
            data.itemStyle.color = '#67C23A';
            usedSet.add(data.itemStyle.color);
          }
          if (!data.itemStyle.color) {
            for (let i = 0; i < colors.length; i++) {
              let color = colors[i];
              if (!usedSet.has(color)) {
                data.itemStyle.color = color;
                usedSet.add(data.itemStyle.color);
                break;
              }
            }
          }
        }
        data.value = item.count;
        issueCharData.push(data);
      });
      this.issueCharData = issueCharData;
    },
  }
}
</script>

<style scoped>
</style>
