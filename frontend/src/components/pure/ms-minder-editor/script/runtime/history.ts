interface History {
  reset: () => void;
  undo: () => void;
  redo: () => void;
  hasUndo: () => boolean;
  hasRedo: () => boolean;
}

export default function HistoryRuntime(this: { minder: any; editText: () => void; history: History }) {
  const { minder } = this;
  const MAX_HISTORY = 100;

  let lastSnap: string;
  let patchLock: boolean;
  let undoDiffs: any[];
  let redoDiffs: any[];

  function reset() {
    undoDiffs = [];
    redoDiffs = [];
    lastSnap = minder.exportJson();
  }

  const _objectKeys = (() => {
    if (Object.keys) return Object.keys;

    return (o: object) => {
      const keys: string[] = [];
      Object.keys(o).forEach((i) => {
        keys.push(i);
      });
      return keys;
    };
  })();

  function escapePathComponent(str: string): string {
    if (str.indexOf('/') === -1 && str.indexOf('~') === -1) return str;
    return str.replace(/~/g, '~0').replace(/\//g, '~1');
  }

  function deepClone(obj: any): any {
    if (typeof obj === 'object') {
      return JSON.parse(JSON.stringify(obj));
    }
    return obj;
  }

  function _generate(mirror: any, obj: any, patches: any[], path: string) {
    const newKeys = _objectKeys(obj);
    const oldKeys = _objectKeys(mirror);
    // eslint-disable-next-line no-shadow
    let deleted = false;

    for (let t = oldKeys.length - 1; t >= 0; t--) {
      const key = oldKeys[t];
      const oldVal = mirror[key];
      // eslint-disable-next-line no-prototype-builtins
      if (obj.hasOwnProperty(key)) {
        const newVal = obj[key];
        if (typeof oldVal === 'object' && oldVal != null && typeof newVal === 'object' && newVal != null) {
          _generate(oldVal, newVal, patches, `${path}/${escapePathComponent(key)}`);
        } else if (oldVal !== newVal) {
          patches.push({
            op: 'replace',
            path: `${path}/${escapePathComponent(key)}`,
            value: deepClone(newVal),
          });
        }
      } else {
        patches.push({
          op: 'remove',
          path: `${path}/${escapePathComponent(key)}`,
        });
        deleted = true; // property has been deleted
      }
    }

    if (!deleted && newKeys.length === oldKeys.length) {
      return;
    }

    for (let t = 0; t < newKeys.length; t++) {
      const key = newKeys[t];
      // eslint-disable-next-line no-prototype-builtins
      if (!mirror.hasOwnProperty(key)) {
        patches.push({
          op: 'add',
          path: `${path}/${escapePathComponent(key)}`,
          value: deepClone(obj[key]),
        });
      }
    }
  }

  function jsonDiff(tree1: any, tree2: any): any[] {
    const patches: any = [];
    _generate(tree1, tree2, patches, '');
    return patches;
  }

  function makeUndoDiff(): boolean | void {
    const headSnap = minder.exportJson();
    const diff = jsonDiff(headSnap, lastSnap);
    if (diff.length) {
      undoDiffs.push(diff);
      while (undoDiffs.length > MAX_HISTORY) {
        undoDiffs.shift();
      }
      lastSnap = headSnap;
      return true;
    }
  }

  function makeRedoDiff() {
    const revertSnap = minder.exportJson();
    redoDiffs.push(jsonDiff(revertSnap, lastSnap));
    lastSnap = revertSnap;
  }

  function undo() {
    patchLock = true;
    const undoDiff = undoDiffs.pop();
    if (undoDiff) {
      minder.applyPatches(undoDiff);
      makeRedoDiff();
    }
    patchLock = false;
  }

  function redo() {
    patchLock = true;
    const redoDiff = redoDiffs.pop();
    if (redoDiff) {
      minder.applyPatches(redoDiff);
      makeUndoDiff();
    }
    patchLock = false;
  }

  function changed() {
    if (patchLock) return;
    if (makeUndoDiff()) redoDiffs = [];
  }

  function hasUndo() {
    return !!undoDiffs.length;
  }

  function hasRedo() {
    return !!redoDiffs.length;
  }

  function updateSelection(e: any) {
    if (!patchLock) return;
    const { patch } = e;
    switch (patch.express) {
      case 'node.add':
        minder.select(patch.node.getChild(patch.index), true);
        break;
      case 'node.remove':
      case 'data.replace':
      case 'data.remove':
      case 'data.add':
        minder.select(patch.node, true);
        break;
      default:
    }
  }

  this.history = {
    reset,
    undo,
    redo,
    hasUndo,
    hasRedo,
  };
  window.minderHistory = this.history;
  reset();
  minder.on('contentchange', changed);
  minder.on('import', reset);
  minder.on('patch', updateSelection);
}
