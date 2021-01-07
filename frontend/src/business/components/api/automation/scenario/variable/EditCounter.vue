<template>
  <el-dialog append-to-body :close-on-click-modal="false" title="计数器编辑"
             :visible.sync="visible" class="environment-dialog" width="610px"
             @close="close">
    <el-form :model="form" label-position="right" label-width="80px" size="small" ref="form" :rules="rule">
      <el-form-item :label="$t('api_test.variable_name')" prop="name">
        <el-input v-model="form.name" :placeholder="$t('api_test.variable_name')"></el-input>
      </el-form-item>

      <el-form-item :label="$t('commons.description')" prop="description">
        <el-input class="ms-http-textarea"
                  v-model="form.description"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 10}"
                  :rows="2" size="small"/>
      </el-form-item>

      <el-form-item label="开始" prop="set">
        <el-input-number size="small" v-model="form.startNumber" placeholder="0" :max="1000*10000000" :min="0"/>
        <span style="margin: 0px 10px 10px ">结束</span>
        <el-input-number size="small" v-model="form.endNumber" placeholder="10" :max="1000*10000000" :min="0"/>
        <span style="margin: 0px 10px 10px ">增量</span>
        <el-input-number size="small" v-model="form.increment" placeholder="1" :max="1000*10000000" :min="0"/>
      </el-form-item>

      <el-form-item label="开始" prop="value">
        <el-input v-model="form.value" placeholder="000产生至少3位数字。user_000输出形式为user_nnn"></el-input>
      </el-form-item>
    </el-form>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="close"
        @confirm="saveParameters"/>
    </template>

  </el-dialog>
</template>

<script>
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";

  export default {
    name: "MsEditCounter",
    components: {
      MsDialogFooter
    },
    data() {
      return {
        visible: false,
        form: {type: "COUNTER"},
        editFlag: false,
        rule: {
          name: [
            {required: true, message: this.$t('api_test.variable_name'), trigger: 'blur'},
          ],
        },
      }
    },
    methods: {
      open: function (v) {
        this.visible = true;
        if (v) {
          this.form = v;
          this.editFlag = true;
        } else {
          this.form = {};
          this.editFlag = false;
        }
        this.form.type = "COUNTER";
      },
      close() {
        this.visible = false;
        this.form = {};
      },
      saveParameters() {
        this.$refs['form'].validate((valid) => {
          if (valid) {
            this.visible = false;
            if (!this.editFlag) {
              this.$emit('addParameters', this.form);
            }
          }
        });
      }
    }
  }
</script>

<style scoped>

</style>
