<template>
  <div v-loading="isloading" class="ms-base-div">
    <el-form ref="apiForm" :model="basicForm" :rules="rules" class="case-form" label-position="right"
             label-width="80px"
             size="small"
             style="margin-left: 5px;margin-right: 5px">
      <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>

      <!-- 基础信息 -->
      <el-row>
        <el-form-item v-if="currentProtocol !== 'TCP'" :label="$t('commons.name')" prop="name">
          <el-input v-model="basicForm.name" class="ms-http-input" size="small"/>
        </el-form-item>

        <el-form-item v-else :label="$t('commons.name')" prop="name">

          <el-input v-model="basicForm.name" class="ms-http-input" size="small">
            <el-select slot="prepend" v-model="basicForm.method" size="mini" style="width: 80px">
              <el-option v-for="item in methodTypes" :key="item.key" :label="item.value" :value="item.key"/>
            </el-select>
          </el-input>

        </el-form-item>
      </el-row>

      <el-row>
        <el-form-item :label="$t('api_test.definition.request.responsible')" prop="userId">
          <el-select v-model="basicForm.userId"
                     :placeholder="$t('api_test.definition.request.responsible')" class="ms-http-select" filterable
                     size="small">
            <el-option
              v-for="item in maintainerOptions"
              :key="item.id"
              :label="item.name + ' (' + item.email + ')'"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-row>
      <el-row>
        <el-form-item :label="$t('test_track.module.module')" prop="moduleId">
          <ms-select-tree ref="msTree" :data="moduleOptions" :defaultKey="basicForm.moduleId" :obj="moduleObj"
                          checkStrictly clearable size="small" @getValue="setModule"/>
        </el-form-item>
      </el-row>
      <el-row>
        <el-form-item :label="$t('commons.status')" prop="status">
          <el-select v-model="basicForm.status" class="ms-http-select" size="small">
            <el-option v-for="item in options" :key="item.id" :label="$t(item.label)" :value="item.id"/>
          </el-select>
        </el-form-item>
      </el-row>
      <el-row>
        <el-form-item :label="$t('commons.tag')" prop="tag">
          <ms-input-tag ref="tag" v-model="basicForm.tags" :currentScenario="basicForm"/>
        </el-form-item>
      </el-row>
      <el-row>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="basicForm.description"
                    :autosize="{ minRows: 1, maxRows: 10}"
                    :rows="1"
                    class="ms-http-textarea"
                    size="small" type="textarea"/>
        </el-form-item>
      </el-row>
      <!-- 自定义字段 -->
      <el-form v-if="isFormAlive" ref="customFieldForm" :model="customFieldForm" :rules="customFieldRules"
               class="case-form" label-position="right" label-width="80px"
               size="small">
        <custom-filed-form-row :default-open="defaultOpen"
                               :disabled="readOnly"
                               :form="customFieldForm"
                               :issue-template="apiTemplate"/>
      </el-form>
    </el-form>
  </div>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import MsSelectTree from "@/business/components/common/select-tree/SelectTree";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import CustomFiledFormRow from "@/business/components/common/components/form/CustomFiledFormRow";
import {API_STATUS, REQ_METHOD} from "@/business/components/api/definition/model/JsonData";
import {hasLicense} from "@/common/js/utils";

export default {
  name: "ApiBaseInfo",
  components: {
    MsFormDivider,
    MsSelectTree,
    MsInputTag,
    CustomFiledFormRow,
  },
  data() {
    let validateModuleId = (rule, value, callback) => {
      if (this.basicForm.moduleId.length === 0 || !this.basicForm.moduleId) {
        callback(this.$t('test_track.case.input_module'));
      } else {
        callback();
      }
    };
    return {
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
        ],
        userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        moduleId: [{required: true, validator: validateModuleId, trigger: 'change'}],
        status: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
      },
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      options: API_STATUS,
      reqOptions: REQ_METHOD,
      methodTypes: [
        {
          'key': "TCP",
          'value': this.$t('api_test.request.tcp.general_format'),
        }
      ],
      basicForm: {},
    }
  },
  props: {
    form: Object,
    isFormAlive: Boolean,
    isloading: Boolean,
    readOnly: Boolean,
    customFieldForm: Object,
    customFieldRules: Object,
    apiTemplate: Object,
    moduleOptions: Array,
    defaultOpen: String,
    maintainerOptions: Array,
    currentProtocol: String
  },
  watch: {
    'basicForm.name': {
      handler(v, v1) {
        if (v && v1 && v !== v1) {
          this.apiMapStatus();
        }
      }
    },
    'basicForm.moduleId': {
      handler(v, v1) {
        if (v && v1 && v !== v1) {
          this.apiMapStatus();
        }
      }
    },
    'basicForm.status': {
      handler(v, v1) {
        if (v && v1 && v !== v1) {
          this.apiMapStatus();
        }
      }
    },
    'basicForm.follows': {
      handler(v, v1) {
        if (v && v1 && JSON.stringify(v) !== JSON.stringify(v1)) {
          this.apiMapStatus();
        }
      }
    },
    'basicForm.description': {
      handler(v, v1) {
        if (v && v1 !== undefined && v !== v1) {
          this.apiMapStatus();
        }
      }
    },
    'basicForm.tags': {
      handler(v, v1) {
        if (v && v1 && JSON.stringify(v) !== JSON.stringify(v1)) {
          this.apiMapStatus();
        }
      }
    },
    customFieldForm: {
      handler(v, v1) {
        if (v && v1 && this.$store.state.apiMap && this.basicForm.id) {
          this.apiMapStatus();
        }
      },
      deep: true
    }
  },
  methods: {
    apiMapStatus() {
      this.$store.state.apiStatus.set("fromChange", true);
      if (this.basicForm.id) {
        this.$store.state.apiMap.set(this.basicForm.id, this.$store.state.apiStatus);
      }
    },
    setModule(id, data) {
      this.basicForm.moduleId = id;
      this.basicForm.modulePath = data.path;
    },
    validateForm() {
      let isValidate = true;
      this.$refs['apiForm'].validate((valid) => {
        if (!valid) {
          isValidate = false;
        }
      });
      return isValidate;
    },
    validateCustomForm() {
      let isValidate = true;
      this.$refs['customFieldForm'].validate((valid) => {
        if (!valid) {
          isValidate = false;
        }
      });
      return isValidate;
    },
    getCustomFields() {
      let caseFromFields = this.$refs['apiForm'].fields;
      let customFields = this.$refs['customFieldForm'].fields;
      Array.prototype.push.apply(caseFromFields, customFields);
      return caseFromFields;
    },
  },
  created() {
    this.basicForm = this.form;
    if (hasLicense()) {
      if (this.methodTypes.length == 1) {
        let esbMethodType = {};
        esbMethodType.key = "ESB";
        esbMethodType.value = "ESB";
        this.methodTypes.push(esbMethodType);
      }
    }
  }
}
</script>

<style scoped>
.ms-base-div {
  height: calc(100vh - 100px) !important;
  overflow: auto !important;
}
</style>
