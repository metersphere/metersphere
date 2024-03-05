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
      <div class="h-full w-[180px] bg-white">
        <a-menu
          class="mr-[16px] w-[180px] min-w-[180px] bg-white p-[4px]"
          :default-selected-keys="defaultModule"
          @menu-item-click="clickModule"
        >
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
      </div>
      <a-divider direction="vertical" margin="8px"></a-divider>
      <div class="flex-1 justify-between p-[24px]">
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
        <div class="mt-[26px] flex h-[calc(100%-150px)]">
          <MsList
            v-model:data="messageHistoryList"
            mode="remote"
            item-key-field="id"
            :item-border="false"
            class="w-full rounded-[var(--border-radius-small)]"
            :no-more-data="noMoreData"
            raggable
            @reach-bottom="handleReachBottom"
          >
            <template #item="{ item }">
              <span class="p-[23px]">
                <div v-if="item.type === 'MENTIONED_ME'">
                  <div class="flex items-center">
                    <MSAvatar :avatar="item.avatar" />
                    <div class="ml-[8px] flex">
                      <div class="font-medium text-[var(--color-text-1)]">{{ item.userName }}</div>
                      <div class="font-medium text-[rgb(var(--primary-5))]"
                        >&nbsp;&nbsp;{{ t('ms.message.me', { var: '@' }) }}</div
                      >
                    </div>
                  </div>
                  <div class="ml-[50px] flex items-center">
                    <div class="font-medium text-[var(--color-text-2)]">{{ item.userName }}&nbsp;&nbsp;</div>
                    <div class="font-medium text-[var(--color-text-2)]">{{ item.subject }}：</div>
                    <MsButton @click="handleNameClick(item)">
                      <div class="one-line-text">
                        {{ item.resourceName }}
                      </div>
                    </MsButton>
                  </div>
                  <div class="ml-[50px] flex items-center">
                    {{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                  </div>
                </div>
                <div v-else>
                  <div class="flex items-center">
                    <div class="font-medium text-[var(--color-text-1)]">{{ t('ms.message.notice.title') }}</div>
                  </div>
                  <div class="flex items-center">
                    <div class="font-medium text-[var(--color-text-2)]">{{ item.userName }}&nbsp;&nbsp;</div>
                    <div class="font-medium text-[var(--color-text-2)]">{{ item.subject }}：</div>
                    <MsButton @click="handleNameClick(item)">
                      <div class="one-line-text">
                        {{ item.resourceName }}
                      </div>
                    </MsButton>
                  </div>
                  <div class="flex items-center">
                    {{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                  </div>
                </div>
              </span>
            </template>
          </MsList>
        </div>
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';
  import dayjs from 'dayjs';

  import MSAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsList from '@/components/pure/ms-list/index.vue';

  import {
    getMessageReadAll,
    MessageHistoryItem,
    OptionItem,
    queryMessageHistoryCount,
    queryMessageHistoryList,
  } from '@/api/modules/message';
  import { getMessageList } from '@/api/modules/project-management/messageManagement';
  import { useI18n } from '@/hooks/useI18n';
  import usePathMap from '@/hooks/usePathMap';

  import { MessageItem } from '@/models/projectManagement/message';

  import useAppStore from '../../../store/modules/app';
  import useUserStore from '../../../store/modules/user';

  const options = ref<OptionItem[]>([]);
  const messageHistoryList = ref<MessageHistoryItem[]>([]);
  const appStore = useAppStore();
  const userStore = useUserStore();
  const projectId = ref<string>(appStore.currentProjectId);
  const props = defineProps<{
    visible: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();

  const { t } = useI18n();
  const { jumpRouteByMapKey } = usePathMap();
  const innerVisible = useVModel(props, 'visible', emit);
  const position = ref('all');
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

  // 全部标记为已读
  async function prepositionEdit() {
    await getMessageReadAll();
  }

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
    res.list.forEach((item) => messageHistoryList.value.push(item));
    pageNation.value.total = res.total;
  }

  // 加载总数count
  async function loadTotalCount(key: string) {
    const res = await queryMessageHistoryCount({
      resourceType: key,
      receiver: userStore.id,
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

  // 加载模块count
  async function loadModuleCount(key: string) {
    const res = await queryMessageHistoryCount({
      resourceType: key,
      receiver: userStore.id,
      current: 1,
      pageSize: 10,
    });
    return res;
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
    } else {
      key = '';
    }
    position.value = 'all';
    messageHistoryList.value = [];
    pageNation.value.current = 1;
    currentResourceType.value = key;
    loadMessageHistoryList('all', key);
  }

  // 切换消息状态
  function changeShowType(value: string | number | boolean, ev: Event) {
    messageHistoryList.value = [];
    pageNation.value.current = 1;
    loadMessageHistoryList(value as string, currentResourceType.value);
  }

  // 滚动翻页
  function handleReachBottom() {
    pageNation.value.current += 1;
    if (pageNation.value.current > Math.ceil(pageNation.value.total / pageNation.value.pageSize)) {
      return;
    }
    loadMessageHistoryList(position.value, currentResourceType.value);
  }

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
    const number = parseInt(count, 10);
    if (number > 99) {
      return '+99';
    }
    return count;
  }

  // 点击名称跳转
  function handleNameClick(item: MessageHistoryItem) {
    console.log('开始跳转了', item);
    /**
     * const routeQuery: Record<string, any> = {
     *       organizationId: item.organizationId,
     *       projectId: item.projectId,
     *       id: item.resourceId,
     *     };
     *     if (item.organizationId === 'SYSTEM') {
     *       delete routeQuery.organizationId;
     *     }
     *     if (item.projectId === 'SYSTEM' || item.projectId === 'ORGANIZATION') {
     *       delete routeQuery.projectId;
     *     }
     *     jumpRouteByMapKey(item.module, routeQuery, true);
     */
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        messageHistoryList.value = [];
        pageNation.value.current = 1;
        // 左侧模块树加载
        loadModuleList();
        // 右边默认数据加载
        loadMessageHistoryList(position.value, '');
        // 左侧消息总数
        loadTotalCount('');
      }
    }
  );
</script>

<style lang="less" scoped>
  .right-align {
    float: right;
  }

  :deep(.arco-list) {
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
    width: 100%;
    overflow-y: auto;
    color: var(--color-text-1);
    font-size: 14px;
    line-height: 1.8715;
    border-radius: var(--border-radius-medium);
  }
</style>
