<template>
  <div>
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="40%"
      class="batch-edit-dialog"
      :destroy-on-close="true"
      @close="handleClose"
      append-to-body
      :close-on-click-modal="false"
      v-loading="loading">
      <span class="select-row">{{$t('test_track.batch_operate_select_row_count', [size])}}</span>

      <el-form :model="form" label-position="top" label-width="180px" size="small" ref="form" :rules="rules" style="margin-top: 24px" class="batchEditForm">
        <el-form-item :label="$t('test_track.case.select_attr')" prop="type">
          <el-select v-model="form.type" style="width: 100%" @change="changeType">
            <el-option v-for="(type, index) in typeArr" :key="index" :value="type.custom ? type.custom : type.id"
                       :label="type.name"/>
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.type === 'projectEnv'" :label="$t('test_track.case.batch_update_to')">
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

        <el-form-item v-else-if="form.type === 'tags'" :label="$t('test_track.case.batch_update_to')">
          <ms-input-tag :currentScenario="form" v-if="showInputTag" ref="tag" class="ms-case-input"></ms-input-tag>
          <el-checkbox v-model="form.appendTag">
            {{ $t('commons.append_tag') }}
            <el-tooltip class="item" effect="dark" :content="$t('commons.append_tag_tip')" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </el-checkbox>
        </el-form-item>

        <el-form-item v-else-if="form.type === 'reviewers'" :label="$t('test_track.case.batch_update_to')" prop="value">
          <el-select v-model="form.value" style="width: 100%" :filterable="filterable" :disabled="!form.type">
            <el-option v-for="(option, index) in options" :key="index" :value="option.id" :label="option.name">
              <div v-if="option.email">
                <span>{{ option.id }}({{ option.name }})</span>
              </div>
            </el-option>
          </el-select>

          <el-checkbox v-model="form.appendTag">
            {{ $t('commons.append_reviewer') }}
          </el-checkbox>
        </el-form-item>

        <el-form-item v-else-if="fieldType === 'custom'" prop="customFieldValue" :label="$t('test_track.case.batch_update_to')">
          <custom-filed-component v-if="customField" :data="customField" prop="defaultValue"/>
        </el-form-item>

        <el-form-item v-else prop="value" :label="$t('test_track.case.batch_update_to')">
          <el-select v-model="form.value" style="width: 100%" :filterable="filterable" :disabled="!form.type" @change="changeValue">
            <el-option v-for="(option, index) in options" :key="index" :value="option.id" :label="option.name">
              <div v-if="option.email">
                <span>{{ option.id }}({{ option.name }})</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.type === 'status' && this.showDescription"
                      :label="$t('commons.description')" :label-width="formLabelWidth" prop="description">
          <el-input v-model="form.description"
                    type="textarea"
                    :autosize="{ minRows: 2, maxRows: 4}"
                    :rows="2"
                    :placeholder="$t('commons.input_un_pass_reason')"/>
        </el-form-item>

        <el-form-item v-if="form.type === 'reviewStatus'"
                      :label="$t('原因')" :label-width="formLabelWidth" prop="description">
          <comment-edit-input :placeholder="'请输入原因'" :data="form" ref="commentEditInput"/>
        </el-form-item>

      </el-form>
      <template v-slot:footer>
        <el-button @click="dialogVisible = false" size="small">{{ $t('commons.cancel') }}</el-button>
        <el-button v-prevent-re-click :type="!form.type ? 'info' : 'primary'" @click="submit('form')"
                   @keydown.enter.native.prevent size="small" :disabled="!form.type" style="margin-left: 12px">{{ $t('commons.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import EnvPopover from "@/business/plan/env/EnvPopover";
import {ENV_TYPE} from "metersphere-frontend/src/utils/constants";
import CustomFiledComponent from "metersphere-frontend/src/components/template/CustomFiledComponent";
import MsInputTag from "metersphere-frontend/src/components/MsInputTag";
import {getOwnerProjects} from "@/business/utils/sdk-utils";
import {getApiScenarioEnvByProjectId} from "@/api/remote/api/api-automation";
import {getCustomField} from "@/api/custom-field";
import CommentEditInput from "@/business/review/view/components/commnet/CommentEditInput";

export default {
  name: "BatchEdit",
  components: {
    CommentEditInput,
    CustomFiledComponent,
    EnvPopover,
    MsDialogFooter,
    MsInputTag
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
      showConfigButtonWithOutPermission: false,
      form: {
        appendTag: true,
        customFieldValue: null,
        tags: null,
        value: null
      },
      formLabelWidth: "100px",
      size: 0,
      rules: {
        type: {required: true, message: this.$t('test_track.case.please_select_attr'), trigger: ['blur', 'change']},
        value: {
          required: true,
          message: this.$t('test_track.case.please_select_required_value'),
          trigger: ['blur', 'change']
        },
        tags: {
          required: true,
          message: this.$t('test_track.case.please_select_required_value'),
          trigger: ['blur', 'change']
        },
        customFieldValue: {
          required: true,
          message: this.$t('test_track.case.please_select_required_value'),
          trigger: ['blur', 'change']
        },
        description: {
          required: true,
          message: this.$t('test_track.case.please_select_required_value'),
          trigger: ['blur']
        }
      },
      options: [],
      filterable: false,
      projectList: [],
      projectIds: new Set(),
      selectRows: new Set(),
      allDataRows: new Set(),
      projectEnvMap: new Map(),
      map: new Map(),
      isScenario: '',
      showDescription: false,
      loading: false,
      environmentType: ENV_TYPE.JSON,
      envGroupId: "",
      customField: null,
      fieldType: "",
      showInputTag: true
    }
  },
  computed: {
    ENV_TYPE() {
      return ENV_TYPE;
    }
  },
  watch: {
    'customField.defaultValue'() {
      this.$set(this.form, 'customFieldValue', this.customField.defaultValue);
    }
  },
  methods: {
    submit(form) {
      if (!this.validateReviewStatus()) {
        return;
      }

      this.$refs[form].validate(async (valid) => {
        if (valid) {
          this.form.projectEnvMap = this.projectEnvMap;
          if (this.form.type === 'projectEnv') {
            if (!await this.$refs.envPopover.checkEnv()) {
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
    validateReviewStatus() {
      if (this.form.type === 'reviewStatus' && this.form.value === 'UnPass' && !this.form.description) {
        this.$refs.commentEditInput.inputLight();
        return false;
      }
      return true;
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
      this.form = {
        appendTag: true
      }
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
    setAllDataRows(rows) {
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
      this.loading = true;
      getCustomField(id)
        .then(res => {
          this.loading = false;
          if (res) {
            res.data.defaultValue = null;
            this.customField = res.data;
          } else {
            this.customField = {defaultValue: null};
          }
          this.customField.options = JSON.parse(this.customField.options);
          if (this.customField.type === 'checkbox' || this.customField.type === 'multipleMember') {
            this.customField.defaultValue = [];
          }
        });
    },
    changeType(val) {
      this.showDescription = false;
      if (val && val.startsWith("custom")) {
        this._handleCustomField(val);
      }
      if (val === 'tags') {
        // 跳过form rules的检查
        this.$set(this.form, "value", "tags");
      } else {
        this.$set(this.form, "value", "");
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

      if (val === 'projectEnv' && this.isScenario !== '') {
        this.projectIds.clear();
        this.map.clear();
        if (this.allDataRows != null && this.allDataRows.length > 0) {
          this.allDataRows.forEach(row => {
            this.getApiScenarioProjectId(row);
          });
        } else {
          this.selectRows.forEach(row => {
            this.getApiScenarioProjectId(row);
          })
        }
      }
    },
    changeValue(val) {
      if (val === 'UnPass') {
        this.showDescription = true;
      } else {
        this.showDescription = false;
      }

      if (this.form.type === 'reviewStatus') {
        if (val === 'UnPass') {
          this.rules.description.required = true;
        } else {
          this.rules.description.required = false;
        }
      }
    },
    getApiScenarioProjectId(row) {
      let id = this.isScenario === 'scenario' ? row.id : row.caseId;
      this.loading = true;
      getApiScenarioEnvByProjectId(id)
        .then(res => {
          this.loading = false;
          let data = res.data;
          data.projectIds.forEach(d => this.projectIds.add(d));
          this.map.set(row.id, data.projectIds);
        });
    },
    getWsProjects() {
      getOwnerProjects()
        .then(res => {
          this.projectList = res.data;
        });
    },
  }
}
</script>

<style scoped>
.select-row {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #646A73;
  flex: none;
  order: 1;
  align-self: center;
  flex-grow: 0;
}

:deep(.el-form-item__label) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
  flex: none;
  order: 0;
  flex-grow: 0;
  padding-bottom: 8px;
}

:deep(.el-button--small span) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  position: relative;
  top: -5px;
}

.el-button--small {
  min-width: 80px;
  height: 32px;
  border-radius: 4px;
}

.batchEditForm {
  margin-top: 24px;
}

.batchEditForm :deep(.el-form-item.is-required > .el-form-item__label:before) {
  display: none;
}

.batchEditForm :deep(.el-form-item.is-required > .el-form-item__label:after) {
  content: "*";
  color: #F56C6C;
  margin-right: 4px;
}

.ms-case-input :deep(.el-tag.el-tag--info) {
  background-color: rgba(31, 35, 41, 0.1);
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  align-items: center;
  color: #1F2329;
  flex: none;
  order: 1;
  flex-grow: 0;
  padding-right: 4px;
}

.ms-case-input :deep(.el-tag.el-tag--info .el-icon-close) {
  font-size: 21px;
  position: relative;
  right: 3px;
}

.ms-case-input :deep(.el-tag.el-tag--info .el-icon-close:hover) {
  color: #783887;;
  background-color: transparent;
}
</style>
