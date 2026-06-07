$ErrorActionPreference = "Stop"
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$RootDir = Join-Path $PSScriptRoot ".." | Resolve-Path

Push-Location $RootDir
.\gradlew.bat :app:assembleDebug @args
$code = $LASTEXITCODE
Pop-Location
exit $code
