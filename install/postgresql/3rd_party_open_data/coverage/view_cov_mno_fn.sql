BEGIN;
CREATE MATERIALIZED VIEW cov_mno_fn AS
  SELECT 
operator,
'BBfixed' AS reference,
'CCBY4.0 BMLRT' AS license,
substr(date,0,11) AS rfc_date,
raster, 
NULL AS dl_normal,
NULL AS ul_normal, 
ROUND(dl_max_mbit*1000000)::BIGINT AS dl_max,
ROUND(ul_max_mbit*1000000)::BIGINT AS ul_max,
technology

FROM cov_bb_fixed
UNION
SELECT operator,reference,CONCAT(license,' ',operator) license,rfc_date,raster,dl_normal,ul_normal,dl_max,ul_max,
'5G' AS technology FROM cov_mno where reference='F7/16'
UNION
SELECT operator,reference,CONCAT(license,' ',operator) license,rfc_date,raster,dl_normal,ul_normal,dl_max,ul_max,
'mobile' AS technology FROM cov_mno where reference='F1/16';
ANALYZE cov_mno_fn;
CREATE INDEX cov_mno_fn_raster_idx
    ON  cov_mno_fn(raster);
COMMIT;

