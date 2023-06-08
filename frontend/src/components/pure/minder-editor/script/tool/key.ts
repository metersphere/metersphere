/* eslint-disable no-bitwise */
import keymap from './keymap';

const CTRL_MASK = 0x1000;
const ALT_MASK = 0x2000;
const SHIFT_MASK = 0x4000;

interface KeyboardEvent {
  keyIdentifier?: string;
  ctrlKey?: string;
  metaKey?: string;
  altKey?: string;
  shiftKey?: string;
  keyCode: number;
}

function hashKeyEvent(keyEvent: KeyboardEvent): number {
  let hashCode = 0;
  if (keyEvent.ctrlKey || keyEvent.metaKey) {
    hashCode |= CTRL_MASK;
  }
  if (keyEvent.altKey) {
    hashCode |= ALT_MASK;
  }
  if (keyEvent.shiftKey) {
    hashCode |= SHIFT_MASK;
  }
  if ([16, 17, 18, 91].indexOf(keyEvent.keyCode) === -1) {
    if (keyEvent.keyCode === 229 && keyEvent.keyIdentifier) {
      hashCode |= parseInt(keyEvent.keyIdentifier.substr(2), 16);
      return hashCode;
    }
    hashCode |= keyEvent.keyCode;
  }
  return hashCode;
}

function hashKeyExpression(keyExpression: string): number {
  let hashCode = 0;
  keyExpression
    .toLowerCase()
    .split(/\s*\+\s*/)
    .forEach((name) => {
      switch (name) {
        case 'ctrl':
        case 'cmd':
          hashCode |= CTRL_MASK;
          break;
        case 'alt':
          hashCode |= ALT_MASK;
          break;
        case 'shift':
          hashCode |= SHIFT_MASK;
          break;
        default:
          hashCode |= keymap[name];
      }
    });
  return hashCode;
}

function hash(unknown: string | KeyboardEvent): number {
  if (typeof unknown === 'string') {
    return hashKeyExpression(unknown);
  }
  return hashKeyEvent(unknown);
}

function is(a: string | KeyboardEvent, b: string | KeyboardEvent): boolean {
  return !!a && !!b && hash(a) === hash(b);
}

export { hash, is };
