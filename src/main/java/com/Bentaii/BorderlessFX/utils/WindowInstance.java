package com.Bentaii.BorderlessFX.utils;

import com.sun.glass.ui.Window;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;

/**
 * Class that provides methods to get the window handle of a JavaFX window.
 */
public class WindowInstance
{
    public final HWND _hwnd;
    public final User32 _user32;
    public final int _oldStyle;

    public WindowInstance()
    {
        var lhwnd = Window.getWindows().get(0).getNativeWindow();
        var lpVoid = new Pointer(lhwnd);
        _hwnd = new HWND(lpVoid);
        _user32 = User32.INSTANCE;
        _oldStyle = _user32.GetWindowLong(_hwnd, WinUser.GWL_STYLE);
    }
}
