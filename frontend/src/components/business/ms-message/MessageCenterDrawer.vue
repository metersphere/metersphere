<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :width="880"
    :footer="false"
    no-content-padding
    unmount-on-close
    class="ms-drawer"
  >
    <template #title>
      <div class="flex">
        <span class="text-[var(--color-text-1)]">{{ t('ms.message.management') }}</span>
        <span class="text-[var(--color-text-4)]">{{ t('ms.message.extend') }}</span>
      </div>
    </template>
    <div class="flex h-full w-full">
      <div class="flex h-full w-[180px] flex-col bg-[var(--color-text-fff)]">
        <a-menu class="ms-message-menu" :default-selected-keys="[defaultModule]" @menu-item-click="clickModule">
          <a-menu-item :key="'all'">
            <div class="flex items-center justify-between">
              <span>{{ t('ms.message.all') }}</span>
              <a-badge class="ml-[4px] text-[var(--color-text-4)]" :text="defaultCount" />
            </div>
          </a-menu-item>
          <a-menu-item v-for="menu of moduleList" :key="menu.type">
            <div class="flex items-center justify-between">
              <span>{{ menu.name || '' }}</span>
              <a-badge class="ml-1" :text="getModuleCount(menu.type)" />
            </div>
          </a-menu-item>
        </a-menu>
        <div class="mt-auto">
          <div
            class="flex cursor-pointer items-center border-t border-[var(--color-text-n8)] px-[20px] py-[16px]"
            @click="openMessageManage"
          >
            <MsIcon type="icon-icon_setting_filled" class="text-[var(--color-text-4)]" />
            <div class="folder-name mx-[4px]">{{ t('ms.message.setting') }}</div>
          </div>
        </div>
      </div>
      <a-divider direction="vertical" :margin="0"></a-divider>
      <div class="flex-1 justify-between overflow-x-hidden p-[16px]">
        <a-radio-group v-model="position" type="button" @change="changeShowType">
          <a-radio value="all">{{ t('ms.message.list.all') }}</a-radio>
          <a-radio value="mentioned_me">{{ t('ms.message.list.me', { var: '@' }) }}</a-radio>
          <a-radio value="unRead">{{ t('ms.message.list.unRead') }}</a-radio>
          <a-radio value="read">{{ t('ms.message.list.read') }}</a-radio>
        </a-radio-group>

        <a-button type="text" class="right-align" @click="prepositionEdit">
          <MsIcon type="icon-icon_logs_outlined" class="mr-1 font-[16px] text-[rgb(var(--primary-5))]" />
          {{ t('ms.message.make.as.read') }}
        </a-button>
        <div class="mt-[16px] flex">
          <MsList
            v-model:data="messageHistoryList"
            mode="remote"
            item-key-field="id"
            :item-border="false"
            class="w-full rounded-[var(--border-radius-small)]"
            :no-more-data="noMoreData"
            :virtual-list-props="{
              height: 'calc(100vh - 136px)',
            }"
            @reach-bottom="handleReachBottom"
          >
            <template #item="{ item }">
              <div v-if="item.type === 'MENTIONED_ME'" class="mb-[16px] pt-[8px]">
                <div class="flex items-center">
                  <MSAvatar v-if="item.avatar" :avatar="item.avatar" :word="item.userName" />
                  <div class="ml-[8px] flex">
                    <a-tooltip :content="item.subject" :mouse-enter-delay="300">
                      <div class="one-line-text max-w-[300px] font-medium leading-[22px] text-[var(--color-text-1)]">
                        {{ item.subject }}
                      </div>
                    </a-tooltip>
                    <div class="font-medium text-[rgb(var(--primary-5))]"
                      >&nbsp;&nbsp;{{ t('ms.message.me', { var: '@' }) }}</div
                    >
                  </div>
                </div>
                <div class="flex items-center">
                  <a-tooltip :content="item.content.split(':')[0]" :mouse-enter-delay="300">
                    <div class="one-line-text ml-[8px] max-w-[300px] text-[var(--color-text-2)]"
                      >{{ item.content.split(':')[0] }}：</div
                    >
                  </a-tooltip>

                  <a-tooltip v-if="item.operation.includes('DELETE')" :content="item.resourceName">
                    <div
                      v-if="item.operation.includes('DELETE')"
                      class="one-line-text max-w-[300px] text-[var(--color-text-1)]"
                    >
                      {{ item.resourceName }}
                    </div>
                  </a-tooltip>
                  <MsButton v-else @click="handleNameClick(item)">
                    <a-tooltip :content="item.resourceName" :mouse-enter-delay="300">
                      <div class="one-line-text max-w-[300px]">
                        {{ item.resourceName }}
                      </div>
                    </a-tooltip>
                  </MsButton>
                </div>
                <div class="ml-[8px] flex items-center text-[var(--color-text-4)]">
                  {{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                </div>
              </div>
              <div v-else class="ms-message-item" @click.stop="setReadMessage(item)">
                <MSAvatar v-if="item.avatar" :avatar="item.avatar" :word="item.userName" />
                <div class="ml-[8px] flex flex-col">
                  <div class="flex items-center">
                    <a-badge v-if="item.status === 'UNREAD'" :count="9" dot :offset="[5, 2]">
                      <a-tooltip :content="item.subject">
                        <div class="one-line-text max-w-[300px] font-medium leading-[22px] text-[var(--color-text-1)]">
                          {{ item.subject }}
                        </div>
                      </a-tooltip>
                    </a-badge>
                    <a-tooltip v-else-if="item.status === 'READ'" :content="item.subject">
                      <div class="one-line-text max-w-[300px] font-medium leading-[22px] text-[var(--color-text-1)]">
                        {{ item.subject }}
                      </div>
                    </a-tooltip>
                  </div>
                  <div class="flex items-center">
                    <a-tooltip
                      v-if="item.operation.includes('DELETE')"
                      :content="item.content.split(':')[0]"
                      :mouse-enter-delay="300"
                    >
                      <div class="one-line-text max-w-[300px] text-[var(--color-text-2)]"
                        >{{ item.content.split(':')[0] }}：</div
                      >
                    </a-tooltip>
                    <a-tooltip
                      v-if="item.operation.includes('DELETE')"
                      :content="item.resourceName"
                      :mouse-enter-delay="300"
                    >
                      <div class="one-line-text max-w-[300px] text-[var(--color-text-1)]">
                        {{ item.resourceName }}
                      </div>
                    </a-tooltip>

                    <div v-else class="resource_template">
                      <a-tooltip :content="item.content.split(':')[0]" :mouse-enter-delay="300">
                        <div class="one-line-text max-w-[300px] text-[var(--color-text-2)]"
                          >{{ item.content.split(':')[0] }}：</div
                        >
                      </a-tooltip>
                      <MsButton @click="handleNameClick(item)">
                        <a-tooltip :content="item.resourceName" :mouse-enter-delay="300">
                          <div class="one-line-text max-w-[300px]">
                            {{ item.resourceName }}
                          </div>
                        </a-tooltip>
                      </MsButton>
                    </div>
                  </div>
                  <div class="flex items-center text-[var(--color-text-4)]">
                    {{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                  </div>
                </div>
              </div>
            </template>
          </MsList>
        </div>
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { useVModel } from '@vueuse/core';
  import dayjs from 'dayjs';

  import MSAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsList from '@/components/pure/ms-list/index.vue';

  import {
    getMessageRead,
    getMessageReadAll,
    MessageHistoryItem,
    OptionItem,
    queryMessageHistoryCount,
    queryMessageHistoryList,
  } from '@/api/modules/message';
  import { getMessageList } from '@/api/modules/project-management/messageManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';

  import { MessageItem } from '@/models/projectManagement/message';
  import { MessageResourceType } from '@/enums/messageEnum';
  import {
    ApiTestRouteEnum,
    BugManagementRouteEnum,
    CaseManagementRouteEnum,
    ProjectManagementRouteEnum,
    TestPlanRouteEnum,
  } from '@/enums/routeEnum';

  const props = defineProps<{
    visible: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();

  const appStore = useAppStore();
  const userStore = useUserStore();
  const router = useRouter();
  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const innerVisible = useVModel(props, 'visible', emit);
  const projectId = ref<string>(appStore.currentProjectId);
  const position = ref('all');
  const options = ref<OptionItem[]>([]);
  const messageHistoryList = ref<MessageHistoryItem[]>([]);
  const noMoreData = ref<boolean>(false);
  const moduleList = ref<MessageItem[]>([]);
  const defaultModule = ref<string>('all');
  const defaultCount = ref<string>('0');
  const currentResourceType = ref<string>('');
  const pageNation = ref({
    total: 0,
    pageSize: 10,
    current: 1,
  });

  // 左側模塊列表加載
  async function loadModuleList() {
    const list = await getMessageList({ projectId: projectId.value });
    moduleList.value = list;
  }

  // 右侧默认数据加载
  async function loadMessageHistoryList(val: string, resourceType: string) {
    let type = val;
    let status = val;
    if (val === 'all') {
      type = '';
      status = '';
    } else if (val === 'mentioned_me') {
      type = 'MENTIONED_ME';
      status = '';
    } else if (val === 'unRead') {
      type = '';
      status = 'UNREAD';
    } else if (val === 'read') {
      type = '';
      status = 'READ';
    } else {
      type = '';
      status = '';
    }
    const res = await queryMessageHistoryList({
      type,
      status,
      resourceType,
      receiver: userStore.id,
      current: pageNation.value.current || 1,
      pageSize: pageNation.value.pageSize,
    });
    messageHistoryList.value = messageHistoryList.value.concat(res.list);
    pageNation.value.total = res.total;
  }

  // 加载未读count
  async function loadTotalCount(key: string) {
    const res = await queryMessageHistoryCount({
      resourceType: key,
      status: position.value === 'all' || position.value === 'mentioned_me' ? '' : position.value,
      receiver: userStore.id,
      type: position.value === 'mentioned_me' ? 'MENTIONED_ME' : '',
      current: 1,
      pageSize: 10,
    });
    options.value = res;
    const find = options.value.find((item) => item.id === 'total');
    if (find) {
      const total = parseInt(find.name, 10);
      if (total > 99) {
        defaultCount.value = '+99';
      } else {
        defaultCount.value = find.name;
      }
    }
  }

  // 切换左侧模块
  function clickModule(key: string) {
    if (key === 'BUG_MANAGEMENT') {
      key = 'BUG';
    } else if (key === 'CASE_MANAGEMENT') {
      key = 'CASE';
    } else if (key === 'API_TEST_MANAGEMENT') {
      key = 'API';
    } else if (key === 'SCHEDULE_TASK_MANAGEMENT') {
      key = 'SCHEDULE';
    } else if (key === 'TEST_PLAN_MANAGEMENT') {
      key = 'TEST_PLAN';
    } else if (key === 'JENKINS_TASK_MANAGEMENT') {
      key = 'JENKINS';
    } else {
      key = '';
    }
    position.value = 'all';
    messageHistoryList.value = [];
    pageNation.value.current = 1;
    currentResourceType.value = key;
    loadMessageHistoryList('all', key);
    loadTotalCount(key);
  }

  function openMessageManage() {
    window.open(
      `${window.location.origin}#${
        router.resolve({ name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT }).fullPath
      }?orgId=${appStore.currentOrgId}&pId=${appStore.currentProjectId}`
    );
  }

  // 切换消息状态
  function changeShowType(value: string | number | boolean) {
    messageHistoryList.value = [];
    pageNation.value.current = 1;
    loadMessageHistoryList(value as string, currentResourceType.value);
    loadTotalCount(currentResourceType.value);
  }

  // 滚动翻页
  function handleReachBottom() {
    pageNation.value.current += 1;
    if (pageNation.value.current > Math.ceil(pageNation.value.total / pageNation.value.pageSize)) {
      return;
    }
    loadMessageHistoryList(position.value, currentResourceType.value);
    loadTotalCount(currentResourceType.value);
  }

  // 获取动态模块count
  function getModuleCount(type: string) {
    let count = '0';
    if (type === 'BUG_MANAGEMENT') {
      const module = options.value.find((item) => item.id === 'BUG');
      if (module) {
        count = module.name;
      }
    }
    if (type === 'CASE_MANAGEMENT') {
      const module = options.value.find((item) => item.id === 'CASE');
      if (module) {
        count = module.name;
      }
    }
    if (type === 'API_TEST_MANAGEMENT') {
      const module = options.value.find((item) => item.id === 'API');
      if (module) {
        count = module.name;
      }
    }
    if (type === 'SCHEDULE_TASK_MANAGEMENT') {
      const module = options.value.find((item) => item.id === 'SCHEDULE');
      if (module) {
        count = module.name;
      }
    }
    if (type === 'TEST_PLAN_MANAGEMENT') {
      const module = options.value.find((item) => item.id === 'TEST_PLAN');
      if (module) {
        count = module.name;
      }
    }
    if (type === 'JENKINS_TASK_MANAGEMENT') {
      const module = options.value.find((item) => item.id === 'JENKINS');
      if (module) {
        count = module.name;
      }
    }
    const number = parseInt(count, 10);
    if (number > 99) {
      return '+99';
    }
    return count;
  }

  // TODO: MessageResourceType
  const resourceTypeRouteMap: Record<string, string> = {
    [MessageResourceType.BUG_TASK]: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
    [MessageResourceType.BUG_SYNC_TASK]: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
    [MessageResourceType.FUNCTIONAL_CASE_TASK]: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE,
    [MessageResourceType.CASE_REVIEW_TASK]: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
    [MessageResourceType.API_DEFINITION_TASK]: ApiTestRouteEnum.API_TEST_MANAGEMENT,
    [MessageResourceType.API_SCENARIO_TASK]: ApiTestRouteEnum.API_TEST_SCENARIO,
    [MessageResourceType.TEST_PLAN_TASK]: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
    [MessageResourceType.JENKINS_TASK]: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
  };

  // 点击名称跳转
  function handleNameClick(item: MessageHistoryItem) {
    const routeQuery: Record<string, any> = {
      pId: item.projectId,
      id: item.resourceId,
    };
    if (item.projectId === 'SYSTEM' || item.projectId === 'ORGANIZATION') {
      delete routeQuery.projectId;
    }
    if (item.resourceType === MessageResourceType.API_DEFINITION_TASK) {
      delete routeQuery.id;
      if (item.operation.includes('CASE')) {
        routeQuery.cId = item.resourceId;
      } else if (!item.operation.includes('MOCK')) {
        routeQuery.dId = item.resourceId;
      }
    }

    const route = resourceTypeRouteMap[item.resourceType];
    openNewPage(route, routeQuery);
  }

  // 全部标记为已读
  async function prepositionEdit() {
    // 全读不需要类型KEY
    await getMessageReadAll(currentResourceType.value);
    messageHistoryList.value = [];
    pageNation.value.current = 1;
    // 左侧消息总数
    await loadTotalCount('');
    await loadMessageHistoryList(position.value, currentResourceType.value);
  }

  async function setReadMessage(item: MessageHistoryItem) {
    if (item.status === 'READ') {
      return;
    }
    try {
      await getMessageRead(item.id);
      loadMessageHistoryList(position.value, currentResourceType.value);
    } catch (error) {
      console.log(error);
    }
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        messageHistoryList.value = [];
        pageNation.value.current = 1;
        currentResourceType.value = '';
        // 左侧模块树加载
        loadModuleList();
        // 右边默认数据加载
        loadMessageHistoryList(position.value, '');
        // 左侧消息总数
        loadTotalCount('');
      }
    },
    { immediate: true }
  );
</script>

<style lang="less">
  .ms-drawer {
    .ms-message-menu {
      background-color: var(--color-text-fff);
      .arco-menu-inner {
        padding: 16px;
      }
    }
    .ms-message-item {
      @apply flex;

      padding: 8px;
      border-radius: var(--border-radius-small);
      &:not(:last-child) {
        margin-bottom: 8px;
      }
      &:hover {
        background-color: var(--color-text-n9);
      }
    }
  }
</style>

<style lang="less" scoped>
  .right-align {
    float: right;
  }
  :deep(.arco-list) {
    display: flex;
    overflow-y: auto;
    width: 100%;
    font-size: 14px;
    border-radius: var(--border-radius-medium);
    color: var(--color-text-1);
    flex-direction: column;
    box-sizing: border-box;
    line-height: 1.8715;
  }
  .resource_template {
    display: flex;
  }
</style>
