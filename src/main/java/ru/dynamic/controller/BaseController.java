package ru.dynamic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.dynamic.service.BaseService;
import ru.dynamic.utils.CommonUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class BaseController {

    @Autowired
    private BaseService baseService;

    @Autowired
    private CommonUtils commonUtils;


    @RequestMapping(value = {"/BaseController.getData/{dataSourceId}"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public byte[] getData(
            @PathVariable(value="dataSourceId") String dataSourceId,
            @RequestBody String parameters
    ) throws Exception {
        Map<String, Object> params = commonUtils.jsonToMap(parameters);
        return ("{\"value\":\"" + baseService.getData(dataSourceId, params) + "\"}").getBytes(StandardCharsets.UTF_8.name());
    }
}
