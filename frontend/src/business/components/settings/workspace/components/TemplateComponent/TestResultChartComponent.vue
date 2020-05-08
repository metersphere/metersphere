<template>

  <common-component :title="'测试结果统计'">

    <template>

      {{executeResult}}/{{charData}}
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
            ["Pass", {value:235, name:'通过', itemStyle: {color: '#67C23A'}}],
            ["Blocking", {value:274, name:'阻塞', itemStyle: {color: '#E6A23C'}}],
            ["Skip", {value:335, name:'跳过', itemStyle: {color: '#909399'}}],
            ["Prepare", {value:265, name:'未开始', itemStyle: {color: '#DEDE10'}}],
            ["Failure", {value:310, name:'失败', itemStyle: {color: '#F56C6C'}}],
            ["Underway", {value:245, name:'进行中', itemStyle: {color: 'lightskyblue'}}]
          ]),
          charData: [],
          isShow: true
        }
      },
      props: {
        executeResult: {
          type: Array
        }
      },
      watch: {
        executeResult() {
          this.charData = [];
          this.executeResult.forEach(item => {
            let data = this.dataMap.get(item.status);
            data.value = item.count;
            this.charData.push(data);
          });
          this.reload();
        }
      },
      created() {
        this.charData.push(this.dataMap.get('Pass'));
        this.charData.push(this.dataMap.get('Blocking'));
        this.charData.push(this.dataMap.get('Skip'));
        this.charData.push(this.dataMap.get('Prepare'));
        this.charData.push(this.dataMap.get('Failure'));
        this.charData.push(this.dataMap.get('Underway'));
      },
      methods: {
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
