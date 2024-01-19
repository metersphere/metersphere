import { RouteRecordName, RouteRecordRaw } from 'vue-router';

import type { Editor, EditorState, EditorView, Node, Range, ResolvedPos, Slice } from '@halo-dev/richtext-editor';
import type { Component } from 'vue';

export interface RouteRecordAppend {
  parentName: RouteRecordName;
  route: RouteRecordRaw;
}
export interface PluginModule {
  /**
   * These components will be registered when plugin is activated.
   */
  components?: Record<string, Component>;

  /**
   * Activate hook will be called when plugin is activated.
   */
  activated?: () => void;

  /**
   * Deactivate hook will be called when plugin is deactivated.
   */
  deactivated?: () => void;

  routes?: RouteRecordRaw[] | RouteRecordAppend[];

  ucRoutes?: RouteRecordRaw[] | RouteRecordAppend[];

  extensionPoints?: any;
}

export interface ToolbarItem {
  priority: number;
  component: Component;
  props: {
    editor: Editor;
    isActive: boolean;
    disabled?: boolean;
    icon?: Component;
    title?: string;
    action?: () => void;
  };
  children?: ToolbarItem[];
}

interface BubbleMenuProps {
  pluginKey?: string;
  editor?: Editor;
  shouldShow: (props: {
    editor: Editor;
    state: EditorState;
    node?: HTMLElement;
    view?: EditorView;
    oldState?: EditorState;
    from?: number;
    to?: number;
  }) => boolean;
  tippyOptions?: Record<string, unknown>;
  getRenderContainer?: (node: HTMLElement) => HTMLElement;
  defaultAnimation?: boolean;
}

export interface BubbleItem {
  priority: number;
  component?: Component;
  props?: {
    isActive?: ({ editor }: { editor: Editor }) => boolean;
    visible?: ({ editor }: { editor: Editor }) => boolean;
    icon?: Component;
    iconStyle?: string;
    title?: string;
    action?: ({ editor }: { editor: Editor }) => Component | void;
  };
}

export interface NodeBubbleMenu extends BubbleMenuProps {
  component?: Component;
  items?: BubbleItem[];
}

export interface ToolboxItem {
  priority: number;
  component: Component;
  props: {
    editor: Editor;
    icon?: Component;
    title?: string;
    description?: string;
    action?: () => void;
  };
}

export interface CommandMenuItem {
  priority: number;
  icon: Component;
  title: string;
  keywords: string[];
  command: ({ editor, range }: { editor: Editor; range: Range }) => void;
}

export interface DragSelectionNode {
  $pos?: ResolvedPos;
  node?: Node;
  el: HTMLElement;
  nodeOffset?: number;
  dragDomOffset?: {
    x?: number;
    y?: number;
  };
}

export interface DraggableItem {
  getRenderContainer?: ({ dom, view }: { dom: HTMLElement; view: EditorView }) => DragSelectionNode;
  handleDrop?: ({
    view,
    event,
    slice,
    insertPos,
    node,
  }: {
    view: EditorView;
    event: DragEvent;
    slice: Slice;
    insertPos: number;
    node: Node;
  }) => boolean | void;
  // allow drag-and-drop query propagation downward
  allowPropagationDownward?: boolean;
}

export interface ExtensionOptions {
  getToolbarItems?: ({ editor }: { editor: Editor }) => ToolbarItem | ToolbarItem[];

  getCommandMenuItems?: () => CommandMenuItem | CommandMenuItem[];

  getBubbleMenu?: ({ editor }: { editor: Editor }) => NodeBubbleMenu;

  getToolboxItems?: ({ editor }: { editor: Editor }) => ToolboxItem | ToolboxItem[];

  getDraggable?: ({ editor }: { editor: Editor }) => DraggableItem | boolean;
}
