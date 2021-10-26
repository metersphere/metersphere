<template>
  <el-header style="width: 100% ;padding: 0px">
    <el-card>
      <el-row>
        <el-col :span="api.protocol==='HTTP'? 7:9">
          <el-tooltip :content="api.name">
            <span class="ms-col-name"> {{api.name}}</span>
          </el-tooltip>
        </el-col>
        <el-col :span="api.protocol==='HTTP'? 6:8">
          <el-tag size="mini" :style="{'background-color': getColor(true, api.method), border: getColor(true, api.method)}" class="api-el-tag">
            {{ api.method}}
          </el-tag>
        </el-col>
        <el-col :span="api.protocol==='HTTP'? 4:0">
          <div class="variable-combine" style="margin-left: 10px">{{api.path ===null ? " " : api.path}}</div>
        </el-col>
        <el-col :span="5">
          <div>
            <ms-environment-select
              :project-id="projectId"
              :is-read-only="isReadOnly"
              :useEnvironment='useEnvironment'
              @setEnvironment="setEnvironment" ref="environmentSelect"/>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </el-header>
</template>

<script>

  import ApiEnvironmentConfig from "../../../test/components/ApiEnvironmentConfig";
  import MsTag from "../../../../common/components/MsTag";
  import MsEnvironmentSelect from "./MsEnvironmentSelect";
  import {API_METHOD_COLOUR} from "../../model/JsonData";
  export default {
    name: "ApiCaseHeader",
    components: {MsEnvironmentSelect, MsTag, ApiEnvironmentConfig},
    data() {
      return {
        methodColorMap: new Map(API_METHOD_COLOUR),
      }
    },
    props: {
      api: Object,
      projectId: String,
      priorities: Array,
      isReadOnly: Boolean,
      useEnvironment: String,
      isCaseEdit: Boolean,
      condition: {
        type: Object,
        default() {
          return {}
        },
      }
    },
    created() {
    },
    methods: {
      refreshEnvironment(){
        this.$refs.environmentSelect.refreshEnvironment();
      },
      setEnvironment(data) {
        if(data){
          this.$emit('setEnvironment', data.id);
        }
      },
      open() {
        this.$refs.searchBar.open();
      },
      addCase() {
        this.$emit('addCase');
        this.refreshEnvironment();
      },
      getColor(enable, method) {
        if (enable) {
          return this.methodColorMap.get(method);
        }
      },
    }
  }
</script>

<style scoped>

  .el-card-btn {
    float: right;
    top: 20px;
    right: 0px;
    padding: 0;
    background: 0 0;
    border: none;
    outline: 0;
    cursor: pointer;
    font-size: 18px;
    margin-left: 30px;
  }

  .variable-combine {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 10px;
  }

  .el-col {
    height: 32px;
    line-height: 32px;
  }

  .ms-api-header-select {
    margin-left: 20px;
    min-width: 100px;
  }

  .el-col {
    height: 32px;
    line-height: 32px;
  }

  .api-el-tag {
    color: white;
  }

  .select-all {
    margin-right: 10px;
  }

  .ms-col-name {
    display: inline-block;
    margin: 0 5px;
    overflow-x: hidden;
    padding-bottom: 0;
    text-overflow: ellipsis;
    vertical-align: middle;
    white-space: nowrap;
    width: 100px;
  }
</style>
