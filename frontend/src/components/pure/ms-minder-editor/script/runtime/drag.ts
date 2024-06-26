/* eslint-disable no-underscore-dangle */
/**
 * @fileOverview
 *
 * 用于拖拽节点时屏蔽键盘事件
 *
 * @author: techird
 * @copyright: Baidu FEX, 2014
 */
interface DragRuntimeOptions {
  fsm: any;
  minder: any;
  receiver: any;
}

function createDragRuntime(this: DragRuntimeOptions) {
  const { fsm, minder } = this;

  // listen the fsm changes, make action.
  function setupFsm() {
    // when jumped to drag mode, enter
    fsm.when('* -> drag', () => {
      // now is drag mode
      minder.fire('dragStart');
    });

    fsm.when('drag -> *', (exit: any, enter: any, reason: string) => {
      if (reason === 'drag-finish') {
        // now exit drag mode
        minder.fire('dragFinish');
      }
    });
  }

  // setup everything to go
  setupFsm();

  let downX: number;
  let downY: number;
  const MOUSE_HAS_DOWN = 0;
  const MOUSE_HAS_UP = 1;
  const BOUND_CHECK = 20;
  let flag = MOUSE_HAS_UP;
  let maxX: number;
  let maxY: number;
  let containerY: number;
  let freeHorizen = false;
  let freeVertical = false;
  let frame: number | null = null;

  function move(direction: 'left' | 'top' | 'right' | 'bottom' | false, speed?: number) {
    if (!direction) {
      freeHorizen = false;
      freeVertical = false;
      if (frame) {
        cancelAnimationFrame(frame);
      }
      frame = null;
      return;
    }
    if (!frame) {
      frame = requestAnimationFrame(
        ((directionF: 'left' | 'top' | 'right' | 'bottom', minderF: any, speedF = 0): any => {
          return () => {
            switch (directionF) {
              case 'left':
                minderF._viewDragger.move(
                  {
                    x: -speedF,
                    y: 0,
                  },
                  0
                );
                break;
              case 'top':
                minderF._viewDragger.move(
                  {
                    x: 0,
                    y: -speedF,
                  },
                  0
                );
                break;
              case 'right':
                minderF._viewDragger.move(
                  {
                    x: speedF,
                    y: 0,
                  },
                  0
                );
                break;
              case 'bottom':
                minderF._viewDragger.move(
                  {
                    x: 0,
                    y: speedF,
                  },
                  0
                );
                break;
              default:
                return;
            }
            if (frame) {
              cancelAnimationFrame(frame);
            }
          };
        })(direction, minder, speed)
      );
    }
  }

  minder.on('mousedown', (e: any) => {
    flag = MOUSE_HAS_DOWN;
    const rect = minder.getPaper().container.getBoundingClientRect();
    downX = e.originEvent.clientX;
    downY = e.originEvent.clientY;
    containerY = rect.top;
    maxX = rect.width;
    maxY = rect.height;
  });

  minder.on('mousemove', (e: any) => {
    if (
      fsm.state() === 'drag' &&
      flag === MOUSE_HAS_DOWN &&
      minder.getSelectedNode() &&
      (Math.abs(downX - e.originEvent.clientX) > BOUND_CHECK || Math.abs(downY - e.originEvent.clientY) > BOUND_CHECK)
    ) {
      const osx = e.originEvent.clientX;
      const osy = e.originEvent.clientY - containerY;

      if (osx < BOUND_CHECK) {
        move('right', BOUND_CHECK - osx);
      } else if (osx > maxX - BOUND_CHECK) {
        move('left', BOUND_CHECK + osx - maxX);
      } else {
        freeHorizen = true;
      }

      if (osy < BOUND_CHECK) {
        move('bottom', osy);
      } else if (osy > maxY - BOUND_CHECK) {
        move('top', BOUND_CHECK + osy - maxY);
      } else {
        freeVertical = true;
      }

      if (freeHorizen && freeVertical) {
        move(false);
      }
    }

    if (
      fsm.state() !== 'drag' &&
      flag === MOUSE_HAS_DOWN &&
      minder.getSelectedNode() &&
      (Math.abs(downX - e.originEvent.clientX) > BOUND_CHECK || Math.abs(downY - e.originEvent.clientY) > BOUND_CHECK)
    ) {
      fsm.jump('drag', 'user-drag');
    }
  });

  window.addEventListener(
    'mouseup',
    () => {
      flag = MOUSE_HAS_UP;
      if (fsm.state() === 'drag') {
        move(false);
        fsm.jump('normal', 'drag-finish');
      }
    },
    false
  );
}
export default createDragRuntime;
