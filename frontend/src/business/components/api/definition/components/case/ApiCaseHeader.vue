<template>
  <el-header style="width: 100% ;padding: 0px">
    <el-card>
      <el-row>
        <el-col :span="api.protocol==='HTTP'? 3:5">
          <div class="variable-combine"> {{api.name}}</div>
        </el-col>
        <el-col :span="api.protocol==='HTTP'? 1:3">
          <ms-tag v-if="api.status == 'Prepare'" type="info" effect="plain" :content="$t('test_track.plan.plan_status_prepare')"/>
          <ms-tag v-if="api.status == 'Underway'" type="warning" effect="plain" :content="$t('test_track.plan.plan_status_running')"/>
          <ms-tag v-if="api.status == 'Completed'" type="success" effect="plain" :content="$t('test_track.plan.plan_status_completed')"/>
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
                       class="ms-api-header-select" @change="getApiTest">
              <el-option v-for="grd in priorities" :key="grd.id" :label="grd.name" :value="grd.id"/>
            </el-select>
          </div>
        </el-col>
        <el-col :span="4">
          <div>
            <ms-environment-select
              :project-id="projectId"
              :is-read-only="isReadOnly || isCaseEdit"
              @setEnvironment="setEnvironment"/>
          </div>
        </el-col>
        <el-col :span="3">
          <div class="ms-api-header-select">
            <el-input size="small" :placeholder="$t('api_test.definition.request.select_case')"
                      :disabled="isCaseEdit"
                      v-model="condition.name" @blur="getApiTest" @keyup.enter.native="getApiTest"/>
          </div>
        </el-col>
        <el-col :span="2" v-if="!(isReadOnly || isCaseEdit)">
          <el-dropdown size="small" split-button type="primary" class="ms-api-header-select" @click="addCase"
                       @command="handleCommand">
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
    export default {
      name: "ApiCaseHeader",
      components: {MsEnvironmentSelect, MsTag, ApiEnvironmentConfig},
      data() {
        return {
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
        },
      },
      methods: {
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
        setEnvironment(data) {
          this.$emit('setEnvironment', data);
        }
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


</style>
