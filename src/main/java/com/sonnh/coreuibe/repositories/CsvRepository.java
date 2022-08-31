package com.sonnh.coreuibe.repositories;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Map;

@Repository
@Transactional
public class CsvRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void createTableDynamic(String tableName, Map<String, String> columnMap) throws Exception {
        if (StringUtils.isEmpty(tableName) || columnMap == null) {
            throw new Exception("Invalid Param");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("DROP TABLE IF EXISTS \"%s\" ;", tableName));
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
        var isColumnExist = checkColumnExist(taleName, columnMap);
        if (isColumnExist) {
            throw new Exception("Column already exists");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO \"TableMeta\" (table_name, column_name, column_display_name, created_date, updated_date)");
        stringBuilder.append("VALUES ( ?1, ?2, ?3, ?4, ?5 );");
        columnMap.forEach((columnName, colomnDisplayName) -> {
            Query query = entityManager.createNativeQuery(stringBuilder.toString());
            query.setParameter(1, taleName);
            query.setParameter(2, columnName);
            query.setParameter(3, colomnDisplayName);
            query.setParameter(4, new Date());
            query.setParameter(5, new Date());
            query.executeUpdate();
        });


    }

    public boolean checkColumnExist(String tableName, Map<String, String> columnMap) throws Exception {
        boolean result = false;
        var columnNames = columnMap.keySet();
        for (String columnName : columnNames) {
            var queryStr = "SELECT id FROM \"TableMeta\" WHERE table_name = ?1 AND column_name = ?2";
            Query nativeQuery = entityManager.createNativeQuery(queryStr);
            nativeQuery.setParameter(1, tableName);
            nativeQuery.setParameter(2, columnName);
            var rows = nativeQuery.getResultList();
            if (CollectionUtils.isNotEmpty(rows)) {
                result = true;
                break;
            }
        }

        return result;
    }
}
//todo luon them updated date khi update
//todo implement log
