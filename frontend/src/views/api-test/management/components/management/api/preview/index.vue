<template>
  <div class="h-full w-full overflow-hidden">
    <div class="px-[18px] pt-[16px]">
      <MsDetailCard
        :title="`【${previewDetail.num}】${previewDetail.name}`"
        :description="description"
        :simple-show-count="4"
      >
        <template #titleAppend>
          <apiStatus :status="previewDetail.status" size="small" />
        </template>
        <template #titleRight>
          <a-button
            v-permission="['PROJECT_API_DEFINITION:READ+UPDATE']"
            type="outline"
            :loading="followLoading"
            size="mini"
            class="arco-btn-outline--secondary mr-[4px] !bg-transparent"
            @click="toggleFollowReview"
          >
            <div class="flex items-center gap-[4px]">
              <MsIcon
                :type="previewDetail.follow ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
                :class="`${previewDetail.follow ? 'text-[rgb(var(--warning-6))]' : 'text-[var(--color-text-4)]'}`"
                :size="14"
              />
              {{ t(previewDetail.follow ? 'common.forked' : 'common.fork') }}
            </div>
          </a-button>
          <a-button type="outline" size="mini" class="arco-btn-outline--secondary !bg-transparent" @click="share">
            <div class="flex items-center gap-[4px]">
              <MsIcon type="icon-icon_share1" class="text-[var(--color-text-4)]" :size="14" />
              {{ t('common.share') }}
            </div>
          </a-button>
        </template>
        <template #type="{ value }">
          <apiMethodName :method="value as RequestMethods" tag-size="small" is-tag />
        </template>
      </MsDetailCard>
    </div>
    <div class="h-[calc(100%-124px)]">
      <a-tabs v-model:active-key="activeKey" class="h-full" animation lazy-load>
        <a-tab-pane key="detail" :title="t('apiTestManagement.detail')" class="px-[18px] py-[16px]">
          <detailTab :detail="previewDetail" :protocols="props.protocols" />
        </a-tab-pane>
        <a-tab-pane key="reference" :title="t('apiTestManagement.reference')" class="px-[18px] py-[16px]">
          <quote :source-id="previewDetail.id" />
        </a-tab-pane>
        <!-- <a-tab-pane key="dependencies" :title="t('apiTestManagement.dependencies')" class="px-[18px] py-[16px]">
        </a-tab-pane> -->
        <a-tab-pane key="changeHistory" :title="t('apiTestManagement.changeHistory')" class="px-[18px] py-[16px]">
          <history :source-id="previewDetail.id" />
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsDetailCard from '@/components/pure/ms-detail-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import detailTab from './detail.vue';
  import history from './history.vue';
  import quote from './quote.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import { toggleFollowDefinition } from '@/api/modules/api-test/management';
  import { findNodeByKey } from '@/utils';

  import { ProtocolItem } from '@/models/apiTest/common';
  import { ModuleTreeNode } from '@/models/common';
  import { RequestMethods } from '@/enums/apiEnum';

  import { getValidRequestTableParams } from '@/views/api-test/components/utils';

  const props = defineProps<{
    detail: RequestParam;
    moduleTree: ModuleTreeNode[];
    protocols: ProtocolItem[];
  }>();
  const emit = defineEmits(['updateFollow']);

  const { copy, isSupported } = useClipboard();
  const { t } = useI18n();

  const previewDetail = ref<RequestParam>(cloneDeep(props.detail));

  watch(
    () => props.detail.id,
    () => {
      previewDetail.value = cloneDeep(props.detail); // props.detail是嵌套的引用类型，防止不必要的修改来源影响props.detail的数据
      const tableParam = getValidRequestTableParams(previewDetail.value); // 在编辑props.detail时，参数表格会多出一行默认数据，需要去除
      previewDetail.value = {
        ...previewDetail.value,
        body: {
          ...previewDetail.value.body,
          formDataBody: {
            formValues: tableParam.formDataBodyTableParams,
          },
          wwwFormBody: {
            formValues: tableParam.wwwFormBodyTableParams,
          },
        },
        headers: tableParam.headers,
        rest: tableParam.rest,
        query: tableParam.query,
        responseDefinition: tableParam.response,
      };
    },
    {
      immediate: true,
    }
  );

  const description = computed(() => [
    {
      key: 'type',
      locale: 'apiTestManagement.apiType',
      value: previewDetail.value.method,
    },
    {
      key: 'path',
      locale: 'apiTestManagement.path',
      value: previewDetail.value.url || previewDetail.value.path,
    },
    {
      key: 'tags',
      locale: 'common.tag',
      value: previewDetail.value.tags,
    },
    {
      key: 'description',
      locale: 'common.desc',
      value: previewDetail.value.description,
      width: '100%',
    },
    {
      key: 'belongModule',
      locale: 'apiTestManagement.belongModule',
      value: findNodeByKey<ModuleTreeNode>(props.moduleTree, previewDetail.value.moduleId, 'id')?.path,
    },
    {
      key: 'creator',
      locale: 'common.creator',
      value: previewDetail.value.createUserName,
    },
    {
      key: 'createTime',
      locale: 'apiTestManagement.createTime',
      value: dayjs(previewDetail.value.createTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      key: 'updateTime',
      locale: 'apiTestManagement.updateTime',
      value: dayjs(previewDetail.value.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    },
  ]);

  const followLoading = ref(false);
  async function toggleFollowReview() {
    try {
      followLoading.value = true;
      await toggleFollowDefinition(previewDetail.value.id);
      Message.success(previewDetail.value.follow ? t('common.unFollowSuccess') : t('common.followSuccess'));
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
      copy(`${window.location.href}&dId=${previewDetail.value.id}`);
      Message.success(t('apiTestManagement.shareUrlCopied'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }

  const activeKey = ref('detail');
</script>

<style lang="less" scoped></style>
