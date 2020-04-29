<template>

  <common-component :title="'测试结果统计'">

    <template>

      <ms-pie-chart :text="'测试结果统计图'" :name="'测试结果'" :data="charData"/>

    </template>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    import MsPieChart from "../../../common/components/MsPieChart";

    export default {
      name: "TestResultChartComponent",
      components: {MsPieChart, CommonComponent},
      data() {
        return {
          charData: [
            {
              value:235, name:'通过',
              itemStyle: {
                color: '#67C23A'
              }
            },
            {
              value:274, name:'阻塞',
              itemStyle: {
                color: '#E6A23C'
              }
            },
            {
              value:310, name:'失败',
              itemStyle: {
                color: '#F56C6C'
              }
            },
            {
              value:335, name:'跳过',
              itemStyle: {
                color: '#909399'
              }
            },
            {
              value:400, name:'未完成',
              itemStyle: {
                color: 'lightskyblue'
              }
            }
          ]
        }
      }
    }
</script>

<style scoped>

  .echarts {
    margin: 0 auto;
  }

</style>
