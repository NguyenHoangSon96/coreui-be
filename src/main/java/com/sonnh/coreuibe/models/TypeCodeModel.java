package com.sonnh.coreuibe.models;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;

@Entity
@Data
@Table(name = "type_code", catalog = "coreui")
public class TypeCodeModel implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;
    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "code_string")
    private String codeString;
    @Basic
    @Column(name = "mass_editable")
    private Boolean massEditable;
    @Basic
    @Column(name = "class_name")
    private String className;
    @Basic
    @Column(name = "bulk_loadable")
    private Boolean bulkLoadable;
    @Basic
    @Column(name = "created_date")
    private Date createdDate;
    @Basic
    @Column(name = "updated_date")
    private Date updatedDate;

}
