package helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class DropsHelperTest {


    File dropFile = Paths.get("src/test/resources/drop1.json").toFile();

    @BeforeEach
    void setUp() {
    }

    @Test
    void parseDropFile() throws IOException {
        System.out.println(dropFile.getAbsolutePath());
        var content = DropsHelper.parseDropFile(FileUtils.readFileToString(new File(dropFile.getAbsolutePath()), Charset.defaultCharset()));
        System.out.println(content);
    }

    @Test
    void getAllRandomTags() {
    }
}