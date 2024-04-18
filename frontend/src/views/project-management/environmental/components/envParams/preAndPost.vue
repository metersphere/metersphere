<template>
  <div class="h-full">
    <a-tabs v-model:active-key="activeTab" lazy-load class="no-content">
      <a-tab-pane v-for="item of tabList" :key="item.key" :title="item.label"></a-tab-pane>
    </a-tabs>
    <a-divider margin="0"></a-divider>
    <div v-if="activeTab === 'scenarioProcessorConfig'" class="h-[calc(100vh - 100px)] mt-4">
      <a-alert class="mb-4" closable>
        {{
          props.activeType === EnvTabTypeEnum.ENVIRONMENT_PRE
            ? t('project.environmental.scenePreAlertDesc')
            : t('project.environmental.scenePostAlertDesc')
        }}
      </a-alert>
      <a-scrollbar
        :style="{
          overflow: 'auto',
          height: 'calc(100vh - 540px)',
        }"
      >
        <PreTab
          v-if="props.activeType === EnvTabTypeEnum.ENVIRONMENT_PRE"
          :show-associated-scene="showAssociatedScene"
          :show-pre-post-request="!showAssociatedScene"
          :active-tab="activeTab"
        />
        <PostTab
          v-if="props.activeType === EnvTabTypeEnum.ENVIRONMENT_POST"
          :show-associated-scene="showAssociatedScene"
          :show-pre-post-request="!showAssociatedScene"
          :active-tab="activeTab"
        />
      </a-scrollbar>
    </div>
    <div v-if="activeTab === 'requestProcessorConfig'" class="mt-4 h-full">
      <a-alert class="mb-4" closable>
        {{
          props.activeType === EnvTabTypeEnum.ENVIRONMENT_PRE
            ? t('project.environmental.requestPreAlertDesc')
            : t('project.environmental.requestPostAlertDesc')
        }}
      </a-alert>
      <PreTab
        v-if="props.activeType === EnvTabTypeEnum.ENVIRONMENT_PRE"
        :show-associated-scene="showAssociatedScene"
        :show-pre-post-request="!showAssociatedScene"
        :request-radio-text-props="requestPropsText"
        :active-tab="activeTab"
      />
      <PostTab
        v-if="props.activeType === EnvTabTypeEnum.ENVIRONMENT_POST"
        :show-associated-scene="showAssociatedScene"
        :show-pre-post-request="!showAssociatedScene"
        :request-radio-text-props="requestPropsText"
        :active-tab="activeTab"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import PostTab from './PostTab.vue';
  import PreTab from './PreTab.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { EnvTabTypeEnum } from '@/enums/envEnum';

  const { t } = useI18n();

  const props = defineProps<{
    activeType: string;
  }>();

  const tabList = ref<{ key: string; label: string }[]>([
    {
      key: 'scenarioProcessorConfig',
      label: t('project.environmental.scenario'),
    },
    {
      key: 'requestProcessorConfig',
      label: t('project.environmental.request'),
    },
  ]);
  const activeTab = ref<string>('scenarioProcessorConfig');
  const showAssociatedScene = computed(() => {
    return activeTab.value === 'scenarioProcessorConfig';
  });

  const requestPropsText = computed(() => {
    if (activeTab.value === 'requestProcessorConfig') {
      return props.activeType === EnvTabTypeEnum.ENVIRONMENT_PRE
        ? {
            pre: t('project.environmental.preScriptBefore'),
            post: t('project.environmental.preScriptAfter'),
            preTip: t('project.environmental.http.preTextPreTip'),
            postTip: t('project.environmental.http.preTextPostTip'),
          }
        : {
            pre: t('project.environmental.postScriptBefore'),
            post: t('project.environmental.postScriptAfter'),
            preTip: t('project.environmental.http.postTextPreTip'),
            postTip: t('project.environmental.http.postTextPostTip'),
          };
    }
  });
</script>

<style scoped lang="less">
  .no-content {
    :deep(.arco-tabs-content) {
      padding-top: 0;
    }
  }
</style>
