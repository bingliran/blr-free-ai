package com.blr19c.blr.free.ai.utils.application;

import com.blr19c.blr.free.ai.BlrFreeAiApplication;
import org.springframework.boot.system.ApplicationHome;

import java.nio.file.Path;

/**
 * Application工具
 */
public class ApplicationUtils {

    /**
     * 获取安装路径
     */
    public static Path getLocalDir() {
        Path path = new ApplicationHome(BlrFreeAiApplication.class).getDir().toPath();
        if (path.normalize().toString().endsWith("/build/classes/java/main")) {
            return path.getParent().getParent().getParent().getParent();
        }
        return path;
    }
}
