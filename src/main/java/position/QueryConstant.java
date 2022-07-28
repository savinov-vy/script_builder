package position;

public class QueryConstant {
    public static final String BEGIN_POSITION_QUERY = "CREATE FUNCTION get_renamed_positions_employee_ids(old_position_name text, new_position_name text)\n" +
            "    RETURNS TABLE (id uuid)\n" +
            "AS\n" +
            "$$\n" +
            "UPDATE covidinfo_employee\n" +
            "SET position   = new_position_name,\n" +
            "    update_ts  = now(),\n" +
            "    updated_by = 'admin'\n" +
            "WHERE position = old_position_name\n" +
            "RETURNING id;\n" +
            "$$ LANGUAGE sql;\n" +
            "\n" +
            "CREATE FUNCTION get_renamed_positions_certificate_ids(old_position_name text, new_position_name text)\n" +
            "    RETURNS TABLE (id uuid)\n" +
            "AS\n" +
            "$$\n" +
            "UPDATE covidinfo_medical_certificate\n" +
            "SET employee_position = new_position_name,\n" +
            "    update_ts         = now(),\n" +
            "    updated_by        = 'admin'\n" +
            "WHERE employee_position = old_position_name\n" +
            "RETURNING id;\n" +
            "$$ LANGUAGE sql;\n" +
            "\n" +
            "CREATE FUNCTION insert_log_employee(old_position_name text, new_position_name text, ids uuid[]) returns void AS\n" +
            "$$\n" +
            "DECLARE\n" +
            "    entityId uuid;\n" +
            "    logId    uuid;\n" +
            "BEGIN\n" +
            "    FOREACH entityId in array ids\n" +
            "        LOOP\n" +
            "            logId = newid();\n" +
            "            insert into covidinfo_changes_log_entity (id, version, create_ts, created_by, update_ts, type, entity_name,\n" +
            "                                                      entity_id)\n" +
            "            values (logId, 1, now(), 'admin', now(), 'change',\n" +
            "                    'covidinfo_Employee', entityId);\n" +
            "\n" +
            "            insert into covidinfo_attribute_change_entity (id, property, new_value, old_value, log_entity_id)\n" +
            "            values (newid(), 'position', new_position_name, old_position_name, logId);\n" +
            "        END LOOP;\n" +
            "END;\n" +
            "$$ LANGUAGE plpgsql;\n" +
            "\n" +
            "CREATE FUNCTION insert_log_certificate(old_position_name text, new_position_name text, ids uuid[]) returns void AS\n" +
            "$$\n" +
            "DECLARE\n" +
            "    entityId uuid;\n" +
            "    logId    uuid;\n" +
            "BEGIN\n" +
            "    FOREACH entityId in array ids\n" +
            "        LOOP\n" +
            "            logId = newid();\n" +
            "            insert into covidinfo_changes_log_entity (id, version, create_ts, created_by, update_ts, type, entity_name,\n" +
            "                                                      entity_id)\n" +
            "            values (logId, 1, now(), 'admin', now(), 'change',\n" +
            "                    'covidinfo_MedicalCertificate', entityId);\n" +
            "            insert into covidinfo_attribute_change_entity (id, property, new_value, old_value, log_entity_id)\n" +
            "            values (newid(), 'employeePosition', new_position_name, old_position_name, logId);\n" +
            "        END LOOP;\n" +
            "END;\n" +
            "$$ LANGUAGE plpgsql;\n" +
            "\n" +
            "CREATE FUNCTION renamePosition(old_position_name text, new_position_name text) returns void AS\n" +
            "$$\n" +
            "DECLARE\n" +
            "    changedPositionEmployees uuid[];\n" +
            "    changedPositionCertificates uuid[];\n" +
            "BEGIN\n" +
            "    changedPositionEmployees :=\n" +
            "            ARRAY(SELECT * FROM get_renamed_positions_employee_ids(old_position_name, new_position_name));\n" +
            "    PERFORM insert_log_employee(old_position_name, new_position_name, changedPositionEmployees);\n" +
            "\n" +
            "    changedPositionCertificates :=\n" +
            "            ARRAY(SELECT * FROM get_renamed_positions_certificate_ids(old_position_name, new_position_name));\n" +
            "    PERFORM insert_log_certificate(old_position_name, new_position_name, changedPositionCertificates);\n" +
            "END;\n" +
            "$$ language plpgsql;\n" +
            "\n" +
            "DO\n" +
            "$$\n" +
            "    BEGIN\n";

    public static final String END_POSITION_QUERY = ";\nEND;\n" +
            "$$;\n" +
            "\n" +
            "drop function get_renamed_positions_employee_ids;\n" +
            "drop function get_renamed_positions_certificate_ids;\n" +
            "drop function insert_log_employee;\n" +
            "drop function insert_log_certificate;\n" +
            "drop function renamePosition;";



