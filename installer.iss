[Setup]
AppName=NutriEnergia Pro
AppVersion=1.0
DefaultDirName={userappdata}\NutriEnergia Pro
DefaultGroupName=NutriEnergia Pro
UninstallDisplayIcon={app}\NutriEnergiaPro.exe
SetupIconFile=food-and-drink.ico
Compression=lzma2/ultra64
SolidCompression=yes
OutputDir=dist
OutputBaseFilename=NutriEnergiaPro_Instalador
PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
Source: "dist\NutriEnergiaPro\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\NutriEnergia Pro"; Filename: "{app}\NutriEnergiaPro.exe"
Name: "{autodesktop}\NutriEnergia Pro"; Filename: "{app}\NutriEnergiaPro.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\NutriEnergiaPro.exe"; Description: "{cm:LaunchProgram,NutriEnergia Pro}"; Flags: nowait postinstall skipifsilent
