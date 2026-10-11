# Packages the ElyPrism instance zip.
# Expects: mod/build/libs/blamegen-*.jar (from the RFG build) and instance-pkg/ (staged).
$ErrorActionPreference = 'Stop'
$root = 'C:\Users\Admin\Documents\Projeto Padrão'
$stage = Join-Path $root 'instance-pkg'
$mods  = Join-Path $stage 'minecraft\mods'

# 1. Locate the freshly built mod jar.
$jar = Get-ChildItem (Join-Path $root 'mod\build\libs') -Filter '*.jar' -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notmatch 'sources|javadoc|dev|shadow' } |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $jar) { throw 'No built mod jar found in mod/build/libs. Run the gradle build first.' }
Write-Host "using mod jar: $($jar.Name) ($($jar.Length) bytes)"

$target = Join-Path $mods 'blamegen-1.0.0.jar'
Copy-Item $jar.FullName $target -Force
Write-Host "copied to $target"

# 2. Sanity-check the jar: it must contain the core classes and the adapter, and its
#    mcmod.info must not contain unexpanded ${} placeholders.
Add-Type -AssemblyName System.IO.Compression.FileSystem
$z = [System.IO.Compression.ZipFile]::OpenRead($target)
try {
    $names = $z.Entries | ForEach-Object { $_.FullName }
    $need = @(
        'blame/blamegen/core/Megastructure.class',
        'blame/blamegen/core/Arrival.class',
        'blame/blamegen/core/Cube.class',
        'blame/blamegen/core/Mat.class',
        'blame/blamegen/BlameCubeGenerator.class',
        'blame/blamegen/BlameWorldType.class',
        'blame/blamegen/BlameGen.class'
    )
    foreach ($n in $need) {
        if ($names -notcontains $n) { throw "mod jar is missing $n" }
    }
    Write-Host "jar contents OK ($($names.Count) entries)"

    $info = $z.GetEntry('mcmod.info')
    if ($info) {
        $sr = New-Object System.IO.StreamReader($info.Open())
        $txt = $sr.ReadToEnd(); $sr.Close()
        if ($txt -match '\$\{[^}]+\}') { throw "mcmod.info still has placeholders:`n$txt" }
        Write-Host "mcmod.info OK"
    } else {
        Write-Host 'WARNING: mcmod.info not found in jar'
    }
} finally {
    $z.Dispose()
}

# 3. CubicChunks jar must be present and untouched.
$cc = Join-Path $mods 'CubicChunks-1.12.2-0.0.1301.0-SNAPSHOT-all.jar'
if (-not (Test-Path $cc)) { throw "CubicChunks jar missing: $cc" }
$h = (Get-FileHash $cc -Algorithm SHA1).Hash.ToLower()
Write-Host "CubicChunks sha1: $h"

# 4. Verify the instance root layout required by Prism/ElyPrism import.
foreach ($f in @('instance.cfg', 'mmc-pack.json', 'blame.png')) {
    $p = Join-Path $stage $f
    if (-not (Test-Path $p)) { throw "missing instance root file: $f" }
}
$mc = Join-Path $stage 'minecraft'
if (-not (Test-Path $mc)) { throw 'missing minecraft/ directory' }
Write-Host 'instance layout OK'

# 5. Report final tree, then zip.
Get-ChildItem $stage -Recurse -File | ForEach-Object {
    '{0,10}  {1}' -f $_.Length, $_.FullName.Replace("$stage\", '')
}
$out = Join-Path $root 'BLAME_Megastructure_Instance.zip'
if (Test-Path $out) { Remove-Item $out -Force }
[System.IO.Compression.ZipFile]::CreateFromDirectory($stage, $out, [System.IO.Compression.CompressionLevel]::Optimal, $false)
Write-Host ""
Write-Host "WROTE $out ($((Get-Item $out).Length) bytes)"
