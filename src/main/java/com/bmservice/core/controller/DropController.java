package com.bmservice.core.controller;

import com.bmservice.core.BmServiceCoreConstants;
import com.bmservice.core.exception.BmServiceCoreException;
import com.bmservice.core.service.DropService;
import com.bmservice.core.helper.DropsHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;

@RestController
@RequestMapping(BmServiceCoreConstants.API_VERSION)
@AllArgsConstructor
public class DropController {

    private final DropService dropService;

    private final DropsHelper dropsHelper;

    @PostMapping("/drops")
    public void drop() throws Exception {
        File dropFile = Paths.get("src/test/resources/drop1.json").toFile();
        //var dropFile = new File(dropFilePath);
        if(!dropFile.exists()) {
            throw new BmServiceCoreException("No Drop File Found !");
        }
       var drop = dropsHelper.parseDropFile(FileUtils.readFileToString(dropFile, Charset.defaultCharset()));
        // dropService.send(drop);
    }


}
