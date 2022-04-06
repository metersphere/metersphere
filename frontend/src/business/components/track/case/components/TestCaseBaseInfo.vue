<template>
  <div v-loading="isloading">
    <el-form :model="form" :rules="rules" ref="caseFrom" class="case-form">
      <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>
      <el-row>
        <el-form-item
          :placeholder="$t('test_track.case.input_name')"
          :label="$t('test_track.case.name')"
          prop="name">
          <el-input :disabled="readOnly" v-model="form.name" size="small" class="ms-case-input"></el-input>
        </el-form-item>
      </el-row>

      <el-row>
        <el-form-item :label="$t('test_track.case.module')" prop="module"
                      v-if="!publicEnable">
          <ms-select-tree :disabled="readOnly" :data="treeNodes" :defaultKey="form.module" :obj="moduleObj"
                          @getValue="setModule" clearable checkStrictly size="small"/>
        </el-form-item>
      </el-row>

      <el-row>
        <el-form-item :label="$t('test_track.case.project')" prop="projectId"
                      v-if="publicEnable">
          <el-select v-model="form.projectId" filterable clearable>
            <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
      </el-row>

      <el-row>
        <el-form-item :label="$t('commons.tag')" prop="tags">
          <ms-input-tag :read-only="readOnly" :currentScenario="form" v-if="showInputTag" ref="tag"
                        class="ms-case-input"></ms-input-tag>
        </el-form-item>
      </el-row>

      <!-- 自定义字段 -->
      <el-form v-if="isFormAlive" :model="customFieldForm" :rules="customFieldRules" ref="customFieldForm"
               class="case-form">
        <custom-filed-form-row :form="customFieldForm"
                                :issue-template="testCaseTemplate"/>
      </el-form>

      <el-row v-if="isCustomNum">
        <el-form-item label="ID" prop="customNum">
          <el-input :disabled="readOnly" v-model.trim="form.customNum" size="small"
                    class="ms-case-input"></el-input>
        </el-form-item>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import MsSelectTree from "@/business/components/common/select-tree/SelectTree";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import CustomFiledFormRow from "@/business/components/common/components/form/CustomFiledFormRow";

export default {
  name: "TestCaseBaseInfo",
  components:{
    MsFormDivider,
    MsSelectTree,
    MsInputTag,
    CustomFiledFormRow,
  },
  data() {
    return {
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 255, message: this.$t('test_track.length_less_than') + '255', trigger: 'blur'}
        ],
        module: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
        customNum: [
          {required: true, message: "ID必填", trigger: 'blur'},
          {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
        ],
        demandName: [{required: true, message: this.$t('test_track.case.input_demand_name'), trigger: 'change'}],
        maintainer: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        priority: [{required: true, message: this.$t('test_track.case.input_priority'), trigger: 'change'}],
        method: [{required: true, message: this.$t('test_track.case.input_method'), trigger: 'change'}],
      },
      moduleObj: {
        id: 'id',
        label: 'name',
      },
    }
  },
  props: {
    form: Object,
    isFormAlive: Boolean,
    isloading: Boolean,
    readOnly: Boolean,
    publicEnable: Boolean,
    showInputTag: Boolean,
    treeNodes: Array,
    projectList: Array,
    customFieldForm: Object,
    customFieldRules: Object,
    testCaseTemplate: Object,
  },
  computed: {
    isCustomNum() {
      return this.$store.state.currentProjectIsCustomNum;
    },
  },
  methods: {
    setModule(id, data) {
      this.form.module = id;
      this.form.nodePath = data.path;
    },
    validateForm(){
      let isValidate = true;
      this.$refs['customFieldForm'].validate((valid) => {
        if (!valid) {
          isValidate = false;
        }
      });
      return isValidate;
    },
    getCustomFields(){
      return this.$refs['customFieldForm'].fields;
    }
  }
}
</script>

<style scoped>

</style>
