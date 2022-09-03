package com.sonnh.coreuibe.repositories;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    public void createTableDynamic(String tableName, List<String> columnNames) throws Exception {
        if (StringUtils.isEmpty(tableName) || CollectionUtils.isEmpty(columnNames)) {
            throw new Exception("Invalid Param");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("CREATE TABLE \"%s\"", tableName));
        stringBuilder.append(" (");
        stringBuilder.append(" id serial PRIMARY KEY");
        columnNames.forEach((columnName) -> {
            stringBuilder.append(String.format(", \"%s\" TEXT ", columnName));
        });
        stringBuilder.append(" );");

        entityManager.createNativeQuery(stringBuilder.toString()).executeUpdate();
    }

    public void saveColumnsMeta(String taleName, List<String> columnNames, List<String> columnDisplayNames) throws Exception {
        if (StringUtils.isEmpty(taleName) || CollectionUtils.isEmpty(columnNames) || CollectionUtils.isEmpty(columnDisplayNames)) {
            throw new Exception("##saveColumnsMeta##: Invalid params ");
        }

        for (int i = 0; i < columnNames.size(); i++) {
            var isColumnExist = checkColumnExist(taleName, columnNames.get(i));
            StringBuilder stringBuilder = new StringBuilder();
            if (isColumnExist) {
                stringBuilder.append("UPDATE \"TableMeta\"");
                stringBuilder.append(" SET");
                stringBuilder.append(String.format(" column_display_name = '%s'", columnDisplayNames.get(i)));
                stringBuilder.append(String.format(" ,updated_date = '%s'", new Date()));
                stringBuilder.append(" WHERE");
                stringBuilder.append(String.format(" table_name = '%s'", taleName));
                stringBuilder.append(String.format(" AND column_name = '%s'", columnNames.get(i)));
                entityManager.createNativeQuery(stringBuilder.toString()).executeUpdate();
            } else {
                stringBuilder.append("INSERT INTO \"TableMeta\" (table_name, column_name, column_display_name, created_date, updated_date)");
                stringBuilder.append("VALUES ( ?1, ?2, ?3, ?4, ?5 );");
                Query query = entityManager.createNativeQuery(stringBuilder.toString());
                query.setParameter(1, taleName);
                query.setParameter(2, columnNames.get(i));
                query.setParameter(3, columnDisplayNames.get(i));
                query.setParameter(4, new Date());
                query.setParameter(5, new Date());
                query.executeUpdate();
            }
        }
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

    public void saveOrUpdateCsvRow(String tableName, List<String> keys, Map<String, String> rowMap) throws Exception {
        if (ObjectUtils.anyNull(tableName, keys, rowMap)) {
            throw new Exception("##saveCsvRows##: Invalid params");
        }

        if (checkIfRowExist(tableName, keys, rowMap)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("UPDATE TABLE \"%s\"", tableName));
            stringBuilder.append(" SET");
            rowMap.forEach((k, v) -> {
                stringBuilder.append(String.format("%s = '%s'", k, v));
            });
            stringBuilder.append(" WHERE");
            for (int i = 0; i < keys.size(); i++) {
                var key = keys.get(i);
                if (i == (keys.size() - 1)) {
                    stringBuilder.append(String.format("%s = '%s'", key, rowMap.get(key)));
                } else {
                    stringBuilder.append(String.format("%s = '%s'", key, rowMap.get(key)));
                    stringBuilder.append(" AND");
                }
            }
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            var columnsStr = String.join(",", rowMap.keySet());
            var rowString = rowMap.values().stream().map(r -> "'" + r + "'").collect(Collectors.joining(","));
            stringBuilder.append(String.format("INSERT INTO \"%s\" (%s) VALUES", tableName, columnsStr));
            stringBuilder.append(String.format(" (%s)", rowString));
            entityManager.createNativeQuery(stringBuilder.toString()).executeUpdate();
        }
    }

    public boolean checkIsTableExist(String tableName) {
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

    public boolean checkIfRowExist(String tableName, List<String> keys, Map<String, String> rowMap) throws Exception {
        if (ObjectUtils.anyNull(tableName, keys, rowMap)) {
            throw new Exception("Invalid params");
        }
        log.info(String.format("checkIfRowExist: %s, %s ,%s", tableName, Arrays.toString(keys.toArray()), rowMap));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("SELECT count(*) FROM \"%s\"", tableName));
        stringBuilder.append(" WHERE");
        keys.forEach(k -> {
            var columnName = k.toLowerCase().replace(" ", "_");
            stringBuilder.append(String.format(" %s = '%s'", columnName, rowMap.get(columnName)));
        });
        var count = (BigInteger) entityManager.createNativeQuery(stringBuilder.toString()).getSingleResult();
        return count.intValue() > 0;
    }
}

//todo luon them updated date khi update
//todo implement log
