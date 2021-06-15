<template>
  <header class="report-header">
    <el-row>
      <el-col>
        <span v-if="!debug">
          <el-input v-if="nameIsEdit" size="mini" @blur="nameIsEdit = false" style="width: 200px" v-model="report.name"/>
          <span v-else>
<!--            <el-link type="info" @click="redirectPage()" target="_blank" style="color: #000000">{{report.name}}-->
<!--            </el-link>-->
            <router-link v-if="isSingleScenario" :to="{name: 'ApiAutomation', params: { dataSelectRange: 'edit:' + scenarioId }}">
              {{ report.name }}
            </router-link>
            <span v-else>
              {{ report.name }}
            </span>
            <i class="el-icon-edit" style="cursor:pointer" @click="nameIsEdit = true" @click.stop/>
          </span>
        </span>
        <span class="time"> {{ report.createTime | timestampFormatDate }}</span>

        <el-button v-if="!debug" v-permission="['PROJECT_API_REPORT:READ+EXPORT']" :disabled="isReadOnly" class="export-button" plain type="primary" size="mini" @click="handleExport(report.name)" style="margin-right: 10px">
          {{$t('test_track.plan_view.export_report')}}
        </el-button>

        <el-button v-if="!debug" :disabled="isReadOnly" class="export-button" plain type="primary" size="mini" @click="handleSave(report.name)" style="margin-right: 10px">
          {{$t('commons.save')}}
        </el-button>

      </el-col>
    </el-row>
  </header>
</template>

<script>

  import {getUUID} from "@/common/js/utils";

  export default {
    name: "MsApiReportViewHeader",
    props: {
      report: {},
      debug: Boolean,
    },
    computed: {
      path() {
        return "/api/test/edit?id=" + this.report.testId;
      },
      scenarioId(){
        if(typeof this.report.scenarioId === 'string'){
          return this.report.scenarioId;
        }else {
          return "";
        }
      },
      isSingleScenario(){
        try {
          JSON.parse(this.report.scenarioId);
          return false;
        } catch(e){
          return true;
        }

      }
    },
    data() {
      return {
        isReadOnly: false,
        nameIsEdit:false,
      }
    },
    methods: {
      handleExport(name) {
        this.$emit('reportExport', name);
      },
      handleSave(name) {
        this.$emit('reportSave', name);
      },
      // redirectPage(){
      //   if(typeof this.report.scenarioId === 'string'){
      //     let uuid = getUUID();
      //     let projectId = getCurrentProjectID();
      //     this.$router.push({name:'ApiAutomation',params:{redirectID:uuid,scenarioId:this.report.scenarioId}});
      //   }
      // },
    }
  }
</script>

<style scoped>

  .export-button {
    float: right;
  }
  .scenario-name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
    width: 100%;
  }


</style>
