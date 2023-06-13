<template>
  <div>
    <a-input-search
      v-model="searchKey"
      class="w-[252px]"
      :placeholder="t('system.userGroup.searchHolder')"
      @press-enter="searchData"
    />
    <div class="mt-2 flex flex-col">
      <div class="second-color">
        <icon-plus-circle v-if="!systemHidden" @click="handleSystemHidden" />
        <icon-minus-circle v-if="systemHidden" @click="handleSystemHidden" />
        <span class="ml-1"> {{ t('system.userGroup.inSystem') }}</span>
      </div>
      <div v-if="systemHidden">
        <div
          v-for="element in systemList"
          :key="element.id"
          :class="{
            'flex': true,
            ' h-[38px]': true,
            'items-center': true,
            'is-active': element.id === currentId,
            'px-[4px]': true,
          }"
          @click="currentId = element.id"
        >
          <div class="draglist-item flex grow flex-row justify-between">
            <div :class="'ml-[20px]'">{{ element.name }}</div>
            <div v-if="element.id === currentId">
              <a-popconfirm position="rb">
                <template #content>
                  <a-button type="primary" @click="addUser(element.id)">{{ t('system.userGroup.addUser') }}</a-button>
                </template>
                <icon-plus />
              </a-popconfirm>
            </div>
          </div>
        </div>
      </div>
      <a-divider />
      <div class="second-color flex items-center justify-between px-[4px]">
        <div>
          <icon-plus-circle v-if="!customHidden" @click="handleCustomHidden" />
          <icon-minus-circle v-if="customHidden" @click="handleCustomHidden" />
          <span class="ml-1"> {{ t('system.userGroup.customUserGroup') }}</span>
        </div>
        <div class="flex items-center">
          <icon-plus-circle class="primary-color text-xl" @click="addSystemUserGroup" />
        </div>
      </div>
      <div class="mt-[16px] px-[4px]">
        <div v-if="customShowEmpty" class="custom-empty">{{ t('system.userGroup.emptyUserGroup') }}</div>
        <draggable v-else v-model="customList" class="list-group" item-key="name" handle=".handle">
          <template #item="{ element }">
            <div
              :class="{
                'flex': true,
                ' h-[38px]': true,
                'items-center': true,
                'is-active': element.id === currentId,
                'px-[4px]': true,
              }"
              @click="currentId = element.id"
            >
              <div v-if="element.id === currentId" class="handle"><icon-drag-dot-vertical /></div>
              <div class="draglist-item flex grow flex-row justify-between">
                <div :class="element.id === currentId ? 'ml-[8px]' : 'ml-[20px]'">{{ element.name }}</div>
                <div v-if="element.id === currentId">
                  <a-popconfirm position="rb">
                    <template #content>
                      <a-button type="primary" @click="addUser(element.id)">{{
                        t('system.userGroup.addUser')
                      }}</a-button>
                    </template>
                    <icon-plus />
                  </a-popconfirm>
                </div>
              </div>
            </div>
          </template>
        </draggable>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import draggable from 'vuedraggable';
  import { UserGroupListItem } from './type';

  const { t } = useI18n();

  const searchKey = ref('');
  // true 关闭 +; false 打开 -
  const systemHidden = ref(true);
  const customHidden = ref(true);
  // 请求loading
  const loading = ref(false);
  const currentId = ref(0);

  const systemList = ref<UserGroupListItem[]>([
    { name: '系统管理员1', id: 1 },
    { name: '系统管理员2', id: 2 },
    { name: '系统管理员3', id: 3 },
  ]);

  const customList = ref<UserGroupListItem[]>([
    { name: '自定义用户组 1 (系统)', id: 4 },
    { name: '自定义用户组 2 (系统)', id: 5 },
    { name: '自定义用户组 3 (系统)', id: 6 },
  ]);
  // const customList = ref<UserGroupListItem[]>([]);

  const currentSystemId = ref(0);

  const handleSystemHidden = () => {
    systemHidden.value = !systemHidden.value;
  };

  const handleCustomHidden = () => {
    customHidden.value = !customHidden.value;
  };

  const addUser = (id: number) => {
    currentSystemId.value = id;
  };

  const addSystemUserGroup = () => {
    // eslint-disable-next-line no-console
    console.log('addSystemUserGroup');
  };

  function searchData(keyword: string) {
    // eslint-disable-next-line no-console
    console.log(keyword);
  }
  const customShowEmpty = computed(() => {
    return !loading.value && !customList.value.length;
  });
</script>

<style scoped lang="less">
  .primary-color {
    color: rgb(var(--primary-5));
  }
  .second-color {
    color: var(--color-text-input-border);
  }
  .handle {
    cursor: move;
    opacity: 0.3;
  }
  .is-active {
    background-color: rgb(var(--primary-1));
  }
  .custom-empty {
    padding: 8px;
    font-size: 12px;
    font-family: 'PingFang SC';
    font-weight: 400;
    border-radius: 4px;
    color: #8f959e;
    background: #f7f9fc;
    font-style: normal;
    line-height: 20px;
    overflow-wrap: break-word;
  }
</style>
