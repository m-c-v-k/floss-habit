# One-time setup: creates the release signing key and stores it as GitHub secrets so CI
# can publish APKs that install as updates over each other.
#
# Run it yourself from the repo root:  powershell -ExecutionPolicy Bypass -File scripts\setup-signing.ps1
#
# The keystore is kept OUTSIDE the repo. Back it up (e.g. password manager): if it is lost,
# installed apps can no longer be updated and a Play Store listing could not be updated either.

$ErrorActionPreference = 'Stop'
$gh = 'C:\Program Files\GitHub CLI\gh.exe'
$keyDir = Join-Path $HOME '.android-keys'
$keystore = Join-Path $keyDir 'floss_habit.jks'
$alias = 'flosshabit'

$keytool = (Get-Command keytool -ErrorAction SilentlyContinue).Source
if (-not $keytool) { $keytool = Join-Path $env:JAVA_HOME 'bin\keytool.exe' }
if (-not (Test-Path $keytool)) { throw 'keytool not found. Set JAVA_HOME to a JDK.' }

if (Test-Path $keystore) {
    Write-Host "Keystore already exists at $keystore; reusing it."
} else {
    New-Item -ItemType Directory -Force $keyDir | Out-Null
}

$secure = Read-Host 'Choose a keystore password (min 6 chars)' -AsSecureString
$password = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure))
if ($password.Length -lt 6) { throw 'Password must be at least 6 characters.' }

if (-not (Test-Path $keystore)) {
    & $keytool -genkeypair -v -keystore $keystore -alias $alias -keyalg RSA -keysize 4096 `
        -validity 10000 -storepass $password -keypass $password `
        -dname 'CN=floss-habit, O=mcvk'
    if ($LASTEXITCODE -ne 0) { throw 'keytool failed.' }
}

$base64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($keystore))
$base64   | & $gh secret set SIGNING_KEYSTORE_BASE64
$password | & $gh secret set SIGNING_STORE_PASSWORD
$alias    | & $gh secret set SIGNING_KEY_ALIAS
$password | & $gh secret set SIGNING_KEY_PASSWORD

Write-Host ''
Write-Host "Done. Keystore: $keystore"
Write-Host 'Back up this file and the password somewhere safe.'
