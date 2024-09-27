<template>
  <el-dialog
    :title="$t('qrcode.service_WE_COM')"
    :visible.sync="detailVisible"
    title-align="start"
    width="680px"
    :loading="loading"
    :close-on-click-modal="false"
    :before-close="cancelEdit"
  >
    <el-form :rules="rules" class="ms-form" ref="weComForm" :model="weComForm" layout="vertical" size="mini">
      <el-form-item
        prop="corpId"
        :label="$t('qrcode.service_corpId')"
      >
        <el-input v-model="weComForm.corpId" :max-length="255" :placeholder="$t('commons.input_content')" />
      </el-form-item>
      <el-form-item
          prop="agentId"
        :label="$t('qrcode.service_agentId')"
      >
        <el-input v-model="weComForm.agentId" :max-length="255" :placeholder="$t('commons.input_content')" />
      </el-form-item>
      <el-form-item
          prop="appSecret"
        :label="$t('qrcode.service_appSecret')"
      >
        <el-input type="password"
          v-model="weComForm.appSecret"
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
            v-model="weComForm.enable"
            size="small"
          />
          <span class="ms-switch-text">{{ $t('qrcode.service_enable') }}</span>
        </div>
        <div>
          <el-button style="margin-left: 14px; background-color: #ededf1;" size="small" @click="cancelEdit">
            {{ $t('commons.cancel') }}
          </el-button>
          <el-button
            style="margin-left: 14px"
            size="small"
            type="primary" plain
            :disabled="
              weComForm.corpId === '' && weComForm.appSecret === '' && weComForm.agentId === '' && weComForm.callBack === ''
            "
            @click="validateInfo"
          >
            {{ $t('qrcode.service_testLink') }}
          </el-button>
          <el-button style="margin-left: 14px" type="primary" size="small" @click="saveInfo">
            {{ $t('commons.confirm') }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {
  validateWeComConfig, getWeComInfo, saveWeComConfig
} from "@/api/qrcode";
import {getLdapInfo} from "@/api/system";


export default {
  name: "weComModal",
  components: {

  },
  props:{
    visible: {
      type: Boolean,
      default() {
        return false;
      },
    },
  },
  data() {
    return {
      loading:false,
      detailVisible:false,
      weComForm:{
        corpId: '',
        agentId: '',
        appSecret: '',
        callBack: '',
        enable: false,
        valid: false,
      },
      rules: {
        corpId: {required: true, message: this.$t('qrcode.service_corpId_required'), trigger: ['change', 'blur']},
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
      this.loading = getWeComInfo().then(res => {
        this.weComForm = res.data;
      });
    },
    async validateInfo() {
      this.$refs['weComForm'].validate(valid => {
        if (valid) {
          this.loading = true;
          try {
            validateWeComConfig(this.weComForm).then(res => {
              this.weComForm.valid = true;
              this.$message.success(this.$t('qrcode.service_testLinkStatusTip'));
            }).catch(e => {
              this.$message.error(this.$t('qrcode.service_testLinkStatusErrorTip'));
              this.weComForm.valid = false;
            });
          } catch (error) {
            this.weComForm.valid = false;
            // eslint-disable-next-line no-console
            console.log(error);
          } finally {
            this.loading = false;
          }
        }else {
          this.loading = false;
          console.log('error submit!!');
          return false;
        }
      });

    },
    async saveInfo() {
      this.$refs['weComForm'].validate(valid => {
        if (valid) {
          this.loading = saveWeComConfig(this.weComForm).then(res => {
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
      this.$refs['weComForm'].resetFields();

    }
  },
  watch: {
    visible(val) {
      console.log(val)
      this.loadList();
      this.detailVisible = val;
    },
    immediate: true
  },
}
</script>

<style scoped>
  .footer-button {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-direction: row;
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
  .ms-form {


  }
</style>
