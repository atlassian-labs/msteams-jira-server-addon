param
(
    [Parameter(Mandatory=$True)]
    [string]$filePath,
    [Parameter(Mandatory=$True)]
    [string]$clientId,
    [Parameter(Mandatory=$True)]
    [string]$pluginKey,
    [Parameter(Mandatory=$True)]
    [string]$webServiceAddress,
    [Parameter(Mandatory=$True)]
    [string]$pluginVersion,
    [Parameter(Mandatory=$True)]
    [string]$teamsAppBaseUrl
)

try
{
   $reader = [System.IO.StreamReader] $filePath
   $data = $reader.ReadToEnd()
   $reader.close()
}
finally
{
   if ($reader -ne $null)
   {
       $reader.dispose()
   }
}

$data = $data -replace "aud_claim=.*","aud_claim=$clientId`r`n"
$data = $data -replace "plugin_key=.*","plugin_key=$pluginKey`r`n"
$data = $data -replace "signalr_hub_url=.*","signalr_hub_url=$webServiceAddress`r`n"
$data = $data -replace "plugin_version=.*","plugin_version=$pluginVersion`r`n"
$data = $data -replace "teams_app_base_url=.*","teams_app_base_url=$teamsAppBaseUrl`r`n"

try
{
   $writer = [System.IO.StreamWriter] $filePath
   $writer.write($data)
   $writer.close()
}
finally
{
   if ($writer -ne $null)
   {
       $writer.dispose()
   }
}