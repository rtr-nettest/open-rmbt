CREATE OR REPLACE FUNCTION public.rmbt_get_distance_iso_a2(point geometry,a2 varchar(5))
 RETURNS float
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
DECLARE
  -- ISO3266 two digit country code (e.g. 'US')
  distance float;
BEGIN
    -- returns the distance in meter (m) betweeen location in WGS84 (EPSG:4236) and a two digit country code (e.g. 'US')
	
	-- Example query: select rmbt_get_distance_iso_a2(st_setsrid(ST_GeomFromText('POINT(-71.064544 42.28787)'),4326),'CA');
 		
	return  ST_DistanceSpheroid(point,(select geom from admin_0_countries ac where iso_a2=a2),'SPHEROID["WGS 84",6378137,298.257223563]');
    
END;
$function$
;
