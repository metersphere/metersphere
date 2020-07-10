<template>
    <div class="schedule-config">
      <div>
        <span class="cron-ico">
          <i class="el-icon-date" size="small"></i>
          <span class="character" @click="scheduleEdit">SCHEDULER</span>
        </span>
        <el-switch :disabled="!schedule.value" v-model="schedule.enable" @change="scheduleChange"/>
        <ms-schedule-edit :schedule="schedule" :save="save" :custom-validate="customValidate" ref="scheduleEdit"/>
        <crontab-result v-show="false" :ex="schedule.value" ref="crontabResult" @resultListChange="resultListChange"/>
      </div>
      <div>
        <span :class="{'disable-character': !schedule.enable}"> {{$t('schedule.next_execution_time')}}：{{this.recentList.length > 0 ? this.recentList[0] : $t('schedule.not_set')}} </span>
      </div>
    </div>
</template>

<script>
    import MsScheduleEdit from "./MsScheduleEdit";
    import CrontabResult from "../cron/CrontabResult";

    function defaultCustomValidate() {return {pass: true};}

    export default {
      name: "MsScheduleConfig",
      components: {CrontabResult, MsScheduleEdit},
      data() {
        return {
          recentList: [],
        }
      },
      props: {
        save: Function,
        schedule: {},
        checkOpen: {
          type: Function,
          default() {
            return {
              checkOpen() {return true;}
            }
          }
        },
        customValidate: {
          type: Function,
          default: defaultCustomValidate
        },
      },
      methods: {
        scheduleEdit() {
          if (!this.checkOpen()) {
            return;
          }
          this.$refs.scheduleEdit.open();
        },
        scheduleChange() {
          this.$emit('scheduleChange');
        },
        resultListChange(resultList) {
          this.recentList = resultList;
        },
        flashResultList() {
          this.$refs.crontabResult.expressionChange();
        }
      }
    }
</script>

<style scoped>

  .schedule-config {
    float: right;
    width: 250px;
    height: 15px;
    line-height: 25px;
  }

  .el-icon-date {
    font-size: 20px;
    margin-left: 5px;
  }

  .character {
    font-weight: bold;
    margin: 0 5px;
  }

  .disable-character {
    color: #cccccc;
  }

  .el-switch {
    margin: 0 5px;
  }

  .cron-ico {
    cursor: pointer;
  }

</style>
