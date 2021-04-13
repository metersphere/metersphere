<template>

  <field-template-edit
    :form="form"
    :visible.sync="showDialog"
    :url="url"
    @refresh="$emit('refresh')"
    scene="ISSUE"
    ref="fieldTemplateEdit">

    <template v-slot:base>
      <el-form-item :label="'缺陷平台'" prop="type">
        <el-select filterable v-model="form.platform" placeholder="用例类型">
          <el-option
            v-for="item in platformOption"
            :key="item.value"
            :label="item.text"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
    </template>

    <template v-slot:default>
      <el-form-item :label="'标题'" prop="title">
        <el-input v-model="form.title" autocomplete="off"></el-input>
      </el-form-item>

      <el-form-item :label="'缺陷内容'" prop="content">
        <el-input :autosize="{ minRows: 2, maxRows: 4}" type="textarea" v-model="form.content"></el-input>
      </el-form-item>
    </template>

  </field-template-edit>

</template>

<script>

import draggable from 'vuedraggable';
import TemplateComponentEditHeader
  from "@/business/components/track/plan/view/comonents/report/TemplateComponentEditHeader";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import {ISSUE_PLATFORM_OPTION} from "@/common/js/table-constants";
import CustomFieldFormList from "@/business/components/settings/workspace/template/CustomFieldFormList";
import CustomFieldRelateList from "@/business/components/settings/workspace/template/CustomFieldRelateList";
import FieldTemplateEdit from "@/business/components/settings/workspace/template/FieldTemplateEdit";

export default {
  name: "IssueTemplateEdit",
  components: {
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
        type: '',
        description: '',
        title: '',
        content: '',
        customFieldIds: [],
      },
      rules: {},
      result: {},
      url: '',

    }
  },
  computed: {
    platformOption() {
      return ISSUE_PLATFORM_OPTION;
    }
  },
  methods: {
    open(data) {
      if (data) {
        Object.assign(this.form, data);
        if (!(data.options instanceof Array)) {
          this.form.options = data.options ? JSON.parse(data.options) : [];
        }
        if (data.id) {
          this.url = 'field/template/issue/update';
        } else {
          //copy
          this.url = 'field/template/issue/add';
        }
      } else {
        this.form = {
          name: "",
          type: '',
          description: '',
          title: '',
          content: '',
          customFieldIds: [],
        };
        this.url = 'field/template/issue/add';
      }
      this.$refs.fieldTemplateEdit.open(data);
    }
  }
}
</script>

<style scoped>

</style>
