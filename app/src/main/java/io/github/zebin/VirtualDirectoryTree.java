package io.github.zebin;

import io.github.zebin.javabash.sandbox.DirectoryTree;
import io.github.zebin.javabash.sandbox.PosixPath;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.stream.Stream;

public class VirtualDirectoryTree implements DirectoryTree {
    public static final PosixPath WORKDIR_LEVEL_CONF = PosixPath.ofPosix("os/user/workdir");
    public static final PosixPath USER_LEVEL_CONF = PosixPath.ofPosix("os/user");
    public static final PosixPath OS_LEVEL_CONF = PosixPath.ofPosix("os");

    private final Map<PosixPath, DirectoryTree> levels;

    /**
     * levels = [ user, local, runtime ]
     * -> user
     * local -> local
     * local/runtime -> runtime
     */
    public VirtualDirectoryTree(DirectoryTree user, DirectoryTree local, DirectoryTree runtime) {
        this.levels = Map.of(
                OS_LEVEL_CONF, user,
                USER_LEVEL_CONF, local,
                WORKDIR_LEVEL_CONF, runtime);
    }

    @Override
    public Writer put(PosixPath posixPath) {
        if (posixPath.startsWith(WORKDIR_LEVEL_CONF)) {
            return levels.get(WORKDIR_LEVEL_CONF).put(posixPath.relativize(WORKDIR_LEVEL_CONF));
        } else if (posixPath.startsWith(USER_LEVEL_CONF)) {
            return levels.get(USER_LEVEL_CONF).put(posixPath.relativize(USER_LEVEL_CONF));
        } else if (posixPath.startsWith(OS_LEVEL_CONF)) {
            return levels.get(OS_LEVEL_CONF).put(posixPath.relativize(OS_LEVEL_CONF));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }

    @Override
    public boolean delete(PosixPath posixPath) {
        if (posixPath.startsWith(WORKDIR_LEVEL_CONF)) {
            return levels.get(WORKDIR_LEVEL_CONF).delete(posixPath.relativize(WORKDIR_LEVEL_CONF));
        } else if (posixPath.startsWith(USER_LEVEL_CONF)) {
            return levels.get(USER_LEVEL_CONF).delete(posixPath.relativize(USER_LEVEL_CONF));
        } else if (posixPath.startsWith(OS_LEVEL_CONF)) {
            return levels.get(OS_LEVEL_CONF).delete(posixPath.relativize(OS_LEVEL_CONF));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }

    @Override
    public Writer patch(PosixPath posixPath) {
        if (posixPath.startsWith(WORKDIR_LEVEL_CONF)) {
            return levels.get(WORKDIR_LEVEL_CONF).patch(posixPath.relativize(WORKDIR_LEVEL_CONF));
        } else if (posixPath.startsWith(USER_LEVEL_CONF)) {
            return levels.get(USER_LEVEL_CONF).patch(posixPath.relativize(USER_LEVEL_CONF));
        } else if (posixPath.startsWith(OS_LEVEL_CONF)) {
            return levels.get(OS_LEVEL_CONF).patch(posixPath.relativize(OS_LEVEL_CONF));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }

    @Override
    public Reader get(PosixPath posixPath) {
        if (posixPath.startsWith(WORKDIR_LEVEL_CONF)) {
            return levels.get(WORKDIR_LEVEL_CONF).get(posixPath.relativize(WORKDIR_LEVEL_CONF));
        } else if (posixPath.startsWith(USER_LEVEL_CONF)) {
            return levels.get(USER_LEVEL_CONF).get(posixPath.relativize(USER_LEVEL_CONF));
        } else if (posixPath.startsWith(OS_LEVEL_CONF)) {
            return levels.get(OS_LEVEL_CONF).get(posixPath.relativize(OS_LEVEL_CONF));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }

    @Override
    public boolean exists(PosixPath posixPath) {
        if (posixPath.startsWith(WORKDIR_LEVEL_CONF)) {
            return levels.get(WORKDIR_LEVEL_CONF).exists(posixPath.relativize(WORKDIR_LEVEL_CONF));
        } else if (posixPath.startsWith(USER_LEVEL_CONF)) {
            return levels.get(USER_LEVEL_CONF).exists(posixPath.relativize(USER_LEVEL_CONF));
        } else if (posixPath.startsWith(OS_LEVEL_CONF)) {
            return levels.get(OS_LEVEL_CONF).exists(posixPath.relativize(OS_LEVEL_CONF));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }

    @Override
    public boolean isDir(PosixPath posixPath) {
        if (posixPath.startsWith(WORKDIR_LEVEL_CONF)) {
            return levels.get(WORKDIR_LEVEL_CONF).isDir(posixPath.relativize(WORKDIR_LEVEL_CONF));
        } else if (posixPath.startsWith(USER_LEVEL_CONF)) {
            return levels.get(USER_LEVEL_CONF).isDir(posixPath.relativize(USER_LEVEL_CONF));
        } else if (posixPath.startsWith(OS_LEVEL_CONF)) {
            return levels.get(OS_LEVEL_CONF).isDir(posixPath.relativize(OS_LEVEL_CONF));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }

    @Override
    public Stream<PosixPath> list(PosixPath posixPath) {
        if (posixPath.startsWith(WORKDIR_LEVEL_CONF)) {
            return levels.get(WORKDIR_LEVEL_CONF).list(posixPath.relativize(WORKDIR_LEVEL_CONF));
        } else if (posixPath.startsWith(USER_LEVEL_CONF)) {
            return levels.get(USER_LEVEL_CONF).list(posixPath.relativize(USER_LEVEL_CONF));
        } else if (posixPath.startsWith(OS_LEVEL_CONF)) {
            return levels.get(OS_LEVEL_CONF).list(posixPath.relativize(OS_LEVEL_CONF));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }
}
