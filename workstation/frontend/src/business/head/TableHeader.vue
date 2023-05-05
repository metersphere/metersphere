<template>
  <el-tabs v-model="activeIndex" @tab-click = "handleSelect">
    <el-tab-pane name="track_case" :label="$t('workstation.table_name.track_case')"  v-if="disabled(['PROJECT_TRACK_CASE:READ'])"></el-tab-pane>
    <el-tab-pane name="track_plan" :label="$t('workstation.table_name.track_plan')"  v-if="disabled(['PROJECT_TRACK_PLAN:READ'])"></el-tab-pane>
    <el-tab-pane name="track_review" :label="$t('workstation.table_name.track_review')"  v-if="disabled(['PROJECT_TRACK_REVIEW:READ'])"></el-tab-pane>
    <el-tab-pane name="track_issue" :label= "$t('workstation.table_name.track_issue')" v-if="disabled(['PROJECT_TRACK_ISSUE:READ'])"></el-tab-pane>
    <el-tab-pane name="api_definition" :label= "$t('workstation.table_name.api_definition')" v-if="disabled(['PROJECT_API_DEFINITION:READ'])"></el-tab-pane>
    <el-tab-pane name="api_case" :label= "$t('workstation.table_name.api_case')" v-if="disabled(['PROJECT_API_DEFINITION:READ'])"></el-tab-pane>
    <el-tab-pane name="api_automation" :label= "$t('workstation.table_name.api_automation')" v-if="disabled(['PROJECT_API_SCENARIO:READ'])"></el-tab-pane>
    <el-tab-pane name="performance" :label= "$t('workstation.table_name.performance')" v-if="disabled(['PROJECT_PERFORMANCE_TEST:READ'])"></el-tab-pane>
  </el-tabs>
</template>

<script>
import {hasPermissions} from "metersphere-frontend/src/utils/permission";

export default {
  name:'TableHeader',
  data(){
    return{
      activeIndex: 'track_case',
      todoList:[
        {
          value: 'track_case',
          permission:['PROJECT_TRACK_CASE:READ']
        },
        {
          value: 'track_plan',
          permission:['PROJECT_TRACK_PLAN:READ']
        },{
          value: 'track_review',
          permission:['PROJECT_TRACK_REVIEW:READ']
        },{
          value: 'track_issue',
          permission:['PROJECT_TRACK_ISSUE:READ']
        },{
          value: 'api_definition',
          permission:['PROJECT_API_DEFINITION:READ']
        },{
          value: 'api_automation',
          permission:['PROJECT_API_SCENARIO:READ']
        },{
          value: 'api_case',
          permission:['PROJECT_API_SCENARIO:READ']
        },{
          value: 'performance',
          permission:['PROJECT_PERFORMANCE_TEST:READ']
        }
      ],
      realTodoList:[],
      showItem:true,
    }
  },

  props:{
    selectShow:{
      type: Boolean,
      default: true,
    },
  },
  methods:{
    handleSelect(key, keyPath) {
      this.$emit('rushTableNode', key.name);
    },
    disabled(permissions) {
      if (!permissions) {
        return false;
      }
      return hasPermissions(...permissions);
    },
    setActiveIndex(name){
      this.activeIndex = name
    }
  },
  created() {
    for (let i = 0; i <this.todoList.length; i++) {
      let todo = this.todoList[i];
      if(hasPermissions(...todo.permission)){
        this.realTodoList.push(todo);
      }
    }
    if(this.realTodoList.length>0){
      this.activeIndex  = this.realTodoList[0].value
      this.$emit('rushTableNode', this.activeIndex);
    }

  }
}

</script>

<style scoped>

</style>
