# Builds a zip with forward-slash entry names (required by the zip spec),
# unlike .NET's CreateFromDirectory which emits backslashes on Windows.
param(
    [Parameter(Mandatory=$true)][string]$Source,
    [Parameter(Mandatory=$true)][string]$Out
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$srcFull = (Resolve-Path $Source).Path.TrimEnd('\')
$root = (Resolve-Path $Source).Path.TrimEnd('\') + '\'

$files = Get-ChildItem -Path $srcFull -Recurse -File | Where-Object { $_.FullName -and $_.Length -ne $null }
Write-Host "packaging $($files.Count) files from $srcFull"

if (Test-Path $Out) { Remove-Item $Out -Force }
$fs = New-Object System.IO.FileStream($Out, [System.IO.FileMode]::Create)
$zip = New-Object System.IO.Compression.ZipArchive($fs, [System.IO.Compression.ZipArchiveMode]::Create)

foreach ($f in $files) {
    $rel = $f.FullName.Substring($root.Length).Replace('\', '/')
    $entry = $zip.CreateEntry($rel, [System.IO.Compression.CompressionLevel]::Optimal)
    $in = New-Object System.IO.FileStream($f.FullName, [System.IO.FileMode]::Open, [System.IO.FileAccess]::Read)
    $os = $entry.Open()
    $in.CopyTo($os)
    $os.Close(); $in.Close()
}
$zip.Dispose()
$fs.Close()
Write-Host "wrote $Out ($((Get-Item $Out).Length) bytes)"
