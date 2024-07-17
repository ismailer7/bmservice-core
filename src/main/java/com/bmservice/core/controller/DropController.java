package com.bmservice.core.controller;

import com.bmservice.core.BmServiceCoreConstants;
import com.bmservice.core.component.DropComponent;
import com.bmservice.core.exception.BmServiceCoreException;
import com.bmservice.core.service.DropService;
import com.bmservice.core.helper.DropsHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

@RestController
@RequestMapping(BmServiceCoreConstants.API_VERSION)
@AllArgsConstructor
@Slf4j
public class DropController {

    private final DropService dropService;

    private final DropsHelper dropsHelper;

    @PostMapping("/drops")
    public void drop() {
        File dropFile = Paths.get("src/test/resources/drop1.json").toFile();
        DropComponent drop = null;
        String dropFileContent = null;
        if(!dropFile.exists()) {
            throw new BmServiceCoreException("No Drop File Found !");
        }
        try {
            dropFileContent = FileUtils.readFileToString(dropFile, Charset.defaultCharset());
        } catch (IOException e) {throw new BmServiceCoreException(e.getMessage());}
        drop = dropsHelper.parseDropFile(dropFileContent);
        dropService.send(drop);

        //if(dropFile.exists()) dropFile.delete();
    }

    @ExceptionHandler(BmServiceCoreException.class)
    public void handleBmServiceCoreException(BmServiceCoreException e) {
        log.error(e.getMessage());
    }
}
