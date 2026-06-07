
@echo test message for console

@rem echo test message from application >> AppConsoleExample.log

@echo off
echo Start pause...
@rem timeout /t 5 /nobreak
ping -n 6 127.0.0.1 > nul
@rem ping -n 100 127.0.0.1 > nul
echo Pause is done.