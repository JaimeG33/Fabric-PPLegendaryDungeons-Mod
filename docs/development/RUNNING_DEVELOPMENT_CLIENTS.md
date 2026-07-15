# Running Fabric and NeoForge Development Clients

## PowerShell commands

Run these commands from the repository root:

```powershell
cd "D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons"
```

### Fabric client

```powershell
.\gradlew.bat :fabric:runClient `
    --no-parallel `
    --max-workers=2 `
    --console=plain
```

### NeoForge client

```powershell
.\gradlew.bat :neoforge:runClient `
    --no-parallel `
    --max-workers=2 `
    --console=plain
```

### Optional dedicated-server commands

Fabric:

```powershell
.\gradlew.bat :fabric:runServer `
    --no-parallel `
    --max-workers=2 `
    --console=plain
```

NeoForge:

```powershell
.\gradlew.bat :neoforge:runServer `
    --no-parallel `
    --max-workers=2 `
    --console=plain
```

## IntelliJ one-click run configurations

Create two permanent Gradle configurations so the top toolbar dropdown can
switch between Fabric and NeoForge.

### Fabric Client configuration

1. Open **Run → Edit Configurations**.
2. Click **+** and choose **Gradle**.
3. Set **Name** to:

   ```text
   Fabric Client
   ```

4. Set **Gradle project** to the repository root project:

   ```text
   cobblemon-explore-legendary-dungeons
   ```

   Use the root project, not only the `fabric` module.

5. Set **Run** or **Tasks and arguments** to:

   ```text
   :fabric:runClient --no-parallel --max-workers=2 --console=plain
   ```

6. Click **Apply**.

### NeoForge Client configuration

1. In the same window, click **+** and choose **Gradle**.
2. Set **Name** to:

   ```text
   NeoForge Client
   ```

3. Set **Gradle project** to the same repository root project.
4. Set **Run** or **Tasks and arguments** to:

   ```text
   :neoforge:runClient --no-parallel --max-workers=2 --console=plain
   ```

5. Click **Apply**, then **OK**.

## Using the toolbar

The run-configuration dropdown at the top of IntelliJ should now contain:

```text
Fabric Client
NeoForge Client
```

Select the desired loader and click the green **Run** triangle.

Useful shortcuts:

```text
Run selected configuration: Shift+F10
Choose a configuration: Alt+Shift+F10
Edit configurations: Alt+Shift+F10, then 0
```

## Debugging

The green **Run** button is the simplest choice for normal gameplay testing.

The bug-shaped **Debug** button can be used when testing breakpoints. If
IntelliJ starts Gradle in debug mode but does not attach cleanly to the launched
Minecraft process, open the Gradle configuration, choose **Modify options**, and
enable the option for debugging forked Gradle tasks in separate debug tabs.

Loom-generated Minecraft client configurations may also appear automatically.
They can be renamed to `Fabric Client (Loom)` and `NeoForge Client (Loom)` if
their module names make the loader clear. Remove or ignore older ambiguous
temporary configurations.

## IntelliJ Gradle settings

Open:

```text
File → Settings → Build, Execution, Deployment → Build Tools → Gradle
```

Recommended values:

```text
Use Gradle from: gradle-wrapper.properties
Gradle JVM: Java 21
```

After changing Gradle files or dependencies, use **Reload All Gradle Projects**
from the Gradle tool window before launching another client.

## Optional project-file storage

In **Edit Configurations**, IntelliJ may show **Store as project file**.

Enable it only when the run configurations should be saved inside the project
and shared through Git. Otherwise, leave it disabled and IntelliJ will keep them
as local IDE settings.
