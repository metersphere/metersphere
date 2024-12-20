<template>
  <MsCard
    has-breadcrumb
    :title="title"
    :loading="loading"
    :is-edit="isEdit"
    @save="saveHandler(false)"
    @save-and-continue="saveHandler(true)"
  >
    <template v-if="!isEdit" #headerRight>
      <a-select
        v-model="bugTemplateId"
        class="w-[240px]"
        :options="templateOption"
        allow-search
        :placeholder="t('bugManagement.edit.defaultSystemTemplate')"
      />
    </template>
    <div class="h-[calc(100vh-168px)] w-full">
      <BugDetail
        ref="bugDetailRef"
        v-model:template-id="bugTemplateId"
        :bug-id="bugId"
        :is-copy-bug="isCopy"
        @save-params="saveParams"
      />
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import BugDetail from './edit.vue';

  import { createOrUpdateBug, getTemplateOption } from '@/api/modules/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import useLeaveUnSaveTip from '@/hooks/useLeaveUnSaveTip';
  import useVisit from '@/hooks/useVisit';
  import router from '@/router';
  import { useAppStore } from '@/store';

  import { BugEditFormObject } from '@/models/bug-management';
  import { BugManagementRouteEnum } from '@/enums/routeEnum';

  defineOptions({ name: 'BugEditPage' });
  const { setIsSave } = useLeaveUnSaveTip();
  setIsSave(false);

  const { t } = useI18n();
  interface TemplateOption {
    label: string;
    value: string;
  }

  const appStore = useAppStore();
  const route = useRoute();
  const templateOption = ref<TemplateOption[]>([]);
  const bugTemplateId = ref<string>('');
  const loading = ref(false);
  const isEdit = computed(() => !!route.query.id && route.params.mode === 'edit');
  const bugId = computed(() => route.query.id as string | undefined);

  const isCopy = computed(() => route.params.mode === 'copy');

  const visitedKey = 'doNotNextTipCreateBug';

  const { getIsVisited } = useVisit(visitedKey);

  const title = computed(() => {
    if (isCopy.value) {
      return t('bugManagement.copyBug');
    }
    return isEdit.value ? t('bugManagement.editBug') : t('bugManagement.createBug');
  });

  const getTemplateOptions = async () => {
    try {
      loading.value = true;
      const res = await getTemplateOption(appStore.currentProjectId);
      templateOption.value = res.map((item) => {
        if (item.enableDefault && !route.query.id) {
          // 选中默认模板
          bugTemplateId.value = item.id;
        }
        return {
          label: item.name,
          value: item.id,
        };
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  const bugDetailRef = ref<InstanceType<typeof BugDetail>>();

  async function saveParams(isContinue: boolean, params: { request: BugEditFormObject; fileList: File[] }) {
    try {
      loading.value = true;
      const res = await createOrUpdateBug(params);
      if (isEdit.value) {
        setIsSave(true);
        Message.success(t('common.updateSuccess'));
        router.push({
          name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
        });
      } else {
        Message.success(t('common.createSuccess'));
        if (isContinue) {
          setIsSave(false);
          bugDetailRef.value?.resetForm();
        } else {
          setIsSave(true);
          // 跳转到成功页
          if (getIsVisited()) {
            router.push({
              name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
            });
            return;
          }
          router.push({
            name: BugManagementRouteEnum.BUG_MANAGEMENT_CREATE_SUCCESS,
            query: {
              ...route.query,
              id: res.data.id,
            },
          });
        }
      }
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const saveHandler = async (isContinue = false) => {
    bugDetailRef.value?.saveHandler(isContinue);
  };

  const initDefaultFields = async () => {
    await getTemplateOptions();
  };

  onBeforeMount(() => {
    initDefaultFields();
  });
</script>

<style lang="less" scoped>
  :deep(.arco-form-item-extra) {
    font-size: 14px;
    color: var(--color-text-4);
  }
  :deep(.arco-form-item-content) {
    overflow-wrap: anywhere;
  }
</style>
