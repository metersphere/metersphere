<template>
  <ms-container>
    <ms-main-container>
      <el-card>
        <calendar-heatmap :end-date="endDate" :values="values" :locale="locale"
                          tooltip-unit="tests"
                          :range-color="colorRange"/>
      </el-card>
    </ms-main-container>
  </ms-container>

</template>

<script>
  import MsContainer from "../../common/components/MsContainer";
  import MsMainContainer from "../../common/components/MsMainContainer";

  export default {
    name: "PerformanceTestHome",
    components: {MsMainContainer, MsContainer},
    data() {
      return {
        values: [],
        endDate: new Date(),
        colorRange: ['#ebedf0', '#dae2ef', '#c0ddf9', '#73b3f3', '#3886e1', '#17459e'],
        locale: {
          // 一月 二月 三月 四月 五月 六月 七月 八月 九月 十月 十一月 十二月
          months: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
          // 星期日 Sun. 星期一 Mon. 星期二 Tues. 星期三 Wed. 星期四 Thur. 星期五 Fri. 星期六 Sat.
          days: ['Sun', 'Mon', 'Tues', 'Wed', 'Thur', 'Fri', 'Sat'],
          No: 'No',
          on: 'on',
          less: 'Less',
          more: 'More'
        },
      }
    },
    mounted() {
      this.$get('/performance/dashboard/tests', response => {
        this.values = response.data;
      });
    },
  }
</script>

<style scoped>

</style>
