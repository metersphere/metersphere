import MentionList from './MentionList.vue';

import { getReviewerList } from '@/api/modules/case-management/featureCase';
import useAppStore from '@/store/modules/app';

import type { UserListItem } from '@/models/setting/user';

import { VueRenderer } from '@halo-dev/richtext-editor';
import type { Instance } from 'tippy.js';
import tippy from 'tippy.js';

const appStore = useAppStore();

const projectMember = ref<UserListItem[]>([]);

async function getMembersToolBar(query: string) {
  const params = {
    projectId: appStore.currentProjectId,
    keyword: query,
  };
  try {
    projectMember.value = await getReviewerList(params.projectId, params.keyword);
  } catch (error) {
    console.log(error);
  }
}

export default {
  items: async ({ query }: any) => {
    await getMembersToolBar(query);
    return projectMember.value.filter((item: UserListItem) => item.name.toLowerCase().startsWith(query.toLowerCase()));
  },

  render: () => {
    let component: VueRenderer;
    let popup: Instance[];

    return {
      onStart: (props: Record<string, any>) => {
        debugger;
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
          content: component.element,
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
