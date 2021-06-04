<template>
  <div v-loading="loading">
    <el-form :model="basicForm" label-position="right" label-width="80px" size="small" :rules="rule" ref="basicForm" style="margin-right: 20px">
      <!-- 基础信息 -->
      <el-row>
        <el-col :span="12">
          <el-form-item :label="$t('commons.name')" prop="name">
            <!--            <el-input class="ms-http-input" size="small" v-model="basicForm.name"/>-->
            <el-input v-model="basicForm.name" class="ms-http-input" size="small">
              <el-select v-model="basicForm.method" slot="prepend" style="width: 100px" size="small" @change="methodChange">
                <el-option v-for="item in methodTypes" :key="item.key" :label="item.value" :value="item.key"/>
              </el-select>
            </el-input>

          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('test_track.module.module')" prop="moduleId">
            <ms-select-tree size="small" :data="moduleOptions" :defaultKey="basicForm.moduleId" @getValue="setModule" :obj="moduleObj" clearable checkStrictly/>

          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item :label="$t('commons.status')" prop="status">
            <el-select class="ms-http-input" size="small" v-model="basicForm.status" style="width: 100%">
              <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('api_test.definition.request.responsible')" prop="userId">
            <el-select v-model="basicForm.userId"
                       :placeholder="$t('api_test.definition.request.responsible')" filterable size="small"
                       class="ms-http-input" style="width: 100%">
              <el-option
                v-for="item in maintainerOptions"
                :key="item.id"
                :label="item.id + ' (' + item.name + ')'"
                :value="item.id">
              </el-option>
            </el-select>

          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12">
          <el-form-item :label="$t('commons.tag')" prop="tag">
            <ms-input-tag :currentScenario="basicForm" ref="tag"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('commons.description')" prop="description">
            <el-input class="ms-http-textarea"
                      v-model="basicForm.description"
                      type="textarea"
                      :autosize="{ minRows: 2, maxRows: 10}"
                      :rows="2" size="small"/>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>
  import {API_STATUS} from "../../model/JsonData";
  import {WORKSPACE_ID} from '../../../../../../common/js/constants';
  import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
  import MsSelectTree from "../../../../common/select-tree/SelectTree";
  import {getCurrentProjectID} from "@/common/js/utils";

  export default {
    name: "MsTcpBasicApi",
    components: {MsInputTag, MsSelectTree},
    props: {
      currentProtocol: {
        type: String,
        default: "HTTP"
      },
      moduleOptions: Array,
      methodTypes: Array,
      basisData: {},
    },
    created() {
      this.getMaintainerOptions();
      this.basicForm = this.basisData;
      if (this.basicForm.protocol == null) {
        this.basicForm.protocol = "TCP";
      }
    },
    data() {
      return {
        basicForm: {},
        httpVisible: false,
        currentModule: {},
        maintainerOptions: [],
        loading: false,
        rule: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
            {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
          ],
          userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          moduleId: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
          status: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
        },
        value: API_STATUS[0].id,
        options: API_STATUS,
        moduleObj: {
          id: 'id',
          label: 'name',
        },

      }
    },
    methods: {
      getMaintainerOptions() {
        this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
          this.maintainerOptions = response.data;
        });
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        })
      },
      validate() {
        this.$refs['basicForm'].validate((valid) => {
          if (valid) {
            this.$emit('callback');
          }
        })
      },
      createModules() {
        this.$emit("createRootModelInTree");
      },
      methodChange() {
        this.$emit("changeApiProtocol", this.basicForm.method);
      },
      setModule(id,data) {
        this.basisData.modulePath = data.path;
        this.basisData.moduleId = id;
      },
    }
  }
</script>

<style scoped>
</style>
