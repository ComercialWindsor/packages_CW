-- =============================================================================
-- Script: Create native Oracle sequences for M_InOut DocumentNo generation
-- Package: packages_CW
-- Purpose: Optimize DocumentNo generation for M_InOut by using native Oracle sequences
--          instead of AD_Sequence table locks
-- =============================================================================

-- Drop sequences if they exist (comment out for production after first run)
-- DROP SEQUENCE SEQ_DTD_M_InOut;
-- DROP SEQUENCE SEQ_DTD_M_InOut_SO;
-- DROP SEQUENCE SEQ_DTD_M_InOut_PO;
-- DROP SEQUENCE SEQ_DTD_M_InOut_MM;

-- Main M_InOut sequence (fallback for any document type)
-- Start from 1000000 to avoid collisions with existing IDs
CREATE SEQUENCE SEQ_DTD_M_InOut
  START WITH 1000000
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 999999999
  NOCYCLE
  CACHE 20
  ORDER;

-- Sales Order Shipments
CREATE SEQUENCE SEQ_DTD_M_InOut_SO
  START WITH 1000000
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 999999999
  NOCYCLE
  CACHE 20
  ORDER;

-- Purchase Order Shipments (Receipts)
CREATE SEQUENCE SEQ_DTD_M_InOut_PO
  START WITH 1000000
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 999999999
  NOCYCLE
  CACHE 20
  ORDER;

-- Material Movement
CREATE SEQUENCE SEQ_DTD_M_InOut_MM
  START WITH 1000000
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 999999999
  NOCYCLE
  CACHE 20
  ORDER;

COMMIT;

-- =============================================================================
-- Verification queries
-- =============================================================================
-- Check sequences created:
-- SELECT sequence_name, last_number FROM user_sequences WHERE sequence_name LIKE 'SEQ_DTD%';

-- Test getting next value:
-- SELECT SEQ_DTD_M_InOut.NEXTVAL FROM DUAL;
-- SELECT SEQ_DTD_M_InOut_SO.NEXTVAL FROM DUAL;
