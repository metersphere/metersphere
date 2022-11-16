<template>
  <div>
    <el-form :model="form" ref="form" label-width="100px" size="small" :rules="rules">
      <el-form-item
        :label-width="labelWidth"
        v-for="item in config.formItems"
        :key="item.name"
        :label="item.i18n ? $t(item.label) : item.label"
        :prop="item.name">
        <custom-filed-component :form="form"
                                :data="item"
                                class="custom-filed"
                                prop="defaultValue"
                                @change="handleChange"/>
        <el-button v-if="item.withProjectCheck"
                   :disabled="!form[item.name]"
                   @click="check"
                   type="primary"
                   class="checkButton">
          {{ $t('test_track.issue.check_id_exist') }}
        </el-button>
        <ms-instructions-icon v-if="item.instructionsIcon" effect="light">
          <template>
            <img class="jira-image"
                 :src="'/platform/plugin/resource/' + config.id + '?fileName=' + item.instructionsIcon"/>
          </template>
        </ms-instructions-icon>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import {getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {
  getPlatformProjectInfo,
  getPlatformProjectOption,
  validateProjectConfig,
} from "@/api/platform-plugin";
import {getPlatformFormRules} from "@/business/workspace/integration/platform";
import CustomFiledComponent from "metersphere-frontend/src/components/template/CustomFiledComponent";

export default {
  name: "ProjectPlatformConfig",
  components: {MsInstructionsIcon, CustomFiledComponent},
  props: {
    labelWidth: String,
    result: {
      type: Object,
      default() {
        return {}
      }
    },
    platformKey: String,
    projectConfig: {
      type: Object,
      default() {
        return {}
      },
    }
  },
  data() {
    return {
      issueTypes: [],
      form: {},
      rules: {},
      config: {}
    }
  },
  watch: {
    platformKey() {
      this.getPlatformProjectInfo();
    },
  },
  mounted() {
    this.getPlatformProjectInfo();
  },
  methods: {
    getPlatformProjectInfo() {
      getPlatformProjectInfo(this.platformKey)
        .then(r => {
          if (r.data) {
            Object.assign(this.form, this.projectConfig);
            r.data.formItems.forEach(item => {
              if (!item.options) {
                item.options = [];
              }
              // 设置默认值
              if (this.form[item.name]) {
                this.$set(item, 'defaultValue', this.form[item.name]);
              }
              // 获取级联选项值
              if (item.cascade && this.form[item.name]) {
                this.getCascadeOptions(item, () => {
                  // 没有选项值会被组件自动清空，获取下拉框选项之后，重新设置默认值
                  if (this.form[item.name]) {
                    this.$set(item, 'defaultValue', this.form[item.name]);
                  }
                });
              }
            });
            this.config = r.data;
            this.rules = getPlatformFormRules(this.config);
          }
        });
    },
    check() {
      validateProjectConfig(this.config.id, this.form)
        .then(() => {
          this.$success(this.$t("system.check_third_project_success"));
        });
    },
    validate() {
      return new Promise((resolve, reject) => {
        this.$refs['form'].validate((valid) => {
          if (!valid) {
            reject();
          }
          resolve();
        });
      });
    },
    handleChange(name) {
      this.config.formItems.forEach(item => {
        if (item.cascade === name) {
          this.$set(item, 'options', []);
          this.getCascadeOptions(item);
        }
      });
    },
    getCascadeOptions(item, callback) {
      getPlatformProjectOption(this.config.id, {
        platform: this.platformKey,
        optionMethod: item.optionMethod,
        workspaceId: getCurrentWorkspaceId(),
        projectConfig: JSON.stringify(this.form)
      }).then((r) => {
        this.$set(item, 'options', r.data);
        if (callback) {
          callback();
        }
      });
    }
  }
}
</script>

<style scoped>

.custom-filed :deep(.el-select) {
  width: 260px !important;
}

.custom-filed :deep(.el-input, .el-textarea) {
  width: 80% !important;
}

.checkButton {
  margin-left: 5px;
}
</style>
