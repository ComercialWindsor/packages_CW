================================================================================
INSTALACION: Native Oracle Sequences para M_InOut DocumentNo
Package: packages_CW
Fecha: 2026-06-03
================================================================================

OBJETIVO:
---------
Optimizar generacion masiva de M_InOut usando secuencias nativas de Oracle
en lugar de AD_Sequence (que causa locks y contenciones).

MODIFICACIONES CODIGO:
-----------------------
1. packages_CW/src/org/compiere/model/MSequence.java
   - Agregado metodo: getNativeSequenceDocumentNo()
   - Modificado: getDocumentNo() para intentar secuencia nativa PRIMERO
   - Fallback automatico a AD_Sequence si secuencia nativa no existe

NOMENCLATURA SECUENCIAS:
------------------------
SEQ_DTD_{TableName}              - Secuencia generica por tabla
SEQ_DTD_{TableName}_{DocTypeKey} - Secuencia por tipo de documento

Ejemplos M_InOut:
- SEQ_DTD_M_InOut    - Generica (fallback)
- SEQ_DTD_M_InOut_SO - Sales Order Shipments
- SEQ_DTD_M_InOut_PO - Purchase Order Receipts
- SEQ_DTD_M_InOut_MM - Material Movements

INSTRUCCIONES INSTALACION:
---------------------------
PASO 1: Ejecutar DDL
  sqlplus admin@BD @packages_CW/sql/01_CREATE_SEQ_DTD_M_InOut.sql

PASO 2: Verificar secuencias creadas
  sqlplus admin@BD @packages_CW/sql/02_VERIFY_SEQ_DTD_M_InOut.sql

  Debe mostrar:
  - SEQ_DTD_M_InOut con LAST_NUMBER=1000000
  - SEQ_DTD_M_InOut_SO con LAST_NUMBER=1000000
  - SEQ_DTD_M_InOut_PO con LAST_NUMBER=1000000
  - SEQ_DTD_M_InOut_MM con LAST_NUMBER=1000000

PASO 3: Compilar packages_CW
  - Recompilar todas clases de packages_CW
  - El archivo MSequence.java tiene cambios

PASO 4: Validar funcionamiento
  - Generar shipment masivo (ej: 100 documentos)
  - Monitorear que DocumentNo comience en 1000000+
  - Verificar en BD_LOGS que usa SEQ_DTD_M_InOut

MONITOREO:
----------
Ver last_number actual:
  SELECT last_number FROM user_sequences
  WHERE sequence_name='SEQ_DTD_M_InOut';

Ver documentos generados con secuencia nativa:
  SELECT M_InOut_ID, DocumentNo FROM M_InOut
  WHERE M_InOut_ID >= 1000000
  ORDER BY Created DESC
  LIMIT 100;

ROLLBACK (si necesario):
------------------------
DROP SEQUENCE SEQ_DTD_M_InOut;
DROP SEQUENCE SEQ_DTD_M_InOut_SO;
DROP SEQUENCE SEQ_DTD_M_InOut_PO;
DROP SEQUENCE SEQ_DTD_M_InOut_MM;

REVERT codigo:
- Revertir MSequence.java a version original de adempiere_360
- O simplemente no usar packages_CW (usar adempiere_360 base nuevamente)

PERFORMANCE ESPERADO:
---------------------
ANTES (AD_Sequence):
- Generacion 500 m_inout: ~5-10 minutos (locks en AD_Sequence)
- Query time: ~100-200ms por documento
- Contenciones de lock frecuentes

DESPUES (Secuencias nativas):
- Generacion 500 m_inout: ~1-2 minutos
- Query time: ~5-10ms por documento
- Sin locks, sin contenciones

REDUCCION: 60-80% mejora en tiempo de generacion masiva

================================================================================
