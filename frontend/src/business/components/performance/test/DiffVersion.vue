<template>

<div v-loading="isReloadData">
  <div class="caall"></div>
  <el-row>
    <el-col :span="12">
      <el-tag>当前{{oldData.versionName }}</el-tag><span style="margin-left: 10px">{{oldData.userName}}</span><span style="margin-left: 10px">{{oldData.createTime | timestampFormatDate }}</span>
    </el-col>
    <el-col :span="12">
      <el-tag>{{ newData.versionName }}</el-tag><span style="margin-left: 10px">{{newData.userName}}</span><span style="margin-left: 10px">{{newData.createTime | timestampFormatDate }}</span>
    </el-col>
  </el-row>
  <div  class="compare-class" id="vdiff" ref="all" >
    <el-card style="width: 50%;" ref="old" id="old" >
      <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>
      <el-row>
        <el-col :span="12">
          <el-form :inline="true">
            <el-form-item :label="$t('load_test.name') ">
              <el-input :disabled="true" :placeholder="$t('load_test.input_name')" v-model="oldData.name"
                        class="input-with-select"
                        size="small"
                        maxlength="30" show-word-limit/>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>

      <ms-form-divider :title="$t('load_test.basic_config')"/>
      <el-row>
        <performance-basic-config :is-read-only="true" :test="oldData" @fileChange="fileChange"  ref="basicConfig" />
      </el-row>
      <ms-form-divider :title="$t('load_test.pressure_config')"/>
      <el-row>
        <performance-pressure-config :is-read-only="true" :test="oldData" :test-id="oldData.id" @fileChange="fileChange" ref="pressureConfig"/>
      </el-row>
      <ms-form-divider :title="$t('load_test.advanced_config')"/>
      <el-row>
        <performance-advanced-config :read-only="true" :test-id="oldData.id" ref="advancedConfig"/>
      </el-row>

    </el-card>
    <el-card style="width: 50%;" ref="new" id="new" >
      <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>
      <el-row>
        <el-col :span="12">
          <el-form :inline="true">
            <el-form-item :label="$t('load_test.name') ">
              <el-input :disabled="true" :placeholder="$t('load_test.input_name')" v-model="newData.name"
                        class="input-with-select"
                        size="small"
                        maxlength="30" show-word-limit/>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>

      <ms-form-divider :title="$t('load_test.basic_config')"/>
      <el-row>
        <performance-basic-config :is-read-only="true" :test="newData" @fileChange="fileNewChange"  ref="newBasicConfig" />
      </el-row>
      <ms-form-divider :title="$t('load_test.pressure_config')"/>
      <el-row>
        <performance-pressure-config :is-read-only="true" :test="newData" :test-id="newData.id"  @fileChange="fileNewChange" ref="newPressureConfig" />
      </el-row>
      <ms-form-divider :title="$t('load_test.advanced_config')"/>
      <el-row>
        <performance-advanced-config :read-only="true" :test-id="newData.id" ref="newAdvancedConfig" />
      </el-row>

    </el-card>
  </div>

</div>


</template>

<script>

import EditPerformanceTest from "@/business/components/performance/test/EditPerformanceTest";
import PerformancePressureConfig from "@/business/components/performance/test/components/PerformancePressureConfig";
import PerformanceBasicConfig from "@/business/components/performance/test/components/PerformanceBasicConfig";
import PerformanceAdvancedConfig from "@/business/components/performance/test/components/PerformanceAdvancedConfig";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";

const {diff} = require("@/business/components/performance/v_node_diff");

