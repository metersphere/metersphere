<template>
  <div class="flex h-full flex-col">
    <div class="border-b border-[var(--color-text-n8)] px-[22px] pb-[16px]">
      <MsEditableTab v-model:active-tab="activeCaseTab" v-model:tabs="caseTabs">
        <template #label="{ tab }">
          <apiMethodName
            v-if="tab.id !== 'all'"
            :method="tab.protocol === 'HTTP' ? tab.method : tab.protocol"
            class="mr-[4px]"
          />
          <a-tooltip :content="tab.name || tab.label" :mouse-enter-delay="500">
            <div class="one-line-text max-w-[144px]">
              {{ tab.name || tab.label }}
            </div>
          </a-tooltip>
        </template>
      </MsEditableTab>
    </div>
    <div v-show="activeCaseTab.id === 'all'" class="flex-1">
      <caseTable :active-module="props.activeModule" :offspring-ids="props.offspringIds" :protocol="props.protocol" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import caseTable from './caseTable.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  const props = defineProps<{
    activeModule: string;
    offspringIds: string[];
    protocol: string;
  }>();

  const { t } = useI18n();

  const caseTabs = ref<RequestParam[]>([
    {
      id: 'all',
      label: t('case.allCase'),
      closable: false,
    } as RequestParam,
  ]);
  const activeCaseTab = ref<RequestParam>(caseTabs.value[0] as RequestParam);
</script>
