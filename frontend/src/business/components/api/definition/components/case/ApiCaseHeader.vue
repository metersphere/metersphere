<template>
  <el-header style="width: 100% ;padding: 0px">
    <el-card>
      <el-row>
        <el-col :span="api.protocol==='HTTP'? 3:5">
          <div class="variable-combine"> {{api.name}}</div>
        </el-col>
        <el-col :span="api.protocol==='HTTP'? 1:3">
          <el-tag size="mini" :style="{'background-color': getColor(true, api.method), border: getColor(true, api.method)}" class="api-el-tag">
            {{ api.method}}
          </el-tag>
        </el-col>
        <el-col :span="api.protocol==='HTTP'? 4:0">
          <div class="variable-combine" style="margin-left: 10px">{{api.path ===null ? " " : api.path}}</div>
        </el-col>
        <el-col :span="2" v-if="!isCaseEdit">
          <div>{{$t('test_track.plan_view.case_count')}}：{{apiCaseList.length}}</div>
        </el-col>
        <el-col :span="3">
          <div>
            <el-select size="small" :placeholder="$t('api_test.definition.request.grade_info')" v-model="condition.priority"
                       :disabled="isCaseEdit"
                       class="ms-api-header-select" @change="getApiTest" clearable>
              <el-option v-for="grd in priorities" :key="grd.id" :label="grd.name" :value="grd.id"/>
            </el-select>
          </div>
        </el-col>

        <el-col :span="3">
          <div class="ms-api-header-select" style="margin-right: 20px">
            <el-input size="small" :placeholder="$t('api_test.definition.request.select_case')"
                      :disabled="isCaseEdit"
                      v-model="condition.name" @blur="getApiTest" @keyup.enter.native="getApiTest" />
          </div>
        </el-col>

        <el-col :span="4">
          <div>
            <ms-environment-select
              :project-id="projectId"
              :is-read-only="isReadOnly"
              @setEnvironment="setEnvironment"/>
          </div>
        </el-col>

        <el-col :span="2" v-if="!(isReadOnly || isCaseEdit)">
          <el-dropdown size="small" split-button type="primary" class="ms-api-header-select" @click="addCase"
                       @command="handleCommand" v-tester>
            +{{$t('api_test.definition.request.case')}}
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="run">{{$t('commons.test')}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </el-col>
      </el-row>
    </el-card>
  </el-header>
</template>

<script>

  import ApiEnvironmentConfig from "../../../test/components/ApiEnvironmentConfig";
  import {parseEnvironment} from "../../../test/model/EnvironmentModel";
  import MsTag from "../../../../common/components/MsTag";
  import MsEnvironmentSelect from "./MsEnvironmentSelect";
  import {API_METHOD_COLOUR} from "../../model/JsonData";

  export default {
    name: "ApiCaseHeader",
    components: {MsEnvironmentSelect, MsTag, ApiEnvironmentConfig},
    data() {
      return {
        environments: [],
        environment: {},
        methodColorMap: new Map(API_METHOD_COLOUR),
      }
    },
    props: {
      api: Object,
      projectId: String,
      priorities: Array,
      apiCaseList: Array,
      isReadOnly: Boolean,
      isCaseEdit: Boolean,
      condition: {
        type: Object,
        default() {
          return {}
        }
      }
    },
    created() {
      this.environment = undefined;
      this.getEnvironments();
    },
    watch: {
      environment() {
        this.$emit('setEnvironment', this.environment);
      }
    },
    methods: {
      getEnvironments() {
        if (this.projectId) {
          this.$get('/api/environment/list/' + this.projectId, response => {
            this.environments = response.data;
            this.environments.forEach(environment => {
              parseEnvironment(environment);
            });
          });
        } else {
          this.environment = undefined;
        }
      },
      openEnvironmentConfig() {
        if (!this.projectId) {
          this.$error(this.$t('api_test.select_project'));
          return;
        }
        this.$refs.environmentConfig.open(this.projectId);
      },
      environmentChange(value) {
        for (let i in this.environments) {
          if (this.environments[i].id === value) {
            this.environment = this.environments[i];
            break;
          }
        }
      },
      environmentConfigClose() {
        this.getEnvironments();
      },
      setEnvironment(data) {
        this.$emit('setEnvironment', data);
      },
      getApiTest() {
        this.$emit('getApiTest');
      },
      addCase() {
        this.$emit('addCase');
      },
      handleCommand(e) {
        if (e === "run") {
          this.$emit('batchRun');
        }
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

</style>
