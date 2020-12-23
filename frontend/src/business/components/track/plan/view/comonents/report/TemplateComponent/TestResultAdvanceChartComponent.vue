<template>

  <common-component :title="$t('test_track.plan_view.result_statistics')">

    <div class="char-component">
      <div class="char-item" v-if="showFunctional">
        <ms-pie-chart v-if="isShow" :text="'功能测试用例'"
                      :name="$t('test_track.plan_view.test_result')" :data="functionalCharData"/>
      </div>

      <div class="char-item" v-if="showApi">
        <ms-pie-chart v-if="isShow" :text="'接口测试用例'"
                      :name="$t('test_track.plan_view.test_result')" :data="apiCharData"/>
      </div>

      <div class="char-item"  v-if="showScenario">
        <ms-pie-chart v-if="isShow" :text="'场景测试用例'"
                      :name="$t('test_track.plan_view.test_result')" :data="scenarioCharData"/>
      </div>
    </div>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    import MsPieChart from "../../../../../../common/components/MsPieChart";

    export default {
      name: "TestResultAdvanceChartComponent",
      components: {MsPieChart, CommonComponent},
      data() {
        return {
          dataMap: new Map([
            ["Pass", {name: this.$t('test_track.plan_view.pass'), itemStyle: {color: '#67C23A'}}],
            ["Failure", {name: this.$t('test_track.plan_view.failure'), itemStyle: {color: '#F56C6C'}}],
            ["Blocking", {name: this.$t('test_track.plan_view.blocking'), itemStyle: {color: '#E6A23C'}}],
            ["Skip", {name: this.$t('test_track.plan_view.skip'), itemStyle: {color: '#909399'}}],
            ["Underway", {name: this.$t('test_track.plan.plan_status_running'), itemStyle: {color: 'lightskyblue'}}],
            ["Prepare", {name: this.$t('test_track.plan.plan_status_prepare'), itemStyle: {color: '#DEDE10'}}]
          ]),
          functionalCharData: [],
          apiCharData: [],
          scenarioCharData: [],
          isShow: true
        }
      },
      props: {
        executeResult: {
          type: Object,
          default() {
            return {
              functionalResult: [
                {status: 'Pass', count: '235'},
                {status: 'Failure', count: '310'},
                {status: 'Blocking', count: '274'},
                {status: 'Skip', count: '335'},
                {status: 'Underway', count: '245'},
                {status: 'Prepare', count: '265'},
              ],
              apiResult: [
                {status: 'Pass', count: '235'},
                {status: 'Failure', count: '310'},
                {status: 'Underway', count: '245'},
              ],
              scenarioResult: [
                {status: 'Pass', count: '205'},
                {status: 'Failure', count: '350'},
                {status: 'Underway', count: '110'},
              ],
            }
          }
        }
      },
     computed: {
       showFunctional() {
         return this.executeResult.functionalResult.length > 0 || (this.executeResult.apiResult.length <= 0 && this.executeResult.scenarioResult.length <= 0);
       },
       showApi() {
         return this.executeResult.apiResult.length > 0;
       },
       showScenario() {
         return this.executeResult.scenarioResult.length > 0;
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
          this.getFunctionalCharData();
          this.getApiCharData();
          this.getScenarioCharData();
          this.reload();
        },
        getFunctionalCharData() {
          this.functionalCharData = [];
          if (this.executeResult.functionalResult) {
            this.executeResult.functionalResult.forEach(item => {
              let data = this.dataMap.get(item.status);
              data.value = item.count;
              this.functionalCharData.push(data);
            });
          }
        },
        getApiCharData() {
          this.apiCharData = [];
          if (this.executeResult.apiResult) {
            this.executeResult.apiResult.forEach(item => {
              let data = this.dataMap.get(item.status);
              data.value = item.count;
              this.apiCharData.push(data);
            });
          }
        },
        getScenarioCharData() {
          this.scenarioCharData = [];
          if (this.executeResult.apiResult) {
            this.executeResult.scenarioResult.forEach(item => {
              let data = this.dataMap.get(item.status);
              data.value = item.count;
              this.scenarioCharData.push(data);
            });
          }
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

  .char-item {
    display: inline-block;
  }

  .char-component {
    text-align: center;
  }

</style>
