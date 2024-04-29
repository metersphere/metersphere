<template>
  <MsDrawer
    v-model:visible="visible"
    unmount-on-close
    :title="t('mockManagement.mockDetail')"
    :width="960"
    :footer="false"
    no-content-padding
  >
    <template #tbutton>
      <div class="right-operation-button-icon flex items-center gap-[4px]">
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_MOCK:READ+UPDATE']"
          type="icon"
          status="secondary"
          @click="isEdit = true"
        >
          <MsIcon type="icon-icon_edit_outlined" />
          {{ t('common.edit') }}
        </MsButton>
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_MOCK:READ+DELETE']"
          type="icon"
          status="danger"
          @click="handleDelete"
        >
          <MsIcon type="icon-icon_delete-trash_outlined" class="text-[rgb(var(--danger-6))]" />
          {{ t('common.delete') }}
        </MsButton>
      </div>
    </template>
    <MsDetailCard :title="`【${mockDetail.num}】${mockDetail.name}`" :description="[]" class="mb-[16px]">
      <template #titleRight>
        <div class="flex items-center gap-[16px]">
          <div class="flex items-center gap-[8px]">
            <div class="whitespace-nowrap text-[var(--color-text-4)]">{{ t('apiTestManagement.apiType') }}</div>
            <apiMethodName :method="mockDetail.method" tag-size="small" is-tag />
          </div>
          <div class="flex items-center gap-[8px]">
            <div class="whitespace-nowrap text-[var(--color-text-4)]">{{ t('apiTestManagement.path') }}</div>
            <a-tooltip :content="mockDetail.apiPath">
              <div class="one-line-text">{{ mockDetail.apiPath }}</div>
            </a-tooltip>
          </div>
        </div>
      </template>
    </MsDetailCard>
    <a-form ref="mockForm" :model="mockDetail">
      <a-form-item
        class="hidden-item"
        field="name"
        :rules="[{ required: true, message: t('mockManagement.nameNotNull') }]"
      >
        <a-input
          v-model:model-value="mockDetail.name"
          :placeholder="t('mockManagement.namePlaceholder')"
          class="w-[732px]"
          :disabled="isReadOnly"
        ></a-input>
      </a-form-item>
      <a-form-item class="hidden-item" :rules="[{ required: true, message: t('mockManagement.nameNotNull') }]">
        <MsTagsInput
          v-model:model-value="mockDetail.tags"
          class="w-[732px]"
          :placeholder="t('mockManagement.namePlaceholder')"
          allow-clear
          unique-value
          retain-input-value
          :max-tag-count="5"
          :disabled="isReadOnly"
        />
      </a-form-item>
    </a-form>
    <div class="mb-[8px] font-medium">{{ t('mockManagement.matchRule') }}</div>
    <div class="mb-[8px] flex items-center justify-between">
      <a-radio-group v-model:model-value="mockDetail.bodyType" type="button" size="small" :disabled="isReadOnly">
        <a-radio v-for="item of RequestBodyFormat" :key="item" :value="item">
          {{ requestBodyTypeMap[item] }}
        </a-radio>
      </a-radio-group>
    </div>
    <div
      v-if="mockDetail.bodyType === RequestBodyFormat.NONE"
      class="flex h-[100px] items-center justify-center rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] text-[var(--color-text-4)]"
    >
      {{ t('apiTestDebug.noneBody') }}
    </div>
    <div v-else class="flex h-[calc(100%-34px)]">
      <MsCodeEditor
        v-model:model-value="currentBodyCode"
        :read-only="isReadOnly"
        class="flex-1"
        theme="vs"
        height="100%"
        :show-full-screen="false"
        :show-theme-change="false"
        :show-code-format="true"
        :language="currentCodeLanguage"
      >
      </MsCodeEditor>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDetailCard from '@/components/pure/ms-detail-card/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';

  import { requestBodyTypeMap } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';

  import { RequestBodyFormat } from '@/enums/apiEnum';

  const emit = defineEmits<{
    (e: 'delete'): void;
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });

  const isEdit = ref(false);
  const mockDetail = ref<any>();
  const isReadOnly = computed(() => !isEdit.value && !mockDetail.value.id);

  // 当前显示的代码
  const currentBodyCode = computed({
    get() {
      if (mockDetail.value.bodyType === RequestBodyFormat.JSON) {
        return mockDetail.value.jsonBody.jsonValue;
      }
      if (mockDetail.value.bodyType === RequestBodyFormat.XML) {
        return mockDetail.value.xmlBody.value;
      }
      return mockDetail.value.rawBody.value;
    },
    set(val) {
      if (mockDetail.value.bodyType === RequestBodyFormat.JSON) {
        mockDetail.value.jsonBody.jsonValue = val;
      } else if (mockDetail.value.bodyType === RequestBodyFormat.XML) {
        mockDetail.value.xmlBody.value = val;
      } else {
        mockDetail.value.rawBody.value = val;
      }
    },
  });
  // 当前代码编辑器的语言
  const currentCodeLanguage = computed(() => {
    if (mockDetail.value.bodyType === RequestBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (mockDetail.value.bodyType === RequestBodyFormat.XML) {
      return LanguageEnum.XML;
    }
    return LanguageEnum.PLAINTEXT;
  });

  function handleDelete() {
    emit('delete');
  }
</script>

<style lang="less" scoped></style>
