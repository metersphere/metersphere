const keymap: Record<string, any> = {
  'Shift': 16,
  'Control': 17,
  'Alt': 18,
  'CapsLock': 20,

  'BackSpace': 8,
  'Tab': 9,
  'Enter': 13,
  'Esc': 27,
  'Space': 32,

  'PageUp': 33,
  'PageDown': 34,
  'End': 35,
  'Home': 36,

  'Insert': 45,

  'Left': 37,
  'Up': 38,
  'Right': 39,
  'Down': 40,

  'Direction': {
    37: 1,
    38: 1,
    39: 1,
    40: 1,
  },

  'Del': 46,

  'NumLock': 144,

  'Cmd': 91,
  'CmdFF': 224,
  'F1': 112,
  'F2': 113,
  'F3': 114,
  'F4': 115,
  'F5': 116,
  'F6': 117,
  'F7': 118,
  'F8': 119,
  'F9': 120,
  'F10': 121,
  'F11': 122,
  'F12': 123,

  '`': 192,
  '=': 187,
  '-': 189,

  '/': 191,
  '.': 190,
};

Object.keys(keymap).forEach((key) => {
  if (Object.prototype.hasOwnProperty.call(keymap, key)) {
    keymap[key.toLowerCase()] = keymap[key];
  }
});

const aKeyCode = 65;
const aCharCode = 'a'.charCodeAt(0);

'abcdefghijklmnopqrstuvwxyz'.split('').forEach((letter) => {
  keymap[letter] = aKeyCode + (letter.charCodeAt(0) - aCharCode);
});

let n = 9;
do {
  keymap[n.toString()] = n + 48;
} while (--n);

export default keymap;
