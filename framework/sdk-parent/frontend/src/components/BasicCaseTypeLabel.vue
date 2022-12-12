<template>
  <span>
      <el-tag v-if="value === 'apiCase'" class="status-label api">
        {{ $t('api_test.home_page.failed_case_list.table_value.case_type.api') }}
      </el-tag>
      <el-tag v-if="value === 'scenario'" class="status-label scene">
        {{ $t('api_test.home_page.failed_case_list.table_value.case_type.scene') }}
      </el-tag>
      <el-tag v-if="value === 'load'" class="status-label load">
        {{ $t('api_test.home_page.failed_case_list.table_value.case_type.load') }}
      </el-tag>
      <el-tag v-if="value === 'testCase'" class="status-label functional">
        {{ $t('api_test.home_page.failed_case_list.table_value.case_type.functional') }}
      </el-tag>
  </span>
</template>

<script>
export default {
  name: "BasicStatus",
  components: {},
  props: {
    value: {
      type: String
    }
  }
}
</script>

<style scoped>
.status-label {
  height: 24px;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  padding: 0 6px;
  border-radius: 2px;
  border-color: transparent;
}

.api {
  background-color: rgba(52, 199, 36, 0.2);
  color: #2EA121;
}

.scene {
  background-color: rgba(52, 199, 36, 0.2);
  color: #2EA121;
}

.load {
  background-color: rgba(255, 136, 0, 0.2);
  color: #DE7802;
}

.functional {
  background-color: rgba(51, 112, 255, 0.2);
  color: #245BDB;
}
</style>
