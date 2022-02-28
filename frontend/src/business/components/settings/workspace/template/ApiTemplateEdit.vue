<template>

  <field-template-edit
    :label-width="labelWidth"
    :form="form"
    :rules="rules"
    :visible.sync="showDialog"
    :url="url"
    @refresh="$emit('refresh')"
    scene="API"
    ref="fieldTemplateEdit">

    <template v-slot:base>
      <el-form-item :label="$t('api_test.definition.api_type')" prop="type" :label-width="labelWidth">
        <el-select :disabled="isSystem" filterable v-model="form.type" :placeholder="$t('api_test.definition.api_type')">
          <el-option
            v-for="item in caseTypeOption"
            :key="item.value"
            :label="item.text"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
    </template>

    <template v-slot:default>
<!--      <el-form-item :label="$t('commons.name')" prop="apiName" :label-width="labelWidth">-->
<!--        <el-input v-model="form.apiName" autocomplete="off"></el-input>-->
<!--      </el-form-item>-->


      <el-row>
        <el-col :span="8">
          <el-form-item :label="$t('commons.name')" prop="apiName" :label-width="labelWidth">
            <el-input v-model="form.apiName" autocomplete="off"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="16">
          <el-form-item :label="$t('api_report.request')" prop="apiPath">
            <el-input :placeholder="$t('api_test.definition.request.path_info')" v-model="form.apiPath"
                      class="ms-http-input" size="small" style="margin-top: 5px" @change="urlChange">
              <el-select v-model="form.apiMethod" slot="prepend" style="width: 100px" size="small">
                <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
              </el-select>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

    </template>

  </field-template-edit>

</template>

<script>

import draggable from 'vuedraggable';
import TemplateComponentEditHeader
  from "@/business/components/track/plan/view/comonents/report/TemplateComponentEditHeader";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import {API_TYPE_OPTION} from "@/common/js/table-constants";
import CustomFieldFormList from "@/business/components/settings/workspace/template/CustomFieldFormList";
import CustomFieldRelateList from "@/business/components/settings/workspace/template/CustomFieldRelateList";
import FieldTemplateEdit from "@/business/components/settings/workspace/template/FieldTemplateEdit";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import TestCaseStepItem from "@/business/components/track/case/components/TestCaseStepItem";
import StepChangeItem from "@/business/components/track/case/components/StepChangeItem";
import {listenGoBack} from "@/common/js/utils";
import {REQ_METHOD} from "@/business/components/api/definition/model/JsonData";

export default {
  name: "ApiTemplateEdit",
  components: {
    StepChangeItem,
    TestCaseStepItem,
    FormRichTextItem,
    FieldTemplateEdit,
    CustomFieldRelateList,
    CustomFieldFormList,
    MsFormDivider,
    TemplateComponentEditHeader,
    draggable
  },
  data() {
    return {
      showDialog: false,
      form: {
        name: "",
        type: 'HTTP',
        description: '',
        apiName: '',
        customFieldIds: [],
        apiUserId:'',
        apiPath:'',
        apiMethod:'GET',
        apiStatusId:'',
      },
      labelWidth: '120px',
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 64, message: this.$t('test_track.length_less_than') + '64', trigger: 'blur'}
        ],
        type: [{required: true,  trigger: 'change'}],
      },
      result: {},
      url: '',
      reqOptions: REQ_METHOD,

    };
  },
  computed: {
    caseTypeOption() {
      return new API_TYPE_OPTION();
    },
    isSystem() {
      return this.form.system;
    }
  },
  methods: {
    open(data, isCopy) {
      if (data) {
        Object.assign(this.form, data);
        this.form.steps = data.steps ? JSON.parse(data.steps) : [];
        if (!(data.options instanceof Array)) {
          this.form.options = data.options ? JSON.parse(data.options) : [];
        }
        if (isCopy) {
          this.url = 'field/template/api/add';
        } else {
          this.url = 'field/template/api/update';
        }
        listenGoBack(() => {
          this.showDialog = false;
        });
      } else {
        this.form = {
          id: "",
          name: "",
          type: 'HTTP',
          description: '',
          apiName: '',
          customFieldIds: [],
          apiUserId:'',
          apiPath:'',
          apiMethod: 'GET',
          apiStatusId:''
        };
        this.url = 'field/template/api/add';
      }
      this.$refs.fieldTemplateEdit.open(data);
    },
    urlChange() {
      if (!this.form.apiPath || this.form.apiPath.indexOf('?') === -1) return;
      let url = this.getURL(this.addProtocol(this.form.apiPath));
      if (url) {
        this.form.apiPath = decodeURIComponent(this.form.apiPath.substr(0, this.form.apiPath.indexOf("?")));
      }
    },
  }
};
</script>

<style scoped>

</style>
