<template>
  <div class="flex flex-row gap-[8px] break-words">
    <MsAvatar :avatar="creatorInfo.avatar" />
    <div class="flex flex-1 flex-col">
      <div class="font-medium leading-[22px] text-[var(--color-text-1)]">
        {{ creatorInfo.name }}
        <span v-if="props.element.replyUser">{{ t('ms.comment.reply') }} {{ replyUserName }}</span>
      </div>
      <div v-dompurify-html="props.element.content" class="markdown-body mt-[4px] break-words break-all"></div>

      <div class="mt-[8px] flex items-center justify-between">
        <div class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
          {{ dayjs(props.element.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
        </div>
        <div class="flex gap-[8px]">
          <div v-if="props.mode === 'parent'" class="comment-btn" @click="expendChange">
            <MsIconfont type="icon-icon_comment_outlined" />
            <span>
              {{
                props.expandKeys.has(props.element.id) ? t('ms.comment.collapseComment') : t('ms.comment.expendComment')
              }}
            </span>
            <span class="text-[var(--color-text-4)]">({{ element.childComments?.length }})</span>
          </div>
          <div
            v-if="hasAnyPermission(props.permissions || [])"
            class="comment-btn hover:bg-[var(--color-bg-3)]"
            :class="{ 'bg-[var(--color-text-n8)]': status === 'reply' }"
            @click="replyClick"
          >
            <MsIconfont type="icon-icon_reply" />
          </div>
          <MoreAction v-if="hasAuth" :list="actionsList" @select="handleMoreActionSelect"></MoreAction>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import dayjs from 'dayjs';

  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsIconfont from '@/components/pure/ms-icon-font/index.vue';
  import MoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { useI18n } from '@/hooks/useI18n';
  import useUserStore from '@/store/modules/user/index';
  import { hasAnyPermission } from '@/utils/permission';

  import { CommentItem } from './types';

  const userStore = useUserStore();
  const { t } = useI18n();

  defineOptions({ name: 'MsCommentItem' });

  const props = defineProps<{
    element: CommentItem; // 评论的具体内容
    mode: 'parent' | 'child'; // 父级评论还是子级评论
    permissions?: string[]; // 权限列表
    onReply?: () => void; // 回复
    onEdit?: () => void; // 编辑
    onDelete?: () => void; // 删除
    expandKeys: Set<string | number>;
  }>();

  // 是否拥有编辑｜删除权限
  const hasAuth = computed(() => {
    return props.element.createUser === userStore.id && hasAnyPermission(props.permissions || []);
  });

  const status = defineModel<'normal' | 'edit' | 'reply' | 'delete'>('status', { default: 'normal' });

  const emit = defineEmits<{
    (event: 'reply'): void;
    (event: 'edit'): void;
    (event: 'delete'): void;
    (event: 'expend'): void;
  }>();

  const expendChange = () => {
    if (!props.element.childComments?.length) {
      return;
    }

    emit('expend');
  };
  const replyClick = () => {
    emit('reply');
    status.value = 'reply';
  };

  const editClick = () => {
    emit('edit');
    status.value = 'edit';
  };

  const deleteClick = () => {
    emit('delete');
    status.value = 'delete';
  };

  const actionsList: ActionsItem[] = [
    {
      label: t('ms.comment.edit'),
      eventTag: 'edit',
      permission: ['PROJECT_BUG:READ+COMMENT', 'FUNCTIONAL_CASE:READ+COMMENT'],
      icon: 'icon-icon_edit_outlined',
    },
    {
      label: t('ms.comment.delete'),
      eventTag: 'delete',
      permission: ['PROJECT_BUG:READ+COMMENT', 'FUNCTIONAL_CASE:READ+COMMENT'],
      danger: true,
      icon: 'icon-icon_delete-trash_outlined1',
    },
  ];

  function handleMoreActionSelect(item: ActionsItem) {
    if (item.eventTag === 'edit') {
      editClick();
    } else if (item.eventTag === 'delete') {
      deleteClick();
    }
  }

  const creatorInfo = computed(() => {
    return props.element.commentUserInfos.filter((item) => item != null && item.id === props.element.createUser)[0];
  });
  const replyUserName = computed(() => {
    if (props.element.replyUser) {
      return props.element.commentUserInfos.filter((item) => item != null && item.id === props.element.replyUser)[0]
        .name;
    }
    return '';
  });
</script>

<style lang="less" scoped>
  .comment-btn {
    @apply flex cursor-pointer items-center;

    padding: 2px 8px;
    font-size: 12px;
    border-radius: 4px;
    color: var(--color-text-4);
    gap: 4px;
  }
</style>
