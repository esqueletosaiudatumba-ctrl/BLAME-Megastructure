# Re-runs the whole offline verification suite and refreshes docs/verification/.
# Pure Java, no Minecraft. Run from the repo root.
$ErrorActionPreference = 'Stop'
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path "$here\..\.." | Select-Object -ExpandProperty Path
Push-Location $root
try {
    $javac = "C:\Users\Admin\AppData\Roaming\ElyPrismLauncher\java\java-runtime-epsilon\bin\javac.exe"
    $java  = "C:\Users\Admin\AppData\Roaming\ElyPrismLauncher\java\java-runtime-epsilon\bin\java.exe"
    if (-not (Test-Path $javac)) {
        $javac = (Get-Command javac -ErrorAction SilentlyContinue).Source
        $java  = (Get-Command java  -ErrorAction SilentlyContinue).Source
    }
    if (-not $javac) { throw "no javac found; set JAVA_HOME or pass a JDK path" }

    $out = Join-Path $root "build\pv"
    Remove-Item $out -Recurse -Force -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force -Path $out | Out-Null

    $src = @()
    $src += Get-ChildItem "$root\mod\src\main\java\blame\blamegen\core" -Filter *.java | ForEach-Object { $_.FullName }
    $src += Get-ChildItem "$root\tools\verify" -Filter *.java | ForEach-Object { $_.FullName }
    $src += "$root\tools\preview\Preview.java"
    Write-Host "compiling $($src.Count) sources..."
    & $javac -nowarn -d $out @src
    if ($LASTEXITCODE -ne 0) { throw "compilation failed" }
    Write-Host "compile OK"

    $v = Join-Path $root "docs\verification"
    New-Item -ItemType Directory -Force -Path $v | Out-Null

    & $java -cp $out blame.blamegen.core.SeamCheck     2>&1 | Out-File "$v\seam-and-containment.txt" -Encoding utf8
    & $java -cp $out blame.blamegen.core.Probe        2>&1 | Out-File "$v\determinism-and-census.txt" -Encoding utf8
    & $java -cp $out blame.blamegen.core.Survey       2>&1 | Out-File "$v\coarse-cell-survey.txt" -Encoding utf8
    & $java -cp $out blame.blamegen.core.LandmarkSurvey 2>&1 | Out-File "$v\landmark-survey.txt" -Encoding utf8
    & $java -cp $out Preview bench 3000               2>&1 | Out-File "$v\bench.txt" -Encoding utf8

    Write-Host "--- seam check:"
    Get-Content "$v\seam-and-containment.txt" | Select-Object -Last 2
    Write-Host "--- determinism:"
    Get-Content "$v\determinism-and-census.txt" | Select-Object -First 1
    Write-Host "--- bench:"
    Get-Content "$v\bench.txt"
    Write-Host "wrote docs/verification/"
} finally {
    Pop-Location
}
