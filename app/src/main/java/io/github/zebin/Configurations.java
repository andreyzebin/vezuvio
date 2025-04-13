package io.github.zebin;

import io.github.andreyzebin.gitSql.config.ConfigTree;
import io.github.zebin.javabash.process.TextTerminal;
import io.github.zebin.javabash.sandbox.FileManager;
import io.github.zebin.javabash.sandbox.PosixPath;
import io.github.zebin.javabash.sandbox.WorkingDirectory;

public class Configurations {

    public static final PosixPath VESUVIO_HOME = PosixPath.ofPosix(".vezuvio");
    public static final PosixPath VESUVIO_HOME_CONF = VESUVIO_HOME.climb("conf");
    private final PosixPath wd;
    private final TextTerminal term;
    private final FileManager fm;

    public Configurations(PosixPath wd, TextTerminal term) {
        this.wd = wd;
        this.term = term;
        this.fm = new FileManager(term);
    }

    public PosixPath getWorkDir() {
        return wd;
    }

    public PosixPath getUserHomeDir() {
        return PosixPath.ofPosix("~");
    }

    public PosixPath getVezuvioLocalHome() {
        return getWorkDir().climb(VESUVIO_HOME);
    }

    public PosixPath getVezuvioUserHome() {
        return getUserHomeDir().climb(VESUVIO_HOME);
    }

    public ConfigTree getConf() {
        return new ConfigTree(new VirtualDirectoryTree(
                new WorkingDirectory(fm, getUserHomeDir().climb(VESUVIO_HOME_CONF), e -> {
                }),
                new WorkingDirectory(fm, getWorkDir().climb(VESUVIO_HOME_CONF), e -> {
                }),
                new WorkingDirectory(fm, getWorkDir().climb(VESUVIO_HOME_CONF), e -> {
                })));
    }

    public TextTerminal getTerm() {
        return term;
    }
}
