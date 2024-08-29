<template>
  <el-dialog
    :title="$t('qrcode.service_DING_TALK') "
    :visible.sync="detailVisible"
    title-align="start"
    width="680px"
    :loading="loading"
    :before-close="cancelEdit"
  >
    <el-form  :rules="rules" ref="dingTalkForm" class="ms-form" :model="dingTalkForm" layout="vertical" size="mini">
      <el-form-item
        prop="appKey"
        :label="$t('qrcode.service_appKey')"
      >
        <el-input v-model="dingTalkForm.appKey" :max-length="255" :placeholder="$t('commons.input_content')" />
      </el-form-item>
      <el-form-item
          prop="agentId"
        :label="$t('qrcode.service_agentId')"
      >
        <el-input v-model="dingTalkForm.agentId" :max-length="255" :placeholder="$t('commons.input_content')" />
      </el-form-item>
      <el-form-item
          prop="appSecret"
        :label="$t('qrcode.service_appSecret')"

      >
        <el-input type="password"
          v-model="dingTalkForm.appSecret"
          allow-clear
          show-password
          :max-length="255"
          :placeholder="$t('commons.input_content')"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="footer-button">
        <div class="ms-switch">
          <el-switch
            v-model="dingTalkForm.enable"
            size="small"
          />
          <span class="ms-switch-text">{{ $t('qrcode.service_enable') }}</span>
        </div>
        <div class="ms-button-group">
          <el-button size="small"  style="margin-left: 14px; background-color: #ededf1;" @click="cancelEdit">
            {{ $t('commons.cancel') }}
          </el-button>
          <el-button
            size="small"
            type="primary" plain
            style="margin-left: 14px"
            :disabled="dingTalkForm.appKey === '' && dingTalkForm.appSecret === '' && dingTalkForm.agentId === ''"
            @click="validateInfo('dingTalkForm')"
          >
            {{ $t('qrcode.service_testLink') }}
          </el-button>
          <el-button size="small" type="primary" style="margin-left: 14px" @click="saveInfo('dingTalkForm')">
            {{ $t('commons.confirm') }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script>

import {
  validateDingTalkConfig, getDingInfo, saveDingTalkConfig, getLarkInfo
} from "@/api/qrcode";


export default {
  name: "dingTalkModal",
  components: {

  },
  props:{
    visible: {
      type: Boolean,
      default() {
        return true;
      },
    },
  },
  data() {
    return {
      loading:false,
      detailVisible:false,
      dingTalkForm:{
        agentId: '',
        appKey: '',
        appSecret: '',
        enable: false,
        valid: false,
      },
      rules: {
        appKey: {required: true, message: this.$t('qrcode.service_corpId_required'), trigger: ['change', 'blur']},
        agentId: {required: true, message: this.$t('qrcode.service_agentId_required'), trigger: ['change', 'blur']},
        appSecret: {required: true, message: this.$t('qrcode.service_appSecret_required'), trigger: ['change', 'blur']},
      },
    }
  },
  created() {
    this.loadList();
  },
  methods: {
    async loadList() {
      this.loading = getDingInfo().then(res => {
        this.dingTalkForm = res.data;
      });
    },
    async validateInfo(dingTalkForm) {
      this.$refs[dingTalkForm].validate(valid => {
        if (valid) {
          this.loading = validateDingTalkConfig(this.dingTalkForm).then(res => {
            this.dingTalkForm.valid = true;
            this.$message.success(this.$t('qrcode.service_testLinkStatusTip'));
          });
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    },
    saveInfo(dingTalkForm) {
      this.$refs[dingTalkForm].validate((valid) => {
        if (valid) {
          this.loading = saveDingTalkConfig(this.dingTalkForm).then(res => {
            this.$message.success(this.$t('commons.save_success'));
            this.$emit('success');
          });
        } else {
          console.log('error submit!!');
          return false;
        }
        this.cancelEdit();
      });
    },
    cancelEdit() {
      this.detailVisible = false;
      this.$emit('update');
      this.$refs['dingTalkForm'].resetFields();
    },
  },
  watch: {
    visible(val) {
      this.detailVisible = val;
      this.loadList();
    },
    immediate: true
  },
}
</script>

<style scoped>
  .footer-button {
    display: flex;
    align-items: center;
    flex-direction: row;
    justify-content: space-between;
  }

  .ms-switch {
    display: flex;
    align-items: center;
    flex-direction: row;
  }
  .ms-button-group {
    display: flex;
    align-items: center;
    flex-direction: row;
  }
  .ms-switch-text{
    margin-left: 3px;
    font-weight: 400;
    color: #323233;
  }
  .ms-modal-upload {
    padding: 2px 0;
  }
  .ms-modal-medium {
    width: 680px;
  }
</style>
