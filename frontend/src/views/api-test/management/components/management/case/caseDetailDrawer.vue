<template>
  <MsDrawer
    v-model:visible="innerVisible"
    unmount-on-close
    :title="t('caseManagement.featureCase.caseDetail')"
    :width="894"
    :footer="false"
    no-content-padding
  >
    <template #tbutton>
      <div class="right-operation-button-icon flex items-center gap-[4px]">
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']"
          type="icon"
          status="secondary"
          @click="caseDerailRef?.editCase()"
        >
          <MsIcon type="icon-icon_edit_outlined" />
          {{ t('common.edit') }}
        </MsButton>
        <MsButton type="icon" status="secondary" @click="caseDerailRef?.share()">
          <MsIcon type="icon-icon_link-copy_outlined" />
          {{ t('common.share') }}
        </MsButton>
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']"
          type="icon"
          status="secondary"
          @click="caseDerailRef?.follow()"
        >
          <MsIcon
            :type="props.detail.follow ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
            :class="[props.detail.follow ? '!text-[rgb(var(--warning-6))]' : '']"
          />
          {{ t('common.fork') }}
        </MsButton>
        <MsButton type="icon" status="secondary">
          <a-dropdown position="br" @select="handleSelect">
            <div>
              <icon-more />
              <span> {{ t('common.more') }}</span>
            </div>
            <template #content>
              <a-doption
                v-permission="['PROJECT_API_DEFINITION_CASE:READ+DELETE']"
                value="delete"
                class="error-6 text-[rgb(var(--danger-6))]"
              >
                <MsIcon type="icon-icon_delete-trash_outlined1" class="text-[rgb(var(--danger-6))]" />
                {{ t('common.delete') }}
              </a-doption>
            </template>
          </a-dropdown>
        </MsButton>
      </div>
    </template>
    <caseDetail
      ref="caseDerailRef"
      v-model:execute-case="executeCase"
      is-drawer
      :detail="props.detail"
      :api-detail="props.apiDetail"
      v-bind="$attrs"
    />
  </MsDrawer>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import caseDetail from './caseDetail.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  const props = defineProps<{
    detail: RequestParam;
    apiDetail?: RequestParam;
  }>();

  const { t } = useI18n();

  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });
  const executeCase = defineModel<boolean>('executeCase', { default: false });
  const caseDerailRef = ref<InstanceType<typeof caseDetail>>();

  function handleSelect(val: string | number | Record<string, any> | undefined) {
    switch (val) {
      case 'delete':
        caseDerailRef.value?.handleDelete();
        break;
      default:
        break;
    }
  }
</script>

<style scoped lang="less">
  .error-6 {
    color: rgb(var(--danger-6));
    &:hover {
      color: rgb(var(--danger-6));
    }
  }
</style>
