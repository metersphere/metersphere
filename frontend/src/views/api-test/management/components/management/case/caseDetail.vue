<template>
  <div class="h-full w-full overflow-hidden">
    <a-tabs v-model:active-key="activeKey" class="h-full px-[16px]" animation lazy-load>
      <template #extra>
        <div class="flex gap-[12px]">
          <environmentSelect v-if="props.isDrawer" ref="environmentSelectRef" />
          <execute
            ref="executeRef"
            v-model:detail="caseDetail"
            :environment-id="environmentId as string"
            is-case-detail
          />
          <a-dropdown position="br" :hide-on-select="false" @select="handleSelect">
            <a-button v-if="!props.isDrawer" type="outline">{{ t('common.operation') }}</a-button>
            <template #content>
              <a-doption v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']" value="edit">
                <MsIcon type="icon-icon_edit_outlined" />
                {{ t('common.edit') }}
              </a-doption>
              <a-doption value="share">
                <MsIcon type="icon-icon_share1" />
                {{ t('common.share') }}
              </a-doption>
              <a-doption v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']" value="fork">
                <MsIcon
                  :type="caseDetail.follow ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
                  :class="`${caseDetail.follow ? 'text-[rgb(var(--warning-6))]' : ''}`"
                />
                {{ t('common.fork') }}
              </a-doption>
              <a-divider v-permission="['PROJECT_API_DEFINITION_CASE:READ+DELETE']" margin="4px" />
              <a-doption
                v-permission="['PROJECT_API_DEFINITION_CASE:READ+DELETE']"
                value="delete"
                class="error-6 text-[rgb(var(--danger-6))]"
              >
                <MsIcon type="icon-icon_delete-trash_outlined" class="text-[rgb(var(--danger-6))]" />
                {{ t('common.delete') }}
              </a-doption>
            </template>
          </a-dropdown>
        </div>
      </template>
      <a-tab-pane key="detail" :title="t('apiTestManagement.detail')" class="px-[18px] py-[16px]">
        <MsDetailCard :title="`【${caseDetail.num}】${caseDetail.name}`" :description="description" class="mb-[8px]">
          <template #type="{ value }">
            <apiMethodName :method="value as RequestMethods" tag-size="small" is-tag />
          </template>
          <template #priority="{ value }">
            <caseLevel :case-level="value as CaseLevel" />
          </template>
        </MsDetailCard>
        <detailTab
          :detail="caseDetail"
          :protocols="protocols as ProtocolItem[]"
          :is-priority-local-exec="isPriorityLocalExec"
          is-case
          @execute="(val: 'localExec' | 'serverExec')=>executeRef?.execute(val)"
        />
      </a-tab-pane>
      <a-tab-pane key="reference" :title="t('apiTestManagement.reference')" class="px-[18px] py-[16px]">
        <tab-case-dependency :source-id="caseDetail.id" />
      </a-tab-pane>
      <a-tab-pane key="executeHistory" :title="t('apiTestManagement.executeHistory')" class="px-[18px] py-[16px]">
        <tab-case-execute-history :source-id="caseDetail.id" module-type="API_REPORT" :protocol="caseDetail.protocol" />
      </a-tab-pane>
      <!-- <a-tab-pane key="dependencies" :title="t('apiTestManagement.dependencies')" class="px-[18px] py-[16px]">
        </a-tab-pane> -->
      <a-tab-pane key="changeHistory" :title="t('apiTestManagement.changeHistory')" class="px-[18px] py-[16px]">
        <tab-case-change-history :source-id="caseDetail.id" />
      </a-tab-pane>
    </a-tabs>
  </div>
  <createAndEditCaseDrawer ref="createAndEditCaseDrawerRef" v-bind="$attrs" />
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsDetailCard from '@/components/pure/ms-detail-card/index.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import detailTab from '../api/preview/detail.vue';
  import createAndEditCaseDrawer from './createAndEditCaseDrawer.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import environmentSelect from '@/views/api-test/components/environmentSelect.vue';
  import execute from '@/views/api-test/components/executeButton.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import TabCaseChangeHistory from '@/views/api-test/management/components/management/case/tabContent/tabCaseChangeHistory.vue';
  import TabCaseDependency from '@/views/api-test/management/components/management/case/tabContent/tabCaseDependency.vue';
  import TabCaseExecuteHistory from '@/views/api-test/management/components/management/case/tabContent/tabCaseExecuteHistory.vue';

  import { deleteCase, toggleFollowCase } from '@/api/modules/api-test/management';
  import useModal from '@/hooks/useModal';

  import { ProtocolItem } from '@/models/apiTest/common';
  import { EnvConfig } from '@/models/projectManagement/environmental';
  import { RequestMethods } from '@/enums/apiEnum';

  const props = defineProps<{
    isDrawer?: boolean; // 抽屉
    detail: RequestParam;
  }>();
  const emit = defineEmits<{
    (e: 'updateFollow'): void;
    (e: 'deleteCase', id: string): void;
  }>();

  const { copy, isSupported } = useClipboard();
  const { t } = useI18n();
  const { openModal } = useModal();

  const caseDetail = ref<RequestParam>(cloneDeep(props.detail)); // props.detail是嵌套的引用类型，防止不必要的修改来源影响props.detail的数据
  watchEffect(() => {
    caseDetail.value = cloneDeep(props.detail); // props.detail是嵌套的引用类型，防止不必要的修改来源影响props.detail的数据
  });

  const activeKey = ref('detail');

  const description = computed(() => [
    {
      key: 'type',
      locale: 'apiTestManagement.apiType',
      value: caseDetail.value.method,
    },
    {
      key: 'priority',
      locale: 'case.caseLevel',
      value: caseDetail.value.priority,
    },
    {
      key: 'path',
      locale: 'apiTestManagement.path',
      value: caseDetail.value.url || caseDetail.value.path,
    },
    {
      key: 'tags',
      locale: 'common.tag',
      value: caseDetail.value.tags,
    },
  ]);

  const followLoading = ref(false);
  async function follow() {
    try {
      followLoading.value = true;
      await toggleFollowCase(caseDetail.value.id);
      Message.success(caseDetail.value.follow ? t('common.unFollowSuccess') : t('common.followSuccess'));
      emit('updateFollow');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      followLoading.value = false;
    }
  }

  function share() {
    if (isSupported) {
      copy(`${window.location.href}&cId=${caseDetail.value.id}`);
      Message.success(t('apiTestManagement.shareUrlCopied'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }

  const createAndEditCaseDrawerRef = ref<InstanceType<typeof createAndEditCaseDrawer>>();
  function editCase() {
    createAndEditCaseDrawerRef.value?.open(caseDetail.value.apiDefinitionId, caseDetail.value, false);
  }

  function handleDelete() {
    openModal({
      type: 'error',
      title: t('apiTestManagement.deleteApiTipTitle', { name: caseDetail.value.name }),
      content: t('case.deleteCaseTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteCase(caseDetail.value.id as string);
          emit('deleteCase', caseDetail.value.id as string);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function handleSelect(val: string | number | Record<string, any> | undefined) {
    switch (val) {
      case 'edit':
        editCase();
        break;
      case 'share':
        share();
        break;
      case 'fork':
        follow();
        break;
      case 'delete':
        handleDelete();
        break;
      default:
        break;
    }
  }

  const protocols = inject<Ref<ProtocolItem[]>>('protocols');

  const environmentSelectRef = ref<InstanceType<typeof environmentSelect>>();
  const currentEnvConfigByDrawer = computed<EnvConfig | undefined>(() => environmentSelectRef.value?.currentEnvConfig);
  const currentEnvConfigByInject = inject<Ref<EnvConfig>>('currentEnvConfig');
  const environmentId = computed(() =>
    props.isDrawer ? currentEnvConfigByDrawer.value?.id : currentEnvConfigByInject?.value?.id
  );

  const executeRef = ref<InstanceType<typeof execute>>();
  const isPriorityLocalExec = computed(() => executeRef.value?.isPriorityLocalExec ?? false);

  defineExpose({
    editCase,
    share,
    follow,
    handleDelete,
  });
</script>

<style lang="less" scoped>
  .arco-tabs {
    @apply flex flex-col;
    :deep(.arco-tabs-nav) {
      border-bottom: 1px solid var(--color-text-n8);
      &-extra {
        line-height: 32px;
      }
    }
    :deep(.arco-tabs-content) {
      @apply flex-1 pt-0;
      .arco-tabs-content-item {
        @apply px-0;
      }
      .arco-tabs-content-list {
        @apply h-full;
      }
      .arco-tabs-content-list .arco-tabs-content-item:nth-of-type(1) .arco-tabs-pane {
        @apply h-full overflow-hidden;
      }
      .arco-collapse {
        height: calc(100% - 85px);
      }
    }
  }
  :deep(.ms-detail-card-desc) {
    gap: 16px;
    flex-wrap: nowrap !important;
    & > div:nth-of-type(n) {
      width: auto;
      max-width: 30%;
    }
  }
  .error-6 {
    color: rgb(var(--danger-6));
    &:hover {
      color: rgb(var(--danger-6));
    }
  }
  :deep(.monaco-editor) {
    height: 212px !important;
  }
</style>