    public static final String BEGIN_DEPARTMENT_QUERY = "CREATE FUNCTION insert_log_department(department_name text, employee_id uuid) returns void AS\n" +
            "$$\n" +
            "DECLARE    logId uuid;\n" +
            "BEGIN\n" +
            "    logId = newid();\n" +
            "    insert into covidinfo_changes_log_entity (id, version, create_ts, created_by, update_ts, type, entity_name,\n" +
            "                                              entity_id)\n" +
            "    values (logId, 1, now(), 'admin', now(), 'change',\n" +
            "            'covidinfo_Employee', employee_id);\n" +
            "\n" +
            "    insert into covidinfo_attribute_change_entity (id, property, new_value, log_entity_id)\n" +
            "    values (newid(), 'department', department_name, logId);\n" +
            "END;\n" +
            "$$ LANGUAGE plpgsql;\n" +
            "\n" +
            "CREATE FUNCTION update_department_employee(set_department_id uuid, set_employee_id uuid) returns void AS\n" +
            "$$\n" +
            "BEGIN\n" +
            "    update covidinfo_employee ce\n" +
            "    set department_id = set_department_id\n" +
            "    where ce.id = set_employee_id;\n" +
            "END;\n" +
            "$$ LANGUAGE plpgsql;\n" +
            "\n" +
            "CREATE FUNCTION update_certificates_employee(old_position_employee text, update_employee uuid,\n" +
            "                                             set_department_id uuid)\n" +
            "    RETURNS TABLE (id uuid)\n" +
            "AS\n" +
            "$$\n" +
            "update covidinfo_medical_certificate\n" +
            "set employee_department_id = set_department_id\n" +
            "where employee_id = update_employee\n" +
            "  and employee_position = old_position_employee\n" +
            "RETURNING id;\n" +
            "$$ LANGUAGE sql;\n" +
            "\n" +
            "CREATE FUNCTION insert_log_certificate(new_department_name text, ids uuid[]) returns void AS\n" +
            "$$\n" +
            "DECLARE\n" +
            "    entityId uuid;\n" +
            "    logId    uuid;\n" +
            "BEGIN\n" +
            "    FOREACH entityId in array ids\n" +
            "        LOOP\n" +
            "            logId = newid();\n" +
            "            insert into covidinfo_changes_log_entity (id, version, create_ts, created_by, update_ts, type, entity_name,\n" +
            "                                                      entity_id)\n" +
            "            values (logId, 1, now(), 'admin', now(), 'change',\n" +
            "                    'covidinfo_MedicalCertificate', entityId);\n" +
            "            insert into covidinfo_attribute_change_entity (id, property, new_value, log_entity_id)\n" +
            "            values (newid(), 'employeeDepartment', new_department_name, logId);\n" +
            "        END LOOP;\n" +
            "END;\n" +
            "$$ LANGUAGE plpgsql;\n" +
            "\n" +
            "CREATE FUNCTION insertDepartmentToEmployee(old_position_employee text, short_name_employee_department text) RETURNS void AS\n" +
            "$$\n" +
            "DECLARE\n" +
            "    TMP_TABLE RECORD;\n" +
            "    changedDepartmentCertificates uuid[];\n" +
            "BEGIN\n" +
            "    FOR TMP_TABLE IN\n" +
            "        SELECT ce.id       as employee_id,\n" +
            "               ce.position as employee_position,\n" +
            "               cd.id       as department_id,\n" +
            "               cd.name     as department_name\n" +
            "        from covidinfo_employee ce\n" +
            "                 join covidinfo_organization co on ce.organization_id = co.id\n" +
            "                 join covidinfo_department cd on cd.organization_id = co.id\n" +
            "        where ce.position = old_position_employee\n" +
            "          and cd.name like short_name_employee_department\n" +
            "          and ce.department_id is null\n" +
            "        LOOP\n" +
            "            perform update_department_employee(TMP_TABLE.department_id, TMP_TABLE.employee_id);\n" +
            "            perform insert_log_department(TMP_TABLE.department_name, TMP_TABLE.employee_id);\n" +
            "\n" +
            "\n" +
            "            changedDepartmentCertificates :=\n" +
            "                    ARRAY(SELECT * FROM update_certificates_employee(TMP_TABLE.employee_position, TMP_TABLE.employee_id,\n" +
            "                                                                      TMP_TABLE.department_id));\n" +
            "            PERFORM insert_log_certificate( TMP_TABLE.department_name, changedDepartmentCertificates);\n" +
            "        END LOOP;\n" +
            "END;\n" +
            "$$ LANGUAGE plpgsql;\n" +
            "\n" +
            "DO\n" +
            "$$\n" +
            "    BEGIN\n";


    public static final String END_DEPARTMENT_QUERY = ";\n" +
            "    END;\n" +
            "$$;\n" +
            "\n" +
            "drop function insert_log_department;\n" +
            "drop function update_department_employee;\n" +
            "drop function update_certificates_employee;\n" +
            "drop function insert_log_certificate;\n" +
            "drop function insertDepartmentToEmployee;";
}
