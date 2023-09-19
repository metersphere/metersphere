<template>
  <div>
    <div style="width: 500px">
      <div style="margin-top: 20px;margin-bottom: 10px">{{ $t('organization.integration.basic_auth_info') }}</div>
      <el-form :model="form" ref="form" label-width="100px" size="small" :disabled="show" :rules="rules">
        <span  v-for="item in config.formItems"
               :key="item.name">
         <el-form-item
           v-if="!item.displayConditions
            || form[item.displayConditions.field] === item.displayConditions.value"
           :label="item.i18n ? $t(item.label) : item.label"
           :prop="item.name">
            <custom-filed-component :form="form"
                                    :data="item"
                                    prop="defaultValue"/>
            <ms-instructions-icon v-if="item.instructionsIcon || item.instructionsTip" effect="light">
              <template>
                <img v-if="item.instructionsIcon"
                     :src="getPlatformImageUrl(config, item)"/>
                <span v-if="item.instructionsTip">
                {{ item.instructionsTip }}
              </span>
              </template>
            </ms-instructions-icon>
         </el-form-item>
        </span>
      </el-form>
    </div>

    <bug-manage-btn @save="save"
                    @init="init"
                    :edit-permission="['WORKSPACE_SERVICE:READ+EDIT']"
                    @testConnection="testConnection"
                    @cancelIntegration="cancelIntegration"
                    @reloadPassInput="reloadPassInput"
                    :form="form"
                    :show.sync="show"
                    ref="bugBtn"/>

    <div class="defect-tip">
      <div>{{ $t('organization.integration.use_tip') }}</div>
      <div v-html="config.tips"></div>
      <div>
        {{ $t('organization.integration.use_tip_two') }}
        <router-link to="/setting/project/all"
                     style="margin-left: 5px;color: #551A8B; text-decoration: underline; cursor: pointer">
          {{ $t('organization.integration.link_the_project_now') }}
        </router-link>
      </div>
      <div>
        {{ $t('organization.integration.use_tip_three') }}
        <span style="margin-left: 5px;color: #551A8B; text-decoration: underline; cursor: pointer"
              @click="resVisible = true">
          {{ $t('organization.integration.link_the_info_now') }}
        </span>
        <el-dialog :close-on-click-modal="false" width="80%"
                   :visible.sync="resVisible" destroy-on-close @close="closeDialog">
          <ms-person-router @closeDialog="closeDialog"/>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script>
import BugManageBtn from "./BugManageBtn";
import {getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import MsPersonRouter from "metersphere-frontend/src/components/personal/PersonRouter";
import CustomFiledComponent from "metersphere-frontend/src/components/template/CustomFiledComponent";
import {delServiceIntegration, getServiceIntegration, saveServiceIntegration} from "../../../api/workspace";
import {generatePlatformResourceUrl, validateServiceIntegration} from "@/api/platform-plugin";
import {getPlatformFormRules} from "metersphere-frontend/src/utils/platform";

export default {
  name: "PlatformConfig",
  components: {MsInstructionsIcon, BugManageBtn, MsPersonRouter, CustomFiledComponent},
  created() {
    this.init();
  },
  props: {
    config: {
      type: Object,
      default() {
        return {}
      },
    }
  },
  data() {
    return {
      show: true,
      showInput: true,
      resVisible: false,
      form: {},
      rules: {},
    };
  },
  methods: {
    init() {
      this.rules = getPlatformFormRules(this.config);

      const workspaceId = getCurrentWorkspaceId();
      let param = {};
      param.platform = this.config.key;
      param.workspaceId = workspaceId;
      this.$parent.loading = getServiceIntegration(param).then(res => {
        let data = res.data;
        if (data.configuration) {
          let config = JSON.parse(data.configuration);
          let form = {};
          Object.assign(form, config);
          this.form = form;
          // 设置默认值
          this.config.formItems.forEach(item => {
            if (this.form[item.name]) {
              this.$set(item, 'defaultValue', this.form[item.name]);
            }
          });
        } else {
          this.clear();
        }
      });
    },
    save() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          let config = {};
          Object.assign(config, this.form);
          const workspaceId = getCurrentWorkspaceId();
          let param = {};
          param.workspaceId = workspaceId;
          param.platform = this.config.key;
          param.configuration = JSON.stringify(config);
          this.$parent.loading = saveServiceIntegration(param).then(() => {
            this.show = true;
            this.$refs.bugBtn.showEdit = true;
            this.$refs.bugBtn.showSave = false;
            this.$refs.bugBtn.showCancel = false;
            this.reloadPassInput();
            this.init();
            this.$success(this.$t('commons.save_success'));
          });
        } else {
          return false;
        }
      });
    },
    clear() {
      this.form = {};
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate();
        }
      });
    },
    testConnection() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          this.$parent.loading = validateServiceIntegration(this.config.id, this.form).then(() => {
            this.$success(this.$t('organization.integration.verified'));
          });
        }
      });
    },
    cancelIntegration() {
      this.$alert(this.$t('organization.integration.cancel_confirm') + this.config.key + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            const workspaceId = getCurrentWorkspaceId();
            let param = {};
            param.workspaceId = workspaceId;
            param.platform = this.config.key;
            this.$parent.loading = delServiceIntegration(param).then(() => {
              this.$success(this.$t('organization.integration.successful_operation'));
              this.init('');
            });
          }
        }
      });
    },
    reloadPassInput() {
      this.showInput = false;
      this.$nextTick(function () {
        this.showInput = true;
      });
    },
    closeDialog() {
      this.resVisible = false;
    },
    getPlatformImageUrl(config, item) {
      return generatePlatformResourceUrl(config.id, item.instructionsIcon);
    }
  }
};
</script>

<style scoped>
.defect-tip {
  background: #EDEDED;
  border: solid #E1E1E1 1px;
  margin: 10px 0;
  padding: 10px;
  border-radius: 3px;
  line-height: 25px;
}

.el-input {
  width: 80%;
}
</style>
