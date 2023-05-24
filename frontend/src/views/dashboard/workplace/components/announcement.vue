<template>
  <a-card
    class="general-card"
    :title="$t('workplace.announcement')"
    :header-style="{ paddingBottom: '0' }"
    :body-style="{ padding: '15px 20px 13px 20px' }"
  >
    <template #extra>
      <a-link>{{ $t('workplace.viewMore') }}</a-link>
    </template>
    <div>
      <div v-for="(item, idx) in list" :key="idx" class="item">
        <a-tag :color="item.type" size="small">{{ item.label }}</a-tag>
        <span class="item-content">
          {{ item.content }}
        </span>
      </div>
    </div>
  </a-card>
</template>

<script lang="ts" setup>
  const list = [
    {
      type: 'orangered',
      label: '活动',
      content: '内容最新优惠活动',
    },
    {
      type: 'cyan',
      label: '消息',
      content: '新增内容尚未通过审核，详情请点击查看。',
    },
    {
      type: 'blue',
      label: '通知',
      content: '当前产品试用期即将结束，如需续费请点击查看。',
    },
    {
      type: 'blue',
      label: '通知',
      content: '1月新系统升级计划通知',
    },
    {
      type: 'cyan',
      label: '消息',
      content: '新增内容已经通过审核，详情请点击查看。',
    },
  ];
</script>

<style scoped lang="less">
  .item {
    display: flex;
    align-items: center;
    margin-bottom: 4px;
    width: 100%;
    height: 24px;
    .item-content {
      overflow: hidden;
      margin-left: 4px;
      font-size: 13px;
      text-overflow: ellipsis;
      text-decoration: none;
      white-space: nowrap;
      color: var(--color-text-2);
      flex: 1;
      cursor: pointer;
    }
  }
</style>
