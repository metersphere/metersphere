import Item from './comment-item.vue';
import CommentInput from './input.vue';

import { useI18n } from '@/hooks/useI18n';
import useModal from '@/hooks/useModal';

import { CommentItem, CommentParams } from './types';
import message from '@arco-design/web-vue/es/message';

export default defineComponent({
  name: 'MsComment',
  props: {
    currentUserId: {
      type: String,
      default: '',
    },

    commentList: {
      type: Array as PropType<CommentItem[]>,
      default: () => [],
    },
  },
  emits: {
    /* eslint-disable @typescript-eslint/no-unused-vars */
    updateOrAdd: (value: CommentParams) => true, // 更新或者新增评论
    delete: (value: string, cb: (result: boolean) => void) => true, // 删除评论
  },
  setup(props, { emit }) {
    const { currentUserId } = toRefs(props);
    const commentList = ref<CommentItem[]>([]);
    const currentItem = reactive<{ id: string; parentId: string }>({ id: '', parentId: '' });
    const { t } = useI18n();
    const { openModal } = useModal();

    const handlePublish = (content: string, item: CommentItem) => {
      const params: CommentParams = {
        ...item,
        content,
        event: 'REPLAY',
      };
      emit('updateOrAdd', params);
    };

    const handleDelete = (item: CommentItem) => {
      openModal({
        type: 'error',
        title: t('comment.deleteConfirm'),
        content: t('comment.deleteContent'),
        okText: t('common.confirmClose'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          emit('delete', item.id, (result: boolean) => {
            if (result) {
              message.success(t('common.deleteSuccess'));
            } else {
              message.error(t('common.deleteFail'));
            }
          });
        },
        hideCancel: false,
      });
    };

    const renderInput = (item: CommentItem) => {
      return <CommentInput onPublish={(content: string) => handlePublish(content, item)} {...item} />;
    };

    const renderChildrenList = (list?: CommentItem[]) => {
      if (!list || list.length === 0) {
        return null;
      }
      return list.map((item) => {
        return (
          <div class="flex flex-col gap-[24px]">
            <Item
              onReply={() => {
                currentItem.id = item.id;
                currentItem.parentId = item.parentId || '';
              }}
              onEdit={() => {
                currentItem.id = item.id;
                currentItem.parentId = item.parentId || '';
              }}
              onDelete={() => handleDelete(item)}
              mode={'child'}
              currentUserId={currentUserId.value}
              element={item}
            />
            {item.id === currentItem.id && renderInput(item)}
          </div>
        );
      });
    };

    const renderParentList = (list: CommentItem[]) => {
      return list.map((item) => {
        return (
          <Item mode={'parent'} onDelete={() => handleDelete(item)} currentUserId={currentUserId.value} element={item}>
            <div class="rounded border border-[var(--color-text-7)] p-[16px]">{renderChildrenList(item.children)}</div>
          </Item>
        );
      });
    };

    return () => <div class="ms-comment">{renderParentList(commentList.value)}</div>;
  },
});
