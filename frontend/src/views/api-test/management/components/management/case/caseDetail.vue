<template>
  <preview
    :detail="activeApiTab"
    :module-tree="props.moduleTree"
    :protocols="protocols"
    is-case-detail
    @update-follow="activeApiTab.follow = !activeApiTab.follow"
  />
</template>

<script setup lang="ts">
  import preview from '@/views/api-test/management/components/management/api/preview/index.vue';

  import { getProtocolList } from '@/api/modules/api-test/common';
  import useAppStore from '@/store/modules/app';

  import { ProtocolItem } from '@/models/apiTest/common';
  import { ModuleTreeNode } from '@/models/common';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  const props = defineProps<{
    moduleTree: ModuleTreeNode[]; // 模块树
  }>();

  const appStore = useAppStore();

  const activeApiTab = defineModel<RequestParam>('activeApiTab', {
    required: true,
  });

  const protocols = ref<ProtocolItem[]>([]);
  async function initProtocolList() {
    try {
      protocols.value = await getProtocolList(appStore.currentOrgId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    initProtocolList();
  });
</script>
