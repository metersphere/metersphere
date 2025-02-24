import MentionList from './MentionList.vue';

import { getReviewUsers } from '@/api/modules/case-management/caseReview';
import useAppStore from '@/store/modules/app';

import { ReviewUserItem } from '@/models/caseManagement/caseReview';

import { VueRenderer } from '@halo-dev/richtext-editor';
import type { Content, Instance } from 'tippy.js';
import tippy from 'tippy.js';

const appStore = useAppStore();

const projectMember = ref<ReviewUserItem[]>([]);

async function getMembersToolBar(query: string) {
  const params = {
    projectId: appStore.currentProjectId,
    keyword: query,
  };
  try {
    projectMember.value = await getReviewUsers(params.projectId, params.keyword);
  } catch (error) {
    // eslint-disable-next-line no-console
    console.log(error);
  }
}

export default {
  allowedPrefixes: null, // 允许触发建议的前缀字符，设置为 null 以允许任何前缀字符
  startOfLine: false, // 在行的任何地方都可以激活
  items: async ({ query }: any) => {
    await getMembersToolBar(query);
    return projectMember.value.filter((item: ReviewUserItem) =>
      item.name.toLowerCase().startsWith(query.toLowerCase())
    );
  },

  render: () => {
    let component: VueRenderer;
    let popup: Instance[];

    return {
      onStart: (props: Record<string, any>) => {
        component = new VueRenderer(MentionList, {
          props,
          editor: props.editor,
        });

        if (!props.clientRect) {
          return;
        }
        popup = tippy('body', {
          getReferenceClientRect: props.clientRect,
          appendTo: () => document.body,
          content: component.element as Content, // FIX TS ERROR
          showOnCreate: true,
          interactive: true,
          trigger: 'manual',
          placement: 'bottom-start',
        });
      },

      onUpdate(props: Record<string, any>) {
        component.updateProps(props);

        if (!props.clientRect) {
          return;
        }

        popup[0].setProps({
          getReferenceClientRect: props.clientRect,
        });
      },

      onKeyDown(props: Record<string, any>) {
        if (props.event.key === 'Escape') {
          popup[0].hide();

          return true;
        }

        return component.ref?.onKeyDown(props);
      },

      onExit() {
        popup[0].destroy();
        component.destroy();
      },
    };
  },
};
