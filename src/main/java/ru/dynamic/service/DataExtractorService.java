package ru.dynamic.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.dynamic.dto.DataDescription;
import ru.dynamic.dto.DescriptionSource;
import ru.dynamic.dto.RequestParam;
import ru.dynamic.dto.ResultData;
import ru.dynamic.utils.CommonUtils;
import ru.dynamic.utils.CustomStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Repository
public class DataExtractorService {

    @Autowired
    @Qualifier("postgres")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommonUtils commonUtils;

    public ResultData getData(DataDescription description, Map<String, Object> parameters, String operationName) throws Exception {
        ResultData resultData = null;
        val source = operationName == null ? description.getSource() : description.getOperations().get(operationName);

        if (source.getResultData() != null) {
            resultData = getPostgresData(description, source, parameters);
        }
        return resultData;
    }


    private ResultData getPostgresData(DataDescription description, DescriptionSource source, Map<String, Object> parameters) throws Exception {
        CustomStatement stmt = null;
        ForkJoinPool forkJoinPool = null;
        ResultSet rs = null;

        try {
            stmt = getStatement(source, parameters);

            val newDt = startPostgresQuery(description, source, stmt);

            val resultData = new ResultData();

            if (source.getResultData() != null) {
                List<Map<String, Object>> result = new ArrayList<>();
                rs = stmt.getResultSet();

                if (rs != null) {
                    while (rs.next()) {

                        ResultSet finalRs = rs;

                        forkJoinPool = new ForkJoinPool(4);

                        Map<String, Object> item = forkJoinPool.submit(() -> Optional.ofNullable(source.getResultData())
                                .stream()
                                .parallel()
                                .flatMap(e -> e.stream())
                                .map(e -> e.getSource())
                                .collect(HashMap<String, Object>::new, (m, e) -> {
                                    try {
                                        m.put(e, finalRs.getObject(e));
                                    } catch (SQLException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }, HashMap::putAll)).get();

                        result.add(item);
                    }
                }
                log.debug("Result data for object '" + description.getId() + "', query '" + source.getPostgresQuery() + "' got in '" + (new Date().getTime() - newDt) + "' ms.");
                resultData.setResult(result);
            }
            return resultData;
        } catch (Exception e) {
            throw e;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (forkJoinPool != null) {
                forkJoinPool.shutdown();
            }
        }
    }

    private CustomStatement getStatement(DescriptionSource source, Map<String, Object> parameters) throws Exception {
        val stmt = new CustomStatement(jdbcTemplate, source.getPostgresQuery());

        for (val parameterSettings : source.getParams()) {
            if (parameterSettings != null) {
                Object value = parameters.get(getParameterKey(parameterSettings));
                int postgresType = commonUtils.getPostgresTypeByStringType(parameterSettings.getType());
                stmt.addParameter(value, postgresType);
            }
        }
        return stmt;
    }

    private Long startPostgresQuery(DataDescription description, DescriptionSource source, CustomStatement stmt) throws SQLException {
        val dt = new Date();
        log.debug("Starting executing query for object '" + description.getId() + "', query '" + source.getPostgresQuery() + "'.");
        stmt.execute();
        val newDt = new Date().getTime();
        log.debug("Query for object '" + description.getId() + "', query '" + source.getPostgresQuery() + "' executed in '" + (newDt - dt.getTime()) + "' ms.");
        return newDt;
    }

    protected Object getParameterKey(RequestParam parameter) {
        return parameter.getRequestName();
    }
}
