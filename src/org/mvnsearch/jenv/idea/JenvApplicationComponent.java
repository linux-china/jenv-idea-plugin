package org.mvnsearch.jenv.idea;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.SystemUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * jenv application component
 *
 * @author linux_china
 */
public class JenvApplicationComponent implements ApplicationComponent {
    /**
     * construct method
     */
    public JenvApplicationComponent() {
    }

    /**
     * init component, setup sdk automatically
     */
    public void initComponent() {
        File jenvHome = new File(new File(System.getProperty("user.home")), ".jenv");
        if (SystemUtils.IS_OS_WINDOWS) {
            jenvHome = new File("c:/jenv");
        }
        if (jenvHome.exists()) {
            File javaHome = new File(jenvHome, "candidates/java");
            if (javaHome.exists()) {
                File[] jdkVersions = javaHome.listFiles();
                if (jdkVersions != null) {
                    List<String> installedVersions = new ArrayList<String>();
                    List<Sdk> javaSdks = ProjectJdkTable.getInstance().getSdksOfType(JavaSdk.getInstance());
                    for (Sdk javaSdk : javaSdks) {
                        installedVersions.add(javaSdk.getName());
                    }
                    for (final File javaVersion : jdkVersions) {
                        String version = javaVersion.getName();
                        if (!version.equals("current") && !installedVersions.contains(version)) {
                            VirtualFile sdkHome = ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
                                @Override
                                public VirtualFile compute() {
                                    return LocalFileSystem.getInstance().refreshAndFindFileByPath(javaVersion.getAbsolutePath());
                                }
                            });
                            if (sdkHome != null) {
                                final Sdk newSdk = SdkConfigurationUtil.setupSdk(new Sdk[]{}, sdkHome, JavaSdk.getInstance(), true, null, version);
                                if (newSdk != null) {
                                    SdkConfigurationUtil.addSdk(newSdk);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void disposeComponent() {

    }

    @NotNull
    public String getComponentName() {
        return "JenvApplicationComponent";
    }
}
