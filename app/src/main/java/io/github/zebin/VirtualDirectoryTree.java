package io.github.zebin;

import io.github.zebin.javabash.sandbox.DirectoryTree;
import io.github.zebin.javabash.sandbox.PosixPath;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.stream.Stream;

public class VirtualDirectoryTree implements DirectoryTree {
    public static final PosixPath RUNTIME = PosixPath.ofPosix("local/runtime");
    public static final PosixPath LOCAL = PosixPath.ofPosix("local");
    public static final PosixPath RELATIVE_ROOT = PosixPath.ofPosix("");
    public static final PosixPath USER = RELATIVE_ROOT;

    private final Map<PosixPath, DirectoryTree> levels;

    /**
     * levels = [ user, local, runtime ]
     * -> user
     * local -> local
     * local/runtime -> runtime
     */
    public VirtualDirectoryTree(DirectoryTree user, DirectoryTree local, DirectoryTree runtime) {
        this.levels = Map.of(
                USER, user,
                LOCAL, local,
                RUNTIME, runtime);
    }

    @Override
    public Writer put(PosixPath posixPath) {
        if (posixPath.startsWith(RUNTIME)) {
            return levels.get(RUNTIME).put(posixPath.relativize(RUNTIME));
        } else if (posixPath.startsWith(LOCAL)) {
            return levels.get(LOCAL).put(posixPath.relativize(LOCAL));
        } else if (posixPath.startsWith(USER)) {
            return levels.get(USER).put(posixPath.relativize(USER));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }

    @Override
    public boolean delete(PosixPath posixPath) {
        if (posixPath.startsWith(RUNTIME)) {
            return levels.get(RUNTIME).delete(posixPath.relativize(RUNTIME));
        } else if (posixPath.startsWith(LOCAL)) {
            return levels.get(LOCAL).delete(posixPath.relativize(LOCAL));
        } else if (posixPath.startsWith(USER)) {
            return levels.get(USER).delete(posixPath.relativize(USER));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }

    @Override
    public Writer patch(PosixPath posixPath) {
        if (posixPath.startsWith(RUNTIME)) {
            return levels.get(RUNTIME).patch(posixPath.relativize(RUNTIME));
        } else if (posixPath.startsWith(LOCAL)) {
            return levels.get(LOCAL).patch(posixPath.relativize(LOCAL));
        } else if (posixPath.startsWith(USER)) {
            return levels.get(USER).patch(posixPath.relativize(USER));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }

    @Override
    public Reader get(PosixPath posixPath) {
        if (posixPath.startsWith(RUNTIME)) {
            return levels.get(RUNTIME).get(posixPath.relativize(RUNTIME));
        } else if (posixPath.startsWith(LOCAL)) {
            return levels.get(LOCAL).get(posixPath.relativize(LOCAL));
        } else if (posixPath.startsWith(USER)) {
            return levels.get(USER).get(posixPath.relativize(USER));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }

    @Override
    public boolean exists(PosixPath posixPath) {
        if (posixPath.startsWith(RUNTIME)) {
            return levels.get(RUNTIME).exists(posixPath.relativize(RUNTIME));
        } else if (posixPath.startsWith(LOCAL)) {
            return levels.get(LOCAL).exists(posixPath.relativize(LOCAL));
        } else if (posixPath.startsWith(USER)) {
            return levels.get(USER).exists(posixPath.relativize(USER));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }

    @Override
    public boolean isDir(PosixPath posixPath) {
        if (posixPath.startsWith(RUNTIME)) {
            return levels.get(RUNTIME).isDir(posixPath.relativize(RUNTIME));
        } else if (posixPath.startsWith(LOCAL)) {
            return levels.get(LOCAL).isDir(posixPath.relativize(LOCAL));
        } else if (posixPath.startsWith(USER)) {
            return levels.get(USER).isDir(posixPath.relativize(USER));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }

    @Override
    public Stream<PosixPath> list(PosixPath posixPath) {
        if (posixPath.startsWith(RUNTIME)) {
            return levels.get(RUNTIME).list(posixPath.relativize(RUNTIME));
        } else if (posixPath.startsWith(LOCAL)) {
            return levels.get(LOCAL).list(posixPath.relativize(LOCAL));
        } else if (posixPath.startsWith(USER)) {
            return levels.get(USER).list(posixPath.relativize(USER));
        }

        throw new IllegalArgumentException("Wrong path: " + posixPath);
    }
}
