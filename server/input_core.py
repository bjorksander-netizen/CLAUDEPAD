#!/usr/bin/env python3
"""
CLAUDEPAD - core input injection & protocol handling (Windows).
Dipisah dari GUI supaya mudah diuji dan dipakai ulang.
"""

import ctypes
import json
import queue
import random
import socket
import subprocess

IS_WINDOWS = hasattr(ctypes, "windll")

if IS_WINDOWS:
    import ctypes.wintypes as wt
else:
    # Non-Windows: modul tetap bisa di-import (untuk pengujian di CI).
    class _StubTypes:
        LONG = ctypes.c_long
        DWORD = ctypes.c_ulong
        WORD = ctypes.c_ushort
    wt = _StubTypes()

WS_PORT = 8765
DISCOVERY_PORT = 8766

PIN = f"{random.randint(0, 9999):04d}"
CLIENTS = {}          # peer -> transport ("wifi" / "usb")
LOGQ = queue.Queue()
HOSTNAME = socket.gethostname()


def new_pin():
    global PIN
    PIN = f"{random.randint(0, 9999):04d}"
    return PIN


def log(msg):
    LOGQ.put(msg)


if IS_WINDOWS:
    user32 = ctypes.windll.user32
    kernel32 = ctypes.windll.kernel32
else:
    user32 = kernel32 = None


# ---------------------------------------------------------------- SendInput --
class MOUSEINPUT(ctypes.Structure):
    _fields_ = [("dx", wt.LONG), ("dy", wt.LONG), ("mouseData", wt.DWORD),
                ("dwFlags", wt.DWORD), ("time", wt.DWORD),
                ("dwExtraInfo", ctypes.c_void_p)]


class KEYBDINPUT(ctypes.Structure):
    _fields_ = [("wVk", wt.WORD), ("wScan", wt.WORD), ("dwFlags", wt.DWORD),
                ("time", wt.DWORD), ("dwExtraInfo", ctypes.c_void_p)]


class _INPUTunion(ctypes.Union):
    _fields_ = [("mi", MOUSEINPUT), ("ki", KEYBDINPUT)]


class INPUT(ctypes.Structure):
    _fields_ = [("type", wt.DWORD), ("u", _INPUTunion)]


INPUT_MOUSE, INPUT_KEYBOARD = 0, 1
MOUSEEVENTF_MOVE = 0x0001
MOUSEEVENTF_LEFTDOWN, MOUSEEVENTF_LEFTUP = 0x0002, 0x0004
MOUSEEVENTF_RIGHTDOWN, MOUSEEVENTF_RIGHTUP = 0x0008, 0x0010
MOUSEEVENTF_MIDDLEDOWN, MOUSEEVENTF_MIDDLEUP = 0x0020, 0x0040
MOUSEEVENTF_WHEEL, MOUSEEVENTF_HWHEEL = 0x0800, 0x1000
KEYEVENTF_KEYUP, KEYEVENTF_UNICODE = 0x0002, 0x0004


def _send(*inputs):
    if not IS_WINDOWS:
        return
    arr = (INPUT * len(inputs))(*inputs)
    user32.SendInput(len(inputs), arr, ctypes.sizeof(INPUT))


def mouse_event(flags, dx=0, dy=0, data=0):
    inp = INPUT(type=INPUT_MOUSE)
    inp.u.mi = MOUSEINPUT(dx, dy, data, flags, 0, None)
    _send(inp)


def mouse_move(dx, dy):
    mouse_event(MOUSEEVENTF_MOVE, int(dx), int(dy))


BUTTONS = {
    "left": (MOUSEEVENTF_LEFTDOWN, MOUSEEVENTF_LEFTUP),
    "right": (MOUSEEVENTF_RIGHTDOWN, MOUSEEVENTF_RIGHTUP),
    "middle": (MOUSEEVENTF_MIDDLEDOWN, MOUSEEVENTF_MIDDLEUP),
}


def mouse_click(btn="left", double=False):
    down, up = BUTTONS.get(btn, BUTTONS["left"])
    for _ in range(2 if double else 1):
        mouse_event(down)
        mouse_event(up)


