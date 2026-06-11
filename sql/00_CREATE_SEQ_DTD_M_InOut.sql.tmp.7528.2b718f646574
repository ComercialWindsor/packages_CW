-- =============================================================================
-- Script: Crear SEQ_DTD_M_InOut - Secuencia nativa Oracle para DocumentNo
-- Package: packages_CW
-- Propósito: Optimizar generación masiva de M_InOut (despachos) sin lock AD_Sequence
--
-- ANALISIS DATOS PRODUCCION (windsorp, 2026-06-03):
-- - M_InOut total: 713,777 registros
-- - Serie operacional despachos (MMS) 2026: max 318,871 (16,006 docs)
-- - Max historico global 1-7 digitos: 3,157,534
--
-- ANOMALIAS (no serie operacional, entradas especiales/manuales):
-- - 10000000-10000004, 10000511-10000514 (MMR antiguos 2002-2011)
-- - 31568646 (1 MMR draft 2026)
-- - 118925031-118925063 (23 MMS, serie especial 2018+2026)
-- - 590039319 (1 MMS 2014)
-- - 1.18-1.19 billones / 13-dig (8 MMS era 2018)
--
-- START WITH 4,200,000:
-- - Por encima del max historico 7-dig (3,157,534) + ~1M gap de seguridad
-- - Zona limpia: 0 colisiones entre 4.2M y bloque 10M
-- - Runway hasta 10M = ~5.8M docs (siglos al volumen actual ~16k/anno)
-- =============================================================================

CREATE SEQUENCE SEQ_DTD_M_InOut
  START WITH 4200000
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 999999999
  NOCYCLE
  CACHE 20
  ORDER;

COMMIT;

-- Verificación
SELECT sequence_name, last_number, cache_size FROM user_sequences
WHERE sequence_name = 'SEQ_DTD_M_INOUT';
