<template>
  <div>
    <div v-for="(pe, pIndex) in eventData" :key="pe.id">
      <el-card shadow="never" style="margin-top: 8px;background: #F5F6F7;border-radius: 4px;">
        <i @click="expandCard(pIndex)" v-if="pe.expendStatus==='close'" class="el-icon-caret-right" style="color: var(--primary_color)"/>
        <i @click="expandCard(pIndex)" v-else class="el-icon-caret-bottom" style="color: var(--primary_color)"/>
        <span class="project-name" :title="getProjectName(pe.id)">
          {{ getProjectName(pe.id) }}
        </span><br/>
        <div v-if="pe.expendStatus==='open'">
          <el-radio-group v-model="pe.envRadio" style="width: 100%;" @change="envRadioChange(pe.envRadio,pIndex)" class="radio-change">
            <el-radio label="DEFAULT_ENV" style="margin-top: 7px">{{$t('api_test.environment.default_environment') }}</el-radio>
            <el-radio label="CUSTOMIZE_ENV" style="margin-top: 7px">{{$t('api_test.environment.choose_new_environment')}}</el-radio>
          </el-radio-group>
          <el-tag v-show="!pe.showEnvSelect"  v-for="(itemName,index)  in selectedEnvName.get(pe.id)" :key="index" size="mini"
                  style="margin-left: 0; margin-right: 2px;margin-top: 8px">{{ itemName }}</el-tag>
          <el-select v-show="pe.showEnvSelect"  v-model="pe['selectEnv']" :placeholder="$t('api_test.environment.select_environment')"
                     style="margin-top: 8px;width: 100%;" size="small" @change="chooseEnv">
            <el-option v-for="(environment, index) in pe.envs" :key="index"
                       :label="environment.name"
                       :value="environment.id"/>
          </el-select>
        </div>
      </el-card>
    </div>
  </div>
</template>


<script>

import {ENV_TYPE} from "metersphere-frontend/src/utils/constants";
import {environmentGetALL} from "metersphere-frontend/src/api/environment";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {parseEnvironment} from "metersphere-frontend/src/model/EnvironmentModel";
import {getEnvironmentByProjectId} from "@/api/api-environment";

export default {
  name: "EnvSelectPopover",
  components: {MsTag},
  data(){
    return {
      radio:ENV_TYPE.JSON,
      visible: false,
      groups:[],
      selectedEnvName:new Map(),
      showEnvName:false,
      eventData:[],
      evnList:[],
      selectEnvMap:new Map(),
    }
  },
  computed: {
    ENV_TYPE() {
      return ENV_TYPE;
    }
  },
  props:{
    projectIds: Set,
    projectList:Array,
    projectEnvMap:Object,
    caseIdEnvNameMap:Object,
    envMap: Map,
    groupId: {
      type: String,
      default() {
        return "";
      }
    },
    isScenario: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    open(){
      this.initDefaultEnv();
      this.getgroups();
    },
    radioChange(val){
      this.radio = val;
    },
    getProjectName(id) {
      const project = this.projectList.find(p => p.id === id);
      return project ? project.name : "";
    },
    envRadioChange(val,index){
      this.eventData[index].envRadio = val
      this.eventData[index].showEnvSelect = this.eventData[index].envRadio === "CUSTOMIZE_ENV";
    },
    viewGroup() {
      this.visible = true;
    },
    getgroups(){
      environmentGetALL().then(res => {
        let data = res.data;
        this.groups = data ? data : [];
      })
    },
    chooseEnv(val){
      let filter = this.evnList.filter(e => e.id === val);
      this.selectEnvMap.set(filter[0].projectId,val);
      this.$emit('setProjectEnvMap', this.selectEnvMap);
    },
    initDefaultEnv(){
      this.selectedEnvName = new Map();
      this.evnList = [];
      this.projectIds.forEach(d => {
        let item = {id: d, envs: [], selectEnv: "",envRadio:"DEFAULT_ENV",showEnvSelect:false,expendStatus:"open"};
        this.eventData.push(item);
        getEnvironmentByProjectId(d)
          .then(res => {
            let envs = res.data;
            envs.forEach(environment => {
              parseEnvironment(environment);
            });
            // 固定环境列表渲染顺序
            let temp = this.eventData.find(dt => dt.id === d);
            temp.envs = envs;
            envs.forEach(t=>{
              this.evnList.push(t);
            })
            if (this.envMap && this.envMap.size > 0) {
              let envId = this.envMap.get(id);
              // 选中环境是否存在
              temp.selectEnv = envs.filter(e => e.id === envId).length === 0 ? null : envId;
            }
            if (this.isScenario){
              if (this.projectEnvMap)  {
                let projectEnvMapElement = this.projectEnvMap[d];
                if (projectEnvMapElement.length>0) {
                  projectEnvMapElement.forEach(envId => {
                    let filter = envs.filter(e => e.id === envId);
                    if (!this.selectedEnvName.has(d)) {
                      let name = [];
                      name.push(filter[0].name)
                      this.selectedEnvName.set(d,name);
                    } else {
                      this.selectedEnvName.get(d).push(filter[0].name);
                    }
                  });
                }
              }
            } else {
              if (this.caseIdEnvNameMap) {
                let envName = new Set();
                for (let key in this.caseIdEnvNameMap) {
                  envName.add(this.caseIdEnvNameMap[key])
                }
                this.selectedEnvName.set(d,envName);
              }
            }
          });
      })
    },
    expandCard(index){
      if (this.eventData[index].expendStatus === "open") {
        this.eventData[index].expendStatus = "close"
      }else {
        this.eventData[index].expendStatus = "open"
      }
    }
  }

}
</script>

<style scoped>
.mode-span{
  margin-left: 6px;
}


</style>
<style  lang="scss" scoped>
.radio-change:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #606266 !important;
}

</style>
