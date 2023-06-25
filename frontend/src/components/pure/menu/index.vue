<script lang="tsx">
  import { defineComponent, ref, h, compile, computed } from 'vue';
  import { useRoute, useRouter, RouteRecordRaw } from 'vue-router';
  import { useI18n } from '@/hooks/useI18n';
  import useUser from '@/hooks/useUser';
  import type { RouteMeta } from 'vue-router';
  import { useAppStore, useUserStore } from '@/store';
  import { listenerRouteChange } from '@/utils/route-listener';
  import { openWindow, regexUrl } from '@/utils';
  import useMenuTree from './use-menu-tree';
  import { PERSONAL_ROUTE } from '@/router/routes/base';
  import { BOTTOM_MENU_LIST } from '@/router/constants';

  export default defineComponent({
    emit: ['collapse'],
    setup() {
      const { t } = useI18n();
      const appStore = useAppStore();
      const userStore = useUserStore();
      const router = useRouter();
      const route = useRoute();
      const { logout } = useUser();
      const { menuTree } = useMenuTree();
      const collapsed = computed({
        get() {
          if (appStore.device === 'desktop') return appStore.menuCollapse;
          return false;
        },
        set(value: boolean) {
          appStore.updateSettings({ menuCollapse: value });
        },
      });

      const openKeys = ref<string[]>([]);
      const selectedKey = ref<string[]>([]);

      const goto = (item: RouteRecordRaw | null) => {
        if (item) {
          // 如果菜单是外链
          if (regexUrl.test(item.path)) {
            openWindow(item.path);
            selectedKey.value = [item.name as string];
            return;
          }
          // 已激活的菜单重复点击不处理
          const { hideInMenu, activeMenu } = item.meta as RouteMeta;
          if (route.name === item.name && !hideInMenu && !activeMenu) {
            selectedKey.value = [item.name as string];
            return;
          }
          router.push({
            name: item.name,
          });
        } else {
          router.push({
            name: 'notFound',
          });
        }
      };
      const personalActiveMenus = ref(['']);
      /**
       * 查找激活的菜单项
       * @param target 目标菜单名
       */
      const findMenuOpenKeys = (target: string) => {
        const result: string[] = [];
        let isFind = false;
        const backtrack = (item: RouteRecordRaw | null, keys: string[]) => {
          if (target.includes(item?.name as string)) {
            result.push(...keys);
            if (result.length >= 2) {
              // 由于目前存在三级子路由，所以至少会匹配到三层才算结束
              isFind = true;
              return;
            }
          }
          if (item?.children?.length) {
            item.children.forEach((el) => {
              backtrack(el, [...keys, el.name as string]);
            });
          }
        };
        menuTree.value?.forEach((el: RouteRecordRaw | null) => {
          if (isFind) return; // 节省性能
          backtrack(el, [el?.name as string]);
        });
        personalActiveMenus.value = [''];
        if (result.length === 0) {
          backtrack(PERSONAL_ROUTE, [PERSONAL_ROUTE.name as string]);
          personalActiveMenus.value = [...result];
        }
        return result;
      };
      /**
       * 监听路由变化，存储打开及选中的菜单
       */
      listenerRouteChange((newRoute) => {
        const { requiresAuth, activeMenu, hideInMenu } = newRoute.meta;
        if (requiresAuth !== false && (!hideInMenu || activeMenu)) {
          const menuOpenKeys = findMenuOpenKeys((activeMenu || newRoute.name) as string);

          const keySet = new Set([...menuOpenKeys, ...openKeys.value]);
          openKeys.value = [...keySet];

          selectedKey.value = [activeMenu || menuOpenKeys[menuOpenKeys.length - 1]];
        }
      }, true);
      const setCollapse = (val: boolean) => {
        if (appStore.device === 'desktop') appStore.updateSettings({ menuCollapse: val });
      };

      const personalMenusVisble = ref(false);

      const personalMenus = [
        {
          label: t('personal.info'),
          icon: <icon-user />,
          // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
          route: PERSONAL_ROUTE.children![0],
        },
        {
          label: t('personal.switchOrg'),
          icon: <icon-swap />,
          event: () => {},
        },
        {
          divider: <a-divider class="ms-dropdown-divider" />,
        },
        {
          label: t('personal.exit'),
          icon: <icon-export />,
          event: logout,
        },
      ];

      const personalInfoMenu = () => {
        return (
          <a-trigger
            v-model:popup-visible={personalMenusVisble.value}
            trigger="click"
            unmount-on-close={false}
            popup-offset={4}
            position="right"
            class={['arco-trigger-menu absolute', personalMenusVisble.value ? 'block' : 'hidden']}
            v-slots={{
              content: () => (
                <div class="arco-trigger-menu-inner">
                  {personalMenus.map((e) => {
                    if (e.divider) {
                      return e.divider;
                    }
                    return (
                      <div
                        class={[
                          'arco-trigger-menu-item',
                          personalActiveMenus.value.includes(e.route?.name as string)
                            ? 'arco-trigger-menu-selected'
                            : '',
                        ]}
                        onClick={() => {
                          if (typeof e.event === 'function') {
                            e.event();
                          } else if (e.route) {
                            goto(e.route);
                          }
                          personalMenusVisble.value = false;
                        }}
                      >
                        {e.icon}
                        {e.label}
                      </div>
                    );
                  })}
                </div>
              ),
            }}
          >
            <a-menu-item class="flex items-center justify-between" key="personalInfo">
              <div class="hover:!bg-transparent">
                <icon-face-smile-fill />
                {userStore.name}
              </div>
              <icon-caret-down class="!m-0" />
            </a-menu-item>
          </a-trigger>
        );
      };

      const renderSubMenu = () => {
        function travel(_route: (RouteRecordRaw | null)[] | null, nodes = []) {
          if (_route) {
            _route.forEach((element) => {
              const icon = element?.meta?.icon ? () => h(compile(`<${element?.meta?.icon}/>`)) : null;
              const node =
                element?.children && element?.children.length !== 0 ? (
                  <a-sub-menu
                    key={element?.name}
                    v-slots={{
                      icon,
                      title: () => h(compile(t(element?.meta?.locale || ''))),
                    }}
                    class={BOTTOM_MENU_LIST.includes(element?.name as string) ? 'arco-menu-inline--bottom' : ''}
                  >
                    {travel(element?.children)}
                  </a-sub-menu>
                ) : (
                  <a-menu-item key={element?.name} v-slots={{ icon }} onClick={() => goto(element)}>
                    {t(element?.meta?.locale || '')}
                  </a-menu-item>
                );
              nodes.push(node as never);
            });
          }
          return nodes;
        }
        return travel(menuTree.value);
      };

      return () => (
        <a-menu
          mode={'vertical'}
          v-model:collapsed={collapsed.value}
          v-model:open-keys={openKeys.value}
          show-collapse-button={appStore.device !== 'mobile'}
          auto-open={false}
          selected-keys={selectedKey.value}
          auto-open-selected={true}
          level-indent={34}
          style="height: 100%;width:100%;"
          onCollapse={setCollapse}
          trigger-props={{
            'show-arrow': false,
            'popup-offset': -4,
          }}
          v-slots={{
            'collapse-icon': () => (appStore.menuCollapse ? <icon-right /> : <icon-left />),
          }}
        >
          {renderSubMenu()}
          {personalInfoMenu()}
        </a-menu>
      );
    },
  });
