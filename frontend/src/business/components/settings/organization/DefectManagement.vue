<template>
  <el-card class="header-title">
    <div>
      <div>请选择要集成的缺陷管理平台：</div>
      <el-radio-group v-model="platform" style="margin-top: 10px">
        <el-radio v-for="(item, index) in platforms" :key="index" :label="item.value" size="small">
          {{item.name}}
        </el-radio>
      </el-radio-group>
    </div>

    <div style="width: 500px">
      <div style="margin-top: 20px;margin-bottom: 10px">Basic Auth账号信息：</div>
      <el-form :model="form" ref="form" label-width="100px" size="small">
        <el-form-item label="API 账号" prop="account">
          <el-input v-model="form.account" placeholder="请输入账号"/>
        </el-form-item>
        <el-form-item label="API 口令" prop="password">
          <el-input v-model="form.password" auto-complete="new-password" placeholder="请输入口令" show-password/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="submit('form')" style="width: 400px">
            保存
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="defect-tip">
      <div>使用指引：</div>
      <div>
        1. Basic Auth 账号信息在"公司管理-安全与集成-开放平台"中查询
      </div>
      <div>
        2. 保存 Basic Auth 账号信息后，需要在 Metersphere 项目中手动关联 ID/key
        <router-link to="/track/project/all" style="margin-left: 5px">马上关联项目</router-link>
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
          account: {required: true, message: "请输入账号", trigger: ['change', 'blur']},
          password: {required: true, message: "请输入密码", trigger: ['change', 'blur']}
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
