' launcher.vbs
' Launches launcher.bat hidden / minimized to system tray
Set WshShell = CreateObject("WScript.Shell")

' 0 = hidden, 1 = normal, 2 = minimized, 3 = maximized
' We'll use 0 to completely hide the CMD window
WshShell.Run Chr(34) & "launcher.bat" & Chr(34), 0, False