</script>

<style lang="less">
  .menu-wrapper {
    background-color: var(--color-bg-3);
  }
  .arco-menu {
    &:hover {
      .arco-menu-collapse-button {
        @apply flex;
      }
    }
    .arco-menu-collapse-button {
      @apply hidden rounded-full;

      top: 22px;
      right: 4px;
      border: 1px solid #ffffff;
      background: linear-gradient(90deg, rgb(var(--primary-9)) 3.36%, #ffffff 100%);
      box-shadow: 0 0 7px rgb(15 0 78 / 9%);
      .arco-icon {
        color: rgb(var(--primary-5));
      }
    }
  }
  .arco-menu-inner {
    @apply flex flex-col;

    padding: 16px 32px 16px 16px !important;
    .arco-menu-inline {
      &--bottom {
        @apply mt-auto;
      }
    }
    .arco-menu-pop-header {
      @apply leading-none;

      padding: 11px;
    }
    .arco-menu-inline-header {
      @apply flex items-center;
    }
    .arco-menu-inline-header,
    .arco-menu-item {
      @apply mb-0 !bg-transparent;

      color: var(--color-text-1) !important;
      &:hover,
      .arco-menu-indent-list:hover,
      .arco-icon:hover {
        background-color: rgb(var(--primary-1)) !important;
      }
      .arco-menu-item-inner,
      .arco-menu-item-inner:hover {
        @apply !bg-transparent;
      }
      .arco-menu-icon {
        .arco-icon {
          &:not(.arco-icon-down) {
            font-size: 18px;
          }

          color: var(--color-text-4);
        }
      }
      .arco-menu-title {
        color: var(--color-text-1);
      }
    }
    .arco-menu-selected {
      color: rgb(var(--primary-5)) !important;
      &:not(.arco-menu-inline-header) {
        background-color: rgb(var(--primary-9)) !important;
      }
      .arco-icon {
        color: rgb(var(--primary-5)) !important;
        &:hover {
          background-color: var(--color-bg-6) !important;
        }
      }
      .arco-menu-title {
        color: rgb(var(--primary-5)) !important;
      }
    }
    .arco-menu-pop {
      @apply bg-transparent;
    }
  }
  .arco-menu-collapsed {
    width: 86px;
  }
</style>
