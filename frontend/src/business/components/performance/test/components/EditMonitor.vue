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

<!--      <h4 style="margin-left: 80px;">认证配置</h4>-->
<!--      <el-form-item label="IP" prop="name">-->
<!--        <el-input v-model="form.ip" autocomplete="off"/>-->
<!--      </el-form-item>-->
<!--      <el-form-item label="用户名" prop="description">-->
<!--        <el-input v-model="form.username" autocomplete="off"/>-->
<!--      </el-form-item>-->
<!--      <el-form-item label="密码" prop="description">-->
<!--        <el-input v-model="form.password" autocomplete="off"/>-->
<!--      </el-form-item>-->

      <h4 style="margin-left: 80px;">监控配置</h4>
      <el-form-item label="地址" prop="host">
        <el-input v-model="form.host" autocomplete="off"/>
      </el-form-item>
      <div v-for="(item,index) in monitorList " :key="index">
        <el-row>
          <el-col :span="8">
            <el-form-item label="指标名" prop="indicator">
              <el-input v-model="item.indicator" autocomplete="off"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="表达式" prop="expression">
              <el-input v-model="item.expression" autocomplete="off"/>
            </el-form-item>
          </el-col>
          <el-col :offset="1" :span="2">
                <span class="box">
                    <el-button @click="addMonitorConfig" type="success" size="mini" circle>
                        <font-awesome-icon :icon="['fas', 'plus']"/>
                    </el-button>
                </span>
            <span class="box">
                    <el-button @click="delMonitorConfig" type="danger" size="mini" circle>
                        <font-awesome-icon :icon="['fas', 'minus']"/>
                    </el-button>
                </span>
          </el-col>
        </el-row>
      </div>

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
      this.monitorList = [
        {
          indicator: '',
          expression: '',
        }
      ];
      this.dialogVisible = true;
      if (data) {
        const copy = JSON.parse(JSON.stringify(data));
        this.form = copy;
        if (copy.monitorConfig) {
          this.monitorList = JSON.parse(copy.monitorConfig);
        }
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
          this.form.monitorConfig = JSON.stringify(this.monitorList);
          // let authConfig = {
          //   "ip": this.form.ip,
          //   "username": this.form.username,
          //   "password": this.form.password,
          // };
          // this.form.authConfig = JSON.stringify(authConfig);
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
          this.form.monitorConfig = JSON.stringify(this.monitorList);
          // let authConfig = {
          //   "ip": this.form.ip,
          //   "username": this.form.username,
          //   "password": this.form.password,
          // };
          // this.form.authConfig = JSON.stringify(authConfig);
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
    convertConfig() {
      let config = [];
      if (this.form.monitorConfig) {
        config = JSON.parse(this.form.monitorConfig);
      }
      this.monitorList = config;
    },
    addMonitorConfig() {
      this.monitorList.push({
        indicator: '',
        expression: ''
      });
    },
    delMonitorConfig(index) {
      if (this.monitorList.length > 1) {
        this.monitorList.splice(index, 1);
      } else {
        this.$warning("不能删除当前节点");
      }
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
