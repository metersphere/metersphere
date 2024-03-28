<template>
  <a-form ref="viewFormRef" layout="vertical" :model="systemForm">
    <div class="systemFieldWrapper">
      <a-form-item
        field="caseModule"
        :required="true"
        :label="t('system.orgTemplate.caseModule')"
        asterisk-position="end"
      >
        <a-select :disabled="true" :placeholder="t('system.orgTemplate.noDefaultPlaceholder')"></a-select>
      </a-form-item>
    </div>
  </a-form>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模板管理-用例模板左侧顶部系统字段内容
   */

  import { ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const systemForm = ref<Record<string, any>>({
    module: '',
  });
</script>

<style scoped></style>
