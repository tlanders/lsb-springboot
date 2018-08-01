package biz.lci.learningspringboot.lsbweb;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class ImageService {
    private static final String UPLOAD_ROOT = "./upload";

    private final ResourceLoader resourceLoader;

    public ImageService(ResourceLoader loader) {
        resourceLoader = loader;
    }

    public Flux<Image> findAllImages() {
        try {
            return Flux.fromIterable(Files.newDirectoryStream(Paths.get(UPLOAD_ROOT)))
                        .map(path -> new Image(path.hashCode(), path.getFileName().toString()));
        } catch(IOException ioe) {
            return Flux.empty();
        }
    }

    public Mono<Resource> findOneImage(String name) {
        return Mono.fromSupplier(() -> resourceLoader.getResource("file:" + UPLOAD_ROOT + "/" + name));
    }

    public Mono<Void> createImage(Flux<FilePart> files) {
        return files.flatMap(file -> file.transferTo(Paths.get(UPLOAD_ROOT, file.filename().toString()).toFile())).then();
    }

    public Mono<Void> deleteImage(String name) {
        return Mono.fromRunnable(() -> {
            try {
                Files.deleteIfExists(Paths.get(UPLOAD_ROOT, name));
            } catch (IOException ioe) {
                // can ignore
            }
        });
    }

    @Bean
    CommandLineRunner setUp() throws IOException {
        return (args) -> {
            FileSystemUtils.deleteRecursively(new File(UPLOAD_ROOT));

            try {
                Files.createDirectory(Paths.get(UPLOAD_ROOT));
            } catch(FileAlreadyExistsException ex) {
                // can ignore this
            }

            FileCopyUtils.copy("Seed Image 1", new FileWriter(UPLOAD_ROOT + "/seed1.jpg"));
            FileCopyUtils.copy("Seed Image 2", new FileWriter(UPLOAD_ROOT + "/seed2.jpg"));
            FileCopyUtils.copy("Seed Image 3", new FileWriter(UPLOAD_ROOT + "/seed3.jpg"));
        };
    }
}
