package biz.lci.learningspringboot.lsbweb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.UUID;

@Service
public class ImageService {
    private static final Logger log = LoggerFactory.getLogger(ImageService.class);
    private static final String UPLOAD_ROOT = "./upload";

    private final ImageRepository imageRepository;
    private final ResourceLoader resourceLoader;

    public ImageService(ResourceLoader loader, ImageRepository imgRepo) {
        resourceLoader = loader;
        imageRepository = imgRepo;
    }

    public Flux<Image> findAllImages() {
        return imageRepository.findAll();
//        try {
//            return Flux.fromIterable(Files.newDirectoryStream(Paths.get(UPLOAD_ROOT)))
//                        .map(path -> new Image(path.hashCode(), path.getFileName().toString()));
//        } catch(IOException ioe) {
//            return Flux.empty();
//        }
    }

    public Mono<Resource> findOneImage(String name) {
//        return imageRepository.findByName(name);
        return Mono.fromSupplier(() -> resourceLoader.getResource("file:" + UPLOAD_ROOT + "/" + name));
    }

    public Mono<Void> createImage(Flux<FilePart> files) {
        return files.flatMap(file -> {
            Mono<Image> saveDbImage = imageRepository.save(
                    new Image(UUID.randomUUID().toString(), file.filename()));

            Mono<Void> copyFile = Mono.just(Paths.get(UPLOAD_ROOT, file.filename()).toFile())
                    .log("createImage-pickTarget")
                    .map(destFile -> {
                        try {
                            destFile.createNewFile();
                            return destFile;
                        } catch(IOException ioe) {
                            throw new RuntimeException(ioe);
                        }
                    })
                    .log("createImage-newFile")
                    .flatMap(file::transferTo)
                    .log("createImage-copy");

            return Mono.when(saveDbImage, copyFile);
        })
        .then();
//        return files.flatMap(file -> {
//            if(file.filename().length() <= 0) {
//                // no file specified
//                log.warn("Invalid file - image not created");
//                return Mono.empty();
//            } else {
//                return file.transferTo(Paths.get(UPLOAD_ROOT, file.filename()).toFile());
//            }
//        }).then();
    }

    public Mono<Void> deleteImage(String name) {
        Mono<Void> deleteDbImage = imageRepository.findByName(name)
                .flatMap(imageRepository::delete);

        Mono<Void> deleteFile = Mono.fromRunnable(() -> {
            try {
                Files.deleteIfExists(Paths.get(UPLOAD_ROOT, name));
            } catch(IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });

        return Mono.when(deleteDbImage, deleteFile)
                .then();
//        return Mono.fromRunnable(() -> {
//            try {
//                Files.deleteIfExists(Paths.get(UPLOAD_ROOT, name));
//            } catch (IOException ioe) {
//                throw new RuntimeException(ioe);
//            }
//        });
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
