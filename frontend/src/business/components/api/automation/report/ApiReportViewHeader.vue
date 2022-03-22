<template>
  <header class="report-header">
    <el-row>
      <el-col>
        <span v-if="!debug">
          <el-input v-if="nameIsEdit" size="mini" @blur="handleSave(report.name)" @keyup.enter.native="handleSaveKeyUp" style="width: 200px" v-model="report.name" maxlength="60" show-word-limit/>
          <span v-else>
            <router-link v-if="isSingleScenario" :to="{name: 'ApiAutomation', params: { dataSelectRange: 'edit:' + scenarioId }}">
              {{ report.name }}
            </router-link>
            <span v-else>
              {{ report.name }}
            </span>
            <i v-if="showCancelButton" class="el-icon-edit" style="cursor:pointer" @click="nameIsEdit = true" @click.stop/>
          </span>
        </span>
        <span v-if="report.endTime || report.createTime">
          <span style="margin-left: 10px">{{$t('report.test_start_time')}}：</span>
          <span class="time"> {{ report.createTime | timestampFormatDate }}</span>
          <span style="margin-left: 10px">{{$t('report.test_end_time')}}：</span>
          <span class="time"> {{ report.endTime | timestampFormatDate }}</span>
        </span>
        <el-button v-if="!isPlan && (!debug || exportFlag) && !isTemplate" v-permission="['PROJECT_API_REPORT:READ+EXPORT']" :disabled="isReadOnly" class="export-button" plain type="primary" size="mini" @click="handleExport(report.name)" style="margin-right: 10px">
          {{ $t('test_track.plan_view.export_report') }}
        </el-button>
        <el-popover
          v-if="!isPlan && (!debug || exportFlag) && !isTemplate"
          v-permission="['PROJECT_PERFORMANCE_REPORT:READ+EXPORT']"
          style="margin-right: 10px;float: right;"
          placement="bottom"
          width="300">
          <p>{{ shareUrl }}</p>
          <span style="color: red;float: left;margin-left: 10px;" v-if="application.typeValue">{{
              $t('commons.validity_period')+application.typeValue
            }}</span>
          <div style="text-align: right; margin: 0">
            <el-button type="primary" size="mini" :disabled="!shareUrl"
                       v-clipboard:copy="shareUrl">{{ $t("commons.copy") }}
            </el-button>
          </div>
          <el-button slot="reference" :disabled="isReadOnly" type="danger" plain size="mini"
                     @click="handleShare(report)">
            {{ $t('test_track.plan_view.share_report') }}
          </el-button>
        </el-popover>
        <el-button v-if="showCancelButton" class="export-button" plain  size="mini" @click="returnView()" >
          {{$t('commons.cancel')}}
        </el-button>

      </el-col>
    </el-row>
  </header>
</template>

<script>

import {generateShareInfoWithExpired} from "@/network/share";
import {getCurrentProjectID} from "@/common/js/utils";

export default {
  name: "MsApiReportViewHeader",
  props: {
    report: {},
    debug: Boolean,
    showCancelButton: {
      type: Boolean,
      default: true,
    },
    isTemplate: Boolean,
    exportFlag: {
      type: Boolean,
      default: false,
    },
    isPlan: Boolean
  },
  computed: {
    path() {
      return "/api/test/edit?id=" + this.report.testId;
    },
    scenarioId() {
      if (typeof this.report.scenarioId === 'string') {
        return this.report.scenarioId;
      } else {
        return "";
      }
    },
    isSingleScenario() {
      try {
        JSON.parse(this.report.scenarioId);
        return false;
      } catch (e) {
        return true;
      }
    }
  },
  data() {
    return {
      isReadOnly: false,
      nameIsEdit: false,
      shareUrl: "",
      application:{}
    }
  },
  created() {

  },
  methods: {
    handleExport(name) {
      this.$emit('reportExport', name);
    },
    handleSave(name) {
      this.nameIsEdit = false;
      this.$emit('reportSave', name);
    },
    handleSaveKeyUp($event) {
      $event.target.blur();
    },
    returnView(){
      this.$router.push('/api/automation/report');
    },
    handleShare(report) {
      this.getProjectApplication();
      let pram = {};
      pram.customData = report.id;
      pram.shareType = 'API_REPORT';
      generateShareInfoWithExpired(pram, (data) => {
        let thisHost = window.location.host;
        this.shareUrl = thisHost + "/shareApiReport" + data.shareUrl;
      });
    },
    getProjectApplication(){
      this.$get('/project_application/get/' + getCurrentProjectID()+"/API_SHARE_REPORT_TIME", res => {
        if(res.data){
          let quantity = res.data.typeValue.substring(0, res.data.typeValue.length - 1);
          let unit = res.data.typeValue.substring(res.data.typeValue.length - 1);
          if(unit==='H'){
            res.data.typeValue = quantity+this.$t('commons.date_unit.hour');
          }else
          if(unit==='D'){
            res.data.typeValue = quantity+this.$t('commons.date_unit.day');
          }else
          if(unit==='M'){
            res.data.typeValue = quantity+this.$t('commons.date_unit.month');
          }else
          if(unit==='Y'){
            res.data.typeValue = quantity+this.$t('commons.date_unit.year');
          }
          this.application = res.data;
        }
      });
    },
  }
}
</script>

<style scoped>

.export-button {
  float: right;
  margin-right: 10px;
}

</style>
