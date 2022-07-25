public class QueryConstant {
    public static final String BEGIN_QUERY = "CREATE FUNCTION get_renamed_positions_employee_ids(old_position_name text, new_position_name text)\n" +
            "    RETURNS TABLE (id uuid)\n" +
            "AS\n" +
            "$$\n" +
            "UPDATE covidinfo_employee\n" +
            "SET position   = new_position_name,\n" +
            "    update_ts  = now() + interval '8 hour',\n" +
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
            "    update_ts         = now() + interval '8 hour',\n" +
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
            "            values (logId, 1, now() + interval '8 hour', 'admin', now() + interval '8 hour', 'change',\n" +
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
            "            values (logId, 1, now() + interval '8 hour', 'admin', now() + interval '8 hour', 'change',\n" +
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



    public static final String END_QUERY = ";\nEND;\n" +
            "$$;\n" +
            "\n" +
            "drop function get_renamed_positions_employee_ids;\n" +
            "drop function get_renamed_positions_certificate_ids;\n" +
            "drop function insert_log_employee;\n" +
            "drop function insert_log_certificate;\n" +
            "drop function renamePosition;";
}
