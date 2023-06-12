<template>
  <div class="type-list">
    <a-input-search v-model="searchKey" @press-enter="searchData" />
    <a-tree :data="treeData">
      <template #title="nodeData">
        <div>{{ nodeData }}</div>
      </template>
    </a-tree>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { TreeNodeData } from '@arco-design/web-vue';

  const originTreeData: TreeNodeData[] = [
    {
      title: 'Trunk 0-0',
      key: '0-0',
      children: [
        {
          title: 'Branch 0-0-1',
          key: '0-0-1',
          children: [
            {
              title: 'Leaf 0-0-1-1',
              key: '0-0-1-1',
            },
            {
              title: 'Leaf 0-0-1-2',
              key: '0-0-1-2',
            },
          ],
        },
      ],
    },
    {
      title: 'Trunk 0-1',
      key: '0-1',
      children: [
        {
          title: 'Branch 0-1-1',
          key: '0-1-1',
          children: [
            {
              title: 'Leaf 0-1-1-0',
              key: '0-1-1-0',
            },
          ],
        },
        {
          title: 'Branch 0-1-2',
          key: '0-1-2',
          children: [
            {
              title: 'Leaf 0-1-2-0',
              key: '0-1-2-0',
            },
          ],
        },
      ],
    },
  ];

  const treeData = ref(originTreeData);

  const searchKey = ref('');

  function searchData(keyword: string) {
    // eslint-disable-next-line no-console
    console.log(keyword);
  }
</script>

<style lang="less" scoped></style>
