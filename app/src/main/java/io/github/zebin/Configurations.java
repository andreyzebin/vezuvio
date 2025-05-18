package io.github.zebin;

import io.github.andreyzebin.gitSql.cache.DirectoryTreeCacheProxy;
import io.github.andreyzebin.gitSql.config.ConfigTree;
import io.github.zebin.javabash.process.TextTerminal;
import io.github.zebin.javabash.sandbox.AllFileManager;
import io.github.zebin.javabash.sandbox.FileManager;
import io.github.zebin.javabash.sandbox.PosixPath;
import io.github.zebin.javabash.sandbox.WorkingDirectory;

import java.util.concurrent.atomic.AtomicReference;

public class Configurations {

    public static final PosixPath VESUVIO_HOME = PosixPath.ofPosix(".vezuvio");
    public static final PosixPath VESUVIO_HOME_CONF = VESUVIO_HOME.climb("conf");
    private final PosixPath wd;
    private final TextTerminal term;
    private final AllFileManager fm;
    private final PosixPath configLevel;
    private ConfigTree ct;

    public Configurations(PosixPath wd, TextTerminal term, PosixPath level) {
        this.wd = wd;
        this.term = term;
        this.fm = new FileManager(term);
        this.configLevel = level;
    }

    public Configurations(PosixPath wd, TextTerminal term, PosixPath level, AllFileManager fm) {
        this.wd = wd;
        this.term = term;
        this.fm = fm;
        this.configLevel = level;
    }

    public PosixPath getWorkDir() {
        return wd;
    }

    public PosixPath getConfLevel() {
        return configLevel;
    }

    public PosixPath getVezuvioHome() {
        if (configLevel == VirtualDirectoryTree.USER_LEVEL_CONF) {
            return getVezuvioUserHome();
        } else if (configLevel == VirtualDirectoryTree.WORKDIR_LEVEL_CONF) {
            return getVezuvioWorkDirHome();
        }

        return getVezuvioLocalHome();
    }

    public PosixPath getUserHomeDir() {
        return PosixPath.ofPosix("~");
    }

    public PosixPath getLocalDir() {
        return PosixPath.ofPosix("/opt");
    }

    public PosixPath getVezuvioWorkDirHome() {
        return getWorkDir().climb(VESUVIO_HOME);
    }

    public PosixPath getVezuvioUserHome() {
        return getUserHomeDir().climb(VESUVIO_HOME);
    }

    public PosixPath getVezuvioLocalHome() {
        return getLocalDir().climb(VESUVIO_HOME);
    }

    public ConfigTree getConf() {
        if (ct == null) {
            ct = new ConfigTree(
                    DirectoryTreeCacheProxy.cachedProxy(
                            new VirtualDirectoryTree(
                                    // TODO add OS level
                                    getDirYree(getUserHomeDir()),
                                    getDirYree(getUserHomeDir()),
                                    getDirYree(getWorkDir())
                            ),
                            new AtomicReference<>("ff")
                    )
            );
        }

        return ct;
    }

    private DirectoryTreeCacheProxy getDirYree(PosixPath userHomeDir) {
        return DirectoryTreeCacheProxy.cachedProxy(
                new WorkingDirectory(fm, userHomeDir.climb(VESUVIO_HOME_CONF), e -> {
                }),
                new AtomicReference<>("kk")
        );
    }

    public TextTerminal getTerm() {
        return term;
    }
}
