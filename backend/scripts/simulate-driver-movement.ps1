param(
    [Parameter(Mandatory = $true)]
    [string]$Token,
    [Parameter(Mandatory = $true)]
    [Guid]$DriverId,
    [Parameter(Mandatory = $true)]
    [double]$DestinationLatitude,
    [Parameter(Mandatory = $true)]
    [double]$DestinationLongitude,
    [string]$BaseUrl = "http://localhost:8080",
    [ValidateRange(1, 200)]
    [int]$Steps = 20,
    [ValidateRange(0, 60)]
    [int]$IntervalSeconds = 2
)

$restaurantLatitude = -12.0919738
$restaurantLongitude = -76.9737017
$headers = @{ Authorization = "Bearer $Token" }

for ($step = 0; $step -le $Steps; $step++) {
    $progress = $step / $Steps
    $latitude = $restaurantLatitude + (($DestinationLatitude - $restaurantLatitude) * $progress)
    $longitude = $restaurantLongitude + (($DestinationLongitude - $restaurantLongitude) * $progress)
    $body = @{
        latitude = $latitude
        longitude = $longitude
    } | ConvertTo-Json

    Invoke-RestMethod `
        -Method Patch `
        -Uri "$BaseUrl/api/v1/drivers/$DriverId/location" `
        -Headers $headers `
        -ContentType "application/json" `
        -Body $body | Out-Null

    Write-Host ("Paso {0}/{1}: {2:F7}, {3:F7}" -f $step, $Steps, $latitude, $longitude)
    if ($step -lt $Steps -and $IntervalSeconds -gt 0) {
        Start-Sleep -Seconds $IntervalSeconds
    }
}
