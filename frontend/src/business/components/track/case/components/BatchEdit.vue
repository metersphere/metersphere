<template>
  <div>
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="25%"
      class="batch-edit-dialog"
      :destroy-on-close="true"
      @close="handleClose"
      v-loading="result.loading"
    >
      <el-form :model="form" label-position="right" label-width="150px" size="medium" ref="form" :rules="rules">
        <el-form-item :label="$t('test_track.case.batch_update', [size])" prop="type">
          <el-select v-model="form.type" style="width: 80%" @change="changeType">
            <el-option v-for="(type, index) in typeArr" :key="index" :value="type.custom ? type.custom : type.id" :label="type.name"/>
          </el-select>
        </el-form-item>
        <el-form-item  v-if="form.type === 'projectEnv'" :label="$t('test_track.case.updated_attr_value')">
          <env-popover :env-map="projectEnvMap"
                       :project-ids="projectIds"
                       @setProjectEnvMap="setProjectEnvMap"
                       :show-config-button-with-out-permission="showConfigButtonWithOutPermission"
                       :project-list="projectList"
                       :environment-type.sync="environmentType"
                       :group-id="envGroupId"
                       :is-scenario="false"
                       @setEnvGroup="setEnvGroup"
                       ref="envPopover"/>
        </el-form-item>
        <el-form-item v-else-if="fieldType === 'custom'" :label="$t('test_track.case.updated_attr_value')">
          <custom-filed-component :data="customField" prop="defaultValue"/>
        </el-form-item>
        <el-form-item v-else :label="$t('test_track.case.updated_attr_value')" prop="value">
          <el-select v-model="form.value" style="width: 80%" :filterable="filterable">
            <el-option v-for="(option, index) in options" :key="index" :value="option.id" :label="option.name">
              <div v-if="option.email">
                <span>{{option.id}}({{option.name}})</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogVisible = false"
          @confirm="submit('form')"/>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import MsDialogFooter from "../../../common/components/MsDialogFooter";
import {listenGoBack, removeGoBackListener} from "@/common/js/utils";
import EnvPopover from "@/business/components/api/automation/scenario/EnvPopover";
import {ENV_TYPE} from "@/common/js/constants";
import CustomFiledComponent from "@/business/components/project/template/CustomFiledComponent";

export default {
  name: "BatchEdit",
  components: {
    CustomFiledComponent,
    EnvPopover,
    MsDialogFooter
  },
  props: {
    typeArr: Array,
    valueArr: Object,
    dialogTitle: {
        type: String,
        default() {
          return this.$t('test_track.case.batch_operate')
        }
      },
    },
    data() {
      return {
        dialogVisible: false,
        showConfigButtonWithOutPermission:false,
        form: {},
        size: 0,
        rules: {
          type: {required: true, message: this.$t('test_track.case.please_select_attr'), trigger: ['blur','change']},
          value: {required: true, message: this.$t('test_track.case.please_select_attr_value'), trigger: ['blur','change']}
        },
        options: [],
        filterable: false,
        projectList: [],
        projectIds: new Set(),
        selectRows: new Set(),
        allDataRows:new Set(),
        projectEnvMap: new Map(),
        map: new Map(),
        isScenario: '',
        result: {},
        environmentType: ENV_TYPE.JSON,
        envGroupId: "",
        customField: {},
        fieldType: ""
      }
    },
    computed: {
      ENV_TYPE() {
        return ENV_TYPE;
      }
    },
    methods: {
      submit(form) {
        this.$refs[form].validate(async (valid) => {
          if (valid) {
            this.form.projectEnvMap = this.projectEnvMap;
            if (this.form.type === 'projectEnv') {
              if (! await this.$refs.envPopover.checkEnv()) {
                return false;
              }
              this.form.map = this.map;
            }
            // 处理自定义字段
            if (this.form.type.startsWith("custom")) {
              this.form.customField = this.customField;
            }
            this.form.environmentType = this.environmentType;
            this.form.envGroupId = this.envGroupId;
            this.$emit("batchEdit", this.form);
            this.dialogVisible = false;
          } else {
            return false;
          }
        });
      },
      setProjectEnvMap(projectEnvMap) {
        this.projectEnvMap = projectEnvMap;
      },
      setEnvGroup(id) {
        this.envGroupId = id;
      },
      open(size) {
        this.dialogVisible = true;
        this.projectEnvMap.clear();
        if (size) {
          this.size = size;
        } else {
          this.size = this.$parent.selectDataCounts;
        }
        listenGoBack(this.handleClose);
        this.getWsProjects();
      },
      setSelectRows(rows) {
        this.selectRows = rows;
        this.projectIds.clear();
        this.selectRows.forEach(row => {
          this.projectIds.add(row.projectId)
        })
      },
      setScenarioSelectRows(rows, sign) {
        this.selectRows = rows;
        this.isScenario = sign;
      },
      setAllDataRows(rows){
        this.allDataRows = rows;
      },
      handleClose() {
        this.form = {};
        this.options = [];
        this.fieldType = "";
        removeGoBackListener(this.handleClose);
      },
      _handleCustomField(val) {
        // custom template field id
        let id = val.slice(6);
        this.fieldType = "custom";
        this.$get("/custom/field/get/" + id, res => {
          this.customField = res ? res.data : {};
          this.customField.options = JSON.parse(this.customField.options);
          if (this.customField.type === 'checkbox' || this.customField.type === 'multipleMember') {
            this.customField.defaultValue = [];
          }
        })
      },
      changeType(val) {
        if (val && val.startsWith("custom")) {
          this._handleCustomField(val);
        }
        this.$set(this.form, "value", "");
        if (val === 'projectEnv' && this.isScenario !== '') {
          this.projectIds.clear();
          this.map.clear();
          if(this.allDataRows != null && this.allDataRows.length > 0){
            this.allDataRows.forEach(row => {
              let id = this.isScenario === 'scenario' ? row.id : row.caseId;
              this.result = this.$get('/api/automation/getApiScenarioProjectId/' + id, res => {
                let data = res.data;
                data.projectIds.forEach(d => this.projectIds.add(d));
                this.map.set(row.id, data.projectIds);
              })
            })
          }else{
            this.selectRows.forEach(row => {
              let id = this.isScenario === 'scenario' ? row.id : row.caseId;
              this.result = this.$get('/api/automation/getApiScenarioProjectId/' + id, res => {
                let data = res.data;
                data.projectIds.forEach(d => this.projectIds.add(d));
                this.map.set(row.id, data.projectIds);
              })
            })
          }
        }
        this.filterable = val === "maintainer" || val === "executor";
        this.options = this.valueArr[val];
        this.typeArr.forEach(item => {
          if (item.id === val) {
            if (item.optionMethod) {
              this.options = [];
              item.optionMethod(this.options);
            }
            return;
          }
        });
        this.typeArr.forEach(item => {
          if (item.id === val && item.uuid) {
            this.$set(this.form, "id", item.uuid);
          }
        });
      },
      getWsProjects() {
        this.$get("/project/getOwnerProjects", res => {
          this.projectList = res.data;
        })
      },
    }
  }
</script>

<style scoped>

</style>
