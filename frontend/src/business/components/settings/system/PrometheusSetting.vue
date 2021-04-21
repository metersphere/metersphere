<template>
  <div v-loading="result.loading">
    <el-form :model="formInline" :rules="rules" ref="formInline" class="demo-form-inline"
             :disabled="show" v-loading="loading" size="small">
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_config.prometheus.host')" prop="host">
            <el-input v-model="formInline.host" :placeholder="$t('system_config.prometheus.host_tip')"/>
            <i>({{ $t('commons.examples') }}:http://ms-prometheus:9090)</i>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div>
      <el-button @click="edit" v-if="showEdit" size="small">{{ $t('commons.edit') }}</el-button>
      <el-button type="success" @click="save('formInline')" v-if="showSave" :disabled="disabledSave" size="small">
        {{ $t('commons.save') }}
      </el-button>
      <el-button @click="cancel" type="info" v-if="showCancel" size="small">{{ $t('commons.cancel') }}</el-button>
    </div>
  </div>
</template>

<script>

export default {
  name: "PrometheusSetting",
  data() {
    return {
      formInline: {host: ''},
      input: '',
      visible: true,
      result: {},
      showEdit: true,
      showSave: false,
      showCancel: false,
      show: true,
      disabledSave: false,
      loading: false,
      rules: {
        host: [
          {
            required: true,
            message: this.$t('system_config.prometheus.host_is_null'),
            trigger: ['change', 'blur']
          },
        ],
      }
    }
  },

  created() {
    this.query()
  },
  methods: {
    query() {
      this.result = this.$get("/system/prometheus/host", response => {
        this.formInline.host = response.data;
        this.$nextTick(() => {
          this.$refs.formInline.clearValidate();
        })
      })
    },
    edit() {
      this.showEdit = false;
      this.showSave = true;
      this.showCancel = true;
      this.show = false;
    },
    save(formInline) {
      this.showEdit = true;
      this.showCancel = false;
      this.showSave = false;
      this.show = true;
      let param = [
        {paramKey: "prometheus.host", paramValue: this.formInline.host, type: "text", sort: 1},
      ];

      this.$refs[formInline].validate(valid => {
        if (valid) {
          this.result = this.$post("/system/save/base", param, response => {
            if (response.success) {
              this.$success(this.$t('commons.save_success'));
            } else {
              this.$message.error(this.$t('commons.save_failed'));
            }
          });
        } else {
          return false;
        }
      })
    },
    cancel() {
      this.showEdit = true;
      this.showCancel = false;
      this.showSave = false;
      this.show = true;
      this.query();
    }
  }
}
</script>

<style scoped>

.el-form {
  min-height: 300px;
}

</style>
