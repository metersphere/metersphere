<template>
  <el-dialog
    :close-on-click-modal="false"
    :title="$t('qrcode.service_LARK')"
    :visible.sync="detailVisible"
    title-align="start"
    width="680px"
    :loading="loading"
    :before-close="cancelEdit"
  >
    <el-form :rules="rules" class="ms-form" ref="larkForm"  :model="larkForm" layout="vertical" size="mini">
      <el-form-item
        prop="agentId"
        :label="$t('qrcode.service_agentId')"
      >
        <el-input v-model="larkForm.agentId" :max-length="255" :placeholder="$t('commons.input_content')" />
      </el-form-item>
      <el-form-item
          prop="appSecret"
        :label="$t('qrcode.service_appSecret')"
      >
        <el-input type="password"
          v-model="larkForm.appSecret"
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
            v-model="larkForm.enable"
            size="small"
          />
          <span class="ms-switch-text">{{ $t('qrcode.service_enable') }}</span>
        </div>
        <div class="ms-button-group">
          <el-button size="small" style="margin-left: 14px; background-color: #ededf1;" @click="cancelEdit">
            {{ $t('commons.cancel') }}
          </el-button>
          <el-button
            size="small"
            type="primary" plain
            style="margin-left: 14px"
            :disabled="larkForm.appSecret === '' && larkForm.agentId === ''"
            @click="validateInfo('larkForm')"
          >
            {{ $t('qrcode.service_testLink') }}
          </el-button>
          <el-button
            :disabled="larkForm.appSecret === '' && larkForm.agentId === ''"
            type="primary"
            size="small"
            style="margin-left: 14px"
            @click="saveInfo('larkForm')"
          >
            {{ $t('commons.confirm') }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {
  getLarkInfo, validateLarkConfig, saveLarkConfig, getLarkSuiteInfo
} from "@/api/qrcode";


export default {
  name: "larkModal",
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
      larkForm:{
        agentId: '',
        appSecret: '',
        callBack: '',
        enable: false,
        valid: false,
      },
      rules: {
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
      this.loading = getLarkInfo().then(res => {
        this.larkForm = res.data;
      });
    },
    validateInfo(larkForm) {
      this.$refs[larkForm].validate(valid => {
        if (valid) {
          this.loading = true;
          try {
            validateLarkConfig(this.larkForm).then(res => {
              this.$message.success(this.$t('qrcode.service_testLinkStatusTip'));
              this.larkForm.valid = true;
            }).catch(e => {
              this.$message.error(this.$t('qrcode.service_testLinkStatusErrorTip'));
              this.larkForm.valid = false;
            });
          } catch (error) {
            this.larkForm.valid = false;
            // eslint-disable-next-line no-console
            console.log(error);
          } finally {
            this.loading = false;
          }
        } else {
          this.loading = false;
          console.log('error submit!!');
          return false;
        }
      });

    },
    async saveInfo(larkForm) {
      this.$refs[larkForm].validate(valid => {
        if (valid) {
          this.loading = saveLarkConfig(this.larkForm).then(res => {
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
      this.$refs['larkForm'].resetFields();
    }
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
