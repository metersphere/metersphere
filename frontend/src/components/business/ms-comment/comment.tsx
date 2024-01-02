// eslint-disable-next-line no-shadow

import Item from './comment-item.vue';
import CommentInput from './input.vue';

import { useI18n } from '@/hooks/useI18n';
import useModal from '@/hooks/useModal';

import { CommentItem, CommentParams } from './types';
import message from '@arco-design/web-vue/es/message';

export default defineComponent({
  name: 'MsComment',
  props: {
    commentList: {
      type: Array as PropType<CommentItem[]>,
      default: () => [],
    },
  },
  emits: {
    /* eslint-disable @typescript-eslint/no-unused-vars */
    updateOrAdd: (value: CommentParams, cb: (result: boolean) => void) => true, // 更新或者新增评论
    delete: (value: string) => true, // 删除评论
  },
  setup(props, { emit }) {
    const { commentList } = toRefs(props);
    const currentItem = reactive<{ id: string; parentId: string }>({ id: '', parentId: '' });
    const { t } = useI18n();
    const { openModal } = useModal();

    const handlePublish = (content: string, item: CommentItem) => {
      const params: CommentParams = {
        ...item,
        content,
        event: 'REPLAY',
      };
      emit('updateOrAdd', params, (result: boolean) => {
        if (result) {
          message.success(t('common.publishSuccess'));
        } else {
          message.error(t('common.publishFail'));
        }
      });
    };

    const handleDelete = (item: CommentItem) => {
      openModal({
        type: 'error',
        title: t('ms.comment.deleteConfirm'),
        content: t('ms.comment.deleteContent'),
        okText: t('common.confirmDelete'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          emit('delete', item.id);
        },
        hideCancel: false,
      });
    };

    const renderInput = (item: CommentItem) => {
      return (
        <CommentInput
          isShowAvatar={false}
          isUseBottom={false}
          onPublish={(content: string) => handlePublish(content, item)}
          {...item}
        />
      );
    };

    const renderChildrenList = (list?: CommentItem[]) => {
      if (!list || list.length === 0) {
        return null;
      }
      return list.map((item) => {
        return (
          <div class="flex flex-col">
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
          <Item mode={'parent'} onDelete={() => handleDelete(item)} element={item}>
            <div class="rounded border border-[var(--color-text-7)] p-[16px]">
              {renderChildrenList(item.childComments)}
            </div>
          </Item>
        );
      });
    };

    return () => <div class="ms-comment gap[24px] flex flex-col">{renderParentList(commentList.value)}</div>;
  },
});
