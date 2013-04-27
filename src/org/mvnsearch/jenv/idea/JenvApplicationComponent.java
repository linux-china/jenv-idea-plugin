package org.mvnsearch.jenv.idea;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
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
     * jenv home
     */
    public File JENV_HOME;

    /**
     * construct method
     */
    public JenvApplicationComponent() {

    }

    /**
     * Fetches the instance of JenvApplicationComponent implementation.
     */
    public static JenvApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(JenvApplicationComponent.class);
    }

    /**
     * get candidate home
     *
     * @param candidate canidate name
     * @param version   version
     * @return candidate home
     */
    public File getCandidateHome(String candidate, String version) {
        return new File(JENV_HOME, "candidates/" + candidate + "/" + version);
    }

    /**
     * init component, setup sdk automatically
     */
    public void initComponent() {
        //associate jenvrc with properties file type
        registerJenvrc();
        //setup java sdk
        JENV_HOME = new File(new File(System.getProperty("user.home")), ".jenv");
        if (SystemUtils.IS_OS_WINDOWS) {
            JENV_HOME = new File("c:/jenv");
        }
        if (JENV_HOME.exists()) {
            File javaHome = new File(JENV_HOME, "candidates/java");
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

    /**
     * register jenvrc pattern for properties file
     */
    private void registerJenvrc() {
        boolean registered = false;
        FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        FileType propertiesFileType = fileTypeManager.getFileTypeByExtension(".properties");
        List<FileNameMatcher> associations = fileTypeManager.getAssociations(propertiesFileType);
        for (FileNameMatcher association : associations) {
            if (association.getPresentableString().equals("jenvrc")) {
                registered = true;
                break;
            }
        }
        if (!registered) {
            fileTypeManager.associatePattern(propertiesFileType, "jenvrc");
        }
    }

    public void disposeComponent() {

    }

    @NotNull
    public String getComponentName() {
        return "JenvApplicationComponent";
    }
}
