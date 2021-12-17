<template>
  <div class="compare-class">
    <el-card style="width: 50%;" ref="old">
      <p>1</p>
      <span>v1</span>
      <h4>1</h4>
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
        <el-col :span="12">
          <el-tooltip :content="$t('commons.follow')" placement="bottom"  effect="dark" v-if="!showFollow">
            <i class="el-icon-star-off" style="color: #783987; font-size: 25px; margin-right: 15px;cursor: pointer;position: relative; top: 5px; " />
          </el-tooltip>
          <el-tooltip :content="$t('commons.cancel')" placement="bottom"  effect="dark" v-if="showFollow">
            <i class="el-icon-star-on" style="color: #783987; font-size: 28px;  margin-right: 15px;cursor: pointer;position: relative; top: 5px; "/>
          </el-tooltip>

        </el-col>
      </el-row>

      <el-tabs v-model="active" @tab-click="clickTab">
        <el-tab-pane :label="$t('load_test.basic_config')" class="advanced-config">
          <performance-basic-config :is-read-only="true" :test="oldData" @fileChange="fileChange"  ref="basicConfig" />
        </el-tab-pane>
        <el-tab-pane :label="$t('load_test.pressure_config')" class="advanced-config">
          <performance-pressure-config :is-read-only="true" :test="oldData" :test-id="oldData.id" @fileChange="fileChange" ref="pressureConfig"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('load_test.advanced_config')" class="advanced-config">
          <performance-advanced-config :read-only="true" :test-id="oldData.id" ref="advancedConfig"/>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    <el-card style="width: 50%;" ref="new">
      <p>2</p>
      <span>v2</span>
      <h4>2</h4>
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
        <el-col :span="12">
          <el-tooltip :content="$t('commons.follow')" placement="bottom"  effect="dark" v-if="!newShowFollow">
            <i class="el-icon-star-off" style="color: #783987; font-size: 25px; margin-right: 15px;cursor: pointer;position: relative; top: 5px; " />
          </el-tooltip>
          <el-tooltip :content="$t('commons.cancel')" placement="bottom"  effect="dark" v-if="newShowFollow">
            <i class="el-icon-star-on" style="color: #783987; font-size: 28px;  margin-right: 15px;cursor: pointer;position: relative; top: 5px; "/>
          </el-tooltip>

        </el-col>
      </el-row>

      <el-tabs v-model="active" @tab-click="clickTab">
        <el-tab-pane :label="$t('load_test.basic_config')" class="advanced-config">
          <performance-basic-config :is-read-only="true" :test="newData" @fileChange="fileNewChange"  ref="newBasicConfig" />
        </el-tab-pane>
        <el-tab-pane :label="$t('load_test.pressure_config')" class="advanced-config">
          <performance-pressure-config :is-read-only="true" :test="newData" :test-id="newData.id"  @fileChange="fileNewChange" ref="newPressureConfig" />
        </el-tab-pane>
        <el-tab-pane :label="$t('load_test.advanced_config')" class="advanced-config">
          <performance-advanced-config :read-only="true" :test-id="newData.id" ref="newAdvancedConfig" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
    <button @click="getDiff"></button>
  </div>
</template>

<script>



import EditPerformanceTest from "@/business/components/performance/test/EditPerformanceTest";
import PerformancePressureConfig from "@/business/components/performance/test/components/PerformancePressureConfig";
import PerformanceBasicConfig from "@/business/components/performance/test/components/PerformanceBasicConfig";
import PerformanceAdvancedConfig from "@/business/components/performance/test/components/PerformanceAdvancedConfig";
import {patch} from "@/business/components/performance/v_node_diff";
import Vue from "vue";

const {diff} = require("@/business/components/performance/v_node_diff");


export default{
  name:"DiffVersion",
  components:{
    EditPerformanceTest,
    PerformancePressureConfig,
    PerformanceBasicConfig,
    PerformanceAdvancedConfig,
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
  data(){
    return{
      active: '0',
      oldDataJson:{

      },
      newDataJson:{

      },

    }
  },
  methods:{
    getDiff(){
      let oldVnode = this.$refs.old
      let vnode = this.$refs.new
      //oldVnode.style.backgroundColor = "rgb(241,200,196)";
      console.log(this.$refs.old)
      console.log(this.$refs.new)
      diff(oldVnode,vnode);

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
  created() {

  }
}
</script>
<style scoped>
.compare-class{
  display: flex;
  justify-content:space-between;
}
</style>