def mouse_button(btn, press):
    down, up = BUTTONS.get(btn, BUTTONS["left"])
    mouse_event(down if press else up)


def mouse_scroll(dy=0, dx=0):
    if dy:
        mouse_event(MOUSEEVENTF_WHEEL, data=ctypes.c_ulong(int(dy)).value)
    if dx:
        mouse_event(MOUSEEVENTF_HWHEEL, data=ctypes.c_ulong(int(dx)).value)


def key_vk(vk, press):
    inp = INPUT(type=INPUT_KEYBOARD)
    inp.u.ki = KEYBDINPUT(vk, 0, 0 if press else KEYEVENTF_KEYUP, 0, None)
    _send(inp)


def type_text(text):
    for ch in text:
        if ch == "\n":
            key_vk(0x0D, True)
            key_vk(0x0D, False)
            continue
        code = ord(ch)
        for flags in (KEYEVENTF_UNICODE, KEYEVENTF_UNICODE | KEYEVENTF_KEYUP):
            inp = INPUT(type=INPUT_KEYBOARD)
            inp.u.ki = KEYBDINPUT(0, code, flags, 0, None)
            _send(inp)


VK = {
    "enter": 0x0D, "esc": 0x1B, "tab": 0x09, "backspace": 0x08, "delete": 0x2E,
    "space": 0x20, "up": 0x26, "down": 0x28, "left": 0x25, "right": 0x27,
    "home": 0x24, "end": 0x23, "pgup": 0x21, "pgdn": 0x22, "win": 0x5B,
    "ctrl": 0x11, "alt": 0x12, "shift": 0x10, "insert": 0x2D, "capslock": 0x14,
    "printscreen": 0x2C, "d": 0x44,
    **{f"f{i}": 0x6F + i for i in range(1, 13)},
}

MEDIA = {
    "playpause": 0xB3, "next": 0xB0, "prev": 0xB1, "stop": 0xB2,
    "volup": 0xAF, "voldown": 0xAE, "mute": 0xAD,
}


def press_key(name, mods=None):
    mods = mods or []
    name = (name or "").lower()
    if name in VK:
        vk = VK[name]
    elif len(name) == 1:
        if not IS_WINDOWS:
            return
        res = user32.VkKeyScanW(ord(name))
        if res == -1:
            type_text(name)
            return
        vk = res & 0xFF
    else:
        return
    for m in mods:
        key_vk(VK.get(m, 0x11), True)
    key_vk(vk, True)
    key_vk(vk, False)
    for m in reversed(mods):
        key_vk(VK.get(m, 0x11), False)


# ---------------------------------------------------------------- Volume -----
_volume_iface = None


def _get_volume_iface():
    """Interface pycaw untuk kontrol volume absolut. None kalau tidak tersedia."""
    global _volume_iface
    if _volume_iface is not None:
        return _volume_iface
    try:
        from ctypes import cast, POINTER
        from comtypes import CLSCTX_ALL
        from pycaw.pycaw import AudioUtilities, IAudioEndpointVolume
        devices = AudioUtilities.GetSpeakers()
        iface = devices.Activate(IAudioEndpointVolume._iid_, CLSCTX_ALL, None)
        _volume_iface = cast(iface, POINTER(IAudioEndpointVolume))
        return _volume_iface
    except Exception as e:                       # pycaw tidak ada / audio error
        log(f"[!] Volume absolut tidak tersedia ({e}); pakai tombol media.")
        return None


def volume_get():
    """Kembalikan volume 0..100, atau None kalau tidak bisa dibaca."""
    iface = _get_volume_iface()
    if iface is None:
        return None
    try:
        return int(round(iface.GetMasterVolumeLevelScalar() * 100))
    except Exception:
        return None


def volume_set(percent):
    percent = max(0, min(100, int(percent)))
    iface = _get_volume_iface()
    if iface is None:
        return False
    try:
        iface.SetMasterVolumeLevelScalar(percent / 100.0, None)
        return True
    except Exception:
        return False


