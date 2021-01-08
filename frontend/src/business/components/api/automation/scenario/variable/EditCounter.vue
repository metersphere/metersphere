<template>
    <el-form :model="editData" label-position="right" label-width="80px" size="small" ref="form2">
      <el-form-item :label="$t('api_test.variable_name')" prop="name">
        <el-input v-model="editData.name" :placeholder="$t('api_test.variable_name')"></el-input>
      </el-form-item>

      <el-form-item :label="$t('commons.description')" prop="description">
        <el-input class="ms-http-textarea"
                  v-model="editData.description"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 10}"
                  :rows="2" size="small"/>
      </el-form-item>

      <el-form-item label="开始" prop="set">
        <el-input-number size="small" v-model="editData.startNumber" placeholder="0" :max="1000*10000000" :min="0"/>
        <span style="margin: 0px 10px 10px ">结束</span>
        <el-input-number size="small" v-model="editData.endNumber" placeholder="10" :max="1000*10000000" :min="0"/>
        <span style="margin: 0px 10px 10px ">增量</span>
        <el-input-number size="small" v-model="editData.increment" placeholder="1" :max="1000*10000000" :min="0"/>
      </el-form-item>

      <el-form-item label="开始" prop="value">
        <el-input v-model="editData.value" placeholder="000产生至少3位数字。user_000输出形式为user_nnn"></el-input>
      </el-form-item>
    </el-form>
</template>

<script>  
  export default {
    name: "MsEditCounter",
    components: {},
    props: {
      editData: {},
    },
  }
</script>

<style scoped>

</style>
