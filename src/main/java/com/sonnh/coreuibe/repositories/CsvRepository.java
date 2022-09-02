package com.sonnh.coreuibe.repositories;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Transactional
@Slf4j
public class CsvRepository {

    @PersistenceContext
    private EntityManager entityManager;

    final ApplicationContext applicationContext;

    public CsvRepository(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void createTableDynamic(String tableName, Map<String, String> columnMap) throws Exception {
        if (StringUtils.isEmpty(tableName) || columnMap == null) {
            throw new Exception("Invalid Param");
        }

        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(String.format("DROP TABLE IF EXISTS \"%s\" ;", tableName));
        stringBuilder.append(String.format("CREATE TABLE \"%s\"", tableName));
        stringBuilder.append(" (");
        stringBuilder.append(" id serial PRIMARY KEY");
        columnMap.forEach((columnName, columnDisplayName) -> {
            stringBuilder.append(String.format(", \"%s\" TEXT ", columnName));
        });
        stringBuilder.append(" );");

        Query query = entityManager.createNativeQuery(stringBuilder.toString());
        query.executeUpdate();
    }

    public void saveColumnsMeta(String taleName, Map<String, String> columnMap) throws Exception {
        columnMap.forEach((columnName, columnDisplayName) -> {
            var isColumnExist = checkColumnExist(taleName, columnName);
            StringBuilder stringBuilder = new StringBuilder();
            if (isColumnExist) {
                stringBuilder.append("UPDATE \"TableMeta\"");
                stringBuilder.append(" SET");
                stringBuilder.append(String.format(" column_display_name = '%s'", columnDisplayName));
                stringBuilder.append(String.format(" ,updated_date = '%s'", new Date()));
                stringBuilder.append(" WHERE");
                stringBuilder.append(String.format(" table_name = '%s'", taleName));
                stringBuilder.append(String.format(" AND column_name = '%s'", columnName));
                entityManager.createNativeQuery(stringBuilder.toString()).executeUpdate();
            } else {
                stringBuilder.append("INSERT INTO \"TableMeta\" (table_name, column_name, column_display_name, created_date, updated_date)");
                stringBuilder.append("VALUES ( ?1, ?2, ?3, ?4, ?5 );");
                Query query = entityManager.createNativeQuery(stringBuilder.toString());
                query.setParameter(1, taleName);
                query.setParameter(2, columnName);
                query.setParameter(3, columnDisplayName);
                query.setParameter(4, new Date());
                query.setParameter(5, new Date());
                query.executeUpdate();
            }
        });
    }

    public boolean checkColumnExist(String tableName, String columnName) {
        boolean result = true;
        var queryStr = "SELECT count(id) FROM \"TableMeta\" WHERE table_name = ?1 AND column_name = ?2";
        Query nativeQuery = entityManager.createNativeQuery(queryStr);
        applicationContext.getBeansOfType(Query.class);
        nativeQuery.setParameter(1, tableName);
        nativeQuery.setParameter(2, columnName);
        var count = (BigInteger) nativeQuery.getResultList().get(0);
        if (count.intValue() == 0) {
            result = false;
        }

        return result;
    }

    public void saveCsvRows(String tableName, List<String[]> rows, Set<String> columnNames) throws Exception {
        var columnsStr = columnNames.stream()
                                    .map(columName -> "\"" + columName + "\"")
                                    .collect(Collectors.joining(","));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("INSERT INTO \"%s\" (%s) VALUES", tableName, columnsStr));
        for (String[] row : rows) {
            var valuesStr = Arrays.stream(row)
                                  .toList()
                                  .stream()
                                  .map(value -> "'" + value + "'")
                                  .collect(Collectors.joining(","));
            stringBuilder.append(String.format(" (%s),", valuesStr));
        }
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        entityManager.createNativeQuery(stringBuilder.toString()).executeUpdate();
    }

    public boolean checkIfTableExist(String tableName) {
        var result = true;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT count(*) FROM information_schema.tables");
        stringBuilder.append(String.format(" WHERE table_name = '%s'", tableName));
        stringBuilder.append(" LIMIT 1;");
        var count = (BigInteger) entityManager.createNativeQuery(stringBuilder.toString()).getResultList().get(0);
        if (count.intValue() == 0) {
            result = false;
        }
        return result;
    }
}
//todo luon them updated date khi update
//todo implement log
