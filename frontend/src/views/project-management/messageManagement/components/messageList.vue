<template>
  <MsCard ref="fullRef" :special-height="132" :is-fullscreen="isFullscreen" simple>
    <div class="flex items-center justify-between">
      <div class="font-medium text-[var(--color-text-000)]">{{ t('project.messageManagement.config') }}</div>
      <div>
        <MsSelect
          v-model:model-value="robotFilters"
          :options="robotOptions"
          :allow-search="false"
          class="mr-[8px] w-[240px]"
          :prefix="t('project.messageManagement.robot')"
          value-key="id"
          :multiple="true"
          :has-all-select="true"
        >
          <template #footer>
            <div class="mb-[6px] mt-[4px] p-[3px_8px]">
              <MsButton type="text" @click="emit('createRobot')">
                <MsIcon type="icon-icon_add_outlined" class="mr-[8px] text-[rgb(var(--primary-6))]" size="14" />
                {{ t('project.messageManagement.createBot') }}
              </MsButton>
            </div>
          </template>
        </MsSelect>
        <a-button type="outline" class="arco-btn-outline--secondary px-[5px]" @click="toggle">
          <template #icon>
            <MsIcon
              :type="isFullscreen ? 'icon-icon_off_screen' : 'icon-icon_full_screen_one'"
              class="text-[var(--color-text-4)]"
              size="14"
            />
          </template>
          {{ t(isFullscreen ? 'common.offFullScreen' : 'common.fullScreen') }}
        </a-button>
      </div>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useFullscreen } from '@vueuse/core';
  import { useI18n } from '@/hooks/useI18n';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';

  import type { SelectOptionData } from '@arco-design/web-vue';

  const emit = defineEmits(['createRobot']);

  const { t } = useI18n();

  const robotFilters = ref([]);
  const robotOptions = ref<SelectOptionData[]>([
    {
      label: '机器人1',
      id: 'robot1',
    },
    {
      label: '机器人2',
      id: 'robot2',
    },
  ]);
  const fullRef = ref<HTMLElement | null>();

  const { isFullscreen, toggle } = useFullscreen(fullRef);
</script>

<style lang="less" scoped></style>
