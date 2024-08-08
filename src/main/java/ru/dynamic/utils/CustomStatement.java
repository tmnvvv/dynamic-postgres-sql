package ru.dynamic.utils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CustomStatement {

    private Connection conn;
    private String sql;
    private List<CustomStatementParameter> parameters;
    private PreparedStatement stmt;
    private ResultSet resultSet;


    public CustomStatement(JdbcTemplate jdbcTemplate, String sql) throws Exception {
        this(jdbcTemplate, sql, new ArrayList());
    }


    public CustomStatement(JdbcTemplate jdbcTemplate, String sql, List<CustomStatementParameter> parameters) throws Exception {
        if (jdbcTemplate == null) {
            throw new Exception("JDBC Template is null!");
        } else {
            this.conn = jdbcTemplate.getDataSource().getConnection();
            this.sql = sql;
            this.parameters = parameters;
        }
    }

    private void setParameters(PreparedStatement  stmt, List<CustomStatementParameter> parameters) throws SQLException {
        int index = 1;
        for (val param: parameters) {
            if (param.getValue() == null) {
                setNull(stmt, param.getType(), index);
            } else if (param.getType() == Types.INTEGER) {
                stmt.setInt(index, Integer.parseInt(String.class.cast(param.getValue())));
            } else if (param.getType() == Types.VARCHAR) {
                stmt.setString(index, String.class.cast(param.getValue()));
            }
            index++;
        }
    }

    private void setNull(PreparedStatement stmt, int type, int index) throws SQLException {
        stmt.setNull(index, type);
    }

    public void execute() throws SQLException {
        this.stmt = this.conn.prepareCall(this.sql);
        this.setParameters(this.stmt, this.parameters);
        this.stmt.execute();
    }

    public void addParameter(Object value, int type) {
        this.parameters.add(new CustomStatementParameter(value, type));
    }


    public ResultSet getResultSet() throws Exception {
        if (this.stmt == null) {
            throw new Exception("ERROR: Call result execute!");
        } else {
            return this.resultSet = this.stmt.executeQuery();
        }
    }

    public void close() throws SQLException {
        if (this.resultSet != null) {
            this.resultSet.close();
        }

        if (this.stmt != null) {
            this.stmt.close();
        }

        if (this.conn != null) {
            this.conn.close();
        }
    }
}
