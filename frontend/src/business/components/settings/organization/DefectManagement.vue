<template>
  <el-card class="header-title">
    <div>
      <div>{{$t('organization.select_defect_platform')}}</div>
      <el-radio-group v-model="platform" style="margin-top: 10px">
        <el-radio v-for="(item, index) in platforms" :key="index" :label="item.value" size="small">
          {{item.name}}
        </el-radio>
      </el-radio-group>
    </div>

    <div style="width: 500px">
      <div style="margin-top: 20px;margin-bottom: 10px">{{$t('organization.basic_auth_info')}}</div>
      <el-form :model="form" ref="form" label-width="100px" size="small">
        <el-form-item :label="$t('organization.api_account')" prop="account">
          <el-input v-model="form.account" :placeholder="$t('organization.input_api_account')"/>
        </el-form-item>
        <el-form-item :label="$t('organization.api_password')" prop="password">
          <el-input v-model="form.password" auto-complete="new-password" :placeholder="$t('organization.input_api_password')" show-password/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="submit('form')" style="width: 400px">
            {{$t('commons.save')}}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="defect-tip">
      <div>{{$t('organization.use_tip')}}</div>
      <div>
        1. {{$t('organization.use_tip_one')}}
      </div>
      <div>
        2. {{$t('organization.use_tip_two')}}
        <router-link to="/track/project/all" style="margin-left: 5px">{{$t('organization.link_the_project_now')}}</router-link>
      </div>
    </div>
  </el-card>
</template>

<script>
  export default {
    name: "DefectManagement",
    data() {
      return {
        form: {},
        platform: '',
        platforms: [
          {
            name: 'TAPD',
            value: 'tapd',
          },
          {
            name: 'JIRA',
            value: 'jira',
          }
        ],
        rules: {
          account: {required: true, message: this.$t('organization.input_api_account'), trigger: ['change', 'blur']},
          password: {required: true, message: this.$t('organization.input_api_password'), trigger: ['change', 'blur']}
        },
      }
    },
    methods: {
      submit(form) {

      }
    }
  }
</script>

<style scoped>
  .header-title {
    padding: 10px 30px;
  }

  .defect-tip {
    background: #EDEDED;
    border: solid #E1E1E1 1px;
    margin: 10px 0;
    padding: 10px;
    border-radius: 3px;
  }
</style>
