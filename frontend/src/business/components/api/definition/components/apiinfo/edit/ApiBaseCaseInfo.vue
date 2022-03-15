<template>
  <!-- 基础信息 -->
  <div class="base-info">
    <el-card class="box-card" style="border: 0px">
      <div slot="header" class="clearfix">
        <span>{{ $t('test_track.plan_view.base_info') }}</span>
      </div>
      <div>
        <el-form :model="api" :rules="rule" ref="httpForm" label-width="80px" label-position="left">
          <!--模块-->
          <el-row>
            <el-col>
              <el-form-item :label="$t('test_track.module.module')" prop="moduleId">
                <ms-select-tree size="small" :data="moduleOptions" :defaultKey="api.moduleId" @getValue="setModule" :disabled="true"
                                :obj="moduleObj" clearable checkStrictly/>
              </el-form-item>
            </el-col>
          </el-row>

          <!--名称-->
          <el-row>
            <el-col>
              <el-form-item :label="$t('commons.name')" prop="name">
                <el-input class="ms-http-input" size="small" v-model="api.name"/>
              </el-form-item>
            </el-col>
          </el-row>
          <!--责任人-->
          <el-row>
            <el-col>
              <el-form-item :label="$t('api_test.definition.request.responsible')" prop="userId">
                <el-select v-model="api.userId"
                           :placeholder="$t('api_test.definition.request.responsible')" filterable size="small"
                           class="ms-http-select">
                  <el-option
                    v-for="item in maintainerOptions"
                    :key="item.id"
                    :label="item.name + ' (' + item.id + ')'"
                    :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <!--状态-->
          <el-row>
            <el-col>
              <el-form-item :label="$t('commons.status')" prop="status">
                <el-select class="ms-http-select" size="small" v-model="api.status">
                  <el-option v-for="item in options" :key="item.id" :label="$t(item.label)" :value="item.id"/>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <!--用例等级-->
          <el-row>
            <el-form-item :label="$t('api_test.automation.case_level')" prop="priority">
              <el-select size="mini" v-model="api.priority" class="ms-api-select">
                <el-option v-for="grd in priorities" :key="grd.id" :label="grd.name" :value="grd.id"/>
              </el-select>
            </el-form-item>
          </el-row>
          <!--标签-->
          <el-row>
            <el-col>
              <el-form-item :label="$t('commons.tag')" prop="tag" class="not-unique-form-item">
                <ms-input-tag :currentScenario="api" ref="tag" v-model="api.tags"/>
              </el-form-item>
            </el-col>
          </el-row>
          <!--描述-->
          <el-row>
            <el-col>
              <el-form-item :label="$t('commons.description')" prop="description" class="not-unique-form-item">
                <el-input class="ms-http-textarea"
                          v-model="api.description"
                          type="textarea"
                          :autosize="{ minRows: 1, maxRows: 10}"
                          :rows="1" size="small"/>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script>

import {getCurrentProjectID} from "@/common/js/utils";
import {API_STATUS, PRIORITY, REQ_METHOD} from "@/business/components/api/definition/model/JsonData";
import MsSelectTree from "@/business/components/common/select-tree/SelectTree";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";

export default {
  name: "ApiBaseCaseInfo",
  components: {MsSelectTree, MsInputTag},
  props: {
    api: {},
    moduleOptions: [],
    isCase: Boolean,
    currentProtocol: String,
  },
  data() {
    let validateURL = (rule, value, callback) => {
      if (!this.httpForm.path.startsWith("/") || this.httpForm.path.match(/\s/) != null) {
        callback(this.$t('api_test.definition.request.path_valid_info'));
      }
      callback();
    };
    return {
      rule: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
        ],
        path: [{required: true, message: this.$t('api_test.definition.request.path_info'), trigger: 'blur'}, {
          validator: validateURL,
          trigger: 'blur'
        }],
        priority: [{required: true, message: this.$t('test_track.case.input_priority'), trigger: 'change'}],
        userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        moduleId: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
        status: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
      },
      maintainerOptions: [],
      reqOptions: REQ_METHOD,
      options: API_STATUS,
      priorities: PRIORITY,
      moduleObj: {
        id: 'id',
        label: 'name',
      },
    }
  },
  created() {
    this.getMaintainerOptions();
  },
  methods: {
    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.maintainerOptions = response.data;
      });
    },
    setModule(id, data) {
      this.api.moduleId = id;
      this.api.modulePath = data.path;
    },
    validateForm(){
      let validateResult = false;
      this.$refs['httpForm'].validate((valid) => {
        validateResult = valid;
      });
      return validateResult;
    }
  }
}
</script>

<style scoped>
/deep/ .not-unique-form-item > .el-form-item__label {
  width: 62px !important;
  margin-left: 8px;
}
</style>
