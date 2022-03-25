<template>
  <div>
    <el-form :model="ruleForm" :rules="rules" ref="editPasswordForm" label-width="120px" class="demo-ruleForm" >
      <el-form-item :label="$t('member.old_password')" prop="password" style="margin-bottom: 29px">
        <el-input v-model="ruleForm.password" autocomplete="off" show-password/>
      </el-form-item>
      <el-form-item :label="$t('member.new_password')" prop="newpassword">
        <el-input v-model="ruleForm.newpassword" autocomplete="off" show-password/>
      </el-form-item>
      <el-form-item :label="$t('member.repeat_password')" prop="repeatPassword">
        <el-input v-model="ruleForm.repeatPassword" autocomplete="off" show-password/>
      </el-form-item>
      <el-form-item>
        <el-button @click="cancel">{{$t('commons.cancel')}}</el-button>
        <el-button type="primary" @click="updatePassword('editPasswordForm')" @keydown.enter.native.prevent>{{$t('commons.confirm')}}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script>

import {logout} from "@/network/user";

export default {
  name:'PasswordInfo',
  data(){
    return{
      result:{},
      updatePasswordPath: '/user/update/password',
      rules: {
        password: [
          {required: true, message: this.$t('user.input_password'), trigger: 'blur'},
        ],
        newpassword: [
          {required: true, message: this.$t('user.input_password'), trigger: 'blur'},
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          },
        ],
        repeatPassword: [
          {required: true, message: this.$t('user.input_password'), trigger: 'blur'},
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          },
        ]
      }
    }
  },
  props:{
    ruleForm:{}
  },
  methods:{
    cancel() {
      this.$emit("cancel");
    },
    confirm() {
      this.$emit("confirm");
    },
    updatePassword(editPasswordForm) {
      this.$refs[editPasswordForm].validate(valid => {
        if (valid) {
          if (this.ruleForm.newpassword !== this.ruleForm.repeatPassword) {
            this.$warning(this.$t('member.inconsistent_passwords'));
            return;
          }
          this.result = this.$post(this.updatePasswordPath, this.ruleForm, response => {
            if(!response.data){
              this.$error(this.$t('commons.personal_password_info'));
            }else {
              this.$success(this.$t('commons.modify_success'));
              logout();
            }
          });
        } else {
          return false;
        }
      });
    },
  }
}
</script>
<style scoped>

</style>
