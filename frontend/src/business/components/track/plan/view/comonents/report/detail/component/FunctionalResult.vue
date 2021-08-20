<template>
 <div>
   <el-row >
     <el-col :span="12">
       <ms-doughnut-pie-chart :name="$t('功能用例数')" :data="caseCharData" ref="functionChar"/>
     </el-col>
     <el-col :span="12">
       <ms-doughnut-pie-chart :name="$t('缺陷数')" :data="issueCharData"/>
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
        ["resolved", {name: this.$t('已解决'), itemStyle: {color: '#67C23A'}}],
        ["new", {name: this.$t('新建'), itemStyle: {color: '#F56C6C'}}],
        ["closed", {name: this.$t('已关闭'), itemStyle: {color: '#909399'}}],
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
      let colors = ['#F56C6C', '#67C23A', '#E6A23C', '#909399', 'lightskyblue', '#DEDE10'];
      let usedSet = new Set();

      this.functionResult.issueData.forEach(item => {
        let status = item.status;
        let data = this.issueDataMap.get(status);
        if (!data) {
          data = {name: status, itemStyle: {color: null}};
          // if (status === 'new' || status === '新' | status === '待办' | status === 'active') {
          //   data.color = '#F56C6C';
          // }
          if (!data.itemStyle.color) {
            for (let i = 0; i < colors.length; i++) {
              let color = colors[i];
              if (!usedSet.has(color)) {
                data.itemStyle.color = color;
                break;
              }
            }
          }
          usedSet.add(data.itemStyle.color);
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
