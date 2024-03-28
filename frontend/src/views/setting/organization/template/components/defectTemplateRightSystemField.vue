<template>
  <a-form ref="viewFormRef" layout="vertical" :model="systemForm">
    <div class="systemFieldWrapper">
      <a-form-item
        field="defectHandleUser"
        :required="true"
        :label="t('system.orgTemplate.defectHandleUser')"
        asterisk-position="end"
      >
        <a-select :disabled="true" :placeholder="t('system.orgTemplate.noDefaultPlaceholder')"></a-select>
      </a-form-item>
    </div>
    <div class="systemFieldWrapper">
      <a-form-item
        field="defectStatus"
        :required="true"
        :label="t('system.orgTemplate.defectStatus')"
        asterisk-position="end"
      >
        <a-select :disabled="true" :placeholder="t('system.orgTemplate.noDefaultPlaceholder')"></a-select>
      </a-form-item>
    </div>
  </a-form>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织/项目-模板管理-缺陷模板右侧顶部系统字段内容
   */

  import { ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const systemForm = ref<Record<string, any>>({
    handleUser: '',
    status: '',
  });
</script>

<style scoped></style>
