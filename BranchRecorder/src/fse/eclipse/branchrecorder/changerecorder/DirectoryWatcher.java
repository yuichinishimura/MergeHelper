package fse.eclipse.branchrecorder.changerecorder;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

public class DirectoryWatcher implements Runnable {

    private final OperationCopier opCopier;
    private final String watchPath;

    private DirectoryWatcher(String watchPath) {
        this.watchPath = watchPath;
        opCopier = new OperationCopier(watchPath);
    }

    public static void start(String watchDirPath) throws IOException {
        File file = new File(watchDirPath);
        if (!file.exists()) {
            FileUtils.forceMkdir(file);
        }

        new Thread(new DirectoryWatcher(watchDirPath)).start();
    }

    /**
     * ファイルが追加されるのを監視する 追加された場合，Historyファイルのコピー処理を行う
     */
    @Override
    public void run() {
        Path path = Paths.get(watchPath);
        FileSystem fileSystem = FileSystems.getDefault();
        try {
            WatchService watchService = fileSystem.newWatchService();
            // ファイル追加を監視
            WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            watch(watchService, watchKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 監視時の処理
     * @param watchService 監視パス
     * @param watchKey 監視イベント
     */
    private void watch(WatchService watchService, WatchKey watchKey) {
        while (watchKey.isValid()) {
            try {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                WatchKey detecedtWatchKey = watchService.poll(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

                if (detecedtWatchKey.equals(watchKey)) {
                    for (WatchEvent<?> event : detecedtWatchKey.pollEvents()) {
                        opCopier.copy((Path) event.context());
                    }
                }
                detecedtWatchKey.reset();

            } catch (InterruptedException ex) {
                watchKey.cancel();
            }
        }
    }
}