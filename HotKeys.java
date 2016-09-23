//package hotkey;

import java.awt.event.KeyEvent;

import com.sun.jna.platform.win32.Kernel32; 
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.KeyboardUtils;
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
    private boolean filter = true;
    private boolean quit = false;

    //http://www.kbdedit.com/manual/low_level_vk_list.html
    final private int VK_LWIN   = 91;
    final private int VK_RWIN   = 92;
    final private int VK_TAB    = 9;
    final private int LLKHF_ALTDOWN = 0x20;
    final private int VK_DELETE = 0x2E;
    final private int VK_ESCAPE = 0x1B;
	final private int VK_CONTROL = 17;

    public static HotKeys getIstance() {
        if (INSTANCE == null) {
            INSTANCE = new HotKeys();
        }

        return INSTANCE;
    }

    private HotKeys() {

        hookproc = new LowLevelKeyboardProc() {
 
            public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT keyInfo) {

                LRESULT result;
                
                if ( (keyInfo.vkCode == VK_LWIN) || (keyInfo.vkCode == VK_RWIN) ||
                        ( (keyInfo.vkCode == VK_TAB)&&((keyInfo.flags & LLKHF_ALTDOWN) != 0) ) ||
                        ( (keyInfo.vkCode == VK_ESCAPE) && ((keyInfo.flags & LLKHF_ALTDOWN) != 0)) ||
                        ( (keyInfo.vkCode == VK_ESCAPE) && KeyboardUtils.isPressed(KeyEvent.VK_CONTROL)) ||
                        ( (keyInfo.vkCode == VK_ESCAPE) && KeyboardUtils.isPressed(KeyEvent.VK_CONTROL) && KeyboardUtils.isPressed(KeyEvent.VK_SHIFT)) ||
						( (keyInfo.vkCode == VK_DELETE) && ((keyInfo.flags & LLKHF_ALTDOWN) != 0) && KeyboardUtils.isPressed(KeyEvent.VK_CONTROL)) 
                    ) 
                {
                    System.out.println("C=" + keyInfo.vkCode + " f=" + keyInfo.flags);
                    return new LRESULT(1);
                }

                return new LRESULT(0);
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

    public void run() {
        User32.MSG msg = new User32.MSG();

        while (!quit) { 
            if (filter)
                User32.INSTANCE.PeekMessage(msg, null, 0, 0, 0);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void quit() {
        quit = true;
    }

    public void filter(boolean h) {
        filter = h;
    }

    public void finalize() {
        quit();
        UnHook();
    }

 
    public static void main(String[] args) {
        HotKeys hotkey = HotKeys.getIstance();

        hotkey.run();
        
        hotkey.finalize();
    } 
}
