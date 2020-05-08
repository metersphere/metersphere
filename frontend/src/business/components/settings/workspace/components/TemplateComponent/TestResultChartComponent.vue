<template>

  <common-component :title="'测试结果统计'">

    <template>

      <ms-pie-chart v-if="isShow" :text="'测试结果统计图'" :name="'测试结果'" :data="charData"/>

    </template>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    import MsPieChart from "../../../../common/components/MsPieChart";

    export default {
      name: "TestResultChartComponent",
      components: {MsPieChart, CommonComponent},
      data() {
        return {
          dataMap: new Map([
            ["Pass", {name:'通过', itemStyle: {color: '#67C23A'}}],
            ["Blocking", {name:'阻塞', itemStyle: {color: '#E6A23C'}}],
            ["Skip", {name:'跳过', itemStyle: {color: '#909399'}}],
            ["Prepare", {name:'未开始', itemStyle: {color: '#DEDE10'}}],
            ["Failure", {name:'失败', itemStyle: {color: '#F56C6C'}}],
            ["Underway", {name:'进行中', itemStyle: {color: 'lightskyblue'}}]
          ]),
          charData: [],
          isShow: true
        }
      },
      props: {
        executeResult: {
          type: Array,
          default() {
            return [
              {status: 'Pass', count: '235'},
              {status: 'Blocking', count: '274'},
              {status: 'Skip', count: '335'},
              {status: 'Prepare', count: '265'},
              {status: 'Failure', count: '310'},
              {status: 'Underway', count: '245'},
            ]
          }
        }
      },
      watch: {
        executeResult() {
          this.getCharData();
        }
      },
      created() {
        this.getCharData();
      },
      methods: {
        getCharData() {
          this.charData = [];
          this.executeResult.forEach(item => {
            let data = this.dataMap.get(item.status);
            data.value = item.count;
            this.charData.push(data);
          });
          this.reload();
        },
        reload() {
          this.isShow = false;
          this.$nextTick(function () {
            this.isShow = true;
          })
        }
      }
    }
</script>

<style scoped>

  .echarts {
    margin: 0 auto;
  }

</style>