export default{
  name:"DiffVersion",
  components:{
    EditPerformanceTest,
    PerformancePressureConfig,
    PerformanceBasicConfig,
    PerformanceAdvancedConfig,
    MsFormDivider,
  },
  props:{
    oldData:{
      type:Object
    },
    newData:{
      type:Object
    },
    showFollow:{
      type:Boolean
    },
    newShowFollow:{
      type:Boolean
    }

  },
  watch:{
  },
  data(){
    return{
      active: '0',
      isReloadData:true,
    }
  },
  methods:{
    getDiff(){
      let oldVnode = this.$refs.old
      let vnode = this.$refs.new
      let oldColor = "";
      let newColor = "";
      if(this.oldData.createTime>this.newData.createTime){
        oldColor = "rgb(121, 225, 153,0.3)";
        newColor = "rgb(241,200,196,0.45)"
      }else{
        oldColor = "rgb(241,200,196,0.45)"
        newColor = "rgb(121, 225, 153,0.3)";
      }
      diff(oldVnode,vnode,oldColor,newColor);
      this.isReloadData = false;
    },
    clickTab(tab) {
      if (tab.index === '1') {
        this.$refs.pressureConfig.calculateTotalChart();
      }
    },
    fileChange(threadGroups) {
      let handler = this.$refs.pressureConfig;
      let csvSet = new Set;
      threadGroups.forEach(tg => {
        tg.threadNumber = tg.threadNumber || 10;
        tg.duration = tg.duration || 10;
        tg.durationHours = Math.floor(tg.duration / 3600);
        tg.durationMinutes = Math.floor((tg.duration / 60 % 60));
        tg.durationSeconds = Math.floor((tg.duration % 60));
        tg.rampUpTime = tg.rampUpTime || 5;
        tg.step = tg.step || 5;
        tg.rpsLimit = tg.rpsLimit || 10;
        tg.threadType = tg.threadType || 'DURATION';
        tg.iterateNum = tg.iterateNum || 1;
        tg.iterateRampUp = tg.iterateRampUp || 10;

        if (tg.csvFiles) {
          tg.csvFiles.map(item => csvSet.add(item));
        }
      });
      let csvFiles = [];
      for (const f of csvSet) {
        csvFiles.push({name: f, csvSplit: false, csvHasHeader: true});
      }

      this.$set(handler, "threadGroups", threadGroups);

      this.$refs.basicConfig.threadGroups = threadGroups;
      this.$refs.pressureConfig.threadGroups = threadGroups;
      this.$refs.advancedConfig.csvFiles = csvFiles;

      this.$refs.pressureConfig.resourcePoolChange();
      handler.calculateTotalChart();
    },
    fileNewChange(threadGroups) {
      let handler = this.$refs.newPressureConfig;
      let csvSet = new Set;
      threadGroups.forEach(tg => {
        tg.threadNumber = tg.threadNumber || 10;
        tg.duration = tg.duration || 10;
        tg.durationHours = Math.floor(tg.duration / 3600);
        tg.durationMinutes = Math.floor((tg.duration / 60 % 60));
        tg.durationSeconds = Math.floor((tg.duration % 60));
        tg.rampUpTime = tg.rampUpTime || 5;
        tg.step = tg.step || 5;
        tg.rpsLimit = tg.rpsLimit || 10;
        tg.threadType = tg.threadType || 'DURATION';
        tg.iterateNum = tg.iterateNum || 1;
        tg.iterateRampUp = tg.iterateRampUp || 10;

        if (tg.csvFiles) {
          tg.csvFiles.map(item => csvSet.add(item));
        }
      });
      let csvFiles = [];
      for (const f of csvSet) {
        csvFiles.push({name: f, csvSplit: false, csvHasHeader: true});
      }

      this.$set(handler, "threadGroups", threadGroups);

      this.$refs.newBasicConfig.threadGroups = threadGroups;
      this.$refs.newPressureConfig.threadGroups = threadGroups;
      this.$refs.newAdvancedConfig.csvFiles = csvFiles;

      this.$refs.newPressureConfig.resourcePoolChange();
      handler.calculateTotalChart();
    },
  },
  updated() {

 },
  mounted() {
    this.$nextTick(function () {
      setTimeout(this.getDiff,(this.$refs.old.$children.length-2)*1000)
    })
  }

}

</script>
<style scoped>
.compare-class{
  display: flex;
  justify-content:space-between;

}
.caall{
  position: absolute;
  width: 100%;
  height: 400%;
  z-index: 10;
  background: rgba(0,0,0,0);
}


</style>
