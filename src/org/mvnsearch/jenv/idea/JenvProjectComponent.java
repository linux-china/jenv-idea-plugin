package org.mvnsearch.jenv.idea;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;
import java.util.Properties;

/**
 * jenv project component
 *
 * @author linux_china
 */
public class JenvProjectComponent implements ProjectComponent {
    /**
     * project
     */
    private Project project;

    public JenvProjectComponent(Project project) {
        this.project = project;
    }

    public void initComponent() {

    }

    public void disposeComponent() {

    }

    @NotNull
    public String getComponentName() {
        return "JenvProjectComponent";
    }

    /**
     * auto setup project sdk according to jenvrc
     */
    public void projectOpened() {
        VirtualFile projectBaseDir = project.getBaseDir();
        VirtualFile jenvrcFile = projectBaseDir.findChild("jenvrc");
        if (jenvrcFile != null && jenvrcFile.exists()) {
            Properties properties = new Properties();
            try {
                properties.load(jenvrcFile.getInputStream());
            } catch (Exception ignore) {

            }
            //java
            if (properties.containsKey("java")) {
                String javaVersion = properties.getProperty("java");
                Sdk jdk = ProjectJdkTable.getInstance().findJdk(javaVersion);
                if (jdk != null) {
                    SdkConfigurationUtil.setDirectoryProjectSdk(project, jdk);
                }
            }
            //maven
            if (properties.containsKey("maven")) {
                String mavenVersion = properties.getProperty("maven");
                if (JenvApplicationComponent.JENV_HOME.exists()) {
                    File mavenHome = new File(JenvApplicationComponent.JENV_HOME, "candidates/maven/" + mavenVersion);
                    if (mavenHome.exists()) {
                        MavenGeneralSettings generalSettings = MavenProjectsManager.getInstance(project).getGeneralSettings();
                        generalSettings.setMavenHome(mavenHome.getAbsolutePath());
                        //generalSettings.setUserSettingsFile(mavenHome.getAbsolutePath() + "/conf/settings.xml");
                    }
                }
            }
        }
    }

    public void projectClosed() {

    }
}
