//package hotkey;

import java.awt.event.KeyEvent;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

import com.sun.jna.platform.win32.Kernel32; 
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;

import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.HOOKPROC;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;

public class HotKeys implements Runnable{

    private static HotKeys INSTANCE;

    private HHOOK hook;
    private HOOKPROC hookproc;
    private boolean quit = false;
    private boolean hearing = true;
    private Thread thread;
    private Set<Integer> keys;

    public static HotKeys getIstance() {
        if (INSTANCE == null) {
            INSTANCE = new HotKeys();
        }

        return INSTANCE;
    }

    private HotKeys() {
        keys = new HashSet<Integer>();

        hookproc = new LowLevelKeyboardProc() {
 
            public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT keyInfo) {

                LRESULT result;
                
                if (keys.contains(keyInfo.vkCode)) {
                    System.out.println("vkCode = " + keyInfo.vkCode + " scan_code = " + keyInfo.scanCode);
                    result = new LRESULT(1);
                }
                else {
                    //System.out.println("nCode =" + nCode + ", wParam =" + wParam + ", vkCode=" + keyInfo.vkCode);
                    result = new LRESULT(0);
                }

                return result;
            } 
        };

        SetHook();
    }

    public void SetHook() {
        hook = User32.INSTANCE.SetWindowsHookEx(User32.WH_KEYBOARD_LL, hookproc, Kernel32.INSTANCE.GetModuleHandle(null), 0);
        if (hook != null) {
            System.out.println("Hooked"); 
        } else {
            System.out.println("Error = " + Kernel32.INSTANCE.GetLastError()); 
        }
    }

    public void UnHook() {
        if (User32.INSTANCE.UnhookWindowsHookEx(hook)) {
            System.out.println("Unhooked");
        }
    }

    public void cycle() {
        User32.MSG msg = new User32.MSG();

        while (!quit) { 
            if (hearing)
                User32.INSTANCE.PeekMessage(msg, null, 0, 0, 0);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //stop();
        setHearing(false);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //stop();
    }

    public void addKey(int key) {
        keys.add(key);
    }

    public void removeKey(int key) {
        keys.remove(key);
    }

    public void start() {
        quit = false;
    }

    public void stop() {
        quit = true;
    }

    public void setHearing(boolean h) {
        hearing = h;
    }

    public void finalize() {
        quit = true;
        UnHook();
    }

 
    public static void main(String[] args) {
        HotKeys hotkey = HotKeys.getIstance();

        hotkey.addKey(91); // Win
        //hotkey.addKey(KeyEvent.VK_A);
        /* ============ CTRL ================*/
        //hotkey.addKey(78);  // Ctrl+N
        //hotkey.addKey(9);   // Ctrl+Tab
        //hotkey.addKey(27);  // Ctrl+Esc
        //hotkey.addKey(KeyEvent.VK_DELETE);

        //Thread t = new Thread(hotkey,"WinLock");
        //t.start();
        hotkey.cycle();
        
        hotkey.finalize();
    } 
}
