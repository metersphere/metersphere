<template>
  <el-dialog
    :close-on-click-modal="false"
    title="添加监控"
    :visible.sync="dialogVisible"
    width="70%"
    @closed="closeFunc"
    :destroy-on-close="true"
    v-loading="result.loading"
  >
    <el-form :model="form" label-position="right" label-width="140px" size="small" :rules="rule" ref="monitorForm">
      <el-form-item :label="$t('commons.name')" prop="name">
        <el-input v-model="form.name" autocomplete="off"/>
      </el-form-item>
      <el-form-item label="所属环境">
        <el-select v-model="form.environmentId" placeholder="选择所属环境" @change="change">
          <el-option
            v-for="item in environments"
            :key="item.id"
            :label="item.name"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
      <h4 style="margin-left: 80px;">监控配置</h4>
      <el-row>
        <el-col :span="12">
          <el-form-item label="IP" prop="ip">
            <el-input v-model="form.ip" autocomplete="off"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Port">
            <el-input-number v-model="form.port" :min="1" :max="65535"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" autocomplete="off"/>
      </el-form-item>
    </el-form>

    <template v-slot:footer>
      <ms-dialog-footer
        v-if="index !== '' && index !== undefined"
        @cancel="dialogVisible = false"
        @confirm="update"/>
      <ms-dialog-footer
        v-else
        @cancel="dialogVisible = false"
        @confirm="create"/>
    </template>

  </el-dialog>
</template>

<script>

import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {CONFIG_TYPE} from "@/common/js/constants";

export default {
  name: "EditMonitor",
  components: {MsDialogFooter},
  props: {
    testId: String,
    list: Array,
    environments: Array
  },
  data() {
    return {
      result: {},
      form: {},
      dialogVisible: false,
      rule: {},
      index: '',
      monitorList: [
        {
          indicator: '',
          expression: '',
        }
      ]
    }
  },
  methods: {
    open(data, index) {
      this.index = '';
      this.dialogVisible = true;
      if (data) {
        const copy = JSON.parse(JSON.stringify(data));
        this.form = copy;
      }
      if (index !== '' && index !== undefined) {
        this.index = index;
      }
    },
    closeFunc() {
      this.form = {};
      this.dialogVisible = false;
    },
    update() {
      this.$refs.monitorForm.validate(valid => {
        if (valid) {
          this.list.splice(this.index, 1, this.form);
          this.$emit("update:list", this.list);
        } else {
          return false;
        }
      })
      this.dialogVisible = false;
    },
    create() {
      this.$refs.monitorForm.validate(valid => {
        if (valid) {
          this.form.loadTestId = this.testId;
          this.form.authStatus = CONFIG_TYPE.NOT;
          this.form.monitorStatus = CONFIG_TYPE.NOT;
          this.list.push(this.form);
          this.$emit("update:list", this.list);
        } else {
          return false;
        }
      })
      this.dialogVisible = false;
    },
    change(data) {
      let env = this.environments.find(env => env.id === data);
      this.form.environmentName = env ? env.name : "";
    }
  }
}
</script>

<style scoped>
.box {
  padding-left: 5px;
}
</style>
