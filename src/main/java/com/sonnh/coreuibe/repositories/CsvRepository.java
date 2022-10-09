package com.sonnh.coreuibe.repositories;

import com.sonnh.coreuibe.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    public void createTableDynamic(String tableName, List<String> headers) throws Exception {
        if (StringUtils.isEmpty(tableName) || CollectionUtils.isEmpty(headers)) {
            throw new Exception("Invalid Param");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("CREATE TABLE \"%s\"", tableName));
        stringBuilder.append(" (");
        stringBuilder.append(" id serial PRIMARY KEY");
        headers.forEach(header -> {
            stringBuilder.append(String.format(", \"%s\" TEXT ", CommonUtils.convertToPostgresColumnName(header)));
        });
        stringBuilder.append(" ,\"created_date\" date");
        stringBuilder.append(" ,\"updated_date\" date");
        stringBuilder.append(" );");

        entityManager.createNativeQuery(stringBuilder.toString()).executeUpdate();
    }

    public void upsertColumnMeta(String taleName, String header, List<String> keys) throws Exception {
        if (StringUtils.isEmpty(taleName) || StringUtils.isEmpty(header)) {
            throw new Exception("##saveColumnsMeta##: Invalid params ");
        }
        log.info("##saveColumnsMeta {}, {}", taleName, header);

        String columnName = header.toLowerCase().replace(" ", "_");
        var isColumnExist = checkColumnExist(taleName, columnName);
        StringBuilder stringBuilder = new StringBuilder();
        if (isColumnExist) {
            stringBuilder.append("UPDATE \"table_meta\"");
            stringBuilder.append(" SET");
            stringBuilder.append(String.format(" column_display_name = '%s'", header));
            stringBuilder.append(String.format(" ,column_name = '%s'", columnName));
            stringBuilder.append(String.format(" ,\"updated_date\" = '%s'", new Date()));
            stringBuilder.append(" WHERE");
            stringBuilder.append(String.format(" table_name = '%s'", taleName));
            stringBuilder.append(String.format(" AND column_name = '%s'", columnName));
            entityManager.createNativeQuery(stringBuilder.toString()).executeUpdate();
        } else {
            stringBuilder.append("INSERT INTO \"table_meta\" (table_name, column_name, column_display_name, created_date, updated_date, is_key)");
            stringBuilder.append(" VALUES ( ?1, ?2, ?3, ?4, ?5, ?6);");
            Query query = entityManager.createNativeQuery(stringBuilder.toString());
            query.setParameter(1, taleName);
            query.setParameter(2, columnName);
            query.setParameter(3, header);
            query.setParameter(4, new Date());
            query.setParameter(5, new Date());
            query.setParameter(6, keys.contains(header));
            query.executeUpdate();
        }
    }

    public boolean checkColumnExist(String tableName, String columnName) {
        boolean result = true;
        var queryStr = "SELECT count(id) FROM \"table_meta\" WHERE table_name = ?1 AND column_name = ?2";
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

    public void upsertRow(String tableName, List<String> keys, Map<String, Object> rowMap) throws Exception {
        log.info("tableName: {}, keys: {}, rowMap: {}", tableName, keys, rowMap);
        if (StringUtils.isEmpty(tableName) || CollectionUtils.isEmpty(keys) || MapUtils.isEmpty(rowMap)) {
            throw new Exception("##upsertRow##: Invalid params");
        }

        List<String> columnNames = new ArrayList<>();
        List<String> values = new ArrayList<>();
        rowMap.forEach((k, v) -> {
            columnNames.add(k.toLowerCase().replace(" ", "_"));
            values.add((String) v);
        });

        StringBuilder stringBuilder = new StringBuilder();
        if (isRowExist(tableName, keys, rowMap)) {
            stringBuilder.append("UPDATE ").append(tableName).append(" SET");
            for (int i = 0; i < rowMap.size(); i++) {
                var isLastItem = i == rowMap.keySet().size() - 1;
                stringBuilder.append(String.format(" %s = ?" + (!isLastItem ? "," : ""), columnNames.get(i)));
            }
            stringBuilder.append(" ,updated_date = ?");
            stringBuilder.append(" WHERE");
            for (int i = 0; i < keys.size(); i++) {
                var key = CommonUtils.convertToPostgresColumnName(keys.get(i));
                var isLastItem = i == keys.size() - 1;
                if (isLastItem) {
                    stringBuilder.append(String.format(" %s = '%s'", key, rowMap.get(keys.get(i))));
                } else {
                    stringBuilder.append(String.format(" %s = '%s' AND", key, rowMap.get(keys.get(i))));
                }
            }

            Query query = entityManager.createNativeQuery(stringBuilder.toString());
            var lastIndex = 0;
            for (int i = 0; i < values.size(); i++) {
                query.setParameter(i + 1, values.get(i));
                lastIndex = i + 2;
            }
            query.setParameter(lastIndex, new Date());
            query.executeUpdate();
        } else {
            var strColumnName = StringUtils.join(columnNames, ",");
            stringBuilder.append(String.format("INSERT INTO \"%s\"", tableName));
            stringBuilder.append(String.format(" (%s", strColumnName));
            stringBuilder.append(" ,created_date");
            stringBuilder.append(" ,updated_date)");
            stringBuilder.append(" VALUES");
            stringBuilder.append("(");
            for (int i = 0; i < values.size(); i++) {
                var isLastItem = values.size() - 1 == i;
                stringBuilder.append("?").append(!isLastItem ? "," : "");
            }
            stringBuilder.append(" , ?");
            stringBuilder.append(" , ?");
            stringBuilder.append(")");
            Query query = entityManager.createNativeQuery(stringBuilder.toString());
            var lastIndex = 0;
            for (int i = 0; i < values.size(); i++) {
                query.setParameter(i + 1, values.get(i));
                lastIndex = i + 1;
            }
            query.setParameter(lastIndex + 1, new Date());
            query.setParameter(lastIndex + 2, new Date());
            query.executeUpdate();
        }

    }

    public boolean isTableExist(String tableName) {
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

    public boolean isRowExist(String tableName, List<String> keys, Map<String, Object> rowMap) throws Exception {
        if (ObjectUtils.anyNull(tableName, keys, rowMap)) {
            throw new Exception("Invalid params");
        }
        log.info(String.format("isRowExist: %s, %s ,%s", tableName, keys, rowMap));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("SELECT count(*) FROM \"%s\"", tableName));
        stringBuilder.append(" WHERE");
        for (int i = 0; i < keys.size(); i++) {
            var isLastItem = i == keys.size() - 1;
            var columnName = CommonUtils.convertToPostgresColumnName(keys.get(i));
            if (isLastItem) {
                stringBuilder.append(String.format(" \"%s\" = '%s'", columnName, rowMap.get(keys.get(i))));
            } else {
                stringBuilder.append(String.format(" \"%s\" = '%s' AND", columnName, rowMap.get(keys.get(i))));
            }

        }
        keys.forEach(k -> {


        });
        var count = (BigInteger) entityManager.createNativeQuery(stringBuilder.toString()).getSingleResult();
        return count.intValue() > 0;
    }
}

//todo luon them updated date khi update
//todo implement log
