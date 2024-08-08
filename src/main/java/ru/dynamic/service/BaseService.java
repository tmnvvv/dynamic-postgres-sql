package ru.dynamic.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dynamic.dto.DataDescription;
import ru.dynamic.dto.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BaseService {

    @Autowired
    protected DataExtractorService extractor;

    @Autowired
    private ConfigurationLoaderService queryComponent;

    public Object getData(String dataSourceId, Map<String, Object> params) throws Exception {
        DataDescription description = null;
        try {
            description = getDescription(dataSourceId);
            try {
                if (description.getSource() != null) {
                    val result = extractor.getData(description
                            , prepareParams(params, description, null)
                            , null
                    ).getResult();
                    return result;
                }
            } catch (Exception e) {
                throw e;
            }
            log.error("Datasource " + dataSourceId + " has no value or result settings.", new Exception("Datasource " + dataSourceId + " has no value or result settings."));
            throw new Exception("Datasource " + dataSourceId + " has no value or result settings.");
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
    protected DataDescription getDescription(String dataSourceId) {
        return queryComponent.get(dataSourceId);
    }

    protected Map<String, Object> prepareParams(Map<String, Object> params,
                                                DataDescription description,
                                                String operationName) {

        Map<String, Object> result = new HashMap<>();
        List<RequestParam> paramsSettings = operationName == null || description.getOperations() == null || description.getOperations().get(operationName) == null
                ? (description.getSource() == null ? new ArrayList<>() : description.getSource().getParams())
                : (description.getOperations().get(operationName).getParams());

        for (RequestParam param: paramsSettings) {
            if (param.getRequestName() != null ) {
                Object val = extractParameter(params, param.getRequestName());
                result.put(param.getRequestName(), val);
            }
        }
       return result;
    }

    public Object extractParameter(Map<String, Object> params, String name) {
        String[] nameParts = StringUtils.split(name, '.');
        Object extractedValue = params.get(nameParts[0]);
        if (extractedValue != null && extractedValue instanceof Map && nameParts.length > 1) {
            val newNameParts = new String[nameParts.length - 1];
            for (int i = 1; i < nameParts.length; i++) {
                newNameParts[i - 1] = nameParts[i];
            }
            extractedValue = extractParameter((Map<String, Object>)extractedValue, StringUtils.join(newNameParts, '.'));
        }
        return extractedValue;
    }
}