# ---------------------------------------------------------------- Gestures ---
def gesture(name):
    """Gesture Windows Precision Touchpad."""
    if name == "taskview":                       # 3 jari ke atas
        press_key("tab", ["win"])
    elif name == "showdesktop":                  # 3 jari ke bawah
        press_key("d", ["win"])
    elif name == "appnext":                      # 3 jari ke kanan
        press_key("right", ["ctrl", "win"])
    elif name == "appprev":                      # 3 jari ke kiri
        press_key("left", ["ctrl", "win"])


def zoom(direction):
    """Pinch: Ctrl + scroll."""
    key_vk(VK["ctrl"], True)
    mouse_scroll(120 if direction > 0 else -120)
    key_vk(VK["ctrl"], False)


# ---------------------------------------------------------------- Dispatch ---
def handle_message(m, reply):
    """
    Proses satu pesan protokol. `reply` adalah callable(dict) untuk balasan.
    Dipisah dari layer WebSocket supaya bisa diuji tanpa jaringan.
    """
    t = m.get("t")
    if t == "move":
        mouse_move(m.get("dx", 0), m.get("dy", 0))
    elif t == "click":
        mouse_click(m.get("b", "left"), m.get("double", False))
    elif t == "down":
        mouse_button(m.get("b", "left"), True)
    elif t == "up":
        mouse_button(m.get("b", "left"), False)
    elif t == "scroll":
        mouse_scroll(m.get("dy", 0), m.get("dx", 0))
    elif t == "zoom":
        zoom(m.get("dir", 1))
    elif t == "gesture":
        gesture(m.get("g", ""))
    elif t == "text":
        type_text(m.get("s", ""))
    elif t == "key":
        press_key(m.get("k", ""), m.get("mods"))
    elif t == "media":
        vk = MEDIA.get(m.get("a", ""))
        if vk:
            key_vk(vk, True)
            key_vk(vk, False)
    elif t == "volset":
        if not volume_set(m.get("v", 50)):
            reply({"t": "volerr"})
    elif t == "volget":
        reply({"t": "vol", "v": volume_get()})
    elif t == "ping":
        reply({"t": "pong"})
    return t


# ---------------------------------------------------------------- Discovery --
def discovery_loop():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    try:
        s.bind(("0.0.0.0", DISCOVERY_PORT))
    except OSError as e:
        log(f"[!] Discovery gagal bind: {e}")
        return
    while True:
        try:
            data, addr = s.recvfrom(256)
            if data.strip() == b"DISCOVER_CLAUDEPAD":
                s.sendto(f"CLAUDEPAD|{HOSTNAME}|{WS_PORT}".encode(), addr)
        except OSError:
            break


def local_ips():
    ips = set()
    try:
        for info in socket.getaddrinfo(socket.gethostname(), None, socket.AF_INET):
            ips.add(info[4][0])
    except socket.gaierror:
        pass
    try:
        probe = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        probe.connect(("8.8.8.8", 80))
        ips.add(probe.getsockname()[0])
        probe.close()
    except OSError:
        pass
    return sorted(ip for ip in ips if not ip.startswith("127."))


def enable_usb_mode():
    """adb reverse supaya HP bisa konek lewat kabel USB."""
    try:
        subprocess.run(["adb", "start-server"], capture_output=True, timeout=20,
                       creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0))
        r = subprocess.run(["adb", "reverse", f"tcp:{WS_PORT}", f"tcp:{WS_PORT}"],
                           capture_output=True, text=True, timeout=20,
                           creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0))
        if r.returncode == 0:
            log("[USB] Aktif. Di aplikasi HP tekan tombol USB.")
            return True
        log("[USB] Gagal: " + (r.stderr.strip() or "cek kabel & USB debugging"))
    except FileNotFoundError:
        log("[USB] adb.exe tidak ditemukan. Install SDK Platform Tools.")
    except subprocess.TimeoutExpired:
        log("[USB] adb tidak merespons.")
    return False
