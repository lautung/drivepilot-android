param(
    [string]$Prototype = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$prototypePath = if ($Prototype) {
    Join-Path $root $Prototype
} else {
    (Get-ChildItem -LiteralPath (Join-Path $root "doc") -Filter "*_v8.html" | Select-Object -First 1).FullName
}
if (-not $prototypePath) { throw "Prototype HTML was not found." }
$drawableDir = Join-Path $root "app/src/main/res/drawable"
$imageDir = Join-Path $root "app/src/main/res/drawable-nodpi"
New-Item -ItemType Directory -Force -Path $drawableDir, $imageDir | Out-Null

$utf8 = New-Object System.Text.UTF8Encoding($false)
$html = [System.IO.File]::ReadAllText($prototypePath, $utf8)

$images = [ordered]@{
    "vehicle_sedan.jpg" = "https://modao.cc/agent-py/media/generated_images/2026-02-09/dbea3e3709af4980a2a484ef91915c49.jpg"
    "road_scene.jpg" = "https://modao.cc/agent-py/media/generated_images/2026-02-09/ebb473e9673f4194abbf16bd77d3ee6c.jpg"
    "vehicle_configurator.jpg" = "https://modao.cc/agent-py/media/generated_images/2026-02-09/b8116cad5c304a2bb5d231de6b8adc47.jpg"
    "wheel_standard.jpg" = "https://modao.cc/agent-py/media/generated_images/2026-02-09/0273164f89bb4ab096800bde6072d63e.jpg"
    "wheel_performance.jpg" = "https://modao.cc/agent-py/media/generated_images/2026-02-09/49de325e37f4455d9a86fb9d34b0de41.jpg"
    "service_avatar.jpg" = "https://modao.cc/agent-py/media/generated_images/2026-02-09/4a9a8166c04c4901947506cdc026db78.jpg"
    "sentry_front.jpg" = "https://modao.cc/agent-py/media/generated_images/2026-02-09/59f9648549604985bf6c3a56c0c99469.jpg"
    "sentry_left.jpg" = "https://modao.cc/agent-py/media/generated_images/2026-02-09/f4f93e8fa185408a9bb38abb4c9d8322.jpg"
    "sentry_right.jpg" = "https://modao.cc/agent-py/media/generated_images/2026-02-09/7fe4992f4b884181b5ea91a55c730fcd.jpg"
    "digital_key_avatar.png" = "https://modao.cc/agent-py/media/user_assets/2026-02-09/ea00e306505349aea4358b6af4dea5e7.png"
    "profile_avatar.png" = "https://modao.cc/agent-py/media/user_assets/2026-02-09/84ed80cf1d8948fc9d64be0cd9f681c2.png"
}

foreach ($entry in $images.GetEnumerator()) {
    $destination = Join-Path $imageDir $entry.Key
    if (-not (Test-Path -LiteralPath $destination)) {
        Invoke-WebRequest -Uri $entry.Value -OutFile $destination -UseBasicParsing
    }
}

function Convert-IconName([string]$iconName) {
    return "ic_" + (($iconName -replace ":", "_") -replace "-", "_") + ".xml"
}

function Escape-Xml([string]$value) {
    return [System.Security.SecurityElement]::Escape($value)
}

$seen = @{}
$svgMatches = [regex]::Matches($html, '<svg\b[^>]*data-icon="([^"]+)"[^>]*>[\s\S]*?</svg>')
foreach ($svgMatch in $svgMatches) {
    $iconName = $svgMatch.Groups[1].Value
    if ($seen.ContainsKey($iconName)) { continue }
    $seen[$iconName] = $true

    $svg = $svgMatch.Value
    $vectorPaths = New-Object System.Collections.Generic.List[string]
    $shapeMatches = [regex]::Matches($svg, '<(path|circle)\b[^>]*>')
    foreach ($shapeMatch in $shapeMatches) {
        $shape = $shapeMatch.Value
        if ($shapeMatch.Groups[1].Value -eq "path") {
            $d = [regex]::Match($shape, '\bd="([^"]+)"').Groups[1].Value
            if (-not $d) { continue }
        } else {
            $cx = [double]([regex]::Match($shape, '\bcx="([^"]+)"').Groups[1].Value)
            $cy = [double]([regex]::Match($shape, '\bcy="([^"]+)"').Groups[1].Value)
            $r = [double]([regex]::Match($shape, '\br="([^"]+)"').Groups[1].Value)
            $left = $cx - $r
            $diameter = 2 * $r
            $d = "M $left,$cy a $r,$r 0 1,0 $diameter,0 a $r,$r 0 1,0 -$diameter,0"
        }

        $fillType = if ($shape -match '(fill-rule|clip-rule)="evenodd"' -or $svg -match '<g[^>]+(fill-rule|clip-rule)="evenodd"') { ' android:fillType="evenOdd"' } else { '' }
        $vectorPaths.Add(('    <path android:fillColor="#FF000000" android:pathData="{0}"{1} />' -f (Escape-Xml $d), $fillType))
    }

    $xml = @(
        '<?xml version="1.0" encoding="utf-8"?>'
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"'
        '    android:width="24dp"'
        '    android:height="24dp"'
        '    android:viewportWidth="24"'
        '    android:viewportHeight="24">'
        $vectorPaths
        '</vector>'
    ) -join "`n"

    $output = Join-Path $drawableDir (Convert-IconName $iconName)
    [System.IO.File]::WriteAllText($output, $xml + "`n", $utf8)
}

# The prototype references solar:megaphone-bold, which no longer exists in the
# Solar collection and therefore has no inline SVG. Use Solar's current bold
# volume glyph for the intended broadcast action while keeping the same family.
if (-not $seen.ContainsKey("solar:megaphone-bold")) {
    $fallbackPath = "M5.003 11.716c.038-1.843.057-2.764.678-3.552c.113-.144.28-.315.42-.431c.763-.636 1.771-.636 3.788-.636c.72 0 1.081 0 1.425-.092q.107-.03.211-.067c.336-.121.637-.33 1.238-.746c2.374-1.645 3.56-2.467 4.557-2.11c.191.069.376.168.541.29c.861.635.927 2.115 1.058 5.073C18.967 10.541 19 11.48 19 12s-.033 1.46-.081 2.555c-.131 2.958-.197 4.438-1.058 5.073a2.2 2.2 0 0 1-.54.29c-.997.357-2.184-.465-4.558-2.11c-.601-.416-.902-.625-1.238-.746a3 3 0 0 0-.211-.067c-.344-.092-.704-.092-1.425-.092c-2.017 0-3.025 0-3.789-.636a3 3 0 0 1-.419-.43c-.621-.79-.64-1.71-.678-3.552a14 14 0 0 1 0-.57"
    $fallbackXml = @(
        '<?xml version="1.0" encoding="utf-8"?>'
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"'
        '    android:width="24dp"'
        '    android:height="24dp"'
        '    android:viewportWidth="24"'
        '    android:viewportHeight="24">'
        ('    <path android:fillColor="#FF000000" android:pathData="{0}" />' -f $fallbackPath)
        '</vector>'
    ) -join "`n"
    [System.IO.File]::WriteAllText((Join-Path $drawableDir "ic_solar_megaphone_bold.xml"), $fallbackXml + "`n", $utf8)
    $seen["solar:megaphone-bold"] = $true
}

Write-Output "Imported $($images.Count) images and $($seen.Count) Solar icons."
