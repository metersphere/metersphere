// eslint-disable-next-line no-shadow

import Item from './comment-item.vue';
import CommentInput from './input.vue';

import { useI18n } from '@/hooks/useI18n';

import { CommentItem, CommentParams } from './types';
import message from '@arco-design/web-vue/es/message';

export default defineComponent({
  name: 'MsComment',
  props: {
    commentList: {
      type: Array as PropType<CommentItem[]>,
      default: () => [],
    },
    disabled: {
      type: Boolean as PropType<boolean>,
      default: false,
    },
  },
  emits: {
    /* eslint-disable @typescript-eslint/no-unused-vars */
    updateOrAdd: (value: CommentParams, cb: (result: boolean) => void) => true, // 更新或者新增评论
    delete: (value: string) => true, // 删除评论
  },
  setup(props, { emit }) {
    const { commentList, disabled } = toRefs(props);
    const currentItem = reactive<{ id: string; parentId: string; status: string }>({
      id: '',
      parentId: '',
      status: 'add',
    });
    const { t } = useI18n();

    const resetCurrentItem = () => {
      currentItem.id = '';
      currentItem.parentId = '';
    };

    const handlePublish = (content: string, item: CommentItem) => {
      const params: CommentParams = {
        ...item,
        content,
        event: 'REPLAY',
        status: currentItem.status,
      };
      emit('updateOrAdd', params, (result: boolean) => {
        if (result) {
          message.success(t('common.publishSuccessfully'));
          resetCurrentItem();
        } else {
          message.error(t('common.publishFailed'));
        }
      });
    };

    const handleDelete = (item: CommentItem) => {
      emit('delete', item.id);
    };

    const handleReply = (item: CommentItem) => {
      if (item.childComments && Array.isArray(item.childComments)) {
        // 父级评论
        currentItem.id = item.id;
        currentItem.parentId = '';
      } else {
        // 子级评论
        currentItem.id = item.parentId || '';
        currentItem.parentId = item.id;
      }
      currentItem.status = 'replay';
    };

    const handelEdit = (item: CommentItem) => {
      currentItem.id = item.id;
      currentItem.parentId = item.parentId || '';
      currentItem.status = 'edit';
    };

    const noticeUserIds = ref<string[]>([]);

    const renderInput = (item: CommentItem) => {
      return (
        <CommentInput
          isShowAvatar={false}
          isUseBottom={false}
          onPublish={(content: string) => handlePublish(content, item)}
          defaultValue={item.content || ''}
          onCancel={() => resetCurrentItem()}
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
              onReply={() => handleReply(item)}
              onEdit={() => handelEdit(item)}
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
          <>
            <Item
              mode={'parent'}
              onReply={() => handleReply(item)}
              onEdit={() => handelEdit(item)}
              onDelete={() => handleDelete(item)}
              element={item}
            >
              <div class="rounded border border-[var(--color-text-7)] p-[16px]"></div>
            </Item>
            {item.id === currentItem.id && renderInput(item)}
          </>
        );
      });
    };

    return () => <div class="ms-comment gap[24px] flex flex-col">{renderParentList(commentList.value)}</div>;
  },
});
