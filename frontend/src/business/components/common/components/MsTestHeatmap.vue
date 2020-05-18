<template>
  <el-card>
    <template v-slot:header>
      <span class="title">{{$t('commons.calendar_heatmap')}}</span>
    </template>
    <calendar-heatmap :end-date="endDate" :values="values" :locale="locale"
                      :tooltip-unit="unit"
                      :range-color="colorRange"/>
  </el-card>
</template>

<script>
  export default {
    name: "MsTestHeatmap",
    props: ['values'],
    data() {
      return {
        endDate: new Date(),
        unit: 'tests',
        colorRange: ['#ebedf0', '#c6e48b', '#7bc96f', '#239a3b', '#196127'],
        locale: {
          // 一月 二月 三月 四月 五月 六月 七月 八月 九月 十月 十一月 十二月
          months: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
          // 星期日 Sun. 星期一 Mon. 星期二 Tues. 星期三 Wed. 星期四 Thur. 星期五 Fri. 星期六 Sat.
          days: ['Sun', 'Mon', 'Tues', 'Wed', 'Thur', 'Fri', 'Sat'],
          No: 'No',
          on: ',',
          less: 'Less',
          more: 'More'
        },
      }
    },
    mounted() {
      this.locale.months = [
        this.$t('commons.months_1'),
        this.$t('commons.months_2'),
        this.$t('commons.months_3'),
        this.$t('commons.months_4'),
        this.$t('commons.months_5'),
        this.$t('commons.months_6'),
        this.$t('commons.months_7'),
        this.$t('commons.months_8'),
        this.$t('commons.months_9'),
        this.$t('commons.months_10'),
        this.$t('commons.months_11'),
        this.$t('commons.months_12')
      ];
      this.locale.days = [
        this.$t('commons.weeks_0'),
        this.$t('commons.weeks_1'),
        this.$t('commons.weeks_2'),
        this.$t('commons.weeks_3'),
        this.$t('commons.weeks_4'),
        this.$t('commons.weeks_5'),
        this.$t('commons.weeks_6')
      ];
      this.unit = this.$t('commons.test_unit')
    }
  }
</script>

<style scoped>

</style>
