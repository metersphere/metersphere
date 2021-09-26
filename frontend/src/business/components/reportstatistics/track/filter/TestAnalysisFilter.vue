<template>
  <div v-loading="loading">
    <el-card :style="{height: h + 'px'}" class="ms-card">
      <el-row style="padding-top: 10px">
        <p class="tip"><span style="margin-left: 5px"></span> {{$t('commons.report_statistics.options')}}</p>
      </el-row>
      <el-row class="ms-row">
        <p>{{$t('commons.report_statistics.type')}}</p>
        <el-checkbox v-model="option.createCase">{{$t('commons.report_statistics.add_case')}}</el-checkbox>
        <el-checkbox v-model="option.updateCase">{{$t('commons.report_statistics.change_case')}}</el-checkbox>
      </el-row>
      <el-row class="ms-row">
        <p> {{$t('api_monitor.date')}}</p>
        <el-date-picker
          size="small"
          v-model="option.times"
          type="datetimerange"
          value-format="timestamp"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :picker-options="datePickerOptions"
          style="width: 100%">
        </el-date-picker>
      </el-row>
      <el-row class="ms-row">
        <p>{{$t('commons.project')}}</p>
        <ms-select-tree size="small" :data="items" :default-key="projectDefaultKey" @getValue="setProjects" :obj="obj" clearable checkStrictly multiple ref="projectSelector"/>
      </el-row>
      <el-row class="ms-row">
        <p>{{$t('test_track.module.module')}}</p>
        <ms-select-tree size="small" :data="modules" :default-key="moduleDefaultKey" :disabled="disabled" @getValue="setModules" :obj="moduleObj" clearable checkStrictly multiple ref="moduleSelector"/>
      </el-row>
      <el-row class="ms-row">
        <p>{{$t('api_test.automation.case_level')}}</p>
        <el-select class="ms-http-select" size="small" v-model="option.prioritys" multiple style="width: 100%">
          <el-option v-for="item in priorityFilters" :key="item.id" :label="item.label" :value="item.id"/>
        </el-select>
      </el-row>
      <el-row class="ms-row">
        <p>{{$t('test_track.case.maintainer')}}</p>
        <ms-select-tree size="small" :data="maintainerOptions" :default-key="userDefaultKey" @getValue="setUsers" :obj="moduleObj" clearable checkStrictly multiple ref="userSelector"/>
      </el-row>

    </el-card>
  </div>
</template>

<script>
  import {getCurrentProjectID} from "@/common/js/utils";
  import MsSelectTree from "@/business/components/common/select-tree/SelectTree";

  export default {
    name: "TestAnalysisTable",
    components: {MsSelectTree},
    data() {
      return {
        option: {createCase: true, updateCase: true, projects: [], times: [new Date().getTime() - 6 * 24 * 3600 * 1000, new Date().getTime()]},
        h: document.documentElement.clientHeight + 80,
        disabled: false,
        loading: false,
        result: {},
        items: [],
        projectDefaultKey:[],
        moduleDefaultKey:[],
        userDefaultKey:[],
        modules: [],
        maintainerOptions: [],
        priorityFilters: [
          {id: 'P0', label: 'P0'},
          {id: 'P1', label: 'P1'},
          {id: 'P2', label: 'P2'},
          {id: 'P3', label: 'P3'}
        ],
        syncReport: true,
        moduleObj: {
          id: 'id',
          label: 'name',
        },
        obj: {
          id: 'id',
          label: 'label',
        },
        datePickerOptions: {
          disabledDate: (time) => {
            let nowDate = new Date();
            let oneDay = 1000 * 60 * 60 * 24;
            let oneYearLater = new Date(nowDate.getTime() + (oneDay * 365));
            return time.getTime() > nowDate || time.getTime() > oneYearLater;//注意是||不是&&
          }
        },
      }
    },
    created() {
      this.init();
      this.initUsers();
    },
    watch: {
      option: {
        handler: function () {
          if(this.syncReport){
            this.$emit('filterCharts', this.option);
          }
        },
        deep: true
      }
    },
    methods: {
      initSelectOption(opt){
        if(opt){
          this.syncReport = false;
          this.loading = true;
          this.option = opt;
          if(opt.projects){
            this.projectDefaultKey = opt.projects;
          }else {
            this.projectDefaultKey = [];
          }
          if(opt.modules && this.projectDefaultKey.length === 1){
            this.moduleDefaultKey = opt.modules;
          }else {
            this.moduleDefaultKey = [];
          }
          if(opt.users){
            this.userDefaultKey = opt.users;
          }else {
            this.userDefaultKey = [];
          }
          this.$nextTick(() => {
            this.loading = false;
            this.syncReport = true;
          });
        }
      },
      init: function () {
        this.result = this.$get("/project/listAll", response => {
          let projects = response.data;
          if (projects) {
            this.items = [];
            projects.forEach(item => {
              let data = {id: item.id, label: item.name};
              this.items.push(data);
            })
          }
        })
      },
      onTimeChange() {
        if (this.option.times[1] > new Date().getTime()) {
          this.$error("结束时间不能超过当前时间");
        }
      },
      initModule() {
        this.result = this.$get("/case/node/list/" + this.option.projects[0], response => {
          this.modules = response.data;
          this.$refs.moduleSelector.setKeys(this.moduleDefaultKey);
        })
      },
      initUsers() {
        this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
          this.maintainerOptions = response.data;
        });
      },
      setProjects(key, data) {//获取子组件值
        if(!key || key === ""){
          key = [];
        }
        this.option.projects = key;
        this.modules = [];
        if (key && key.length > 1) {
          this.moduleDefaultKey = [];
          this.disabled = true;
        } else {
          this.disabled = false;
        }
        if (this.option.projects && this.option.projects.length == 1) {
          this.initModule();
        }
        if(this.syncReport){
          this.$emit('filterCharts', this.option);
        }
      },
      setModules(key, data) {//获取子组件值
        if(!key || key === ""){
          key = [];
        }
        this.option.modules = key;
        if(this.syncReport){
          this.$emit('filterCharts', this.option);
        }
      },
      setUsers(key, data) {//获取子组件值
        if(!key || key === ""){
          key = [];
        }
        this.option.users = key;
        if(this.syncReport){
          this.$emit('filterCharts', this.option);
        }
      },
      getOption(){
        return this.option;
      }
    },
  }
</script>

<style scoped>

  .tip {
    float: left;
    font-size: 14px;
    border-radius: 2px;
    border-left: 2px solid #783887;
    margin: 0px 10px 0px;
  }

  .ms-row {
    margin: 0px 10px 0px;
  }

  .ms-card {
    width: 480px;
  }

</style>
