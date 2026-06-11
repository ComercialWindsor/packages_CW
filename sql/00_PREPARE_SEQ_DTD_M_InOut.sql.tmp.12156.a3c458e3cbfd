-- =============================================================================
-- Script: Crear SEQ_DTD_M_InOut - Secuencias nativas Oracle para DocumentNo
-- Package: packages_CW
-- Propósito: Optimizar generación masiva de M_InOut usando secuencias nativas
-- START WITH: 1000000 (gap seguro contra documentos existentes)
-- =============================================================================

-- Verificación pre-creación (opcional)
SELECT COUNT(*) as total_m_inout FROM M_InOut;
SELECT MAX(DocumentNo) as max_documentno_actual FROM M_InOut;

-- =============================================================================
-- CREAR SECUENCIAS
-- =============================================================================

CREATE SEQUENCE SEQ_DTD_M_InOut
  START WITH 1000000
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 999999999
  NOCYCLE
  CACHE 20
  ORDER;

CREATE SEQUENCE SEQ_DTD_M_InOut_SO
  START WITH 1000000
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 999999999
  NOCYCLE
  CACHE 20
  ORDER;

CREATE SEQUENCE SEQ_DTD_M_InOut_PO
  START WITH 1000000
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 999999999
  NOCYCLE
  CACHE 20
  ORDER;

CREATE SEQUENCE SEQ_DTD_M_InOut_MM
  START WITH 1000000
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 999999999
  NOCYCLE
  CACHE 20
  ORDER;

COMMIT;

-- Verificación post-creación
SELECT sequence_name, last_number FROM user_sequences
WHERE sequence_name LIKE 'SEQ_DTD%'
ORDER BY sequence_name;

