<template>
  <el-form :model="accountConfig" ref="form" label-width="100px" size="small" :rules="rules">
    <el-form-item :label="config.i18n ? $t(config.label) : config.label">
      <ms-instructions-icon size="10" :content="config.i18n ? $t(config.instructionsInfo) : config.instructionsInfo"/>
    </el-form-item>
    <span  v-for="item in config.formItems"
           :key="item.name">
       <el-form-item
         v-if="!item.displayConditions
            || accountConfig[item.displayConditions.field] === item.displayConditions.value"
         :label="item.i18n ? $t(item.label) : item.label"
         :prop="item.name">
       <custom-filed-component :form="accountConfig"
                              :data="item"
                              prop="defaultValue"/>
    </el-form-item>

    </span>

    <el-form-item>
      <el-button type="primary" style="float: right" @click="handleAuth" size="mini">
        {{ $t('commons.validate') }}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import MsInstructionsIcon from "../MsInstructionsIcon";
import {getPlatformFormRules} from "../../utils/platform";
import CustomFiledComponent from "metersphere-frontend/src/components/template/CustomFiledComponent";
import {validateAccountConfig} from "../../api/platform-plugin";

export default {
  name: "PlatformAccountConfig",
  components: {MsInstructionsIcon, CustomFiledComponent},
  props: {
    config: {
      type: Object,
      default() {
        return {}
      },
    },
    accountConfig: {
      type: Object,
      default() {
        return {}
      },
    }
  },
  data() {
    return {
      rules: {},
    }
  },
  watch: {
    accountConfig() {
      this.init();
    },
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      this.config.formItems.forEach(item => {
        if (!item.options) {
          item.options = [];
        }
        // 设置默认值
        if (this.accountConfig[item.name]) {
          if (this.accountConfig[item.name]) {
            this.$set(item, 'defaultValue', this.accountConfig[item.name]);
          }
        }
      });
      this.rules = getPlatformFormRules(this.config);
      for (const key in this.rules) {
        this.rules[key].required = false;
      }
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
    handleAuth() {
      this.validate()
        .then(() => {
          validateAccountConfig(this.config.id, this.accountConfig)
            .then(() => {
              this.$success(this.$t('organization.integration.verified'));
            });
        });
    },
  }
}
</script>

<style scoped>
.instructions-icon {
  margin-left: -5px;
}
</style>
