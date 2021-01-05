package me.bed0.jWynn.util;

import org.apache.commons.io.FileUtils;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

// todo: use lazy reading with java 8 streams instead of wrapping the blocking readFileToString call
// todo related to^:  make write operation reactive
public class ReactiveFileUtils {

    public static Mono<String> readFileToString(File file, Charset charset) {
        return Mono.create(sink -> {
            try {
                sink.success(FileUtils.readFileToString(file, charset));
            } catch (IOException e) {
                sink.error(e);
            }
        });
    }

    public static Mono<Void> writeStringToFile(File file, String string, Charset charset) {
        return Mono.create(sink -> {
            try {
                FileUtils.writeStringToFile(file, string, charset);
                sink.success();
            } catch (IOException e) {
                sink.error(e);
            }
        });
    }

}
